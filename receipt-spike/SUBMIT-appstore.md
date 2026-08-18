# 🍎 App Store に審査を出す — レシート（撮ってAIで読む）

> **これは何か**: TestFlight まで来ているアプリを、**App Store の審査に出すまで**の手順書。
> ビルドの作り方は [BUILD-ios.md](BUILD-ios.md)、Play 側は [BUILD-android.md](BUILD-android.md)。
> ⚠ **ASC（App Store Connect）の画面操作と最終の提出ボタンはユーザー（ゆう）が押す**。ここは「何をどう答えるか」の台本。

- appId: `io.github.yutsutke.receipt`
- ホーム画面の表示名: **レシート**
- App Store 上の名前: **レシート — 撮ってAIで読む**（⚠ Play 上は「レシートと健康— 撮ってAIで読む」＝**3つ食い違っている**。§7 参照）
- プライバシーポリシー: https://yutsutke.github.io/photo-memory-spike/receipt-spike/privacy.html
- サポート: https://yutsutke.github.io/photo-memory-spike/receipt-spike/support.html
- 連絡先: anohiapp@gmail.com（あの日と共通）

---

## 🔴 1. いちばんの関門＝審査担当者が「試せない」問題（Guideline 2.1）

このアプリは **BYOK（本人の APIキーを登録して使う）**。**キーが無いと読み取りが1回も動かない**＝審査担当者はアプリの中心機能を試せない。
Apple は「**レビュー担当者が全機能を試せること**」を求めるので、**ここを外すとほぼ確実に差し戻される**。

**対処＝審査用のキーを1本用意して、審査メモに書く。**

- [ ] **審査専用の APIキーを発行する**（⚠ ゆうの作業。私は代行しません）
  - おすすめ＝**OpenRouter か OpenAI で、このアプリの審査専用に1本**。⚠ **支出上限を低く**（$5〜10 程度）設定する
  - 名前を `appstore-review-2026-08` のようにしておくと、あとで失効させやすい
- [ ] **審査メモにキーと手順を書く**（下の §5 の文面にそのまま貼る欄がある）
- [ ] **審査が終わったら失効させる**（承認・却下どちらでも。⚠ 忘れると誰かに使われ続ける）

> 💡 補足: 「キーが無くても撮って残せる」は事実だが、**それだけでは中心機能の説明にならない**。キーを渡すのがいちばん短い道。

---

## 2. App のプライバシー（データ収集の申告）＝「収集する」で出す

**方針（2026-08-18 決定）**: 実態より**広めに申告する**。理由＝BYOK でも**写真は実際に端末の外へ出る**（本人が選んだ AI へ）。
狭すぎる申告は差し戻しの定番だが、広めの申告で落とされることはほぼ無い。

ASC ▸ App のプライバシー で、**すべて「アプリの機能」・「ユーザーに紐付けられていない」・「トラッキングに使用しない」**として申告する。
（⚠「紐付けられていない」の根拠＝**アカウントも識別子も無い**。アプリは誰の記録かを一切知らない）

| 申告するデータ型 | 何が該当するか | 用途 | 紐付け | 追跡 |
|---|---|---|---|---|
| **写真またはビデオ** | 読み取りのために送るレシート・料理・健診・薬の写真 | アプリの機能 | しない | しない |
| **購入履歴**（Financial Info ▸ Purchase History） | レシートから読み取った店名・金額・品目 | アプリの機能 | しない | しない |
| **健康**（Health & Fitness ▸ Health） | 健康診断の結果・服用中の薬 | アプリの機能 | しない | しない |
| **その他のユーザーコンテンツ** | 本人が書いたメモ・AI への依頼文 | アプリの機能 | しない | しない |

**申告しないもの（理由を審査メモにも書く）**
- **位置情報**: 位置情報の権限を要求しない。写真の GPS も読まない。💴 支出マップの座標は「**店の住所の文字**」から引いたもので、本人の居場所ではない
- **連絡先・識別子・使用状況データ・診断データ**: アナリティクスも広告 SDK も入っていない

---

## 3. 年齢レーティング

- **医療／治療に関する情報**: 「該当なし」で答える。⚠ 根拠＝本アプリは**本人の書類を書き写して整理するだけ**で、**診断も治療の助言もしない**。アプリ内（🩺 タブ）とプライバシーポリシーに「**医療機器ではない・AI の要約は参考・判断は医師へ**」と明記してある（r72 で追加）
- 暴力・性的表現・ギャンブル等はすべて「なし」
- 想定＝**4+**（あの日と同じ）

---

## 4. 提出に要るもの（そろっているか）

| 項目 | 状態 | メモ |
|---|---|---|
| バンドルID・ASC のアプリレコード | ✅ | `io.github.yutsutke.receipt` |
| TestFlight ビルド | ✅ 1.0 (4)（**中身は r65 相当＝古い**） | ⚠ **提出前に最新でビルドし直す**（いま web は r72） |
| プライバシーポリシー URL | ✅ | 上記 |
| サポート URL | ✅ | 上記 |
| 連絡先（氏名・電話・メール） | ✅ | あの日と同じでよい |
| スクリーンショット | ⬜ **未** | 6.9"（1290×2796 など）を数枚。⚠ **キーを入れた状態**で撮る（空の画面ばかりだと魅力が伝わらない） |
| 説明文・キーワード・サブタイトル | ⬜ **未** | §6 に下書き |
| カテゴリ | ⬜ 未 | 第1＝**ファイナンス**／第2＝仕事効率化（家計簿として探される方が近い） |
| 年齢レーティングの回答 | ⬜ 未 | §3 |
| App のプライバシー | ⬜ 未 | §2 |
| 審査メモ＋審査用キー | ⬜ **未（最重要）** | §1・§5 |
| 輸出コンプライアンス | ✅ | `ITSAppUsesNonExemptEncryption = false` を Info.plist に設定済み |

