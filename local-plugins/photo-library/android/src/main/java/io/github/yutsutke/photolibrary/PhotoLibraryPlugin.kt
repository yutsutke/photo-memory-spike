package io.github.yutsutke.photolibrary

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PermissionState
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * 自前 Photos プラグインの Android 実装 (Approach B の MediaStore 版)。
 * iOS (PhotoLibraryPlugin.swift) と同じ契約:
 *   requestAccess() -> { status: authorized|limited|denied|restricted|notDetermined }
 *   enumerate({limit}) -> { count, returned, ms, items: [{id, w, h, date?, dateSource?, lat?, lng?}] }
 *   thumbnail({id, size}) -> { dataUrl, lat?, lng? }
 *
 * iOS との本質差 (web 層は importOneNative の GPS マージ以外は無改修):
 *  - 日時: MediaStore.DATE_TAKEN (EXIF 由来, ms) が無い写真は DATE_ADDED (保存日, 秒) に
 *    フォールバックし dateSource='saved' を返す (このアプリの日時純度 EXIF>保存日 の流儀)。
 *  - GPS: API 29+ の MediaStore は EXIF 位置を隠す (LATITUDE/LONGITUDE 列は常に null)。
 *    全件列挙で 2000 ファイルを開くと「即時列挙」が死ぬため、enumerate は GPS を返さず、
 *    thumbnail (どうせファイルを開く) のついでに ACCESS_MEDIA_LOCATION + setRequireOriginal で
 *    EXIF から読み、{lat, lng} を dataUrl に同乗させる。JS 側で record にマージ。
 *  - 権限: 33+=READ_MEDIA_IMAGES / 34+=部分許可(READ_MEDIA_VISUAL_USER_SELECTED)→limited /
 *    <=32=READ_EXTERNAL_STORAGE。既に authorized/limited なら再リクエストしない
 *    (Android 14 の部分許可は再リクエストのたびに選択ダイアログが出る → 起動時 auto-import が
 *    毎回ダイアログを出す事故を防ぐ。iOS の「一度決まったら再プロンプトしない」と同じ挙動)。
 */
@CapacitorPlugin(
    name = "PhotoLibrary",
    permissions = [
        Permission(alias = "photosLegacy", strings = [Manifest.permission.READ_EXTERNAL_STORAGE]),
        Permission(alias = "photos13", strings = ["android.permission.READ_MEDIA_IMAGES"]),
        Permission(
            alias = "photos14",
            strings = [
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.READ_MEDIA_VISUAL_USER_SELECTED"
            ]
        ),
        Permission(alias = "mediaLocation", strings = ["android.permission.ACCESS_MEDIA_LOCATION"]),
        Permission(alias = "camera", strings = [Manifest.permission.CAMERA])
    ]
)
class PhotoLibraryPlugin : Plugin() {

    // サムネ生成/列挙は I/O なので専用シングルスレッドで (JS 側は直列に 8ms 間隔で呼ぶ)
    private val executor = Executors.newSingleThreadExecutor()

    // v228: バッチ (thumbnails) 用の並列デコードプール。古い端末＝遅い CPU でもコアは複数ある
    // ことが多く、直列デコードはコアを1つしか使えていなかった。コア数-1 (2..4) で並列に。
    // 調整(バッチの受付と順序)は従来の executor が担い、デコードだけをこのプールへ farm out する。
    private val thumbPool = Executors.newFixedThreadPool(
        maxOf(2, minOf(4, Runtime.getRuntime().availableProcessors() - 1))
    )

    override fun handleOnDestroy() {
        executor.shutdown()
        thumbPool.shutdown()
    }

    // ---------------- requestAccess ----------------

    @PluginMethod
    fun requestAccess(call: PluginCall) {
        val cur = currentStatus()
        if (cur == "authorized" || cur == "limited") {
            // 一度決まった状態では再プロンプトしない (iOS と同じ・クラス冒頭コメント参照)
            call.resolve(JSObject().put("status", cur))
            return
        }
        val aliases = if (Build.VERSION.SDK_INT >= 29) {
            // ACCESS_MEDIA_LOCATION は独自ダイアログを出さない (メディア許可に同乗して付与)
            arrayOf(primaryAlias(), "mediaLocation")
        } else {
            arrayOf(primaryAlias())
        }
        requestPermissionForAliases(aliases, call, "accessCallback")
    }

