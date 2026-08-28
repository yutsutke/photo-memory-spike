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
| **vc8** | **1.1** | **r110** | 2026-08-29 | `receipt-signing\receipt-1.1-vc8.aab` / `.apk` |
| vc7 | 1.1 | r109 | 2026-08-29 | `receipt-signing\receipt-1.1-vc7.aab` / `.apk` |
| vc6 | 1.1 | r108 | 2026-08-29 | `receipt-signing\receipt-1.1-vc6.aab` / `.apk` |
| vc5 | 1.0 | r104 | 2026-08-23 | `receipt-signing\receipt-1.0-vc5.aab` / `.apk` |
| vc4 | 1.0 | r94 | 2026-08-21 | `receipt-signing\receipt-1.0-vc4.aab` / `.apk` |
| vc3 | 1.0 | r81 | 2026-08-20 | `receipt-signing\receipt-1.0-vc3.aab` / `.apk` |
| vc2 | 1.0 | r79 | 2026-08-19 | `receipt-signing\receipt-1.0-vc2.aab` / `.apk` |
| vc1 | 1.0 | r70 | 2026-08-17 | `receipt-signing\receipt-1.0-vc1.aab` / `.apk` |

⚠ **vc8 ＝ vc7 ＋ r110**（🧹 自動書き出しを「ファイルを選ぶ」1本に＝📁 フォルダを選ぶ を画面から外し、①→②→⬆️ 今すぐ書き出す を1つの流れに）。**Play に上げるならこれ**。検証3点 green（署名 `META-INF/RECEIPT-.SF`/`.RSA`・証明書 `a7911190…bd4b` ／ versionCode 8・versionName 1.1 ／ 中の web が r110）。

⚠ **vc7 ＝ vc6 ＋ r109 だけ**（🩹 ② で結びつけた実際のファイル名を画面とトーストに出す）。vc6 を Android 実機で触って**②で古い「レシートデータ.txt」を選んでいた**ことが Drive 側の確認で分かったため、**間違った上書きが正常に見えない**ように直した版。検証3点 green（署名 `META-INF/RECEIPT-.SF`/`.RSA` ／ versionCode 7・versionName 1.1 ／ 中の web が r109）。証明書 SHA-256 は登録済みのアップロード鍵と一致（`a7911190…bd4b`）。
🎉 **vc6 で Android 実機の判定が出た**＝**フォルダは選べない**（読み通り＝ドライブの Android アプリは SAF に出ない）が、**ファイル方式は動く・自動更新も効く**（06:10:31 撮影 → 06:10:40 書き出しを Drive 側で確認）。

⚠ **vc6 には r105〜r108 の4版ぶんが入っている＝1.1 の本体「AI に読ませる置き場へ自動で書き出す」**（r105 自動書き出し＋自前プラグイン folder-sink／r106 置き場に SQL も並べる＋版を 1.1 へ／r107 プラグインの取り出し方の修正／r108 フォルダを渡さない置き場むけに「ファイルを覚える」道）。**versionName も 1.0 → 1.1**（1.0 は App Store で公開済み）。
⚠ **vc6 の焼き方**＝`BUILD_NUMBER=6` で `clean bundleRelease assembleRelease`（**1分9秒・一発**）。検証3点 green（署名 `META-INF/RECEIPT-.SF`/`.RSA` ／ versionCode 6・versionName 1.1 ／ 中の web が r108）。証明書 SHA-256 は登録済みのアップロード鍵と一致（`a7911190…bd4b`・V2 Signer）。
⚠ **Android の folder-sink は一度も実機で動かしていない**（ビルドは通っているだけ）＝vc6 で初めて触ることになる。
✅ **訂正（2026-08-29 実機）＝上の見込みは半分間違いだった**。**フォルダを選ぶ（`ACTION_OPEN_DOCUMENT_TREE`）では Drive は出ない**が、**ファイルを選ぶ（`ACTION_OPEN_DOCUMENT`）では Drive が出て、書ける**。→ **Android は ② のファイル方式で Google ドライブ対応が完成**（撮影 → 自動更新が Drive 側で確認ずみ）。**B案（Drive API）が要るのは iOS だけ**になった。

