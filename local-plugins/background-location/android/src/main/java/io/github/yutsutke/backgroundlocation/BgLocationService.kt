package io.github.yutsutke.backgroundlocation

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.Locale

/**
 * 位置記録の本体＝通知つき前面サービス (FGS type=location)。iOS の BackgroundLocationManager に相当。
 *
 * iOS との対応:
 *  - important (重要な移動のみ) = iOS の SLC 相当 → balanced power / 500m 動いたら1点 (電池にやさしい)
 *  - frequent  (こまめ)         = iOS の distanceFilter=25 相当 → high accuracy / 25m 動いたら1点
 *  - 「アプリを閉じても記録」= このサービスが生き続けることで成立 (ACCESS_BACKGROUND_LOCATION 不要:
 *    FGS が前面扱いなので while-in-use 権限だけで位置が取れる)。ホームに引っ込めても・最近のアプリから
 *    スワイプで消しても (素の Android では) 動き続ける。※「設定→アプリ→強制停止」だけは止まる —
 *    その場合も次にアプリを開いた時にプラグインの load()/JS の startLogger() が再アームする。
 *  - OS に殺されたら START_STICKY で再起動し、保存モードから復元 (intent=null 分岐)。
 *
 * 記録点は BgLocationStore (SharedPreferences) にバッファし、JS が drain して track ストアへ合流させる
 * (座標+時刻のみ・端末内だけ・外部送信なし)。
 */
class BgLocationService : Service() {

    companion object {
        private const val CHANNEL_ID = "bgloc"
        private const val NOTIF_ID = 7301
        const val EXTRA_MODE = "mode"

        fun hasLocationPermission(ctx: Context): Boolean =
            ctx.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ctx.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        /** サービス起動 (モード変更も同じ経路 = onStartCommand が冪等に適用)。 */
        fun start(ctx: Context, mode: String) {
            val i = Intent(ctx, BgLocationService::class.java).putExtra(EXTRA_MODE, mode)
            try {
                if (Build.VERSION.SDK_INT >= 26) ctx.startForegroundService(i) else ctx.startService(i)
            } catch (_: Exception) {
                // 背景からの FGS 起動制限など。次にアプリが前面に来た時の start で再アームされる
            }
        }

        fun stop(ctx: Context) {
            try { ctx.stopService(Intent(ctx, BgLocationService::class.java)) } catch (_: Exception) {}
        }
    }

    private var fused: FusedLocationProviderClient? = null
    private var callback: LocationCallback? = null
    private var activeMode: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // STICKY 再起動 (intent=null) は保存モードから復元 = OS の kill から「閉じても記録」を守る
        val mode = intent?.getStringExtra(EXTRA_MODE) ?: BgLocationStore.mode(this)
        if ((mode != "important" && mode != "frequent") || !hasLocationPermission(this)) {
            stopSelf()
            return START_NOT_STICKY
        }
        try {
            goForeground(mode)
        } catch (_: Exception) {
            // API 31+/34+ の FGS 起動制限で弾かれたら静かに諦める (クラッシュループにしない)
            stopSelf()
            return START_NOT_STICKY
        }
        if (mode != activeMode) {
            requestUpdates(mode)
            activeMode = mode
        }
        return START_STICKY
    }

    override fun onDestroy() {
        removeUpdates()
        super.onDestroy()
    }

    // ---------------- 位置更新 ----------------

    @SuppressLint("MissingPermission")   // 呼び出し前に hasLocationPermission を確認済み
    private fun requestUpdates(mode: String) {
        removeUpdates()
        val client = fused ?: LocationServices.getFusedLocationProviderClient(this).also { fused = it }
        val req = if (mode == "important") {
            // iOS SLC 相当: 基地局/Wi-Fi 精度・500m 動いたら1点・電池ほぼ無コスト
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 180_000L)
                .setMinUpdateIntervalMillis(60_000L)
                .setMinUpdateDistanceMeters(500f)
                .build()
        } else {
            // iOS distanceFilter=25 相当: 高精度・25m 動いたら1点
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 20_000L)
                .setMinUpdateIntervalMillis(10_000L)
                .setMinUpdateDistanceMeters(25f)
                .build()
        }
        val cb = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    BgLocationStore.append(this@BgLocationService, loc.latitude, loc.longitude, loc.accuracy, loc.time)
                }
            }
        }
        callback = cb
        try {
            client.requestLocationUpdates(req, cb, Looper.getMainLooper())
            // 開始直後の1点 (web の「開いた瞬間の1点」/ iOS の即時 fix と同じ手触り。静止中のデスクテストでも点が出る)
            client.getCurrentLocation(
                if (mode == "frequent") Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).addOnSuccessListener { loc ->
                if (loc != null) BgLocationStore.append(this, loc.latitude, loc.longitude, loc.accuracy, loc.time)
            }
        } catch (_: SecurityException) { /* 権限が同時に剥がされた場合 — 次の start で再アーム */ }
    }

    private fun removeUpdates() {
        val cb = callback ?: return
        callback = null
        try { fused?.removeLocationUpdates(cb) } catch (_: Exception) {}
    }

    // ---------------- 常駐通知 ----------------

    private fun goForeground(mode: String) {
        val ja = Locale.getDefault().language == "ja"
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                CHANNEL_ID,
                if (ja) "位置ロガー" else "Location logger",
                NotificationManager.IMPORTANCE_LOW   // 音なし・さりげなく
            )
            ch.setShowBadge(false)
            nm.createNotificationChannel(ch)
        }
        val title = if (ja) "位置を記録中" else "Logging location"
        val text = if (mode == "frequent") {
            if (ja) "🛰️ こまめに（端末内だけ・外部送信なし）" else "🛰️ Frequent (on-device only, nothing sent out)"
        } else {
            if (ja) "🚶 重要な移動のみ（端末内だけ・外部送信なし）" else "🚶 Major moves only (on-device only, nothing sent out)"
        }
        val builder = if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(this)
        }
        builder.setSmallIcon(R.drawable.ic_stat_bgloc)
            .setContentTitle(title)
            .setContentText(text)
            .setOngoing(true)
        packageManager.getLaunchIntentForPackage(packageName)?.let { launch ->
            builder.setContentIntent(
                PendingIntent.getActivity(this, 0, launch, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
        }
        val notif = builder.build()
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIF_ID, notif)
        }
    }
}
