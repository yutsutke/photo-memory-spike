# 📱 Android アプリのビルドと配布（receipt-spike）

> **なぜ native にしたか**: ブラウザ版は iOS Safari の ITP（**7日間開かないとサイトデータを消す**）でキーとレシートを失う。
> アプリ版のデータは「サイトデータ」ではなく**アプリ領域**に入るので自動削除の対象外（消えるのはアプリを削除した時だけ）。
> ログイン方式（サーバー保存）は採らない＝**データは端末から出さない**方針は web spike のまま。

- **appId**: `io.github.yutsutke.receipt`（あの日 `io.github.yutsutke.madeleine` とは別アプリ）
- **アプリ名**: レシート
- **権限**: `INTERNET` のみ。**CAMERA は宣言しない**（Capacitor の `isMediaCaptureSupported()` は「宣言していなければ許可不要」＝OS のカメラアプリを intent で呼ぶ。宣言すると逆に実行時許可が必須になり、こちらは要求コードを持たないので撮影が壊れる）

---

## 手元でビルド（Android Studio 不要）

前提: Node 22+ と JDK 21（Capacitor 8 の capacitor-android は Java 21 ソースレベル）、Android SDK（`ANDROID_HOME`）＝**vc4 を作った環境がそのまま使える**。

### Windows（PowerShell・vc4 と同じ流儀）

```powershell
git fetch origin claude/start-ncjb6i        # ← receipt-spike は 2026-08-10 時点 main 未マージ＝このブランチにある
git checkout claude/start-ncjb6i            #    （merge 済みなら main のままでよい）
cd receipt-spike
npm install                                  # 初回のみ
npm run sync:web
npx cap sync android
.\android\gradlew.bat -p android assembleDebug --no-daemon
```

出力: `receipt-spike\android\app\build\outputs\apk\debug\app-debug.apk`

### Mac / Linux

```bash
cd receipt-spike
npm install            # 初回のみ
npm run build:debug    # sync:web → cap sync android → ./gradlew assembleDebug（⚠ Unix専用スクリプト）
```

### 端末へ入れる

- **adb があるなら**: `adb install -r android/app/build/outputs/apk/debug/app-debug.apk`（`-r`=上書き更新）
- **無ければ**: APK を Drive/メール等で端末に送り、タップ →「提供元不明のアプリ」を許可 → インストール

**デバッグ APK で配って構わない**（サイドロードに署名の種類は関係ない）。手間が最小で、リリース鍵の管理も要らない。

> ⚠ **更新して配り直す時は `versionCode` を上げる**（同じか古い版は端末が弾く）。`android/app/build.gradle` の値を直接上げるか、`BUILD_NUMBER=2 npm run build:debug` のように env で渡す。
> ⚠ **署名が変わると上書き更新できない**（アンインストールが必要になる）。debug 鍵は `~/.android/debug.keystore` ＝**毎回同じPCでビルド**すれば問題ない。

## 知り合いの端末に入れてもらう

1. APK を渡す（メール添付・Drive・LINE 等どれでも）
2. 受け取った側は「提供元不明のアプリ」を許可 → APK をタップ → インストール

### ⚠ 2026-09 以降の Android デベロッパー確認について

Google の「デベロッパー確認」が始まる。調べた結果（2026-08-10 時点）:

- **開始は 2026-09-30、まずブラジル・インドネシア・シンガポール・タイのみ**。米国含むグローバルは **2027年**
- **確認済みの開発者は、直接配布（サイドロード）の自由をそのまま維持できる**
- ユーザーは ADB 経由でならいつでもインストール可能

→ **オーナーは 2026-07-10 に本人確認済み（$25 決済済み）＝この配布は当面そのまま通る。日本は第一弾に入っていない。**
将来グローバル適用が近づいたら、Play Console で**このアプリ（パッケージ名＋署名鍵）を「Play 外配信アプリ」として登録**すれば継続できる。あの日の TODO にあった「対応不要」メモは *Play のみ配信だった頃*の判断なので、直接配布を始めた今は上記に読み替える。

---

## データの持ち出し（バックアップ）

アプリ版でもデータは端末内だけ＝**アンインストール・端末紛失で消える**。逃げ道を3つ用意してある（📤 期間を指定して書き出し）:

| 手段 | 使える場所 |
|---|---|
| 📤 共有シート | Web Share API が効く環境（未確認＝要実機） |
| 📄 JSON / 📊 CSV ダウンロード | ブラウザ版は確実。WebView は DownloadListener 未実装だと落ちる（要実機） |
| **📋 コピー** | **どこでも効く**（クリップボード）＝最後の逃げ道として r4 で追加 |

`android:allowBackup="true"`（Capacitor 既定のまま）なので、Google の自動バックアップで機種変更時にデータが移る可能性がある。これは OS が暗号化して扱うもので、こちらのサーバーではない（あの日のプライバシー方針と同じ扱い）。

---

## iOS はどうするか

同じ Capacitor 構成で `npx cap add ios` すれば作れるが、**配布が重い**（App Store 審査か TestFlight）。
当面は **ブラウザ版＋ホーム画面に追加**で回避する（ホーム画面に追加した PWA は ITP の7日削除の対象外）。⚙ にその案内を出している。
