package io.github.yutsutke.foldersink

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.documentfile.provider.DocumentFile
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.ActivityCallback
import com.getcapacitor.annotation.CapacitorPlugin
import java.io.IOException

/**
 * folder-sink の Android 実装（SAF＝ストレージアクセスフレームワーク）。
 * iOS (FolderSinkPlugin.swift) と同じ契約＝JS は両OSで無改修:
 *   pick()              -> { ok, label, error }   ok=false かつ error='' は「選ばずに閉じた」
 *   pickFile({slot})    -> { ok, label, error }   slot＝この名前で書く時に上書きするファイル
 *   status()            -> { ok, label, files: [slot,…] }
 *   write({name, text}) -> { ok, path?, error? }  name と同じ slot のファイルがあればそちらへ
 *   forget()            -> { ok }
 *
 * 覚え方は2通り（r108 で2つ目を足した）:
 *  - **フォルダを覚える**（本命）＝ACTION_OPEN_DOCUMENT_TREE。中に何個でも書ける。
 *  - **ファイルを覚える**（逃げ道）＝ACTION_OPEN_DOCUMENT。「上書きし続ける1つのファイル」を選ばせる。
 *    ⚠ iOS で **Google ドライブがフォルダを渡さない**（実機 2026-08-28）ことが分かったので足した道。
 *       Android の Drive も同じ形（フォルダは渡さない）である見込みなので、契約を揃えておく。
 *
 * iOS との本質差:
 *  - 覚え方が bookmark ではなく **永続 URI 権限**（takePersistableUriPermission）。
 *    端末の再起動やアプリの更新をまたいで残るが、**アプリのデータを消すと消える**（その時は選び直し）。
 *  - 権限宣言は不要（本人が選んだ場所だけが渡る）＝AndroidManifest は空。
 *  - 上書きは「同じファイルを truncate して書く」。⚠ 消して作り直すと、Drive 側で
 *    「ファイル名 (1)」が生えたり共有リンクが切れたりする＝**同じファイルを使い続ける**のが正しい。
 */
@CapacitorPlugin(name = "FolderSink")
class FolderSinkPlugin : Plugin() {

    private val prefs by lazy { context.getSharedPreferences("rcpt_sink", Context.MODE_PRIVATE) }

    private fun fail(msg: String) = JSObject().put("ok", false).put("error", msg)
    private fun fileKey(slot: String) = "f_$slot"
    private fun slots(): MutableSet<String> =
        HashSet(prefs.getStringSet("slots", emptySet()) ?: emptySet())

    @PluginMethod
    fun status(call: PluginCall) {
        val uri = prefs.getString("uri", null)
        val sl = slots()
        call.resolve(
            JSObject().put("ok", uri != null || sl.isNotEmpty())
                .put("label", prefs.getString("label", "") ?: "")
                .put("files", JSArray(sl.toList()))
        )
    }

    @PluginMethod
    fun forget(call: PluginCall) {
        val ed = prefs.edit()
        val release = { u: String, flags: Int ->
            try { context.contentResolver.releasePersistableUriPermission(Uri.parse(u), flags) }
            catch (_: Exception) { /* 既に無い＝何もしなくてよい */ }
        }
        val rw = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        prefs.getString("uri", null)?.let { release(it, rw) }
        for (s in slots()) prefs.getString(fileKey(s), null)?.let { release(it, rw) }
        ed.clear().apply()
        call.resolve(JSObject().put("ok", true))
    }

    @PluginMethod
    fun pick(call: PluginCall) {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        startActivityForResult(call, i, "pickedTree")
    }

    /** 「この名前で書く時に上書きするファイル」を選ばせる（フォルダを渡せない置き場むけ） */
    @PluginMethod
    fun pickFile(call: PluginCall) {
        val slot = call.getString("slot") ?: ""
        if (slot.isEmpty()) { call.resolve(fail("どのファイルの代わりか分かりません")); return }
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
            .addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        startActivityForResult(call, i, "pickedFile")
    }