    @PermissionCallback
    private fun accessCallback(call: PluginCall) {
        val st = currentStatus()
        // リクエスト直後の非許可は denied 扱い (iOS の requestAuthorization 後と同じ語彙)
        call.resolve(JSObject().put("status", if (st == "notDetermined") "denied" else st))
    }

    // ---------------- requestCameraAccess / openAppSettings (v215) ----------------
    // 🎞️ 重ね撮り (getUserMedia) 用。Capacitor 8 の BridgeWebChromeClient は CAMERA が
    // manifest 宣言済みならランタイム権限→grant を自動処理するはずだが、実機 (v208/vc2 FB) で
    // ダイアログが出ず Permission denied になるケースが出た＝一度拒否が残ると回復導線がない。
    // → getUserMedia の前にアプリ自身が権限を確保する (授与済みなら即 granted・ダイアログなし)。

    @PluginMethod
    fun requestCameraAccess(call: PluginCall) {
        if (hasPerm(Manifest.permission.CAMERA)) {
            call.resolve(JSObject().put("status", "granted"))
            return
        }
        requestPermissionForAlias("camera", call, "cameraCallback")
    }

    @PermissionCallback
    private fun cameraCallback(call: PluginCall) {
        call.resolve(
            JSObject().put("status", if (hasPerm(Manifest.permission.CAMERA)) "granted" else "denied")
        )
    }

