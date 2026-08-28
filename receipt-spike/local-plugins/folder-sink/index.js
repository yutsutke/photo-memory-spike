// 最小エントリ（バンドラ無し運用）。あの日の app-shortcuts / photo-library と同じ規約。
// index.html 側は window.Capacitor.registerPlugin('FolderSink') で直接叩く（JS ラッパー不要）。
// cap sync はこのファイルを実行せず、package.json の "capacitor" と Package.swift だけを読む。
module.exports = {};