    @ActivityCallback
    fun pickedTree(call: PluginCall, result: ActivityResult) {
        val uri = result.data?.data
            ?: run { call.resolve(JSObject().put("ok", false).put("label", "").put("error", "")); return }
        if (!take(uri)) { call.resolve(fail("この場所は覚えられませんでした")); return }
        val label = try { DocumentFile.fromTreeUri(context, uri)?.name } catch (_: Exception) { null }
            ?: "選んだフォルダ"
        // フォルダを選び直したら、前のファイルの Uri は捨てる（別の場所の記憶を引きずらない）
        prefs.edit().putString("uri", uri.toString()).putString("label", label).remove("file").apply()
        call.resolve(JSObject().put("ok", true).put("label", label).put("error", ""))
    }

    @ActivityCallback
    fun pickedFile(call: PluginCall, result: ActivityResult) {
        val slot = call.getString("slot") ?: ""
        val uri = result.data?.data
            ?: run { call.resolve(JSObject().put("ok", false).put("label", "").put("error", "")); return }
        if (!take(uri)) { call.resolve(fail("このファイルは覚えられませんでした")); return }
        val label = try { DocumentFile.fromSingleUri(context, uri)?.name } catch (_: Exception) { null }
            ?: "選んだファイル"
        val sl = slots(); sl.add(slot)
        prefs.edit().putString(fileKey(slot), uri.toString()).putStringSet("slots", sl).apply()
        call.resolve(JSObject().put("ok", true).put("label", label).put("error", ""))
    }

    private fun take(uri: Uri): Boolean = try {
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )
        true
    } catch (_: Exception) { false }

    @PluginMethod
    fun write(call: PluginCall) {
        val name = call.getString("name") ?: ""
        val text = call.getString("text") ?: ""
        if (name.isEmpty()) { call.resolve(fail("ファイル名がありません")); return }
        val bytes = text.toByteArray(Charsets.UTF_8)
        try {
            // ⚠ ファイルを覚えていればそちらが優先＝フォルダを渡せない置き場でも書ける
            val slotUri = prefs.getString(fileKey(name), null)
            if (slotUri != null) {
                writeTo(Uri.parse(slotUri), bytes)
                call.resolve(JSObject().put("ok", true).put("path", slotUri))
                return
            }
            val saved = prefs.getString("uri", null)
                ?: run { call.resolve(fail("置き場が選ばれていません")); return }
            val dir = DocumentFile.fromTreeUri(context, Uri.parse(saved))
            if (dir == null || !dir.canWrite()) {
                call.resolve(fail("フォルダに書けません（選び直してください）")); return
            }
            var target = prefs.getString("file", null)
                ?.let { u -> try { DocumentFile.fromSingleUri(context, Uri.parse(u)) } catch (_: Exception) { null } }
                ?.takeIf { it.exists() }
            if (target == null) target = dir.findFile(name)
            if (target == null) target = dir.createFile("application/json", name)
            if (target == null) { call.resolve(fail("ファイルを作れませんでした")); return }
            try {
                writeTo(target.uri, bytes)
            } catch (e: Exception) {
                // truncate に対応しないプロバイダ＝消して作り直す（最後の逃げ道）
                try { target.delete() } catch (_: Exception) {}
                target = dir.createFile("application/json", name)
                    ?: run { call.resolve(fail("書き直せませんでした: ${e.message}")); return }
                (context.contentResolver.openOutputStream(target.uri, "w")
                    ?: throw IOException("開けません")).use { it.write(bytes) }
            }
            prefs.edit().putString("file", target.uri.toString()).apply()
            call.resolve(JSObject().put("ok", true).put("path", target.uri.toString()))
        } catch (e: Exception) {
            call.resolve(fail(e.message ?: "書けませんでした"))
        }
    }

    /** "wt" = 中身を空にしてから書く。⚠ "w" だけだと短くなった時に前の文字が末尾に残り JSON が壊れる */
    private fun writeTo(uri: Uri, bytes: ByteArray) {
        (context.contentResolver.openOutputStream(uri, "wt") ?: throw IOException("開けません"))
            .use { it.write(bytes) }
    }
}
