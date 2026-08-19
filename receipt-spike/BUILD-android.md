# 📱 Android アプリのビルドと配布（receipt-spike）

> **なぜ native にしたか**: ブラウザ版は iOS Safari の ITP（**7日間開かないとサイトデータを消す**）でキーとレシートを失う。
> アプリ版のデータは「サイトデータ」ではなく**アプリ領域**に入るので自動削除の対象外（消えるのはアプリを削除した時だけ）。
> ログイン方式（サーバー保存）は採らない＝**データは端末から出さない**方針は web spike のまま。

- **appId**: `io.github.yutsutke.receipt`（あの日 `io.github.yutsutke.madeleine` とは別アプリ）
- **アプリ名**: レシート
- **権限**: `INTERNET` のみ。**CAMERA は宣言しない**（Capacitor の `isMediaCaptureSupported()` は「宣言していなければ許可不要」＝OS のカメラアプリを intent で呼ぶ。宣言すると逆に実行時許可が必須になり、こちらは要求コードを持たないので撮影が壊れる）

---

---

## 🏪 Google Play（内部テスト）に出す

### 焼いた版の記録（⚠ versionCode は一度使うと二度と使えない＝次は必ず +1）

| versionCode | versionName | 中身の web | 焼いた日 | 置き場 |
|---|---|---|---|---|
| **vc2** | 1.0 | **r79** | 2026-08-19 | `receipt-signing\receipt-1.0-vc2.aab` / `.apk` |
| vc1 | 1.0 | r70 | 2026-08-17 | `receipt-signing\receipt-1.0-vc1.aab` / `.apk` |

⚠ **vc2 には r71〜r79 の9版ぶんが入っている**（🧹まとめて削除／📄ポリシー／新アイコン／🔁📋 食事のコピーと繰り返し／📈推移／📷保存サイズ／🩹 書き起こしが止まる件の一連）。

### 元の手引き（vc1 を作った時の記録・手順は同じ）

> **debug APK の直接配布とは別の道**。Play に出すには **署名付きの AAB** が要る（debug 鍵では弾かれる）。
> あの日（madeleine）と**同じ流儀・別の鍵**（別アプリなので鍵も別）。

### 鍵の置き場（この PC・git 管理外）

```
C:\Users\yutsu\Documents\receipt-signing\
  receipt-upload.jks      ← アップロード鍵（2026-08-17 作成・有効期限 10000日）
  CREDENTIALS.txt         ← 人が読む控え（パス・別名・パスワード）
  receipt-1.0-vc1.aab     ← Play に上げる物
  receipt-1.0-vc1.apk     ← 同じ中身の署名付き APK（実機に直接入れて試す用）
```

- ⚠ **失うと同じ鍵で更新できない**。ただし **Play App Signing** を使うので、アップロード鍵は Play で再設定できる（配布用の鍵は Google が持つ）＝致命傷にはならない。それでも `receipt-signing` フォルダごとバックアップしておくこと。
- ⚠ **PKCS12 形式では鍵のパスワード＝ストアのパスワード**（`keytool -keypass` は黙って無視される）。別々に指定したつもりで gradle が `Given final block not properly padded` で落ちるのはこれが原因（2026-08-17 に踏んだ）。
- 証明書の SHA-256: `a7:91:11:90:57:94:89:bc:a3:3f:1b:b3:ee:2d:1a:24:a0:40:b2:56:e5:e2:ff:c7:17:3e:81:42:e7:3b:bd:4b`

### AAB を作る（PowerShell・1コールで流す）

⚠ **PowerShell の env は呼び出しをまたいで残らない**＝資格情報の設定と `gradlew` は**同じ1コール**にする（別々に打つと未署名の AAB ができて Play に弾かれる）。

```powershell
cd C:\Users\yutsu\Documents\GitHub\photo-memory-spike\receipt-spike
npm run sync:web ; npx cap sync android
$cred = Get-Content C:\Users\yutsu\Documents\receipt-signing\CREDENTIALS.txt
$get = { param($l) ($cred | Where-Object { $_ -match "^$l\s*:" } | Select-Object -First 1) -replace "^$l\s*:\s*", '' }
$env:CM_KEYSTORE_PATH = (& $get 'Keystore file'); $env:CM_KEYSTORE_PASSWORD = (& $get 'Store password')
$env:CM_KEY_ALIAS = (& $get 'Key alias'); $env:CM_KEY_PASSWORD = (& $get 'Key password')
$env:BUILD_NUMBER = "1"   # ← Play に上げるたびに +1（同じ番号は二度と使えない）
.\android\gradlew.bat -p android bundleRelease --no-daemon
```

出力: `android\app\build\outputs\bundle\release\app-release.aab`

### 上げる前の検証3点（毎回やる）

| 見るもの | 通っている状態 | 見かた |
|---|---|---|
| **署名されているか** | `META-INF/RECEIPT-.RSA` がある | AAB を zip として開いて `META-INF/` を見る |
| **versionCode** | 前回より大きい | 同じ設定で `assembleRelease` した APK を `aapt2 dump badging` |
| **中身の web の版** | 出したい版（例 r70） | AAB 内 `base/assets/public/index.html` の先頭の `const BUILD` |

⚠ **AAB のマニフェストは protobuf 形式で `aapt2` が読めない**（`could not identify format of APK` になる）。versionCode を見たい時は、同じ env のまま `assembleRelease` も流して**その APK を** `aapt2 dump badging` する（ついでに実機用の署名付き APK も手に入る）。

