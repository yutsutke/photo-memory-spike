import Foundation
import UIKit
import UniformTypeIdentifiers
import Capacitor

/// フォルダを一度だけ選ばせて覚え、以後は聞かずに同じ場所へ上書きするプラグイン（r105・1.1 の A案）。
///
/// 狙い: 書き出した「解析用の写し」を、AI が読める置き場（Google ドライブ等）へ**毎回押さずに**置く。
///
/// 仕組み（OAuth ゼロ＝アカウント連携をしない）:
/// - pick(): 書類ピッカー（「ファイル」アプリ）でフォルダを選ばせる。Google ドライブは File Provider として
///   「ファイル」に出ているので、その中のフォルダも選べる（＝これが通るかがこの版の最大の未知）。
/// - 選んだ URL から **bookmark**（＝次に開くための許可証）を作り UserDefaults に保存。
///   ⚠ iOS の bookmarkData に `.withSecurityScope` は無い（あれは macOS）。素の bookmarkData で、
///      resolve したあと startAccessingSecurityScopedResource() を呼ぶのが iOS の作法。
/// - write(): bookmark を resolve → 権限を開く → **NSFileCoordinator 経由で書く** → 閉じる。
///   ⚠ File Provider（クラウドのフォルダ）へは coordinator 無しで書くと壊れる/失敗することがある。
///   ⚠ atomic 書き込み（temp を作って rename）は File Provider が拒むことがある → 落ちたら素の書き込みに落とす。
///
/// JS 契約（Android の FolderSinkPlugin.kt と同じ・JS は両OSで無改修）:
///   pick()   -> { ok, label, error }   ok=false かつ error='' は「選ばずに閉じた」
///   status() -> { ok, label }
///   write({name, text}) -> { ok, path?, error? }
///   forget() -> { ok }
@objc(FolderSinkPlugin)
public class FolderSinkPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "FolderSinkPlugin"
    public let jsName = "FolderSink"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "pick",   returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "status", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "write",  returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "forget", returnType: CAPPluginReturnPromise)
    ]

    private static let bmKey = "rcpt_sink_bookmark"
    private static let lbKey = "rcpt_sink_label"

    /// ピッカーは「押した時の call」をあとから解決する＝bridge に預けて callbackId で取り戻す。
    private var pickCallId: String?
    /// delegate を plugin 自身にすると UIDocumentPickerDelegate の準拠が Capacitor の型と混ざるので別オブジェクトに分ける。
    private lazy var pickerDelegate = FolderPickerDelegate(owner: self)

    @objc func status(_ call: CAPPluginCall) {
        let d = UserDefaults.standard
        call.resolve(["ok": d.data(forKey: Self.bmKey) != nil,
                      "label": d.string(forKey: Self.lbKey) ?? ""])
    }

    @objc func forget(_ call: CAPPluginCall) {
        let d = UserDefaults.standard
        d.removeObject(forKey: Self.bmKey)
        d.removeObject(forKey: Self.lbKey)
        call.resolve(["ok": true])
    }

    @objc func pick(_ call: CAPPluginCall) {
        bridge?.saveCall(call)
        pickCallId = call.callbackId
        DispatchQueue.main.async { [weak self] in
            guard let self = self, let vc = self.bridge?.viewController else {
                self?.finishPick(ok: false, label: "", error: "画面を開けませんでした")
                return
            }
            let p = UIDocumentPickerViewController(forOpeningContentTypes: [UTType.folder], asCopy: false)
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
            d.set(bm, forKey: Self.bmKey)
            d.set(name, forKey: Self.lbKey)
            finishPick(ok: true, label: name, error: "")
        } catch {
            finishPick(ok: false, label: "", error: "この場所は覚えられませんでした: \(error.localizedDescription)")
        }
    }

    private static func label(for url: URL) -> String {
        let n = url.lastPathComponent.removingPercentEncoding ?? url.lastPathComponent
        return n.isEmpty ? "選んだフォルダ" : n
    }

    private func finishPick(ok: Bool, label: String, error: String) {
        guard let id = pickCallId, let call = bridge?.savedCall(withID: id) else { return }
        pickCallId = nil
        call.resolve(["ok": ok, "label": label, "error": error])
        bridge?.releaseCall(call)
    }

    @objc func write(_ call: CAPPluginCall) {
        let name = call.getString("name") ?? ""
        let text = call.getString("text") ?? ""
        guard !name.isEmpty else { call.resolve(["ok": false, "error": "ファイル名がありません"]); return }
        guard let bm = UserDefaults.standard.data(forKey: Self.bmKey) else {
            call.resolve(["ok": false, "error": "フォルダが選ばれていません"]); return
        }
        DispatchQueue.global(qos: .utility).async {
            var stale = false
            let dir: URL
            do {
                dir = try URL(resolvingBookmarkData: bm, options: [], relativeTo: nil, bookmarkDataIsStale: &stale)
            } catch {
                call.resolve(["ok": false, "error": "フォルダを開けませんでした（選び直してください）: \(error.localizedDescription)"])
                return
            }
            let scoped = dir.startAccessingSecurityScopedResource()
            defer { if scoped { dir.stopAccessingSecurityScopedResource() } }
            // 覚えていた許可証が古くなっていたら作り直す（フォルダを動かした・端末を移した時）
            if stale, let fresh = try? dir.bookmarkData(options: [], includingResourceValuesForKeys: nil, relativeTo: nil) {
                UserDefaults.standard.set(fresh, forKey: Self.bmKey)
            }
            let target = dir.appendingPathComponent(name)
            let data = Data(text.utf8)
            var writeErr: Error?
            var coordErr: NSError?
            NSFileCoordinator(filePresenter: nil).coordinate(writingItemAt: target, options: .forReplacing, error: &coordErr) { u in
                do { try data.write(to: u, options: .atomic) }
                catch {
                    // ⚠ クラウドのフォルダは atomic（temp→rename）を拒むことがある → 素で書き直す
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
