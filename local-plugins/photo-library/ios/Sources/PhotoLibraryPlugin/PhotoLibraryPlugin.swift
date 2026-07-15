import Foundation
import UIKit          // UIImage.jpegData / CGSize
import CoreLocation   // CLLocation.coordinate
import Capacitor
import Photos

/// 自前 Photos プラグイン（Approach B）。
/// 本命アーキ＝「全件メタデータを即時列挙（カウントは一瞬）＋サムネは画面に出る分だけオンデマンド」。
/// community/media の "全サムネ base64 一括" のメモリ/速度問題を避けるための土台。
@objc(PhotoLibraryPlugin)
public class PhotoLibraryPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "PhotoLibraryPlugin"
    public let jsName = "PhotoLibrary"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "requestAccess", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "enumerate", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "thumbnail", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "thumbnails", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "savePhoto", returnType: CAPPluginReturnPromise)
    ]

    private let iso = ISO8601DateFormatter()

    /// 写真ライブラリの読み取り許可を要求し、状態文字列を返す。
    @objc func requestAccess(_ call: CAPPluginCall) {
        let respond: (PHAuthorizationStatus) -> Void = { status in
            call.resolve(["status": PhotoLibraryPlugin.statusString(status)])
        }
        if #available(iOS 14, *) {
            PHPhotoLibrary.requestAuthorization(for: .readWrite, handler: respond)
        } else {
            PHPhotoLibrary.requestAuthorization(respond)
        }
    }

    /// 全画像を「メタデータだけ」列挙する。count は遅延評価で一瞬。
    /// items はサムネを含まない軽量配列（id/日時/GPS/サイズ）。limit=0 で全件。
    @objc func enumerate(_ call: CAPPluginCall) {
        let limit = call.getInt("limit") ?? 0   // 0 = 全件
        DispatchQueue.global(qos: .userInitiated).async {
            let t0 = Date()
            let options = PHFetchOptions()
            options.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: false)]
            let result = PHAsset.fetchAssets(with: .image, options: options)
            let total = result.count                                  // 真の全件数（即時）
            let cap = limit > 0 ? min(limit, total) : total
            var items: [[String: Any]] = []
            items.reserveCapacity(cap)
            if cap > 0 {
                result.enumerateObjects(at: IndexSet(integersIn: 0..<cap), options: []) { asset, _, _ in
                    var m: [String: Any] = [
                        "id": asset.localIdentifier,
                        "w": asset.pixelWidth,
                        "h": asset.pixelHeight
                    ]
                    if let d = asset.creationDate { m["date"] = self.iso.string(from: d) }
                    if let loc = asset.location {
                        m["lat"] = loc.coordinate.latitude
                        m["lng"] = loc.coordinate.longitude
                    }
                    items.append(m)
                }
            }
            let ms = Int(Date().timeIntervalSince(t0) * 1000)
            call.resolve([
                "count": total,
                "returned": items.count,
                "ms": ms,
                "items": items
            ])
        }
    }

    /// 1枚だけサムネをオンデマンド生成し data URL で返す（id は localIdentifier）。
    @objc func thumbnail(_ call: CAPPluginCall) {
        guard let id = call.getString("id") else {
            call.reject("id required")
            return
        }
        let size = call.getInt("size") ?? 200
        let fetch = PHAsset.fetchAssets(withLocalIdentifiers: [id], options: nil)
        guard let asset = fetch.firstObject else {
            call.reject("asset not found")
            return
        }
        let opts = PHImageRequestOptions()
        opts.deliveryMode = .highQualityFormat
        opts.resizeMode = .fast
        opts.isNetworkAccessAllowed = true   // iCloud 上の写真も取得（必要分だけ）
        opts.isSynchronous = false
        let target = CGSize(width: size, height: size)
        var done = false
        PHImageManager.default().requestImage(
            for: asset,
            targetSize: target,
            contentMode: .aspectFill,
            options: opts
        ) { image, info in
            if done { return }
            if let image = image, let data = image.jpegData(compressionQuality: 0.7) {
                done = true
                call.resolve(["dataUrl": "data:image/jpeg;base64," + data.base64EncodedString()])
            } else if let error = info?[PHImageErrorKey] as? Error {
                done = true
                call.reject("thumbnail failed: \(error.localizedDescription)")
            }
            // image==nil かつ error 無しの中間コールバックは無視し、最終結果を待つ
        }
    }

    /// v228: バッチ版 thumbnail。N枚を1往復で返す (Android と同契約)。
    /// PHImageManager へ一斉に要求＝内部で並列デコード。ブリッジ往復は 1/N に。
    /// items は ids と同順・失敗した枚だけ {id, error} (バッチ全体は落とさない)。
    @objc func thumbnails(_ call: CAPPluginCall) {
        guard let idsAny = call.getArray("ids"), let ids = idsAny as? [String], !ids.isEmpty else {
            call.reject("ids required")
            return
        }
        let size = call.getInt("size") ?? 200
        DispatchQueue.global(qos: .userInitiated).async {
            let fetch = PHAsset.fetchAssets(withLocalIdentifiers: ids, options: nil)
            var byId: [String: PHAsset] = [:]
            fetch.enumerateObjects { asset, _, _ in byId[asset.localIdentifier] = asset }
            let opts = PHImageRequestOptions()
            opts.deliveryMode = .highQualityFormat
            opts.resizeMode = .fast
            opts.isNetworkAccessAllowed = true   // iCloud 上の写真も取得（必要分だけ）
            opts.isSynchronous = false
            let target = CGSize(width: size, height: size)
            let group = DispatchGroup()
            let lock = NSLock()
            var results: [String: [String: Any]] = [:]
            for id in ids {
                guard let asset = byId[id] else {
                    lock.lock(); results[id] = ["id": id, "error": "asset not found"]; lock.unlock()
                    continue
                }
                group.enter()
                var done = false
                PHImageManager.default().requestImage(
                    for: asset,
                    targetSize: target,
                    contentMode: .aspectFill,
                    options: opts
                ) { image, info in
                    if done { return }
                    if let image = image, let data = image.jpegData(compressionQuality: 0.7) {
                        done = true
                        lock.lock(); results[id] = ["id": id, "dataUrl": "data:image/jpeg;base64," + data.base64EncodedString()]; lock.unlock()
                        group.leave()
                    } else if let error = info?[PHImageErrorKey] as? Error {
                        done = true
                        lock.lock(); results[id] = ["id": id, "error": error.localizedDescription]; lock.unlock()
                        group.leave()
                    }
                    // image==nil かつ error 無しの中間コールバックは無視し、最終結果を待つ (thumbnail と同じ)
                }
            }
            group.wait()
            let items: [[String: Any]] = ids.map { results[$0] ?? ["id": $0, "error": "no result"] }
            call.resolve(["items": items])
        }
    }

    /// 撮った写真データ（base64 JPEG）を端末の写真ライブラリ（カメラロール）に保存する（v170・重ね撮り用）。
    /// 追加のみ権限（.addOnly）を確認してから保存。Info.plist の NSPhotoLibraryAddUsageDescription が文言。
    @objc func savePhoto(_ call: CAPPluginCall) {
        guard let b64 = call.getString("base64"), let data = Data(base64Encoded: b64) else {
            call.reject("base64 required")
            return
        }
        let write: () -> Void = {
            PHPhotoLibrary.shared().performChanges({
                let req = PHAssetCreationRequest.forAsset()
                req.addResource(with: .photo, data: data, options: nil)
            }, completionHandler: { success, error in
                if success {
                    call.resolve(["saved": true])
                } else {
                    call.reject("save failed: \(error?.localizedDescription ?? "unknown")")
                }
            })
        }
        if #available(iOS 14, *) {
            PHPhotoLibrary.requestAuthorization(for: .addOnly) { status in
                if status == .authorized || status == .limited {
                    write()
                } else {
                    call.reject("no photo add permission")
                }
            }
        } else {
            PHPhotoLibrary.requestAuthorization { status in
                if status == .authorized { write() } else { call.reject("no photo permission") }
            }
        }
    }

    private static func statusString(_ s: PHAuthorizationStatus) -> String {
        switch s {
        case .authorized: return "authorized"
        case .limited: return "limited"
        case .denied: return "denied"
        case .restricted: return "restricted"
        case .notDetermined: return "notDetermined"
        @unknown default: return "unknown"
        }
    }
}
