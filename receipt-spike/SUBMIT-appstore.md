# 🍎 App Store に審査を出す — レシート（撮ってAIで読む）

> **これは何か**: TestFlight まで来ているアプリを、**App Store の審査に出すまで**の手順書＋**そのまま貼れる文章**。
> ビルドの作り方は [BUILD-ios.md](BUILD-ios.md)、Play 側は [BUILD-android.md](BUILD-android.md)。
> ⚠ **ASC（App Store Connect＝アップルの管理画面）の操作と最終の提出ボタンはユーザー（ゆう）が押す**。ここは「何をどう答えるか」の台本。

- appId: `io.github.yutsutke.receipt`
- ホーム画面の表示名: **レシート**
- プライバシーポリシー: https://yutsutke.github.io/photo-memory-spike/receipt-spike/privacy.html
- サポート: https://yutsutke.github.io/photo-memory-spike/receipt-spike/support.html
- 連絡先: anohiapp@gmail.com（あの日と共通）

---

## 0. いまの状態（2026-08-23）

| もの | いま |
|---|---|
| web（GitHub Pages） | **r102** |
| TestFlight | 1.0 (7) ＝ **r94**（2026-08-21 配信） |
| いま回しているビルド | **1.0 (8) ＝ r102**（Codemagic が CI 側で `sync:web` するので中身は最新） |
| Google Play | vc4 ＝ r94（内部テスト） |

---

## 🔴 1. 先に決める2つ（これが決まらないと入力を作り直しになる）

### ① iPad を対象に含めるか → **おすすめ＝iPhone 専用にする**

いまの設定は `TARGETED_DEVICE_FAMILY = "1,2"`（＝iPhone と iPad の両方）。Capacitor の既定のまま。
**あの日は `"1"`（iPhone 専用）で出している**。

iPad を含めたままだと:
- ASC が **iPad 13インチのスクリーンショットを必須で要求する**（iPhone のぶんだけでは提出できない）
- 審査担当者が **iPad で開いて崩れを見る**（この画面は 375px 幅を基準に作ってある＝広い画面での確認は一度もしていない）

iPhone 専用に変える手数は **1行**（`ios/App/App.xcodeproj/project.pbxproj` の2か所を `"1"` に）＋**ビルドし直し**。
⚠ 変えると **いま回っているビルドは使えない**（焼き直しが要る）。iPad を捨てるわけではなく、**後から広げるのは自由**（狭く出して広げるのは簡単・逆は面倒）。

### ② ストア上の名前を Play に合わせるか

| どこ | いまの名前 |
|---|---|
| ホーム画面（`CFBundleDisplayName`） | **レシート** |
| App Store（ASC） | **レシート — 撮ってAIで読む** |
| Google Play | **レシートと健康— 撮ってAIで読む** |

**おすすめ＝ASC も「レシートと健康 — 撮ってAIで読む」に寄せる**（18字・上限30字）。
理由＝r83〜r97 で 🫀 血圧・⚖ 体重と体組成計・📸 からだの写真・💊 薬 が増え、**健康はもうレシートと並ぶ柱**（トップの3タブのうち1つ）。名前に入っていないと「家計簿アプリ」としてしか探されない。
⚠ ASC の名前は**審査に出す前なら何度でも変えられる**。ホーム画面の「レシート」は短いままでよい（アイコンの下は短い方が読める）。

---

## 🔴 2. いちばんの関門＝審査担当者が「試せない」問題（Guideline 2.1）

このアプリは **BYOK（本人の APIキーを登録して使う）**。**キーが無いと読み取りが1回も動かない**＝審査担当者はアプリの中心機能を試せない。
Apple は「**レビュー担当者が全機能を試せること**」を求めるので、**ここを外すとほぼ確実に差し戻される**。

**対処＝審査用のキーを1本用意して、審査メモに書く。**