⚠ **vc5 には r95〜r104 の10版ぶんが入っている**＝⭐ いつものメニュー（r95）／📷 下の帯を設定で選べる・🍽 は食事の時間帯に大きく（r96）／⚖ 体組成計の5数値を体重と同じ1件に（r97）／🔁 繰り返しの管理画面（r98）／📷 下の帯ぜんぶを1タップで隠す（r100-r102）／📈 簡易分析に 🫀 血圧・体重＋渡すもの5種を選べる（r103）／🩹 iPhone で 📋🔁⭐ が無反応だったのを修正（r104）。
⚠ **vc5 の焼き方**＝`BUILD_NUMBER=5` で `clean bundleRelease assembleRelease`（**59秒・一発**）。検証3点 green（署名 `META-INF/RECEIPT-.SF`/`.RSA` ／ versionCode 5 ／ 中の web が r104）。証明書 SHA-256 は登録済みのアップロード鍵と一致（`a7911190…bd4b`・V2 Signer）。
💡 **versionCode は `android\app\build\outputs\bundle\release\output-metadata.json` を見るのがいちばん早い**（apk 側にも同じものがある）＝versionCode/versionName がそのまま書いてある。**AAB の protobuf マニフェストを aapt2 で読もうとして詰まる必要はない**。

⚠ **vc4 には r82〜r94 の13版ぶんが入っている**＝💴 予算（r82／r90 分類ごと・この月だけ／r91 📊統計／r94 📈推移・💴マップ・📤書き出し・📈AI分析）／🫀 からだの数字（血圧・体重を写メで・r83）／🗂 健康を5つのサブタブに（r84-r85）／📸 からだの写真＝👻透かしカメラ・⇆重ねる・⊟並べる・画面いっぱい（r85-r89）／🧾📤 レシートも書き出すか選べる＋⚙ から重なった入口3つを外した（r92）／✍️ 写真の無い収支を手で入れる（r93）。
⚠ **vc4 の焼き方**＝`BUILD_NUMBER=4` で `clean bundleRelease assembleRelease`（**57秒・一発**）。検証3点 green（署名 `META-INF/RECEIPT-.SF`/`.RSA` ／ versionCode 4 ／ 中の web が r94）。⚠ **APK 側に v1 署名（`META-INF/*.RSA`）が無いのは正常**＝minSdk が 24 以上なので v2/v3 署名になる。確かめるのは `apksigner verify --print-certs`（`V2 Signer … a7911190…bd4b` ＝ 2026-08-19 に Play へ登録したアップロード鍵と同じ）。

⚠ **vc3 には r80・r81 が入っている**＝✍️🧾 **写真なしで食事を記録**（撮り忘れた食事を手で入れる／その日のレシートの品目から起こす。⚠ 買い物は勝手に食事にしない＝チェックを付けるのは人）。
⚠ **vc2 には r71〜r79 の9版ぶんが入っている**（🧹まとめて削除／📄ポリシー／新アイコン／🔁📋 食事のコピーと繰り返し／📈推移／📷保存サイズ／🩹 書き起こしが止まる件の一連）。

### 🚧 Android デベロッパーの確認（2026-08-19 に vc2 で詰まった所・調査中）

Play Console でリリースを進めようとすると、こう出て止まる:

> このリリースを進めるには、Android デベロッパーの確認要件を満たすようすべての鍵を登録してください。

⚠ **アプリの中身の問題ではない**（焼いた AAB は検証3点 green）。**Play アカウント側の新しい要件**で、
**2026年9月30日までに、すべての Play アプリを「パッケージ名＋署名鍵」で登録**しないと、
未登録のアプリは Play から削除される（Google のリマインダーメール 2026-08-07）。

**確認済みの事実**

| いつ | 何が |
|---|---|
| 2026-07-10 | **本人確認 完了** |
| 2026-07-16 | アプリの自動登録 完了（メール） |
| 2026-08-17 | レシートの Play アプリを作成（vc1）＋ **この日にパッケージ名が登録されている** |
| 2026-08-19 | vc2 を上げようとして上のエラー |

**「Android デベロッパーの確認」ページの実際の表示（2026-08-19）**

| パッケージ名 | ステータス | キー | 最終更新 |
|---|---|---|---|
| `io.github.yutsutke.madeleine`（あの日） | ✅ 登録済み | **1** | 2026-07-13 |
| `io.github.yutsutke.receipt`（レシート） | ✅ 登録済み | **3** | 2026-08-17 |
| `io.github.yutsutke.voicecalendar` | ✅ 登録済み | **2** | 2026-08-15 |

⚠ **パッケージ名は3つとも「登録済み」**＝「アプリが未登録だから止まっている」のではない
（**この読み違いを一度した**＝一覧のステータスだけ見て「レシートだけ未登録」と決めつけた。実際は登録済み）。

