import Foundation
import UIKit
import UniformTypeIdentifiers
import Capacitor

/// 置き場（フォルダ or 上書きするファイル）を一度だけ選ばせて覚え、以後は聞かずに書き続けるプラグイン。
///
/// 狙い: 書き出した「解析用の写し」を、AI が読める置き場（Google ドライブ等）へ**毎回押さずに**置く。
///
/// 覚え方は2通り（r108 で2つ目を足した）:
///  - **フォルダを覚える**（本命）＝書類ピッカーでフォルダを選ばせ、以後その中に何個でも書ける。
///  - **ファイルを覚える**（逃げ道）＝「上書きし続ける1つのファイル」を選ばせる。
///    ⚠ **Google ドライブは「ファイル」アプリにフォルダを渡さない**（実機 2026-08-28・選択画面で灰色）。
///       ファイル単体なら渡せる見込みなので、置き場ごとにこちらへ落ちられるようにした。
///       使い方＝先に共有シートで1回ドライブに置いてもらい、そのファイルを選んで結びつける。
///
/// 仕組み（OAuth ゼロ＝アカウント連携をしない）:
/// - 選んだ URL から **bookmark**（＝次に開くための許可証）を作り UserDefaults に保存。
///   ⚠ iOS の bookmarkData に `.withSecurityScope` は無い（あれは macOS）。素の bookmarkData で、
///      resolve したあと startAccessingSecurityScopedResource() を呼ぶのが iOS の作法。
/// - write(): bookmark を resolve → 権限を開く → **NSFileCoordinator 経由で書く** → 閉じる。
///   ⚠ File Provider（クラウドのフォルダ）へは coordinator 無しで書くと壊れる/失敗することがある。
///   ⚠ atomic 書き込み（temp を作って rename）は File Provider が拒むことがある → 落ちたら素の書き込みに落とす。
///
/// JS 契約（Android の FolderSinkPlugin.kt と同じ・JS は両OSで無改修）:
///   pick()                  -> { ok, label, error }   ok=false かつ error='' は「選ばずに閉じた」
///   pickFile({slot})        -> { ok, label, error }   slot＝この名前で書く時に使うファイル
///   status()                -> { ok, label, files: [slot,…] }
///   write({name, text})     -> { ok, path?, error? }  name と同じ slot のファイルがあればそちらへ
///   forget()                -> { ok }                 フォルダもファイルも全部忘れる
@objc(FolderSinkPlugin)
public class FolderSinkPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "FolderSinkPlugin"
    public let jsName = "FolderSink"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "pick",     returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "pickFile", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "status",   returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "write",    returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "forget",   returnType: CAPPluginReturnPromise)
    ]

    private static let bmKey = "rcpt_sink_bookmark"
    private static let lbKey = "rcpt_sink_label"
    private static let slotsKey = "rcpt_sink_slots"                 // 覚えているファイルの名前の一覧
    private static func fileKey(_ slot: String) -> String { "rcpt_sink_f_" + slot }

    /// ピッカーは「押した時の call」をあとから解決する＝bridge に預けて callbackId で取り戻す。
    private var pickCallId: String?
    /// 今回のピッカーが「ファイルを覚える」用なら、その名前（フォルダ用なら nil）
    private var pickSlot: String?
    /// delegate を plugin 自身にすると UIDocumentPickerDelegate の準拠が Capacitor の型と混ざるので別オブジェクトに分ける。
    private lazy var pickerDelegate = FolderPickerDelegate(owner: self)

    @objc func status(_ call: CAPPluginCall) {
        let d = UserDefaults.standard
        let slots = d.stringArray(forKey: Self.slotsKey) ?? []
        call.resolve(["ok": d.data(forKey: Self.bmKey) != nil || !slots.isEmpty,
                      "label": d.string(forKey: Self.lbKey) ?? "",
                      "files": slots])
    }

    @objc func forget(_ call: CAPPluginCall) {
        let d = UserDefaults.standard
        for slot in (d.stringArray(forKey: Self.slotsKey) ?? []) { d.removeObject(forKey: Self.fileKey(slot)) }
        d.removeObject(forKey: Self.slotsKey)
        d.removeObject(forKey: Self.bmKey)
        d.removeObject(forKey: Self.lbKey)
        call.resolve(["ok": true])
    }

    @objc func pick(_ call: CAPPluginCall) { present(call, slot: nil) }

    /// 「この名前で書く時に上書きするファイル」を選ばせる（フォルダを渡せない置き場むけ）
    @objc func pickFile(_ call: CAPPluginCall) {
        let slot = call.getString("slot") ?? ""
        guard !slot.isEmpty else { call.resolve(["ok": false, "error": "どのファイルの代わりか分かりません"]); return }
        present(call, slot: slot)
    }

    private func present(_ call: CAPPluginCall, slot: String?) {
        bridge?.saveCall(call)
        pickCallId = call.callbackId
        pickSlot = slot
        DispatchQueue.main.async { [weak self] in
            guard let self = self, let vc = self.bridge?.viewController else {
                self?.finishPick(ok: false, label: "", error: "画面を開けませんでした")
                return
            }
            // フォルダを覚える時は .folder、ファイルを覚える時は .data（フォルダ以外のあらゆるファイル）
            let types: [UTType] = (slot == nil) ? [UTType.folder] : [UTType.data]
            let p = UIDocumentPickerViewController(forOpeningContentTypes: types, asCopy: false)
            p.delegate = self.pickerDelegate
            p.allowsMultipleSelection = false
            p.modalPresentationStyle = .formSheet
            vc.present(p, animated: true)
        }
    }

    /// ピッカーの結果（nil = 選ばずに閉じた）
    fileprivate func picked(_ url: URL?) {
        guard let url = url else { finishPick(ok: false, label: "", error: ""); return }
        let scoped = url.startAccessingSecurityScopedResource()
        defer { if scoped { url.stopAccessingSecurityScopedResource() } }
        do {
            let bm = try url.bookmarkData(options: [], includingResourceValuesForKeys: nil, relativeTo: nil)
            let name = Self.label(for: url)
            let d = UserDefaults.standard
            if let slot = pickSlot {
                d.set(bm, forKey: Self.fileKey(slot))
                var slots = d.stringArray(forKey: Self.slotsKey) ?? []
                if !slots.contains(slot) { slots.append(slot); d.set(slots, forKey: Self.slotsKey) }
            } else {
                d.set(bm, forKey: Self.bmKey)
                d.set(name, forKey: Self.lbKey)
            }
            finishPick(ok: true, label: name, error: "")
        } catch {
            finishPick(ok: false, label: "", error: "この場所は覚えられませんでした: \(error.localizedDescription)")
        }
    }

    private static func label(for url: URL) -> String {
        let n = url.lastPathComponent.removingPercentEncoding ?? url.lastPathComponent
        return n.isEmpty ? "選んだ場所" : n
    }

    private func finishPick(ok: Bool, label: String, error: String) {
        guard let id = pickCallId, let call = bridge?.savedCall(withID: id) else { return }
        pickCallId = nil
        pickSlot = nil
        call.resolve(["ok": ok, "label": label, "error": error])
        bridge?.releaseCall(call)
    }

    @objc func write(_ call: CAPPluginCall) {
        let name = call.getString("name") ?? ""
        let text = call.getString("text") ?? ""
        // r111: .sqlite（バイナリ）のため。base64 があれば文字ではなくそのバイト列を書く
        let b64  = call.getString("base64") ?? ""
        guard !name.isEmpty else { call.resolve(["ok": false, "error": "ファイル名がありません"]); return }
        let payload: Data
        if b64.isEmpty { payload = Data(text.utf8) }
        else if let d = Data(base64Encoded: b64) { payload = d }
        else { call.resolve(["ok": false, "error": "中身を読み取れませんでした"]); return }
        let d = UserDefaults.standard
        // ⚠ ファイルを覚えていればそちらが優先＝フォルダを渡せない置き場（Google ドライブ）でも書ける
        let fileBm = d.data(forKey: Self.fileKey(name))
        let dirBm = d.data(forKey: Self.bmKey)
        guard let bm = fileBm ?? dirBm else {
            call.resolve(["ok": false, "error": "置き場が選ばれていません"]); return
        }
        let isFile = (fileBm != nil)
        DispatchQueue.global(qos: .utility).async {
            var stale = false
            let base: URL
            do {
                base = try URL(resolvingBookmarkData: bm, options: [], relativeTo: nil, bookmarkDataIsStale: &stale)
            } catch {
                call.resolve(["ok": false, "error": "置き場を開けませんでした（選び直してください）: \(error.localizedDescription)"])
                return
            }
            let scoped = base.startAccessingSecurityScopedResource()
            defer { if scoped { base.stopAccessingSecurityScopedResource() } }
            // 覚えていた許可証が古くなっていたら作り直す（場所を動かした・端末を移した時）
            if stale, let fresh = try? base.bookmarkData(options: [], includingResourceValuesForKeys: nil, relativeTo: nil) {
                UserDefaults.standard.set(fresh, forKey: isFile ? Self.fileKey(name) : Self.bmKey)
            }
            let target = isFile ? base : base.appendingPathComponent(name)
            let data = payload
            var writeErr: Error?
            var coordErr: NSError?
            NSFileCoordinator(filePresenter: nil).coordinate(writingItemAt: target, options: .forReplacing, error: &coordErr) { u in
                do { try data.write(to: u, options: .atomic) }
                catch {
                    // ⚠ クラウドの置き場は atomic（temp→rename）を拒むことがある → 素で書き直す
                    do { try data.write(to: u) } catch { writeErr = error }
                }
            }
            if let e = coordErr ?? writeErr {
                call.resolve(["ok": false, "error": e.localizedDescription])
                return
            }
            call.resolve(["ok": true, "path": target.path])
        }
    }
}

/// ピッカーの受け皿だけを持つ小さな delegate（plugin 本体に UIKit の準拠を混ぜない）
private class FolderPickerDelegate: NSObject, UIDocumentPickerDelegate {
    weak var owner: FolderSinkPlugin?
    init(owner: FolderSinkPlugin) { self.owner = owner }
    func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        owner?.picked(urls.first)
    }
    func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        owner?.picked(nil)
    }
}
