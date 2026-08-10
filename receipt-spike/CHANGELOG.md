# レシート spike — CHANGELOG

> あの日（リポ直下）の CHANGELOG とは独立。こちらは r1, r2, … で進む。

## r1 — 撮る→自動AI分析→一覧→期間書き出し (2026-08-10)

**背景**
- 「あの日」の設計相談から派生。レシートを写メ→詳細がたまる→AI分析→統計地図分析、をやりたいが、核3要素（偶然/久しぶり/よみがえる）と正面衝突するため**別アプリ**と決定（経緯はリポ直下 CHANGELOG v233・確定方針は本フォルダ TODO.md）。
- まず web spike で「撮る→自動で読める→取り出せる」の一周が気持ちいいかを確かめる。

**設計判断**
- **BYOK直接送信**: ユーザーのAPIキーで端末→Anthropic API へ直接 fetch。サーバー・中継なし。CORS は公式オプトイン（`anthropic-dangerous-direct-browser-access: true`）。SDKもバンドラも使わない生 fetch（単一HTMLファイル・CDN依存ゼロの流儀を踏襲）。
- **structured outputs（output_config.format=json_schema）**で店名/日付/合計/通貨/カテゴリ/支払/品目を抽出。プロンプトで形式を頼むのではなくAPIレベルでスキーマ保証＝JSON.parse が落ちない。nullable は anyOf で表現（type配列は仕様上未保証のため）。
- **モデルは⚙で選択**（既定 Opus 5 / Sonnet 5 / Haiku 4.5）。コスト目安を選択肢に併記（1568px縮小で画像≈1600トークン→Opus5で2〜3円/枚）。effort=low を付与（Haiku 4.5 は effort 非対応のため除外分岐）。
- **接続テストは count_tokens**（無料エンドポイント）＝キー検証に課金させない。
- **画像は1568px長辺に縮小して送信**（visionの推奨解像度・トークン最小化）。保存も縮小後のみ（原本は端末の写真アプリに残る前提）。
- 一覧の並びは**レシート記載の日付**（撮影日ではなく）。月グループ＋月合計（JPYのみ合算）。
- 書き出しは**バージョン付きJSON**（`format: receipt-spike-export, version: 1`）を正とし、CSV(BOM付き)とOS共有シートを添える。共有シートがAIアプリへの「渡し口」＝リンクのホストはしない。

**ハマり回避（あの日の教訓を先回りで適用）**
- `createImageBitmap` 不使用 → `<img>`+`<canvas>`+`URL.createObjectURL`（+必ず revoke）。
- `e.target.value = ''` は `finally` で（await 前に消すと File 参照が無効化される疑い）。
- IndexedDB/localStorage は同一オリジンで あの日 web と共有 → `receipt-spike` / `rcpt.*` で名前空間分離。

**結果 / 観察**
- preview 未確認・実機未確認（このセッションは remote 環境）。**次セッション: 実機 iPhone Safari で一周通すのが最初の仕事**。

**残課題 / 次の方向**
- 実機確認 → FBを r2 へ。支出マップ（Notion ライフログ構築メモの家計簿/日誌構想を参考）はデータが溜まってから。
- APIキーの置き場所: web spike は localStorage（XSS面は単一ファイル・外部依存ゼロで緩和）。native化時に SecureStorage へ。
