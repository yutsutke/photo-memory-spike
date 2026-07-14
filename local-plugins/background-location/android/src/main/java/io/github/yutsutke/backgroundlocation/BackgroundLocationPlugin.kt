package io.github.yutsutke.backgroundlocation

import android.Manifest
import android.os.Build
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.PermissionState
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback

/**
 * Capacitor ブリッジ (薄い窓口)。本体は BgLocationService + BgLocationStore。
 * iOS (BackgroundLocationPlugin.swift) と同じ契約:
 *   requestAlways() -> { status }        // 位置権限 (+33+ は通知権限) を要求
 *   start({mode})   -> { ok, status }    // mode: "50"/"150"/"500"（旧 important/frequent も互換）
 *   stop()          -> { ok }
 *   drain()         -> { points: [{id, t, lat, lng, acc}] }
 *   status()        -> { status }        // always|whenInUse|denied|restricted|notDetermined
 *
 * status の Android 流マッピング: FGS モデルでは while-in-use 権限だけで「閉じても記録」が成立する
 * (iOS の Always に等価) ため、fine か coarse が下りていれば "always" を返す。"whenInUse" は返さない
 * (返すと JS が iOS 向けの「設定→常に許可」ナッジを出して誤誘導になる)。
 */
@CapacitorPlugin(
    name = "BackgroundLocation",
    permissions = [
        Permission(
            alias = "location",
            strings = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]
        ),
        Permission(alias = "notifications", strings = ["android.permission.POST_NOTIFICATIONS"])
    ]
)
class BackgroundLocationPlugin : Plugin() {

    /** 起動時 (プロセス再生成を含む): モードが ON のままなら再アーム (iOS の resumeFromSavedMode と同じ)。 */
    override fun load() {
        val mode = BgLocationStore.mode(context)
        if (mode != "off" && BgLocationService.hasLocationPermission(context)) {   // v211: off 以外はオン
            BgLocationService.start(context, mode)
        }
    }

    @PluginMethod
    fun requestAlways(call: PluginCall) {
        val want = mutableListOf<String>()
        if (!BgLocationService.hasLocationPermission(context)) want.add("location")
        if (Build.VERSION.SDK_INT >= 33 && getPermissionState("notifications") != PermissionState.GRANTED) {
            want.add("notifications")   // 常駐通知を見せるため (拒否でも記録自体は動く)
        }
        if (want.isEmpty()) {
            call.resolve(JSObject().put("status", statusString()))
            return
        }
        requestPermissionForAliases(want.toTypedArray(), call, "permsCallback")
    }

    /** 権限ダイアログの結果。位置が下りたら保存モードを再アーム (iOS の didChangeAuthorization → applyMode と同じ)。
     *  JS は requestAlways() 直後に await せず start({mode}) を呼ぶため、その時点で権限が無ければ
     *  モードだけ保存されている — ここが「許可が下りた瞬間にサービスを起こす」役を担う。 */
    @PermissionCallback
    private fun permsCallback(call: PluginCall) {
        if (BgLocationService.hasLocationPermission(context)) {
            val mode = BgLocationStore.mode(context)
            if (mode != "off") BgLocationService.start(context, mode)
        }
        call.resolve(JSObject().put("status", statusString()))
    }

    @PluginMethod
    fun start(call: PluginCall) {
        val mode = call.getString("mode") ?: "500"
        BgLocationStore.setMode(context, mode)
        if (BgLocationService.hasLocationPermission(context)) {
            BgLocationService.start(context, mode)
        }
        call.resolve(JSObject().put("ok", true).put("status", statusString()))
    }

    @PluginMethod
    fun stop(call: PluginCall) {
        BgLocationStore.setMode(context, "off")
        BgLocationService.stop(context)
        call.resolve(JSObject().put("ok", true))
    }

    @PluginMethod
    fun drain(call: PluginCall) {
        val pts = BgLocationStore.drain(context)
        val arr = try { JSArray(pts.toString()) } catch (_: Exception) { JSArray() }
        call.resolve(JSObject().put("points", arr))
    }

    @PluginMethod
    fun status(call: PluginCall) {
        call.resolve(JSObject().put("status", statusString()))
    }

    private fun statusString(): String {
        if (BgLocationService.hasLocationPermission(context)) return "always"
        return when (getPermissionState("location")) {
            PermissionState.DENIED -> "denied"
            else -> "notDetermined"
        }
    }
}
