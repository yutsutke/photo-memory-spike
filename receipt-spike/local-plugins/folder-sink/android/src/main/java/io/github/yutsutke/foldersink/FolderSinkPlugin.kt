package io.github.yutsutke.foldersink

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.documentfile.provider.DocumentFile
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
 *   pick()   -> { ok, label, error }   ok=false かつ error='' は「選ばずに閉じた」
 *   status() -> { ok, label }
 *   write({name, text}) -> { ok, path?, error? }
 *   forget() -> { ok }
 *
 * iOS との本質差:
 *  - 覚え方が bookmark ではなく **永続 URI 権限**（takePersistableUriPermission）。
 *    端末の再起動やアプリの更新をまたいで残るが、**アプリのデータを消すと消える**（その時は選び直し）。
 *  - 権限宣言は不要（本人が選んだフォルダだけが渡る）＝AndroidManifest は空。
 *  - 上書きは「同じファイルを truncate して書く」。⚠ 消して作り直すと、Drive 側で
 *    「ファイル名 (1)」が生えたり共有リンクが切れたりする＝**同じファイルを使い続ける**のが正しい。
 *    そのため作った時の Uri を覚えておき、次回はそれを直接使う（名前で探し直さない＝
 *    プロバイダが拡張子を足して名前が変わっても迷子にならない）。
 */
@CapacitorPlugin(name = "FolderSink")
class FolderSinkPlugin : Plugin() {

    private val prefs by lazy { context.getSharedPreferences("rcpt_sink", Context.MODE_PRIVATE) }

    private fun fail(msg: String) = JSObject().put("ok", false).put("error", msg)

    @PluginMethod
    fun status(call: PluginCall) {
        val uri = prefs.getString("uri", null)
        call.resolve(JSObject().put("ok", uri != null).put("label", prefs.getString("label", "") ?: ""))
    }

    @PluginMethod
    fun forget(call: PluginCall) {
        prefs.getString("uri", null)?.let {
            try {
                context.contentResolver.releasePersistableUriPermission(
                    Uri.parse(it),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) { /* 既に無い＝何もしなくてよい */ }
        }
        prefs.edit().clear().apply()
        call.resolve(JSObject().put("ok", true))
    }

    @PluginMethod
    fun pick(call: PluginCall) {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        startActivityForResult(call, i, "picked")
    }

    @ActivityCallback
    fun picked(call: PluginCall, result: ActivityResult) {
        val uri = result.data?.data
            ?: run { call.resolve(JSObject().put("ok", false).put("label", "").put("error", "")); return }
        try {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (e: Exception) {
            call.resolve(fail("この場所は覚えられませんでした: ${e.message}")); return
        }
        val label = try { DocumentFile.fromTreeUri(context, uri)?.name } catch (_: Exception) { null }
            ?: "選んだフォルダ"
        // フォルダを選び直したら、前のファイルの Uri は捨てる（別のフォルダの記憶を引きずらない）
        prefs.edit().putString("uri", uri.toString()).putString("label", label).remove("file").apply()
        call.resolve(JSObject().put("ok", true).put("label", label).put("error", ""))
    }

    @PluginMethod
    fun write(call: PluginCall) {
        val name = call.getString("name") ?: ""
        val text = call.getString("text") ?: ""
        if (name.isEmpty()) { call.resolve(fail("ファイル名がありません")); return }
        val saved = prefs.getString("uri", null)
            ?: run { call.resolve(fail("フォルダが選ばれていません")); return }
        try {
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

            val bytes = text.toByteArray(Charsets.UTF_8)
            try {
                // "wt" = 中身を空にしてから書く。⚠ "w" だけだと短くなった時に前の文字が末尾に残り JSON が壊れる
                (context.contentResolver.openOutputStream(target.uri, "wt")
                    ?: throw IOException("開けません")).use { it.write(bytes) }
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
}
