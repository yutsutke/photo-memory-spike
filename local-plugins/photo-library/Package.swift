// swift-tools-version: 5.9
import PackageDescription

// 重要: パッケージ名・プロダクト名は cap sync が npm 名 "photo-library" から
// 導出する "PhotoLibrary" と必ず一致させること。CapApp-SPM 側が
//   .package(name: "PhotoLibrary", path: ...)
//   .product(name: "PhotoLibrary", package: "PhotoLibrary")
// を生成して参照するため、ここが違うと SPM 解決が失敗し
// xcodebuild -showBuildSettings が exit 74 で落ちる（media も同じ規約で名前一致）。
let package = Package(
    name: "PhotoLibrary",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "PhotoLibrary",
            targets: ["PhotoLibrary"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "PhotoLibrary",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/PhotoLibraryPlugin")
    ]
)