- [ ] **審査専用の APIキーを発行する**（⚠ ゆうの作業。私は代行しません）
  - おすすめ＝**OpenRouter**（鍵1本で複数のモデルが使える・**クレジットを $5〜10 だけ入れておけば被害はそこで止まる**）
  - 名前を `appstore-review-2026-08` のようにしておくと、あとで失効させやすい
- [ ] **審査メモにキーと手順を書く**（§5 の文面に貼る欄がある）
- [ ] **審査が終わったら失効させる**（承認・却下どちらでも。⚠ 忘れると誰かに使われ続ける）

> 💡 「キーが無くても撮って残せる」は事実だが、**それだけでは中心機能の説明にならない**。キーを渡すのがいちばん短い道。

---

## 3. やることの一本道（この順に進む）

**A. 出す前に直すもの（コード・文書）**
- [ ] ① iPad を外すか決める → 外すなら `TARGETED_DEVICE_FAMILY = "1"` に変えて **Codemagic を回し直す**
- [x] ② プライバシーポリシーを r102 の中身に合わせる（🫀 血圧・脈／⚖ 体重と体組成計の5つ／📸 からだの写真／⭐ いつものメニュー／🔁 繰り返し を追記）＝**2026-08-23 更新済み**
- [ ] ③ 新しいビルドが TestFlight に着いたら、**実機で一通り触る**（キー登録 → 🧾 レシート1枚 → 🍽 → 🩺 の5タブ → 📤 書き出し → 🧹 まとめて削除）

**B. ASC に入れるもの（§4〜§7 のコピペ）**
- [ ] ④ **App 情報**: 名前・サブタイトル・カテゴリ（第1＝ファイナンス／第2＝仕事効率化）・年齢レーティング（§6）
- [ ] ⑤ **価格**: 無料（¥0）／アプリ内課金なし
- [ ] ⑥ **App のプライバシー**（§7）＝データ収集の申告
- [ ] ⑦ **バージョン情報**: プロモーションテキスト・説明文・キーワード・サポートURL・マーケティングURL（空でよい）・著作権
- [ ] ⑧ **スクリーンショット**（§8）＝⚠ **キーを入れて中身が入った状態**で撮る
- [ ] ⑨ **ビルドを選ぶ**: 1.0 (8)（⚠ 一覧に古いビルドしか出ない時は**保存 → 再読み込み**で全部出る＝あの日でハマった罠）
- [ ] ⑩ **App Review 情報**: サインイン不要／連絡先（氏名・電話・anohiapp@gmail.com）／**審査メモ（§5）に審査用キーを貼る**
- [ ] ⑪ **リリース方法**: 「手動でリリース」を選ぶ（承認されてもすぐ公開されない＝落ち着いて確認できる）
- [ ] ⑫ **提出**（ボタンはゆうが押す）

**C. 出したあと**
- [ ] ⑬ 審査用キーを**失効させる**
- [ ] ⑭ Play 側も同じ版（r102）へ上げる＝vc5 を焼く（[BUILD-android.md](BUILD-android.md)・一発50秒）
- [ ] ⑮ 承認されたら「このバージョンをリリース」を押す

---

## 4. コピペ用の文章（ASC のそれぞれの欄へ）

### App 名（上限30字）

```
レシートと健康 — 撮ってAIで読む
```

### サブタイトル（上限30字）

```
撮るだけ。AIが家計と健康を書き写す
```

### プロモーションテキスト（上限170字・⚠ **審査なしでいつでも差し替えられる**）

```
撮るだけで、家計も食事も健康も、AI が読み取って貯めていきます。サーバーはありません。記録はあなたの端末の中だけ。お使いの AI の APIキーを登録して使う、あなた専用の記録帳です。
```

### 説明文（上限4000字）

