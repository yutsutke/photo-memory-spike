# CHANGELOG — 写真思い出スパイク

> spike なので軽量に。バージョン番号は commit 順 (v1, v2, ...) で振る。
> 詳細は git log/diff で見られるので「背景・設計判断・教訓・残課題」だけ書く。

---

## v3 — Phase 0 クローズ: スマホ実機で日時/GPS/HEIC 読めること確認 (2026-05-28)

**背景**
- v2 で change は拾えるようになったが、スマホからアクセスする手段が未決のままだった。
- file:// で開くと CDN ロードがブロックされて「起動中…」のまま固まる症状を確認。
- 順に潰す必要があった: ホスティング → ブラウザキャッシュ → exifr ビルド選定 → GPS 仮説 → HEIC 検証。

**設計判断**
- **ホスティング: GitHub Pages**。
  - 当初 private repo のまま有効化を試みたが GitHub Free では不可 (Pro 以上必要)。
  - spike のコードは公開しても問題ない (HTML + docs のみ、写真は含まれない) ためリポを public 化。
  - `https://yutsutke.github.io/photo-memory-spike/` で配信。
- **HTML キャッシュ抑制メタタグを追加**。
  - 改修を push してもスマホ Safari が古い HTML を返し続けてデバッグが進まなかった。
  - `Cache-Control: no-cache, no-store, must-revalidate` + `Pragma` + `Expires: 0` を head に。spike 中は毎回フレッシュに取りたい。
- **exifr を mini → full ビルドに変更**。
  - mini で parse(f, true) を呼ぶと `segment parser 'jfif' was not loaded` で全件エラー。
  - mini に閉じ込めて parse オプションを絞り続けるより full に切る方が早い。bundle サイズ差は spike では誤差。
  - 副次的に HEIC EXIF も full 単体で読めることが判明 → heic2any は Phase 1 のサムネ描画で必要なら導入で OK。
- **GPS 0% は Safari strip ではなく元画像欠如だった**。
  - iPhone「写真」アプリで該当写真を開き「位置情報を追加…」が出たことで、元画像に GPS 自体が無いと確認。
  - 別撮りの GPS つき写真で再テストすると 100% 取れた。

**結果 (実機: iPhone iOS 26 系 / Safari)**
- JPEG 4枚: 日時 4/4 (100%), GPS 0/4 (元画像に無し)
- JPEG 1枚 (GPS つき): 日時 1/1, GPS 1/1 (35.6937,139.4174)
- HEIC 1枚: 日時 1/1, GPS 1/1, エラー 0
- exifr full ビルドは Safari ピッカー経由でも HEIC を直接パース可能。

**教訓**
- **private repo の GitHub Pages は GitHub Free プランでは使えない** (Pro/Team/Enterprise 必須)。
- **iOS Safari の HTML キャッシュは強烈**。改修したのに反映されない症状の常連。spike では no-cache メタを必ず入れる。
- **exifr mini は parse(f, true) と相性が悪い** (JFIF パーサー非搭載)。明示的にセグメント絞るより full の方が考えることが減る。
- **iOS Safari のフォトピッカーは HEIC を裏で JPEG 変換して渡す**。HEIC のまま渡したいときは「ファイル」アプリ経由で選ぶ必要がある。EXIF/GPS はどちらの経路でも保持される。
- 「GPS が読めない」を疑う前に、まず iPhone 写真アプリで該当写真に GPS があるかを確認すべき (撮影時の設定で記録されていないケースが多い)。

**残課題**
- Phase 1 (IndexedDB 保存 / サムネ生成 / ランダム3枚 / 連想展開) に進む。

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