### Play Console の手順（ブラウザ・人がやる）

1. **アプリを作る**（初回だけ）— [Play Console](https://play.google.com/console) → **アプリを作成**
   - アプリ名: **`レシートと健康— 撮ってAIで読む`**（2026-08-17 にこの名前で登録）。⚠ **Play 上の名前**であって、ホーム画面の表示名「レシート」（`strings.xml` の `app_name`）とは別物。⚠ **iOS（App Store Connect）は「レシート — 撮ってAIで読む」のまま**＝r65 で 🩺 健康が3本目の柱になったのを名前に反映したのは Play 側だけ。揃えるなら ASC でも改名する（審査前なら何度でも変えられる）
   - 言語: 日本語 / **アプリ**（ゲームではない）/ **無料**
   - ⚠ パッケージ名は**最初のアップロードで確定**＝`io.github.yutsutke.receipt`（あとから変えられない）
2. **内部テスト** → **新しいリリースを作成**
   - **Play App Signing**：初回に「続行」＝Google が配布用の鍵を管理し、こちらが上げるのは**アップロード鍵で署名した AAB**（今回作った鍵）
   - `receipt-1.0-vc1.aab` を D&D
   - リリース名（例 `1.0 (1) r70`）とリリースノートを書く
3. **テスターを追加** — 内部テスト → テスター → メールアドレスのリスト（自分を入れる）→ **リンクをコピー**して端末で開く
4. **審査は無いが、公開前に「アプリのコンテンツ」を埋める必要がある**（内部テストでも必須の項目がある）:
   - **プライバシーポリシー**の URL ／ **データセーフティ**（下記）／ 広告の有無（**なし**）／ コンテンツのレーティング ／ 対象年齢 ／ ニュースアプリか（いいえ）
5. **公開** → テスターの端末に届く（数分〜数時間）

### ⚠ データセーフティの書き方（このアプリ固有）

このアプリは**サーバーを持たない**が、**BYOK（本人の APIキー）で本人が選んだ AI に写真を送る**。ここを正直に書く:

- 収集する（アプリ運営者が集める）データ：**なし**
- **共有**（第三者に送られる）：**写真**（レシート・料理・健診・薬）と、そこから読み取ったテキスト。送り先は**利用者自身が ⚙ で選んだ AI 事業者**（Anthropic / OpenAI / OpenRouter 等）。送るのは**利用者の操作時のみ**、利用者自身の APIキーで**端末から直接**。
- 暗号化：転送時は HTTPS。端末内のデータは**端末から出ない**（IndexedDB）
- 削除依頼：アプリを削除すればデータも消える（サーバーに無い）

⚠ **「収集なし」だけで済ませない**＝写真が外部の AI へ出るのは事実。あの日 v1 の「収集なし」クリーン審査とは事情が違う。

### 実機に直接入れて試す（Play を通さず）

`receipt-1.0-vc1.apk` を端末へ送ってインストール。⚠ **debug APK とは署名が違う**ので、debug 版が入っている端末では**一度アンインストール**が要る。

---

## 手元でビルド（Android Studio 不要）

前提: Node 22+ と JDK 21（Capacitor 8 の capacitor-android は Java 21 ソースレベル）、Android SDK（`ANDROID_HOME`）＝**vc4 を作った環境がそのまま使える**。

### Windows（PowerShell・vc4 と同じ流儀）

```powershell
git checkout main && git pull               # receipt-spike は main にマージ済み（2026-08-10）
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

> ⚠ 初回だけ `android/local.properties` に SDK の場所が要る（git 管理外・この PC 固有）:
> `sdk.dir=C\:\\Users\\yutsu\\AppData\\Local\\Android\\Sdk`

### 端末へ入れる

- **adb があるなら**: `adb install -r android/app/build/outputs/apk/debug/app-debug.apk`（`-r`=上書き更新）
- **無ければ**: APK を Drive/メール等で端末に送り、タップ →「提供元不明のアプリ」を許可 → インストール

#### 📶 ケーブルが使えない時＝無線デバッグ（2026-08-10 実績・手持ちのケーブルが充電専用だった）

PC と端末が**同じ Wi-Fi** にいること。端末: 開発者向けオプション → **ワイヤレス デバッグ** を ON。

```powershell
adb mdns services                                  # ★まずこれ。ペア済みなら接続用ポートごと見つかる
adb connect 192.168.x.x:<接続用ポート>             # 例: _adb-tls-connect._tcp 192.168.10.15:37825
adb pair 192.168.x.x:<ペア用ポート> <6桁コード>    # 未ペアの時だけ（画面の「専用コードによるペア設定」）
adb -s 192.168.x.x:<接続用ポート> install -r ...
```

> ⚠ **`adb pair` が `protocol fault (couldn't read status message)` で落ちたら、たいてい「もうペア済み」**（2026-08-11）。`adb mdns services` を先に見れば、ペア用ポートと接続用ポートを取り違える罠ごと消える。

- ポート番号は**ペア用と接続用で別物**（ここでよく間違える）
- 端末が Windows のデバイス一覧にすら出ない時は、ケーブルが充電専用＝USBデバッグの設定を疑う前にケーブルを疑う
- スクショは `adb shell screencap -p /data/local/tmp/x.png` → `adb pull`（**PowerShell の `>` はバイナリを壊す**ので `exec-out >` を使わない）

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
