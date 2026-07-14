import Foundation
import CoreLocation   // CLLocationManager / CLLocation
import Capacitor

/// アプリのライフサイクルと独立して動く位置記録の本体（singleton）。
/// - important = Significant Location Change（基地局ベース・低電池・アプリを完全終了しても
///   iOS が背景でアプリを起こして1点配送する＝「閉じても効く」の中核）。
/// - frequent  = 標準の継続更新（`allowsBackgroundLocationUpdates`＝背景でも更新）＋保険で SLC も併用。
/// 記録点は UserDefaults にバッファし、JS が起動/復帰時に drain して IndexedDB の track ストアへ
/// 合流させる（座標+時刻のみ・端末内だけ・外部送信なし＝機種変更で移せる）。
final class BackgroundLocationManager: NSObject, CLLocationManagerDelegate {
    static let shared = BackgroundLocationManager()

    private let manager = CLLocationManager()
    private let defaults = UserDefaults.standard
    private let bufKey = "pms_bgloc_buffer"   // 記録点のバッファ（[[String:Any]]）
    private let modeKey = "pms_bgloc_mode"     // "off" | "50"/"150"/"500"（旧 important/frequent も互換）
    private var wantsAlways = false

    private override init() {
        super.init()
        manager.delegate = self
        // Info.plist の UIBackgroundModes=location が無いと true 設定で例外になるため、両者は同じ commit で入れる。
        manager.allowsBackgroundLocationUpdates = true
        manager.pausesLocationUpdatesAutomatically = false
        manager.activityType = .other
        resumeFromSavedMode()   // 起動（背景再起動を含む）で前回モードを再アーム＝閉じても継続
    }

    // MARK: - JS から叩く API
    func requestAlways() {
        wantsAlways = true
        switch currentStatus() {
        case .notDetermined: manager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse: manager.requestAlwaysAuthorization()
        default: break
        }
    }

    func start(_ mode: String) {
        defaults.set(mode, forKey: modeKey)
        applyMode(mode)
    }

    func stop() {
        defaults.set("off", forKey: modeKey)
        manager.stopMonitoringSignificantLocationChanges()
        manager.stopUpdatingLocation()
    }

    /// バッファした点を返して空にする（JS が track ストアへ合流させる）。
    func drain() -> [[String: Any]] {
        let buf = defaults.array(forKey: bufKey) as? [[String: Any]] ?? []
        defaults.removeObject(forKey: bufKey)
        return buf
    }

    func statusString() -> String { BackgroundLocationManager.string(currentStatus()) }

    // MARK: - 内部
    private func currentStatus() -> CLAuthorizationStatus {
        if #available(iOS 14.0, *) { return manager.authorizationStatus }
        return CLLocationManager.authorizationStatus()
    }

    private func resumeFromSavedMode() {
        let mode = defaults.string(forKey: modeKey) ?? "off"
        if mode != "off" { applyMode(mode) }   // v211: 距離値/旧値どちらも applyMode が解釈
    }

    private func applyMode(_ mode: String) {
        manager.stopMonitoringSignificantLocationChanges()
        manager.stopUpdatingLocation()
        // v211: 距離モード（"50"/"150"/"500"）。旧値 important→500 / frequent→50 を互換で受ける。
        let m = (mode == "important") ? "500" : (mode == "frequent") ? "50" : mode
        if m == "500" {
            // 約500m・SLC＝基地局ベースで電池にやさしい・アプリ完全終了でも復帰
            manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
            if CLLocationManager.significantLocationChangeMonitoringAvailable() {
                manager.startMonitoringSignificantLocationChanges()
            }
        } else if let dist = Double(m) {   // "50" / "150"
            manager.desiredAccuracy = kCLLocationAccuracyBest
            manager.distanceFilter = dist   // その距離動いたら1点＝静止中は間引かれる
            manager.startUpdatingLocation()
            if CLLocationManager.significantLocationChangeMonitoringAvailable() {
                manager.startMonitoringSignificantLocationChanges()   // 完全終了からの復帰の保険
            }
        }
    }

    private func append(_ loc: CLLocation) {
        var buf = defaults.array(forKey: bufKey) as? [[String: Any]] ?? []
        buf.append([
            "id": UUID().uuidString,
            "t": loc.timestamp.timeIntervalSince1970 * 1000.0,
            "lat": loc.coordinate.latitude,
            "lng": loc.coordinate.longitude,
            "acc": loc.horizontalAccuracy
        ])
        if buf.count > 5000 { buf.removeFirst(buf.count - 5000) }   // 暴走防止の上限
        defaults.set(buf, forKey: bufKey)
    }

    // MARK: - CLLocationManagerDelegate
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        for loc in locations where loc.horizontalAccuracy >= 0 { append(loc) }
    }

    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) { /* 次の点を待つ */ }

    @available(iOS 14.0, *)
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if wantsAlways, manager.authorizationStatus == .authorizedWhenInUse {
            manager.requestAlwaysAuthorization()   // WhenInUse が下りたら Always に昇格要求
        }
        let mode = defaults.string(forKey: modeKey) ?? "off"
        if mode != "off" { applyMode(mode) }   // v211: 距離値/旧値どちらも applyMode が解釈
    }

    private static func string(_ s: CLAuthorizationStatus) -> String {
        switch s {
        case .authorizedAlways: return "always"
        case .authorizedWhenInUse: return "whenInUse"
        case .denied: return "denied"
        case .restricted: return "restricted"
        case .notDetermined: return "notDetermined"
        @unknown default: return "unknown"
        }
    }
}

/// Capacitor ブリッジ（薄い窓口）。本体は BackgroundLocationManager.shared。
@objc(BackgroundLocationPlugin)
public class BackgroundLocationPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "BackgroundLocationPlugin"
    public let jsName = "BackgroundLocation"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "requestAlways", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "start", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "stop", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "drain", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "status", returnType: CAPPluginReturnPromise)
    ]

    /// 起動時（背景再起動を含む）に singleton を生かし、保存モードを再アームさせる。
    public override func load() {
        _ = BackgroundLocationManager.shared
    }

    @objc func requestAlways(_ call: CAPPluginCall) {
        DispatchQueue.main.async { BackgroundLocationManager.shared.requestAlways() }
        call.resolve(["status": BackgroundLocationManager.shared.statusString()])
    }

    @objc func start(_ call: CAPPluginCall) {
        let mode = call.getString("mode") ?? "500"
        DispatchQueue.main.async { BackgroundLocationManager.shared.start(mode) }
        call.resolve(["ok": true, "status": BackgroundLocationManager.shared.statusString()])
    }

    @objc func stop(_ call: CAPPluginCall) {
        DispatchQueue.main.async { BackgroundLocationManager.shared.stop() }
        call.resolve(["ok": true])
    }

    @objc func drain(_ call: CAPPluginCall) {
        call.resolve(["points": BackgroundLocationManager.shared.drain()])
    }

    @objc func status(_ call: CAPPluginCall) {
        call.resolve(["status": BackgroundLocationManager.shared.statusString()])
    }
}