```
レシートを撮るだけ。AI が店名・日付・金額・品目を読み取って、表にして貯めます。
食事の写真も、血圧も、体重も、健康診断の結果も、同じ「撮るだけ」で残せます。

■ 3つのタブ

🧾 レシート
撮る／写真から選ぶ → 数秒で読み取り。1枚に複数のレシートが写っていても分けます。
写真の無い支出は、手で1件だけ入れることもできます。

🍽 食事
料理の写真から、手作り／中食（買って帰る）／外食を見分けて記録します。
よく食べる献立は ⭐ で覚えて次から1タップ。毎日の定番は 🔁 繰り返しで自動的に入ります。

🩺 健康
🫀 血圧・脈（1日に何回でも）／⚖ 体重と体組成計の数値（体脂肪率・骨格筋率・基礎代謝・BMI・内臓脂肪レベル）／
📸 からだの写真（同じ角度で撮って重ねる）／💊 薬（いつから何を飲んでいるか）／🩺 健康診断の結果。

■ 貯まったものを眺める・取り出す

・📊 統計／📈 推移（週・月・年）／💴 支出マップ（レシートに印字された住所から地図に置きます）
・💰 予算を決めると、統計・推移・マップ・書き出しに反映されます
・🔍 レシートも食事も健康もまとめて検索
・📤 期間と種類を決めて書き出し → 共有シートから Claude / ChatGPT / Google ドライブへそのまま渡せます

■ サーバーはありません

記録はあなたの端末の中だけに貯まります。アカウント登録も不要です。
AI に読ませる部分は、あなた自身の APIキー（Claude / ChatGPT / OpenRouter）を ⚙ 設定に登録して使います。
写真は端末から、あなたが選んだ AI へ直接送られます。開発者はそれを受け取りません。
読み取り用と分析用でモデルを分けられます（写真は安いモデル・分析は賢いモデル、という組み合わせができます）。

■ ご注意

・ご利用には AI 事業者の APIキーが必要です（料金はあなたと事業者の間で発生します）
・本アプリは医療機器ではありません。診断も医学的な助言もしません。AI の要約は参考です。判断は医師にご相談ください
```

### キーワード（上限100字・カンマ区切り・⚠ スペースを入れない）

```
レシート,家計簿,支出,経費,OCR,読み取り,AI,節約,食事,記録,健康診断,薬,血圧,体重,家計
```

### サポートURL

```
https://yutsutke.github.io/photo-memory-spike/receipt-spike/support.html
```

### プライバシーポリシーURL

```
https://yutsutke.github.io/photo-memory-spike/receipt-spike/privacy.html
```

### 著作権（Copyright）

```
2026 yutsutke
```

⚠ あの日で入れたものと同じ表記に揃える（ASC の「販売元名」と食い違わせない）。

### このバージョンの新機能

⚠ **1.0（初回）では出てこない欄**。1.1 以降で使う。

---

## 5. 審査メモ（App Review 情報 ▸ メモ欄へそのまま貼る）

> ⚠ `__________` に、§2 で発行した**審査用キー**を貼ってから提出する。