    /** 恒久 deny (「今後表示しない」/2回拒否) からの回復用＝OS のアプリ情報画面を開く。 */
    @PluginMethod
    fun openAppSettings(call: PluginCall) {
        try {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null)
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            call.resolve()
        } catch (e: Exception) {
            call.reject("open settings failed: ${e.message}")
        }
    }

    private fun primaryAlias(): String = when {
        Build.VERSION.SDK_INT >= 34 -> "photos14"
        Build.VERSION.SDK_INT >= 33 -> "photos13"
        else -> "photosLegacy"
    }

    private fun hasPerm(p: String): Boolean =
        context.checkSelfPermission(p) == PackageManager.PERMISSION_GRANTED   // minSdk 24 >= API 23 なので直接呼べる

    private fun currentStatus(): String {
        if (Build.VERSION.SDK_INT >= 33) {
            if (hasPerm("android.permission.READ_MEDIA_IMAGES")) return "authorized"
            if (Build.VERSION.SDK_INT >= 34 &&
                hasPerm("android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
            ) return "limited"   // 部分許可 (選択した写真のみ) = iOS の limited
            return notGrantedState()
        }
        return if (hasPerm(Manifest.permission.READ_EXTERNAL_STORAGE)) "authorized" else notGrantedState()
    }

    private fun notGrantedState(): String =
        when (getPermissionState(primaryAlias())) {
            PermissionState.PROMPT, PermissionState.PROMPT_WITH_RATIONALE -> "notDetermined"
            else -> "denied"
        }

    // ---------------- enumerate ----------------

    private data class Row(val id: Long, val dateMs: Long, val exifDate: Boolean, val w: Int, val h: Int)

    @PluginMethod
    fun enumerate(call: PluginCall) {
        val limit = call.getInt("limit") ?: 0   // 0 = 全件
        executor.execute {
            try {
                val t0 = System.currentTimeMillis()
                val proj = arrayOf(
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT
                )
                // 29+: 書き込み途中 (IS_PENDING) の行を除外
                val selection = if (Build.VERSION.SDK_INT >= 29) "${MediaStore.Images.Media.IS_PENDING}=0" else null
                val rows = ArrayList<Row>(1024)
                context.contentResolver.query(imagesUri(), proj, selection, null, null)?.use { c ->
                    val iId = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                    val iTaken = c.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                    val iAdded = c.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
                    val iW = c.getColumnIndex(MediaStore.Images.Media.WIDTH)
                    val iH = c.getColumnIndex(MediaStore.Images.Media.HEIGHT)
                    while (c.moveToNext()) {
                        val taken = if (iTaken >= 0 && !c.isNull(iTaken)) c.getLong(iTaken) else 0L
                        val added = if (iAdded >= 0 && !c.isNull(iAdded)) c.getLong(iAdded) * 1000L else 0L
                        rows.add(
                            Row(
                                id = c.getLong(iId),
                                dateMs = if (taken > 0) taken else added,
                                exifDate = taken > 0,
                                w = if (iW >= 0 && !c.isNull(iW)) c.getInt(iW) else 0,
                                h = if (iH >= 0 && !c.isNull(iH)) c.getInt(iH) else 0
                            )
                        )
                    }
                }
                // iOS と同じ「新しい順」。SQL の NULL 順の癖を避けて Kotlin 側で確定させる
                rows.sortByDescending { it.dateMs }

                val total = rows.size
                val cap = if (limit > 0) minOf(limit, total) else total
                // ISO8601 UTC (iOS の ISO8601DateFormatter と同形・epoch は絶対時刻なので TZ 安全)
                val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    .apply { timeZone = TimeZone.getTimeZone("UTC") }
                val items = JSArray()
                for (i in 0 until cap) {
                    val r = rows[i]
                    val m = JSObject()
                    m.put("id", r.id.toString())
                    m.put("w", r.w)
                    m.put("h", r.h)
                    if (r.dateMs > 0) {
                        m.put("date", fmt.format(Date(r.dateMs)))
                        m.put("dateSource", if (r.exifDate) "exif" else "saved")
                    }
                    items.put(m)
                }
                val res = JSObject()
                res.put("count", total)
                res.put("returned", cap)
                res.put("ms", (System.currentTimeMillis() - t0).toInt())
                res.put("items", items)
                call.resolve(res)
            } catch (e: Exception) {
                call.reject("enumerate failed: ${e.message}")
            }
        }
    }

    private fun imagesUri(): Uri =
        if (Build.VERSION.SDK_INT >= 29) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    // ---------------- thumbnail ----------------

    /** 1枚分のサムネ+EXIF同乗 JSObject を作る (thumbnail / thumbnails の共通部・任意スレッドから安全)。
     *  GPS に加え DateTimeOriginal も返す: Android は MediaStore.DATE_TAKEN が欠ける写真が多い
     *  (スキャナ癖/アプリ保存) ため、JS 側で「保存日 → EXIF 撮影日」に格上げする (日時純度 EXIF>保存日)。 */
    private fun thumbResult(idStr: String, size: Int): JSObject {
        val id = idStr.toLong()
        val uri = ContentUris.withAppendedId(imagesUri(), id)
        val bmp: Bitmap = if (Build.VERSION.SDK_INT >= 29) {
            // loadThumbnail は EXIF 向き補正済み・アスペクト維持で size に収める
            context.contentResolver.loadThumbnail(uri, Size(size, size), null)
        } else {
            legacyThumbnail(id)
        }
        val out = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
        val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        val res = JSObject()
        res.put("dataUrl", "data:image/jpeg;base64,$b64")
        readExif(uri)?.let { ex ->
            if (ex.lat != null && ex.lng != null) {
                res.put("lat", ex.lat)
                res.put("lng", ex.lng)
            }
            if (ex.dateMs != null) {
                val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                    .apply { timeZone = TimeZone.getTimeZone("UTC") }
                res.put("exifDate", fmt.format(Date(ex.dateMs)))
            }
        }
        return res
    }

    @PluginMethod
    fun thumbnail(call: PluginCall) {
        val idStr = call.getString("id")
        if (idStr == null) {
            call.reject("id required")
            return
        }
        val size = call.getInt("size") ?: 200
        executor.execute {
            try {
                call.resolve(thumbResult(idStr, size))
            } catch (e: Exception) {
                call.reject("thumbnail failed: ${e.message}")
            }
        }
    }

    // ---------------- thumbnails (v228: バッチ+並列) ----------------

    /** N枚のサムネを1往復で返す。デコードは thumbPool (コア数に応じ 2..4) で並列＝
     *  古い端末の直列ボトルネックをコア数ぶん短縮。items は ids と同順で、
     *  失敗した枚だけ {id, error} (バッチ全体は落とさない＝JS 側で個別 skip)。 */
    @PluginMethod
    fun thumbnails(call: PluginCall) {
        val idsArr = call.getArray("ids")
        if (idsArr == null || idsArr.length() == 0) {
            call.reject("ids required")
            return
        }
        val size = call.getInt("size") ?: 200
        val ids = ArrayList<String>(idsArr.length())
        try {
            for (i in 0 until idsArr.length()) ids.add(idsArr.getString(i))
        } catch (e: Exception) {
            call.reject("bad ids: ${e.message}")
            return
        }
        executor.execute {   // 受付は従来 executor＝バッチ同士は直列 (プールの取り合いを防ぐ)
            try {
                val futures = ids.map { idStr ->
                    thumbPool.submit(Callable {
                        try {
                            thumbResult(idStr, size).put("id", idStr)
                        } catch (e: Exception) {
                            JSObject().put("id", idStr).put("error", e.message ?: "failed")
                        }
                    })
                }
                val items = JSArray()
                for (f in futures) items.put(f.get())
                call.resolve(JSObject().put("items", items))
            } catch (e: Exception) {
                call.reject("thumbnails failed: ${e.message}")
            }
        }
    }

    /** API <29: MINI_KIND サムネ + ORIENTATION 列で手動回転 (getThumbnail は向きを補正しない) */
    @Suppress("DEPRECATION")
    private fun legacyThumbnail(id: Long): Bitmap {
        var bmp = MediaStore.Images.Thumbnails.getThumbnail(
            context.contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null
        ) ?: throw IllegalStateException("no thumbnail")
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.ORIENTATION),
            "${MediaStore.Images.Media._ID}=?",
            arrayOf(id.toString()),
            null
        )?.use { c ->
            if (c.moveToFirst() && !c.isNull(0)) {
                val deg = c.getInt(0)
                if (deg != 0) {
                    val mtx = Matrix().apply { postRotate(deg.toFloat()) }
                    bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, mtx, true)
                }
            }
        }
        return bmp
    }

    // ---------------- savePhoto (v170) ----------------

    /** 撮った写真（base64 JPEG）を端末の写真アプリ（MediaStore / Pictures）に保存する（重ね撮り用）。
     *  API 29+ は自分で作ったメディアの追加に権限不要。<=28 は WRITE_EXTERNAL_STORAGE が要る（未宣言なので
     *  失敗しうる＝JS 側でベストエフォート扱い）。 */
    @PluginMethod
    fun savePhoto(call: PluginCall) {
        val b64 = call.getString("base64")
        if (b64 == null) { call.reject("base64 required"); return }
        val name = call.getString("name") ?: "rephoto_${System.currentTimeMillis()}.jpg"
        executor.execute {
            try {
                val bytes = Base64.decode(b64, Base64.DEFAULT)
                val resolver = context.contentResolver
                val collection = if (Build.VERSION.SDK_INT >= 29)
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= 29) {
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }
                val uri = resolver.insert(collection, values)
                    ?: throw IllegalStateException("insert failed")
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: throw IllegalStateException("open output failed")
                if (Build.VERSION.SDK_INT >= 29) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
                call.resolve(JSObject().put("saved", true))
            } catch (e: Exception) {
                call.reject("save failed: ${e.message}")
            }
        }
    }

    private class ExifLite(val lat: Double?, val lng: Double?, val dateMs: Long?)

    /**
     * EXIF から GPS + 撮影日時を読む。
     * GPS は 29+ で ACCESS_MEDIA_LOCATION + setRequireOriginal が必要 (無ければ redacted =
     * 日時だけ読める)。日時は EXIF の「TZ なしローカル時刻」を端末 TZ で解釈 (web の EXIF
     * パースと同じ流儀)。
     */
    private fun readExif(uri: Uri): ExifLite? {
        return try {
            val canOriginal = Build.VERSION.SDK_INT < 29 || hasPerm("android.permission.ACCESS_MEDIA_LOCATION")
            val target: Uri = if (Build.VERSION.SDK_INT >= 29 && canOriginal) MediaStore.setRequireOriginal(uri) else uri
            context.contentResolver.openInputStream(target)?.use { input ->
                val exif = ExifInterface(input)
                val ll = if (canOriginal) exif.latLong else null
                val dateMs = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)?.let { s ->
                    try {
                        SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.US).parse(s.trim())?.time
                    } catch (_: Exception) {
                        null
                    }
                }
                ExifLite(ll?.getOrNull(0), ll?.getOrNull(1), dateMs)
            }
        } catch (_: Exception) {
            null   // EXIF なし/読めない形式は「情報なし」扱い (取り込み自体は続行)
        }
    }
}
