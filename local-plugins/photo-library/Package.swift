// swift-tools-version: 5.9
import PackageDescription

// ローカル Capacitor プラグイン（Approach B）。
// cap sync が CapApp-SPM/Package.swift の依存にこのパッケージを追加し、
// xcodebuild が SPM で解決してビルドする（pod 不要・pbxproj 手編集も不要）。
let package = Package(
    name: "PhotoLibraryPlugin",
    platforms: [.iOS(.v15)],
    products: [
        .library(
            name: "PhotoLibraryPlugin",
            targets: ["PhotoLibraryPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "8.0.0")
    ],
    targets: [
        .target(
            name: "PhotoLibraryPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/PhotoLibraryPlugin")
    ]
)
