# 🍎 iOS（iPhone）版のビルドと TestFlight（receipt-spike）

> **なぜ作るか**: iPhone のブラウザ版は Safari の ITP（**7日間開かないとサイトデータを消す**）でキーとレシートを失う。
> アプリ版のデータは「サイトデータ」ではなく**アプリ領域**＝自動削除の対象外。Android で先に解決した問題を iPhone でも解決する。
>
> **Mac は要らない**。「あの日」で通した道（Codemagic の macOS マシンでビルド → 自動署名 → TestFlight）をそのまま使う。
> 違いは3つだけ＝ ①作業場所が `receipt-spike/` ②バンドルIDが `io.github.yutsutke.receipt` ③プラグインは share と filesystem だけ。

- **appId（バンドルID）**: `io.github.yutsutke.receipt`
- **ホーム画面の表示名**: レシート（`CFBundleDisplayName`）
- **権限**: カメラ（📷 レシートを撮る）・写真（🖼 選ぶ）だけ。位置は使わない
- **ワークフロー**: リポ直下 `codemagic.yaml` の **`receipt-ios-testflight`**

---

## A. Apple 側（ブラウザでの手作業・一度だけ）

**⚠ ここは自動化できない**。App Store Connect に**アプリのレコード**が無いと、ビルドを上げても弾かれる。

1. **バンドルIDを登録**（すでにあれば飛ばす）
   [developer.apple.com](https://developer.apple.com/account/resources/identifiers/list) → Certificates, Identifiers & Profiles → **Identifiers** → ＋ → **App IDs** → **App**
   - Description: `Receipt`
   - Bundle ID: **Explicit** → `io.github.yutsutke.receipt`
   - Capabilities: **何も付けない**（プッシュ通知もサインインも使わない）

2. **App Store Connect でアプリを作る**
   [App Store Connect](https://appstoreconnect.apple.com/apps) → マイApp → ＋ → 新規App
   - プラットフォーム: **iOS**
   - **名前**: ⚠ **App Store 上の名前は世界で一意**（「レシート」単体は取られている可能性が高い）。例:「レシート — 撮ってAIで読む」。
     **ホーム画面の表示名（レシート）とは別物**で、審査に出す前なら何度でも変えられる
   - 主言語: **日本語**
   - バンドルID: 上で作った `io.github.yutsutke.receipt`
   - SKU: 任意（例 `receipt-spike`）

## B. ビルド（Codemagic・押すだけ）

リポはすでに接続済み。**あの日と同じ資格情報をそのまま使う**（同じ Apple チームのため）:
- App Store Connect 統合（`MadeleineASC`）
- 環境変数グループ `signing` の `CERTIFICATE_PRIVATE_KEY`（配布証明書の秘密鍵）

1. Codemagic → このリポ → ワークフロー **「レシート iOS → TestFlight」** を選ぶ
2. **Start new build**（20〜30分・無料枠 500分/月の範囲）
3. 終わると **TestFlight に自動でアップロード**される（審査提出はしない設定）

## C. iPhone で受け取る（TestFlight）

1. App Store Connect → 対象アプリ → **TestFlight** → **内部テスト** → グループを作り、**自分を追加**
   - **内部テスト＝審査なしで即配信**（自分＋チームの人まで）
   - 知人にも配るなら「外部テスト」＝ **Beta App Review**（通常1日程度）が要る
2. iPhone に **TestFlight アプリ**を入れて、招待から「レシート」をインストール

---

## つまずきやすい所（あの日で踏んだもの）

| 症状 | 原因 / 対処 |
|---|---|
| `archive` が **exit 65** で落ちる | `CERTIFICATE_PRIVATE_KEY` が未設定＝証明書を作れない。Codemagic の Secure 環境変数（グループ `signing`）を確認 |
| `No matching profiles found` | `fetch-signing-files` に `--create` が付いているか（新規バンドルIDは作成が要る） |
| アップロードで弾かれる | **App Store Connect にアプリレコードが無い**（手順A-2） |
| `xcodebuild -scheme App` が見つからない | 共有スキーム（`ios/App/App.xcodeproj/xcshareddata/xcschemes/App.xcscheme`）が要る＝**コミット済み** |
| `agvtool` がビルド番号を上げられない | `VERSIONING_SYSTEM = apple-generic` が要る＝**設定済み** |
| 同じバージョンで再アップロードできない | **公開済みの版番号にはビルドを足せない**。`MARKETING_VERSION`（いま 1.0）を上げる。ビルド番号は Codemagic の連番で自動 |
| 輸出コンプライアンスを毎回聞かれる | `ITSAppUsesNonExemptEncryption = false` を Info.plist に入れてある（HTTPS のみ＝独自の暗号なし） |

## ⚠ 将来 App Store に「公開」するときの論点（TestFlight では不要）

このアプリは **BYOK（本人の APIキーを登録して使う）**＝キーが無いと分析が動かない。審査では
「**レビュー担当者が全機能を試せること**」が求められる（Guideline 2.1）ので、**審査メモにデモ用のキーか、
キー無しでも中身が見える手順**を書く必要がある。内部テスト（TestFlight）では自分のキーを使うので問題にならない。

## Windows で足場を作った記録（2026-08-11）

`npx cap add ios` は **Windows でも通る**（Capacitor 8 は CocoaPods ではなく SPM ＝ `pod install` が要らない）。
Mac が要るのは**ビルドの瞬間だけ**で、そこは Codemagic の macOS マシンが担当する。
