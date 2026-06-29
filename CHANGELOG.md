# CHANGELOG — 写真思い出スパイク

> spike なので軽量に。バージョン番号は commit 順 (v1, v2, ...) で振る。
> 詳細は git log/diff で見られるので「背景・設計判断・教訓・残課題」だけ書く。

---

## v110 — 初回審査リジェクト対応：アプリ名一致（2.3.8）＋サポートページ（1.5） (2026-06-30)

**背景**
- v1.0(15) の初回審査結果が 2026-06-29 に到着＝**リジェクト（Changes needed）**。ただし指摘は2点ともメタデータ/設定レベルで、**最も警戒していた 4.2（web ラッパーが薄い）は出ず**・機能/プライバシー/位置/クラッシュの指摘もゼロ。native 要素（自前 PhotoLibrary 等）が効いて「実質的なアプリ」と認められたと読める。レビュー環境＝iPhone 17 Pro Max / 1.0(15) / Review date Jun 29。Submission ID `c4451f04-d791-46e6-9030-95191a096a8c`。

**指摘と対処**
- **Guideline 2.3.8（Accurate Metadata・名前不一致）**: ストア名「あの日 — 写真と足跡」とデバイス表示名「Madeleine」が乖離（"makes it difficult for users to find apps they have downloaded"）。→ [Info.plist](ios/App/App/Info.plist) の `CFBundleDisplayName` を Madeleine→**「あの日 — 写真と足跡」**に変更（ストア名と完全一致＝最も安全）。**バイナリ変更なので再ビルド必須**（次 Codemagic ビルド 1.0(16)）。原因＝Capacitor の `appName=Madeleine` がそのまま `CFBundleDisplayName` に乗っていた。
- **Guideline 1.5（Safety・Support URL）**: ASC の Support URL が `…/photo-memory-spike/`（アプリ本体）で、質問/サポートを依頼できる情報ページになっていない。→ **[support.html](support.html) を新設**（使い方・FAQ・連絡先 yutsutke@gmail.com・privacy.html と同トーン/ja-en 切替）。公開 URL=`https://yutsutke.github.io/photo-memory-spike/support.html`。**メタデータのみ＝ASC で URL を差し替えるだけ**（再ビルド不要）。

**設計判断（世界展開と名前の両立）**
- ユーザー意向＝「ストア名は“あの日 — 写真と足跡”のまま、でも世界展開で Madeleine も活かしたい」。iOS もストアも**名前はロケール別に出し分け可能**なので二択ではない: アプリ側は `InfoPlist.strings`(ja/en) に `CFBundleDisplayName`（ja=あの日 — 写真と足跡 / en=Madeleine）、ストア側は ASC に英語ローカライズを追加し名前=Madeleine。各地域でデバイス名⇄ストア名が一致して 2.3.8 を満たしつつ 🇯🇵あの日/🇬🇧Madeleine の二刀流が成立。
- **ただし英語展開は v1.1 に回す**（英語ストアはスクショ/説明/キーワード一式が要り、今やると v1 承認が遅れる）。v1 は日本語単独・デバイス名フル一致で最短承認を狙う。
- ホーム画面のアイコン下ラベルは iOS 仕様で ~11字超は末尾省略（「あの日 — 写真と…」表示）だが、`CFBundleDisplayName` の値自体は完全一致なので審査/設定/検索/Spotlight/ストアではフル表示＝2.3.8 的には完全一致が最安全。

**結果 / 観察**
- 修正2点を実装＋ BUILD phase3.62。**コア体験のコードには一切触れていない**（Info.plist 1行＋新規 support.html のみ）。
- 再提出の経路: ① push→Codemagic がビルド 1.0(16) を生成（CFBundleDisplayName 変更が乗る）② ASC でビルドを 15→16 に差し替え ③ Support URL を support.html に更新 ④「審査用に追加」→再提出（typically 48h 以内に応答）。

**教訓**
- 初回審査の典型リジェクトは「名前の不一致」と「サポート URL がトップページ」の2つ＝**中身でなく“見せ方/設定”で落ちる**。コア体験が薄いと疑われた形跡はなく、native 化（自前プラグイン）の投資が 4.2 回避に効いた可能性が高い。
- Capacitor の `appName` がそのまま `CFBundleDisplayName` になる。日本語ストア名で出すなら最初からデバイス名も合わせるべきだった（次回アプリでの予防）。

**残課題 / 次の方向**
- ASC 操作（ビルド差し替え＋Support URL 更新＋再提出）をユーザーと実施。
- 承認後 v1.1: 英語ローカライズ（InfoPlist.strings＋ASC 英語名 Madeleine）／iPad 対応／位置ロガー復活／広告（[[monetization-v1-adfree]]）。

## v109 — iOS 初回提出は iPhone 専用に（iPad スクショ要件を外す・提出ブロッカー解消） (2026-06-28)

**背景**
- ASC バージョンページで「審査用に追加」を押したら3つの提出ブロッカーが出た: ①13インチ iPad ディスプレイのスクショが必要 ②コンテンツ配信権が未設定 ③価格帯が未選択。②③は ASC 上で即解消（後述）。①は「アプリを iPad 対応にするか／iPhone 専用にするか」の製品判断。

**設計判断**
- **iPhone 専用にする**（`TARGETED_DEVICE_FAMILY` を `"1,2"`→`"1"`、Debug/Release 両方）。理由: このプロダクトのコア（連想ウォーク／足跡／全画面の没入／過去の今日）は**電話前提の体験**で、iPad 向けレイアウトは設計していない。未設計の iPad レイアウトを審査・iPad ユーザーに見せるリスク（リジェクト要因・体験低下）を避け、**コアに忠実な iPhone 専用で初回を通す**。iPad 対応は要望が固まってから別途（“今は作らない”寄り）。
- 代替案=「iPad 対応のまま 13インチ用スクショを追加」は再ビルド不要で最速だが、上記の理由で不採用。
- ビルド番号は Codemagic の `$BUILD_NUMBER` 自動採番（`agvtool new-version -all`）なので手動 bump 不要＝push で次番号（>14）が付く。`submit_to_testflight: true` は据え置き（外部ベータ審査の失敗は非ブロッキング＝.ipa は問題なくアップロード/処理される、と前回確認済み）。

**ASC 上で解消した②③（再ビルド不要）**
- **コンテンツ配信権=「はい（必要な権利を保有）」**。地図が CARTO/OpenStreetMap と地理院タイル（国土地理院）の**第三者タイルを表示**し、コード上も出典 attribution をライセンス上残している（`index.html` の地図初期化）。よって「サードパーティ製コンテンツを表示し権利を保有」が正直な回答。
- **価格=無料（基準 US $0.00／全 174 地域 $0.00 自動算出）**。[[monetization-v1-adfree]] の通り v1 は広告なし・無料。
- **App プライバシー「公開」も実行**（「データの収集なし」ラベルを公開済みに）＝提出の前提。

**結果 / 観察**
- 🎉 **2026-06-28 v1.0 を App Store 審査に正式提出＝「審査待ち（Waiting for Review）」**。push → Codemagic が iPhone 専用ビルド **15** を生成（ユーザーが「終了」を確認）→ ASC でビルドを 14→15 に差し替え保存 → 「審査用に追加」で**3ブロッカーが全て解消済**（iPad はビルド 15 が iPhone 専用なので消滅）→「提出物の下書き」に 1.0(15) が「提出準備完了」で表示 →（最終ボタン直前でユーザー確認）→「審査へ提出」。**輸出コンプライアンスは `ITSAppUsesNonExemptEncryption=false` で自動スキップ・EU DSA 質問も出ず**＝追加質問ゼロでそのまま送信完了。
- Mac を一台も持たずに spike から製品の初回ストア提出まで到達（[[madeleine-product-repo]] にマイルストーン記録）。
- **リリース方法は「手動」に設定**（承認後すぐ自動公開せず、ASC で「公開」を押した時に App Store へ）＝初回は公開タイミングを自分で選ぶ（深夜の自動公開を回避）。審査待ち中でも変更可だった。

**残課題 / 次の方向**
- 審査結果待ち（最大48h・完了でメール）。次セッション開始時に Gmail で App Review の結果を確認（[[session-start-gmail-check]]）。リジェクト時はガイドライン理由を読んで対応（薄い web ラッパー＝4.2 が出たら native 要素を増やす／その他は個別）。
- 承認後 v1.1: iPad 対応に戻す（`"1"`→`"1,2"`＋iPad スクショ）／位置ロガー復活（NATIVE_LOCATION=true＋background-location を package.json に戻す＋Info.plist の位置キー）／広告 AdMob npa=1＋¥300 除去（[[monetization-v1-adfree]]）。

## v108 — 版表示をヘッダ→ℹ️へ移動＋スクショ用 ?shot モード（ストア掲載の下ごしらえ） (2026-06-27)

**背景**
- スクショ撮影の前にユーザーが2点指摘: ①ヘッダの BUILD 版文字列はスクショ・本番で邪魔 ②スマホの native アプリだと全画面スクショが撮りにくい／Web 版なら全画面をフルで撮れる。

**設計判断**
- **版表示をヘッダから ℹ️ 情報モーダル末尾＋`console.log` に移動**（ヘッダの `#ver` は空に）。spike 中はヘッダ常時表示が必須だったが、製品化フェーズで「すっきり優先」に転換。ビルド確認は ℹ️ かコンソールで継続可（Mac なしでも実機で ℹ️ を開けば版が読める）。CLAUDE.md の運用ルールも更新（ヘッダ再表示禁止を明記）。
- **Web を App Store スクショに使えるようにする**: Web は審査対象外で位置 UI（🛰️/地図トグル）が出ているため、そのまま撮ると提出物（native v1=位置オフ）と食い違う。→ URL に `?shot` で `SHOT_MODE` を立て、`LOCATION_AVAILABLE=(!IS_NATIVE||NATIVE_LOCATION) && !SHOT_MODE` で**位置 UI を隠し v1 見えに**統一。地図の“写真の足跡”（位置+日時のある写真を線でつなぐ＝`tripLayers`）は v1 でも出るのでスクショに使える。Web 起点なら全画面・任意ピクセルサイズで撮れる（Apple はアプリを正確に表せば Web 起点のスクショも可）。

**結果 / 観察**
- ブラウザ検証（preview）: 通常 web＝`SHOT_MODE=false / LOCATION_AVAILABLE=true`・🛰️ 表示・ヘッダ版文字列は空。`?shot`＝`SHOT_MODE=true / LOCATION_AVAILABLE=false`・🛰️ 非表示（残=🗺/🧠/📦/🗑/ℹ️）。ℹ️ モーダルに「バージョン phase3.60 (v108…」表示。`vm.Script` parse 0エラー。スクショで clean なヘッダを確認。

**教訓**
- 「Web で撮ったスクショを native アプリのストア掲載に使う」時は、**Web と提出 native の見た目差（このアプリでは位置 UI の有無）を埋める一手**が要る。`?shot` のような“見え合わせモード”で提出物と一致させるのが安全。

**残課題 / 次の方向**
- スクショ本番: ユーザーが Web を `?shot` で開き、自分の写真を入れて 6.7"/6.9" サイズで撮る（連想ウォーク/過去の今日/地図の足跡/全画面の4枚案）→ こちらで規定サイズに調整。
- 提出ビルドは 1.0(12) 以降（v107 アイコン＋v108）。

## v107 — アプリアイコン差し替え（ユーザー作の玉ボケ案を full-bleed 1024 に再構成） (2026-06-27)

**背景**
- 1.0(11) が警告ゼロ（ITMS-90683/91053 解消）になったのを機に、案B「ストア素材」へ。ユーザーが**あの日用アイコン（暗い背景＋暖色の玉ボケ＝写真/記憶のあたたかさ）**を作成・提供（`Desktop/anohi.jpg`）。

**設計判断**
- 受領ファイルは 1024×1024・透過なしで素性は良いが、**「角丸＋影＋背景枠つきのプレビュー版」**だった（四隅＝背景色 `25,32,42`／本体は角丸の中）。iOS は角丸を自動でかけるため、このまま使うと二重縁取り＋影が出る。ユーザー選択＝「この画像から作り直す」。
- **本体を四隅まで full-bleed 化**: 本体の straight-edge 内側 `[140..893]`(753px 正方形・中央) をクロップ → 1024 全面へ拡大描画（本体＝アイコンになるので**作者の構図比率を保持**）。角に僅かに残る背景枠は**角丸クリップ(半径175)＋本体ビネット近似の縦グラデ塗り**で除去（iOS の角丸 ~229 より内側なので継ぎ目なし）。System.Drawing(.NET) で生成。
- **Apple 要件を厳守**: 出力は **colorType RGB・アルファ無し**（透過アイコンは審査で弾かれる）。再読込時の 32bppArgb は GDI のメモリ表現で、ファイル実体は RGB（IHDR を node で確認）。`Contents.json` は既存の単一 1024 universal のまま、`AppIcon-512@2x.png` を上書き＝配線無改修。

**ハマったところ / 検証**
- 本体境界は閾値検出だと背景ビネット＋影で誤りやすい → **中央十字の実測スキャン**で本体 ≈ x[130,903]/y[130,901]・上が明るく下が黒に近づくビネットと確定してからクロップ。
- 検証: 生成 PNG を node で IHDR チェック（1024²/RGB/alpha=false）＋ 角・辺・中央のピクセル値で「背景枠が消え本体色で full-bleed」を数値確認 → repo 上書き後も同一を再確認。ユーザーが原寸（Desktop コピー）で見て OK。

**教訓**
- 配布物の「アプリアイコン・プレビュー画像」は角丸/影/余白が焼き込まれていることが多い。App 用は**四隅まで塗りが続く正方形・透過なし**が鉄則（角丸は OS 任せ）。Windows でも System.Drawing で正方形化＋アルファ除去は完結でき、IHDR を直接見ればアルファ有無を確実に判定できる。

**残課題 / 次の方向**
- 案B 残り: スクリーンショット（実機キャプチャ）＋ 説明文/キーワード（日本語ドラフト）＋ ASC プライバシーラベル（収集なし）→ 審査 submit。
- 任意: web 側 favicon / iOS web-clip / スプラッシュにも同アイコンを流用（今は未対応）。

## v106 — ITMS-90683 解消＝background-location プラグインをビルドから除外（位置 v1 完全オフを徹底） (2026-06-27)

**背景**
- 1.0(10)（v104+v105）を Codemagic でアップロード → **警告 ITMS-90683「Missing purpose string」**（`NSLocationWhenInUseUsageDescription` と `NSLocationAlwaysAndWhenInUseUsageDescription` が必要だが無い）。
- **v104 の判断ミスが露見**: 「プラグインは残置して JS で呼ばないだけ」では不十分。background-location の `CLLocationManager`（`requestWhenInUseAuthorization`/`requestAlwaysAuthorization`/`allowsBackgroundLocationUpdates`）が**バイナリにコンパイルされて残る**と、Apple の静的スキャンが「位置 API を参照しているのに purpose string が無い」と警告する。警告が Always/WhenInUse を名指ししたのは、まさに background-location がこれらを呼ぶから（photo-library は位置 authorization を要求しない＝CLLocation データ型を読むだけ）。提出時には**リジェクト要因**。

**設計判断**
- **位置を本当に外す＝コードをバイナリから消す**。`package.json` の `"background-location": "file:./local-plugins/background-location"` を削除 → CI の `npx cap sync ios` が `CapApp-SPM/Package.swift` を **photo-library のみ**で再生成（委ねられた Package.swift は「DO NOT MODIFY＝CLI 管理」でプラグイン未記載＝CI が package.json から毎回注入する作り。よって package.json を外すだけでビルドから消える）。
- **プラグインのソースは残置**（`local-plugins/background-location/`）＝承認後の復活用。復活手順 = ① index.html `NATIVE_LOCATION=true` ② package.json に file: 依存を戻して `npm install` ③ Info.plist の NSLocation*/UIBackgroundModes を戻す（3点を Info.plist と index.html のコメントに明記）。
- JS は無改修で安全＝`NATIVE_LOCATION=false` のとき `BgLoc` の三項が短絡して `registerPlugin('BackgroundLocation')` を呼ばない（プラグイン不在でも事故らない）。
- `package-lock.json` は一旦 extraneous エントリが残ったので**再生成**してクリーンに（CI は `npm install` だが将来 `npm ci` でも詰まらないように）。

**結果 / 観察**
- ローカル検証: `npm install` 後 node_modules から background-location 消失・photo-library 維持／lock の background-location 参照 0／inline script `vm.Script` parse 0エラー。**真の確認は次 Codemagic ビルド（ITMS-90683 が消えるか）**。
- 残リスク: photo-library が `import CoreLocation`＋`CLLocation`（PHAsset.location のデータ型）を使う。これは位置サービス要求ではない（Photos 許可で管理）ので 90683 は出ない見込みだが、次ビルドで When-In-Use 警告が残れば photo-library 側も対処する（KVC で CoreLocation import を外す等）。

**教訓**
- 「審査用に機能を落とす」時、**JS で呼ばない／Info.plist 宣言を消す だけでは Apple の静的スキャンを欺けない**。purpose string を要求する API（CLLocationManager 等）は、参照がバイナリに残る限り宣言を強制される。完全に外すなら**依存ごとビルドから除外**が必須。フラグ（NATIVE_LOCATION）は JS/UI の可逆スイッチとして有効だが、native の API 除外とセットで初めて「位置ゼロ」になる。
- Capacitor 8（SPM）でプラグインを抜くのは **package.json から外すだけ**でよい（CI の cap sync が Package.swift を再生成）。委ねられた Package.swift を手で触る必要はない。

**残課題 / 次の方向**
- 次 Codemagic ビルド（1.0(11)）で ITMS-90683 解消を確認。残れば photo-library の CoreLocation も外す。
- その後 案B 残り: ストア素材（スクショ・説明文）＋ プライバシーラベル（収集なし）→ 審査 submit。

## v105 — Privacy Manifest（PrivacyInfo.xcprivacy）を追加＝App Store 提出必須を充足 (2026-06-27)

**背景**
- 案B 提出準備の残作業①。2024-05 以降、App Store 提出には **Privacy Manifest** が必須（required-reason API の未宣言は ITMS-91053 でリジェクト）。TestFlight は警告止まりだが正式提出前に潰す。

**設計判断**
- **実コードを読んで required-reason API を確定**（推測で書かない）: 自前2プラグインの Swift を精査 → ① photo-library＝PhotoKit/UIImage/CoreLocation/Date のみ＝**対象 API なし**（Photos は用途文言で管理・required-reason ではない）② background-location＝`UserDefaults.standard` で自アプリ専用キー（`pms_bgloc_buffer`/`pms_bgloc_mode`）を読み書き＝**`NSPrivacyAccessedAPICategoryUserDefaults` / 理由 `CA92.1`**（同一アプリ内の情報アクセス）。CoreLocation は manifest 対象外。位置オフ build でもプラグインはバイナリにコンパイルされる＝Apple の静的スキャンが UserDefaults を検出するので宣言は必須。
- **データ収集＝空（Data Not Collected）/ トラッキング＝なし**: 写真も位置も端末内処理で外部送信が一切ないため、Apple の定義（収集＝端末外への送信）に照らし `NSPrivacyCollectedDataTypes` は空配列。App Store のプライバシーラベルも「収集なし」と一致させる（ローカル完結の強みをそのまま宣言）。
- **配置＝App ターゲットに1枚**（`ios/App/App/PrivacyInfo.xcprivacy`）。自前プラグインは local SPM で App バイナリに静的リンクされるため、app 直下の manifest が全 first-party のコンパイル済みコードをカバー（per-plugin manifest は SPM resources 配線が要り脆い＝見送り）。Capacitor framework は自前の manifest を同梱済み。
- **pbxproj に手動登録**（4部位＝PBXFileReference / PBXBuildFile / App グループ children / Resources ビルドフェーズ）。`cap sync`（Capacitor 8＝SPM）は App ターゲットのリソース一覧を書き換えないので手動追加が CI を通して永続する。UUID は既存衝突を避けて `AA0104A1…` を採番（FileRef×3・BuildFile×2 の相互参照を確認）。

**ハマったところ / 検証**
- Windows・Mac なしのため Xcode で開けない → **ローカル検証を二重で**: ① `node` + `plist` パッケージで PrivacyInfo.xcprivacy をパース＝構造一致（tracking=false / 収集空 / UserDefaults+CA92.1）② 同じく Info.plist をパース＝位置キー全消去・写真キー維持・暗号化フラグ維持。pbxproj は ASCII 旧形式でパーサが無い（Capacitor 8 が SPM 化で xcode パーサ依存を落としたため node_modules に無い）→ **編集後に再読して4部位の整合とインデントを目視確認**。真の検証は次 Codemagic ビルド。

**教訓**
- Privacy Manifest は「使ってる API を実コードで確認して必要分だけ」が鉄則（過不足どちらもリジェクト要因）。ローカル完結アプリは収集＝空にできて素直＝プライバシーは設計の勝ち筋。
- Capacitor 8（SPM）では App の `project.pbxproj` は cap sync で再生成されない＝リソース追加は手で入れれば永続。pbxproj パーサが無い環境では XML plist は `plist` パッケージで、pbxproj は再読で担保。

**残課題 / 次の方向**
- 案B 残り: ② ストア素材（アイコン/スクショ/説明文）＋ App Store Connect のプライバシーラベル入力（収集なし・トラッキングなしで manifest と一致）③ 次ビルド → 審査 submit。
- 承認後の位置復活時（[[location-logger]] / v104）も UserDefaults 宣言はそのまま有効（プラグインは残置のため）。Always 復活時に位置の収集は依然「端末内のみ＝収集なし」を維持できる。

## v104 — iOS 初回審査は「位置情報を完全オフ」で提出する準備（案B＝コア先行） (2026-06-27)

**背景**
- App Store 提出準備に着手（前セッションの再開ポイント A）。方針＝**案B＝写真コアを先に審査に通し、Always 背景ロガー（v96 実装済）は承認後のアップデートで追加**。初回 v1 に Always/背景位置を載せると 4.2・プライバシーで審査の注目を浴びやすいため。
- セッション開始時に AskUserQuestion で「v1 で位置をどこまで外すか」を確認 → ユーザー選択＝**完全に外す**（前面ロガーも含めて native v1 では位置ゼロ・プライバシーラベルも「位置なし」）。

**設計判断**
- **単一フラグ `NATIVE_LOCATION = false`（index.html）＋ 派生 `LOCATION_AVAILABLE = !IS_NATIVE || NATIVE_LOCATION`** で位置機能を一括スイッチ。承認後の更新で `true` に戻すだけで native のロガー/軌跡が復活する＝**最小・最も復元しやすい形**を選択（プラグイン自体の npm uninstall は見送り。`file:` 依存を外すと CI の cap sync / iOS SPM 配線が壊れやすく、次の更新で戻すコストも高い。JS で「呼ばない」を保証すれば審査上の決定要因＝Info.plist 宣言・プライバシーラベル・到達可能な位置要求は全て消える）。
- web（GitHub Pages＝審査対象外）は `IS_NATIVE=false` で常に `LOCATION_AVAILABLE=true`＝**従来どおり前面ロガーが使える**（退行なし）。位置オフは native build だけに効く。
- ゲート箇所（すべて `LOCATION_AVAILABLE` で分岐）: ① `BgLoc` を `(IS_NATIVE && NATIVE_LOCATION)` 時のみ生成（位置オフ build では null）② `startLogger()` 先頭で早期 return（あらゆる経路で geolocation/BgLoc を呼ばせない最終チョークポイント）③ ヘッダ 🛰️ ボタンを非表示＋クリック未配線 ④ boot の前回モード自動再開を停止 ⑤ 地図「⋯表示」メニューの「🛰️ 自分の軌跡」「📍 GPSなし写真を軌跡から配置」トグルを非表示（描画しないので `querySelector` 結果に null ガードを追加）。
- **Info.plist（ios/App/App/Info.plist）から位置宣言を削除**＝`NSLocationWhenInUseUsageDescription` / `NSLocationAlwaysAndWhenInUseUsageDescription` / `UIBackgroundModes=location`。写真の用途文言と `ITSAppUsesNonExemptEncryption=false` は維持。削除箇所に「承認後に戻す」breadcrumb コメントを残置。

**結果 / 観察**
- ブラウザ検証（一時的に 5274 で起動・5273 は別チャット占有）: web 実行で `IS_NATIVE=false / NATIVE_LOCATION=false / LOCATION_AVAILABLE=true / BgLoc=null`、🛰️ ボタン表示維持・boot のコンソール warn/error 0・BUILD=phase3.56 表示。inline script の `vm.Script` parse＝1ブロック 0 エラー。native 位置オフ経路を再現したメニュー描画シミュレーション＝軌跡/推定トグルが消え、封印・説明は残り、`if(mmTrack)` ガードが安全に skip。
- **native（位置オフ）の実機確認は次 TestFlight ビルドで**（web では IS_NATIVE を切れないため原理的に未確認＝フラグ分岐とプラグイン非生成は静的レビュー＋シミュレーションで担保）。

**教訓**
- 「審査用に機能を一時的に落とす」は **削除でなくフラグ**が筋が良い（次の更新で全部戻すのが前提なら、可逆性＝レビューの宿題を減らす）。位置の到達経路は複数（ボタン・boot 自動再開・visibilitychange・地図メニュー）あるので、**最終チョークポイント（startLogger の早期 return）＋入口非表示**の二段で「絶対呼ばれない」を担保するのが安全。
- ローカル静的サーバ（`python -m http.server <port>`）は autoPort と相性が悪い（PORT env を読まない）。別チャットがポート占有中の検証は、launch.json を一時的に別ポートへ→検証→戻す、が現実的。

**残課題 / 次の方向**
- 残る案B: ② ストア素材（アイコン/スクショ/説明文）＋ プライバシーラベル入力（位置=なし／写真=端末内のみ）③ **Privacy Manifest（PrivacyInfo.xcprivacy）**＝app と photo-library プラグインに required-reason API を記載（提出に必須・TestFlight は警告止まり）。
- 承認後の更新で `NATIVE_LOCATION=true` ＋ Info.plist の位置宣言を復活（Always 用途文言・UIBackgroundModes）。プラグインは repo に残置済みなので配線変更は不要。

## v103 — ウォークの再中心化（近くの6枚/足跡）も毎回上端へスクロール (2026-06-27)

**背景**
- v102 で「入場時だけ上端へ／ウォーク内の再中心化は位置保持」にしたが、実機で触ったユーザー「6枚から選んだ後の遷移も、入場と同じように画面トップを表示してほしい」＝歩いた先の中央写真を毎回先頭に見たい。

**設計判断**
- `showExplore` の `enteringWalk` ガードを撤廃し、render() 後に **無条件 `window.scrollTo(0,0)`**。入場・近くの6枚タップ（再中心化）・足跡タップ、すべてで上端へ。v102 で懸念した「歩行で飛ぶと鬱陶しい」は杞憂で、実機では「歩いた先の写真が毎回ちゃんと見える」方が良い＝ユーザー判断を優先。

**結果 / 観察**
- ブラウザ検証（`window.scrollTo` spy）: state='random'→showExplore（入場）＝[0,0]／state='explore'→showExplore（再中心化）＝[0,0]。両方で上端へ。構文 OK（inline script 1）。実機は次ビルド。

**教訓**
- スクロールの「どこまで自動化するか」は机上で決めず実機の手触りで。v102 は保守的に絞ったが、ユーザーは全遷移の統一を選んだ＝reminiscence では「タップした写真を毎回正面に」が気持ちいい。最小実装→実機→調整の周回が効く。

## v102 — ウォーク入場で上端へスクロール ＋ 未使用プラグイン削除 (2026-06-27)

**背景**
- 実機: 「初期3枚タップ→ウォークに来ると6枚グリッドの方にスクロールされている。入場時は画面トップ（中央写真）を見せてほしい」。原因＝アプリにスクロール制御コードが無く（grep でヒット0）、`render()` が `#main` の中身を入れ替えても window が前のスクロール位置を保持するため、ホームで下にスクロールした状態でカードを押すとウォークでもその位置（6枚グリッド）に残っていた。
- ＋ v101 で未使用化した `@capacitor-community/media`（spike A テスト専用）を削除（ユーザー「外しておいて」）。

**設計判断**
- `showExplore` 冒頭で `enteringWalk = (state !== 'explore')` を取り、`render()` 後に `if (enteringWalk) window.scrollTo(0,0)`。**ウォークへの「入場」時だけ上端へ**（中央＝タップした写真を先頭に見せる）。**ウォーク内の再中心化（近くの6枚/足跡タップ）は位置を保つ**＝歩くたびに飛ばない（reminiscence のリズムを壊さない）。レイアウトは window スクロール（body に overflow 無し・`#main` も非スクロール）なので `window.scrollTo`。
- `npm uninstall @capacitor-community/media`＝package.json/lock 更新（deps は @capacitor/core・ios＋自前 photo-library・background-location の4つに）。CI が cap sync。

**結果 / 観察**
- ブラウザ検証（`window.scrollTo` を spy）: state='random'→`showExplore`＝`scrollTo(0,0)` が1回／state='explore'→`showExplore`（再中心化）＝スクロール呼び出し0。構文 OK（inline script 1）。
- 実機確認は次ビルド。

**教訓**
- innerHTML 差し替えで画面遷移する作りでは、ブラウザは旧スクロール位置を保持する。「画面が変わったら上端」を期待する遷移には明示 `scrollTo` が要る。ただし**全 render で上端に飛ばすと歩行が鬱陶しい**ので、状態遷移（入場）に限定するのが要点。

**残課題 / 次の方向**
- 提出前 Privacy Manifest（PrivacyInfo.xcprivacy）。実機で入場スクロールの手触り（再中心化での位置保持が自然か）。

## v101 — spike 撤去：🧪「写真スパイク」診断パネルを本体から削除 (2026-06-27)

**背景**
- App Store 提出ビルド（1.0(9)）を回した流れで、ユーザー「写真スパイクがまだ残ってる。もう使わないよね」。`#spk-*`「🧪 写真スパイク」＝v92-93 の「写真全件アクセス 診断」自己完結スパイク（A:`@capacitor-community/media` 一括 vs B:自前 PhotoLibrary を実機計測）。**B は v94 で本体統合済み＝役目終了**。提出前の整理（TODO「診断 block 撤去」）を実行。

**設計判断**
- block は末尾に自己完結（HTMLコメント＋`<style>`＋`<script>` IIFE・約227行・`window.Capacitor.isNativePlatform()` の時だけ FAB を出す造り）。main app と疎結合（main は `#spk-*` を一切参照しない）だったので、**block ごと丸削除**。main app の `</script>` が直接 `</body>` に。
- 削除は node で marker（🧪 ネイティブ写真全件アクセス 最小スパイク）の直前 `<!--` 〜 直前の `</script>` を slice。`@capacitor-community/media` は **この block 専用**だった（grep で確認）→ 撤去で未使用化。package.json から外せばビルドが軽く、提出前のデッドな写真アクセスプラグインを消せる（**ユーザー確認のうえ別途・cap sync と次ビルドが要る**）。

**結果 / 観察**
- ブラウザ検証: 構文 OK（inline script 2→1）。reload 後 `#spk-fab`/`#spk-overlay` 消滅・`あの日` 通常 boot・コンソールエラー0。web 体験は無傷（元々 native 限定 FAB なので web には出ていない）。
- 実機（native）では左下の紫 `🧪 写真スパイク` が消える＝次ビルドで確認。

**教訓**
- 検証スパイクは「末尾に自己完結 block＋native 限定で出す」造りにしておくと、役目を終えた時に**丸ごと安全に外せる**（main と疎結合・web に出ない）。block 冒頭コメントに「検証が済んだら撤去」と書いてあったので回収漏れしない＝撤去予定はコード内に明記しておくと効く。

**残課題 / 次の方向**
- `@capacitor-community/media`（A 専用・未使用化）を package.json から外すか＝ユーザー確認。提出前 Privacy Manifest（PrivacyInfo.xcprivacy）も残課題。

## v100 — 連想ウォーク中央の写真をタップで全画面 (2026-06-27)

**背景**
- 実機フィードバック。拡大（全画面）をよく使うので「いま見ている中央の写真は長押しせずタップで開きたい」。当初は「タップ=全画面に全面変更」の要望だったが、AskUserQuestion で現状を表に落として整理した結果、**変えるのは中央写真のタップだけ**に確定（核のウォーク操作＝初期3枚タップ=ウォーク開始／近くの6枚タップ=歩く、は不変）。

**設計判断**
- 中央カードの `attachPhotoGestures(centerEl, center, null)` を `() => showFullImage(center)` に。タップでも長押しでもフル画像。近くの6枚（tap=歩く・長押し=フル）と初期3枚（tap=ウォーク開始・長押し=フル）は従来どおり＝タップの競合なし。ヘルプ文も「中央の写真はタップでも」を追記。
- 中央カードの地図ボタン／✕除外は従来どおり pointer 系で propagation 停止＝タップ拡大と誤爆しない。

**結果 / 観察**
- ブラウザ検証: テスト写真を注入→`showExplore`→中央カードの画像を click→`.full-overlay` が出現（タップ前は無し）。地図ボタンは伝播停止のまま誤爆なし。全 inline script 構文 OK。実機確認は次。

**教訓**
- 「タップ=全画面に全面変更」は核のウォーク（タップで歩く）と競合する → AskUserQuestion で**現状の挙動を表で見せ**、ユーザー自身に競合点（近くの6枚）だけ判断してもらうと、最小の1点変更に着地できた。要望の言葉そのままに実装せず、競合を可視化して詰める。

**残課題 / 次の方向**
- 実機で中央タップの手触り。初期画面からウォークに入った直後、中央写真が画面に見える位置にあるか（スクロール）は要観察＝必要なら入場時に上端へスクロール（要望②「タップした写真がしっかり見える」の仕上げ）。

## v99 — 全画面 ⛶ と「地図の種類」の位置を交換（使用頻度に合わせる） (2026-06-27)

**背景**
- v98 で全画面を ⋯表示メニューに入れたが、ユーザー「全画面の方が使う。全画面表示と地図の切り替え（ダーク/地理院）の位置を交換したほうがいい」。使用頻度＝全画面 > 地図の種類。

**設計判断**
- **⛶ 全画面を右上の常設トグルに昇格**（旧 `L.control.layers` があった位置）。タップで全画面 ON / もう一度で解除＝1アクションのトグル（没入中もこの ⛶ だけ残り teal で「解除」を示す）。旧 v98 の左上 解除ボタン（`.map-immersive-exit`）と ⋯メニューの `mmFull` は廃止。
- **「地図の種類」(ダーク/地理院 標準/淡色) を ⋯表示メニューへ**＝Leaflet の `L.control.layers` を撤去し、メニュー内に chip（アクティブを teal）で実装。タップで `removeLayer`→`addLayer`＋`bringToBack`（タイルは最背面）＋localStorage 保存（旧 `baselayerchange` ハンドラの保存を移譲）。初期レイヤは従来どおり localStorage から。
- **desktop の ⛶ 位置**: 通常はタイムライン右サイドバー(320px)の左＝`right:330px`、没入で地図全幅になるので `right:10px`（mobile は常に右上 `right:10px`）。

**結果 / 観察**
- ブラウザ検証: ⛶ 右上に常設・トグル ON/OFF で chrome 開閉・没入中も ⛶ 残存。地図の種類 chip で base layer 切替＋teal 強調＋localStorage 保存（gsiStd 確認）。Leaflet layers コントロール／旧 exit／mmFull すべて消滅。desktop で ⛶ がサイドバーに被らない（toggle right 670 < sheet 680）・没入で right:10。全 inline script 構文 OK。
- 実機確認は次。

**教訓**
- コントロールの一等地（常設アイコン）は使用頻度で決める。設定寄り（地図の種類＝set-once）はメニューへ、行為（全画面＝高頻度）は常設へ。Leaflet 標準コントロールも自前 UI に寄せると配置・見た目を一貫させられる（attribution だけは Leaflet 管理のまま残す＝タイル規約）。

**残課題 / 次の方向**
- 実機で ⛶ トグルの手触り（右上の指の届き・地図の種類切替）。

## v98 — 地図に「地図だけ全画面（没入表示）」＝動線と写真だけ (2026-06-27)

**背景**
- v97 でツールバーがすっきりした流れで、ユーザー要望「地図と動線と写真だけ／全画面解除だけのアイコン／タイムラインの出ない全画面表示が欲しい」。地図を邪魔なく眺めるための没入モード。[[ui-minimalism-works]] ど真ん中（削ぎ落とす全画面）。

**設計判断**
- **入口は ⋯表示メニュー**（`⛶ 地図だけ表示（全画面）`）＝v97 で整えた2段ツールバーを汚さない。没入は時々使う操作なので2タップで十分、デフォルトのすっきりを優先。
- **没入中は解除アイコン1つだけ**＝左上に `⛶`（teal・通常の ✕閉じる と同じ位置／✕は隠れる）。タップで通常表示に戻る（地図を閉じるのではなく没入を抜ける＝ユーザー指定「全画面解除だけのアイコン」）。
- **隠す対象**: ツールバー（`.map-ctrlbar`）・タイムライン（`.timeline-sheet`）・✕閉じる・ズーム・レイヤ切替。**地図の出典 attribution はライセンス上 残す**（CARTO/OSM/地理院）。実装は overlay に `.immersive` クラス1枚＝CSS で `display:none`（状態を overlay の class に集約＝[[map-view-unparked]] と同型）。
- **desktop は地図を全幅に**: 通常 `#mapEl{right:320px}`（タイムライン=右サイドバー）→ 没入で `right:0` に広げ `map.invalidateSize()` で再描画。mobile はもともと全画面なので class だけで済む。
- 動線（写真の旅行線＋ロガー軌跡）と写真マーカーは通常どおり残る＝「地図と動線と写真だけ」。マーカーの tap/popup も生きる。

**結果 / 観察**
- ブラウザ検証（computed-style eval）: 没入 ON で toolbar/timeline/close/zoom/layers すべて `display:none`・exit `⛶` 表示・attribution 残存。`⋯→⛶` で没入 ON＋メニュー閉じ。exit タップで全復元。**desktop で #mapEl が 320px→0 に広がり、解除で 320px に戻る**。全 inline script 構文 OK。
- 実機確認は次（GitHub Pages / TestFlight）。

**教訓**
- 「削ぎ落とす全画面」は class 1枚＋CSS で軽く乗る。chrome の出し入れは overlay の class に集約すると分岐が散らからない。**attribution だけは消さない**（タイル規約）。スクショが詰まる地図ページは computed-style の eval で決定的に検証。

**残課題 / 次の方向**
- 実機で没入の手触り（解除 `⛶` の分かりやすさ・desktop の地図再描画）。違和感あれば入口を常時アイコンに昇格 or 解除ボタンの見た目を調整。

## v97 — 地図ツールバーを1種類に統一（モードで変わる二重UIを解消） (2026-06-27)

**背景**
- ユーザー指摘「地図のUIが2種類になっている」。実機ネイティブ=`◀▶`無しの4アイコン／web=`◀▶`有りの6アイコン／全期間ブラウズ=テキストボタン、とモードでツールバーの出し入れが起きて不統一だった。1種類に固定したい。

**設計判断**
- **どのモードでも同じ並びに固定**: 1段目 `◀ 📆 ▶`（過去/未来の「今日」を辿る・📆=今日に戻る&現在地）／2段目 `🎲 偶然 ／ 📅全期間 ／ ⋯表示`。`.map-ctrlbar` を flex-column 2段（`.mc-row`）に。`updateCtrlMode` は「ボタンの出し入れ」をやめ、🎲 の title 切替（random3 中=もう一度引く）だけに縮小。
- **✕のみ**（旧「✕ 閉じる」のテキストを落とす）。
- **⋯表示メニューに ⓘ を統合**（旧スタンドアロンの ⓘ ボタン＝`mcHelp` を廃止し、メニュー項目 `mmHelp`＝「ⓘ アイコンの説明」から凡例 pop を開く）。`↩︎ 全部を見る`（`mcAll`）は廃止し `📅全期間` がその役割を吸収（タップ＝全期間に戻る＆期間でしぼる。`clearPick` はタイムラインの「✕ 全部を見る」が引き続き使うので関数は残す）。
- **🎲 偶然の3日（核①偶然）は2段目に残す**＝AskUserQuestion でユーザーが「見える位置に保つ」を選択（メニュー埋没／地図から外す は不採用）。📆 今日リセットは `◀ 📆 ▶` の中央（同 AskUserQuestion）。

**ハマったところ**
- 2段化したら2段目（🎲 📅全期間 ⋯表示）が **絶対配置の縦flexの shrink-to-fit で意図せず折り返し**、バーが109pxに膨らんで pop（`top:128px`固定）と重なった（geometry eval で `popClearsBar:false`・`mcMore` が2行目に落ちているのを検出）。→ `.map-ctrlbar { width: max-content }` で「広い方の行」にバー幅を合わせ、両段とも1行に（バー70px・pop が確実に下）。`📅` ボタンは `max-width:46vw`+省略で長い期間ラベルでも折り返さない。

**結果 / 観察**
- ブラウザ検証（page context の geometry eval）: **5モード（today/random3/day/days/browse）全てで hidden:[]＝どのモードでもボタンが消えない**（二重UI解消）。mobile(375)/desktop(1000) 両ブレークポイントで pop がバーに被らない。⋯表示→ⓘで凡例（◀📆▶/🎲/📅/⋯）が開く。全 inline script 構文 OK。
- スクショは Leaflet のタイル常時読み込み＋wakeLock でレンダラが idle にならず timeout（ページ不具合ではない）→ geometry eval で代替検証＋mockup で可視化。
- 実機確認は次（GitHub Pages / TestFlight）。

**教訓**
- 「モードでボタンを出し入れする」設計が二重UIの根。**固定した最小セット**の方が学習しやすく [[ui-minimalism-works]] とも両立（情報を増やすのではなく、出入りを止める）。
- 絶対配置の縦 flex は shrink-to-fit で折り返す。`width: max-content`+`max-width` で安定。スクショが詰まる重い動的ページは **geometry eval（getBoundingClientRect）で視覚レイアウトを決定的に検証**できる。

**残課題 / 次の方向**
- 実機で2段ツールバーの押し心地・📆中央リセットの分かりやすさ。
- （別件・記録のみ）App Store 提出は案B（コア先行→後で Always 追加）に決定＝memory [[location-logger]] に記録。

## v96 — 位置ロガーをネイティブ背景記録に（アプリを閉じても記録） (2026-06-26)

**背景**
- [[location-logger]] で「閉じている間も記録する常時ログは native の本命・post-v1」と置いていた項目を、自前プラグインの土台（PhotoLibrary）が実機で通った今、ユーザー要望で un-park。「位置ロガーがアプリを閉じても機能するようにしたい」。

**設計判断**
- **自前 Swift プラグイン `BackgroundLocation`**（PhotoLibrary と同じローカル SPM プラグイン方式）をユーザーが選択（依存を増やさずフル制御・local-first）。コミュニティ製（@capacitor-community/background-geolocation）は不採用。
- **iOS の現実に合わせた2モード**:
  - **重要な移動 = Significant Location Change (SLC)**: 基地局ベース・約500m・超低電池。**アプリを完全終了しても iOS が背景でアプリを起こして配送**＝「閉じても効く」の中核。今の「重要な移動=500m」と一致。
  - **こまめ = 標準の継続更新**（`allowsBackgroundLocationUpdates`・distanceFilter 25m・accuracy best）＝背景中（別アプリ/ロック）でも記録、高精度だが電池を使う。保険で SLC も併用。
- **終了後の復帰**: `BackgroundLocationManager` を singleton にし `init` で前回モードを UserDefaults から復元して監視を再アーム。プラグインの `load()`（起動時＝背景再起動含む）で singleton を生かす。
- **JS 不在中の永続化**: 記録点はネイティブが **UserDefaults にバッファ**（座標+時刻+精度のみ）。JS は起動/前面復帰/8秒タイマーで `drain()` し、**既存の track ストアへ合流**（keyPath=id で冪等・座標+時刻のみ＝機種変更で移せる §⑥）。downstream（地図の青緑軌跡・GPSなし写真の補完 v74）は無改修。
- **権限**: `NSLocationAlwaysAndWhenInUseUsageDescription` 追加＋`UIBackgroundModes=location`。WhenInUse→Always の段階要求（`didChangeAuthorization` で昇格）。requestAlways はユーザー操作（モード選択）の文脈でだけ（起動 resume では再要求しない＝ナグらない）。
- **web は完全に従来どおり**: `BgLoc` は native のみ。web は前面のみの `navigator.geolocation`（`visibilitychange` で停止）を維持。全分岐 `if (BgLoc)`。

**検証（実機前にブラウザで）**
- 全 inline script 構文 OK。web preview で BgLoc=null・IS_NATIVE=false・コンソールエラー無し・ロガーパネルは web 版文言、を確認＝web 経路は無傷。
- ローカル `npx cap sync ios` で **3プラグイン認識**＋生成 `CapApp-SPM/Package.swift` の `.product(name:"BackgroundLocation",package:"BackgroundLocation")` 一致を確認（v93 の exit-74 を事前回避）。生成物（Windows パス）は revert（CI 再生成）。
- **実機が要るのは CLLocationManager の背景挙動（SLC 配送・終了後の復帰・Always 許可フロー・電池）＝次の TestFlight で確認**。

**教訓**
- 自前ネイティブの2個目。PhotoLibrary の規約（package/product/target 名一致＝`BackgroundLocation`、import Foundation/CoreLocation/Capacitor）がそのまま効いた。
- 「閉じても」の肝は **singleton + load() で起動時に監視を再アーム**＋**ネイティブ側バッファ（JS 不在でも記録を落とさない）**。WKWebView/IndexedDB は背景で動かないので、永続化はネイティブに置くのが必須。

**残課題 / 次の方向**
- **🎉実機 YES（2026-06-27・重要のみ/SLC）**: 1日歩行で**地図に軌跡が出た**＝閉じても背景記録 OK／**電池ほぼ無コスト**（iOS バッテリー画面で Madeleine 5%・内訳 画面上1時間9分／バックグラウンド4分のみ・夕方18時で66%残）／15点/日（SLC 約500m級の粗さ）。**残**＝完全終了(force-quit)→SLC 復帰の明示テスト・こまめ(25m)計測・Always 許可フロー（案B で初回提出は位置オフなので急がない）。
- （旧残課題）実機 TestFlight で: 散歩中アプリを閉じて軌跡が貯まるか／完全終了→SLC で復帰して点が入るか／Always 許可フロー／電池感／前面復帰で drain される手触り。
- 完全終了からの SLC 復帰が弱ければ AppDelegate でも singleton を起こす配線（モジュール import の確認が要る）を追加。
- App Store 審査で Always 位置の用途説明を準備（「あとから軌跡を振り返る」＝正当だが要注意項目）。track は将来 SQLite。

## v95 — 全ライブラリ reminiscence が実機で強い YES ＋ 拡大表示の旧 iOS 回避策2つを撤去 (2026-06-26)

**背景**
- v94 でネイティブ全ライブラリ取り込みが実機 YES（速い・止まらない）。その先＝「全ライブラリで reminiscence の手触りが上がるか」を実機で確認し、全ライブラリ＋綺麗な表示で不要になった旧 web 回避策を畳む。

**結果 / 観察（実機・全項目で強い YES＝生の言葉）**
- **🗺 On This Day（数年分の同じ月日）**: 刺さる。「あのときからもう何年たったのか…また同じ年数の未来に僕はどこにいるんだろう」＝**過去だけでなく「同じ年数の未来の自分」まで想像**＝核②久しぶり×時間連続性が full library で一段深化（[[reminiscence-at-scale-works]] [[on-this-day-daily-entry]]）。
- **連想ウォーク**: 「あーなつかしい。あれどこだっけ、そうそう」＝**偶然引かれた古い写真で "うわ久しぶり" ＋ 能動的な想起**（核①偶然×②久しぶり×③よみがえる）。
- **地図**: 「あーそんな道歩いたな。**写真には無い記憶も思い出す**」＝**軌跡そのものが、写真に写っていない記憶まで呼ぶ**＝地図/位置ロガー方向の価値の深い裏づけ（[[map-view-unparked]] [[location-logger]]）。
- **タイムラインから位置を直す行為自体が記憶を呼ぶ**（ユーザー自発言）＝[[editing-triggers-reminiscence]] を full library 規模で再確認。
- **拡大画質（512px 流用）**: 「いいね。めっちゃきれい」＝**原寸 fullImage は急がなくてよい**（512 で実機満足）。
- **差分同期**: 「完璧」＝📚再押しで新規だけ追加・重複なし（assetId dedup が実機で効いた）。

**設計判断（撤去）**
- 拡大表示の **「📷 写真アプリで開く」（`photos-redirect://`）と「日付をタップでコピー」（純正アプリで日付検索する用）を撤去**。両方とも web 時代の iOS 回避策＝原画を持たない web で「鮮明に見たい/元写真を探す」ための動線だった（v24-25）。native で全ライブラリを 512px で綺麗に表示＝アプリ内で完結し、純正アプリへ飛ばす理由が消えた（Phase 1 計画どおり）。ユーザーも「この2つは不要になった」と明言。
- 残すのは **🗺 地図 / 📅 日付を直す**（日付表示自体は想起の手がかりとして維持）。使い方モーダルの該当案内・dead CSS（`.to-photos` / `.dt small`）も整合撤去。preview で撤去後のキャプション（地図＋日付を直すの2ボタンのみ・コピー誘導/写真アプリ消滅・実行時エラー無し）を確認。

**教訓**
- native 化＝単に器を替えるだけでなく、**web 制約のために作った回避策（原画なし→純正アプリ動線、品質 caveat）を畳めるタイミング**。制約が消えたら、それを補う UI も一緒に消すと体験がシンプルになる（[[ui-minimalism-works]]）。
- 「速い/止まらない」の次に確かめるべきは必ず**体感（核の payoff）**。今回 import 速度の YES と reminiscence の YES は別物として両方取れた＝後者がプロダクトの本丸。

**残課題 / 次の方向**
- 拡大=原寸 fullImage は**保留**（512 で実機「めっちゃきれい」＝優先度低）。要望が出たら追加。
- 次の自然な一手＝ストア提出に向けた素材（アイコン/スクショ/説明文）or AdMob/IAP、または起動時 自動差分同期。診断 block（#spk-*）撤去・Privacy Manifest は提出準備で。

## v94 — B をアプリ本体に統合（ネイティブ全ライブラリ取り込み→既存の体験ロジックがそのまま動く） (2026-06-26)

**背景**
- v93 で自前 PhotoLibrary プラグインの API（requestAccess/enumerate/thumbnail）が実機 YES。Phase 1 の本丸＝この B をアプリ本体の取り込み動線に統合し、ネイティブで「全ライブラリ取り込み」したものが既存の体験ロジック（連想ウォーク/地図/タイムライン/On This Day/色・意味の近傍）で**そのまま動く**状態にする。

**設計判断**
- **案1（既存パイプラインに流し込む）を採用** — enumerate で全件メタ→各写真のサムネを `thumbnail({id,size:512})` で取得し、**web と同一形の record（thumb=Blob, color=Float32Array(48)）を dbPut**。downstream（表示 thumbUrl / 色 backfill / CLIP / 地図 / walk）は**完全無改修**で乗る。案2（メタのみ保存＋オンデマンド参照）は超省メモリだが全表示経路の大改修＝スケールが問題化してから。2054枚は案1で余裕（[[storage-tradeoffs-accepted]]）。
- **案1の中でさらに軽量版** — サムネは createThumbnail で再エンコードせず、**OS 生成サムネ Blob をそのまま thumb に保存**し、色だけ backfillColors と同じ 16×16 縮小で抽出（再エンコード無し＝速い・PHImageManager は向き補正済みを返すので回転不要・色は backfill 済み web 写真と同じ精度で整合）。
- **写真キーは UUID 維持・assetId(localIdentifier) は属性** — dedup キー `asset|<localIdentifier>` と差分同期に使う（機種変更耐性・Notion §⑥）。dedup インデックスの `asset|` 範囲走査（collectDummyKeys と同型・値を読まずキーだけ）で「取り込み済み assetId 集合」を高速回収→**再実行は差分（新しく撮った写真）だけ追加**。
- **段階導線**（核＝偶然/久しぶり/よみがえる × [[ui-minimalism-works]]） — ネイティブの空状態は「📚 ライブラリ全体から始める」を主役に、web ピッカーは「数枚だけ選んで試す」副導線へ。取り込みメニューにも「📚 ライブラリ全体（おすすめ）」を追加。**全て `Capacitor.isNativePlatform()` で分岐＝web は完全に従来どおり**。
- fast-track（先頭6枚で即遊べる）→残りは背景低優先（`NATIVE_IMPORT_DELAY=8ms`・web の HEIC デコードより遥かに軽い）で web の importFiles と同じ二段構え。進捗は既存 🌀 `updateBgStatus`、サマリ・色/embedding backfill も既存関数を再利用＝新規写真にも自動で色・意味軸が乗る。
- 新規コードは index.html の importOne 直後に1ブロック集約: `importNativeLibrary`/`importNativeAsset`/`importOneNative`/`processNativeRest`/`collectImportedAssetIds`/`colorFromBlob`/`dataUrlToBlob` ＋ `IS_NATIVE`/`PhotoLib` 参照。

**検証（実機前にブラウザで中核ロジックを固めた）**
- ネイティブの**記録生成パイプライン本体**は Capacitor 非依存なので preview の page context で関数単体テスト＝ダミー JPEG(canvas)→`dataUrlToBlob`→`importNativeAsset` に流し、record が web と同一形（id=UUID36 / assetId / `dedup='asset|...'` / datetime=Date / blob=null / thumb=Blob / **color=F32(48)・実色と一致** / dateSource='exif'）、`collectImportedAssetIds` が回収、再取り込み→`duplicate`、date=null→`no-datetime` skip を確認。
- 全 inline script を `vm.Script` で parse 検査＝web もろとも壊れる構文事故ゼロ。web は IS_NATIVE=false でネイティブ UI を隠して従来通り描画（コンソールエラー無し・空状態スナップショット確認）。
- **実機が要るのは PhotoLibrary プラグイン呼び出し（requestAccess/enumerate/thumbnail）と IS_NATIVE 分岐のみ**＝次の TestFlight で確認。

**結果 / 観察（実機・強い YES, 2026-06-26）**
- 実機 iPhone の TestFlight で「📚 ライブラリ全体」→ **約2000枚を高速に取り込み、最後まで止まらなかった**。ユーザーの生の言葉:「完璧だね。すごい速さで写真を取り込んだ。しかも止まらなかった。2000枚もあったのに」。
- **web/iPad で起きていた raw JPEG デコードの OOM/wedge（v35-37・47枚で落ちた）が native では起きない**＝OS が既に縮小サムネを返す「案1軽量版（再エンコードしない・サムネは OS 任せ）」の狙いどおり。fast-track→背景スロットルも詰まらず完走。
- **capability(v92)＋本命アーキ B(v93)＋本体統合(v94) が実機で全部つながった＝ボトルネック①（写真全件アクセス）が完全クローズ**。残る観察は「全ライブラリでの reminiscence の体感」「差分同期」「拡大画質」。

**教訓**
- 「downstream を無改修にする」設計（record の形を web と一字一句同じにする）が効いた＝統合の継ぎ目を `importNativeAsset` 1点に閉じ込められ、表示/色/CLIP/地図/walk を一切触らずに済んだ。新ランタイム統合は「既存の中心データ構造に合わせて作る」と影響範囲が激減する。
- Capacitor 非依存な純ロジック（record 生成・色・dedup・差分集合）は**ブラウザ page context で関数単体テストできる**＝高コストな実機ビルドの前に大半を固められる。実機でしか分からないのはプラグイン境界だけに絞る。

**残課題 / 次の方向**
- 実機 TestFlight で: 「📚 ライブラリ全体」→数百〜2054枚が背景取り込みされ、連想ウォーク/地図/On This Day/色・意味の近傍が全ライブラリで動くか／取り込み速度・体感・メモリ／差分同期（再実行で新規だけ）。
- 拡大=原寸: 今は512サムネを拡大にも流用（web と同じ）。プラグインに `fullImage({id})` を足してオンデマンド原寸差替えは次段（native での容量制約解消・[[storage-tradeoffs-accepted]]）。
- 起動時の自動差分同期は今は手動（📚再押し）。自動化は許可ダイアログの出方を実機で見てから。
- 提出前 Privacy Manifest（PrivacyInfo.xcprivacy）／診断 block（#spk-*）撤去は本体統合の実機確認が済んでから（今回は比較用に残置）。memory [[native-photo-access-works]]。

## v93 — 自前 Photos プラグイン（Approach B 第一歩）＋診断に B 列・暗号化質問を恒久スキップ (2026-06-26)

**背景**
- v92 で capability（全ライブラリ到達）が YES。次は製品でそのまま使う本命アーキ＝「全件メタを即時列挙＋サムネはオンデマンド」を自前 Capacitor プラグインとして第一歩実装し、実機で B 版 API を検証する。

**設計判断**
- **ローカル Capacitor プラグイン package（`local-plugins/photo-library`・root package.json から `file:` 参照）** として作成。理由＝Mac なしで pbxproj/storyboard を手編集する事故を避ける。`cap sync` が `package.json` の `capacitor.ios.src` と `Package.swift`(SPM) を読んで CapApp-SPM に自動配線（媒体プラグインと同じ仕組みを node_modules の実物から真似た）。
- **ローカルで `npx cap sync ios` を実行して push 前に配線を確認**（`Found 2 Capacitor plugins: @capacitor-community/media, photo-library` ＋ 生成 Package.swift に `PhotoLibrary` 追加を確認）。生成される `CapApp-SPM/Package.swift` は環境依存パス（Windows はバックスラッシュ）になるので**コミットせず CI 再生成に任せる**（v92 と同じ運用・`git checkout` で戻す）。
- プラグイン API（最小3つ）: `requestAccess()`（PHPhotoLibrary 読み書き許可）/ `enumerate({limit})`（`PHAsset.fetchAssets` で**全件数は即時**＋先頭 limit 件のメタ＝id/日時/GPS/サイズ・サムネ無し）/ `thumbnail({id,size})`（`PHImageManager` で**1枚オンデマンド**・dataURL 返却）。
- 診断オーバーレイに **B 列（③全件カウント＋メタ2000 / ④サムネ48枚オンデマンド）** を追加し、A（媒体プラグイン）と1ビルドで比較可能に。
- ついでに **Info.plist に `ITSAppUsesNonExemptEncryption=false`** を追加＝毎ビルドの暗号化コンプライアンス質問を恒久スキップ（標準 HTTPS のみ＝免除）。

**ハマったところ（初回 CI ビルド失敗→修正）**
- **症状**: Step6「IPA をビルド」が `Failed to show build settings` / exit 74（Swift コンパイル**前**＝SPM パッケージ解決の失敗）。
- **原因**: cap sync は npm 名 `photo-library` から SPM 名 **`PhotoLibrary`** を導出し、`CapApp-SPM` に `.product(name:"PhotoLibrary", package:"PhotoLibrary")` を生成する。ところがプラグインの `Package.swift` は package/product を `PhotoLibraryPlugin` と宣言していた＝**product 名の不一致**で解決失敗。
- **対処（fix・v93）**: プラグインの Package.swift の package 名・product 名・target 名を **`PhotoLibrary`** に統一（media が `CapacitorCommunityMedia` で名前一致させているのと同じ規約）。**教訓: ローカル plugin の Package.swift の名前は「npm 名から cap sync が導出する名前」に必ず一致させる**。ローカルで `npx cap sync ios` → 生成 `CapApp-SPM/Package.swift` の `.product(name:..., package:...)` を読んで一致確認するのが確実。
- ローカル `cap sync ios` は Windows パスで `CapApp-SPM/Package.swift` を書く（Mac で無効）→ CI が再生成するので**この生成物はコミットしない**。`local-plugins/` 本体・`package.json`(file:)・`package-lock.json` はコミット必須。

**結果 / 観察（実機 iPhone・B も強い YES）**
- env: `PhotoLibrary(B): 検出 ✅` ／ `許可: authorized`（自前プラグインが CI で配線・登録され Swift もコンパイル＝**Mac なしで初の自前ネイティブコードがビルドに乗った**）。
- **③ enumerate**: **全件数(即時) 2054 枚**（ピッカー無しの真の総数）＋先頭2000のメタを **往復 180ms / native内部 158ms**。日時 2000/2000・GPS 215/2000・範囲 2021/8/5〜2026/6/26。
- **④ thumbnail（オンデマンド）**: 48/48 枚を **341ms＝7ms/枚**。サムネ描画 OK。
- **A 比（同規模 約2000枚）**: A は「全サムネ base64 一括」で約6000ms。B は**全件カウント＋2000メタが180ms＝約33倍速**、しかもサムネは要る分だけ7ms/枚＝**メモリも軽い**。「列挙(メタ)とサムネ生成を分離」する本命アーキの優位を実機で確認。

**教訓**
- 自前 Capacitor プラグインの first-build 失敗は2種に切り分ける: ①`showBuildSettings`/exit 74＝**SPM 配線**（package/product 名を cap sync 導出名に一致）②`error:` 行＝**Swift コンパイル**。ローカル `cap sync ios`（①の名前一致）＋目視レビューで import 確認（②）を push 前にやると、高コストな実機ビルドを節約できた（今回 2サイクルで配線確立）。
- B の本質的優位＝**列挙(メタ)とサムネ生成の分離**。A は両者が密結合で全件 base64＝スケールに弱い。

**残課題 / 次の方向**
- B 版 API は実機 OK（v93・第一歩完了）。次は **アプリ本体の取り込み動線に統合**（ピッカー→全ライブラリ列挙へ・メタは IndexedDB→将来 SQLite・サムネはオンデマンド or キャッシュ・拡大は原寸）＝Phase 1 の本丸。
- 写真キーは OS の localIdentifier を直接 DB キーにせず UUID 維持（機種変更耐性・Notion §⑥）＝localIdentifier はメタの一属性として保持。
- 最終提出時は Photos アクセスの Privacy Manifest（PrivacyInfo.xcprivacy）が要る見込み（TestFlight は警告止まりで通る）。
- 診断 block は本体統合が済んだら撤去。memory [[native-photo-access-works]]。

## v92 — ネイティブ写真全件アクセスの最小スパイク（Approach A: コミュニティプラグインで可否判定） (2026-06-26)

**背景**
- 署名パイプライン(v91)が通り、残る最大の de-risk＝「ネイティブで全ライブラリ（ピッカーでなく）にサムネ＋EXIF 付きで届くか」を実機1ビルドで判定する番。最小スパイクの趣旨に沿い、まず手数の最も少ない方法で capability を確かめる。

**設計判断**
- 2案を一次ソースで比較 → **A＝`@capacitor-community/media`（コミュニティプラグイン）を採用**。ネイティブ手編集ゼロ（`npm install`＋web JS のみ／`cap sync` が CI で自動配線）＝初回ビルド成功率が最も高く「最小スパイク」に合う。`getMedias()` が 件数・base64 サムネ・`creationDate`・`location`(GPS) を返すので、**列挙＋サムネ＋EXIF の3点を1ビルドで判定**できる。
- **採用しなかった案 B＝自前 Swift プラグイン（Photos framework 直叩き）**: 全件カウント即時＋オンデマンドサムネ＝本命アーキだが、Mac なしで Xcode プロジェクト(pbxproj)/プラグイン登録を手編集する罠が多く初回ビルドで詰まりやすい。**capability が YES と出てから B に進む**（A の弱点＝base64 一括返却でスケールに弱い、は B で解消）。ユーザーも「まず最小で可否確認(A)」を選択。
- スパイクは **完全自己完結のオーバーレイ診断**として index.html 末尾に追加。`window.Capacitor.isNativePlatform()` が真のときだけ浮きボタン「🧪 写真スパイク」を出し、**web(GitHub Pages)では何も出さない＝既存体験に非干渉**（preview で `#spk-fab` 非生成・コンソールエラー無しを確認）。検証が済んだら block ごと撤去して本実装へ。
- 診断は「①300枚」「②2000枚」の2ボタン＝まず安全に、次にスケール感を見る。結果に 件数 / 取得時間 / 日時あり数 / GPSあり数 / 日時レンジ / 先頭48サムネ を表示。

**実装メモ**
- `@capacitor-community/media@^9.1.0`（v9 系が Capacitor 8 対応・`Package.swift` あり＝SPM 互換・`jsName="Media"`＝コードの `Capacitor.Plugins.Media` と一致を確認）。プラグイン取得は `Plugins.Media` ∥ `registerPlugin('Media')` の二段構えで堅牢化。
- Info.plist は v89 で `NSPhotoLibraryUsageDescription`（読み取り）既設＝追加不要。CI は既存 codemagic.yaml の `npm install`→`npx cap sync ios` でプラグインを配線（**yaml 変更不要**）。

**結果 / 観察（実機 iPhone・強い YES）**
- 初回ビルド一発成功（`cap sync` が SPM プラグインを自動配線・`platform: ios / Media: 検出 ✅`）。
- **300枚**: 9037ms / 日時 300/300 / GPS 212/300 / 範囲 2026/5/28〜6/26。
- **2000枚**: **5954ms** / 日時 **2000/2000** / GPS **215**/2000 / 範囲 **2021/8/15〜2026/6/26（約5年）**。サムネ OS から描画 OK。
- **(a) 全ライブラリ到達=YES**（ピッカー無しで 2000枚・約5年分） **(b) サムネ=OK** **(c) 日時=100%・GPS=取得 OK** **(d) 許可「すべての写真」で全件**。
- **速度の解釈（重要）**: 300枚が9秒で2000枚が6秒と逆転したのは、**1回目の getMedias が許可ダイアログを出し、計測開始後のタップ待ちが 9037ms に混入**したから。2回目は許可済み＝**2000枚6秒（≒3ms/枚）が真の速度**。「全サムネ base64 一括」という最重量方式でこの速度＝十分速い。
- **GPS は直近リッチ・古いほど希薄**（直近300は71%／2000全体は約11%）→ 地図は近年で濃く、古い写真は**日時軸（100%）が普遍の頼り**。On This Day・GPSなし写真の軌跡補完（v74-75）の設計と整合。

**教訓**
- ボトルネック①（写真全件アクセス）と②（Mac なし署名・v91）が両方消え、**native 化の二大リスクが解消**。最小スパイク（A）の趣旨どおり、最少手数で最大の未知を実機で潰せた。
- 計測に**許可ダイアログ待ちが混入し得る**点に注意（初回コールの時間は割り引いて読む）。

**残課題 / 次の方向**
- capability 確定 → **本命 B（自前 Photos プラグイン: `PHAsset.fetchAssets` で全件メタデータ即時列挙＋`PHImageManager` でオンデマンドサムネ＋拡大時に原寸）** に進む。理由＝community プラグインは全サムネを base64 一括返却＝数千〜数万枚をメモリ保持できない。Mac なしでは pbxproj 手編集を避け**ローカル Capacitor プラグイン package（file: 参照→cap sync が SPM 配線）**が堅い。memory [[native-photo-access-works]]。
- 診断 block（index.html 末尾 #spk-*）は B 実装時に撤去。

## v91 — Codemagic 署名突破＋初 TestFlight 到達（実機 iPhone で起動・取り込み高速の初期signal） (2026-06-26)

**背景**
- v90 の宿題＝archive が `error: "App" requires a provisioning profile`（exit 65）で詰まっていた。失敗ビルドのログを Chrome 拡張で一緒に読んで原因を確定し、初 TestFlight・実機起動まで通した回。

**ハマり → 真因 → 対処（核心）**
- **症状**: 署名ステップで `Cannot save Signing Certificates without certificate private key` / `Did not find any certificates` / `Did not find matching provisioning profiles` → 証明書が1枚も用意できず、archive が `CODE_SIGN_STYLE=Manual` で必須プロファイル無しのまま exit 65。
- **真因**: v90 の診断（use-profiles の適用ミス）は表層。**本質は「`fetch-signing-files --create` に証明書の秘密鍵を渡していなかった」**。Mac なし CI で証明書を作る時は、永続的な秘密鍵を環境変数で渡す必要がある（無いと作った証明書の private key が保存できない）。
- **対処（commit `21d81e3` / v91）**:
  - ローカルで RSA 秘密鍵生成（`ssh-keygen -t rsa -b 2048 -m PEM ...`）。
  - Codemagic に **Secure 環境変数 `CERTIFICATE_PRIVATE_KEY`（グループ `signing`）** として登録（public リポなので yaml には置けない＝UI 登録／秘密情報の入力はユーザー操作）。
  - `codemagic.yaml`: `environment.groups: [signing]` 追加 ＋ `fetch-signing-files` に `--certificate-key @env:CERTIFICATE_PRIVATE_KEY` 追加。
  - → 再ビルドで **署名 3s ✅ / IPA ビルド 45s ✅ / App.ipa 1.40MB 生成 ✅ / App Store Connect へアップロード＆Apple 処理完了 ✅**。

**もう1つの小さな関門（解決済み）**
- post-processing「App Store distribution」が赤 = バイナリは TestFlight に乗ったが、**外部テスト提出に必要な Test Information 未入力**で submit だけ失敗。内部テストはレビュー不要なので無関係。
- App Store Connect で **暗号化コンプライアンス**（標準暗号化=HTTPS のみ→免除／フランス配信=いいえ）を回答 → ビルド 1.0(3)「提出準備完了」→ **内部テストグループ「自分」に自分を追加 → iPhone の TestFlight でインストール・起動成功**。

**結果 / 観察（強い signal）**
- **Mac を一台も持たずに署名済み iOS アプリを実機（iPhone）で起動**＝このプロジェクト最大級のリスク「Mac なし署名」を実機で de-risk 完了（ボトルネック②クリア）。
- **取り込みが web/Safari より明確に速い**：実機で **200枚をピック→アプリ画面復帰まで約17秒**。WKWebView（native ラッパー）が モバイル Safari より速いため。※これはまだ **web の `<input>` ピッカー**での結果＝native の「全ライブラリ一括アクセス」プラグインは未着手（次の de-risk）。

**教訓**
- Codemagic（Mac なし自動署名）の本当の必須要素は **`CERTIFICATE_PRIVATE_KEY`（永続秘密鍵）を Secure env で渡すこと**。これが無いと「証明書が作れない→プロファイルも当たらない→archive が manual で必須プロファイル無し」で落ちる。**エラーは下流（profile 無い）に出るが根は上流（証明書の鍵）**。ログのステップ名（署名適用 vs build-ipa）で切り分けると速い。
- 「web 体験ロジックをそのまま native でラップ」戦略は性能面でも追い風（取り込みが速くなった）。整理（写真全件 native 化・SQLite）は段階的でよい。

**残課題 / 次の方向**
- 🔴 **ネイティブ写真全件アクセスの最小スパイク**（PHAsset 全件列挙＋OS サムネ/EXIF＝「久しぶり=全ライブラリ」の生命線・最優先 de-risk）。
- 実機で web 体験（連想ウォーク・地図・取り込み）が web 版と同じ手触りか継続観察。
- 任意の後始末: `Info.plist` に `ITSAppUsesNonExemptEncryption=false`（暗号化質問を恒久スキップ）／外部テストするなら Test Information 入力／毎ビルドの external 提出失敗が気になるなら yaml 調整。

## v90 — Codemagic 接続＋初TestFlightビルド実行（署名は通過・archive で詰まり中） (2026-06-26)

**背景**
- v89 のリポ足場が出来たので、Chrome 拡張で画面を一緒に操作しながら Apple/Codemagic 側を一気に接続し、初ビルドを TestFlight まで回そうとした回。

**やったこと（Apple / Codemagic 側・全てユーザー操作を Claude が誘導）**
- **Apple Developer**: App ID `io.github.yutsutke.madeleine`（Explicit）登録。
- **App Store Connect**: アプリレコード作成。名前は「あの日」単独が商標で使用済みだったため **「あの日 — 写真と足跡」**に（公開前に再変更可）。primary 日本語 / SKU `madeleine-001`。
- **App Store Connect API キー**（Users and Access→Integrations）: 名前 `Codemagic CI`・権限 **App Manager**。Issuer ID `cc160ccb-f80f-4f15-acdd-3d6b6b333c96` / Key ID `FNMWA45D94` / .p8 はユーザーが DL 保管（1回限り）。⚠️ API キー/.p8 の入力は Claude は代行せず、値を提示してユーザーが入力（認証情報入力の安全方針）。
- **Codemagic**: Personal Account（無料 Individual）・GitHub OAuth(All repos) でサインアップ→リポ `photo-memory-spike` 追加（app id `6a3e20ec4f57d6c27a45f181`）→ Developer Portal integration を **「MadeleineASC」**で登録（yaml の `integrations.app_store_connect` と一致）。

**ハマったところ（2件・1件目は解決・2件目が次セッションの宿題）**
- **① 初ビルド「No matching profiles found」**: codemagic.yaml の `environment.ios_signing` ブロックによる自動署名は「既存プロファイルを探すだけ」で、新規アカウントは証明書もプロファイルも無く失敗。→ **`ios_signing` ブロックを撤去し、明示スクリプト `keychain initialize` → `app-store-connect fetch-signing-files "$BUNDLE_ID" --type IOS_APP_STORE --create` → `keychain add-certificates` → `xcode-project use-profiles` に切替**（commit `fe1a572`）。
- **② 2回目ビルド: 署名ファイル作成は通過したが Step 6「IPA をビルド」で失敗（未解決）**: `error: "App" requires a provisioning profile. Select a provisioning profile in the Signing & Capabilities editor. (in target 'App')`。archive 行に `CODE_SIGN_STYLE=Manual` はあるが PROVISIONING_PROFILE_SPECIFIER / DEVELOPMENT_TEAM が未注入。exit 65。＝`use-profiles` がプロファイルを App ターゲットへ適用できていない。

**次の方向（次セッション最優先）**
- まず**失敗ビルドの署名ステップのログ**を読む（fetch-signing-files が profile を何個どこに保存し use-profiles が何個適用したか）。
- 修正候補（順に）: ①`fetch-signing-files` に `--platform IOS` 追加 ②`use-profiles` と `build-ipa` を同一スクリプトステップにまとめる ③プロジェクトに `DEVELOPMENT_TEAM = 25TM5C27YT` を明示注入 ④最終手段＝Codemagic UI「Code signing identities」へ手動で配布証明書＋プロファイルをアップロードする方式に切替。
- 完全独立で進められる de-risk＝**ネイティブ写真全件アクセスの最小スパイク**（[[product-core-defined]] 核②の生命線）。署名で長引くならこちらを先行してよい。

**教訓**
- 新規 Apple アカウント＋Codemagic の初回は「自動署名ブロックだけ」では証明書/プロファイルが作られず詰まる。`fetch-signing-files --create` で明示生成が要る。だが --create で作っても `use-profiles` の適用まで通って初めて archive が通る＝**署名は『作成』と『プロジェクトへの適用』の2段で、それぞれ別に失敗しうる**。1ビルドごとに失敗ステップが1つずつ前進するので、ログのステップ名で切り分けるのが速い。
- 認証情報（.p8/API キー）の入力はユーザーに委ね、Claude は値の提示と画面誘導に徹する運用がうまく回った。

## v89 — native: iOS プラットフォーム生成＋Codemagic で TestFlight パイプライン足場 (2026-06-26)

**背景**
- Apple のメンバーシップ有効化（6/25）＋ App Store Connect 初期設定完了（6/26・有料契約/税務/口座/ASBP 申請送信）でブロッカーが消えた。本命の前進＝**「整理の順番①＝今のコードで TestFlight まで通す」**（パイプライン de-risk＝Mac なし署名の最大リスクを早期に潰す）に着手。ASBP 承認待ちとは無関係に進められる。

**設計判断**
- **`npx cap add ios` で iOS プラットフォームを生成しコミット**（`ios/` を追跡。生成物＝`public/`・`capacitor.config.json`・`config.xml`・`capacitor-cordova-ios-plugins` は gitignore 済み、CI の `cap sync` で再生成）。Info.plist 等の手編集を永続化するため platform はリポに持つ方針（毎ビルド再生成しない）。
- **Capacitor 8 = Swift Package Manager (SPM)**。Podfile が無い → **CI に `pod install` 不要**。`xcodebuild` がビルド時に SPM を解決する。TODO のボトルネック②（Mac なし署名）が一段軽くなった。
- **Info.plist 用途文言（日本語）**: `NSPhotoLibraryUsageDescription`（写真＝コア「久しぶり＝全ライブラリ」）／`NSPhotoLibraryAddUsageDescription`（取り込み画像の任意保存）／`NSLocationWhenInUseUsageDescription`（位置ロガー）。いずれも「端末内だけで処理・外部送信しない」を明記（プライバシーポリシーと整合）。**位置は When-In-Use のみ**（背景常時＝Always は審査リスク・post-v1＝[[location-logger]]）。
- **共有スキーム `App.xcscheme` を手作成しコミット**（CI の `xcodebuild -scheme App` が確実に検出できるよう。BlueprintIdentifier＝native target UUID `504EC3031FED79650016851F`）。
- **`VERSIONING_SYSTEM = "apple-generic"` を App ターゲットに追加** → CI で `agvtool new-version -all "$BUILD_NUMBER"`（Codemagic 連番）でビルド番号を自動採番できる。
- **`codemagic.yaml`**: `mac_mini_m2`／`xcode: latest`／`node: 22`。流れ＝`npm install` → `npm run sync:web && npx cap sync ios` → `xcode-project use-profiles`（自動署名）→ ビルド番号採番 → `xcode-project build-ipa` → `publishing.app_store_connect`（`auth: integration`・`submit_to_testflight: true`）。署名は App Store Connect API キー（Codemagic の Team integration に登録した名前を `integrations.app_store_connect` に書く＝既定プレースホルダ `MadeleineASC`）。

**ハマったところ**
- Capacitor 8 が CocoaPods でなく SPM だと最初は気付かず pod install 前提で考えていた → `cap add ios` 後に Podfile 不在＋`Package.swift` 生成を確認して CI を SPM 前提に修正（`pod install` ステップを削除）。次の Claude は「Capacitor 8 = SPM・Podfile 無し」を前提に。

**結果 / 観察**
- Windows で `cap add ios` / `cap sync ios` が成功（pod install はスキップ＝CI 不要）。`git add -n` で生成物が正しく除外され platform 本体＋スキームのみ追跡されることを確認。**xcodebuild 実ビルドは Codemagic（Mac）でのみ可能＝CI 接続後に初検証**（Windows では検証不能）。

**教訓**
- Mac 無し署名の de-risk は「足場を全部コミットしてから CI を一度回す」のが最短。ローカル（Windows）で潰せるのは構成の正しさ（ファイル追跡・YAML 構文・SPM 構成）まで。署名・SPM 解決・アーカイブの真の検証は最初の1ビルドでしか分からない＝だからこそ早く回す。

**残課題 / 次の方向**
- **ユーザー操作（CI 接続）**: ① App Store Connect で API キー発行（Issuer ID / Key ID / .p8）② Codemagic にサインアップ＆リポ接続＆API キーを integration 登録（名前を `codemagic.yaml` の `MadeleineASC` に合わせる or yaml を書き換え）③ App レコード作成（bundle `io.github.yutsutke.madeleine`）④ 初ビルド実行 → TestFlight。
- 初ビルドで詰まりやすい点: スキーム検出・SPM 解決・自動署名（Bundle ID 登録漏れ）・`agvtool`（apple-generic 入れたので可）。ログで切り分け。
- 🔴 並行 de-risk＝**ネイティブ写真全件アクセスの最小スパイク**（PHAsset 全件・`@capacitor-community/media` 等）はパイプラインと独立に進められる。

## v88 — 実機フィードバック: タイムライン下端ハンドルを safe-area 分持ち上げ＋ⓘアイコン説明 (2026-06-22)

**背景**
- v87 を実機（iPhone Safari）で確認したユーザーから2点: ①「**タイムラインが画面から消えた・下すぎて触れない**」②「**🎲・📆 のマークは ⓘ ボタンで説明がないと分からない**かな?」（アイコンのみ化＝v85 [[ui-minimalism-works]] の副作用）。

**設計判断**
- **① 下端ハンドル**: 原因＝`<meta viewport>` に `viewport-fit=cover` が無く `env(safe-area-inset-bottom)` が常に 0 → ボトムシートの折りたたみハンドル（下端 46px）が iOS のホームインジケータ帯／下部バーと重なって掴めない。対処＝viewport に `viewport-fit=cover` を追加し、折りたたみ transform を `translateY(calc(100% - 54px - env(safe-area-inset-bottom)))` に（ハンドルを safe-area 分持ち上げる）。ハンドルも 46→54px・グリップを太く明るく（#555→#888）して見つけやすく・本文は `padding-bottom: calc(16px + env(safe-area-inset-bottom))` で最下段が隠れないように。下端固定の他要素は `.full-cap` が既に safe-area 対応・`.log` はデバッグ用なので cover 化の副作用なし。
- **② アイコン説明**: 既定はアイコンのみ（v85）を維持し、説明は **ⓘ ボタン＝押した時だけ開く凡例**に逃がす（[[ui-minimalism-works]] の「追加は使う時だけ広がる」）。凡例＝📆今日／◀▶前後の日／🎲偶然の3日／↩︎全部／📅期間／⋯表示。コントロール並び＝`🎲 ◀ 📆 ▶ ↩︎ ⓘ`（ⓘ は常時表示）。

**結果 / 観察**
- preview green（DOM 検証）: ⓘ→凡例6行・排他クローズ／ハンドル高54・開閉トグル／v87 ◀▶ ラベル回帰なし／コンソールエラー0。**safe-area の持ち上げは実 iOS でしか見た目が出ない**（ヘッドレスは env=0）→ 実機で「ハンドルが掴める位置に出るか」を要確認。

**教訓**
- iOS の下端固定 UI は `viewport-fit=cover` ＋ `env(safe-area-inset-bottom)` をセットで入れないと、ホームインジケータ帯に食われて「消えた・触れない」になる。cover 化は下端固定の全要素の safe-area 対応とセットで（今回は影響要素が少なく安全）。
- v85 のアイコンのみ化は正解だが「初見の意味不明」コストは残る → 既定を汚さず ⓘ で開く凡例が落とし所。

**残課題 / 次の方向**
- 実機で ①ハンドルが掴める位置か ②ⓘ 凡例が役に立つか を確認。だめなら「モバイルは 📆 入口でシートを少し開いた状態で出す」案も。

---

## v87 — 地図 📆「過去の今日」を ◀▶ で前後の日にずらす（過去の昨日／明日／明後日…） (2026-06-22)

**背景**
- v83 で 🗺=「過去の今日（On This Day・数年分の同じ月日を重ねる）」を毎日の入口にした（[[on-this-day-daily-entry]]）。ユーザー実機「過去の昨日・過去の明日・過去の明後日も選べると、**予定を立てるときや振り返るとき**に便利」。今日だけでなく前後の日にも同じ「数年分の重なり」を広げたい＝当初から拡張候補に挙げていた「今日±数日に広げる」の実装。

**設計判断**
- 固定チップ（昨日/今日/明日/明後日）でなく **◀▶ の矢印で何日でも送れる**形に（ユーザーの「等」＝任意レンジ）。最小操作で無限に拡張でき、[[ui-minimalism-works]] とも整合。
- 既存の `pickToday()` を `pickToday(offset)` に一般化（offset 日だけ起点をずらし、その月日に一致する年を全部）。状態は `mapState.todayOffset`（0=今日／-1=昨日／+1=明日…）。モードは `'today'` のまま（ラベルだけ相対表示）。
- コントロール並びは **`🎲 ◀ 📆 ▶ ↩︎`**。◀▶ は `'today'` モードのときだけ表示。📆 は offset=0 リセット（今日に戻る）。タイムライン見出しが「📆 明日 6/23（N年分）」のように相対追従（`relDayLabel`：今日/昨日/明日/一昨日/明後日/N日前/N日後）。
- **写真ゼロの日に送ったら「0年分」を正直に表示**（空でも `setPick` する）＝「この月日は歴史的に何も無い」が分かる＝予定立て・振り返りに有用。`applyFilter` は `fg.length` で fitBounds をガード済みなので空でも安全（地図はその場に留まる）。

**結果 / 観察**
- preview E2E green（DOM 検証）: today 起点で `🎲 ◀ 📆 ▶ ↩︎` 全表示／▶→「明日6/23」▶→「明後日6/24（0年分・クラッシュなし）」◀×3→「昨日6/21」📆→「今日6/22」とラベル相対追従／`pickToday(-1/0/+1/+2)` の年数が seed と一致／day・random3・ブラウズでは ◀▶ 非表示／コンソールエラー0。
- 実機手触りは未確認（次セッション）。

**教訓**
- 「今日」を起点に前後へずらす導線は、固定選択肢でなく**矢印1対＋相対ラベル**が最小かつ拡張的（昨日も来週も同じ操作で届く）。On This Day の習慣（②久しぶり）が「予定立て（前へ）」と「振り返り（後ろへ）」の両用途に自然に広がった。

---

## v86 — 地図のベースタイル選択を記憶（前回選んだ地図で開く） (2026-06-19)

**背景**
- 地図はいつもダーク (CARTO dark) で開いていた。ユーザー「以前選んだ地図が出るといい。地理院(淡色)を選んだらそれで表示されるように」。毎日開く入口 ([[on-this-day-daily-entry]]) なので、毎回タイルを選び直すのは習慣の摩擦。

**設計判断**
- ベースレイヤを `BASE_LAYERS`（key/name/layer）に整理し、`map.on('baselayerchange')` で選択 key を `localStorage['pms-mapLayer']` に保存。次回 `openMapView` 時にその key のレイヤを初期表示に使う（未保存・不正値は既定=ダークにフォールバック）。
- key を保存（表示名でなく）＝ラベル文言を変えても壊れない。localStorage は try/catch で囲み、無効環境でも地図は開く（保存だけ諦める）。

**結果 / 観察**
- preview E2E green: 保存なし→ダーク起動 / 地理院(淡色)選択→`pms-mapLayer='gsiPale'` 保存・チェック移動 / 閉じて再オープン→地理院(淡色)で開く / コンソールエラー0。

**教訓**
- 毎日使う入口は「前回の状態で開く」が効く（選び直しは習慣の摩擦）。表示名でなく安定 key を保存するのは i18n・ラベル変更に強い小さな保険。

---

## v85 — 地図の入口操作 🎲/📆/↩︎ をアイコンのみに簡素化 (2026-06-19)

**背景**
- v83 で地図入口に「🎲 偶然の3日 / 📆 今日 / ↩︎ 全部を見る」の3ボタンを文字付きで並べたが、ユーザー「情報が増えすぎ。**アイコンにして並べるだけ**がいい」。[[ui-minimalism-works]]（余計を削ると想起されやすい）の地図版。

**設計判断**
- 3ボタンを**アイコンのみ**に（🎲 / 📆 / ↩︎）。説明は `title` 属性に退避（desktop ホバー / a11y 用）。`.mc-icon` 修飾で少し大きめ・コンパクトな正方形寄りに。
- 🎲 はモードで意味が変わる（random3=もう一度引く / それ以外=偶然の3日）が、**アイコンは据え置き・`title` だけ切替**（文字を出さない）。
- 「何を表示中か」の説明は**タイムライン上部のフィルタ見出し**（「📆 今日 M/D（N年分）」「🎲 ランダムな3日」）に既に出ているので、コントロール行から重複テキストを削っても情報は失われない。

**結果 / 観察**
- preview E2E green: 3つが `mc-icon` でアイコンのみ表示・title 正しい・🎲 クリックで random3 化し title が「もう一度引く」に切替（アイコンは🎲固定）・コンソールエラー0。実機の見た目バランスは未確認。

**教訓**
- 機能を足した直後はラベルで意味を補強しがちだが、**入口が増えたら文字を削ってアイコン列に**。意味の説明は「いま何を見ているか」を示す別の場所（タイムライン見出し）に一本化すると、操作行は静かになる。

---

## v84 — トップ画面に「過去の今日」フィルタチップを追加 (2026-06-19)

**背景**
- v83 の地図「🗺＝今日」が刺さった（[[on-this-day-daily-entry]]・実機「線が無くてもタイムラインに数年分出るだけで心が動く」）→ ユーザー「**トップ画面のチップでも**、この時季や3ヶ月前に加えて、**過去の今日みたいなチップ**が欲しい」。ランダム引きの段階から On This Day で絞れると、地図を開かなくても毎日「過去の今日」に触れられる。

**設計判断**
- `FILTERS` に `{ key:'today', label:'過去の今日' }` を**全期間の次（この時季より前）**に追加。チップ描画・母数脚注・空メッセージは FILTERS を回す既存の汎用処理に全部乗るので**配列1行＋`pickPool` 1分岐**で完結。
- `pickPool('today')` ＝今日と**ちょうど同じ月日**（`getMonth`/`getDate`、年問わず）。既存の `'season'`「この時季」(±15日) より**精密な記念日プール**で、地図の 🗺＝today と同じ抽出条件。両者は対（チップ＝引く / 🗺＝重ねて地図化）。
- 並び順は 全期間 → 過去の今日 → この時季 → 3ヶ月前… ＝精密な記念日を先頭付近に置き、毎日の入口として目に入りやすく。

**結果 / 観察**
- preview E2E green: チップ並び正しい・選択状態・母数脚注（過去の今日=該当日のみ/全期間=全件）・`pickPool('today')` は ±1日を含まず exact 月日のみ（season は ±1日も含む）・コンソールエラー0。**実機の手触り（チップから過去の今日を引く習慣）は未確認**。

**教訓**
- 刺さった体験（地図の On This Day）は**別の入口（トップのチップ）にも横展開**すると習慣の接触点が増える。既存の `FILTERS`/`pickPool` 抽象が素直で、新軸が1行で足せた（[[on-this-day-daily-entry]] の `pickDays` と同じく、モデルが拡張に強い）。

**残課題 / 次の方向**
- 実機で「過去の今日」チップを引く感触（地図の today と使い分けるか・どちらを主に使うか）。将来案: 「過去の今日」で引いた1枚から 🗺 today へ自然に繋ぐ／写真が少ない日の体験。

---

## v83 — 🗺＝今日（On This Day・数年分の同じ月日を重ねる）を毎日の入口に (2026-06-19)

**背景**
- ユーザー要望「地図ボタンを押したら、今日の月日で数年分を重ねて表示。**毎日習慣的に使うこと**が目的」。例＝今日 2026/06/19 なら 2026/6/19・2025/6/19・2023/6/19…（写真がある年だけ）が一度に出る。
- これは核の②**久しぶり**＋習慣ループ（毎日ひらく口実）。①**偶然**を体現する従来の「🗺＝ランダム3日」とどちらを既定にするか確認 → ユーザー判断「**🗺＝今日に。偶然3日は🎲で残す**」。

**設計判断**
- **新モード `'today'`**（既存 `pickDays`/`pickMode` モデルに合流）。`pickToday()` ＝今日と同じ月日(`getMonth`/`getDate`)の写真がある年の dayKey を全部・**年昇順（昔→今）**。位置の有無は問わない（GPS 有＝軌跡 / 無＝タイムラインに出る・v74 で時刻位置に推定配置）。
- **ヘッダ 🗺（focusPhoto なし）の既定を 'today' に**。今日の写真が無ければ `pickRandom3()` → それも無ければ従来ブラウズ、と二段フォールバック（毎日必ず何か出る）。
- **偶然3日は地図内に退避**＝コントロールバーを入口モードで「🎲 偶然の3日 / 📆 今日 / ↩︎ 全部を見る」の3つに。`'today'` も入口モード扱いで 📅期間/⋯表示 を隠す。🎲 は random3 中だけ「もう一度引く」、それ以外は「偶然の3日」。📆今日 は今日の写真がある時だけ表示（`hasTodayPhotos`）。→ 🗺で今日 → 🎲で偶然へ → 📆で今日へ、を地図を閉じずに往復可能。
- タイムラインのフィルタ見出しに `'today'` ラベル「📆 今日 M/D（N年分）」。既存の選択モード表示（v72）に乗せただけ。

**結果 / 観察**
- preview E2E green: 複数年6/19（GPS有/無混在）で 🗺→`today`・pickDays 昔→今・別日(6/20)は除外・コントロール3つ・ラベル「📆 今日 6/19（3年分）」/ 🎲↔📆今日でモード&ラベル切替 / 今日ゼロ→random3 フォールバック&📆今日非表示 / コンソールエラー0。**実機の習慣手触り（毎朝ひらいて昔の今日がよみがえるか・数年が重なって見える快感）は未確認**。

**教訓**
- 「偶然(①)」と「久しぶり(②)」は別々の入口に値する核。片方を潰さず **既定＝習慣(久しぶり)／一手で偶然へ** が、毎日使う動機（②）と発見の喜び（①）を両立。既存の `pickDays` 抽象が `'today'` をほぼ無改造で受けた＝モデルが素直だった。

**残課題 / 次の方向**
- 実機で①毎朝の習慣として開きたくなるか②数年分が色（昔→今）で重なって読めるか③1〜2年分しか無い日の寂しさ（最初は写真が少ない）をどう橋渡しするか。将来案: 「今日±数日」に広げる/通知（opt-in の偶然通知と接続）/「N年前の今日」見出し。

---

## v82 — 注意書きに「タイムラインで並びを見る／位置を直す＝想起」を追記 (2026-06-19)

**背景**
- v81 のヒント「その日の写真一覧が開きます」だけでは**次に何をすればいいか**が伝わらない、とユーザー。狙いは「タイムラインを確認してください」の一言＋**なぜ確認するとよいか**＝タイムラインの並びを眺める／そこから写真を選んで位置を直すことが**想起につながる**（[[editing-triggers-reminiscence]] と同型＝編集・能動的関与がコア「よみがえる」の表面）。

**設計判断**
- ヒント文を「位置のある写真が無い日でも、その日の写真一覧（**タイムライン**）が開きます。**並びを眺めたり、写真を選んで位置を直すのも想起のきっかけに。**」に拡張。中心カード／フル画像とも同文。
- 単なる動作説明（何が出るか）から、**想起への動線案内**（何をするとよいか・なぜ）へ。注意書きを「作業の補足」でなくコア体験への入口として書く。GPS あり「この場所を見る」には引き続き出さない。

**結果 / 観察**
- preview E2E green: no-GPS で新文言表示・GPS あり非表示・コンソールエラー0。実機での「タイムラインから位置を直す→想起」の流れは未確認。

**教訓**
- spike の注意書きは「状態の説明」で止めず「次の一手＝想起のきっかけ」まで書くと、実用導線がコアを強化する（位置のない写真→その日の地図→タイムライン→位置を直す＝よみがえる、という設計された流れ）。

---

## v81 — 「この日の地図へ」に飛び先の注意書きを添える (2026-06-19)

**背景**
- v80 で位置情報なし写真にも「🗺 この日の地図へ」を常時表示にしたが、その日に GPS 写真が無いと飛び先の地図にマーカー/軌跡が出ない。ユーザー要望「ボタンのすぐそばに『軌跡が無くてもその日の写真一覧（索引）が出る』趣旨の注意書きを」。

**設計判断**
- 「🗺 この日の地図へ」（＝位置情報なしの時だけ）の**直下に小さなヒント**「位置のある写真が無い日でも、その日の写真一覧が開きます」を表示。中心カード（`.center-map-hint` 新設・11px グレー中央）／フル画像（既存 `.de-hint` スタイル流用）の両方。
- **GPS あり（「この場所を見る」）には出さない**（実際の場所へ飛ぶので注意書き不要）。[[ui-minimalism-works]] に照らし、出すのは「飛び先が想定と違いうる」no-GPS の時だけに限定。

**結果 / 観察**
- preview E2E green: no-GPS で中心カード/フル画像ともボタン直下にヒント表示・GPS ありは非表示・スタイル 11px/グレー/中央・コンソールエラー0。実機の体感は未確認。

---

## v80 — 「この日の地図へ」を位置情報なし写真に常時表示 (2026-06-19)

**背景**
- v79 直後、実機（まち）でユーザーが「🗺 この日の地図へが見つからない」。原因＝v79 は「同じ日に GPS 写真が1枚でもある時だけ」導線を出す仕様（`dayHasGps` ゲート）で、対象写真（2021/02/07・位置情報なし）の**その日が丸ごと GPS 皆無**だったため非表示だった。ユーザー判断＝「その日に GPS 写真が無くても**常に出す**」（要望の「位置情報がなくても日時で飛ぶ」を文字どおり優先）。

**設計判断**
- **`dayHasGps` ゲートを撤去**。導線の条件を「GPS あり、または **日時がある**」に変更（中心カード／フル画像とも）。実質、日時を持つ全写真に地図導線が出る（GPS あり＝「🗺 地図でこの場所を見る」／なし＝「🗺 この日の地図へ」）。
- **空の日でも安全**: その日に GPS が皆無なら飛び先の地図にマーカー/軌跡は出ないが、`openMapView` は `setPick([dayKeyOf],'day')` でその日に絞るだけ・マーカー寄せは `if(rec)` ガードで何もしない → エラーなし（preview 確認）。地図 overlay のタイムラインはその日の写真を出すので「空白の地図」ではなく「その日の索引」にはなる。
- **日時不明（`datetime` null）の写真は対象外**（`!!photo.datetime` でガード）。`dayKeyOf` が null で落ちるのを防ぎ、そもそも「その日」が無いので導線も無意味。
- 「空の地図を見せない」より「**要望どおり予測可能に常に出す**」を優先（v79 の clever なゲートはユーザーには「消えた」と映った＝予測可能性の負け）。

**結果 / 観察**
- preview E2E green: GPSなし&その日GPS皆無の写真C→中心カード/フル画像とも「🗺 この日の地図へ」表示・クリックで `pickDays:['2021-1-7']/mode:'day'` に絞れて開く・コンソールエラー0。GPSあり→「この場所を見る」。日時 null→ボタン非表示。
- 実機での想起の手触りは**未確認**（GPS 皆無の日の地図が「その日の索引」として役立つか・空マーカーが拍子抜けにならないか）。

**教訓**
- spike の機能追加で「賢い非表示ゲート」は、ユーザーが**明示要求した導線を黙って消す**と「壊れた/見つからない」と受け取られる。要望が明確なら**予測可能（常に出す）を優先**し、空ケースは飛び先側で穏当に degrade させる方が体感がよい。

**残課題 / 次の方向**
- 将来案: GPS 皆無の日に飛んだ時、地図を世界デフォルトでなく「前後で一番近い GPS 写真の日」へ寄せて文脈を出す案（ただしスコープ注意）。飛び先で当該写真自身をハイライト／「位置を設定」への橋渡し（[[editing-triggers-reminiscence]]）。

---

## v79 — 写真ページから「位置情報なしでも日時でその日の地図へ」 (2026-06-19)

**背景**
- まちで実機を触りながらの機能充実。ユーザー要望「個々のページから、位置情報がなくても**日時情報でその日の地図に飛ぶ**ように」。従来は地図への導線（中心カード「🗺 地図でこの場所を見る」）が**GPS のある写真にしか出なかった**。GPS の無い写真（寺の写真など iPhone でも位置が欠ける）が地図体験から切り離されていた。

**設計判断**
- **既存ロジックがほぼそのまま使えた**: `openMapView(focusPhoto)` は元から `setPick([dayKeyOf(focusPhoto)], 'day')` で「その写真の日」に絞る実装（GPS 有無に依らない）。マーカー popup を開く処理も `if (rec)` ガードで、GPS 無し＝マーカー無しなら静かにスキップ。つまり**飛び先の仕組みは既にあり、足りなかったのは「入口（ボタン）を GPS 無しでも出す」だけ**だった。
- **ボタンを出す条件 = `dayHasGps(p)`**（新ヘルパー）: その写真自身に GPS が無くても、**同じ暦日に GPS 写真が1枚でもあれば**導線を出す。理由＝飛んだ先で空っぽの世界地図を見せても無意味。軌跡（その日の GPS）がある時だけ意味がある。さらに **v74 でこの GPS 無し写真自身が軌跡上の時刻位置に推定配置される**ので、「日付だけは分かる1枚」が当時の足跡の上に浮かぶ＝コア（よみがえる）に直結。
- **ラベルで状態を伝える**: GPS あり＝「🗺 地図でこの場所を見る」（その場所へ）／GPS 無し＝「🗺 この日の地図へ」（その日の軌跡へ）。同じ `openMapView(p)` を呼ぶが、ユーザーには「場所が分かる/分からない」が文言で伝わる。
- **入口は2か所**: ①連想ウォークの中心カード ②フル画像ビュー（長押し）の `.full-actions`。フル画像は z-index 2000 で地図 overlay（150）より前面なので、🗺 を押したら**先に `closeFullImage()` してから `openMapView(p)`**。
- GPS 無し & その日に GPS が1枚も無い写真は**ボタンを出さない**（飛んでも何も無い）＝[[ui-minimalism-works]]「使う時だけ広がる」。

**結果 / 観察**
- preview で合成データ E2E green: A=GPS無し(同日にGPSあり)→「🗺 この日の地図へ」表示・クリックで地図が `pickDays:['2026-3-18'] / mode:'day'` に絞れて開きフル画像は閉じる ／ B=GPSあり→「🗺 地図でこの場所を見る」 ／ C=GPS無し(同日もGPS無し)→ボタン非表示。`dayHasGps` A=true/B=true/C=false。コンソールエラー0。
- 実機の手触り（その日の地図に飛んで「この写真は当時ここにいた頃の1枚だ」と想起が起きるか・v74 の推定配置が一緒に見えるか）は**実機未確認**。

**教訓**
- 「位置が無い写真」を地図から締め出さない＝**日時は GPS が無くても残る最後の手がかり**。色/意味と同じく「日時だけでも記憶の扉が開く」（[[external-import-savedate-works]]）の地図版。編集（位置/日付）に続き、**閲覧導線でも「GPS 無し写真を一級市民に」**が効く流れ。
- 機能追加の前に「飛び先の仕組みは既にあるか」を確認したら、実装が**入口1個＋ヘルパー1個**で済んだ。spike の体験ロジックが資産として効いている好例。

**残課題 / 次の方向**
- 実機で①その日の地図に飛んだ時の想起②GPS 無し写真が v74 で軌跡上に出るか（単一 GPS 点しか無い日は補間できず出ない＝想定）③ラベル「この日の地図へ」が意図通り伝わるか。
- 将来案: 飛んだ先でその写真自身をハイライト（推定配置のピンに寄せる）／「位置を設定」への自然な橋渡し（地図で当時の軌跡を見ながら位置を直す＝[[editing-triggers-reminiscence]]）。

---

## v78 — Capacitor 足場（Phase 1 着手・native プロジェクト化の最初の一歩） (2026-06-18)

**背景**
- native GO の Phase 1。Apple 承認待ちと並行で、このリポを Capacitor でビルド可能な土台にする。「いきなり綺麗にしない」方針（CLAUDE/TODO）に従い、既存 index.html を webview にそのまま乗せる**足場だけ**先に作る（写真全件/SQLite 置換はまだ）。

**設計判断**
- **GitHub Pages を壊さない構造**: web 本体（`index.html` / `vendor/` / `privacy.html`）は**ルートのまま**（Pages URL 維持＝実機 web 確認の運用を死守）。Capacitor の `webDir` は **`www/`**（生成物・gitignore）にし、`scripts/sync-web.mjs` でルート→www へコピーしてから `cap sync/copy`。www を別フォルダにした理由＝webDir=ルートだと node_modules / ios / docs まで native バンドルに混ざるため。
- **appId = `io.github.yutsutke.madeleine`**（英語ブランド Madeleine ＋ GitHub 由来の逆ドメイン・parked madeleine と継続）。**App Store 初回申請まで変更可**。
- **appName = `Madeleine`**（Xcode プロジェクト名は ASCII が安全。日本語表示名「あの日」は Info.plist ローカライズで後付け＝Phase 1 i18n）。
- **`cap add ios` は今やらない**: CocoaPods（pod install）が Mac 必須で Windows では半端になる → ios/ 生成は Codemagic/Mac（最初の1ビルド時）に回す。「整理の順番」①最初の1ビルドを TestFlight に合流。
- Capacitor **8.4.0**（core / ios / cli）。`backgroundColor=#111111` で起動時の白フラッシュ回避（アプリのダーク基調に合わせる）。

**結果 / 観察**
- preview/CLI 検証: `npm run sync:web` で www/ に index.html+privacy.html+vendor 一式が生成、`npx cap --version`=8.4.0、`npx cap ls`=config 読み込み OK（native platform 未追加）。**index.html は無変更につき BUILD は phase3.28 据え置き**（web アプリ自体は変えていない＝足場のみ）。

**残課題 / 次の方向**
- iOS プラットフォーム生成（`cap add ios`）＋ Info.plist 用途文言（写真/位置）＝Mac/Codemagic 環境で。Apple 有効化後に「最初の1ビルド→TestFlight」（今のコードで・パイプライン de-risk）。その後 web ハック撤去→ネイティブ写真全件/SQLite。

---

## v77 — CDN を vendoring（exifr/heic2any/fflate/Leaflet をローカル同梱） (2026-06-17)

**背景**
- native 化 Phase 1 のチェック項目「CDN vendoring（4.2 対策）」。App Store 審査 4.2（外部 CDN 依存の薄い web ラッパーに見られない）+ オフライン/CDN 障害耐性のため、外部 CDN 依存をローカル同梱に切替。web spike としても堅牢化。Capacitor で `www/` に同梱すれば実行時ネットワーク不要。

**設計判断**
- 対象 = exifr@7 / heic2any@0.0.4 / fflate@0.8.2（head の静的 script）+ Leaflet 一式（leaflet@1.9.4 + markercluster@1.5.3 + polylinedecorator@1.6.0、遅延ロード）。`vendor/<pkg>/` に配置。版・出所・ライセンスは `vendor/README.md`。
- Leaflet の `images/`（マーカー/レイヤーアイコン）も同梱。`leaflet.css` の `.leaflet-default-icon-path` から相対参照されるので leaflet.css と同階層に置いた。
- **対象外（意図的）**: 地図タイル（CARTO/地理院＝実行時の地図データでローカル化不能）/ `@huggingface/transformers`（CLIP＝モデル重みも実行時 DL なので JS だけ同梱は無意味、Phase 1「AI(CLIP)の持ち方決定」で別途）。
- 参照パスは固定（head `<script>` と `loadLeaflet()`）→ 版更新時は同じパスに置き換えるだけで index.html は無変更。

**結果 / 観察**
- preview green: BUILD phase3.28、`window.exifr/heic2any/fflate` 定義済、`loadLeaflet()` で L 1.9.4 + markerClusterGroup + polylineDecorator + css ロード、**外部 CDN リクエスト 0 / vendor ローカル 9 本**、失敗リクエスト 0。

**残課題 / 次の方向**
- 実機（iPhone Safari／将来 Capacitor）で vendored 版でも HEIC 変換・zip 解凍・地図が動くか。`@huggingface/transformers` の on-device 化は Phase 1「AI(CLIP)の持ち方決定」で。

---

## v76 — アプリ名を「あの日 — 写真と足跡から蘇る」に変更 (2026-06-17)

**背景**
- native 化（App Store 申請）に向けた最初の一歩として、ユーザーが表示名を刷新。旧「写真思い出」→ **「あの日」**（タグライン「写真と足跡から蘇る」）。コア「偶然よみがえった久しぶりの記憶」に直結（あの日＝久しぶり / 蘇る＝よみがえる / 足跡＝軌跡・タップ履歴に乗る）。

**設計判断**
- 変えたのは**表示名だけ**（`<title>` と ヘッダ `<h1>`）。`DB_NAME='photo-memory-spike'`・リポ名・GitHub Pages URL は**据え置き**（既存 IndexedDB データと運用を壊さない）。
- ヘッダは「あの日」を主・「写真と足跡から蘇る」を淡色 11px のタグライン（`.tag`）で従に。reminiscence 画面（random/explore）には出さない（[[ui-minimalism-works]]）。
- 英語名 **Madeleine** は据え置き（今回は日本語表示名のみ変更）。

**結果 / 観察**
- preview（desktop / mobile375）green: desktop は1行、mobile は h1 が全幅折り返しでも **BUILD 可視**（iOS キャッシュ確認の生命線）、コンソールエラー0。

**残課題 / 次の方向**
- Notion（WHY/HOW の名前）の名称も「あの日」に追従が必要（DOCMAP / memory product-core-defined は本コミットで更新済み、Notion はユーザー確認後）。

---

## v75 — 推定マーカーに「位置を直して確定」を追加 (2026-06-17)

**背景**
- v74 を見たユーザー「いい感じ。1点直したい。軌跡あり・位置情報なしの写真の推定マーカー、**『この場所で確定』しかない。位置を修正して確定ボタンも欲しい**」。推定がズレている時に、推定をそのまま受けるか／微調整して確定するかを選べるように。

**設計判断**
- 推定 popup を**2択**に: 「📍 この場所で確定」（推定のまま `confirmEstimatedPos`）＋ 「✏️ 位置を直して確定」（v59 のドラッグ編集を**推定座標から開始**）。
- v59 `startLocationEdit(photo)` に **`seedLatLng` 引数**を追加（`let start = seedLatLng || 既存GPS || nearestGps || 中心`）。推定マーカーからは `startLocationEdit(p, [est.lat, est.lng])` で呼ぶ＝ドラッグピンが**推定地点から出る**ので「ほぼ合っている所から微調整」になる。保存は既存 `saveLocationEdit`（`geoManual=true`・封印再計算・`dbPut`）。
- 「確定」を緑系プライマリ（`.mp-confirm` teal #13a08c）、「直して確定」をグレー（既存 `.mp-edit`）で主従を付けた。

**結果 / 観察**
- preview E2E green（DB 非汚染・後始末で写真0/track0・エラー0）: 推定 popup の4ボタン（連想/この場所で確定/位置を直して確定/削除）／「位置を直して確定」→ **編集ピン＋バーが推定座標 35.695,139.7675（記録点の中点）から起動**／ドラッグせず保存→推定→実マーカー昇格（est0/real1）。

**残課題 / 次の方向**
- v74 の残（実機で補間/snap 閾値・推定の納得感・確定まで使うか）に合流。微調整した位置の由来は今 `geoManual`（推定由来 `geoFromTrack` と区別せず）＝必要なら後で分ける。

---

## v74 — GPSなし写真を「軌跡の時刻位置」に配置 (2026-06-17)

**背景**
- v73 でロガーを入れた直後、ユーザー「**軌跡の記録が残っている時間帯の画像の扱い**: ①写真に位置情報があるとき＝今まで通りその位置／②写真に位置情報がないとき＝写真の時刻と軌跡から、その時刻に軌跡上のどこにいたかの場所。このロジックにできる?」。
- これは Notion §③(b)「**GPSなし写真の位置補完**（写真時刻→位置履歴→撮影地推定→時空軸に参加）」そのもの。ロガー（v73）が入って初めて成立する次の一手。

**設計判断**
- **推定 = `estimateFromTrack(timeMs, pts)`**（pts は t 昇順の記録点）: 写真時刻を**挟む前後点**があり時間差が `TRACK_BRACKET_MS`(1時間)以内 → **線形補間**（歩いた途中の位置）。挟めない時は近い片側に `TRACK_SNAP_MS`(30分)以内なら **snap**。どちらも満たさない（記録の穴/外）→ **null＝補完しない**（ユーザー指定「記録が残っている**時間帯のみ**」を厳密に満たす）。二分探索で O(log n)。
- **非破壊（derived）を既定に**: 推定位置は**写真に保存しない**で地図に表示するだけ。理由＝(a) 軌跡が増えれば推定が自動で改善/出現、(b) 「実GPS」と「推定」を取り違えない正直さ。**実GPSの白い実線枠と違い、推定は青緑の破線枠**（`.photo-marker-est`）で見た目を変え「これは推定」と一目で分かるように。
- **確定（persist）はユーザーの明示操作だけ**: 推定マーカーの popup に「📍 この場所で確定」→ `confirmEstimatedPos` が v59 の手動保存と同じ流儀で `lat/lng`+`geoFromTrack` を `dbPut`（封印 `_sealed` 再計算）→ 以後は**通常のGPS写真扱い**（クラスタ/動線に乗り、推定マーカーは消える）。編集＝想起の入口（[[editing-triggers-reminiscence]]）の横展開。
- **描画は `refreshTrack` に相乗り**（同じ window・同じ track 点を使うので一度の async load で軌跡線と推定配置を両方描く）。GPSなし写真は `showEstimated` ON のとき、表示中の日/期間に含まれ推定が non-null なものだけ配置。⋯メニューに **「📍 GPSなし写真を軌跡から配置」トグル（既定ON）**。畳んだ日は出さない。
- **fitBounds フォールバック**: GPS写真ゼロで軌跡/推定しか無い窓でも収まるよう、`refreshTrack(canFit)` が `filteredGps` 空かつ点ありなら推定/軌跡点に `fitBounds`（applyFilter の fitBounds は実GPS基準で空振りするため）。

**結果 / 観察**
- preview E2E green（DB 非汚染・合成は検証後 `dbClear`/`trackClear` で完全撤去、写真0/track0 確認）:
  - `estimateFromTrack` 単体: 補間（+5分→中点 lng 0.005）/ 穴（間隔80分→null）/ snap（10分前→近接点）/ 圏外（60分前→null）/ 空→null。
  - 描画: 軌跡3点＋GPSなし写真2枚（圏内 now-5分 / 圏外 10日前）→ **推定マーカーは圏内の1枚だけ**・実GPSマーカー0。⋯トグル OFF=0 / ON=1。
  - 確定: `confirmEstimatedPos` で補間値 35.695/139.7675（記録点の中点ピッタリ）+`geoFromTrack` を保存 → 推定マーカー→実マーカーへ昇格（est 0 / real 1）。コンソールエラー0。
- 実機（iPhone Safari）は未確認（軌跡を貯めるには実機で記録が要る）。

**教訓**
- ロガー（v73）の `track`（座標+時刻）を入れた途端、「GPSなし写真の救済」が**新ストア無し・推定関数1本＋描画相乗り**で乗った。位置**履歴**は写真の位置補完という別価値を自動で生む（§③b の読み筋が当たった）。
- 「推定」を実データに混ぜない（derived＋見た目で区別＋確定は明示操作）ことで、reminiscence の土台（日時・場所）を**汚さずに広げられる**。[[ui-minimalism-works]] の「使う時だけ広がる」をデータ品質側で実践。

**残課題 / 次の方向**
- **実機で**: ①補間/snap の閾値（1時間/30分）が歩き・電車の実軌跡に合うか②推定マーカーが「だいたい合ってる」と感じるか（破線枠で推定と伝わるか）③「確定」まで使うか、見るだけか。
- **将来**: タイムラインのGPSなし写真タップ→推定マーカーへフォーカス（今はフル画像のまま）/ 推定位置を連想ウォークの時空軸に参加させる（今は地図表示のみ）/ 複数枚まとめて確定 / 別日の軌跡からの補完（今は同 window 内のみ）。

---

## v73 — 位置ロガー: 開いている間だけ軌跡を記録 (2026-06-17)

**背景**
- ユーザー「ロガーを作る。**あとから振り返ったときのその日の軌跡を振り返れる**ようにしたい。モードは3つ＝まずオフ、重要な移動のみ記録、あとはこまめに記録」。
- これは Notion §③（位置の現在地/履歴）そのもの。直前の「OS-id 議論 → Notion §③⑦ 現状照合」で **⬜ 未着手** と整理した部分を un-park（地図 un-park と同型: 検証 YES の周回で明示要求が来たら着手）。

**設計判断**
- **web spike の根本制約を先に確認**（AskUserQuestion）: ブラウザは「前面（アプリを開いて画面が点いている間）」しか位置を取れない。背景の常時記録（SLC/CLVisit）は native の宿題（Notion §④）。普段の外出中はこのアプリが開いていないので「その日の軌跡」はほぼ取れない。→ それでも **spike で作る**を選択（データ構造＋「振り返り UI」を先に確定し、散歩中アプリを開いて手触り検証する価値がある）。
- **記録 = `track` ストア（DB v1→v2）** に `{id, t(epoch ms), lat, lng, acc, mode}`。**座標+時刻のみで写真キーに依存しない** → 機種変更でそのまま移せる（Notion §⑥ の「必ず運ぶ」side。OS-id 問題と無縁）。`t` インデックスで日/期間を範囲取得。
- **3モード**（localStorage `pms-loggerMode`、ヘッダ 🛰️ パネルで選択）:
  - `off`（既定）/ `important`（前の記録点から **500m 以上**動いた時だけ＝疎・電池軽い・SLC 風）/ `frequent`（**25m 以上 or 20秒経過**ごと＝密）。
  - エンジン: `loggerOn` なら `getCurrentPosition`（開いた瞬間の1点）＋`watchPosition`。`loggerRecord` が `haversine` でモード閾値フィルタ。`force`（起動/復帰の1点）は無条件。
  - **前面でしか記録できない制約に正直に対応**: `visibilitychange` で background なら `stopLogger`、前面復帰で `startLogger`（復帰時に1点）。記録中は `wakeLock` で画面維持（既存 `withWakeLock` と同じ系）。
- **振り返り = 地図に重ねる**（別ビューを作らない）。`refreshTrack` が **いま地図に出ている日/期間ぶんだけ**ロガー軌跡を描く（pickDays＝各日の窓 / range＝その期間 / 全期間ブラウズ＝全記録）。**写真の動線とは別レイヤ・別の見た目**＝青緑 `#2ee6c0` の点線＋点（「自分が通った道」）。畳んだ日（foldedDays）は写真と同じく軌跡も消す。`applyFilter` 末尾から呼び、表示中の日/期間に自動追従。
- **トグル**＝⋯「表示」メニューに「🛰️ 自分の軌跡」（既定 ON）。全削除（🗑）は track も含めて消す。パネルから「🗗 記録を消す」（軌跡だけ消去・写真は残す）。

**ハマったところ**
- top-level `let`（`loggerMode`/`mapState` 等）は classic script では window に乗らない（`function` 宣言は乗る）→ preview からの状態読みは関数経由 or DOM/localStorage 経由で行った。

**結果 / 観察**
- preview E2E green（合成4点を `track` に注入→検証後に `trackClear` で完全撤去、usage 0MB 確認）: 4点同日→**1本の点線＋4ドット＝5 paths** で描画 / ⋯トグル ON=5・OFF=0・再ON=5 / パネル＝3モード・既定オフ・`important` 切替で localStorage 永続＆🛰️点灯＆「今日 N 点」カウント / **フィルタ閾値: important(500m)→2点・frequent(25m/20s)→2点**。コンソールエラー0。
- ※ v72 と同様、地図 overlay 下で preview の screenshot ツールがタイムアウトする事象あり（ページ応答・eval 通る・エラー0）。DOM/状態を直接集計して検証。
- 実機（iPhone Safari）の手触りは未確認（geolocation 権限は実機でしか出ない）。

**教訓**
- spike の「最速で手触りに触る」は、**できないこと（背景記録）を native に切り、できること（前面記録＋振り返り UI＋データ構造）だけ先に確定**する形で守れる。制約を隠さず先に共有（AskUserQuestion）したことで、owntracks 型の期待ズレを未然に回避。
- 地図の状態集約（pickDays/range/foldedDays + applyFilter 1本）に**新レイヤ（ロガー軌跡）を1関数足して末尾で呼ぶだけ**で乗った。v64〜v72 の「状態に寄せる」投資が、別種データの重ね描きでも効いた。

**残課題 / 次の方向**
- **実機（iPhone Safari）で手触り**: ①散歩中アプリを開いて軌跡が貯まるか／前面復帰の1点が効くか②重要/こまめの粒度・電池感③「その日の軌跡を地図で振り返る」が想起を呼ぶか（編集と同じく [[editing-triggers-reminiscence]] 的に効くか）。
- **将来**: 滞在地クラスタ（通過/滞在の分離）/ 再訪検出（N年ぶり）/ **GPSなし写真の位置補完**（写真時刻→位置履歴→撮影地推定。§③b）/ ロガー軌跡から連想ウォークへ / 軌跡の色を時刻→空色に（写真線の `skyColor` 流用）。本命の背景常時記録は native（madeleine）= Notion §④。

---

## v72 — タイムラインは「表示中の日」だけ + days モードも折りたたみ (2026-06-16)

**背景**
- v71（📅から日/期間を追加）を見たユーザー「**タイムラインも、地図に表示されているのだけで十分**。そう変えたい。タイムラインの日にちをトグルで畳んで地図のライン・写真ピンも非表示になる機能はそのまま使いたい」。
- v66〜v67 の `days` モードは「全日の見出しを残し、選んだ日を ✓ 強調＋他は足す候補として見せる」設計だった。が、日の追加が v71 でカレンダー「＋追加」に移ったので、タイムラインに全候補日を出す必要が消えた → 余計を削ると想起されやすい（[[ui-minimalism-works]]）。

**設計判断**
- **`days` モードを入口モード（random3/day）と完全統一**。3箇所の条件に `'days'` を足すだけ（状態集約のおかげで安かった）:
  - `updateTimelineUI`: 選択モードは常に「選ばれた日（＝地図に出ている日）だけ表示・他は見出しごと隠す」に一本化（候補日を見せる `multi` 分岐と `tl-day-on` ✓ を撤去）。
  - 日見出しタップ: `days` も `toggleFold`（折りたたみ）に。**ブラウズ（全期間）だけ `toggleDayFilter`（その日の選択を始める）**。
  - `applyFold` の `foldable` と `applyFilter` の `useFold` に `'days'` を追加 → 畳んだ日は線（drawTrips がスキップ）も**マーカー（filteredGps から除外）**も消える（v69/v70 の仕組みをそのまま `days` に適用）。
- **畳む≠集合から外す**: 折りたたみは pickDays に残したまま地図から隠す（再タップで戻る）。「📅 N日」の N は選んだ日数のまま（畳んでも減らない）。日を完全に外すのは ✕全部を見る、または別の組み直し（カレンダー）。非連続選択の「足す/外す」役割はカレンダー「＋追加」に集約済み。
- バーの「日付タップで足す/外す」ヒントは撤去（タップ＝折りたたみになったため）。folディスカバリは見出しの ▾/▸ に委ねる（入口モードと同じ作法）。

**結果 / 観察**
- preview E2E green（合成12枚＝京都2019旅2日＋東京2022旅2日＋孤立日＋GPS無し、DB 非汚染、BUILD `phase3.23`）: カレンダーで2旅行を `days` に組む → **タイムラインは選んだ4日だけ・候補2日（孤立日/GPS無し）は非表示**・4日とも ▾。京都5/2 を畳む → **見出しは ▸ で残り写真は collapse・地図のマーカー10→8＆その日の線が消える・pickDays は4日のまま**。再タップで復元（10マーカー/線4本）。ブラウズ日タップ＝1日選択開始（「📅 1日を重ねて」）/ ✕全部を見る＝全6日表示。コンソールエラー0。
- ※preview の screenshot ツールが地図 overlay でタイムアウトする事象あり（ページは応答・eval は通る・エラー0）。DOM 可視状態を直接集計して検証。

**教訓**
- v64〜v71 で「状態1箇所（pickDays/foldedDays）＋更新関数1本（applyFilter）」に集約し続けてきた結果、3モード（random3/day/days）の振る舞い統一が「3つの条件式に `'days'` を足す」だけで済んだ。分岐を増やさず状態に寄せておくと、後からの「揃える」要望が安い。
- 「畳む（隠すが集合に残す・戻せる）」と「外す（集合から消す）」を分け、外す＝カレンダー、隠す＝タイムライン折りたたみ、と入口で役割を割ると、同じ「地図から消える」結果でも操作の意味がぶれない。

**残課題 / 次の方向**
- 実機で①候補日が消えてスッキリしたタイムラインの手触り②`days` での折りたたみが「畳む（戻せる）」と伝わるか（▸ の発見性）③日を完全に外したい時に ✕全部を見る＋組み直しで不便がないか（不便なら days に「外す」動線を戻す検討）。

## v71 — 📅パネルから飛び飛びの日/期間を「追加」できる (2026-06-16)

**背景**
- ユーザー「入口が📅 全期間（ブラウズ）。飛び飛びの日もカレンダーから期間で選べるようにしたい。全期間・適用に加えて『追加』ボタンがあると分かりやすい。**タイムラインからだけだと数年前の日を選ぶのが大変**」。
- v66〜v67 で非連続の複数日選択（`pickDays`/`days` モード）は実装済みだが、入口がタイムラインの日付タップのみ → 数年前の日に届くまで延々スクロールが要る痛み。TODO の将来案「📅パネルで期間を追加して非連続な複数期間（trip 単位）」を、明示要求が来たので un-park。

**設計判断**
- **既存の `days` モードにカレンダー入口を足すだけ**（新モデルを作らない）。📅パネルに **`＋ 追加`** ボタンを新設し、入力中の日／期間を `pickDays` にマージして `pickMode='days'` に。タイムラインの日付タップ（`toggleDayFilter`）と**同じ状態に集約**＝片方で足した日がもう片方にも反映され、出し入れが相互にできる（実機 E2E で確認済）。
- **入力の解釈**: 開始のみ＝その1日 / 終了のみ＝その1日 / 両方＝その範囲。範囲は**写真が実在する日だけ**に展開（`dayKeysWithPhotos`）＝空の日を足して無反応にしない。Set でマージ（重複は無視）。
- **`適用` と `追加` で役割を分ける**: 適用＝連続期間「だけ」を表示（range モード・v58 連続線）/ 追加＝重ねる（overlay・各日が別色の独立軌跡）。3ボタンは [全期間][＋追加][適用(primary)]。
- **パネルは追加後も開いたまま**、入力をクリアし status 行に「✓ N日を追加（いま M日を重ねて表示）」を出す＝**離れた複数の旅行を続けて足す**動線。再オープン時も `days` モードなら「📅 いま N日を重ねて表示中」を表示。
- **多日範囲を v58 風の1本連続線で描くのは今回やらない**（既存 `days`＝各日独立軌跡を流用、ヒントに「各日が別色の軌跡」と明記して期待を合わせる）。1旅行が分断して見えると実機で出たら次周回。spike らしく感じた痛み（数年前の日に届かない）を最小で潰す。

**結果 / 観察**
- preview E2E green（合成12枚＝京都2019旅2日5枚＋東京2022旅2日5枚＋孤立日1＋GPS無し1、DB 非汚染）: 全部を見る→ブラウズ11マーカー→📅で京都旅（5/1〜5/2）追加＝`days`2日・5マーカー・「✓ 2日を追加」「📅 2日」・入力クリア→東京旅（9/10〜9/11）追加＝**4日・10マーカー・2旅行が重なる**・タイムライン✓4日。エッジ＝空入力／写真なし期間（2025）／既出の日／開始のみ1日追加に各メッセージ。**タイムラインで 2021/01/15 をタップして外す→4日**（カレンダー追加日をタイムラインから出し入れ可）。適用＝連続範囲（東京旅5マーカー）・全期間＝解除11・再オープンで status 表示。コンソールエラー0。BUILD `phase3.22`。

**教訓**
- 既に「状態1箇所（`pickDays`）＋更新関数1本（`applyFilter`）」に集約されていた（v64〜v70 の積み上げ）ので、新しい入口（カレンダー）を足すのは「同じ状態に書き込む別の口」を1つ増やすだけで済んだ。状態を集約しておくと入口追加が安い、の実例。

**残課題 / 次の方向**
- 実機で①数年前の日にカレンダーで一発で届く快適さ②多日範囲が各日別色の独立軌跡で見えるのが旅として読めるか（読めなければ v58 風の連続線＝期間を unit で持つ `pickRanges` 化を検討）③追加→閉じて確認、の往復の手触り。将来: 📅パネルに「最近の期間プリセット」/ 足した期間を unit でラベル表示・削除。

## v70 — 折りたたんだ日は地図の写真(マーカー)も非表示 (2026-06-16)

**背景**
- v69（折りたたみ＝線を非表示）に続けてユーザー「線だけじゃなく、折りたたんだ日の写真も同様に非表示に」。畳んだ日は地図から丸ごと消す（線＋マーカー）。

**設計判断**
- **マーカー（`filteredGps`）から折りたたんだ日の写真を除外**。`applyFilter` で `visibleGps()`（＝全 pickDays）から、`foldedDays` の日を `filter` で落としたものをマーカーに使う（入口モードのみ）。
- **線の色 index を安定させるため `lineGps` は全日のまま**にし、`drawTrips` が畳んだ日の線だけスキップ（v69）。これで「畳むと残った日の色が入れ替わる/単一日になって空色化する」事故を防ぐ：例 大阪🔴/京都🟡/東京🔵 で京都を畳んでも、大阪🔴・東京🔵は元の色のまま（idx が全日基準で固定）、東京以外を全部畳んでも 大阪は palette のまま（lineGps が3日 → `days.length≠1` で空色にならない）。
- マーカーは消すが folded 判定は入口モード（random3/day）限定。browse/days は `foldedDays` 空なので無影響。`toggleFold` は v69 同様 `applyFilter({skipFit:true})` 一本で地図・タイムライン両方更新（ビュー維持）。

**結果 / 観察**
- preview E2E green（合成9枚＝大阪/京都/東京 各3枚）: もう一度引く＝線3色🔴🟡🔵・マーカー9。京都を畳む → **線2本(🔴🔵)・マーカー6**（京都の3枚と線が消える、残2日の色は不変）。東京も畳む → 線1本(🔴)・マーカー3（大阪は palette のまま）。全部畳む → 線0・マーカー0。京都を開く → 戻る。非同期エラー0・コンソールエラー無し。BUILD `phase3.21`。

**教訓**
- 「畳んだ日を消す」を `filteredGps`（マーカー）だけに効かせ、`lineGps`（色 index）は全日に保つことで、表示の絞り込みと色の安定を両立。減らす対象（マーカー）と、安定させたい基準（色の並び順）を別の集合に分けるのが鍵。

**残課題 / 次の方向**
- 実機で、畳んだ日が地図から丸ごと消える挙動の手触り（v69 の「線だけ」より自然か）。

## v69 — 折りたたんだ日は地図の線も非表示 (2026-06-16)

**背景**
- v68（タイムラインの折りたたみ）に続けてユーザー「タイムラインで折りたたんだ日の線は地図でも非表示にしてほしい」。タイムラインで畳んだ＝今は見たくない日 → 地図の軌跡も消えると見通しが良い。

**設計判断**
- **`drawTrips` の pickDays 分岐で、折りたたんだ日（`mapState.foldedDays`）の線（polyline＋矢印）を描かない**。dk（dayKey 文字列）→ dayStart(ms) に変換して `foldedDays` と突合。**マーカーは残す**（ユーザーは「線」を指定。ピンは消さない＝どこで撮ったかは見える）。
- **`toggleFold` を `applyFold()` 単独から `applyFilter({skipFit:true})` に変更**：drawTrips で線を貼り直し（畳んだ日を除外）＋ updateTimelineUI→applyFold でタイムラインの畳みも反映。**`skipFit` でビューは維持**（畳む度に地図が動かない）、マーカーは visibleGps 不変なので残る。`applyFilter` は buildTimeline の引数で受けているのでスコープ内から呼べる。

**結果 / 観察**
- preview E2E green（合成9枚＝3日×3枚）: もう一度引く＝3本の軌跡・9マーカー。1日折りたたむ → **線 3→2 本・マーカーは9のまま**・タイムラインも畳む。もう1日畳む → 1本。開き直す → 2本に戻る。非同期エラー0・コンソールエラー無し。BUILD `phase3.20`。

**教訓**
- 「タイムラインの折りたたみ」と「地図の線」を同じ `foldedDays` 状態で駆動し、`toggleFold` を `applyFilter({skipFit:true})` 一本に通すと、両者が常に一致する（タイムライン・地図で別々に隠す処理を書かない）。状態1箇所＋更新関数1本の型（[[map-view-unparked]]）。

**残課題 / 次の方向**
- 実機で、線だけ消してマーカーは残す挙動が直感に合うか（マーカーも消したくなるか）。`days`/browse での折りたたみ要望が出るか。

## v68 — もう一度引く/写真入口で日見出しタップ＝折りたたみ (2026-06-16)

**背景**
- ユーザー「もう一度引くの『その3日だけ』のタイムラインでも折りたたむ機能があると便利」。各日に写真が多いと3日でも縦に長い → 日ごとに畳んで見出しだけにできると見通しが良い。

**設計判断**
- **入口モード（`pickMode` random3 / day）では日見出しタップ＝その日の折りたたみ**（v67 で入口モードの日タップは選択無効＝inert にしていたので、そこに折りたたみを乗せる。browse/`days` は従来どおり日タップ＝選択）。日見出しのクリックハンドラを pickMode で分岐（`toggleFold` / `toggleDayFilter`）。
- 折りたたみは**タイムラインだけの見せ方**（地図のマーカー/軌跡は変えない）。`mapState.foldedDays`（dayStart の Set）で管理し、`applyFold` が ① 折りたためる見出しに `.tl-foldable`（▾）② 畳んだ見出しに `.tl-folded`（▸）③ 畳んだ日の写真に `.tl-fold`（非表示）を付ける。`updateTimelineUI` 末尾でも `applyFold` を呼び、フィルタ更新後も状態維持。`days`/browse では `foldable=false` で解除。
- **タイムライン再構築ごとに `foldedDays` をリセット**（`buildTimeline` 先頭）＝もう一度引く/全部を見るで畳み状態は初期化（新しい3日は全開）。

**結果 / 観察**
- preview E2E green（合成15枚＝3日×5枚）:
  - もう一度引く → 3見出しすべて ▾（foldable）・全15枚表示。日見出しタップで ▸＋その日5枚が隠れる（15→10→5）、再タップで戻る（▾）。
  - 🎲 もう一度引く → 折りたたみリセット（foldedDays 0・全15表示）。
  - 全部を見る（browse）→ 見出しに ▾ 無し・日タップは折りたたみでなく日選択（'days'）。非同期エラー0・コンソールエラー無し。BUILD `phase3.19`。

**教訓**
- 「入口モードでは日タップが空いている」状態を作っておいた（v67）ので、折りたたみを自然に乗せられた。同じジェスチャでもモードで意味を変える時は、片方を意図的に inert にしておくと次の機能の置き場所になる。

**残課題 / 次の方向**
- 実機で折りたたみの手触り（畳んだ日に枚数「(N枚)」を出すと開く判断がしやすいか）。`days`/browse でも折りたたみが欲しくなるか（今は入口モードのみ）。

## v67 — 非連続選択の置き場所を整理 (もう一度引く=3日だけ / 全期間で飛び飛び) (2026-06-16)

**背景**
- v66 のユーザーフィードバック「もう一度引くのほうは3日分のタイムラインだけでれば十分」「全期間アイコンがあるほうが、いまは連続日しか選べないけど、飛び飛び日（期間）も選べるようにしたい」。
- v66 は pickDays の全モードで「全日付の見出しを出す＋日タップでトグル」にしたため、**もう一度引く（random3）でも全日付が並び**、非連続選択が「入口」側に漏れていた。ユーザーの整理＝**非連続は『全期間（ブラウズ）の文脈』に、入口（もう一度引く/写真）は素直にその日だけ**。

**設計判断**
- **入口モード（`pickMode` random3 / day）はタイムラインを「その日だけ」に戻す**（v65 の見せ方）。`updateTimelineUI` を pickMode で分岐：random3/day=選ばれた日だけ表示・日タップ無効（`toggleDayFilter` 先頭で return）/ `days`=全日付の見出しを残し選んだ日を ✓＋写真（足す候補が見える）。
- **非連続選択は browse から始まる `days` モードに集約**＝「全期間（📅）の文脈」。`days` モードでは **📅/⋯ を隠さず残し**（`updateCtrlMode` で entry のみ隠す）、📅 ボタンは選択枚数「📅 N日」を表示（`updateCtrlLabels`）。解除はタイムラインの「✕ 全部を見る」。
- **📅 パネル（連続範囲）と非連続日選択は排他**：📅 で連続範囲を適用 or 全期間にすると pickDays を解除（連続レンジ＝v58 連続線＋封印が使える従来動作を温存）。逆に日タップは range を解除。パネルのヒントも「飛び飛びはタイムラインの日付タップ」に更新。封印が使う `mapState.range` には触れないので封印は無影響。

**結果 / 観察**
- preview E2E green（合成: 京都2旅行＋東京＋大阪）:
  - **もう一度引く → タイムラインは3日だけ**（全4日中3日表示）・🎲/↩︎表示・📅⋯隠す。
  - 全部を見る → ブラウズ（📅全期間/⋯表示・全7項目）。**browse で日タップ→ `days`：📅 が "📅 1日" で残り、全日付の見出しは見えたまま、選んだ日 ✓。もう1日タップで「📅 2日」・軌跡2本(#ff6b6b/#feca57)が重なる**。
  - ✕全部を見る→ブラウズ復帰。📅パネルで連続範囲適用→ pickDays 解除・"📅 7/20"。写真入口は日タップ無効（pickDays 不変）。写真フォーカス（setView）非同期エラー0。コンソールエラー無し。BUILD `phase3.18`。

**教訓**
- 同じ「日の集合」でも、**入口で与えられた集合（random3/day）と、ユーザーが全期間で組む集合（days）は見せ方を分ける**べきだった（前者＝素直にその日 / 後者＝候補も見せて編集）。pickMode で UI を分岐するだけで両立。
- 機能の置き場所はユーザーの語彙に合わせる（「もう一度引く」「全期間」）。非連続を入口側に置くと混乱、全期間側に置くと腑に落ちる。

**残課題 / 次の方向**
- 実機で、全期間での日タップ非連続の発見性（パネルのヒストで気づくか）/ 大量の日付見出しから足す日を探す手間（期間プリセット・検索）。将来案: 📅 パネルで「期間を追加」して非連続な複数期間（trip 単位）を足す（今は連続範囲 or 日タップの個別日）。

## v66 — 飛び飛びの複数日を重ねる + markercluster クラッシュ根治 (2026-06-16)

**背景**
- ユーザー要望「2回同じ地域に旅行。その2つの旅行の軌跡を重ねたい」=「飛び飛びの複数日フィルタ」。v51 以来の日付フィルタは連続範囲しか選べなかった。
- 土台は v64 の `pickDays`（表示する日の集合）。地図側はすでに「複数日＝各日が別色の軌跡」で重なるので、足りないのは**日を非連続に複数選ぶ UI**。

**設計判断**
- **タイムラインの日付見出しタップを「その日を pickDays に出し入れ（トグル）」に変更**（`toggleDayFilter` を連続レンジ計算から集合トグルへ）。離れた2日を足すと各日が別色の軌跡で地図に**重なる**。`pickMode='days'`（手で組んだ集合＝🎲 は出さない）。最後の1日を外すと `clearPick` でブラウズへ。連続範囲は 📅 パネルに残置（住み分け）。
- **タイムラインは「全日の見出しを残しつつ、選んだ日だけ ✓ 強調＋写真表示、他は見出しだけ畳む」**（v65 は選択日以外を完全非表示だったが、それだと「足す日」を選べない）→ 他の日も「重ねる候補」として見え、タップで足せる。`.tl-day-on`（緑＋✓）。バー「📅 N日を重ねて · 日付タップで足す/外す」。トグルは `applyFilter` のみ（DOM 再構築なし＝スクロール維持）。
- **markercluster クラッシュの根治**: 近接マーカー（同じ場所の写真）＋ `fitBounds`/`zoomToShowLayer`（タイムライン写真フォーカス）の組合せで、moveend 内の `_recursivelyRemove/AddChildrenFromMap` が消えたレイヤを参照し **`Cannot use 'in' operator to search for '_leaflet_id' in undefined`** を投げる。非同期なので呼び出し側 try/catch では捕まらない。→ **クラスタ生成に `removeOutsideVisibleBounds: false`**（moveend ごとの画面外マーカー付け外しをやめる＝クラッシュ箇所を通らない）で解決。`animate: false` も併用（瞬時クラスタ化）。数十〜数百規模なら全保持で問題なし。

**効かなかった対処（次の Claude が周回しないように）**
- `fitBounds` の `animate:false`：moveend を同期化するだけで回避にならない。
- `fitBounds` を try/catch：その場は握れるが `_currentShownBounds` が壊れたまま残り、後続の `setView`（タイムライン写真フォーカス＝非同期）で再発。
- クラスタを再構築の間だけ `removeLayer`/`addLayer`：効果なし。
- `cluster._moveEnd` を instance で try/catch ラップ：moveend リスナーは別参照で登録済みのため差し替わらず無効。
- **効いたのは `removeOutsideVisibleBounds: false` のみ**（クラッシュする内部処理自体を走らせない）。

**結果 / 観察**
- preview E2E green（合成: 京都2旅行 2024-05-10 / 2023-11-03 ＋東京＋大阪）:
  - 写真入口(京都旅1)→ 京都旅2の日付タップ→ **pickDays 2日・軌跡2本(#ff6b6b/#feca57)が重なる・両日 ✓・写真4枚**。外すと1日へ、最後を外すとブラウズ(✓残らず📅復活)。
  - random3 で日タップ→ 'days' 化で 🎲 消滅。
  - **クラッシュ根治の確認**: 写真入口→重ねる→タイムライン写真フォーカス連続→ setPick→生 setView(animate)、すべて**非同期エラー0・thrown なし・地図健全**。コンソールエラー無し。BUILD `phase3.17`。

**教訓**
- markercluster の `_leaflet_id in undefined`（moveend 内 hasLayer）は、近接マーカー＋ビュー変更で出る既知の罠。**呼び出し側の try/catch では非同期ゆえ無力**。`removeOutsideVisibleBounds: false` で「画面外付け外し」を止めるのが根本回避（規模が小さければ全保持で実害なし）。
- 「集合（pickDays）に状態を寄せておく」と、ヘッダ3日・写真1日・**非連続N日**が同じ仕組みに乗る（v64 の設計が v66 を1日タップのトグル追加だけで実現させた）。

**残課題 / 次の方向**
- 実機で、離れた2旅行を重ねた時の軌跡の見分け（色）・タイムラインで日を足す操作感（大量の日付見出しから目的日を探せるか／検索やジャンプが要るか）。1万枚規模で `removeOutsideVisibleBounds:false`（全マーカー保持）の描画負荷。

## v65 — タイムラインも「表示中の日」に対応 (2026-06-16)

**背景**
- v64 のヘッダ=ランダム3日 / 写真=その日 が両方とも好感触（ユーザー「どちらの入口からもいい感じ」）。続けて「**どちらの入口から入ってもタイムラインもその日に対応させて**」。
- v64 時点では pickDays モードでもタイムラインは全件表示（「次の入口を選ぶ索引」として全件のままにしていた）だった。

**設計判断**
- **タイムラインを表示中の日に絞る**: `updateTimelineUI` の pickDays ブランチで、全件表示をやめ **選ばれた日 (ヘッダ=3日 / 写真=その日) の項目だけ表示**、他は `tl-out` で非表示（v55 の期間フィルタと同じ見せ方）。`pickDays` の暦日キー文字列を `dayStart(ms)` に変換し `itemEls/dayHeaders` の `dayStart` と突合（`new Date(y,m,d).getTime()` 同士で厳密一致）。
- **その日の GPS 無し写真も出す**: 突合は「日」単位なので、選ばれた日に GPS 無し写真があればタイムラインに出る（地図には出ないがその日の記録として一覧に並ぶ）。地図(GPS のみ)とタイムライン(その日の全部)で役割分担。
- **fitBounds を try/catch で握り潰し**: 🎲 連打と地図開閉が極端に重なると markercluster の moveend が `_leaflet_id in undefined` で落ちる事象を再確認（`animate:false` は moveend を同期化するだけで回避にならない）。実ユーザー相当の素早い開閉再openでは再現しないが、保険として fitBounds を try/catch（最悪フィットしないだけ・マーカー/線は描画済み）。

**結果 / 観察**
- preview E2E green（合成9枚: 京都A=3GPS+1無GPS / 東京B=2 / 大阪C=2 / 無関係日の無GPS1）:
  - ヘッダ(A,B,C)→ タイムライン **8項目表示**（A4+B2+C2、無関係1は非表示）・日見出しは3日のみ・地図マーカー7。
  - 写真入口(A)→ タイムライン **4項目**（その日の3GPS+1無GPS）・日見出し1日・地図3。
  - 全部を見る → 9項目（全件）・バー「全期間」。
  - fitBounds try/catch を確認・コンソールエラー無し。BUILD `phase3.16`。

**教訓**
- 「地図＝GPS のみ / タイムライン＝その日の全部」を**日単位で対応**させると、GPS 無し写真も“その日の記憶”として一覧に乗り、地図とタイムラインが補完関係になる（地図に出ない写真も思い出の入口にできる）。
- markercluster の moveend クラッシュは `animate:false` では消えない（同期化されるだけ）。確実な握り潰しは呼び出し側の try/catch。

**残課題 / 次の方向**
- 実機で、絞ったタイムライン（特にヘッダ3日が日付降順で離れて並ぶ）が「軌跡↔一覧」の行き来として使いやすいか。次の周回: 飛び飛び複数日フィルタ（`pickDays` の集合編集 UI）。

## v64 — 地図=軌跡から想起: ランダム3日 / 写真→その日 (v62 を作り替え) (2026-06-16)

**背景**
- ユーザーが地図の体験を再定義: **メイン = 自分の過去の軌跡から記憶を思い出す / サブ = その写真が撮られた場所のみで想起**。単位を「写真の近傍」から **「日（その日の軌跡）」** へ。
- 具体仕様: **🗺ヘッダ = ランダムに位置情報のある3日を選び、それぞれの日の全GPS写真を軌跡に**（軌跡上にその日の他の写真もマーカー）。GPSが1枚だけの日は線なし＝その1点のみ（それでよい）。**1本の軌跡をタップするとその日以外の線が暗くなる**。**「🎲 もう一度引く」で別の3日**。**📷写真入口 = その写真の日**（地図のフィルターがその日になる）。
- これは v62（seed＝写真の時空近傍＋意味echo）を置き換える。echo の扱いは確認の上 **外して軌跡に集中**（ユーザー選択。`meaningNeighbors` 自体は explore で使うので残置）。

**設計判断**
- **状態を `mapState.pickDays`（表示する暦日キーの配列）に集約**。`pickDays` があれば その日の全GPSを表示、null は従来の期間ブラウズ。`pickMode`='random3'(ヘッダ) / 'day'(写真)。`focusedDay` = タップで注目した日（他を暗く）。**v62 の seed/seedSpacetime/seedMeaning/showEcho/lineGpsOf を全廃**し pickDays に一本化。
- **軌跡の描き分け（`drawTrips` を再構成）**: ① pickDays モード = 選ばれた**各日を独立した1本**に（日順パレット色 / `focusedDay` で他を opacity 0.15 に / 単一日なら時刻→空の色 v63）。各線に click → その日を `focusedDay` トグル → 再描画（`L.DomEvent.stop` で背景タップと衝突回避）。背景タップで解除。② ブラウズ = 従来の v58（旅行連続線・日ごと色）/ v63（単一日は空色）を据え置き。
- **写真入口＝その日**は `setPick([dayKeyOf(photo)],'day')` ＝ 単一日なので **v63 の空色がそのまま効く**。ヘッダ＝3日は複数日なので **v58 パレットで3本が色分け**＝「3本の線」がそのまま読める（v63 の色ルールが新モデルに自然に乗る）。
- **コントロール**: pickDays 時は 📅期間/⋯表示 を隠し、`🎲 もう一度引く`（random3 のみ）と `↩︎ 全部を見る`（`clearPick`→ブラウズ）を出す。タイムラインのバーも「🎲 ランダムな3日 / 📅 その日」＋「✕ 全部を見る」。日付見出しタップは pickDays を抜けて期間ブラウズへ。
- **`btnMap` は引数なし `openMapView()`** で random3、explore中心カード/popup 等は `openMapView(photo)` で day モード。
- **堅牢化**: `applyFilter` の `fitBounds` を `animate:false` に。`🎲` 連打など連続再描画でアニメ中フィットが重なると markercluster が `_leaflet_id in undefined` で落ちる事象を確認 → 非アニメ即フィットで根絶（連打/即closeのストレスでクラッシュ無しを確認）。

**結果 / 観察**
- preview E2E green（合成10枚: 京都A3/東京B3/大阪C2/札幌D1＋GPS無し1、DB非汚染）:
  - ヘッダ→random3（3日・🎲/↩︎表示・📅⋯非表示）。A,B,C 固定で **3本パレット #ff6b6b/#feca57/#48dbfb**（日順）・markers 8。
  - **B にフォーカス → B=opacity0.85 / A,C=0.15**（タップで他が暗く）。
  - D(1枚) を含む3日 → **D は線なし**（線は2本・markers 6）。
  - 写真入口 A → **day モード・単一日→空色2レグ（#7EC8E3/#BFE3F0）**・🎲非表示・tlBar「📅 2024/05/10」。
  - 全部を見る → 全GPS9・v58で3本・📅復活。**🎲連打8回＋即close でクラッシュ無し**。コンソールエラー無し。BUILD `phase3.15`。

**教訓**
- 入口（ヘッダ/写真/フィルタ）が違っても、地図の表示は結局「**どの日を出すか**」に帰着する。状態を `pickDays`（日の集合）に集約したことで、ヘッダ=3日 / 写真=1日 / 将来の飛び飛び複数日 が同じ口に乗る（[[map-view-unparked]] の「状態1箇所集約」の徹底）。
- leaflet + markercluster は**アニメ中の fitBounds が重なると落ちる**。フィルタのような頻繁・連続再描画では `animate:false` が安全（スナップして UX もむしろ機敏）。

**残課題 / 次の方向**
- **次の周回（ユーザー要望・保留中）**: **飛び飛びの複数日フィルタ**（連続 range でなく「表示する日の集合」を任意に選ぶ＝2回の旅行の軌跡を重ねる）。`pickDays` の集合を UI から編集できるようにする延長で実装可能（今回その土台はできた）。
- 実機で① ランダム3日が「軌跡から思い出す」体験になるか／3日が地理的に離れすぎて各軌跡が小さくならないか（タップでその日へズーム寄せする案）② 1本タップ→他を暗く、の操作感（線が細くタップしづらくないか）③ 写真入口→その日の手触り。
- 色の日順は dayKey 文字列ソート（月が1桁/2桁跨ぎで厳密な時系列とずれうるが3日の色割当のみで実害なし）。

## v63 — 一日のみ表示のとき、線を時刻→空の色に (2026-06-16)

**背景**
- ユーザー案「**一日のみ表示したとき、線の色を空の色に寄せる**のはどうだろう」+ 9 つの時間帯→色の対応表 (深夜=藍/朝焼け=オレンジ/青空/夕焼け=茜/夜=濃紺…)。
- 単一日の動線は v58 のパレット色 (本来は「昔→今」を複数日で読ませる軸) では意味が乗らない。**その日の光の移り変わり**で塗れば、1日の歩きが「夜明け→昼→夕焼け→夜」として手触りで蘇る ([[editing-triggers-reminiscence]] と同じ「実用が裏でコアを強める」筋 = 時刻という実データが よみがえる を撃つ)。

**設計判断**
- **適用は「一日のみ表示」に限定**。表示中の線 (`mapState.lineGps`) が**単一の暦日に収まる時だけ** `skyColor(時刻)` でグラデーション。複数日表示は **v58 のパレット (昔→今を色で読む) を据え置き** (全日が同じ空グラデになると日の区別が消えるため)。判定は `new Set(lineGps.map(dayKeyOf)).size === 1` の1行で、3つの入口 (seed の近傍が単一日 / 📅 単日フィルタ / タイムライン日付タップ) すべてに自動で効く。
- **塗り方は per-leg**: 連続する2点ごとに短いポリラインを引き、**出発点の時刻の空色**で塗る → 線が朝→夜へ連続的に変化。`skyColor(d)` は 9 バンドの単純な hour 分岐 (ユーザー表をそのまま実装、14-17時は `#9DB8C9` を採用、`#E8C97A` は代替)。
- **矢印は薄い白** (`rgba(255,255,255,0.65)`) に変更。空色の上で進行方向が読めるように (パレット時は線色と同色のまま)。線の太さは単一日だけ 3→4 にして主役感を出す。

**結果 / 観察**
- preview E2E green (制御テストデータ・DB 非汚染):
  - `skyColor` 9 バンド全て表通り (0h=#1B2A4A … 23h=#23304F)。
  - 京都を1日歩く7点 (5/7/9/12/16/18/20時) → **6レグが #4A4E7C→#F4A36C→#7EC8E3→#BFE3F0→#9DB8C9→#E8743B** = 各レグが出発点の時刻色 (朝焼け→昼→夕焼け)。
  - 連続2日の旅 → 空色レグ 0・**v58 パレット (#ff6b6b/#feca57) のまま** = 回帰なし。
  - browse で 📅 単日に絞る → 単一日判定で空色に切替 (seed 以外の入口でも発火)。
  - ページ応答・コンソールエラー無し。BUILD `phase3.14`。(スクショはキャプチャ側のタイムアウトで未取得だが、レグ色は描画レイヤを直接読んで確認。)

**教訓**
- 「複数日=昔→今(パレット) / 単一日=その日の光(空色)」のように、**同じ線でも表示スコープで意味の軸を切り替える**と、各スコープで一番効く読ませ方ができる。判定を `lineGps` の暦日数1本に集約したので、入口 (seed/📅/日付タップ) を増やしても自動で正しく分岐する ([[map-view-unparked]] の「状態を1箇所に集約」と同じ型)。

**残課題 / 次の方向**
- 実機で① 空色の移り変わりが「その日」を蘇らせるか ② per-leg の境界が階段状に見えないか (滑らかにするなら時刻で線形補間も可) ③ 線色の意味 (空=時刻) を凡例なしで気づけるか (気づきにくければ単日時に小さなヒントを検討、[[ui-minimalism-works]] で吟味)。14-17時の色は `#9DB8C9`/`#E8C97A` どちらが手触りに合うか実機で。

## v62 — 入り口で変わる地図: 偶然の1スライス / 写真の時空+意味 (2026-06-15)

**背景**
- ユーザーの問い「**位置情報のある写真が増えたときに、どこまで・どういう基準で表示するか**」。現状の地図は `visibleGps()` が「期間内の全 GPS 写真」を全部マーカー化して `fitBounds` で全部収める = **「全部見せる」**。枚数が少ないうちは良いが、増えると ①数千ピンのクラッタで想起が死ぬ ([[ui-minimalism-works]] の逆) ②線が国を貫く糸になる ③fitBounds が大陸スケールで無意味に、と破綻する。
- 相談の結論 = ユーザー案「**地図に入る入り口で基準を変える**」。ただの 🗺 ボタン = **偶然の1スライス** (A)。特定の写真からの入り口 = その写真の**時空周辺 + 意味が近いもの (時空無視)** が地図に現れる。さらに seed view の「開く広さ」は **(A) 時空に寄せて開く → 似た場所は後で『広げる』** を選択。

**設計判断**
- **seed = 入り口で決まる「どこまで」**。`mapState.seed` があれば その写真の近傍に絞り、無ければ従来の期間ブラウズ。**両入口とも「seed + その近傍」を出すだけ**で、違いは seed が偶然(ランダム)か明示か = **全件を撒く必要が消え、「どこまで」が自動で有界**になる (元の問いの構造的な解)。
- **近傍の定義は既存資産をそのまま投影**: 時空 = `spacetimeNeighbors(seed, pool, 24)` + seed 自身 / 意味echo = `meaningNeighbors(seed, pool, 12)` を時空集合と重複排除。**色は混ぜない** (ユーザー指定。色は地理的に飛びすぎる)。地図が連想ウォークの「空間レンダリング」になる。
- **視覚文法 (凡例なしで読み分け)**: 起点 = 強調リング (`.photo-marker-seed` 緑) / 時空近傍 = 通常ピン + **その日の線でつながる** / 意味echo = **線のない破線リングの単独ピン** (`.photo-marker-echo` 紫)。**線は時空近傍だけ** (`mapState.lineGps`、`drawTrips` の入力を時空集合に) → 「線あり=実際の足跡 / 線なし=似た場面の木霊」。
- **開く広さ = (A) 段階的**: 開幕は時空近傍だけ表示し bounds をタイトに。`🌍 似た場所 N` ボタンで意味echoを足し (showEcho)、`fitBounds` が echo を含めて広がり散らばりが見える。N = GPS のある意味echo数 (0 ならボタンを出さない)。AI 未 opt-in は `meaningNeighbors` が空 → 自然 degrade (時空だけ)。
- **入口の結線**: ヘッダ🗺 = `openMapView()` (引数なし) → GPS 写真からランダム1枚を seed (偶然の1スライス)。explore 中心カード/フル画像/popup = `openMapView(photo)` → その写真 seed。**`btnMap` の click 配線を `() => openMapView()` に修正** (旧 `openMapView` 直渡しは click Event が focus 引数に入る = seed 化で壊れるため必須)。
- **コントロールの入れ替え** (`updateCtrlMode`): seed モードでは 📅期間 / ⋯表示 を隠し、`🌍 似た場所 N` と `↩︎ 全部を見る` を出す。`全部を見る` (= `clearSeed`) で従来ブラウズへ。タイムラインのフィルタバーも seed 時は「🌱 この写真の周り / ✕ 全部を見る」に。
- **タイムラインは全件のまま** (seed 時も淡色化しない) = 次の起点を選ぶ索引。項目タップは従来どおり (地図にあればフォーカス / 無ければフル画像)。**日付見出しタップは seed を抜けて期間ブラウズへ** (日付で絞る = スライスから出る、と解釈)。

**採用しなかった案**
- (B) 最初から echo 込みで全部 fit → 遠い echo があると seed 単位で広域ズームに戻り原問題が再発。(C) 意味を地図に出さない → ユーザーの「意味も地図に」を諦める。→ (A) 段階的が「偶然似た場所がよみがえる」段差も作れる。
- 意味の近さに**閾値は設けず上位N枚** (既存の意味モードと同じ)。小ライブラリでは無関係も混じりうるが、実スケール (数千枚) では上位12は本当に近い。閾値の magic constant を増やさない判断。

**結果 / 観察**
- preview E2E (制御テストデータ11枚: 京都の旅5 + 遠方echo3 + 無関係2 + GPS無し1、合成embedding、dbPut せず allPhotos 直挿しで DB 非汚染)。**全 green**:
  - 起点 p1 で開く: seedSpacetime=6(seed+5, GPS無し含む)/ markersOnMap=5(GPS時空)/ lineGps=5 / 📅⋯=display:none / 🌍似た場所5 / ↩︎全部を見る / tlBar「🌱 この写真の周り ✕全部を見る」/ seed・echo マーカーのアイコン html に `photo-marker-seed`/`photo-marker-echo` クラス。
  - 🌍 展開: showEcho=true / markersOnMap=10(時空5+echo5) / **lineGps=5 のまま (線は時空のみ)** / ラベル「似た場所を隠す」/ echo ピン=破線紫。
  - 全部を見る: seed=null / markersOnMap=10(全GPS) / 📅⋯ 復活・🌍↩︎ 非表示 / tlBar「全期間」。
  - ヘッダ経路 (`openMapView()` 無引数): ランダム GPS 写真で seed 化・📅 非表示・全部を見る表示。
  - コンソールエラー無し。スクショで「日本付近にクラスタ + パリ/沖縄に破線紫の echo が散る」絵を確認。BUILD `phase3.13`。

**教訓**
- 「増えると破綻する表示」は**表示量を削るより『起点を1つ選ぶ』で有界化**できた。全件を1画面に出す前提を捨て、入口ごとに seed を1つ持たせると、量の問題が自動で解ける ([[ui-minimalism-works]]: 余計を削ると想起されやすい、の地図版)。
- 既存の連想軸 (`spacetimeNeighbors`/`meaningNeighbors`) は explore 専用ではなく**「写真→近傍集合」の汎用関数**。地図はそれを空間に投影しただけで新ロジックほぼ不要。軸を関数として切り出しておくと別表面に再利用できる。
- markercluster は近接ピンを束ねるので、seed の強調リング等は**クラスタ展開前は DOM に出ない**。クラス付与の検証は描画 DOM でなく**マーカー生成時の icon html** を見ると確実。

**残課題 / 次の方向**
- **実機で手触り**: ①ヘッダ🗺の「偶然の1スライス」が毎回違って楽しいか/ lone pin (近傍ゼロの写真) に当たった時の寂しさ ②`🌍 似た場所` 展開の驚き (意味echoが本当に「似てる」と感じるか・AI解析済み前提) ③時空タイト開幕の親密さ vs 全体を見たい欲求のバランス。
- **次の芽**: マーカー popup から「🎯 この写真を中心に」で**地図内 re-seed** (今は timeline 経由 or 一度 explore へ)。意味echoに similarity 閾値 (実スケールで無関係が混じるなら)。seed 中の位置/日付編集で「スライス外の写真」を触った時の挙動整理。**「直す→そのまま連想ウォーク」**([[editing-triggers-reminiscence]]) と合流。

## v61 — 地図のマーカー / タイムラインから画像を削除 (2026-06-15)

**背景**
- ユーザー「地図を開いている時に写真を削除する動線がない。マーカーの『連想ウォーク・位置を直す』の下に『画像を削除』がほしい。タイムラインにも削除ボタンがほしい」。
- 削除 (= 非破壊の `excludePhoto` 除外、♻️ で復元可) は今までカードの ✕ だけだった。地図文脈には削除導線が無く、よみがえってほしくない1枚をその場で消せなかった。

**設計判断**
- **既存の `excludePhoto` (excluded フラグ・非破壊・トースト取消・♻️復元) をそのまま流用**。新しい削除概念は作らない。マーカー popup に「🗑 画像を削除」、タイムライン各項目に「🗑」を追加し、どちらも `excludePhoto(p)` を呼ぶだけ。
- **地図同期を `excludePhoto`/`undoExclude` 側に集約**: 末尾で `if (mapState) mapState.refreshData()` を呼ぶようにし、除外/取消したら**地図が開いていればマーカー・タイムラインから即消える/戻る**。各呼び出し元に散らさず1箇所で面倒を見る (位置/日付編集の refreshData と同じ型)。
- **トーストの z-index を 200→1700 に引き上げ**: 地図 overlay (150) や下部シート (1100)・コントロール (〜1600) より前面にし、**地図を開いたままでも「取り消す」が見えて押せる**ように。full 画像 (2000) より下なので競合しない。
- タイムラインは項目右側のボタンを `.tl-actions` でまとめ (位置を直す + 🗑)。🗑 はアイコンのみ + `title="削除（♻️で戻せる）"`、赤系で削除を示唆。

**結果 / 観察**
- preview E2E (dbPut スタブ・map 開いた状態): ①タイムライン各項目に 🗑、マーカー popup に「🗑 画像を削除」②タイムライン 🗑 で対象が excluded → **マーカーもタイムライン項目も即消える**・dbPut 記録・トースト表示③トースト z-index=1700④「取り消す」で excluded=false → **地図にマーカー復活**・トースト消滅⑤popup の 🗑 も同様。コンソールエラー無し。BUILD `phase3.12`。
- トーストは 5 秒で自動消滅 (既存仕様)。eval 往復が 5 秒超だと消えて見えるが、単一同期実行で表示・取消とも確認済。

**教訓**
- 「同じ操作 (除外) を複数の入口 (カード✕ / popup / タイムライン) から」出す時は、**副作用 (地図リフレッシュ) を操作関数側に集約**すると入口を増やすほど楽になる (各入口は `excludePhoto(p)` を呼ぶだけ)。位置(v59)・日付(v60)・削除(v61) すべて「データ更新 → refreshData」の同じ型。
- overlay を重ねる設計では、トーストのような一時 UI の z-index も「どの overlay の上に出すべきか」を明示しないと最前面 overlay の裏に隠れる (v54 のフル画像 z-index と同じ教訓)。

**残課題 / 次の方向**
- 実機でタイムライン項目のボタン過密 (位置を直す + 🗑) が窮屈でないか (狭幅端末)。将来: 複数選択して一括削除 / フル画像にも削除導線。

## v60 — 写真の日付も後から直せる (フル画像から日時を編集) (2026-06-15)

**背景**
- v59 (位置の後付け) の実機フィードバック「**位置を直す行為自体が当時の記憶を想起させる**」([[editing-triggers-reminiscence]]) を受け、ユーザー「同じ発想を**日付**にも。日付がおかしい/抜けてる写真を直せるように」。
- 保存日 (mtime) で取り込んだ写真 (スクショ・フォルダ・SNS) は日時が「今」寄りに固まりがちで、タイムライン/地図の並びが実体験とズレる → 本当の日時に直せるように。

**設計判断**
- **編集の入口はフル画像ビュー** (`showFullImage`)。理由: 位置は地図 (ピン=空間メタファ) が要るが、日付は日時入力でよく**地図に依存しない**。フル画像は日時を大きく出す場所で、random/explore (長押し)・タイムライン (GPS無し項目タップ)・マーカー popup (サムネタップ) の**どこからでも到達**できる = 全文脈で直せる。タイムライン項目をこれ以上混雑させない判断も兼ねる。
- フル画像のキャプションに **「📅 日付を直す」** ボタン → インラインの `<input type="datetime-local">` (既存日時をプリフィル) + 保存/キャンセル。日付だけでなく時刻も (旅程内の並び順に効く)。`datetime-local` は iOS Safari ネイティブピッカーで堅い ([[map-view-unparked]] の date 入力と同系統)。
- **保存** (`de-save`): `photo.datetime` を新日時に、`dateSource='manual'` (出所タグ "手動" を新設) → `dbPut` 永続。`mapState.refreshData()` (地図が開いていればタイムライン再ソート・線の日ごと色を再計算) + `render()` (本体のカード日付・時間フィルタ) + `showFullImage(photo)` で**新しい日付で開き直す** (古い closure を残さずクリーンに再描画)。
- `datetime-local` の往復は `toDatetimeLocalVal`/`fromDatetimeLocalVal` (ローカル時刻でパース、タイムゾーンずれを避け手動 `new Date(y,m,d,h,mm)`)。

**結果 / 観察**
- preview E2E (dbPut スタブで実DB非汚染): ①ヘルパー往復一致②保存日写真のフル画像に「📅 日付を直す」+ 出所タグ "保存日"③押すとエディタ表示・現日時プリフィル④新日時保存で `datetime`+`dateSource='manual'`・`dbPut` 記録・**フル画像が新日付+"手動"タグで開き直す**⑤**地図を開いた状態で**「今」寄り(2026)の保存日写真を 2023-05-01 に直すと**タイムラインが日付順の正しい位置に並び替わった** (05/02→05/01 12:00→05/01 10:00)。コンソールエラー無し。BUILD `phase3.11`。

**教訓**
- 「直す」系は **対象が要求するメタファに合わせて入口を変える** のが自然: 位置=地図のピン (空間)、日付=フル画像の日時入力 (時間)。同じ "編集=想起" でも UI は別。フル画像は「1枚に集中する場所」なので、その写真のメタデータ編集ハブとして据わりがよい。
- 編集後の整合は「データ更新 → `mapState.refreshData()`(地図) + `render()`(本体) + 表示中ビューの開き直し」の3点セットで漏れなく。位置(v59)・日付(v60)で同じ型。

**残課題 / 次の方向**
- 実機で①日時ピッカーの操作感②直した後にそのまま連想ウォークへ繋ぐと「思い出した→辿る」の波に乗るか ([[editing-triggers-reminiscence]] の次の芽)。将来: 名前/キャプション編集 / "手動" を出所タグ以外でも示す / タイムラインから直接フル画像を開く動線 (今は GPS 写真はマーカー経由)。

## v59 — 写真の位置を後から設定/修正 (地図のピンをドラッグ) (2026-06-15)

**背景**
- ユーザー「タイムラインの画像の位置情報を修正・追加できるようにしたい。撮ったのに位置情報がたまたま入ってない写真がある。**できれば地図上のピンを動かすことで位置を入力**できるとやりやすい」。
- 取り込み源によっては GPS が剥がれる/元から無い (スクショ・SNS・古い機種)。色/意味の連想には乗るが地図・軌跡には出ない → 後付けで救えるように。

**設計判断**
- **タイムラインの各項目に「📍 位置を設定 / 位置を直す」ボタン**を追加 (GPS無し=設定[青系で促す] / 有り=直す)。マーカー popup にも「📍 位置を直す」。= 「修正(既存GPS)」も「追加(GPS無し)」も同じ導線。
- **地図にドラッグ可能なピンを出して位置入力** (`startLocationEdit`)。ピンはサムネ入りの divIcon (draggable)。**ドラッグでも地図タップでも移動**でき、下部の編集バーに座標/地名を実時間表示 + 保存/キャンセル。モバイルは起動時にタイムラインシートを畳んで地図を見せる。
- **初期位置を賢く** (`nearestGps`): 既存GPS → なければ**時間的に最も近いGPS写真(3日以内)の座標** → なければ地図中心。旅行中に1枚だけ GPS が抜けたケースを「ほぼ正しい位置から微調整」で救える (3日以内に同行写真があれば一発)。
- **保存** (`saveLocationEdit`): `photo.lat/lng` を上書き + `geoManual=true` を立て `dbPut` で永続。場所封印に該当しうるので `_sealed` を再計算。`mapState.refreshData()` でマーカー追加・タイムライン更新 (ボタンも「位置を直す」に変化)・本体 `render()` を整合。付けた位置へ寄せる。
- `startLocationEdit` は `mapState` に載せ、top-level の `buildTimeline`/`buildMarkerPopup` から `mapState.startLocationEdit(p)` で呼ぶ (地図の overlay スコープ依存を避ける)。編集中は `map` の click でピン移動 (既存の closePops と併存)。

**結果 / 観察**
- preview E2E (dbPut スタブで実DB非汚染): GPS2枚(同旅行)+GPS無し1枚(同日近接) を投入 → ①タイムラインで GPS無しだけ「📍 位置を設定」、有りは「位置を直す」②編集開始で**ピンが近接GPS写真の位置から出る**(nearestGps 3日以内)・バー表示・座標読み取り③ピン移動→保存で `lat/lng`+`geoManual` が入り `dbPut` 記録・**地図にマーカー追加**(filteredGps に合流)・タイムラインのボタンが「位置を直す」に変化④マーカー popup に「📍 位置を直す」⑤キャンセルは位置不変・dbPut せず。コンソールエラー無し。BUILD `phase3.10`。
- **preview 制約**: 地図 0×0 のためピンの実ドラッグ操作感・タイル上での見え方は実機待ち (ロジック・状態遷移は green)。

**教訓**
- 「位置の後付け」は単独だと面倒だが、**時間的近傍の GPS から初期値を引く**だけで「ほぼ合ってる→微調整」になり実用度が跳ねる (旅行は連続だから当たりやすい)。reminiscence の文脈 (連続した旅行) がそのまま初期値の精度に効く。
- 地図 overlay 内のロジックを `mapState` に載せると、top-level の描画関数 (タイムライン/popup) から疎結合に呼べる。

**残課題 / 次の方向**
- 実機で①ピンのドラッグ操作感 (タイルの上で掴みやすいか)②近接初期値が「当たる」率③タップ移動とドラッグ移動の使い分け。将来: 複数枚まとめて同じ場所に / 住所文字列から検索 (逆ジオを使わない方針なので要検討) / `geoManual` を popup や出所タグに表示。

## v58 — 線のつなぎを1日固定 + 複数日は「日ごとに色が変わる連続線」に (2026-06-15)

**背景**
- ユーザー（v57 を見て）「**線のつなぎ方は複雑になるだけ**。線のつなぎは**1日**。複数日表示するときには、**線の色がかわる方式**にしましょう。記憶の封印が表示の中にある UI は継続」。
- v57 で「旅行の区切りを日数で選ぶ」セレクタ (1日〜ぜんぶ+日ごと) を入れたが、**選択肢 UI 自体が複雑**だった → 固定化し、複数日は色で読ませる方向へ。

**設計判断**
- **線のつなぎを「1日」で固定** (`TRIP_GAP_MS=86400000`)。1日以上あいたら別の線 (= 別の旅行)。セレクタ (`LINE_MODES`/`lineModeIdx`/`segmentByMode`/`segmentByDay`/`popLine`/チップ/localStorage `pms-lineMode`) を**全廃**。
- **複数日にまたがる旅行は1本の連続線として描き、日が変わるごとに色を変える** (`drawTrips` 書き換え)。実装: 旅行 (gap>1日で区切った塊) の中を「暦日」でサブ線に割り、各サブ線は**前日の最後の点から引いて橋渡し**することで見た目は1本の連続線のまま、色 (`TRIP_COLORS`) だけ日替わり。色は旅行内の日インデックス (0,1,2…) で割り当て、**旅行ごとにリセット** (どの旅行も初日が同色 = 「N日目」が色で分かる)。= 「昔→今」が色で読める。`dayKeyOf(p)` を新設。
- **地図上部は「📅 期間 + ⋯ 表示」のまま**。⋯ メニューから線項目を外し、**今は「🔒 記憶の封印」のみ**を残す (ユーザー「封印が表示の中にある UI は継続」)。1項目でもメニュー構造は維持 (将来また足せる / 封印の置き場所を変えない)。

**結果 / 観察**
- preview E2E: 3日連続の旅行 (各日2枚) + 3ヶ月後の1日 (別旅行) を投入 → ①Trip A = **3本のサブ線 (🔴day1 / 🟡day2 / 🔵day3) で各サブの始点が前日の終点と一致**（= 1本の連続線として日替わりで色変化）②Trip B = 別の線で🔴から再開 (旅行ごとに色リセット)。③UI = トップは 📅+⋯ のみ、⋯ メニューは「🔒 記憶の封印」1項目、⋯→🔒 で封印パネル。コンソールエラー無し。BUILD `phase3.9`。
- **preview 制約**: 地図 0×0 のため範囲封印の実描画・線の実見た目は引き続き実機待ち（論理は green）。

**教訓**
- v57→v58 で「選ばせる」をやめ「固定 + 色で読ませる」に倒した。**設定の選択肢を足すより、良い既定 + 視覚エンコード (色=日) のほうが手触りがシンプル**。ユーザーの「複雑になるだけ」は UI 選択肢への一貫した忌避 signal ([[ui-minimalism-works]])。
- 連続線で色だけ替えるには「サブ線を前点から橋渡し」が要 (各日を独立に描くと日境界に隙間ができる)。

**残課題 / 次の方向**
- 実機で①日替わり色が「昔→今」の流れとして読めるか / 旅行内の色数が多い (8日超で色一巡) 時の見え方②1日固定の区切りが旅行の体感に合うか (中日に写真ゼロだと割れる)。色を連続グラデーション (categorical でなく) にする案は引き続き候補。

## v57 — 線=旅行ごとに1本 (日数で区切り選択) + 地図UIを「📅 + ⋯」に整理 (2026-06-15)

**背景**
- ユーザー「つなぐ線も**1日単位で選びたい**。2日の旅行も5日の旅行もある。**旅行の動静をすべて一本でつなぎたい**」。+「マップの**日ごとと🔒は基本使わない**。マップ上は**期間ともう一つのアイコン**（タップで日ごとと🔒を選べる）にしたい」。
- 既定 `日ごと` は暦日ごとに線を割る → 数日の旅行が日数分の線に分断され「旅行の軌跡を1本で見る」体験にならなかった。

**設計判断**
- **線モードを「旅行の区切り＝日数」に再設計** (`LINE_MODES`)。既存の gap ベース分割 (`segmentByGap`: 前の写真から gapDays 以上あいたら別の線) をそのまま使い、選択肢を **1日 / 2日 / 3日 / 5日 / 1週間 / ぜんぶ / 日ごと** に。**既定 = 2日**（連続した旅行の動静を1本につなぐ。毎日撮る旅行なら内部 gap < 2日で連結、別の旅行とは数日以上あくので分離）。`日ごと` は「基本使わない」要望どおり**末尾**に退避（暦日分割は残す）。
- **選んだ区切りを localStorage 永続** (`pms-lineMode`)。従来は module スコープでリロードで戻っていた → ユーザーが選ぶ前提なので覚える。
- **地図上部を「📅 期間」+「⋯ 表示」の2つに整理**。よく使わない `線` と `🔒封印` を常時トップに置かず、**⋯ → メニュー（〰 線のつなぎ方 / 🔒 記憶の封印）** に畳む。📅 だけは主役なのでトップ維持。`⋯` メニューの線項目ラベルは現在の区切りを表示（例「〰 線のつなぎ方：2日」）。
- 実装: トップの `mcLine`/`mcSeal` ボタンを廃し `mcMore` 1つに。`togglePop` を廃して 📅/⋯ 各々の開閉ハンドラに。封印パネルの開封は `elSealBtn` ハンドラ → `openSealPanel()` に切り出して ⋯ メニューから呼ぶ。`closePops` は `popMore` も閉じる。情報設計は [[ui-minimalism-works]]（デフォルトは最小、使う時だけ広がる）に沿う。

**結果 / 観察**
- preview E2E: ①分割ロジック — 2日旅行(4枚)+5日旅行(毎日1枚計5枚) が **1日/2日/3日/5日/1週間 すべて `[4,5]`（各旅行1本）**、ぜんぶ=`[9]`(1本)、日ごと=`[2,2,1,1,1,1,1]`(7本=避けたい過分割)。②地図UI — トップは 📅+⋯ のみ（mcLine/mcSeal 削除確認）、⋯ メニューに2項目、線→7チップ(2日選択中)、3日選択で `lineModeIdx`/localStorage 更新+自動クローズ、⋯→🔒で封印パネル。コンソールエラー無し。BUILD `phase3.8`。
- **preview 制約**: 地図 0×0 のため範囲封印の実描画は引き続き実機待ち（v56 と同様）。

**教訓**
- 「線の単位」は暦日でなく**旅行（gap で区切る連続塊）**が自然。日数の閾値を1つ選ばせるだけで「2日の旅も5日の旅も1本」が同時に成立する（旅行内の gap < 閾値 < 旅行間の gap）。プリセット(3日/1週間)より1日刻みのほうがチューニングしやすい。
- 地図のトップは増やすほど散る。**主役(期間)だけ残し、たまに使う設定は1アイコン裏のメニューに畳む**と最小に保てる。封印パネルの開封ロジックを関数に切り出しておくと入口の付け替えが楽。

**残課題 / 次の方向**
- 実機で①既定 2日 が旅行の体感に合うか（短すぎ/長すぎ）/ 多日旅の中日に写真ゼロの日があると割れる点②⋯ メニューの発見性（線・封印にたどり着けるか）。色を時間グラデーションにして昔→今を線で補強する案は引き続き候補。

## v56 — 記憶の封印: 場所×期間を指定して非表示にできる (2026-06-14)

**背景**
- ユーザー要望「**記憶の封印** — 場所・時期を指定して非表示にできる」。
- コア (偶然よみがえった久しぶりの記憶) と表裏: 偶然よみがえる体験は、**よみがえってほしくない記憶 (別れた人・つらい時期・思い出したくない場所) の不意打ち**リスクと裏表。封印できると「偶然の再会」を安心して楽しめる = コアを**守る**機能。

**設計判断**
- **ルール型 (materialize でなく条件保存)**: 封印は `{from, to, bounds, label}` を `localStorage('pms-seals')` に保存し、`recomputeSealed()` で各写真に `_sealed` を貼る。利点: ①以後の取り込みにも自動適用 (取り込んだ瞬間に `addPhotos` で `_sealed` 付与) ②一覧から条件ごと解除 ③ラベルで「何を封じたか」が残る。単なるフラグ materialize だと②③と自動適用を失うので不採用。
- **削除 (excluded) と別概念**: ✕除外/♻️は「この写真が嫌」(写真ごと・ゴミ箱)、封印は「この場所/時期を出さない」(条件・一覧)。混ぜると年単位の封印が削除一覧に溢れて混乱するため `_sealed` を独立フラグに。
- **隠す判定を `visible(p) = !p.excluded && !p._sealed` に集約**し、random/explore プール・地図・タイムライン・足跡・件数・AI 候補の `!p.excluded` を一括で `visible(p)` に差し替え (約14ヶ所)。漏れると封印写真がプールに漏れるので1関数に寄せた。
- **入口は地図**: 地名検索/逆ジオコーディングを持たない (座標を外に出さない方針) ため「場所」を意味的に選べるのは地図だけ。地図に既にある **場所=表示範囲 `map.getBounds()`** と **期間=`mapState.range` (📅フィルタ)** をそのまま流用。地図上部に 🔒 → パネル (期間表示 / 「📍見えている範囲だけ」チェック / 名前 / プレビュー件数 / 封印ボタン / 封印中一覧+解除)。
- **GPS 無し写真**: 「範囲だけ」封印は位置のある写真のみ対象。位置の無い写真は「場所問わず (期間だけ)」で封印できる (チェックを外す)。これで GPS 有無に関わらず封じられる。
- **全期間×範囲なし = 全部の封印**は禁止 (封印ボタン無効化)。最低1つの制約を要求。
- 封印追加/解除時は `render()` (背面の本体) + `mapState.refreshData()` (地図マーカー/線/タイムライン/封印一覧の張り替え) で両方更新。地図 overlay は `$main` 外なので render と独立。`applyFilter` は封印を都度反映するため `visibleGps()` で `allPhotos` から算出する形に変更 (旧: 開いた時点の `gps` const を filter)。

**結果 / 観察**
- preview E2E (合成5枚, dbput非汚染): ①件数判定 — 期間のみ=4 / 場所のみ=3 / 複合=2 / GPS無しは場所のみで非該当=false。②封印実行 — 該当2枚に `_sealed`、`visible` 3枚、random プールから封印分が消え該当外が繰り上がる。③地図UI — 🔒パネルが開きプレビュー「2枚」「名前=2019/3/1〜2019/3/31」、封印ボタンで実行→自動クローズ→ヘッダ「1枚」。④全期間に戻しても封印写真は地図/タイムライン/件数すべてで非表示。⑤解除で3枚復活・seals=0・ヘッダ「3枚」。コンソールエラー無し。BUILD `phase3.7`。
- **preview 制約**: headless は地図コンテナが 0×0 で `getBounds()` が一点に縮退 → 範囲封印は preview では0件になる (広域 bounds 指定では3件で論理確認)。**範囲封印の実件数は実機 (地図に実サイズ) で要確認**。

**教訓**
- 「隠す」系は判定箇所が散らばる (プール/描画/件数/AI)。最初に `visible(p)` の1関数へ寄せると、新しい隠し軸 (封印) を足すのが1行差し替えで済む。次に隠し軸が増えてもここに足せばよい。
- 封印は core を「広げる」のでなく「守る」機能。reminiscence の不意打ちリスクを下げることで、全ライブラリ投入 (久しぶり=全件) を安心して後押しできる ([[reminiscence-at-scale-works]] / [[storage-tradeoffs-accepted]] の延長)。

**残課題 / 次の方向**
- 実機で①地図の実 bounds による範囲封印が直感に合うか (ズーム量と封印範囲の関係) ②「封印した」安心感が実際に体験を良くするか ③封印一覧の解除動線が分かりやすいか。
- 将来: 地図に「封印エリアを薄く塗る」可視化 / 封印を期間プリセット (この年・この季節) から作る / 本体ヘッダからの封印一覧入口 (今は地図 🔒 のみ・ℹ️ に説明)。

## v55 — 地図で期間を絞るとタイムラインもその期間だけ表示 (2026-06-14)

**背景**
- ユーザー「マップで期間設定をしたら、**タイムラインを開いてもその期間のみ表示される**ほうが UI 的にいい」。

**設計判断**
- v51 ではフィルタ中、タイムラインの圏外項目は**淡色化 (`.dimmed`)** して残していた → **非表示 (`.tl-out` = `display:none`)** に変更。タイムライン = 「絞り込んだ期間の一覧」として読める。日付見出しも圏外は隠す。
- 空期間 (写真の無い範囲を date 入力で指定) のとき「**この期間に写真はありません**」の注記を出す (`emptyNote`、`updateTimelineUI` で可視判定)。
- マップ 📅 パネルでもタイムラインの日付タップでも同じ `mapState.range` を更新するので、**どちらから絞ってもタイムラインがその期間だけになる**(状態一元化の自然な帰結)。トレードオフ: タイムラインからの2タップ期間選択は圏外が隠れるため使えなくなるが、期間指定は 📅 パネル (date 入力) が担うので実害なし。`✕ 解除` で全期間に戻る。

**結果 / 観察**
- preview E2E: 全期間=5日/5件 → 📅 で `2023/11/12〜13` 絞り込み → タイムラインも**該当2日・2件だけ表示** (offsetParent で実表示を確認、圏外は非表示)、フィルタバーも同期。写真の無い `2020/01` → 「この期間に写真はありません」。`✕ 解除` で5日/5件に復帰。コンソールエラー無し。BUILD `phase3.6`。

**教訓**
- 「フィルタ中の圏外」は淡色か非表示かで体験が変わる。地図と連動する一覧では**非表示**のほうが「絞った期間を見ている」感覚に合う (淡色は全体俯瞰向き)。状態を 1 箇所 (`mapState.range`) に集約してあったので表示方針の切替は数行で済んだ。

## v54 — fix: 地図のタイムライン/マーカーからフル画像が出ない (z-index) (2026-06-14)

**背景**
- ユーザー「タイムラインで**位置情報なしをタップしても写真が大きくならない**」。

**設計判断 / 原因**
- タイムラインの位置情報なし項目タップは `showFullImage` を呼んでおり DOM 上は `.full-overlay` を生成していたが、**`.full-overlay` の `z-index:100` が地図オーバーレイ (`.map-overlay` z-index:150、コントロール類は〜1250) より低く、フル画像が地図の裏に隠れて見えなかった**。地図を開いていない通常画面 (random/explore の長押し) では地図が無いので顕在化せず、地図内 (タイムライン項目 / マーカーのサムネタップ) でだけ出るバグ。
- `.full-overlay` を `z-index:2000`、`.full-cap` を `2001` に引き上げ、地図オーバーレイと全コントロールより前面に。これで地図内のどこから開いてもフル画像が最前面に出る。

**結果 / 観察**
- preview E2E: 地図を開いて位置情報なし項目タップ → `.full-overlay` の `z-index=2000 > map 150`、かつ**画面中央の最前面要素がフル画像の `<img>`**（実際に見えている）。キャプションの出所タグ (保存日) も表示。コンソールエラー無し。BUILD `phase3.5`。

**教訓**
- overlay を `$main` の外に積む設計 (地図 / フル画像) では z-index の上下関係が事故りやすい。新しい全画面 overlay を足したら、既存の最前面 overlay (フル画像) がその上に来るか必ず確認する。

## v53 — 取り込みを初心者向けに1本化 + 日時の出所表示 (2026-06-14)

**背景**
- ユーザー「取り込みが複雑で初心者にわかりにくい。初心者は基本『画像』だけで足りる。基本は撮影日、無ければ最終更新日。ファイル/フォルダの下に**詳細設定**を入れて別モーダルで『EXIFあり画像のみ/全データ』『EXIFのみ/無いとき最終更新日』を選べるように。**既定=全取り込み・EXIF優先・無ければ最終更新**。あとで**どの日時情報を使ったか**画像で分かる表示が欲しい」。

**設計判断**
- **2系統 (📷写真=厳格 / 🖼️画像=寛容) → 1ボタン「📷取り込む」に統合**。メニューは `📄ファイル / 📁フォルダー / ⚙️詳細設定`。初心者はこれだけ。空状態も1ボタン化。
- **強弱は詳細設定 (別モーダル) に退避**。ラジオ2択に集約: 「**すべての画像を取り込む**（撮影日があればそれ・無ければ保存日）」=既定 / 「**撮影日(EXIF)のある画像だけ**（保存日は使わない）」。ユーザー列挙の4項目（EXIFあり画像のみ/全データ/EXIFのみ/無いとき最終更新）は、実際に成立する挙動が2つ（寛容/厳格）なので2択に整理（全データ×EXIFのみ=日時無し取り込み=タイムライン破綻で不可）。`importMode` を localStorage 永続。
- 取り込みフローは `importMode` から `importFallback` を都度導出（`all`→保存日フォールバックあり / `exifOnly`→EXIF 必須）。`importOne` の二系統ロジックはそのまま流用。
- **日時の出所 `dateSource` を記録**（`'exif'`=撮影日 / `'mtime'`=保存日 / `'posted'`=投稿日(IG/X)）。`importOne` で確定。画像を見る場所すべて（ランダムカード / 連想ウォーク中心カード / フル画像キャプション / 地図マーカー popup）の日時に小さな**出所タグ**（撮影日/保存日/投稿日）を添える。旧データ（`dateSource` 無し）はタグ非表示。
- ℹ️ 使い方も新方式に更新。📦(IG/X zip) は上級者向けとして残置。

**結果 / 観察**
- preview E2E: ヘッダ1ボタン「📷取り込む」（旧2ボタン消滅）/ メニュー3項目 / 詳細設定2択は `importMode` 反映・localStorage 永続。`importOne` を dbPut スタブで検証 → EXIF無し+寛容=`mtime`(2021)・EXIF無し+厳格=`no-datetime`スキップ・override=`posted`(2019)。表示: カード/中心/フルに「撮影日/保存日/投稿日」、legacy はタグ無し。コンソールエラー無し。BUILD `phase3.4`。

**教訓**
- ユーザーが挙げた選択肢が一見4つでも、実際に成立する挙動は2つということがある。**broken な組み合わせを作らない2択**に整理する方が「初心者にわかりやすく」の意図に沿う。
- 「どの情報源の日時か」は記録時に1フィールド (`dateSource`) 足すだけで、表示は共通ヘルパ (`srcTagHtml`) を各所に差すだけで済む。後付けでも安い。

**残課題 / 次の方向**
- 旧データ（出所不明）に遡及タグは付けられない（前方互換のみ）。実機で「初心者がメニュー1本で迷わず入れられるか」「出所タグが連想の邪魔にならないか（[[ui-minimalism-works]]）」。

## v52 — 地図上にフィルタ/線UI (アイコン→パネル) + ♻️の件数を撤去 (2026-06-14)

**背景**
- 2要望:
  - 「日付/期間の絞り込みを**タイムラインからだけでなくマップから直接**やりたい。マップ上に**日付/期間フィルタのアイコン**と**線の日数のアイコン**を置き、開いて選べるUIがいい」。
  - 「連想ウォーク画面の**♻️(リサイクル)横の数値は不要**。削除一覧モーダル側の件数はそのままでよい」。

**設計判断**
- **常時表示の線チップ列 → アイコン2つ + 開くパネル**に変更。地図上部中央に `📅{現在の絞り込み}` と `〰{現在の線モード}` の2ボタン。タップで各パネル（ポップオーバー）が開く。普段は状態をボタンに要約表示し、操作時だけ広がる（[[ui-minimalism-works]]）。
  - **📅 日付/期間パネル**: `開始`/`終了` の `<input type=date>`（iOS ネイティブピッカー）+ `適用`/`全期間`。片方だけでもOK（開始のみ=その日以降 / 終了のみ=その日まで、`dataMin/Max` で補完）、逆指定は入れ替え。
  - **〰 線パネル**: 既存の `日ごと/3日/1週間/ぜんぶ` チップ。
- **状態は一元化**: マップのパネルもタイムラインの日付タップも同じ `mapState.range`/`lineModeIdx` を更新し `applyFilter()`/`drawTrips()` に集約。`applyFilter` から `updateTimelineUI` と `updateCtrlLabels` の両方を呼ぶので、**どちらから絞ってもボタン表示とタイムラインのバーが同期**する。地図タップ (`map.on('click')`) でパネルを閉じる。
- **♻️ の件数を撤去**: `updateTrashBtn` を `♻️${n}` → `♻️` に。連想ウォーク中に除外件数が目に入るのが余計、との指摘。0 件で隠す挙動と、削除一覧モーダルの件数表示 (`🗑 削除した写真 (N)`) はそのまま。

**結果 / 観察**
- preview E2E: 📅 パネルで `2023-11-12〜13` 適用 → ボタン `📅 11/12〜13`・4マーカー・日ごと2線、タイムラインのバーも同期。〰 パネルで `3日` → 1線・ボタン `〰 3日`。`全期間` 解除後にタイムラインの日付タップ → 📅ボタンが `📅 5/3` に同期。地図タップでパネルが閉じる。♻️ はアイコンのみ・モーダルは `(1)` 表示維持。コンソールエラー無し。BUILD `phase3.3`。

**教訓**
- 同じ状態を複数の入口（マップのパネル / タイムラインの日付タップ）から操作させる時は、入口ごとに状態を持たず**単一の `mapState.range` に集約**し、更新後に全 UI（マーカー/線/bounds/タイムライン/ボタンラベル）を一括リフレッシュする関数を1本通すと破綻しない。
- 「常時チップ」→「アイコン+パネル」は、現在値をアイコンに要約表示すれば情報量を増やさずに操作の幅だけ広げられる。

**残課題 / 次の方向**
- 実機で `<input type=date>` の手触り（iOS のネイティブピッカーで期間指定がやりやすいか）。月/年単位のクイック選択（「2023年11月」等）が欲しくなるか。

## v51 — 地図: 日付/期間フィルタ + 連想ウォークの座標表示を撤去 (2026-06-14)

**背景**
- v50 へのフィードバック + 2要望:
  - 「**日にち、または期間を限定した画像・移動線だけを表示**できるようにしたい」（軌跡が多いと全部出て見づらい → 見たい日/期間に絞りたい）。
  - 「連想ウォーク中は『🗺 地図でこの場所を見る』が増えたので、**📍マークと経度緯度の表示は余計**」。

**設計判断**
- **フィルタ ≠ 線の日数**。v50 の「線の日数」は線の *つなぐ粒度*、今回は *表示する日付/期間* の絞り込み（別軸）。両者は合成可能（例: 期間=2日 × 線=3日 → その2日が1本）。
- **タイムラインの日付見出しタップで絞り込む**（既に日付グループなので操作の主役にする）。カレンダー的レンジ選択: なし→その日（単日）/ 単日で別の日タップ→期間レンジ / 単日で同じ日タップ→解除 / 期間でタップ→新しい単日。`mapState.range={start,end}`（ms、null=全期間）。タイムライン先頭に固定の**フィルタバー**（`全期間` or `📅 日付`/`📅 開始〜終了` + `✕ 解除`）+ 圏外アイテムの淡色化 + 選択日の緑ハイライト。
- **マーカー/動線/bounds をフィルタに追従**。`applyFilter()` で `cluster.clearLayers()`+`markerById.clear()`→絞り込んだ集合で再投入、`drawTrips()`（filteredGps を読む）、`fitBounds`(絞り込み後)。cluster/markerById は**作り直さず中身入れ替え**（タイムライン側 closure の参照を保つため）。
- **写真タップの判定を「いま地図に出ているか」に変更**: `markerById.has(id)` で、フィルタ内の GPS 写真→地図フォーカス / 圏外 or GPS無し→フル画像。フィルタで隠れたピンに飛んで空の地図を見せる事故を防ぐ。
- **連想ウォーク中心カードの座標撤去**: `📍 lat,lng` 行を削除。raw 座標は人に意味が薄く、写真への集中を逸らす情報だった（[[ui-minimalism-works]]）。位置は「🗺 地図でこの場所を見る」が担う。マーカー popup の座標は地図上の文脈として残置。

**ハマったところ**
- フィルタで markers を張り替えるとき `markerById` を新 Map に差し替えると、`buildTimeline` の click closure が古い Map を握って壊れる → **同じ Map/cluster を clear→再投入**して参照を保つ方式に。
- フィルタバー（sticky top:0）と日付見出し（sticky）が重なる → 見出しを `top:33px` にしてバーの下に貼り付く。

**結果 / 観察**
- preview E2E（東京1日 / 京都→大阪 連続2日 / 札幌単日 / GPS無し）: 全期間=7マーカー/3線 → **東京単日タップ=2マーカー/1線・選択1日・圏外6件淡色** → 解除で復帰 → **京都→大阪で期間レンジ=4マーカー・日ごと2線、線モード3日で1線に連結**。圏外アイテムタップでフル画像。中心カードは座標が消え日時+🗺 のみ。コンソールエラー無し。BUILD `phase3.2`。

**教訓**
- 「絞り込み」と「つなぐ粒度」は混ぜず別軸の操作にして合成可能にすると、少ない UI で表現力が出る。日付グループ済みのタイムラインを**そのままフィルタ UI に転用**できたので追加要素は最小（バー1本 + 見出しを tappable に）。

**残課題 / 次の方向**
- 実機で日付タップ→期間レンジの操作が直感的か（2タップ目で範囲、の発見可能性）。期間が長い時の見出しスクロール量。フィルタ中の「線の日数」併用が混乱しないか。

## v50 — 地図: 線の日数を選択可 + 連想ウォーク→地図ピン (2026-06-14)

**背景**
- v49 地図ビューへのフィードバック「いいかんじ」+ 2つの要望:
  - 「**一日だけの線を見たいときも、複数日の線を見たいときもある。線の日数を選べるようにしたい**」
  - 「**連想ウォークのある一枚の写真から、その写真の地図上のピンにもとべるようにしたい**」

**設計判断**
- **「線の日数」= つなぐ範囲の粒度コントロール**と解釈（特定の1日だけ残すフィルタではなく、日をまたいで繋ぐかの粒度）。地図上部中央に小さなチップ `日ごと / 3日 / 1週間 / ぜんぶ`（`LINE_MODES`, `lineModeIdx`）。
  - `日ごと` = 暦日で切る（`segmentByDay`、1日=1本 = 一日だけの線）。`3日`/`1週間` = その日数を超えて空いたら切る（`segmentByGap`、連続した数日の旅が1本に）。`ぜんぶ` = 全 GPS を1本（gapDays=Infinity）。
  - **既定 = 日ごと**（v49 の 12h ギャップから変更）。`lineModeIdx` は module スコープで地図を開き直しても選択を覚える。
  - 動線描画を `drawTrips()` に関数化し、引いたレイヤを `mapState.tripLayers` に保持 → チップ切替で線だけ張り替え（マーカーは線モードと無関係なので不動・再構築しない）。
- **連想ウォーク → 地図ピン**: explore の中心カード（その walk の「ある一枚」）に GPS がある時だけ「🗺 地図でこの場所を見る」を出す。`openMapView(focusPhoto)` を拡張し、focus があれば fitBounds せずそのピンへ `setView`+`zoomToShowLayer`+popup。地図(軌跡)↔連想(偶然)の**双方向**動線が揃った（v49 はマーカー→連想の片方向だった）。中心カードのジェスチャ(長押し=フル画像)と衝突しないようボタンの pointer 系は `stopPropagation`。

**ハマったところ**
- モバイル(375px)で中央配置の線コントロールが左上の「閉じる」と重なった（中央寄せ + 幅 245px が close の右端 93px に食い込む）→ `@media(max-width:719px)` で線コントロールを top:52px に1段下げて回避。

**結果 / 観察**
- preview E2E（東京1日 / 京都→大阪 連続2日 / 札幌単独 / GPS無し の決定的データ）: **日ごと=3本 / 3日=2本（京都旅2日が連結）/ ぜんぶ=1本** で粒度が効くことを実数で確認。中心カードの 🗺 は GPS 写真のみ表示・GPS 無しは非表示、クリックで地図が当該ピン（大阪城 34.687,135.526）に正確に寄り popup 表示。モバイルの重なり解消・コンソールエラー無し。BUILD `phase3.1`。

**教訓**
- 「線の日数」は**しきい値（gap）と暦日**の2系統を1つのチップ列に同居させると素直（`日ごと`だけ暦日、他は gap 日数）。`Infinity*86400000=Infinity` で「ぜんぶ1本」を分岐なしに表現できた。
- 線の張り替えはレイヤ配列を保持して remove→再 add するだけ。マーカー/クラスタは作り直さない＝切替が軽い。

**残課題 / 次の方向**
- 実機で粒度の使い分け（日ごと⇔複数日）が想起の手触りに合うか。`3日/1週間` の刻みが妥当か（旅の長さに対して）。中心カード以外（近傍・足跡の写真）からもピンに飛びたくなるか。

## v49 — 地図ビュー（軌跡 + タイムライン）を un-park (2026-06-14)

**背景**
- ユーザー「`owntracks-supabase-notion` を参考に、地図機能を足したい。写真の位置情報から日本地図に撮った場所を配置、撮影順に線でつなぐ。サイドバーのタイムラインで見る。位置情報の無いものは地図に出さずタイムラインで見れる。**主眼は想起** — 写真を見て、自分の移動した軌跡を見て、想起を促す」。
- 地図ビューは TODO / CLAUDE.md / memory で長く「今は作らない」に置いていたが、但し書きは一貫して**「検証が当たれば次の周回」**。reminiscence の手触り検証は 1000 枚規模で核心の検証問いに強い YES = 達成済み ([[reminiscence-at-scale-works]])。**ユーザーの明示要求 = memory の「スコープ要確認」への回答**と受け取り un-park した。

**設計判断**
- **核（連想ウォーク）に合流させる**。地図は新しい入口だが、マーカーのポップアップに「🔮 連想ウォーク」を置き、タップで既存 `showExplore(photo)` へ接続。地図(軌跡で想起) → 連想(偶然よみがえる) が一本に繋がる。地図を孤立した別機能にしない。
- **線は「旅/日」ごとに自動分割**（`segmentTrips`、`TRIP_GAP_MS=12h`）。全 GPS 写真を1本で繋ぐと全国を貫く糸の絡まりになり軌跡が読めない（ユーザーと相談して決定）。撮影時刻が大きく空いた所で線を切り、連続した時間帯=ひとつの動線に。各セグメントを色分け + 進行方向の矢印（`polylineDecorator`）。
- **モバイル=下部シート / PC=右サイドバー**（同じ DOM を CSS の `@media(min-width:720px)` で出し分け）。主端末 iPhone で地図を全面に見せ、タイムラインは下から引き出す。
- **逆ジオコーディングは入れない**。参照アプリは Nominatim で地名化するが、座標を外部に送るのは「写真は端末から出さない」の精神から一歩踏み込みすぎ。`fmtPlace` の座標表示のまま。地図タイル（CARTO ダーク + 地理院、無料/APIキー不要）とライブラリは CDN 取得だが、送るのはビューポートのタイル要求のみでユーザーデータは出さない。
- **Leaflet は遅延ロード**（`loadLeaflet`、CLIP と同じ「使う時だけ」）。🗺 初回タップ時に CSS/JS を冪等注入。起動コストは増やさない。デフォルト体験（random/explore）は不変＝[[ui-minimalism-works]] の「使う時だけ広がる」。
- **実装の置き場**: `render()` は `$main` を毎回作り直すので Leaflet の安定コンテナと両立しない → 地図は `$main` の外のフルスクリーン overlay として `showFullImage` と同様に命令的に build/teardown（`openMapView`/`closeMapView`、状態 `mapState`）。
- **採用しなかった**: 地名検索・ヒートマップ・滞在/時間内訳（参照アプリにはあるが spike のスコープ外）。GPS 無し写真を地図に 0,0 等で出す（誤配置になるので一覧のみ）。

**ハマりどころ**
- preview の `preview_click` で Leaflet popup 内ボタンが発火しなかった（popup は absolute 配置でヒットテスト座標がずれる）。JS から `.click()` 直接発火では正常 → ロジックは正しく、実機タップは発火する。検証は programmatic click で担保。
- markercluster 内の marker は `marker.openPopup()` だけでは開かない（layer が map 上に無い）。タイムラインからのフォーカスは `cluster.zoomToShowLayer(marker, cb)` でクラスタを解いてから開く（参照アプリと同じ作法）。
- シート開閉・ビューポート変更でコンテナ高が変わったら `map.invalidateSize()` を呼ばないと Leaflet が古いサイズのまま欠ける。
- 単独点（1点だけの旅）も色パレットの index を1つ消費するので、最古が単独点だと動線の色が赤始まりにならない（軽微・無害）。

**結果 / 観察**
- preview E2E（検証用に日本国内の決定的データ=東京の旅4点 / 京都→大阪3点 / 札幌単独 / GPS無し2枚を一時投入、確認後に削除）: クラスタ "4"/"3" が旅の枚数と一致・色分け動線 + 方向矢印・日本にフィット・タイムライン5日10件(GPS無し2)・マーカー popup → 連想ウォーク → explore(中心カード+近傍6)・タイムライン GPS タップで zoom 6→13+popup・GPS 無しタップでフル画像・PC右サイドバー(960+320)/モバイル下部シート(46px↔78vh)・コンソールエラー無し。BUILD `phase3.0 (地図ビュー)`。
- **実機での手触り（想起に効くか）はこれから**。

**教訓**
- 「次の周回」に parked した機能でも、検証問いが YES になった時にユーザーが明示要求したら un-park の合図。但し**核（連想ウォーク）への合流口を必ず作る**ことで、地図が「別アプリの貼り付け」でなく reminiscence の一部になる。
- 参照アプリ（owntracks）は同じ無料タイル/ポリラインの作法をそのまま流用でき、収支・滞在など重い機能は持ち込まない、と取捨できたのが速かった。

**残課題 / 次の方向**
- 実機（iPhone Safari）で、(a) 実写真の GPS 分布で軌跡が「想起を促す」体験になるか、(b) `TRIP_GAP_MS=12h` の分割粒度が旅の感覚に合うか（多日旅が日ごとに割れすぎないか）、(c) 大量 GPS 写真でのクラスタ/描画負荷、を観察。
- 色を時間順（昔→今）のグラデーションにして時間的連続性を補強する案（今はパレット循環）。タイムラインと地図の双方向ハイライト強化。

## v48 — 削除（除外）一覧から復活できるように (2026-06-07)

**背景**
- ユーザー「間違えて削除した時用に復活方法を作りたい。削除一覧から戻せるようにしたい」。
- 調査(マッピング workflow)で確認: アプリの「削除」は ✕ の **除外(`excluded` 非破壊フラグ)** で、`undoExclude` という復元関数も既存。ただし**復元は直後の5秒トーストだけ**で、窓を逃すと除外写真を見る/戻す手段が無かった。これが穴。(🗑 全削除=`dbClear` は別物・不可逆、per-photo の物理削除は無し。)

**設計判断**
- **新しい削除概念を足さない**。既存 `excluded` フラグが既に「ソフト削除リスト」そのものなので、足すのは **(1) それを一覧表示するモーダル + (2) 「戻す」= 既存 `undoExclude` 呼び出し** だけ。新ストア/新フラグ不要。
- UI は情報/AI モーダルと同じ `.ai-modal`/`.ai-modal-card` パターンを踏襲(`renderExcludedModal`)。各サムネに「戻す」、2枚以上なら「すべて戻す」(`restoreAllExcluded`)。
- 入口は **ヘッダの ♻️N バッジ**(除外数。**0 のときは隠す**=使う時だけ広がる [[ui-minimalism-works]])。`render()` 内 `updateTrashBtn()` で常に最新化。ℹ️ の「連想の歩き方」にも「✕ は直後トースト or 右上 ♻️ からいつでも戻せる」と明記。🗑 全削除は据え置き(全削除時は `excludedModalOpen=false` で整合)。
- **採用しなかった**: 全削除の undo(数千件 blob 退避で過剰)、✕ とは別概念の per-photo ゴミ箱(`excluded` と二重概念で混乱)。

**ハマりどころ / 注意 (調査で先回り)**
- thumb は `thumbUrl(photo)` で**都度取得し URL を変数に握らない** → モーダル表示中に `revokeAllThumbUrls`(大量取り込み/backfill 完了)が走っても、次 render で再生成され回復。
- 復元は既存レコードの `excluded` 変更なので `addPhotos`/`loadedIds` には触れない(新規追加経路を通すと二重登録)。足跡・currentRandom 等は `undoExclude` の `render()` で自動復帰。
- dedup は除外しても残るため「除外中の画像を再取り込み」では戻らない=一覧復元が唯一の戻し口、で整合。

**結果 / 観察**
- preview E2E(dbPut スタブで実 DB 非汚染、in-memory に excluded 写真を seed): ♻️N 表示/非表示(0で隠れる)、モーダル一覧(サムネ blob: URL)、個別「戻す」で一覧縮小+ヘッダ ♻️ 減算、「すべて戻す」で全復元、空状態「ありません」、復元のたび dbPut 発火、モーダル開閉、コンソールエラー無しを確認。BUILD `phase2.10 (削除一覧から復活)`。

**教訓**
- 既にある非破壊フラグ(`excluded`)+ 復元関数(`undoExclude`)が「ゴミ箱」の中身そのもの。新概念を足さず**一覧 UI を被せるだけ**で「削除一覧から復活」が成立。マッピング調査で「足すのはモーダル1枚」と先に分かったのが効いた。

**残課題 / 次の方向**
- 実機で ♻️ の発見可能性(✕ 直後トーストを逃しても気付けるか)。除外が大量(数百)になった時の一覧描画(今は全件、必要なら先頭N+もっと見る)。

## v47 — 取り込み口を2系統(📷写真/🖼️画像)に整理 + バックアップ撤去 (2026-06-07)

**背景**
- 実機フィードバックが強い: 「**保存日だけでも、記憶の扉は開いてくれました。その時のことを思い出しました**」(v46 への肯定 = date-less 画像を入れて連想に乗せる方向は正しい)。
- ユーザー「機能を整理。＋=日時ありのまま+フォルダも / 📁=日時なしのまま+個別ファイルも / ↑(復元)はいらない / ＋と📁を分かりやすく」。

**設計判断**
- 取り込み口を **「日時の扱い」軸の2系統**に再編 (ファイル/フォルダの別ではなく):
  - **📷写真** = EXIF 日時のある写真だけ (厳格、日時なしはスキップ)。
  - **🖼️画像** = 日時の無い画像も保存日で取り込み (寛容)。
  - 各ボタンを押すと **「📄ファイル / 📁フォルダ」の小メニュー** (`openImportMenu`)。web は1つの input で file 選択と folder(webkitdirectory) を兼ねられないため、**`importFallback` フラグ + 既存 #picker(file)/#folder(folder) を共用**して「(厳格/寛容)×(ファイル/フォルダ)」の 2×2 を実現 (入力は2つのまま、メニューでフラグと入力先を切替)。
- **JSON バックアップ機能 (⇩書き出し/⇧復元) を撤去** (ユーザー「↑はいらない」+ 復元を消すなら書き出しも片割れなので両方)。`exportAll`/`importBackup`/`blobToB64`/`b64ToBlob`/#importer input/両ハンドラを削除。写真は IndexedDB に残る (消去は 🗑 のみ)。
- **ヘッダのレイアウト**: テキスト付きボタン (📷写真/🖼️画像) は emoji より幅広 → 狭幅で崩れないよう `header` を `flex-wrap: wrap` + `@media(max-width:600px)` でタイトル行(h1)を `flex-basis:100%` にしてボタンを下段へ折り返す。desktop は1行維持。

**ハマったところ**
- 最初 `flex-wrap` だけ足したら、h1(flex:1) が最小幅 49px に潰れ、長い BUILD 文字列が縦に 200px 積もってヘッダが激高に (これは元々 nowrap でも狭幅で起きていた潜在問題)。次に h1 を `white-space:nowrap; text-overflow:ellipsis` にしたら 1 行に収まったが **BUILD が見えなくなる** → iOS キャッシュ確認 (CLAUDE.md) の生命線を潰すので却下。最終解 = 狭幅でタイトル/BUILD を独立行にし全文表示、ボタンは下段。**「省スペース」より「BUILD 可視」を優先**。

**結果 / 観察**
- preview 検証 (mobile 375 / desktop 1280): 📷/🖼️ × ファイル/フォルダ の 4 組合せが正しい `fallbackMtime` で発火 (📷→false / 🖼️→true、どちらも file/folder 可)、メニュー開閉・空画面ボタンからも動作。ヘッダは desktop 1 行 (50px)・mobile 2 行 (96px) で横 overflow 無し・**BUILD 可視**。削除した関数の dangling 参照なし・コンソールエラー無し。BUILD `phase2.9`。

**教訓**
- 「ファイル/フォルダ」と「日時の厳格/寛容」は直交する 2 軸。UI はユーザーが気にする軸 (日時の扱い) でボタンを割り、もう片方 (file/folder) はメニューに落とすと分かりやすい。実装はフラグ + 入力共用で 2 入力に保てる。
- 長い BUILD 文字列はヘッダ折返しの火種。省スペース化で BUILD を隠すのは本末転倒 (キャッシュ確認用)。狭幅では独立行にして全文を残す。

**残課題 / 次の方向**
- v46 同様、保存日タイムラインの実機手触り。ヘッダのテキストボタンが iPhone でどう見えるかも実機で。

## v46 — フォルダごと取り込み + 保存日 (lastModified) フォールバック (2026-06-07)

**背景**
- ユーザー「撮影時間も投稿時間も無い画像も扱えるようにしたい。**保存時間を時間として扱う**のはどうだろう? その写真を保存したという行為自体が記憶を思い出すきっかけになる。**色検索や意味検索が可能なため**画像の思い出に拡張したい。**写真入りフォルダごとのアップ**も可能にしたい」。
- IG/X 取り込み成功を受けた自然な拡張要望。日時の無い画像 (スクショ、保存画像、フォルダ内の素材) を「保存日」で取り込み、色・意味の連想に乗せたい。

**設計判断**
- **保存日フォールバックは opt-in (Option A、ユーザー選択)**。新設の **📁 フォルダ取り込み** だけ `file.lastModified` で日時補完し、**通常の「+」ピッカーは EXIF 厳格のまま** (日時なしは従来どおりスキップ) → 既定タイムラインの純度を守りつつ、フォルダは「何でも入る」と役割を分離。全面適用 (Option B) より核 (時間軸) への影響を限定。
- **フラグ伝播**: `importFiles(files, {fallbackMtime})` → fast-track の `importOne(file, {fallbackMtime:true})` と、`enqueueBackground(rest, fallbackMtime)` → bgQueue を `{file, fallbackMtime}` のオブジェクトに変更 → `runBackgroundImport` で取り出して `importOne` へ。`importOne` は「EXIF 日時なし & override.fallbackMtime & file.lastModified が妥当 (>0 かつ未来でない)」の時だけ `new Date(lastModified)` を採用。
- **フォルダ選択は `<input webkitdirectory multiple>`** (PC/デスクトップ向け。iOS Safari はフォルダ選択非対応)。change で画像だけ抽出 (type=image/\* かつ tiff 以外、または既知拡張子)、`importFiles(imgs, {fallbackMtime:true})` へ。入口は ヘッダ 📁 + 空画面ボタン。
- **根拠 (ユーザーの洞察が的確)**: 色ジャンプ・意味ウォークは日時非依存で刺さっている軸。日時が不正確でも「まず入れて色・意味の連想に乗せる」方が核 (よみがえる) を強める。「保存=心が動いた瞬間」の捉え方も reminiscence と整合。

**ハマったところ / 注意**
- **「今」への固まりリスク**: フォルダをコピーした直後の画像は lastModified が全部「今日」に寄りうる → 時間軸 (昔→今→未来) で今に塊ができ、その群の「久しぶり」感は弱まる (色・意味の連想は健在)。元から手元に長くあるファイルやスクショは保存日がバラけるので、実機で手触りを観察する前提で割り切り採用。
- 既存 `importFiles` の呼び出し (「+」ピッカー) は引数なし → `opts={}` で従来挙動を完全維持 (回帰なし) であることをテストで確認。

**結果 / 観察**
- preview E2E (dbPut/dbHasDedup/addPhotos スタブで実 DB 非汚染): EXIF 日時の無い jpg 8 枚 (明示 lastModified 付き) を 📁 経由で取り込み → **fast-track 6 + background 2 の両キュー経路**で flag が伝播し、全 8 枚が保存日付きで dbPut、各 datetime が lastModified と一致。**「+」相当 (opts なし) では同じ 8 枚が 0 件取り込み = date-less スキップ**を確認 (既定の純度維持)。コンソールエラー無し。BUILD `phase2.8 (フォルダ取り込み・保存日フォールバック)`。

**教訓**
- 核 (時間軸) に触れる変更は「全面適用」でなく「専用入口で opt-in」に倒すと、既定体験を壊さず拡張できる。バックグラウンドキューに状態を載せる時は、ファイル配列でなく `{file, …}` で付帯情報を持たせると素直。

**残課題 / 次の方向**
- 実機で「保存日タイムライン」の手触り (今への固まりが連想を阻害しないか、スクショの保存日がよい起点になるか)。固まりが問題なら、取り込み時に日付を少し散らす / 「保存日です」表示で期待値調整、等を検討。

## v45 — X(Twitter) アーカイブ (zip) からの画像投稿取り込み (2026-06-06)

**背景**
- Instagram 取り込み成功 (v43-44) を受けてユーザー「ツイッターもできるかな? 画像/文字/動画どれが対応できる?」。
- 相談の結果 **画像投稿だけ対応**で合意。文字投稿は写真が無く本アプリ(写真で思い出す)の核に乗らない/動画はサムネ化が要るので見送り。「写真がよみがえる」価値は IG と同じく成立。

**設計判断**
- **📦 を Instagram / X 自動判別に一般化** (`importIgZip` → `importArchiveZip`)。パス1のメタデータ読み取りだけ形式別に分岐 (IG=JSON / X=`tweets.js`)、パス2の画像取り込み・サムネ化・日時付与・入口 UI・ストリーミング解凍は完全共通。ボタンは増やさず 1 つ (ヘッダ 📦 + 空画面ボタンのラベルを「Instagram / X」に更新)。
- 進捗・成否判定を**画像枚数ベース** (`stats.images`) に統一し、`stats.files` (メタファイル数) と合わせて診断を 2 段階に: ファイル 0=形式違い/メディア未同梱の案内、画像 0=テキスト/動画のみの案内。
- X 形式は実装前に**3視点 web 調査 + 突き合わせ**で裏取り (IG と同じ進め方)。確定要点:
  - データは純 JSON でなく **JS ラッパー** `window.YTD.tweets.partN = [ … ]` → 先頭 `[` 〜 末尾 `]` を切って `JSON.parse` (プレフィックスを丸ごと捨てる)。`tweet.js`/`tweets.js`/`tweets-part0.js…` の分割を正規表現で全部拾う。
  - 各要素は **`{tweet:{…}}` で 1 段ネスト** → `el.tweet ?? el` でアンラップ (旧フラット版も吸収)。アンラップ忘れが最頻バグ。
  - 日時は `tweet.created_at` の英語固定・UTC 文字列 (`"Wed Oct 10 20:19:24 +0000 2018"`) → `Date.parse` で解釈可 (月名・曜日が英語固定でロケール非依存)。ID は `id_str` (数値 id は 64bit 超で精度落ちするため不可)。
  - メディアは **`extended_entities.media[]`** を見る (`entities.media` は先頭1件・type 誤判定で不可)。**`type==='photo'` だけ**採用 → 動画/animated_gif (実体 mp4) を確実に除外。
  - ローカル実体の命名規則 (実コードで確定): `data/tweets_media/<tweet.id_str>-<media_url の basename>`。これは既存の byBase (ファイル名一致) 突合にそのまま乗る (`${id_str}-${urlBase}` をキーに置くだけ)。media_url のクエリ (`?name=` 等) は除去。
- **採用しなかった**: 文字投稿の画像化 (核を薄める)、動画のフレーム抽出 (iOS デコード可否 + 一手間。要望が出たら次)。

**結果 / 観察**
- preview で**合成 X アーカイブを本物パイプラインに通す E2E** (dbPut/dbHasDedup/addPhotos スタブで実 DB 非汚染): 単一画像 + 複数画像 (カルーセル相当) ツイートの取り込み 3 枚成功、**動画・テキストのみ・mp4 をすべて除外**、`created_at` から日時復元、ファイル名 `tweetId-urlBasename` 一致を確認。X 単体表明 (ラッパー剥がし / `{tweet}` アンラップ + フラット fallback / `?name=` 除去 / 2006年前・不正日時除外) 全 pass。**Instagram 取り込みの回帰も green** (リファクタ後も 1 枚正しく取り込み)。コンソールエラー無し。BUILD `phase2.7 (X(Twitter) zip 取り込み対応)`。

**教訓**
- 2 つ目の取り込み元を足す時は「共通部 (解凍・サムネ・日時付与・突合) ↔ 形式依存部 (メタ読み取り) 」の境界を切ると、追加は**パーサ 1 枚**で済む。byBase をファイル名一致にしておいたのが効いて、X の `id-basename` 命名がそのまま乗った。
- X は IG と違い「形式選択」が無く zip に HTML+JSON 両方入る。読むのは `data/tweets.js` + `tweets_media/` だけ。`.js` ラッパーと `{tweet:{}}` ネストが二大つまずきどころ。

**残課題 / 次の方向**
- 実機で実 X アーカイブ取り込み (フォルダ名版差 `tweet_media`、巨大アーカイブのメモリ/時間)。投稿日時=ツイート日時の手触りは IG 同様要観察。動画対応は要望次第。

## v44 — Instagram zip 取り込みの入口を空画面にも (2026-06-06)

**背景**
- v43 直後、ユーザーが PC で実取り込みを試した際「zip を選択できない」。原因は**空画面の大きな「📷 写真を選ぶ」(画像専用ピッカー, `accept=image/*`) を押していた**こと。zip の入口はヘッダの小さな 📦 だけで、初見では気づけなかった (画像フィルタで zip が一覧に出ず詰まる)。
- なお実機検証の副産物: 最初のエクスポートは **HTML 形式 + メディア未同梱** (`media/no-data.txt`) で使えず → JSON 形式 + 投稿を含めて再エクスポートしてもらったら **jpg 31枚・全件 creation_timestamp 有効・uri↔jpg 31/31 一致** を zip 直読みで確認。取り込みロジック自体は実データで OK。

**設計判断**
- 空画面 (`state==='empty'`) に**第2の入口** 「📦 Instagram の zip から取り込む」+ 一言説明を追加。プライマリは従来通り「📷 写真を選ぶ」のまま、zip は「または」で副次提示 (デフォルト体験は変えず、必要な人にだけ見える [[ui-minimalism-works]] の流儀)。ヘッダ 📦 は据え置き。

**教訓**
- 新しい取り込み経路を足したら**入口の発見可能性**まで含めて設計する。ヘッダの絵文字1個だけだと、空画面で大きなプライマリに吸い寄せられて気づかれない。
- エクスポートは「JSON 形式」かつ「投稿(メディア)を含める」の両方が要る。どちらか欠けると no-data / HTML になり取り込めない → 案内文に明記済み。

## v43 — Instagram エクスポート (zip) からの取り込み: 投稿日時を復元 (2026-06-06)

**背景**
- ユーザー「インスタから自分の画像をダウンロードした画像を使いたい」。
- 調べると、**Instagram からダウンロードした画像は EXIF (撮影日時) が剥がされており**、`importOne` の `if (!datetime) return {skipped:'no-datetime'}` ゲートで**無言で全スキップ**されていた (コードにも「ネット画像は EXIF 日時が無く入らない」と既知メモ)。このアプリの核は時間軸 (昔→今→未来 / 久しぶり) なので「日時をどう与えるか」が肝。
- ユーザーと相談し方針を確定: **公式「情報をダウンロード」(JSON 形式) の zip に残る `creation_timestamp` から投稿日時を復元**して取り込む (核を最も忠実に保てる)。食わせ方は **zip 1個を選ぶだけ (アプリが端末内で解凍)** を選択 (iPhone Safari で操作が最小・uri で正確突合)。

**設計判断**
- **エクスポート形式は調査で裏取りしてから実装** (3視点 web 調査 + 突き合わせ)。確定した要点: `creation_timestamp` は **Unix 秒** (×1000)。posts JSON はパス可変 (`content/` ↔ `your_instagram_activity/media/`、日付入りトップフォルダ、`posts_1/2…` 分割) → **決め打ちせず正規表現で再帰的に拾う**。構造は投稿配列だが**単一投稿だとオブジェクト**になる版・`{ig_stories:[...]}` ラップ版あり → 正規化が要る。日時は **root と media[] 要素の両方に出うる** → media 優先・root フォールバック。`uri` は zip ルート相対 (`media/posts/YYYYMM/…`)、メディア実体は JSON の隣でなく zip 直下 `media/` 配下。
- **メモリ最小の 2 パス・ストリーミング解凍** (`fflate` の `Unzip`/`UnzipInflate`、CDN 追加)。zip 全体をメモリに載せない: 各 push のたびに完了エントリを 1 件ずつ await 処理してから次を読む = バックプレッシャで常に ~1 エントリ分だけ保持。**iOS の OOM 履歴 (CHANGELOG v23/v36/v37) を踏まえた選択**。`file.stream()` を 2 回開く (パス1=JSON のみ展開して日時マップ / パス2=画像のみ展開して取込)。`want()=false` のエントリは `start()` を呼ばず展開もスキップ (動画 `.mp4` 等を弾く)。
- **`importOne(file, override)` に外部日時を渡せるよう拡張**し EXIF 必須ゲートをバイパス。override 時も `readMeta` を best-effort で呼び orientation/GPS は拾える分拾う。既存の写真ピッカー/バックアップ復元の経路は**完全に無変更** (override 無しなら従来通り)。zip 画像は `new File([bytes], basename, {type})` に包んで既存パイプライン (`toJpegBlob`/`createThumbnail`/dedup/`dbPut`) にそのまま合流。
- **uri 突合は 2 段**: エントリ名・uri を `media/` 起点に正規化して相対パス一致 → 外れたら basename フォールバック (別フォルダ同名が別日時なら衝突として basename は無効化)。
- **GPS のおまけ復活**: `media_metadata.photo_metadata.exif_data` に lat/lng があれば拾い、時空ウォークの軸を部分的に取り戻す。
- **HTML 形式の誤出力を検出**: 日時マップが 0 件なら「JSON 形式で出し直して」と案内 (HTML には生の数値日時が無くパース不能)。
- **採用しなかった案**: (a) ダウンロード日 (`lastModified`) を日時に → 全部「今」に寄り「久しぶり/昔」が壊れるので却下。(b) 手動日付入力 → 枚数が多いと非現実的。(c) 解凍後に JSON+画像を手選択 → iOS で多数選択がつらく相対パスが失われ basename 頼みになるので却下。

**ハマったところ**
- **構造正規化のバグ (preview で発見)**: 「トップがオブジェクトなら中の配列を探す」素朴な実装だと、**単一投稿オブジェクト `{creation_timestamp, media:[…]}` の内側 `media` 配列を投稿配列と誤認**し、各 media 要素を投稿として扱って **root の日時を落とす** (media に日時が無い版で取りこぼし)。→ オブジェクトが `media`/`uri`/`creation_timestamp` を持つ (=投稿そのもの) なら `[data]`、持たない (=`{ig_stories:[…]}` 等のラッパー) 時だけ中の配列を取り出す、に分岐して修正。
- fflate の `ondata` で渡る chunk は**バッファを再利用するので必ず複製** (`chunk.slice()`) してから蓄積。

**結果 / 観察**
- preview で**合成 zip を本物パイプラインに通す E2E** を実施 (`dbPut`/`dbHasDedup`/`addPhotos` をスタブし**実 IndexedDB を汚さず**検証)。配列版 posts と**単一投稿オブジェクト版** (content/ 配下・日付トップフォルダ・mp4 混在) の両方で、日時復元 (秒→正しい Date)・サムネ生成・動画/JSON 除外・パス正規化・root フォールバックすべて green。`igTsToDate`/`igNorm`/`igCollectDates`/`igLookup` の単体表明も全 pass (0/未来/文字列除外、ms 救済、ラップ配列、basename フォールバック)。コンソールエラー無し。BUILD `phase2.5 (Instagram zip 取り込み)`。

**教訓**
- ネット由来画像は EXIF が無い前提で「**日時をどこから供給するか**」を設計の中心に据える。spike の核が時間軸である以上、ここを曖昧にすると体験ごと壊れる。
- エクスポート形式はバージョンで動く (パス・配列/オブジェクト・分割) → **決め打ちを避け、正規表現探索 + 構造 coercion + 2 段マッチ**で頑健にする。投稿日時 (≠ 撮影日時) である点は「久しぶり」の手触りに効きうるので実機で要観察。

**残課題 / 次の方向**
- 実機 (iPhone Safari) で**実エクスポート zip** を取り込み、(a) 大容量 zip の解凍時間/メモリ、(b) 投稿日時が reminiscence (久しぶり) に馴染むか、を観察。重ければ「期間を絞ってエクスポート」運用 or `media_metadata.exif_data` の撮影日時優先を検討。

## v42 — リロード時は足跡を残しつつ画面はランダム3枚から (2026-05-31)

**背景**
- ユーザー「リロードしたときには、いまのまま足跡の記憶はあるけど、ページはランダム3枚だといいね」。
- v41 はリロードで直前の中心 (explore 画面) に復帰していた。足跡の記憶は保持したまま、**画面の入口は毎回ランダム3枚に**する。

**設計判断**
- 起動 `restoreOrRandom()` を「足跡の id を先読みしてから必ず `drawRandom()`」に簡素化。explore へ戻す分岐を撤去。
- 直前の中心保存 (`pms-walkCenter`) を**廃止**: もうリロードで中心へ戻らないので不要。`saveTrail()` は足跡 id 列 (`pms-walkTrail`) だけ書く。
- 足跡そのものは v41 のまま (ウォークをまたいで貯まる / 30枚リングバッファ / id 解決 / 🗑 でのみクリア)。「足跡は残す・画面はリセット」= リロードが**新しい連想ウォークの始まり**として自然 (random に戻る/引き直しと同じ意味になり、挙動が一貫)。

**結果 / 観察**
- preview (dev40枚): 4歩 walk → リロード → `state=random`・`currentCenter=null`・足跡4枚は保持 (persisted 4) を確認。random 画面には足跡 UI は出ず、カードをタップして explore に入ると記憶していた 4 + 新タップ = 5 が strip に出る。コンソールエラー無し、inline JS パース OK。BUILD `phase2.4 (足跡 永続/起動random)`。

**残課題 / 次の方向**
- 実機で「リロードでランダムに戻る + 足跡は残る」手触り。

## v41 — 足跡を永続化 (リロードで残る・30枚リングバッファ) (2026-05-31)

**背景**
- ユーザー「リロードした後も足跡が残る設定にしたい。30枚ぐらい足跡が残って、それを超えたら古いのから消える設計はどうだろう」。
- v39/v40 の足跡は「1回の連想ウォークの経路」で、リロード・引き直しでリセットされる作りだった。これを**ウォークをまたいで貯まる永続履歴**に作り替える。

**設計判断**
- **id 列で持つ + localStorage 永続**: `walkTrail` (写真オブジェクト配列) → `walkTrailIds` (id 配列, `pms-walkTrail`) に変更。表示時に `photoById(id)` で解決 (allPhotos 参照/件数が変わった時だけ Map を作り直すキャッシュ)。写真オブジェクトを直接 JSON 化しない (blob を含むので不可) のが id 化の理由。
- **30枚リングバッファ**: `MAX_TRAIL=30`。新規追加で超えたら `shift()` で古い順に消える。
- **重複は積まない**: 既に足跡にある写真へ戻る (近傍/足跡タップ) 時は push せず順序維持 → タップで過去地点に戻れる動作はそのまま。**v40 までの「先の枝を畳む (slice)」は廃止** (貯める方針なので、戻ってもそこから先の足跡は残す)。
- **リロード復帰**: 直前の中心も `pms-walkCenter` に保存し、起動時 `restoreOrRandom()` で可能なら explore 画面に復元。中心/足跡の写真が初期ロード窓 (ランダム3000件) の外でも、`dbGet(id)` で IDB から直接引いてメモリに足す → 大規模ライブラリでも復元できる。解決できない id (削除済み等) は静かに落とす。
- **引き直し/ランダムに戻る**: 足跡は消さず貯め続ける (中心ポインタだけクリア → リロード先が random になる)。**🗑 全削除のみ足跡もクリア** (写真が無くなるため)。

**結果 / 観察**
- preview (dev40枚): 35枚walk→trail 30 で頭打ち (最古drop/最新keep) / リロードで explore 復帰・trail 30・中心復元 / **中心が初期窓外のケースを強制し dbGet 経由の復元 (35→40件) も確認** / 引き直しで trail 維持 (5→5)・新タップで append (5→6) / opener 開閉 OK。コンソールエラー無し、inline JS パース OK。BUILD `phase2.3 (足跡 永続)`。

**残課題 / 次の方向**
- 実機で「リロードしても足跡が残る」嬉しさ + 30枚の体感。30枚が多すぎ/少なすぎなら `MAX_TRAIL` を調整。

## v40 — 足跡を開閉式に (モード切替と同じ流儀) (2026-05-31)

**背景**
- v39 の足跡帯は常時表示だった。ユーザー「この感じいいですね。モード切替と同じ開閉式の機能をたしてから push して」。

**設計判断**
- **モード切替 (`modeToggleVisible`) と同型の開閉**: `trailVisible` を localStorage (`pms-trailVisible`) で永続。普段は小さな opener (`.mode-toggle` 流用) だけ、開いた時だけサムネ帯を出す。デフォルト畳み = 写真への集中を守る ([[ui-minimalism-works]] の「使う時だけ広がる」)。
- opener はウォーク ≥2 歩で出現。畳んでいる時は枚数も見せる (`🔖 足跡 3 ▼`) → 開かずに「何歩来たか」が分かる。開くと `🔖 足跡 ▲`。
- 既存のモード opener と同じ `saveModePrefs()` に相乗り (永続キーを1つ追加するだけ)。

**結果 / 観察**
- preview (dev30枚): 3歩ウォークで既定畳み (opener `🔖 足跡 3 ▼`・帯非表示) → opener タップで展開 (`trailVisible=true` / localStorage `1` / opener `🔖 足跡 ▲` / 帯3枚・現在地1) → 再タップで畳み (localStorage `0`) → **リロード後も畳み状態を復元** (`persistedOnBoot=0`) を確認。コンソールエラー無し、inline JS パース OK。BUILD `phase2.2 (足跡 開閉)`。

**残課題 / 次の方向**
- 実機 (iPhone Safari) で開閉の手触り + 足跡そのものの価値を観察。

## v39 — タップ履歴「足跡」: 連想ウォークの軌跡を辿り直せる (2026-05-31)

**背景**
- ユーザー要望「タップした写真の履歴をみれるようにしたい」([[tap-history-wanted]])。Phase 1 では意図的に「履歴スタックは持たない、シンプル化」としていた (「ランダム3枚に戻る」のみ) が、1000枚規模で立ち上がった価値=**時間的連続性 (昔→今→未来) の物語** ([[reminiscence-at-scale-works]]) と地続きで、歩いた経路そのものが小さな物語になりうる、という見立てで復活させた。
- プロダクト化 (madeleine) は native を parked、ユーザーは「またプロトタイプに戻る」意向 → spike (このリポ) で実装。

**設計判断**
- **足跡 = 今の連想ウォークで辿った写真の列**。`showExplore` (random カードタップも近傍タップも通る単一の choke point) で更新、`drawRandom` (引き直し / ランダムに戻る = 新しい歩きの始まり) でリセット。state は `walkTrail` 一本。
- **写真そのものをサムネ帯で並べる** (日付テキストや番号でなく)。思い出しの意識を写真から逸らさない原則 ([[ui-minimalism-works]]) に沿う。
- **足跡タップで過去の地点へ戻り、その先の枝は畳む** (`slice(0, idx+1)`)。近傍タップで既訪問の写真に戻った時も同じく巻き戻す → 一本道を保つ (= パンくず的後戻り。amnesiac だった従来の walk にできなかった操作)。現在地は青枠 + 不透明で強調、過去は半透明。

**結果 / 観察**
- dev ダミー30枚で検証: 4歩 walk→足跡4枚 (現在地1枚だけ強調) / 足跡#2タップ→長さ2に巻き戻り中心が切替 / その後の近傍タップで枝が伸び直す (長さ3) / 引き直しで足跡リセット。コンソールエラー無し。
- ※ この v39 のログは追記タイミングを逃し v40 と同コミットで記録 (機能自体は be95181 で push 済み)。

**残課題 / 次の方向**
- v40 で開閉式に改良。実機の手触り観察は次。

## v38 — 注意書きを ℹ️「使い方・注意」に集約 (2026-05-29)

**背景**
- 一連の検証で注意書きが各所に散らばった (プライバシー / 取り込みの小バッチ / 鮮明な写真は純正アプリ / 差分取り込み / AI opt-in)。ユーザー: 「注意書きをまとめて書いておいたほうがいい」。

**設計判断**
- **ℹ️ ヘッダボタン → 「使い方・注意」モーダル**を新設 (AI モーダルと同じ overlay スタイル流用)。1 箇所に集約: 🔒 プライバシー / 📥 取り込み (差分 dedup・古い端末は 10〜20枚) / 🖼 鮮明な写真は純正写真アプリで日付検索 / 🎲 連想の歩き方 (モード・✕除外) / 🧠 意味(AI) opt-in。
- **reminiscence 画面 (random) の常時 caveat (v11) を撤去** → ℹ️ に移動。デフォルト体験を写真に集中させる ([[ui-minimalism-works]] の「思い出しの意識を逸らさない」)。**ℹ️ を押した時だけ開く** = 「使う時だけ広がる」。
- 取り込み時の privacy 行 (v26)・準備ガイド (v34) など **contextual な短い注記は残す** (不安・待ちが生じる瞬間に出るのが効くため)。集約 = 「常時表示の説明文」を 1 箇所に、の意。

**結果 / 観察**
- preview 検証: ℹ️ ボタン存在、モーダル開閉、5 セクション (プライバシー/取り込み/鮮明/連想/AI) 全表示、ランダム画面に常時 caveat が出ないことを確認。pass。

**教訓**
- 機能を足すたびに増える「説明文」は散らかる → **常時表示のものは1つの help に集約し、体験の中心 (reminiscence) からは外す**。contextual (その瞬間だけ) なものは残す、の住み分け。

**残課題 / 次の方向**
- 触り込みフェーズへ (iPhone で 1000枚規模、やめにくさ・旅のヒント性・枚数効果)。

---

## v37 — HEIC 変換を OS に任せて heic2any を回避 (iPad メモリ本命) (2026-05-29)

**背景**
- v36 (早期解放 + ペース) でも **iPad 47枚でまた落ちた**。メモリ天井の本命は別 → **heic2any (JS の HEIC→JPEG 変換) が最大のメモリ食い**と判断。
- 仮説: `<input accept="image/*,.heic,.heif">` に `.heic` を含めると iOS が **HEIC 原本**を渡してくる → それを JS の heic2any でデコード (重い)。3GB iPad で 47枚分の HEIC デコードが OOM。iPhone14 は余力で通っていた。

**設計判断**
- **`accept` から `.heic,.heif` を外す** (`accept="image/*"`)。iOS フォトピッカーは HEIC を **OS 側で JPEG に変換して渡す** (ネイティブ=軽い) → heic2any 不要に。GPS は JPEG でも保持 (Phase 0 で確認済)。
- **`isHeic` を type 優先に**: `type` が heic/heif、または type 空で拡張子 .heic の時だけ HEIC とみなす。**ファイル名が .HEIC でも type が image/jpeg なら heic2any を呼ばない** (誤検出で JPEG に heic2any を呼んでいた可能性を断つ)。
- **診断**: 取り込みサマリに「(うち HEIC変換 N)」を表示 (`importStats.heic`)。heic2any が実際に呼ばれた枚数が見える → N=0 なら OS 変換に乗れている証拠。原因究明をユーザーの目視で確定できるようにした。
- heic2any 自体は「ファイル app から HEIC 原本」等の保険として残す (呼ばれなくなるだけ)。

**結果 / 観察**
- preview 検証: `accept='image/*'`。type 優先判定 (JPEG型+.HEIC名→false / image/heic→true / 空type+.heic→true / jpg→false)。`toJpegBlob` は JPEG型の .HEIC ファイルを heic2any 通さず素通し。サマリは heic>0 で「(うち HEIC変換 N)」、heic=0 で非表示。全 pass。
- 実機: iPad で 47枚+ が通るか、サマリの「HEIC変換 N」が 0 になるか次回。
- **実機結果 (確定)**: iPad で 5枚→「✓ 新規 5枚」(HEIC変換の表示なし=0)。→ **heic2any は呼ばれておらず無実、accept 変更は成功**。だが 47枚は依然 OOM (即落ち) → **iPad の真因は raw な JPEG デコード (full-res→サムネ) のメモリ**で、3GB 端末のハード限界。コードで簡単に消せるバグではないと確定。**結論: メインは iPhone (問題なし)、iPad は 20枚以下の小バッチ運用。iPad の深追いはここで打ち切り** (spike の目的は iPhone で達成済)。深追いするなら createImageBitmap 縮小デコードだが iOS で不安定なので保留。診断カウンタ (HEIC変換 N) で実機の数字を見て犯人を切り分けられたのが収穫。

**教訓**
- **重いネイティブ処理 (HEIC デコード) は OS に任せ、JS ライブラリ (heic2any) は最後の手段**。`accept` 属性が iOS のファイル受け渡し挙動を変える (HEIC を入れると原本が来る) ことを忘れない。
- 原因が不確かな時は**実際に呼ばれた回数を可視化** (HEIC変換 N) して、実機で犯人を確定できるようにする。推測の連鎖を切る。

**残課題 / 次の方向**
- iPad で 47枚→通るか + 「HEIC変換 0」を確認。**もし N>0 のまま** (iOS が依然 HEIC を渡す) なら、別策: 取り込み前に各 File を一旦 `createImageBitmap`/縮小 decode で JPEG 化する、または「一度に N枚」ソフト上限。
- 通れば iPad もスケール検証可能に。

---

## v36 — 取り込みのメモリ改善 (iPad 47枚でタブ再読込クラッシュ) (2026-05-29)

**背景**
- v35 (タイムアウトで wedge 解消) 後、iPad で: **9枚→「新規9/日時なし1」OK、20枚 OK、47枚→「新規表示なし、初期画面に戻る」**。
- これは wedge ではなく **メモリ不足クラッシュ**。iPad 第8世代 (3GB RAM) で 47枚の画像デコード (HEIC変換/サムネ生成) がメモリを使い切り、**iOS が Safari タブを jetsam (再読込)** → 初期画面へ。summary が出ないのは reload で中断されたため。9・20 は耐え 47 で超えた = メモリ天井。

**設計判断**
- **`createThumbnail` の finally でデコードバッファを早期解放**: `img.onload/onerror=null; img.src='';` (デコード画像を解放) と `canvas.width=canvas.height=0;` (canvas バッキングストア解放)。返す `thumb`/`color` は解放前に確定済みなので安全。
- **`BG_IMPORT_DELAY` 120→220ms**: 背景取り込みの間隔を広げ、1枚ごとに GC の余裕を作る。
- **ただし根本は古い端末のメモリ限界** → 確実策は **20〜30枚ずつのバッチ取り込み** (dedup で二重にならない、v22)。クラッシュしても取り込み済みは IndexedDB に残るので**再取り込みで続きから積み上がる** (データ喪失なし)。

**結果 / 観察**
- preview 検証: 解放処理を入れても `createThumbnail` は正常 (thumb は有効 Blob、color 48次元・赤ソースを正しく検出)。`BG_IMPORT_DELAY=220`。pass。
- 実機: iPad で 47枚が通るようになったか次回。通らなければ次の大きい手は HEIC 判定見直し (下記)。

**教訓**
- wedge (詰まり) を直すと次の壁 (OOM) が見える。**古い端末の大量画像デコードは「早期解放 + ペース + 小バッチ」の三点**で凌ぐ。新しい端末 (iPhone14) では顕在化しない端末差。
- デコードした `<img>`/`canvas` は参照を切らないと iOS が解放を遅らせる → finally で明示的に潰す。

**残課題 / 次の方向**
- iPad で 47枚→通るか確認。ダメなら **HEIC 判定の見直し** (`isHeic` がファイル名 .HEIC で誤判定し、ピッカーが既に JPEG 変換した画像にも heic2any を呼んでいる可能性 → 呼ばなければ大幅にメモリ減)。`accept` から .heic を外して iOS 側транスコードに任せる案も。これが iPad の本丸かもしれない。
- そもそも古い端末向けに「一度に N枚まで」のソフト上限 + ガイドを出すか。

---

## v35 — 取り込みに per-photo タイムアウト (「3枚でもダメ」= 詰まり解消) (2026-05-29)

**背景**
- v34 (枚数ガイド) の後、ユーザー: **「3枚でも UP できない」**。これで「枚数が多い→OS 準備が遅い」説は否定。3枚でダメ = 別問題。
- 症状の流れ「iPad で最初10枚OK→以降は何枚でもダメ」から **wedge (詰まり)** と判断: **1枚の処理 (heic2any の HEIC→JPEG 変換、または `<img>`+canvas のサムネ生成) が古い iPad の Safari で固まって `importFiles` が return しない** → 後始末の `finally` (`importingNow=false`) が走らず **`importingNow` が立ったまま** → 以降 `$picker` の change ハンドラが「処理中」と即 return → **何枚選んでも取り込めない**。再読込すると JS 状態が消えて一時回復するが、また固まると再発。

**設計判断**
- **各 `importOne` を `withTimeout(30s)` でラップ** (fast-track と background 両ループ)。固まった写真はタイムアウト → catch で失敗カウント → ループ続行 → **`importFiles` が必ず完走 → `importingNow` がリセット**され詰まらない。固まった1枚は「失敗 N」に出る (heic2any が原因なら、その写真だけ落ちて他は入る)。
- `withTimeout` は `Promise.race` + `setTimeout`、決着時に `clearTimeout` (遅延発火の dangling 防止)。reject はそのまま透過 (本物のエラーを潰さない)。30s は通常処理 (<数秒) を誤爆せず、無限ハングだけ救う値。

**結果 / 観察**
- preview 検証: `withTimeout` は 速い promise→値 / 固まる promise→`timeout` reject / reject promise→そのまま透過。`IMPORT_TIMEOUT_MS=30000`、import 系関数も定義 (構文OK)。pass。
- 実機: まず iPad を最新 (`取込タイムアウト`) に更新 → 3枚→数枚で安定するか。HEIC が原因なら「失敗 N」が出るはず (→ 次の手の手がかり)。

**教訓**
- **1個の await が固まると全体が詰まる経路 (ここでは importFiles→importingNow→change) には必ずタイムアウト**。特に古い端末 + heic2any のような重い/不安定な変換。「失敗しても全体を止めない」を直列パイプラインの既定に。
- 症状の差分 (「10枚OK」→「3枚もダメ」) が原因切り分けの鍵だった。枚数ではなく**順序 (一度詰まると以降全部)** に注目すると wedge が見えた。

**残課題 / 次の方向**
- 実機で 🆗 確認。もし「失敗 N」が HEIC で多発するなら、古い iPad の heic2any が遅い/不安定 → (a) ピッカーが返す JPEG をそのまま使う (HEIC のまま渡さない) か (b) タイムアウト値調整 (c) 「ファイル」アプリ経由を案内。
- そもそも heic2any 依存を減らせるか (iOS ピッカーは通常 JPEG 変換して渡すので、HEIC 判定が誤検出している可能性も要確認)。

---

## v34 — 取り込み準備が長い時のガイド (古い端末対策) (2026-05-29)

**背景**
- ユーザー実機: **iPhone 14 では取り込みOKだが、iPad 第8世代では 10枚超で「準備中」スピナーが 30分以上回って進まない**。
- 切り分け: (1) スクショの BUILD が `phase2 (初期ランダム)` = **iPad は古い版 (v29) のまま**だった (iOS の HTML キャッシュが強烈)。(2) 回っているのは `preparing` のスピナー = `change` 前 = **iOS 側のファイル準備 (HEIC→JPEG 変換等)** の待ち。これは Phase 1.8 で確定した「ピッカー側は web 不可侵」が **古い端末 (A12・3GB RAM) で極端に遅く/stall** する形で顕在化。iPhone 14 (A15・6GB) は速いので問題化しない。アプリのバグというより端末の限界。

**設計判断**
- OS 側の待ちは web から制御できない → **`preparing` が 25秒を超えたら「少なめ(10〜20枚)に分けて」とガイド表示** (`prep-hint`)。30分無言で回るのを防ぎ、有効な回避策 (小分け取り込み) に誘導。`importFiles` 開始 / `cancelPicker` でタイマー解除。
- 回避策の根拠: 重複は dedup で自動スキップ (v22) なので**小分けに取り込んでも二重にならない** → 古い端末では 10〜20枚ずつ積み上げれば良い。

**結果 / 観察**
- preview 検証: `preparing` 描画に `#prep-hint` が出て初期は空、書き込み可能。BUILD 更新。pass。
- 実機検証は次回 (まず iPad を最新版に更新 → 10〜20枚バッチで安定するか)。

**教訓**
- **同じコードでも端末世代でちぎれる**。古い iOS 端末は OS 側のメディア処理 (HEIC 変換) が遅く、多数選択で stall する。「web 不可侵の OS 待ち」は新しい端末では見えず、古い端末で初めて顕在化する → 端末差を前提に、長い待ちには必ずガイドと回避策 (小分け) を用意。
- デバッグ前に**端末ごとに BUILD 文字列で版を揃える**。古い端末が古いキャッシュのままだと切り分けを誤る (BUILD 表示を入れておいた価値)。

**残課題 / 次の方向**
- iPad を最新に更新 → 10〜20枚バッチで安定するか確認。**小分けでも stall するなら app 側の wedge (importingNow が hung で固まる等) を疑い、`importOne` に per-photo timeout を追加** (1枚の処理が固まっても全体を巻き込まない)。今回はまず OS 側待ちと判断しガイド+小分けで対処。

---

## v33 — 長時間処理を Wake Lock で継続 (画面スリープ対策) (2026-05-29)

**背景**
- v32 のダミー削除を実機投入。ユーザー: **「6000 ぐらいまで削除できた。そのままスマホを放っておいて、画面が暗くなって削除が途中で止まった。いま再開したところ」**。
- iOS は画面オフ/バックグラウンドで `setTimeout` を止める/間引く → チャンク処理の合間 (`setTimeout(0)`) で停止する。3万件削除は時間がかかり、見ていないと画面が寝て止まる。

**設計判断**
- **そもそも削除は冪等で再開可能** — 毎回「その時点で IDB に残っているダミー」を `collectDummyKeys` で集めて消すので、途中停止しても 🧹 再押し (or ページ復帰でループ再開) で続きが消える。二重削除も本物への影響も無い。これは安全網として効いている (ユーザーも再開で進めている)。
- **画面スリープ抑止 (`withWakeLock`)**: 生成/削除の間だけ `navigator.wakeLock.request('screen')` で画面点灯を維持。非対応・失敗時は無視して処理続行 (iOS 16.4+ で対応)。これで見張らなくても完了する。
- **削除チャンクを 1000→2000** に増やし完了を速く (スリープに捕まる前に終わりやすく)。

**結果 / 観察**
- preview 検証: `withWakeLock(fn)` は WakeLock 非対応環境でも `fn` の戻り値をそのまま返す (透過・グレースフル)。`dbDeleteDummies` (CH=2000) で 5000 ダミー + 50 本物 → 5000 削除・本物 50 残存。pass。
- 実機で 30k を一気に削除しきれるか (wake lock の効き) は次回。

**教訓**
- **長時間のクライアント処理は「画面が寝ると止まる」前提で組む**: (a) `wakeLock` で点灯維持 (b) それでも止まる前提で**冪等・再開可能**に設計 (途中状態から続けられる)。この両構えが iOS では効く。一気に終わらせようとせず「止まっても困らない」が本筋。

**残課題 / 次の方向**
- 実機で 30k 生成/削除が wake lock で見張らずに完走するか。生成は非冪等 (再実行は追加生成) なので、特に wake lock が効くと安全。
- (任意) 同じ wake lock を本番の重処理 (大量取り込み / AI 抽出) にも広げるか検討。

---

## v32 — ダミー削除を堅牢化 (実機で消えなかった) (2026-05-29)

**背景**
- v31 を実機投入。ユーザー: **「🧹 は出ている。OK しても しても ダミーが消えないよ」**。3万件規模で 🧹 が効かない。

**設計判断 (原因と対処)**
- **原因**: v31 の `dbDeleteDummies` は**ストア全件を1つの readwrite トランザクションでカーソル走査し、`dummy-` を delete** する実装。30k 規模だと iOS Safari でこの巨大・長時間トランザクションが不安定 (完了せず、結果消えない)。さらに起動直後は Phase 2 背景読み込み (📥) が走っており読み書きが競合しやすい。「OK しても しても」= 完了feedbackが来ず連打 → 多重起動でさらに悪化。
- **対処 (2段に分割)**:
  1. **`collectDummyKeys`**: `dedup` インデックスの `IDBKeyRange.bound('dummy-', 'dummy-￿')` を **`openKeyCursor`** で走査し、ダミーの **primaryKey だけ**集める。全件 (本物含む数万件) を読まない・値もロードしない → 軽量・確実。
  2. **`dbDeleteDummies`**: 集めたキーを **1000件ずつの小トランザクション**で `delete`。巨大1トランザクションを避ける。進捗を 🌀 に出す。
- **連打ガード** (`devBusy`): 生成/削除の多重起動を弾く (「OK しても しても」対策)。

**結果 / 観察**
- preview 検証: 2500 ダミー + 100 本物 (計 2600) → `collectDummyKeys` が 2500 キー収集 → `dbDeleteDummies` が 3 チャンクで 2500 削除 → 本物 100 だけ残存、進捗クリア。全 pass。チャンク境界 (1000) を跨ぐ規模で確認。
- 既存の生成済みダミーも `dedup`='dummy-...' なのでこの方法で消える。実機で 30k → 🧹 が効くか次回確認。

**教訓**
- **iOS Safari の IndexedDB は「巨大・長時間の1トランザクション」が不安定** (取り込みの fast-track 直列化 v7、tx.error=null v16 に続く iOS IDB の弱さ)。大量を触る時は **(a) インデックス範囲で対象を最小化 (b) 書き込みは小バッチtrに分割** が鉄則。全件カーソルは避ける。
- 重い処理ボタンは**完了 feedback と多重起動ガード**を最初から付ける。無いとユーザーが連打して状態が悪化する。

**残課題 / 次の方向**
- 実機で 30k → 🧹 でダミーだけ消え本物 2000 が残ることを確認。
- 同種の「巨大1トランザクション」が他に無いか (色 backfill / embedding 抽出は1枚ずつ tx なので OK、export の dbGetAll は読みのみ)。

---

## v31 — ダミーだけ削除 (本物の写真を守る) (2026-05-29)

**背景**
- v30 のダミー生成を実機で確認 (起動の遅れは数秒で許容、📥 裏読みも増える)。
- ユーザーの鋭い気づき: **「全削除するとサンプル以外も消えちゃう?」**。その通り — 🗑 全削除 (`dbClear`) はストアを丸ごと空にするので、ダミーも**本物の 2000 枚も両方消える**。ダミー掃除のつもりで本物を失う事故が起きうる。

**設計判断**
- **ダミーは `name` が `dummy-` で始まる**ので判別可能 → `dbDeleteDummies()` がカーソルで `dummy-*` だけ削除し本物は残す。
- `#dev` の時だけヘッダに **🧹「ダミーだけ削除」**を追加 (🧪 生成と並ぶ dev 専用)。confirm で「本物は残る」と明示。完了後リロード。
- 🗑 全削除は用途 (全部消す) として正しいのでそのまま。ダミー掃除は 🧹 を使う、という役割分担。

**結果 / 観察**
- preview 検証: 本物 5 (IMG_*) + ダミー 10 (dummy-*) を投入 → `dbDeleteDummies` がダミー 10 だけ削除、本物 5 は全て残存。🧹 は無 hash で hidden。全 pass。

**教訓**
- テスト用ダミーと本物データが**同じストアに混在**する設計では、「全消し」しか無いと本物を巻き添えにする。**判別キー (name 接頭辞) を生成時に仕込んでおく**と、後から安全に「ダミーだけ削除」できる。dev データは識別子を最初から付けておくのが吉。

**残課題 / 次の方向**
- 実機で 30k ダミー生成 → 🧹 でダミーだけ消える / 本物 2000 が残ることを確認。
- スケール体感 (スクロール・連想の重さ) を 30k で観察。

---

## v30 — dev ダミー大量生成 (#dev) でスケール検証 (2026-05-29)

**背景**
- ユーザー: 手持ち写真が 2000 枚しかないが 30k スケールを試したい。「サンプル画像をネットでまとめて拾えるところは?」
- 正直な壁: **配布サイト (Unsplash/Pexels 等) の画像は EXIF (特に撮影日時) を削って配信**するのが普通 → このアプリは日時の無い写真をスキップ (`no-datetime`) するため、ネット画像を大量 DL してもほぼ入らない。日時が残るのは Flickr geotag / Wikimedia の一部くらいで 1万枚規模は手間。
- → スケール (機械) の検証には**アプリ内ダミー生成が圧倒的に楽で確実**。ユーザー選択で実装。

**設計判断**
- **`#dev` (URL hash) でだけヘッダに 🧪 を出す隠し機能**。通常 UI はゼロ干渉 ([[ui-minimalism-works]])。`location.hash` に 'dev' を含む時だけボタンを unhide + ハンドラ付与。
- **`devGenerate(N)`**: 日時 (直近~12年に散らす=各フィルタにデータが乗る)/GPS (近場70%・遠出20%・なし10%=near/far フィルタが効く)/色つきサムネ (24 hue を使い回し、色軸が hue でクラスタ)/color(48次元) を持つダミーを `dbPutMany` で 1000 件/トランザクションのバッチ生成。`blob` は null (thumb のみ, v23 準拠)。
- 生成後 **`location.reload()`** で実際の起動経路 (Phase 1 ランダム回転 + Phase 2 段階読み込み) をそのまま実数検証できる。
- 上限 100000、`prompt` で枚数指定、`confirm` で確認。中身はダミー (色の四角+日付) なので **機械の検証用 (起動/段階読み込み/回転/フィルタ/色・時空連想/メモリ)。reminiscence の「感じ」検証とは別**と明記。

**結果 / 観察**
- preview 検証: 関数定義 OK / dev ボタンは無 hash で hidden・`#dev` で表示 (配線確認) / `devGenerate(50)` で 50 件生成、各レコードが name=dummy- / datetime=Date / color len48 / thumb=Blob / blob=null / dedup=dummy- / GPS 45件(近35,遠10)+なし5 / 日時スパン ~11.6年。全 pass。
- 実機で 30k 生成 → 起動速度・段階読み込み・メモリ・発熱の実数は次回。

**教訓**
- 「テストデータをネットで集める」は、アプリの入力要件 (ここでは EXIF 日時必須) と配布画像の実態 (EXIF 削除) が噛み合わず詰むことがある。**要件に合うデータを自前生成する dev ツール**の方が速くて確実。`#dev` hash gate で本番 UI を汚さず同梱できる。

**残課題 / 次の方向**
- 実機で大量ダミー生成 → 30k の起動・メモリ・スクロール感を計測。重ければ thumb 別ストア化。
- (任意) meaning をスケールで試すならダミーに random embedding を持たせる版も追加可。
- 検証が済んだら 🗑 全削除でクリーンに戻す (ダミーと実写真が混ざらないよう注意)。

---

## v29 — 起動の初期プールをランダム回転に (2026-05-29)

**背景**
- v28 の段階読み込みで、ユーザーが鋭い指摘: **「起動において毎回同じ写真が読み込まれる？チャンクをランダムに選べない？」**。
- その通り。v28 の Phase 1 は `dbGetSome(null, 3000)` = UUID キー昇順の**先頭 3000 件固定** → 毎起動で同じ初期プール。「開いた瞬間の最初の3枚」(reminiscence の核) が常に同じ 1/10 から出る弱点。Phase 2 が数秒で埋めるとはいえ、第一印象が毎回同じなのは惜しい。

**設計判断**
- **Phase 1 を「ランダムな開始点からの回転」に**。`startKey = crypto.randomUUID()` を毎起動で生成し、`getAll(IDBKeyRange.lowerBound(startKey), INITIAL_LOAD)` で取得。UUID はランダムなので、ランダム開始点以降の N 件 = 全期間の代表サンプル、かつ**毎起動で違う**。
- **リング回り込み**: 開始点が後ろ寄りで N 件に満たなければ `getAll(upperBound(startKey, true), 不足分)` で先頭から補完。UUID 空間を環とみなして回す。これで枚数が N より多ければ常に N 件取れる。
- **Phase 2 は先頭から全件 walk + dedup に変更** (v28 は Phase 1 の末尾キーから前進だった)。Phase 1 がランダム位置になったので、Phase 2 は先頭から全件読み、Phase 1 で読んだ ~3000 は `addPhotos` の id 重複で弾く。再読込は ~10% だが背景なので許容。複雑な「補集合レンジ計算」を避けてシンプルに。

**結果 / 観察**
- preview 検証 (合成 100 件): ランダム開始点 6 回で **6 個すべて異なる部分集合** (len=30 固定・全て実在) → 回転で毎回変わることを確認。最大級の開始点で lowerBound=0 → 先頭補完で 30 (リング回り込み OK)。実コード経路 (INITIAL_LOAD=3000, 100件は全件) + Phase 2 で 100 件・二重化なし・全件回収。全 pass。
- 実機での「毎回違う3枚で開く」体感は次回。

**教訓**
- 「キー空間がランダム (UUID) なら先頭 N が代表サンプル」(v28) は正しいが、**先頭固定だと"代表"だが"毎回同じ"**。ランダム代表 *かつ* 毎回違う、を両立するには**開始点をランダムに回す**だけでよい (全件キー列挙やシャッフルは不要)。ソート済みランダムキー空間の「ランダム回転」は安価で強力。

**残課題 / 次の方向**
- 実機で起動のたびに初期 3 枚が変わるか・起動速度を確認。
- (将来) さらに重ければ thumb 別ストア化。

---

## v28 — 起動の段階読み込み (Phase 1: 数千で即UI / Phase 2: 残りを背景) (2026-05-29)

**背景**
- 30k 目標で残る最大の弱点 = **起動時に全件をメモリに読む待ち**。ユーザーが「めっちゃ気になる」と強く要望。
- ユーザー案: 「グルーピングしておいて、最初の読み込みは数千まで。その数千もグループからランダムに取る。初期 UI までお客さんを待たせない。触り始めてから残りを読み込む」。

**設計判断**
- **明示グルーピングは不要だった** — 写真の id は `crypto.randomUUID()`。IndexedDB はキー (=UUID) 昇順で取れる。**UUID は撮影日と無相関なので「先頭 N 件」= 全期間からのランダム代表サンプル**になる。→ ユーザー案「グループからランダムに数千」を、グループ構造を作らずキー順だけで実現 (実装が軽い)。
- **Phase 1**: 起動時に `dbGetSome(null, INITIAL_LOAD=3000)` で先頭 3000 件だけ読んで即 UI (`drawRandom`)。数万枚あっても起動待ちが出ない。
- **Phase 2**: `loadRestInBackground()` がユーザーが触り始めた裏で、`IDBKeyRange.lowerBound(lastKey, true)` で続きを `REST_CHUNK=2000` ずつ追い読み。`setTimeout(0)` で 1 フレームずつ譲りメイン操作を止めない。完了後に色 backfill。ヘッダに `📥 件数` を表示。
- **二重化防止**: `allPhotos` への追加を `addPhotos(records)` に一元化し `loadedIds` Set で id 重複を弾く。段階読み込み (Phase 2) と取り込み (fast/bg) が並行しても安全。全置換時 (バックアップ復元・全削除) は `resetPhotos` で `loadedIds` も貼り替え。
- **取り込みも全件読み直しをやめた** — 旧 `importFiles` は fast-track 後に `allPhotos = await dbGetAll()` で全件再読込していた (大量ライブラリへの取り込みで起動級の待ちが再発)。→ 新規分だけ `addPhotos` で追記。既存の thumb URL キャッシュも有効なので revoke 不要に。

**結果 / 観察**
- preview 検証 (合成 50 件 + 手動チャンク chunk=20): `dbGetSome(null,N)` は昇順 N 件 / チャンク走査が全 50 件を過不足なく1回ずつ昇順回収 / Phase1+loadRest で 50 件・二重化なし・全件回収・`restLoading` false / `addPhotos` が既存 id を弾く (既存1+新規1→+1)。全 pass。実 consts (3000/2000) の多チャンク経路は同じ `lowerBound` ページネーションを小 chunk で網羅検証。
- 実機 (1000〜30k) の起動体感は次回。Phase 1 が常に一定 (3000 件) なので枚数によらず起動は軽いはず。

**教訓**
- 「ランダム代表サンプルを取る」のに **ソート済みキー空間がランダム (UUID) なら、先頭 N がそのまま代表サンプル**。わざわざグルーピングやランダム抽選を組まなくてよい。データのキー設計が機能要件をタダで満たすことがある。
- 「待たせない」の本質は **必要な分だけ先に・残りは後で**。全部を速くするのではなく、初期 UI に要る最小限 (数千) を切り出す。

**残課題 / 次の方向**
- 実機で起動体感・Phase 2 完了までの時間・メモリを計測。さらに重ければ次段の thumb 別ストア化 (起動メタデータのみ + サムネ都度読み) を検討 (まだ不要のはず)。
- フィルタ (10年前 等) は Phase 2 完了前は母数が部分的 → 数秒後に full。気になるか観察。

---

## v27 — 意味解析(CLIP)をランダム標本に間引く上限 (2026-05-29)

**背景**
- ユーザーが上限の目安を **3万枚** に設定 (スマホ写真枚数分布: 1000未満ライト層 ~30-35% / 3000以上ヘビー層 ~20%、二極化)。
- ユーザー案: **「1万超えてきたら、時間かかりそうな部分でランダム間引きして処理。事前タグ付け/グルーピングでもいい」**。

**設計判断 (案を正しい1点に絞る)**
- **間引くべきは CLIP embedding 抽出だけ**。30k で各処理のコストを分けると: 色近傍/時空近傍/ランダム引き/フィルタは O(N) でも激安 (数ms) → **全件のまま** (これが「枚数=満足度」の源泉、間引くと逆効果)。重いのは CLIP 推論 (1枚ずつ・発熱・時間) の一点。なのでそこだけ間引く。
- **`MEANING_SAMPLE_CAP = 10000`**: embedding はこの枚数のランダム標本で打ち止め。自動抽出 (`maybeAutoExtractEmbeddings`, v20) は「残り枠 (cap - done) だけ」抽出し、上限到達で停止。`runEmbeddingExtraction` は元々シャッフル済 (v18) なので、上限で切る = 自動的に代表標本。
- **モーダルも整合**: stat に「解析済み N / 標本上限 10,000 (全 M 枚中)」、間引き説明 (「色・時空・ランダムは全件使う」)、解析ボタンは「残り枠だけ」(`toAnalyze = min(remain, room)`)。
- **JIT (タップ時にその場で中心を解析) は不採用**: CLIP をインタラクティブ経路に戻すと v20/v21 の「重い処理は裏だけ・前面でやると発熱&サムネ?化」に逆行。サンプルで割り切り、mix は意味が無い中心では時空+色に degrade。
- **グルーピング (層化サンプリング) は将来**: ランダムで偏りが気になれば、時期/イベント/場所ごとに満遍なく標本を取る方向 (ユーザー案の後半)。今はランダムで十分。

**結果 / 観察**
- preview 検証 (runEmbeddingExtraction を spy): 自動経路は done=3 で room=9997 を渡して呼ぶ / done=10000 (上限) で呼ばない。モーダルは total=12000・done=8000 で「標本上限 10,000 (全 12,000)」「ランダム標本に間引き」表示・ボタン「2,000 枚を解析」→ room=2000 で実行。全 pass。
- 実機での 30k 体感は次回。

**教訓**
- 「大量で重い」と感じた時、**全処理を一律に重い前提で間引くのは間違い**。コストを処理ごとに分解すると、重いのは1つ (ここでは CLIP) だけで、他は全件で良い。間引きは「安い軸の網羅性 (満足度の源泉) を守りつつ、重い1点だけ標本化」が正解。
- 別軸の弱点 (起動時の全件読み込み) は間引きでは消えない。混同せず分けて扱う。

**残課題 / 次の方向**
- 30k 実機計測。起動の全件読み込みがもっさりしたら thumb 別ストア化 (レイジー起動)。
- 偏りが出たら層化サンプリング (グルーピング)。

---

## v26 — クラウド非アップロードの注意書きを明示 (2026-05-29)

**背景**
- v25 まで実機完結 (コピー→写真アプリ検索で該当写真が出る、確認済)。
- 強い signal: **「写真の枚数が増えれば、増えるほど満足度が上がる」** = reminiscence はスケールで強くなる → ユーザーは全ライブラリを入れたくなる。
- だが「写真を UP する」「1万枚アップロード」という言葉は **クラウド送信の不安**を呼ぶ。ローカル完結 (写真は端末から出さない) が本プロジェクトの core 価値なのに、それが伝わっていないと大量投入の心理的ハードルになる。→ ユーザー要望「画像はクラウドに UP されない旨の注意書きを、わかりやすく」。

**設計判断**
- **privacy 文言は「取り込みの瞬間」だけに置く。reminiscence 画面 (random/explore) には出さない** ([[ui-minimalism-works]] の「思い出しの意識を写真から逸らさない」)。不安が出るのは取り込み時であって、写真を眺めている時ではない。
- 3 箇所に配置:
  1. **空状態** (初回=信頼を判断する場): 緑の枠で「🔒 写真はこの端末の中だけ。クラウドや外部サーバーにアップロードされません。すべてブラウザ内 (IndexedDB) に保存、AI 解析も端末内」。
  2. **準備中 / 取り込み中**: 「🔒 アップロードではありません。この端末内だけで処理しています」(UP してる感が出る瞬間に安心を添える)。
  3. **AI モーダル**: 既存「端末から出ません」を強化 → 「🔒 解析も端末内。写真はどこにもアップロード・送信されません (DL するのは AI モデルだけ)」。CLIP で「画像が AI サーバに送られる?」という典型的誤解を先回りで否定。
- 表現は専門語 (IndexedDB) を残しつつ、頭に **平易な一文 + 🔒** を置いて「わかりやすく」。

**結果 / 観察**
- preview 検証: 空状態に privacy ボックス、preparing/importing に privacy-line、AI モーダルに送信なし明示、reminiscence 画面には非表示。全 pass。
- 実機での文言の伝わり方・安心感は次回。

**教訓**
- ローカル完結は **黙っていると伝わらない**。むしろ「UP/取り込み」という UI 語彙がクラウド連想を呼ぶので、**価値 (プライバシー) は明示してこそ機能する**。ただし出す場所は不安が生じる瞬間 (取り込み) に限定し、体験の中心 (reminiscence) は汚さない。

**残課題 / 次の方向**
- 触り込みフェーズ: 大量 (1000〜) で「枚数が増えるほど満足度が上がる」をさらに観察。やめにくさ / 旅のヒント性。

---

## v25 — 写真アプリ動線: コピーを「日付のみ」に修正 (2026-05-29)

**背景**
- v24 を実機確認: 3点 (日時表示 / 写真アプリ起動 / コピー) とも OK。ただし **コピーした `2021/06/05 11:58` を写真アプリ検索に貼ると不一致**。「日にちで検索すると写真は出てくるが、時刻まで含めると引っかからない」(ユーザー報告)。

**設計判断**
- **コピー値を「日付のみ」に**変更。純正写真アプリの検索は日付では効くが時刻入りで落ちる → 時刻を外す。日本語端末なので **和暦 `2021年6月5日`** でコピー (numeric `2021/06/05` より日本語写真検索に通りやすい想定)。
- **時刻は表示には残す** (`📅 2021/06/05 11:58`)。役割分担: **日付でその日まで飛ぶ → 時刻で同じ日の中から目視で選ぶ**。時刻は検索キーではなく目視特定の手がかり。
- コピー後の hint を `「2021年6月5日」をコピー` にして、何がコピーされたか (= 検索に使う値) を明示。

**結果 / 観察**
- preview 検証 (clipboard を spy して捕捉): 表示は時刻あり `📅 2021/06/05 11:58`、**コピー値は日付のみ `2021年6月5日`**、hint も追従。pass。
- 実機で「コピー → 写真アプリ検索でその日が出る」かは次回確認 (和暦が最も通りやすいはずだが、ダメなら numeric や `6月5日` も試す)。

**教訓**
- **外部アプリへ橋渡しする値は、相手の検索仕様に合わせる**。こちらの「正確な情報 (時刻まで)」がそのまま相手で使えるとは限らない。表示用とコピー用を分けるのが正解だった (表示=人間の目視特定、コピー=機械の検索クエリ)。

**残課題 / 次の方向**
- 実機で和暦日付が写真検索に通るか確認。通らなければ format を調整。
- 触り込みフェーズへ。

---

## v24 — 純正写真アプリへの動線 (フル表示に日時 + 写真アプリ起動) (2026-05-29)

**背景**
- v23 で原画を持たなくなった → 「鮮明な写真を見たい」需要をどう満たすか。ユーザー判断: **「鮮明なのは純正写真アプリで見ればいい。写真に時間が入っているので見つけやすい」**。アプリ内に原画を抱える必要なし ([[storage-tradeoffs-accepted]])。
- ユーザー要望「純正写真アプリへの動線で良いアイデアがあれば」。

**設計判断 (と Web の硬い制約)**
- **Web から iOS 写真アプリの特定の1枚は直接開けない** — 写真の識別子を web は取得できず、写真へのディープリンク URL スキームも存在しない。`photos-redirect://` は写真アプリを開くだけ (非公式・特定写真には飛べない)。→ 「動線」の上限は **「アプリを開く + 日時を見せて自分で辿ってもらう」**。
- **長押しフル表示 (= 鮮明に見たい瞬間) にキャプションを追加**:
  - **日時を大きく表示** (`📅 2021/06/05 11:58`)。これが純正アプリで辿る鍵 (時刻まで出るので同日内でも特定しやすい)。
  - **日時タップでクリップボードにコピー** (純正アプリの検索に貼れる)。https でない/権限拒否時は「長押しでコピー」にフォールバック。
  - **「📷 写真アプリで開く」ボタン** (`photos-redirect://`)。開ければ写真アプリへ、ダメなら無反応 (日時表示が確実な土台)。
- キャプション内のタップは `stopPropagation` で overlay の閉じる動作を発火させない (画像タップ=閉じる、は維持)。

**結果 / 観察**
- preview 検証: キャプションに日時 `📅 2021/06/05 11:58` と「📷 写真アプリで開く」が出る、キャプションクリックで overlay は閉じない (stopPropagation)、日時クリックでコピー試行が落ちない (localhost はクリップボード拒否→フォールバック文言)、closeFullImage で後始末。全 pass。
- 実機での `photos-redirect://` の起動可否、クリップボードコピーの動作は次回確認。

**教訓**
- ユーザーが「鮮明な原画も欲しい」と言わず「純正アプリで見ればいい、日時で探せる」と割り切ってくれたのは大きい。**アプリが全部を抱え込まず、OS の標準アプリに役割を委ねる**設計が spike では軽くて強い。日時という既存メタデータが「外部アプリへの橋」になった。
- web の「できないこと」(特定写真ディープリンク) は早めに正直に伝え、できる範囲 (アプリ起動+日時) で最小実装。

**残課題 / 次の方向**
- 実機で `photos-redirect://` が iOS で写真アプリを開くか確認。開かない iOS 版なら日時コピー一本に倒す。
- (将来案) GPS つき写真ならフル表示に Apple Maps 動線も置けるが、これは「旅のヒント」検証フェーズで判断。
- 触り込みフェーズへ: 1000枚規模で連想の手触り・やめにくさ・旅のヒント性を観察。

---

## v23 — 1万枚スケール対応 (原画を捨て thumb のみ保存) (2026-05-29)

**背景**
- ユーザー要望「数回使った人が、多少時間かかってもいいから 1万枚を一括 UP したい」。時間は許容、本当の壁は **保存容量**。
- 現状 `importOne` は原画 JPEG (`blob`) も IndexedDB に保存 (拡大表示用)。1万枚 × 3〜5MB ≈ **30GB** → iOS Safari の quota 超過、または Safari による非永続ストレージ消去でデータ喪失リスク。

**設計判断**
- **原画保存をやめ thumb のみに倒す** (`KEEP_ORIGINAL = false`、`blob: null`)。10k でも数百MB に収まる。ユーザーが AskUserQuestion で「full画像の保存をやめ thumb のみ / 長押し原画拡大が使えなくなる tradeoff」を明示選択済み。
- **`THUMB_PX` を 300 → 512 に**。原画を持たない以上「長押し拡大」も thumb を使う → グリッド表示と拡大を 1 枚で兼用。512px は phone のフルスクリーンで実用品質、10k で ~500MB。**thumb サイズは sticky** (大量取り込み後に変えると 1万枚再処理) なので bulk 前に確定する判断。色特徴は 4x4 平均なので 300→512 でも既存写真との比較互換あり。
- **`showFullImage` は `photo.blob || photo.thumb`** にフォールバック。既存写真 (原画あり) は従来どおり鮮明、新規 (thumb のみ) は 512px 拡大。
- **export/import を null-safe に** (`p.blob ? ... : null`)。原画が無くてもバックアップが壊れない (かつ軽くなる)。
- **起動時に `navigator.storage.persist()`** を best-effort 要求 (大量サムネの消去耐性)。`estimate()` の usage/quota も console 出力し実機で容量を見られるように。await しない (起動を遅らせない)。

**結果 / 観察**
- preview smoke: 構文 OK (全関数定義)、`THUMB_PX=512` / `KEEP_ORIGINAL=false`、`showFullImage({blob:null, thumb})` が thumb で blob URL を作り overlay 開閉まで成功 (落ちない)、`requestPersistentStorage()` 例外なし。
- 実機での 1万枚: 取り込み所要・発熱・実 quota・消去有無・thumb 512 の体感品質は次回計測。

**教訓**
- 「大量対応」の本丸は速度より **容量と eviction**。Web の IndexedDB はサイズ無制限ではなく、非永続だと OS が消す → `persist()` と「原画を持たない」の両輪。
- 解像度パラメータが **取り込み時に焼き込まれる (sticky)** 場合、後から変えると全件再処理。大量投入の前にこそ「後で変えたくない値」を決めきる。

**残課題 / 次の方向**
- 実機計測 → 重ければ: dedup を一括 `getAllKeys('dedup')` で Set 化 (10k で 10k トランザクションを回避) / CLIP 自動抽出に低優先間隔 / thumb 品質再検討。
- **別 gap (今回対象外)**: export/import が `embedding` を含まない → バックアップで意味軸が失われる。10k のバックアップは thumb base64 だけで巨大になるため backup は元々小規模向け、という整理。
- 触り込みフェーズ: 1万枚で連想の手触りがどう変わるか (リッチ化 vs ノイズ増) を検証問いに追加。

---

## v22 — 差分取り込みフィードバック (2026-05-29)

**背景**
- ユーザー要望: ①「数回使った人が、多少時間かかってもいいから 1万枚を一括 UP したい」②「使い続けると新しい画像を UP するのが面倒。アプリを開くと差分が UP される / 差分ボタンで UP される仕組みが欲しい」。
- 調査で 2 つ判明: (a) **重複判定は既に実装済み** (`importOne` の `dedup = name|size|datetime` を `dedup` index で引いてスキップ)。つまり「同じ写真を選び直しても二重に入らない」差分の核は既にある。(b) ただし**重複は黙ってスキップ**されていて、ユーザーに「差分が起きた」フィードバックが無かった。
- 「アプリを開くと自動で差分 UP」は **web プラットフォームで不可能** — Web アプリは写真ライブラリを勝手に読めず、毎回ユーザーのピッカー操作が必須、バックグラウンド動作も不可 (Phase 1.8 の「待ちは iOS ピッカー側で web 不可侵」と同じ壁)。→ 実現可能な上限は「ボタンで選び直す → 既存はスキップして新規だけ入る」。

**設計判断**
- **新しい「差分ボタン」は足さない** — 既存の「+」が既に dedup 取り込み = 差分取り込みそのもの。同じ機能のボタンを増やすのは UI 重複 ([[ui-minimalism-works]] に反する)。代わりに**「+」に差分フィードバックを付けて差分が起きているのを可視化**する。これが「差分ボタンで UP」のユーザー期待の実体。
- `importStats = {added, dup, skipped, failed}` をセッション単位で集計 (`importFiles` 先頭でリセット)。fast-track と background の両ループで `tallyImport(importOne の戻り値)` 加算。完了時 (`importFiles` の no-rest 分岐 / `runBackgroundImport` 末尾) に `showImportSummary()`。
- 表示は **緑の `logInfo`** (エラーの赤 `logErr` と区別)。`$log` overlay を流用しタップで消せる ([[ui-minimalism-works]] の「居座らせない」)。エラー表示中に info が来たら赤を維持 (severity を消さない)。
- 全部重複なら専用文言「✓ 新規なし — 選んだ N 枚はすべて取り込み済みでした」で「差分ゼロ」を明示 (再 UP の不安を消す)。

**結果 / 観察**
- preview ユニット検証: `tallyImport` の加算 (ok→added / duplicate→dup / その他→skipped)、`showImportSummary` の 4 文言 (mixed / 全重複 / 失敗込み / 新規のみ)、緑 info クラス付与、サマリ後の `importStats` クリア、エラー表示中は info を付与しない (赤維持)。全 pass。
- 実機での「1万枚を選び直して差分だけ入る」体感・所要は v23 (大量スケール) と合わせて次回。

**教訓**
- ユーザーが「新機能が欲しい」と言う時、**実は機能は既にあって "見えていないだけ"** のことがある (ここでは dedup)。要望を機能追加に直訳せず、既存挙動の可視化で満たせないかをまず疑う。
- web の「できないこと」(自動ライブラリ読み取り) は早めに正直に伝える。期待を現実 (ボタン式が上限) に寄せてから最小実装する方が、無理筋を作るより速い。

**残課題 / 次の方向**
- v23: 1万枚スケール対応 (full blob 保存をやめ thumb のみに倒し IndexedDB 容量を節約、CLIP 自動抽出の発熱対策)。
- 大量再取り込み時、`dbHasDedup` は index 引きで速いが 10k 件で 10k トランザクション → 体感は v23 実機で要計測。重ければ一括 `getAllKeys('dedup')` で Set 化して in-memory 判定に。

---

## v21 — AI 自動解析後にサムネが「?」化するバグ修正 (2026-05-29)

**背景**
- v20 の自動 embedding 抽出を実機投入。🌀 (色) → 🧠 (AI) が自動で回ることは確認できたが、**解析後に explore のサムネの一部 (中心含む) が青い「?」(broken image) になる**症状をユーザーが報告 (スクショ)。
- ユーザーの問い「これは除外した画像?」→ **No**。除外写真は `filter(p => !p.excluded)` で表示候補から外れるのでカードごと出ない。出ているのに「?」= 別問題。

**設計判断 (原因と対処)**
- **原因**: `thumbUrls` Map がキャッシュする blob URL を、**CLIP の重い推論中に iOS Safari が巻き込んで無効化**する (CHANGELOG v5/v7 と同じ「canvas/Blob のメモリ管理が緩い」罠)。`backfillColors` は完了時に `revokeAllThumbUrls()` + 再描画で新 URL を発行しこれを回避していたが、**`runEmbeddingExtraction` にはこの後始末が無かった** → 無効化された URL が `<img src>` に残り「?」になる。
- **対処**: `runEmbeddingExtraction` 完了時に `revokeAllThumbUrls()` を呼び、`state==='explore'` なら `refreshNeighbors()`、そして**常に `render()`** して新しい blob URL で描き直す。`render()` は `renderAIModal()` も呼ぶのでモーダル表示中も同時に更新される (旧コードの `if (aiModalOpen) render()` 分岐は無条件 render に統合)。

**結果 / 観察**
- preview smoke (iOS 固有の無効化はデスクトップでは再現しないため構文・配線確認): スクリプトが構文エラーなくロード (全関数定義済)、`runEmbeddingExtraction` のソースに `revokeAllThumbUrls()` → `render()` の後始末が入ったことを確認。
- 実機での「解析後も全サムネが正しく描かれる」確認は次回。
- 補足: 解析ループ中 (完了前) は、モーダルを閉じて explore を見ていると mid-loop 再描画が走らない (`% 3` 描画は `aiModalOpen` 時のみ) ため一時的に「?」が出うる。完了時の後始末で必ず復旧する。backfill と同じ「重い処理中は許容、完了時に必ず描き直す」方針。

**教訓**
- **重い画像処理 (色 backfill / CLIP 推論) を足したら、完了時の `revokeAllThumbUrls()` + 再描画はセットで必ず付ける**。iOS の blob URL 無効化は「重い処理を新しく足すたびに再発する」類の罠。新しい重処理パスを追加する時のチェックリスト項目にする。

**残課題 / 次の方向**
- 実機で v21 を確認 → 「?」が消えること。もし特定写真だけ「?」が残るなら、それは thumb blob 自体の破損 (別問題) として個別調査。

---

## v20 — 新規取り込み時に embedding を自動抽出 (2026-05-29)

**背景**
- v19 で mix (時空+色+意味) が「混ざったほうがおもしろい、単体だと退屈」と実機で決着。mix が既定として正解と確定。
- だが意味軸の供給源は手動 🧠 backfill だけ。**新規取り込みした写真は embedding 未保持のまま** → 時間が経つほど mix の意味軸が新しい写真で薄くなる (既定体験がじわじわ劣化する)。
- ユーザー選択で「新規取り込み時の自動抽出」を実装する方針に。Phase 2 TODO の最後の本実装項目。

**設計判断**
- **`maybeAutoExtractEmbeddings()` を新設**し、取り込み完了パスの `backfillColors()` の後にチェーン (`.then(maybeAutoExtractEmbeddings)`)。対象は 2 箇所のみ: `importFiles` の fast-track だけケースと `runBackgroundImport` の末尾。
- **opt-in ゲート = `aiEmbeddedCount() > 0`** — 「AI を一度でも使った人 (embedding を 1 枚以上持つ)」だけ自動起動。未 opt-in (embedding ゼロ) には**一切何もしない**。これで未試行ユーザーに 100MB DL を強制しない ([[ui-minimalism-works]] の最小干渉)。モデルは初回 opt-in 時に DL 済 → 2 回目以降は Cache Storage から高速復元 ([[meaning-walk-works]] の性能 signal)。
- **起動時 (1595行) と backup インポート (1568行) には付けない** — 「新規取り込み」だけが対象。起動のたびに CLIP が回るのは過剰。backup は embedding を JSON 同梱で復元するので不要。
- **色 backfill の後に逐次実行** (同時並行にしない) — canvas/getImageData (色) と CLIP 推論を同時に走らせると iOS のメモリ/発熱が厳しい。軽い色 backfill を先に終えてから重い embedding を回す。
- 既存の `runEmbeddingExtraction()` を limit 無し (未解析の全件) でそのまま再利用。新規取り込み分だけが未解析なので、実質「新しく入った写真だけ」を解析する。

**結果 / 観察**
- preview ゲート検証 (`runEmbeddingExtraction` を spy に差し替え、実 DL を起こさず判定): opt-in 無し+未解析あり→起動せず / opt-in あり+未解析あり→1 回起動 / 全部解析済→起動せず / 既に解析中→起動せず / 未解析が除外のみ→起動せず。全 5 ケース pass。
- 実機で「新規取り込み後に勝手に 🧠 が回って新しい写真も意味で繋がる」体験の馴染み・発熱は次回検証。

**教訓**
- 自動起動系は **opt-in 状態を既存データから推論できる形** (embedding を持つ=opt-in 済) にすると、新しいフラグ/設定を増やさずにゲートできる。`pms-phase2-tried` localStorage も使えたが、「意味軸を維持する価値があるのは embedding を既に持つ人だけ」という意味的に正しい判定が `aiEmbeddedCount() > 0` 一本で済んだ。
- 重い処理 (色 / CLIP) は「同時に走らせない」を設計の既定に。チェーン (`.then`) で逐次化するだけで iOS のメモリ脆弱性 (CHANGELOG v5, v7) を避けられる。

**残課題 / 次の方向**
- 実機で新規取り込み → 自動抽出の発熱/バッテリー/所要時間を計測。重ければ抽出ループに低優先 setTimeout 間隔を入れる (今は `setTimeout(0)`)。
- 大量取り込み時、ヘッダ 🧠 進捗は出るが**自動起動だと中断 UI が無い** (中断ボタンはモーダル内のみ)。実機で「止めたい」場面が出たらヘッダからの中断 or モーダル誘導を検討。
- これで Phase 2 本実装の主要項目は完了。次は実装ゼロの触り込み + 検証問い観察フェーズへ。

---

## v19 — 意味軸を mix プールに合流 (2026-05-29)

**背景**
- v18 で「意味での手触り最高」を実機検証 ([[meaning-walk-works]])、全件 backfill も用意済み。だが「🧠 意味」はまだ explore の独立モードで、既定体験 (🎲 ミックス) には入っていなかった。
- pilot で意味を分離していたのは色ジャンプ ([[color-jump-works]]) との比較のため。刺さりが確認できたので「軸を選ばせず混ぜる」既定 ([[mix-walk-works]]) に統合するのが TODO・両メモリ・v18 残課題すべてが指す次手だった。

**設計判断**
- **`mixedNeighbors` を 時空+色 → 時空+色+意味 の3軸純シャッフルに拡張**。`meaningNeighbors(center, all, limit)` を 1 行足してプールに合流させるだけ。比率は固定しない ([[mix-walk-works]] の「割合は固定しない」を踏襲、3軸でも純シャッフル)。
- **graceful degrade を維持** — 中心に embedding が無ければ `meaningNeighbors` は空を返し、自然に時空+色だけで混ざる (色が無ければ時空だけ、と同じ pattern)。未 opt-in / 未 backfill のユーザーは v18 と完全に同じ挙動。新フラグも分岐も足していない。
- **これは配線でなく実験** — TODO Phase 2 の検証問い「AI の意味ワープが完璧すぎると標準フォトアプリの AI アルバムと既視感が出ないか → あえて時空/色のノイズを混ぜると連想がドライブするか」を、mix 合流そのものが試す。だから「意味単独 (🧠)」モードは残し、mix と単独を実機で比較できる状態にした。

**結果 / 観察**
- preview 合成検証 (T=時空近傍のみ / C=色近傍のみ / M=意味近傍のみ になるよう disjoint な合成写真 36 枚を構成): 各軸が純度 12/12 で独立に正しい近傍を引き、mix 単発で `{時空5, 色5, 意味2}` と 3 軸から混ざることを確認。embedding を剥がすと意味軸が空になり mix が時空+色だけに降格 (M は一切出ない)。
- **実機フィードバックで検証問いに決着: 「混ざったほうがおもしろい、単体だけだとランダムのおもしろさがなくて退屈」**。= ノイズ(混合)が連想を駆動し、意味単独 (=完璧な AI 整列) は退屈 → 「標準アプリの AI アルバム既視感」問題は mix が解決していた。意味軸ですら単独だと退屈になる = mix の「何が出るか分からなさ」こそが報酬 ([[mix-walk-works]] に追記)。mix を既定にした判断が正解と確定。

**教訓**
- mix を最初から「持っている軸だけで混ざる」設計 ([[mix-walk-works]]) にしておいたので、新軸の合流が `meaningNeighbors` 呼び出し 1 行で済んだ。spike でも「軸を足せる器」を素直に作っておくと統合コストがほぼゼロになる。

**残課題 / 次の方向**
- 実機で mix vs 意味単独の手触り比較 → mix の意味配分が薄すぎ/濃すぎなら調整 (今は純シャッフルで各軸均等期待値)。
- 新規取り込み (v7 フェーズB) に embedding 抽出を相乗り — ただしモデル DL 済み (AI opt-in 済み) の人だけ自動抽出する設計が要る (未 opt-in に 100MB DL を強制しない)。

---

## v18 — Phase 2 実機 GO → 全件 backfill 化 (2026-05-29)

**背景**
- v17 pilot を実機投入。ユーザー検証: **「意味での手触り最高」「モデルの読み込み時間はかかるけど、そのあと画像の解析はそんなに時間かからない。300枚ぐらいまでだったら待てそう」** ([[meaning-walk-works]] に記録)。
- = Phase 2 本実装 GO。事前懸念「色ジャンプ ([[color-jump-works]]) が既に刺さってるから意味の限界価値は小さいのでは」は外れ、意味ワープは別格に刺さった。
- pilot の 50 枚 cap は「未解析の写真に意味モードがよく当たる」体験の穴 → 全件解析できるようにするのが最優先 (ユーザー選択)。

**設計判断**
- **`PHASE2_BATCH=50` cap を撤廃** — `runValidationBatch()` → `runEmbeddingExtraction(limit)` に改名・一般化。`limit` 無しで未解析の全件を処理。性能 signal「~300枚まで待ち許容」が cap 不要の根拠。
- **中断ボタン追加** (`aiCancel`) — 300枚規模の長時間 run に備え、抽出ループ中だけ「中断」を出す。DL 中は中断不可 (pipeline init は分割できない)。
- **ヘッダ 🧠 進捗** — `updateBgStatus` を拡張し、モーダルを閉じても `🧠 40/300` が出る (取り込みの 🌀 と同列、色は `.ai-bg` で薄紫に差別化)。3枚ごとに更新。
- **抽出完了で explore 自動更新** — backfill 中に explore を開いていたら `refreshNeighbors()` + `render()` で意味モードの近傍を反映。
- **シャッフルしてから解析** — 途中中断しても満遍なく解析済みになるよう、対象を毎回シャッフル。

**結果 / 観察**
- preview synthetic test: 主ボタンが「6 枚を解析」(=残り全件、50固定でない)、stat「解析済み 4/10 (未解析 6)」、抽出中は「閉じる(処理は続く)」+「中断」、進捗バー %、ヘッダ `🧠 42/300` 表示→完了で hidden。
- 実機での 379 枚全件 backfill の所要時間は次回計測。

**教訓**
- pilot → 本実装の移行は「cap を外す + 中断/進捗を足す」だけで済んだ。**pilot を本実装の縮小版として最初から同じ関数で組んでおいた**ので差分が小さい (validation 専用コードを捨てる無駄が出なかった)。

**残課題 / 次の方向**
- 新規取り込み (v7 フェーズB) に embedding 抽出を相乗り (今は手動 🧠 のみ、新規写真は未解析のまま)。
- mix プールへの meaning 合流 ([[mix-walk-works]] の時空+色に意味を足す)。
- 実機で全件 backfill の所要時間・発熱・バッテリーを計測。重ければ低優先 setTimeout 間隔を入れる (今は `setTimeout(0)`)。

---

## v17 — Phase 2 pilot: 「意味の近傍」を 50 枚スケールで価値検証 (2026-05-29)

**背景**
- v5 の色ジャンプが「夜祭と夜景」級の刺さりを出し、v12 で mix が「より歩きたくなる」に到達。
- Phase 2 (CLIP) は「意味ワープ」を足せるが、本実装はモデル DL ~100MB + 全件 backfill (CPU/WebGPU でかなり重い) + 検証問い「色 vs 意味どちらが報酬系に効くか」が未確定。
- → **本実装前に opt-in で 50 枚だけ embedding を抽出し、explore の「🧠 意味」モードで手触り比較**する pilot を組む方針 (ユーザー合意)。

**設計判断**
- **完全 opt-in**: ヘッダに 🧠 ボタン + 専用モーダル。デフォルト体験ゼロ干渉 ([[ui-minimalism-works]] 遵守)。試さなければ Phase 1 と同じ挙動・同じ通信量。
- **モデルは `Xenova/clip-vit-base-patch32` (~100MB)** を採用。理由: 「pilot で意味ワープが弱かった時に『モデルが弱かったせい』を切り分けたい」→ 標準モデルで判定する。lighter (MobileCLIP s0 etc.) は本実装の最適化フェーズで検討。
- **ESM 動的 import** (`await import('https://cdn.jsdelivr.net/npm/@huggingface/transformers@3.0.2/dist/transformers.min.js')`) を opt-in ハンドラ内で。v2 教訓の「ESM 動的 import 不安定」は app 起動時の話 → 機能ハンドラ内ならリスク許容。
- **50 枚 / 1 バッチ** (`PHASE2_BATCH=50`) ランダムサンプル。「もっと解析」ボタンで追加 50 枚ずつ増やせる。最初から全件 backfill しない理由: pilot は **撤退可能** を死守、刺さらなければ 🗑 全削除で消える状態を維持。
- **IDB スキーマ**: 既存 record に `embedding: Float32Array(512)` を append (sparse)。色 backfill と同じ「持ってる人だけ」方式。スキーマ version は上げない (既存 record 互換)。
- **explore モード切替に「🧠 意味」を追加**するが、`aiEmbeddedCount() >= 3` の時だけ出現。pilot 未試行ユーザーには見えない。
- **中心が embedding 未保持** → 「この写真はまだ AI 解析されていません (🧠 から追加解析)」と説明的に degrade (color モードと同じ pattern)。
- **cosine 類似度**: embedding を `normalize: true` で抽出しているので内積 = cosine。`meaningNeighbors` は単純 dot product で済む (512 dim × 50 photos = 26K mul / tap、ms 以下)。

**結果 / 観察**
- preview synthetic test (Float32Array(8) を 7 枚に sin で生成して類似度判定): meaningNeighbors の並び順が seed の近さと一致、mode toggle に 🧠 意味 が embedding ≥3 で出現、no-embedding center で fallback ラベル、aiEmbeddedCount 正しく追従、モーダルが btnAI / 背景タップで開閉。
- 実機での「pilot を回した時の体感」「色 vs 意味の刺さり比較」は次回検証。

**教訓**
- pilot は「撤退可能」を最初から組み込んでおくと心理的負荷が下がる (「ダメだったら消せばいい」)。spike の「捨ててよい」精神を機能粒度に適用。
- モード追加 (mix / 時空 / 色 / 意味) も「特定条件で出現」にすれば、未使用ユーザーへの UI 干渉ゼロを保てる ([[ui-minimalism-works]])。

**残課題 / 次の方向**
- 実機で 50 枚解析 → 「意味」モードを 1 回触って手触り判定。所要時間 (DL 数分 + 解析数分?) も測る。
- 撤退する場合の「embedding だけ消す」ボタンが無い (今は 🗑 全削除のみ) → pilot が刺さらなかった時に追加検討。
- WebGPU vs WASM の自動切替は transformers.js デフォルトに任せている。iOS 18+ で WebGPU が効くはず。実機 console で確認余地。
- mix への合流は本実装時 (pilot 段階では分離したまま比較する方が判定しやすい)。

---

## v16 — 取り消しで赤エラー固定化のバグ修正 (2026-05-29)

**背景**
- 実機 (iOS, 379枚)で「除外 → 取り消す」を押すと画面下に
  `[reject] null is not an object (evaluating 'e.message')` が出て、ずっと残る。
- ユーザー報告のスクショで挙動確認。取り消しが効いてないようにも見える。

**設計判断 (原因と対処)**
- **真の原因は3層**:
  1. `dbPut` が `reject(tx.error)` していたが、**iOS Safari は `tx.error` が `null` で onerror を発火させるケースがある**。
  2. 受け側 `catch (e) { logErr(\`...${e.message}\`) }` が**`e === null` で `e.message` 読みに失敗 → 再 throw**。async 関数が reject、click handler が await してないので **unhandledrejection** に昇格。
  3. `logErr` は append-only で **dismiss 手段なし** → メッセージが永久に居座る。
- **副作用**: catch が throw すると後続の `render()` が走らず、**取り消しの反映が画面に出ない** (メモリは `excluded=false` になっているのに見た目は除外のまま)。これがユーザーの「取り消すが効かない」感覚の正体。
- **対処**:
  - `dbPut` を `tx.error || new Error('IDB transaction aborted')` に。null reject を根絶。`onabort` も同様に。
  - `excludePhoto` / `undoExclude` を**描画先行 / DB 保存は後**に並び替え。保存失敗でも UX をブロックしない (メモリ状態が真実、DB は best-effort)。
  - catch を `e?.message || String(e)` に。global `error` / `unhandledrejection` ハンドラも同じく optional chaining。
  - `$log` にクリックで `innerHTML=''` + `classList.remove('show')` を仕込み**タップで消せる**ように。

**結果 / 観察**
- preview synthetic test (dbPut を `reject(null)` に差し替えて再現): 旧版なら謎の `[reject] null is not an object...` が永久残留 → 新版は `除外保存失敗: null` / `取消保存失敗: null` という読めるメッセージ、`excluded` フラグもメモリ上は正しく反転、3 枚キープも維持、ログはタップで消える。
- 実機で実際に dbPut が成功するなら、そもそも何のメッセージも出ない (今までは catch 内の throw で誤って [reject] を量産していた)。

**教訓**
- **iOS Safari の IndexedDB tx.error は null になりうる**。`reject(tx.error)` だけだと catch 側でこのバグを必ず踏む。常に `tx.error || new Error('...')` で防御。
- **catch 内で生メッセージを `e.message` で読むのは罠**。`e` が null/undefined/string のケースに備えて `e?.message || String(e)` をパターン化。catch が throw すると後続の同期処理 (render など) が静かにスキップされ、UX のバグになる。
- **logErr 系は dismiss 手段を必ず付ける**。spike でも「永久残留」は怖い (ユーザーが「動いてない」と誤解する)。

**残課題 / 次の方向**
- 実機で 379 枚規模で「除外 → 取り消す」を再現し、本物の dbPut が成功してログ自体出なくなることを確認。
- もし dbPut が本当に失敗しているなら別問題 (quota? 一時的な競合?) として深掘り。今は「失敗しても UX は壊さない」までで止める。

---

## v15 — 時間軸フィルタに 3ヶ月前 / 3年前 を追加 (2026-05-29)

**背景**
- これまで時間軸チップは「この時季 / 1年前 / 10年前 / 久しぶり」で、1年と10年の間が空き過ぎ。3年前 (最近の中で「ちょっと前」感) と 3ヶ月前 (季節は同じだが少し前) の二段がほしいというユーザー要望。

**設計判断**
- **粒度を 5 段階に**: この時季 (0) / 3ヶ月前 / 1年前 / 3年前 / 10年前。等間隔ではなく対数的に間隔を広げる (人の記憶の歪み方に合わせる、最近は密に・遠くは粗に)。
- **3ヶ月前の幅 ±15日** — `this 時季` (年問わず ±15日) と被るリスクはあるが、こちらは「今年の」3ヶ月前を狙うので役割は別。`setMonth(now.getMonth() - 3)` で月演算。
- **3年前の幅 ±30日** — 1年 (±15) と 10年 (±60) の中間。サンプルが薄くなる前提で広めに。
- **pickPool の `1y || 10y` 分岐を `1y || 3y || 10y` に拡張** (`years` と `widthDays` を key で分ける)。3m だけ月演算が混ざるので別ブロックにした。

**結果 / 観察**
- preview synthetic test (offsets [0, 90, 365, 365*3, 365*10] 系の合成データ): チップ順 [全期間/この時季/3ヶ月前/1年前/3年前/10年前/久しぶり/たくさん撮った日/近場/遠出] で表示、3m→3枚・3y→2枚 が正しく抽出、母数脚注も追従。
- 実機での「3ヶ月前/3年前 が記憶のフックとして効くか」は次回触り込みで観察。

**教訓**
- 時間軸の粒度は等間隔より対数的 (近くは密・遠くは粗) が記憶想起の体感に合いそう。今後 5年前/30年前 も足す余地。

**残課題 / 次の方向**
- 3m が「この時季」と意味的に被って見えないか (前者は今年の3ヶ月前、後者は年問わずの同月日)。脚注の母数差で違いは判別可能だが、ラベルで誤解が出るなら "今年の3ヶ月前" 等への変更も。

---

## v14 — 除外したら即座に別の1枚を補充 (2026-05-29)

**背景**
- v13 で ✕ 除外を入れたが、ランダム3枚から1枚消すと2枚になる。ユーザー要望「✕ で一枚減ったらすぐ別の一枚が出てくるように」。

**設計判断**
- **「先頭N枚を表示」モデルに変更** — `currentRandom` に**全候補をシャッフルして保持**し、表示は `filter(!excluded).slice(0,3)`。explore グリッドも近傍を **12枚キャッシュ**して `slice(0,6)` 表示。
- これにより: 除外 → filter で消える → slice が次の候補を繰り上げる → **常に3枚 (グリッドは6枚)**。配列をいじらず render の slice だけで補充が成立。
- **取り消しも自動で正しい** — un-exclude すると filter に戻り slice が元の順序を再現 → **除外前の3枚に完全復元、他の2枚は終始不動**。excludePhoto 側の特別処理は不要 (render の slice が全部吸収)。

**結果 / 観察**
- preview test: 除外後もカード3枚キープ・他2枚不動・新1枚が末尾に追加・取り消しで元の3枚に完全一致。explore グリッド 6→6。

**教訓**
- 「消えたら次が出る + 取り消しで戻る」は、配列の削除/挿入で実装すると undo が難しい。**全候補を順序固定で保持し、表示は filter+slice** にすると除外・補充・取り消しが副作用なく一貫する。

---

## v13 — ワンタップ除外 (negative curation) (2026-05-29)

**背景**
- mix 連想が刺さって「より歩きたくなる」状態 ([[mix-walk-works]]) → だからこそ歩く中で出る「気に入らない写真」を消したい欲求が顕在化。
- ユーザー要望「ワンタップで気に入らない写真を二度と表示しない」。
- TODO の「今は作らない」にあった negative curation だが、そこに *「Phase 1 検証中に必要性が出てきたら追加検討」* と明記 + 検証問い「悪い写真で気分が落ちたか (curation 必要量の signal)」のゲート条件が成立 → 着手。

**設計判断**
- **非破壊フラグ方式** — レコードに `excluded: true` を立てるだけ (blob は消さない)。理由: 一度きりの誤タップで写真を永久削除するのは怖い。random/explore/母数/枚数すべてで `!p.excluded` で弾く。エクスポート/インポートにも `excluded` を含めて永続。
- **ワンタップ = 各写真右上の ✕** — カード / explore 中心 / グリッドサムネすべてに小さな半透明 ✕。ユーザー明示の「ワンタップ」要望を優先 ([[ui-minimalism-works]] とは緊張するが、控えめな半透明・押下で赤に留める)。
- **親ジェスチャとの衝突回避** — ✕ は pointerdown/move/up/click すべて stopPropagation。これをしないとカードの長押し (フル画像) や tap (explore) が誤発火する。
- **誤タップ救済** — 除外直後に「1枚を除外しました [取り消す]」トースト (5秒)。`excluded` フラグなのでワンタップで完全復元。
- **中心を除外したら random へ** — explore 中心写真を ✕ したら見るものが無くなるので別の3枚へ。

**結果 / 観察**
- preview synthetic test: random で ✕→2枚に+トースト+count 8→7+DB 永続、取り消し→3枚復活+DB false、explore 中心/グリッドに ✕、中心除外→random 遷移、全確認。
- 実機での「除外が手に馴染むか / 誤タップ頻度」は次回観察。

**教訓**
- 子要素ボタンを長押し/タップ領域に重ねる時は **pointer 系イベントを全部 stopPropagation** しないと親ジェスチャが誤発火する (attachPhotoGestures との併用)。

**残課題 / 次の方向**
- 除外した写真を見返す/まとめて戻す画面は未実装 (今はトーストのワンタップ取り消しのみ)。必要になったら追加。
- ✕ が常時表示でうるさく感じないか実機で確認。うるさければ長押しフルビュー内に逃がす案。

---

## v12 — explore をミックス既定に + モード切替を開閉式に (2026-05-29)

**背景**
- これまで explore は「⏳ 時空 / 🎨 カラー」の二択トグルで、どちらか一方の近傍6枚を表示。
- ユーザー要望: **基本は時空と色を混ぜたランダムが並ぶ (合計6枚以内、割合は固定しない)**。トグルは普段隠して、必要な時だけ出せるように。

**設計判断**
- **既定モードを `mix` に** — `mixedNeighbors`: 時空近傍と色近傍を1プールに統合 → 重複排除 → **純シャッフル** → 上位6枚。
  - 「割合は固定しない」= 3+3 のような固定配分にせず、シャッフルで時空/色の比率を毎回ランダムに (実測で 4/0〜1/3 までばらける)。
  - 中心に色が無ければ色近傍は空 → 自然に時空のみへ degrade。
- **再シャッフル抑制** — `currentNeighbors` にキャッシュ。中心 or モード変更時だけ `refreshNeighbors()` で再計算。モード開閉やバックフィル再描画ではシャッフルし直さない (グリッドのチラつき防止)。
- **モード切替を開閉式に** — 普段は小さな「モード ▼」opener のみ ([[ui-minimalism-works]]: 使う時だけ広がる)。展開すると `🎲 ミックス / ⏳ 時空 / 🎨 カラー` の3択。
- **選択を localStorage 永続** (`pms-exploreMode` / `pms-modeToggleVisible`) — 「常に出す/隠す」の choice が次回も効く。private mode 例外は try/catch で握り潰し。

**結果 / 観察**
- preview の synthetic test で確認: mix は常に ≤6、比率がばらける (3/1,1/3,2/2,4/0…)、既定 mix + トグル隠し、opener で展開→3択表示。
- 実機での「混ざった連想がより歩きたくなるか」は次回触り込みで観察。

**教訓**
- 「割合を固定しない」は固定配分ロジックより**純シャッフルの方が要望に忠実**。設計を足し込むより引いた方が要望に合うことがある。

**残課題 / 次の方向**
- mix が時空/色どちらに偏ったか分かる微マーカーが要るか (今は無印、まず無印で観察)。
- Phase 2 (CLIP) の「意味」軸も将来この mix プールに合流できる。

---

## v10–11 — 取り込み待ちの注意書き (追加 → ランダム画面上部へ移設) (2026-05-29)

**背景 / 判断**
- v9 で「待ちは iOS 側 (短縮・タップ時表示は不可)」と確定 → ならば**事前に期待値を伝える**のが最善、というユーザー判断。
- preparing 画面の文言も「写真が多いと選択後の準備に数十秒かかることがあります」に具体化。
- 文言は v8 の実測に合わせて正直に: **百枚規模で数十秒**、準備後は裏で読み込みつつ使える (v7 背景パイプライン)。
- 置き場所は当初 empty 画面に置いたが、**ユーザーが実際に居るのはランダム画面 (412枚で empty は出ない)** ため移設 → **ランダム画面の最上部 (ヘッダ + の直下)** に変更 (`.caveat`)。
  - `ui-minimalism-works` との緊張はあるが、ユーザー明示要望。footnote 級に dim/小さく (11px #777) してデフォルト体験への干渉を最小化。

**残課題**
- 再取り込み時 (ヘッダ +) のネイティブピッカー前面区間には出せないため、preparing 画面の一瞬の文言で代替。

---

## v9 — 計測撤去 + 読み込み中表示の強化 (待ちは iOS 側と確定) (2026-05-29)

**背景**
- v8 の計測を実機で実行。結果: **`取込処理 0.8s`(我々) / `+押下→取込開始 44.2s`(iOS 準備 + 選択操作)**。
- = 待ち時間は **100% iOS ネイティブ写真ピッカーの中** (`change` 発火前)。我々のコードは 0.8 秒で完了しており、ボトルネックではないと確定。
- ユーザーの要望「タップした瞬間に『読み込み開始』とか出せないか」への回答も兼ねる。

**設計判断 / 確定した制約**
- **☑(Done) タップは iOS ネイティブピッカーのボタン。web には一切イベントが届かない。** 我々のコードが起きるのは `change` で、それは iOS が全ファイル準備を終えた後 (=18〜44秒後)。よって「☑ を押した瞬間」にこちらから何かを出すことは原理的に不可能。
- **反応できる唯一のタップは「+」(我々のボタン)** → `openPicker` で即 `preparing` 画面を出す。これは既存。
- **計測コード (v8) を全撤去** — `pickerOpenedAt/changeFiredAt/lastImportSummary/elapsedTimer`、importing の経過カウンタ、random の 📊 行。デフォルト体験を最小に戻す ([[ui-minimalism-works]])。
- **preparing 画面を強化** — CSS スピナー + 「大量の写真は iOS の準備に時間がかかります」。我々の画面が見える区間 (ピッカーが閉じてから change まで) でだけ効くが、見えるなら確実に「動いている」と分かるように。

**結果 / 観察**
- 待ちの主因が iOS 側と判明したことで、「短縮」「タップ時表示」はこの区間には効かないと結論。背景パイプライン (v7) で準備さえ終われば即遊べる (`🌀 N/M`) のが唯一にして有効な緩和。

**教訓**
- **ネイティブモーダル (写真ピッカー) が前面の間、web は完全に蚊帳の外** — イベントも描画権も無い。「ロード表示」を出せるのは自前の DOM が前面にある瞬間 (=「+」押下時) だけ。
- 体感の遅さを web のせいと早合点せず、`change` 基準で測ったのが効いた (v8)。

**残課題 / 次の方向**
- 一括ロードの一度きりコストとして受容。どうしても短縮したいなら「ファイル」アプリ経由で HEIC 原本を受け取り iOS 変換を回避する案があるが、fast-track が重くなる + ピッキング手順が変わるため保留。

---

## v8 — 取り込み18秒の切り分け計測 (診断用・後で外す) (2026-05-29)

**背景**
- v7 投入後、実機で **200枚取り込み → ☑ から次画面まで18秒** という報告。
- ユーザー観察: ☑ を押した後「写真(ピッカー)がそのまま」18秒表示され続け、その後切り替わる。
- 仮説: この18秒は iOS ネイティブピッカーが200枚を準備 (HEIC→JPEG 変換 or サンドボックスへのコピー) している時間で、`change` 発火**前**。我々のコードは一行も動いておらず、ピッカーが画面を覆っているため web 側から短縮も表示も不可能 — を**計測で確定させる**ことにした。

**設計判断**
- **`change` 発火を基準に2区間を分離表示** — `pickerOpenedAt` (「+」押下) と `changeFiredAt` (change 発火=我々の始動) を記録。
  - `+押下→取込開始` = iOS 準備時間 + ユーザーの選択操作時間 (混在に注意)
  - `取込処理` = `change`→random 表示。**純粋に我々の処理時間** (fast-track 6枚 + dbGetAll + drawRandom)
- random 画面に `📊 …` の控えめな1行で出す (スマホで console 開けないため画面表示が必須)。importing 画面には経過秒ライブカウンタも併設。
- **これは診断用。切り分けが済んだら外す** (BUILD/コメントに明記)。

**結果 / 観察**
- preview の synthetic test で計測ロジック・画面表示を確認。実機での数値待ち。
- 期待: `取込処理` が小さく (数秒) `+押下→取込開始` が ~18秒なら、18秒は iOS 側と確定 → 次の手 (分割取り込み or Files経由HEIC自前変換) に進む。

**教訓**
- iOS ネイティブモーダル (写真ピッカー) が前面の間は web から手出し不能。「短縮/表示」を実装する前に、**問題が `change` の前か後か**を測るのが先。`change` 基準の計測がその切り分けに効く。

**残課題 / 次の方向**
- 実機の数値次第。iOS 側確定なら計測コードを撤去し、分割取り込み最適化 or HEIC 自前変換 (背景パイプライン相乗り) を検討。

---

## v7 — Phase 1.8: Progressive Indexing (取り込み待ち時間ゼロ化) (2026-05-29)

**背景**
- 実機の痛点: 数十枚選んで ☑ を押してから「取り込み中」画面に変わるまでが長く、その間に連打すると先に進まなくなる (詰まる)。
- 原因は 2 つ: (1) iOS が Done 後にファイル準備 (HEIC→JPEG 変換等) で数秒かかり、その間 `change` が発火せず画面が変わらない。(2) その隙に再タップ → 多重 `change` / picker 再オープンで共有 state が壊れる。
- TODO の Phase 1.8 (大量投入でも待ち時間ゼロでメインに入る) に合致するので、ここで対処。

**設計判断**
- **picker を開いた瞬間に画面を切り替える** — `openPicker()` で即 `state='preparing'` (「📷 写真を読み込んでいます…」) に。iOS の準備ギャップ中も画面が変わって見える。ユーザー要望「押したらすぐ別画面に」に直接応える。
- **連打詰まり対策は 2 段ガード** — `importBusy` (picker open〜完了/キャンセル、多重 open 防止) + `importingNow` (実処理中、多重 change 弾き)。詰まりの根本 (多重 change の同時実行) を断つ。
- **キャンセル復帰は 2 重化** — ネイティブ `cancel` イベント (Safari 16.4+) + preparing 画面の「やめる」ボタン。古い環境で `cancel` が来なくても恒久ロックしない保険。
- **フェーズA/B分割 (Progressive)** — 先頭 `FAST_TRACK_COUNT=6` 枚だけ進捗バーで処理 → 即 random 画面へ。残りは `enqueueBackground` でキュー化、1本のループが `BG_IMPORT_DELAY=120ms` 間隔で低優先消化。完了で `🌀 12/30` インジケータが消える。
- **背景処理は並行でなく直列** — TODO 案は「先頭を並行処理」だったが、iOS の canvas/Blob メモリ脆弱性 (CHANGELOG v5) を踏まえ HEIC 変換の同時実行を避け直列に。fast-track を 6 に絞ることで体感速度を確保。
- **再インポートはキューに積むだけ** — 多重バックグラウンドループを作らず、走行中なら `bgQueue.push` で合流。

**ハマったところ**
- **`nextPaint()` (二重 rAF) がハング** — 進捗画面を確実に描画させてから重い処理に入る狙いで `requestAnimationFrame` 二重待ちを入れたが、preview のヘッドレス環境 (および背景タブ) では rAF が発火せず Promise が永久に解決しない → importFiles 全体が固まる。対処: `setTimeout(finish, 100)` の保険を併設して rAF が来なくても進む。
- **背景処理中に `revokeAllThumbUrls()` してはいけない** — 表示中の random サムネ URL を壊して ?化する (v5 と同じ罠)。背景取り込みは新規 record を `allPhotos.push` で in-memory 追加するだけにし、URL は表示時 lazy 生成。フェーズA の遷移時 (まだ何も表示してない) だけ revoke。

**結果 / 観察**
- preview の synthetic test (importOne を stub 化) で確認: 10枚投入 → fast 6 で即 random、背景 4 枚 (重複1スキップ) を消化して最終 9 枚、`🌀` インジケータが出て完了で消える、count も追従。
- 連打ガード: preparing 中に `openPicker` を連打しても `$picker.click()` は 1 回のみ。キャンセルで元画面に復帰。
- 実機での体感 (待ち時間が本当に減ったか/詰まらなくなったか) は次回確認。

**教訓**
- preview のヘッドレス環境では **`requestAnimationFrame` が発火しない**。rAF に依存した待ちは必ずタイムアウト保険を付ける (テスト可能性 + 背景タブ堅牢性の両取り)。
- 同環境では **`setTimeout` が ~1000ms にスロットル** される (非表示タブ)。背景ループの所要時間テストはこれを織り込む (120ms 設計でも実測 ~1s/枚に見える)。
- iOS picker の「準備ギャップ」は `change` 前なので JS から潰せない。**picker を開く側で先に画面を変える**のが唯一効く手。

**残課題 / 次の方向**
- 実機で数十〜百枚投入し、fast-track 枚数 (6) と背景遅延 (120ms) を体感チューニング。
- Phase 2 (CLIP) はこの背景パイプラインに embedding 抽出を相乗りさせる前提が整った。

---

## v6 — Phase 1 残課題クローズ: 長押しでフル画像表示 (2026-05-29)

**背景**
- Phase 1 で唯一未着手だった「拡大表示 (フル画像)」を回収。TODO L70-73、「サムネをロングタップまたは別ジェスチャでフル画像表示」。
- v5 までは表示が常にサムネ (300px)。元画は IndexedDB の `blob` に入っているが、見る手段が無かった。
- 軽量で完結する範囲なので、Phase 1.8 / 2 に進む前に拾った。

**設計判断**
- **ジェスチャは長押し (500ms)**。「ボタン追加」案より `ui-minimalism-works` 尊重 — UI は増やさず、必要な時だけ広がる形に倒す。iOS Photos の peek と同じ手触り。
- **対象は 3 種類すべて** — ランダム3枚カード / explore 中心カード / explore グリッドサムネ。中心カードだけ通常タップ動作なし (長押し専用)。
- **共通の `attachPhotoGestures(el, photo, onTap)` に集約**。click 抑止フラグ (`didLongPress`) を持って、長押し発火後の click は onTap を呼ばない。これで navigation との競合を回避。
- **オーバレイは fixed inset:0 / object-fit:contain**。pinch zoom はスコープ外、画面に収める。タップで閉じる + ESC でも閉じる (desktop)。
- **長押し離した直後の click でオーバレイが即閉じる問題** → オーバレイの click は 200ms グレースを置く。
- **iOS の画像長押しメニュー (画像をコピー/保存) はジェスチャを奪う** → CSS `-webkit-touch-callout: none` + `contextmenu` の preventDefault。
- **10px 超の pointermove で長押しキャンセル** → スクロールを邪魔しない。

**ハマったところ** (未然回避)
- iOS の `-webkit-touch-callout` を忘れると、500ms 経過時に OS のメニューが出て自前タイマが意味を失う。CSS の rule set に最初から入れた。
- 原画 blob URL は `createObjectURL` の都度生成 + closeFullImage 時に必ず revoke。サムネ URL のように長期キャッシュしない。

**結果 / 観察**
- preview の synthetic event テストで 5 経路 (長押し開く / ESC 閉じる / 長押し後 tap 抑止 / 短タップは navigate / move でキャンセル) 全て通過。
- 実機での「色味の確認」は次回触り込み時に確認。

**教訓**
- click 抑止の仕組みを capture phase + stopImmediatePropagation でやるより、**フラグ + 通常 listener で順序依存を消す**ほうが堅い。makeCard 側の onTap 配線と attachPhotoGestures 内の click 配線が同一クロージャでフラグを共有する形に。

**残課題 / 次の方向**
- Phase 1 は完全クローズ。次は Phase 1.8 (Progressive Indexing) か Phase 2 (CLIP) か、あるいは触り込み観察。

---

## v5 — Phase 1.5 クローズ: 色彩トーン展開 (純粋色モードが刺さった) (2026-05-28)

**背景**
- v4 で reminiscence 体験が「めっちゃいい」と感じた手応えを受けて、ユーザーがじっくり考えた追加仕様 (Phase 1.5 色 / 1.8 progressive / 2 CLIP) のうち、最軽量の Phase 1.5 から着手。
- AI なしで「夕焼け→別の夕焼け」「緑→別の緑」のような色のワープが効くか? の検証。

**設計判断**
- **色彩特徴量: 4x4 グリッドの平均 RGB (48次元 Float32Array)**。dominant color や k-means より軽量で、トーン感を捕捉するには十分という仮説。
- **抽出は createThumbnail 内で同じ canvas を共有** (二度描画しない)。
- **既存写真は起動時に非同期バックフィル** (1枚 50ms 遅延、メイン操作を止めない)。
- **explore に「⏳ 時空 / 🎨 カラー」トグル**。デフォルト体験 (時空) は変えない (`ui-minimalism-works` 尊重)。
- **カラーモードを「純粋色 (時空無視)」に振った** — TODO 案では時空近傍30→色順 (AND) だったが、触ってみたら色ワープ感が弱かったため、全件 (color 持ち) から色距離順に変更。これが情緒的に強く刺さった (`color-jump-works` memory に記録)。

**ハマったところ**
- **画像が ? マークに化ける** — phase1.9 投入後、バックフィル中に画像が broken-image に。原因: 135枚 × 300×300 canvas での getImageData が 1200万ピクセルのバッファ → iOS Safari が Blob ストアを部分解放 → 表示中の thumb URL が無効化。
  - 対処: バックフィルは 16×16 縮小描画してから色抽出 (256 px/枚で 99% 軽量化)、tiny canvas を使い回し、完了時に thumbUrls キャッシュを revoke→再描画 (URL 復活)。

**結果 / 観察**
- バックフィル後、純粋色モードに切り替えると **「夜祭」と「夜景」のセット**のように、文脈の違う写真が色で結びつくジャンプが起きる。
- ユーザーの言葉「色似たやつもいいよ / 夜祭と夜景の風景のセットになったり / これで確定」。
- 4x4=48次元の粗い特徴量でも色ワープは成立 → AI (CLIP 512次元) が必須とは限らない signal。

**教訓**
- TODO の案 (時空 AND 色) を素直に実装したが、触ってみたら別の解 (純粋色) のほうが良かった。**設計案は紙の上では決まらない、触って初めて決まる** — spike の流儀通り。
- iOS Safari は canvas/Blob 系のメモリ管理が緩い。バックフィル系の処理は **最初から縮小描画でメモリ最小化**するのが安全。
- バックフィル中も古い URL が解放されうるので、**完了時に URL キャッシュ全 revoke + 再描画** をパターン化。

**残課題 / 次の方向**
- Phase 1.8 (Progressive Indexing) → Phase 2 (CLIP) は別セッション推奨。
- フル画像表示 (TODO 既存項目) は未着手のまま。
- 触り込んで TODO の検証問い「やめにくいか」「次どこ行く？のヒントになるか」を深堀りする時間も価値あり。

---

## v4 — Phase 1 MVP 完成 + 条件付ランダム (2026-05-28)

**背景**
- Phase 0 で前提検証 (日時/GPS/HEIC 読める) が済んだので Phase 1 本体に着手。
- TODO のゴール定義「開く→3枚→1枚タップ→時空近傍→さらに連想ウォーク→閉じても保持」を達成して、reminiscence の手触りが本当に気持ちいいかを確かめるのが目的。
- 触ってる中で「条件付ランダムが欲しい」(6月ごろ / 10年前 / 遠く / 近く) が自然発生したので同セッションで拡張。

**設計判断**
- **取り込み**: exifr full ビルドで EXIF (日時/GPS/Orientation) を読む。HEIC は heic2any で JPEG 変換 (EXIF 抽出は exifr が直接読めるので変換は描画のため)。サムネは 300px、orientation は EXIF 値を canvas transform で補正。
- **サムネ生成は `createImageBitmap` を使わない** — iOS Safari の特定バージョンで TypeError を投げる。古典的な `<img>` + `<canvas>` + `URL.createObjectURL` 方式に。
- **IndexedDB スキーマ**: `{id, name, datetime, lat, lng, blob (フル JPEG), thumb (300px JPEG), dedup, importedAt}`。重複検出は `name|size|datetime` の dedup インデックス。
- **近傍ロジック**: GPS 5km以内 → 同日 → ±3日 → ±7日 の優先順位、各レイヤー内は時間差順。上位 6 枚で打ち切り。
- **連想ウォークの戻る**: 履歴スタックは持たず「ランダム3枚に戻る」のみ (シンプル化)。
- **エクスポート**: JSON + base64 inline。TODO の「最初から入れる」指定通り。圧縮しない単純構造。
- **キャッシュ問題対策**:
  - HTML に no-cache メタタグ (iOS Safari の HTML キャッシュ強烈問題)
  - ヘッダに **BUILD バージョン文字列** を表示 (`phase1.X · YYYY-MM-DD`)。画面を見て新旧が一目で分かる。commit のたびに上げる運用。
- **条件付ランダム (フィルタチップ)**: デフォルト経験 (完全ランダム) を壊さないため、ランダム3枚画面の上にチップ1行を追加。
  - 時間軸: 全期間 / この時季 / 1年前 / 10年前 / 久しぶり / たくさん撮った日
  - 場所軸: 近場📍 / 遠出📍 (写真の GPS 重心からの距離 30km 閾値、Geolocation 不要)
  - チップ下に母数脚注を表示 (`12 枚から`、`48 枚から (GPS付き 48/135 枚)`)
  - 「久しぶり」=撮影日が古い 20% プール、「たくさん撮った日」=同日5枚以上の日プール (チューニング値は鉛筆書きで定数化)

**結果**
- 取り込み・連想ウォーク・永続化すべて動作。**135 枚で動作確認 OK**。
- 「情報量が少なくて写真に意識が向く」「めっちゃいい」のフィードバック (UI minimalism が刺さった、memory に保存)。
- 触ってる中で「6月ごろ / 10年前 / 遠く / 近く」が自然発生 → 同セッションで実装、「久しぶり」「たくさん撮った日」も追加。

**ハマったところ (実機検証で潰した順)**
1. **関数名衝突**: 画像処理用 `makeThumb` と DOM 生成用 `makeThumb` が両方定義されていた。JS は後ろ勝ちなので importOne が呼ぶ `makeThumb(blob, 300)` は DOM 版に解決され、内部の `URL.createObjectURL(blob.thumb=undefined)` で TypeError @ createObjectURL@[native code]。画像処理用を `createThumbnail` に改名して解決。**phase1.0 〜 phase1.5 で原因を追い続けた最大の犯人**。
2. **createImageBitmap が iOS Safari で TypeError** — `imageOrientation: 'from-image'` オプションが原因の説。`<img>` + canvas 古典方式に書き換え、orientation は exifr で読んで canvas transform で適用。
3. **iOS Safari の HTML キャッシュ強烈** — push しても古い HTML が返り続けた。no-cache メタタグ + URL バージョンクエリ + 画面表示 BUILD で運用。
4. **iOS のフォトピッカーは HEIC を JPEG 変換して渡す** — HEIC のまま検証するには「ファイル」アプリ経由が必要 (Phase 0 で記録済み)。

**教訓**
- spike でも一画面に 2 つの関数を同名で書くのは事故。検索しても気付きにくい (両方ヒットする)。短くても**異なる責務には異なる名前**。
- iOS Safari の `createImageBitmap` は信用しない。古典 `<img>+canvas` の方が堅い。
- 画面上の BUILD バージョン表示は iOS Safari キャッシュ問題のデバッグに必須。spike 初期から入れるべきだった。
- 「ユーザーが触ってる中で出てきた要望」は spike の最強 signal。デフォルト経験を壊さない形なら同セッションで応える価値あり。

**残課題 / 次の方向**
- Phase 1.5 (色彩トーン展開) — ユーザーがじっくり考えた追加仕様の最初。AI なしで「夕焼け→夕焼け」「緑→緑」のワープを試す。
- Phase 1.8 (Progressive Indexing) → Phase 2 (CLIP) は Phase 1.5 のあとに別セッションで。
- 動作未確認: フル画像表示 (拡大、ロングタップ) — ゴール定義外なので後回し継続。

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
