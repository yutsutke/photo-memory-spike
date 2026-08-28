// swift-tools-version: 5.9
import PackageDescription

// 重要: パッケージ名・プロダクト名は cap sync が npm 名 "folder-sink" から導出する
// "FolderSink" と必ず一致させること。CapApp-SPM 側が
//   .package(name: "FolderSink", path: ...) / .product(name: "FolderSink", package: "FolderSink")
// を生成して参照するため、ここが違うと SPM 解決が失敗し
// xcodebuild -showBuildSettings が exit 74 で落ちる（photo-library / app-shortcuts と同じ罠）。
let package = Package(
    name: "FolderSink",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "FolderSink",
            targets: ["FolderSink"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "FolderSink",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/FolderSinkPlugin")
    ]
)
