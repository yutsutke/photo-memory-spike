# CHANGELOG — 写真思い出スパイク

> spike なので軽量に。バージョン番号は commit 順 (v1, v2, ...) で振る。
> 詳細は git log/diff で見られるので「背景・設計判断・教訓・残課題」だけ書く。

---

## v2 — スマホで change が拾えない問題を修正 (2026-05-28)

**背景**
- スマホ実機で写真を選んでも `📥 N件選択` が出ず `未選択` のまま。
- Phase 0 の検証が全くできない状態だった。

**設計判断**
- 原因仮説2つ:
  1. ESM CDN (`exifr@7/dist/lite.esm.js`) がロード失敗 → change handler が attach されない
  2. `display:none` した `<input type="file">` が iOS Safari で picker を確実に開けない
- どちらが本命か切り分ける時間より、両方塞ぐ方が早いと判断。
- exifr は **mini.umd.js** (UMD, JPEG + EXIF, グローバル公開) に変更。
  - `full` まで盛らない理由: Phase 0 は「日時/GPS が読めるか」の signal だけ欲しいので JPEG だけで足りる。HEIC は混じったら ERR として観測する (それも signal)。
- file input は可視化し、ボタンも併設して tap target を確保。
- 起動時に「✅ exifr OK」/「❌ exifr 読めない」を画面表示 (CDN 失敗の可視化)。
- change 発火直後に「N 件選択」を即時表示 (parse 前)。これでどこまで処理が進んだか切り分けられる。
- `window.error` / `unhandledrejection` をログ DOM に出す (スマホで console を開けない用)。

**教訓**
- spike のフロントで CDN ESM dynamic import に依存しない。UMD + `<script src>` が頑健。
- スマホ実機検証では「change 発火したか」「parse でコケたか」を画面上で見える化しないとデバッグできない。

**残課題**
- スマホからアクセスする手段がまだ決まってない (GitHub Pages 有効化 / LAN / Cloudflare Pages)。
- 実際に写真投げて日時取得率を測定するのはここから。

---

## v1 — Phase 0 最小HTML + リポジトリ初期化 (2026-05-28)

**背景**
- 写真マップ案からピボット。前案と基層 (EXIF索引) は共通だが、UX は「ランダム3枚 + 連想ウォーク」に差し替え。
- 地図ビューも汎用プラットフォーム化もしない。reminiscence の手触りだけ最短で触る。

**設計判断**
- norireco とは独立プロジェクトとして `C:\Users\yutsu\Documents\GitHub\photo-memory-spike\` に作成。
- GitHub に private リポジトリも作成 (`yutsutke/photo-memory-spike`)。spike だが履歴は欲しい。
- Phase 0 は `index.html` 単体。バンドラなし、CDN から exifr 直接 import。
- スコープ境界を README/TODO の冒頭に明記 (作らないもの一覧)。spike 中に膨らませない歯止め。

**残課題**
- スマホ実機検証で問題発覚 → v2 へ。