```
[English]
Thank you for reviewing.

HOW TO TEST THE MAIN FEATURE (please read first)
This app has no server and no account. It reads photos using AI through the
user's OWN API key (BYOK). Without a key, photos are only stored on the device
and no reading happens. For review, please use this temporary key:

  Provider: OpenRouter
  API key:  __________

  Steps:
   1. Open the app and tap the gear icon (settings) at the top right.
   2. Under "AI for reading receipts", choose "OpenRouter".
   3. Paste the key into the field below it.
   4. Tap "Fetch the list of usable models" and pick any model from the list.
   5. Tap "Connection test" (free, not billed) - it should say OK.
   6. Close settings, tap the camera button at the bottom and photograph any
      receipt, or pick a photo of a receipt from the library.
   The store name, date, total and line items appear within a few seconds.

  The same flow works for meals, blood pressure, weight, medication and health
  check-up results from the three tabs at the top (Receipts / Meals / Health).

PRIVACY
- We (the developer) operate no server and receive nothing. Photos are sent
  DIRECTLY from the device to the AI provider the user chose, authenticated
  with the user's own API key. That provider's privacy policy applies, and this
  is stated in our privacy policy and in the settings screen.
- We declared Photos, Purchase History, Health and Other User Content as
  "collected" for app functionality, not linked to identity, no tracking,
  because those items do leave the device when the user runs a reading.
- No location permission is requested and photo GPS is never read. The
  coordinates on the spending map come from the printed store ADDRESS TEXT
  (geocoded via GSI Japan / OpenStreetMap), not from the user's location.
- No ads, no analytics, no tracking SDKs.

HEALTH
The app only transcribes and organises the user's own documents and numbers.
It gives no diagnosis and no medical advice; a disclaimer to that effect is
shown in the Health tab and in the privacy policy. HealthKit is not used.

DATA DELETION
Settings > bottom > "Delete in bulk" removes data by period and by kind
(receipts / meals / check-ups / medication / body numbers / body photos /
analysis history / store locations / repeat rules / saved menus).
Deleting the app removes everything.

[日本語]
審査ありがとうございます。

■ 中心機能の試し方（BYOK のため、先にお読みください）
本アプリはサーバーもアカウントも持たず、写真の読み取りは「利用者ご自身の
APIキー」で行います（BYOK）。キーが無いと写真は端末に保存されるだけで読み取り
は動きません。審査用に一時キーをご用意しました。

  事業者: OpenRouter
  APIキー: __________

  手順:
   1. アプリを開き、右上の ⚙（設定）を開く
   2. 「📷 レシートの読み取りに使うAI」で「OpenRouter」を選ぶ
   3. その下の欄にキーを貼る
   4. 「🔄 使えるモデルの一覧を取得」を押し、一覧から1つ選ぶ
   5. 「🔌 接続テスト（無料・課金されません）」を押して OK を確認
   6. 設定を閉じ、下の 📷 でレシートを撮る（または 🖼 から写真を選ぶ）
   数秒で店名・日付・金額・品目が表示されます。
  料理・血圧・体重・薬・健康診断も、上の3つのタブ（🧾 レシート／🍽 食事／
  🩺 健康）から同じ流れで使えます。

■ プライバシー
・開発者はサーバーを持たず、何も受け取りません。写真は端末から、利用者が選んだ
  AI 事業者へ直接送られます（利用者自身のキーで認証）。その事業者の方針が適用
  される旨は、プライバシーポリシーと設定画面に明記しています。
・写真・購入履歴・健康・その他のユーザーコンテンツを「収集する（アプリの機能・
  本人に紐付けない・追跡しない）」として申告しています。読み取り時に実際に端末
  の外へ出るためです。
・位置情報の権限は要求せず、写真の GPS も読みません。支出マップの座標はレシート
  に印字された「住所の文字」から引いたもの（国土地理院／OpenStreetMap）で、
  利用者の居場所ではありません。
・広告・アナリティクス・トラッキング SDK はありません。

■ 健康について
本アプリは利用者本人の書類や数値を書き写して整理するだけで、診断も医学的助言も
行いません。その旨を 🩺 タブとプライバシーポリシーに明記しています。HealthKit は
使用していません。

■ データの削除
⚙ 設定の一番下「🧹 まとめて削除」で、期間と種類（レシート／料理／健診／薬／
からだの数字／からだの写真／分析の履歴／店の場所／繰り返しの決まり／いつもの
メニュー）を選んで削除できます。アプリの削除ですべて消えます。
```

**審査連絡先（同じ画面の上の方）**

- サインインが必要ですか → **いいえ**
- 連絡先: 氏名・電話番号（あの日と同じ）・メール `anohiapp@gmail.com`

---

## 6. 年齢レーティング（そのまま答える）

- **医療／治療に関する情報**: 「**該当なし**」
  - 根拠＝本アプリは**本人の書類と数値を書き写して整理するだけ**で、**診断も治療の助言もしない**。アプリ内（🩺 タブ）とプライバシーポリシーに「**医療機器ではない・AI の要約は参考・判断は医師へ**」と明記してある（r72 で追加）