---

## 5. 審査メモの下書き（そのまま貼れる形）

> ⚠ `__________` の所に、§1 で発行した**審査用キー**を貼ってから提出する。

```
[English]
Thank you for reviewing.

HOW TO TEST THE MAIN FEATURE (please read first)
This app has no server and no account. It reads receipts using AI through the
user's OWN API key (BYOK). Without a key, photos are only stored on the device
and no reading happens. For review, please use this temporary key:

  Provider: (OpenRouter / OpenAI)   [choose one]
  API key:  __________

  Steps: open the app > tap the gear icon (settings) > choose the provider >
  paste the key > tap "Connection test" > close settings > tap the camera
  button and photograph any receipt (or pick a photo of a receipt from the
  library). The store name, date, total and line items appear within a few
  seconds. The same flow works for meals, health check-up results and
  medication leaflets from the top tabs.

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
  (sent to GSI Japan / OpenStreetMap), not from the user's location.
- No ads, no analytics, no tracking SDKs.

HEALTH
The app only transcribes and organises the user's own documents. It gives no
diagnosis and no medical advice; a disclaimer to that effect is shown in the
Health tab and in the privacy policy.

DATA DELETION
Settings > bottom > "Delete in bulk" removes data by period and by kind
(receipts / meals / check-ups / medications / analysis history / store
locations). Deleting the app removes everything.

[日本語]
審査ありがとうございます。

■ 中心機能の試し方（BYOK のため、先にお読みください）
本アプリはサーバーもアカウントも持たず、レシートの読み取りは**利用者ご自身の
APIキー**で行います（BYOK）。キーが無いと写真は端末に保存されるだけで読み取り
は動きません。審査用に一時キーをご用意しました。

  事業者: （OpenRouter / OpenAI のいずれか）
  APIキー: __________

  手順: アプリを開く → ⚙（設定）→ 事業者を選ぶ → キーを貼る → 「接続テスト」
  → 設定を閉じる → 📷 でレシートを撮る（または 🖼 から写真を選ぶ）。数秒で
  店名・日付・金額・品目が表示されます。料理・健康診断・薬も同じ流れです。

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
本アプリは利用者本人の書類を書き写して整理するだけで、診断も医学的助言も行い
ません。その旨を 🩺 タブとプライバシーポリシーに明記しています。

■ データの削除
⚙ 設定の一番下「🧹 まとめて削除」で、期間と種類（レシート／料理／健診／薬／
分析の履歴／店の場所）を選んで削除できます。アプリの削除ですべて消えます。
```

---

## 6. 掲載文の下書き

**サブタイトル（30字以内）**
`撮るだけ。AIが家計と健康を書き写す`

**説明文（下書き）**
```
レシートを撮るだけ。AI が店名・日付・金額・品目を読み取って、表にして貯めます。

・📷 撮る/🖼 選ぶ → 数秒で読み取り（1枚に複数のレシートが写っていても分けます）
・🍽 料理の写真も残せます（手作り／中食／外食を AI が見分けます）
・🩺 健康診断の結果と 💊 薬も、撮って残せます
・🔍 横断検索・📊 統計・💴 支出マップで、貯まったものを眺められます
・📤 期間を決めて書き出し → Claude / ChatGPT / Google ドライブへそのまま渡せます

■ サーバーはありません
記録はあなたの端末の中だけに貯まります。アカウント登録も不要です。
AI に読ませる部分は、あなた自身の APIキー（Claude / ChatGPT / OpenRouter）を
設定に登録して使います。写真は端末から、あなたが選んだ AI へ直接送られます。
開発者はそれを受け取りません。

■ ご注意
・ご利用には AI 事業者の APIキーが必要です（料金はあなたと事業者の間で発生します）
・本アプリは医療機器ではなく、医学的な助言をしません。AI の要約は参考です
```

**キーワード（100字以内・カンマ区切り）**
`レシート,家計簿,OCR,AI,支出,家計,健康診断,薬,記録,節約,経費,読み取り`

---

## 7. 名前の食い違い（決めていない宿題）

| どこ | いまの名前 |
|---|---|
| ホーム画面（`strings.xml` / `CFBundleDisplayName`） | **レシート** |
| App Store（ASC） | **レシート — 撮ってAIで読む** |
| Google Play | **レシートと健康— 撮ってAIで読む** |

ストア名は探されやすさで決まるので**違っていても構わない**（あの日も日英で違う）。
ただし **Play と ASC でどちらに寄せるか**は決めておくと迷わない。⚠ ASC の名前は**審査に出す前なら何度でも変えられる**。

---

## 8. 出す順番（おすすめ）

1. **審査用キーを発行**（§1）＝これが無いと先へ進めない
2. 最新（r72 以降）で **Codemagic → TestFlight** ビルド（[BUILD-ios.md](BUILD-ios.md)）
3. **実機で一通り触る**（キー登録→レシート1枚→🍽→🩺→📤→🧹 まとめて削除）
4. ASC で **App のプライバシー**（§2）→ **年齢レーティング**（§3）→ 掲載文（§6）→ スクショ
5. **審査メモ**（§5）にキーを貼る → 提出
