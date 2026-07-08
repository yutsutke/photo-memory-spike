import Foundation
import UIKit
import Capacitor

/// ホーム画面のアプリアイコン長押し（Quick Action）→ JS へ橋渡しする最小プラグイン (v165)。
///
/// 仕組み（BgLoc と同じ「UserDefaults バッファ＋drain」型）:
/// - AppDelegate の performActionFor が UserDefaults に pending を書き、Notification を post する。
/// - コールド起動: WebView 起動後に JS が getPending() で drain（写真の追い読み完了を待ってから開ける）。
/// - 起動中（温かい）: Notification → notifyListeners("shortcut") で即配信。
/// - static shortcut 自体は Info.plist の UIApplicationShortcutItems（type = "rephoto"）。
@objc(AppShortcutsPlugin)
public class AppShortcutsPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "AppShortcutsPlugin"
    public let jsName = "AppShortcuts"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "getPending", returnType: CAPPluginReturnPromise)
    ]

    /// AppDelegate と共有するキー（AppDelegate 側は文字列リテラルで参照＝App target からの import 不要）
    static let pendingKey = "pms_shortcut_pending"
    static let noteName = Notification.Name("AppShortcutTapped")

    public override func load() {
        NotificationCenter.default.addObserver(forName: AppShortcutsPlugin.noteName, object: nil, queue: .main) { [weak self] note in
            let type = (note.userInfo?["type"] as? String) ?? ""
            self?.notifyListeners("shortcut", data: ["type": type])
        }
    }

    /// pending の shortcut type を返して消す（無ければ空文字）。
    @objc func getPending(_ call: CAPPluginCall) {
        let d = UserDefaults.standard
        let type = d.string(forKey: AppShortcutsPlugin.pendingKey) ?? ""
        d.removeObject(forKey: AppShortcutsPlugin.pendingKey)
        call.resolve(["type": type])
    }
}
