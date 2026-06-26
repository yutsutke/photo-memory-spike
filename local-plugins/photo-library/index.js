// 最小エントリ（バンドラ無し運用）。
// このプラグインは index.html 側で
//   window.Capacitor.registerPlugin('PhotoLibrary')
// として「ネイティブ登録済みプラグイン」に直接アクセスする（JS ラッパー不要）。
// cap sync はこのファイルを実行せず、package.json の "capacitor" フィールドと
// Package.swift を読んで iOS の SPM 依存に配線するだけ。ここは存在すれば良い。
module.exports = {};
