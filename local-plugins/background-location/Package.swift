// swift-tools-version: 5.9
import PackageDescription

// 重要: パッケージ名・プロダクト名は cap sync が npm 名 "background-location" から
// 導出する "BackgroundLocation" と必ず一致させること。CapApp-SPM 側が
//   .package(name: "BackgroundLocation", path: ...)
//   .product(name: "BackgroundLocation", package: "BackgroundLocation")
// を生成して参照するため、ここが違うと SPM 解決が失敗し
// xcodebuild -showBuildSettings が exit 74 で落ちる（PhotoLibrary/media と同じ規約で名前一致）。
let package = Package(
    name: "BackgroundLocation",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "BackgroundLocation",
            targets: ["BackgroundLocation"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "BackgroundLocation",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/BackgroundLocationPlugin")
    ]
)
