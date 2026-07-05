package io.github.yutsutke.backgroundlocation

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * 記録点のバッファとモードの永続化 (iOS の UserDefaults バッファと同型)。
 * - サービス (記録側) とプラグイン (drain 側) の両方から触るので synchronized。
 * - 点は {id, t(ms), lat, lng, acc} — iOS 実装 (BackgroundLocationPlugin.swift の append) と同じキー。
 *   JS の drainNativeTrack はこの形をそのまま IndexedDB の track ストアへ合流させる。
 */
object BgLocationStore {
    private const val PREFS = "pms_bgloc"
    private const val KEY_BUF = "buffer"    // JSON array 文字列
    private const val KEY_MODE = "mode"     // "off" | "important" | "frequent"
    private const val MAX_POINTS = 5000     // 暴走防止の上限 (iOS と同じ)
    private val lock = Any()

    private fun prefs(ctx: Context) =
        ctx.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun mode(ctx: Context): String = prefs(ctx).getString(KEY_MODE, "off") ?: "off"

    fun setMode(ctx: Context, mode: String) {
        prefs(ctx).edit().putString(KEY_MODE, mode).apply()
    }

    fun append(ctx: Context, lat: Double, lng: Double, acc: Float, t: Long) {
        synchronized(lock) {
            val arr = try { JSONArray(prefs(ctx).getString(KEY_BUF, "[]")) } catch (_: Exception) { JSONArray() }
            arr.put(JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("t", t)
                put("lat", lat)
                put("lng", lng)
                put("acc", acc.toDouble())
            })
            // 上限を超えたら古い方から捨てる
            val out = if (arr.length() > MAX_POINTS) {
                JSONArray().also { dst -> for (i in arr.length() - MAX_POINTS until arr.length()) dst.put(arr.get(i)) }
            } else arr
            prefs(ctx).edit().putString(KEY_BUF, out.toString()).apply()
        }
    }

    /** バッファした点を返して空にする (JS が track ストアへ合流させる)。 */
    fun drain(ctx: Context): JSONArray {
        synchronized(lock) {
            val s = prefs(ctx).getString(KEY_BUF, "[]") ?: "[]"
            prefs(ctx).edit().remove(KEY_BUF).apply()
            return try { JSONArray(s) } catch (_: Exception) { JSONArray() }
        }
    }
}
