// swift-tools-version: 5.9
import PackageDescription

// 重要: パッケージ名・プロダクト名は cap sync が npm 名 "app-shortcuts" から
// 導出する "AppShortcuts" と必ず一致させること。CapApp-SPM 側が
//   .package(name: "AppShortcuts", path: ...)
//   .product(name: "AppShortcuts", package: "AppShortcuts")
// を生成して参照するため、ここが違うと SPM 解決が失敗し
// xcodebuild -showBuildSettings が exit 74 で落ちる（photo-library と同じ規約）。
let package = Package(
    name: "AppShortcuts",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "AppShortcuts",
            targets: ["AppShortcuts"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "AppShortcuts",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/AppShortcutsPlugin")
    ]
)