- 暴力・性的表現・ギャンブル・アルコール等はすべて「**なし**」
- 「アプリ内で無制限のウェブアクセスが可能」→ **いいえ**
- 想定＝**4+**（あの日と同じ）

---

## 7. App のプライバシー（データ収集の申告）＝「収集する」で出す

**方針（2026-08-18 決定）**: 実態より**広めに申告する**。理由＝BYOK でも**写真は実際に端末の外へ出る**（本人が選んだ AI へ）。
狭すぎる申告は差し戻しの定番だが、広めの申告で落とされることはほぼ無い。

ASC ▸ App のプライバシー で、**すべて「アプリの機能」・「ユーザーに紐付けられていない」・「トラッキングに使用しない」**として申告する。
（⚠「紐付けられていない」の根拠＝**アカウントも識別子も無い**。アプリは誰の記録かを一切知らない）

| 申告するデータ型 | 何が該当するか | 用途 | 紐付け | 追跡 |
|---|---|---|---|---|
| **写真またはビデオ** | 読み取りのために送るレシート・料理・健診・薬・からだの写真 | アプリの機能 | しない | しない |
| **購入履歴**（Financial Info ▸ Purchase History） | レシートから読み取った店名・金額・品目 | アプリの機能 | しない | しない |
| **健康**（Health & Fitness ▸ Health） | 健康診断の結果・薬・血圧・体重・体組成計の数値 | アプリの機能 | しない | しない |
| **その他のユーザーコンテンツ** | 本人が書いたメモ・AI への依頼文 | アプリの機能 | しない | しない |

**申告しないもの（理由は審査メモにも書いてある）**

- **位置情報**: 位置情報の権限を要求しない。写真の GPS も読まない。💴 支出マップの座標は「**店の住所の文字**」から引いたもので、本人の居場所ではない
- **連絡先・識別子・使用状況データ・診断データ**: アナリティクスも広告 SDK も入っていない

---

## 8. スクリーンショット（⚠ 未・のちほどゆうが実機で撮って送る）

**サイズ**: iPhone **6.9インチ 1290×2796**（16 Pro Max 等）を **3〜5枚**。⚠ 6.5インチ 1284×2778 のスロットでも可（あの日はそちらで5枚出した）。**縦向き**。iPhone 専用にすれば iPad のぶんは要らない（§1①）。

**撮る前に**: ⚠ **キーを入れて、中身が入った状態**にする（空の画面ばかりだと魅力が伝わらず、審査でも「機能が見えない」と見られる）。

**撮る画面（おすすめの順＝1枚目がいちばん効く）**

1. **🧾 レシート一覧** — 読み取り済みが数件並んでいるところ（「貯まる」が一目で分かる）
2. **レシート1件の詳細** — 店名・日付・金額・品目が表になっているところ（「AI が書き写す」の証拠）
3. **🍽 食事** — 料理の写真と、手作り／中食／外食の区別が見えるところ
4. **🩺 健康** — ⚖ 体重＋体組成計の数値、または 🫀 血圧のグラフ
5. **📊 統計 か 💴 支出マップ** — 貯めた先に何が見えるか

**⚠ 注意**: 実際の店名・金額・健康の数値が写る。**人に見せたくない中身が写っていないか**を出す前に確認する（審査用に数件だけ入れた端末で撮るのがいちばん安全）。

---

## 9. 提出後 / 承認後

- [ ] 審査用の APIキーを**失効させる**（⚠ いちばん忘れやすい）
- [ ] 承認 → ASC で「**このバージョンをリリース**」（手動リリースを選んでいるため、押すまで公開されない）
- [ ] Play 側も r102 へ（vc5・[BUILD-android.md](BUILD-android.md)）＝web・iOS・Android の3つを同じ版に揃える