**パッケージの詳細（→ を開いた画面・2026-08-19）＝鍵は3つとも「確認済み」**

| # | 登録されているフィンガープリント（SHA-256） | ステータス |
|---|---|---|
| 1 | `F5:AA:0E:D2:91:07:95:DA:66:68:81:3D:6C:9B:82:71:D1:68:84:54:B5:4D:A9:B0:F8:95:44:F6:29:4F:FD:DF` | 確認済み |
| 2 | `97:E9:68:A9:BC:E4:EF:6C:D2:F2:69:CA:6F:91:B1:68:43:F5:E1:49:42:42:30:0B:69:8E:13:00:1D:22:39:8C` | 確認済み |
| 3 | `F6:54:49:E1:96:48:D0:EE:40:E8:FC:34:E5:C1:2E:DF:1F:43:07:8E:C7:08:E1:C4:80:4B:57:83:06:95:F5:1F` | 確認済み |

🥇 **手元の鍵を実際に読み出して突き合わせた結果＝どちらも、この3つに入っていない**

| 手元の鍵 | SHA-256 | 上の表に |
|---|---|---|
| **アップロード鍵** `receipt-upload` | `A7:91:11:90:57:94:89:BC:A3:3F:1B:B3:EE:2D:1A:24:A0:40:B2:56:E5:E2:FF:C7:17:3E:81:42:E7:3B:BD:4B` | ❌ **無い** |
| **debug 鍵** `androiddebugkey` | `74:80:2F:79:37:5F:44:B1:5D:D6:4E:35:BA:C9:F2:A0:A6:5F:D3:AA:17:08:31:E8:BF:7E:00:75:21:11:B8:78` | ❌ **無い** |

⚠ **3つが何の鍵かは不明**（Google 側で用意・自動登録されたものと見える。Play App Signing の配信用鍵など）。
⚠ **AAB に署名しているのはアップロード鍵**（`META-INF/RECEIPT-.RSA`）で、それが**未登録**。
エラーの文言が「アプリを登録して」でなく「**すべての鍵を登録して**」なのと符合する。

**✅ 解決（2026-08-19 実証済み）＝この画面の「鍵の追加」でアップロード鍵を足す**

- 足したのは **`A7:91:…:4B`**（アップロード鍵）。→ **リリースが通り、vc2 が内部テスターに公開された**（15:35）
- 根拠だった一文（リマインダーメール）＝「**Google Play アプリに対し Google Play 以外での署名に使用する別の鍵を追加する**」
- ⚠ つまり **Play App Signing を使っていても、アップロード鍵は自動登録されない**。
  自動登録されるのは Google 側の鍵だけなので、**自分の鍵は自分で足す**必要がある。

**🔑 3つのアプリ全部で確認した（2026-08-19）＝どれもアップロード鍵は未登録だった**

| アプリ / パッケージ名 | 登録済みの鍵 | 手元のアップロード鍵（SHA-256） | 登録されて<br>いたか |
|---|---|---|---|
| **レシート** `io.github.yutsutke.receipt` | 3つ | `A7:91:11:90:57:94:89:BC:A3:3F:1B:B3:EE:2D:1A:24:A0:40:B2:56:E5:E2:FF:C7:17:3E:81:42:E7:3B:BD:4B` | ❌→**足して解決** |
| **あの日** `io.github.yutsutke.madeleine` | 1つ | `55:70:A0:A4:23:90:5B:AB:2C:FE:C1:C6:71:01:51:C7:CE:49:54:98:08:F5:BE:DA:23:42:8C:45:2F:3E:B1:58` | ❌ **未登録** |
| **ボイスカレンダー** `io.github.yutsutke.voicecalendar` | 2つ | `57:73:C3:D8:3A:A2:DC:37:9F:4C:20:61:EA:AD:00:6F:2D:82:E5:57:26:F9:29:28:F0:61:C8:A2:9C:D6:EE:18` | ❌ **未登録** |

⚠ **あの日 と ボイスカレンダー は、次にリリースを出そうとした時に同じ所で止まる**＝先に足しておくと詰まらない。
⚠ 鍵の置き場＝`~\Documents\madeleine-signing\madeleine-upload.jks`（別名は同フォルダの CREDENTIALS.txt）/
`~\Documents\voice-calendar-signing\upload-keystore.jks`（別名 `upload`・パスワードは同フォルダの `.properties`）。

⚠ **教訓**: 一覧のステータス列だけで判断しない。エラーの文言が「鍵」と言っているなら、**鍵の単位まで開いて見る**。

---

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
