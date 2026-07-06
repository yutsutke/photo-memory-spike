# 写真思い出スパイク — TODO

> **これは何か**: 自分の写真を読み込み、ランダムに3枚カードを引いて、タップで時空の近傍に展開する、ローカル完結の reminiscence 試作。
> 「思い出すのは気持ちいいか」「次の旅のヒントになるか」を *自分の手で確かめる* spike。
> 捨ててよい spike。きれいさより「あの感じ」に最速で触ることを優先。

---

## 🔒 スコープの境界（最初に必ず読む）
- **作るのは「ランダム引き＋連想展開」。プラットフォームの器は作らない。**（地図ビューは v49 で un-park = 検証が当たった次周回として追加。核＝連想ウォークへ合流させる形で実装）
- 写真は端末から出さない。バックエンド／認証なし。
- 迷ったら「これは A（今すぐ）か？」で判定。A以外は末尾「今は作らない」へ。

---

## 現在地 — BUILD: phase3.105（🐛**v152：英語UIの間隔ラベル i18n**（"2.0年 apart"→"2.0 years apart"・`intervalLabel` を tr() 化・複数形・唯一の呼び出し=思い出ログのギャップ・preview en/ja 確認済）＝**1.3(34) には未反映＝次ビルドに乗る**。｜🎉**1.3(34) リリース済み（2026-07-07・ex-EU 148・café ヒーロー版）**＝7/6 審査提出→承認→手動リリース。店頭コピーを「囲む→時系列（久しぶりがよみがえる）」前面に刷新（プロモ用テキスト/説明文=先頭にヒーロー/リリースノート・日英）＋café「囲んだ場所/Encircled area」を日英とも**1枚目(ヒーロー)**に差し替え（1284×2778）＋build34。｜🗺️**v151：地図に「📍現在地」ボタン ＋ 🫧囲む→ログ(タイムライン)で開く**＝①現在地＝前面 `navigator.geolocation` で地図を寄せて青ドット（`.here-marker`・パルス付き）。コントロールバー行2（🎲の隣・「📍現在地」）に配置し **`LOCATION_AVAILABLE` 時のみ**（位置オフ/`?shot` では出さない＝許可を求めない）。②「囲んで思い出を見る」を deck(1枚) ではなく**思い出の場所ログと同じタイムライン**で開く＝一時オブジェクト（`_transient`・`visitsFromPhotos`）＋`openAnnivTimeline` 再利用・未登録は編集系（メモ/基準日/代表写真/＋続き）を隠し「📍思い出の場所に登録」で `registerAnniversary`→本物ログ（編集可）に昇格・deck 動線 `openAnnivDeck` は温存（未使用化）。**web のみ＝native 契約不変**・preview 実コードパス検証 green（現在地スタブ移動＋青ドット描画／囲む→🫧囲んだ場所5枚→写真グリッド→登録→📖本物ログ昇格）・console 0・スクショ確認・push 済・**実機の手触り残**。｜🛰️**v150：Android にも位置ロガー（B＝背景・iOS 同等）**＝`background-location` に Android(Kotlin) 実装を新規追加＝**通知つき前面サービス (FGS type=location)＋fused provider・`ACCESS_BACKGROUND_LOCATION` なし**（Play 最厳格審査ゲートを構造的に回避しつつ「アプリを閉じても記録」成立）。iOS と同契約5メソッド＝JS 無改修（`NATIVE_LOCATION` を ios||android に＋文言2箇所のみ・web/iOS 不変）。**🎉 S21 実機 E2E 全クリア**＝権限2段（位置→通知）→「今日2点を記録」（service→バッファ→drain→track→UI）→ dumpsys `isForeground=true`/`allowWhileInUsePermissionInFgs=true` → **recents スワイプ kill 後もサービス生存（One UI の swipe-away を生き延びた＝最大の未知数クリア）** → 再起動でモード復元・点数維持。テスト後は借り物端末を清掃（オフ・記録0・📍消灯）。残＝実移動の軌跡/電池（実使用待ち）。｜✅**v149：地図タイムラインの絞り込みバグ修正済み**＝🖼写真タブ/⇆比べるが `timelineFilteredPhotos(all)` で 📋一覧と同じ範囲（`mapState.pickDays`/`range`）に揃う。preview で述語5ケース pass・一覧 `updateTimelineUI` は無変更＝回帰なし・inline 0エラー・push 済。**🎉実機 YES**（ユーザー・TestFlight **1.3 build 33**＝「修正されてました」＝絞り込んだ範囲だけになる）。📱**iOS=1.2 公開済み**（位置ロガー版・ロードマップ②）／**1.3(33)** に v149＋v148 の思い出の場所改修が乗る＝**1.3 の審査提出が次**。｜**v148：タイムラインの見せ方 統一**＝①思い出ログの「⇆比べる」を写真タブのバー(大きく/小さくの隣)へ移動(openAnnivCompare 共通化)②地図タイムラインに **📋一覧/🖼写真 切替＋⇆比べる** を追加＝一覧(位置を直す/🗑/フィルタ)は据え置き・写真は全可視写真の時系列グリッド(月見出し・タップで全画面)・写真モードは updateTimelineUI を no-op で一覧機構と非干渉＝**現状を壊さず付け足す**。preview で 思い出ログ(top に compare 無し/写真バーに比べる)・地図(切替/グリッド/一覧戻すと位置直す/🗑 健在) 検証green・inline 0エラー。実機の手触り残。｜**v147：思い出ログに表示モード切替「📖ログ / 🖼写真」**＝写真モードは全写真を新しい→古いの時系列グリッドでいっぱい(月見出し・小4列/大2列トグル・タップで全画面スワイプ・戻ってもモード維持)。ログ(日ごと+メモ)据え置き。既存 photosInAnniv/thumbUrl/showFullImage 再利用・web完結。preview で切替/グリッド/月見出し/枚数/小大/往復 検証green・inline 0エラー。実機の手触り残。｜**v146：地図⋯「囲んで比べる」→「囲んで思い出を見る」に表現変更**（英語 Encircle to see memories・ラベル1箇所 mmCompare のみ・機能不変）。｜**v145：機能名を「記念日」→「思い出の場所」に改名**＝UI に見える文言を日英とも全置換（日本語は一括／英語=Memory place）。**DBストア名 `'anniversaries'`・コード識別子（`registerAnniversary`/`.anniv-*`）・一部コメントは不変＝既存データ/コード互換**。ヘッダ(⚙内)・一覧・空状態・登録/移転モーダル・トースト・確認・静的i18n を網羅。grep で `記念日` 0件・preview で表示確認・inline 0エラー。｜**v144：思い出の場所マルチ地点の同日バグ修正**＝別の場所を足した写真が「同じ日」だとタイムラインに載らなかった（`mergeVisits` が既存 dayKey を丸ごとスキップ＝比べる[全円ライブ]には出るのに visits スナップに入らない）→**同日は既存訪問に写真を統合**（重複除去・上限8）／別日は新規訪問。preview で検証（p1,p2+p3,p2→p1,p2,p3・memo保持・上限8）。ユーザー実機報告の修正。｜**v143：記念日「この場所の記憶」をマルチ地点対応**＝ユーザー機能質問（①エリアを先に登録すると中の店を別記念日にできない＝重なると自動吸収②お店が移転したら前後両方で思い出を積みたい）に対応。ユーザー選択A＝**重なったら自動吸収せず必ず選ばせる**（`promptAnnivRegister`＝「◯◯に足す／新規／移転で既存に足す」）。記念日を **`center/radius`単一→`circles[]`複数円の器**に拡張＝写真は全円の和集合（`photosInAnniv`）＝移転しても場所の同一性が1つの記念日で続く。移転の入口2つ（登録モーダルの select／詳細の「📍別の場所」→エンサークル直接attach）＋詳細に「📍N か所」表示。旧単一円は `circlesOf` で後方互換（IndexedDB スキーマ変更なし）・読み手5箇所を統一。preview page context で純ロジック＋モーダル検証 green（circlesOf/annivContains/photosInAnniv/mergeVisits・初回は名前入力だけ）・inline 0エラー。**実機でエンサークル→attach の手触り確認が残**。web 完結＝native 無改修・審査中1.2 無関係。｜**v142：本丸＝写真全件アクセスの Android 実装（photo-library に MediaStore/Kotlin）＝エミュE2E YES**＝iOS の自前 Swift plugin と**同契約**で Android を Kotlin/MediaStore 新規実装（requestAccess=13+/14部分許可=limited・enumerate=全件即時列挙で DATE_TAKEN→保存日フォールバック dateSource・thumbnail=loadThumbnail＋EXIF の GPS/撮影日時を同乗）。web 層は GPS/日時マージ3行＋権限文言分岐のみで無改修。権限はプラグイン側 Manifest に隔離＝**app 本体はクリーンのまま**フル版を両立。`cap sync android` で自動配線（settings.gradle include・plugins.json classpath）。**エミュE2E（adb 自動化）＝テストJPEG7枚→Android14 実権限ダイアログ→📚全体取り込み7枚成功＝カード「2023/07/03 Captured」で保存日→EXIF撮影日の格上げを実動確認**。GPS は emulator が redact（コードは正・requireOriginal付与/ACCESS_MEDIA_LOCATION許可済＝実機で要確認）。ローカルビルド確立（Android Studio JBR21 で gradlew assembleDebug 通過）。**実機で GPS/大量スループット確認が次**。｜**v141：Android 着手＝クリーン版(位置なし/広告なし)を scaffold**＝`npx cap add android` で android/ 生成（applicationId=iOS と同じ `io.github.yutsutke.madeleine`・minSdk24/compile・target36・app名 en 既定"A Past Day"＋`values-ja`「あの日 — 写真と足跡」）。`NATIVE_LOCATION` を固定 true→`getPlatform()==='ios'`（iOS のみ位置あり＝1.2 不変／Android・web は false）。background-location/photo-library は `capacitor.ios` のみ宣言＝Android 非登録で Gradle も権限も汚さない→AndroidManifest の権限は **INTERNET のみ**＝完全クリーンが scaffold 直後に成立。Codemagic に **android-debug workflow**（mac_mini_m2・java21・sync:web→cap sync android→gradlew assembleDebug→APK artifact・デバッグ署名でサイドロード可）追加。native 本体はコミット・生成物のみ ignore（dry-run 54ファイル・危険パターン0）・codemagic.yaml parser green。**🎉 Codemagic build #3 green（app-debug.apk 4.58MB）→ Android Studio エミュレータ（Pixel7/API36.1）で起動 YES＝本スパイク達成**（空状態UI完全描画・クラッシュなし・🛰️位置なし確認・英語ロケールで i18n も乗る・CDN/vendor OK）。ビルドの壁3つ＝instance(linux→mac)/SDK36自動DL/JDK17→21(capacitor-android は Java21ソース)。写真の Android 実装＝MediaStore(Kotlin) は本丸として次。web 側は NATIVE_LOCATION 分岐のみ＝挙動不変。｜**v140：足跡から写真の位置を自動確定＋位置の由来バッジ＋位置ロガーの日別/全削除**＝①GPSなし写真を地図の「表示中の窓」で足跡から自動確定（既定ON・`autoConfirmTrackPass`／geoFromTrack+geoAuto で永続・`_autoTrackTried` で1回だけ試行・新軌跡が来たら再試行可・🛰️パネルで OFF にすると従来の手動確定に戻る）②位置の由来を1本化（`locSource`＝none/exif/track/edited/manual）＝バッジ「足跡から/位置を修正/手動」＋マーカー隅ドット（teal足跡/amber修正）・exif/none は非表示（clutter回避）③`saveLocationEdit` で元EXIF GPSを動かしたら geoEdited＝「位置を修正」表示④🛰️ロガーパネルに「🗑すべての記録を消す」(confirm必須)＋「📅日ごとに消す」(日別集計リスト・各行 `trackClearRange`)。自動でも由来バッジ＋「📍位置を直す」で修正可＝[[editing-triggers-reminiscence]] を殺さない。preview green（12:30写真→軌跡中点に補間・由来分岐・日別リスト・console 0）・**実機確認待ち**（native 1.2 に載る）。｜**v139：地図の線を「写真＋ロガーを時刻順に1本」へ**＝ロガー点を擬似点{lat,lng,datetime}にして写真の点列とマージ→時刻順1本（飛び飛びの区間を軌跡が埋める・ユーザー要望）。青緑の点線は廃止・記録点ドットは残す・軌跡トグルOFF=写真のみ。2段描画（写真のみ即→ロガー点が積めたら合流して引き直し）・窓替わりで trackMerge=null（race防止）。preview green（casing頂点5/セグメント4/ドット3/点線0・OFFで写真のみ・console 0）・**実機確認待ち**。｜**v138：位置ロガー復活（native 1.2）**＝NATIVE_LOCATION=true・background-location を package.json に復活(npm install済)・Info.plist に NSLocation×2＋UIBackgroundModes=location・**用途文言を InfoPlist.strings ja/en にロケール別実装（写真2キーの英訳も追加＝1.1の穴）**・MARKETING_VERSION 1.1→1.2（審査中の1.1と分離）。web 無影響確認済。**push後の Codemagic 1.2 ビルドを TestFlight で実機確認**（位置ダイアログ/🛰️3モード/背景記録/英語文言）→ 1.1公開後に 1.2 審査提出。｜**v137：トップの並び替え**＝写真をヘッダ(過去の今日/明日)直下に・フィルタチップ(全期間等)と枚数は最下部へ(ユーザー実機FB・append順の変更のみ・空状態でもチップは残す)。preview green(DOM順＋mobile実座標・console 0)・実機確認待ち。｜**v136：拡大表示にピンチズーム**＝2本指で拡大縮小(1〜5倍・つまんだ点を画面上に保持)・拡大中は1本指パン＋横スワイプ(snap)停止(等倍復帰で自動再開)・拡大中のタップは等倍へ戻す(もう一度で閉じる)。**🎉実機 YES「いいですね。問題なさそう」**。｜**v134：初期画面（ネイティブ空状態）の主導線ボタン「写真ライブラリ全体から始める」に（おすすめ）/EN (recommended) を付与**＝取り込みメニュー(L5197)の「ライブラリ全体（おすすめ）」と用語統一・実機FB・JP/EN 両方・preview green（起動ログ BUILD=3.87・console エラー0）。｜**v133：英語化の総仕上げ（実機で発見の残り＋長文）**＝写真詳細🗺ボタン(重複2箇所)・スワイプヒント・取り込みドロップダウン・詳細設定モーダル・情報モーダル本文6段落・自動取り込みプロンプト・位置ロガーパネル全体・日時ソースタグ(撮影日/保存日/投稿日)を tr() 化＝**dev(🧪#dev 開発者専用)以外ほぼ完全英語化**。教訓＝「地図でこの場所を見る」が3箇所に重複実装→静的網羅1回では拾えず**実機確認が最終網羅**（実機FB 3連続で新未訳を発掘）。preview green。｜**v132：二次画面の英語化＋i18n関数 t()→tr() 改名**＝残りの二次画面を英語化＝記念日(一覧/ログ/基準日/登録/予定/代表写真)・封印・ⓘヘルプ・AIモーダル・削除写真一覧・確認ダイアログ・各トースト・「N枚から」件数。⚠️**i18n関数 t() が既存の t ローカル変数(20箇所)と衝突→ `tr()` に全面改名**（sed で `\bt(`→`tr(` 一括・v130/131 で t() を使った地図/記念日一覧/情報モーダルの潜在バグも同時に解消）。長文の情報モーダル本文/位置ロガー説明/devは次段。preview green（?lang=en 空状態英語・?lang=ja 不変・console 0）。｜**v131：世界版の続き（海外地図フォールバック＋主要導線の残り英語化）**＝①日本語モードの地図に世界の下地(CARTO)を常時敷き、日本人が海外旅行の写真を見ても地理院の外(海外)が真っ白にならずシームレスに（`worldUnderlay`・`LANG==='ja'` 時のみ・切替時も bringToBack で最背面維持）＝「言語で地図を切替」の穴（言語≠見る場所）を塞いだ。②Explore で未訳UI約150箇所を網羅→**触る主要画面**を t() 化＝フィルタチップ/連想ウォーク(Wander)/写真詳細/地図(ツールバー/popup/フィルタ/⋯/タイムライン/囲む比較)/ヘッダtitle（機能名統一 Wander/Footprints/Anniversary/Seal）。二次画面(記念日詳細/封印/情報モーダル本文/確認/dev)は次段。preview green（?lang=en 全面英語・?lang=ja 不変・console 0）。｜**v130：英語 i18n の下ごしらえ＋世界地図**＝公開後ロードマップ①（世界版）の基盤を web に先行投入（native 1.0(21) は審査待ちのまま・非接触）。端末言語(navigator.language)で日英出し分け＝`t(ja,en)`（ja は第1引数そのまま＝**日本語は1px も不変**・非日本語は第2引数・未指定は ja フォールバック）・言語自動判定・主要導線（ヘッダ/空状態/取り込み進捗/モーダル見出し/枚数）を英語化。地図は非日本語ロケールで地理院→CARTO Voyager/Positron に差し替え（`BASE_LAYERS` を LANG 出し分け・key 共通で localStorage 互換・描画/保存ロジック無改修）。preview green（?lang=ja 回帰なし・?lang=en 全面英語＋タブ名・console エラー0）。`?lang=en` で検証可。｜**v129：記念日の表現＋基準日**＝①「＋未来の予定」→「＋あの日の続き」（作業感を外し記憶の続きへ・未来バッジ 予定→これから・データ不変）②記念日に任意の基準日を設定でき、ログの各日の横に「N日前／N日後／基準日」を表示＝あの日からの隔たりを日数で体感（`a.baseDate`・IndexedDB スキーマ変更なし・既定候補=最古訪問日）。preview green（862日後 検算一致・前後両方向・teal）。**🔀方針＝承認済み 1.0(16) は release せず、UI仕上げ版を再提出しデビュー版にする → ✅2026-07-01 実行＝Codemagic ビルド1.0(21)（v111-129 UI入り）を ASC で再提出→「審査待ち」（デビュー審査・2回目・追加質問ゼロ）。承認後 ASC で手動公開＝デビュー**。｜**v128：UI微調整3点**＝①地図「📖記念日のログ」を常時表示化＝記念日由来の地図→そのログ／**通常の地図→記念日一覧**（無反応を解消）②地図の種類チップ ダーク/地理院標準/地理院淡色 を1行に③ヘッダの写真枚数をタイトル行へ移動＝2段に圧縮（⚙が2段目に収まる）。preview green（②③実測・①ロジック）。｜v127：**記念日ログの作り込み**＝①時系列を逆に（現在→過去へ遡る・新しいが上）②訪問の間隔が空くほどログ上の「間」を広げる（log スケール＋「N年のあいだ」＝時間の長さを体で感じる）③一覧タップ→ログへ直行（記念日の家＝物語）④ログの代表写真タップ→拡大＋記念日内を日にち横断スワイプ⑤相互動線＝ログ↔一覧/左右で比べる/足跡の地図（比較・地図からもログへ・`anniv` を compareState/mapState に伝播）。preview green（全方向）。｜v126：①地図 popup を暗く（日時が読めるように）②記念日一覧おしゃれ③記念日「📖ログ」＝日別タイムライン(写真/コメント/未来の予定)。｜v125：微調整6点＝①足跡の地図から記念日登録②🎲1日③連想ウォーク統一④位置なし説明⑤位置あり拡大スワイプ⑥今日0年分。｜v124：微調整4点＝①比較スワイプ即応(画像 pointer-events:none)②比較に「🗺足跡の地図」③ヘッダ「🗺過去の明日」(todayOffset:1)④囲む後も記念日と同じ deck 動線に統一(openAnnivDeck・違いは canRegister の登録ボタンだけ)。｜v123：動線タップの実機「無反応」修正＝透明な太い当たり判定線(weight24/opacity0)・見た目線 interactive:false。教訓＝preview の合成クリックは実機タップ命中を保証しない。｜v122：**記念日の詳細を作り替え**＝記念日タップ→写真1枚大表示＋スワイプ(showFullImage deck)→「⇆左右で比べる」／「🗺足跡の地図」(全訪問日を動線表示・1本タップでその日だけ明るく＋タイムライン連動)。一覧に🗑。｜v121：記念日(過去半分)＝円を名前付き保存し訪問が積もる器・IndexedDB v3。｜v120：地図「囲んで比べる」(実機「いいかんじ」YES)。｜v119：起動時の自動差分取り込み(native)。v111-118 UI改善✅。｜🎉🎉**App Store 承認（2026-07-01）！ビルド1.0(16)が審査通過**＝「Welcome to the App Store / eligible for distribution」。**ただし 16 は公開せず**（旧UI）＝手動リリース設定のまま「リリースをキャンセル」→ **v111-129 UI入りの ビルド1.0(21) を再提出＝「審査待ち」（2回目）**→ **次の一手＝審査結果待ち→承認後 ASC で手動公開＝デビュー**。英語名 A Past Day／未来=カレンダー書き出し(EventKit)／位置ロガー復活／iPad／広告 は native 次版。）

> ### 📍 次セッションの再開ポイント（2026-07-07 セッション24 更新・まずここを読む）
>
> ## 🎉 セッション23-24（2026-07-06→07）＝iOS 1.3(34) 審査提出→**リリース済み**（店頭コピー刷新・café ヒーロー）＋ v152 英語ラベル修正
> - **🎉🎉 1.3(34)＝審査提出(7/6)→承認→手動リリース済み(7/7)＝ex-EU 148 地域に配信開始**（App Store 反映〜24h・café ヒーロー版）。中身＝v148/v149/v151（囲む→ログ・📍現在地ボタン・思い出の場所改修・タイムライン修正）。実機で v151（囲む→時系列）が YES、ユーザー「自分で使って刺さる」。
> - **🇪🇺 リリース前に配信地域を確認（7/7）＝「配信可能148個/販売不可27個」で正しい（175ではない・保存ボタン=グレー=変更なし）**。除外27=EU27（アイルランド/イタリア/エストニア…）は状況「**トレーダーステータスが提供されていません**」＝**trader 未申告なので Apple が EU を自動除外**＝住所非公開の安全状態。EU を足すには trader 申告→**住所公開**が必要（個人の自宅住所＝安全面も）。無料でも non-trader で EU 配信＋住所非公開が今のルールで通るかは要・Apple 最新確認。位置ロガーは端末内完結で EU 判定に無関係。
> - **店頭コピーを刷新（ASC＝Claude がブラウザ操作／最終「審査へ提出」はユーザー）**: プロモ用テキスト＝ヒーロー／説明文＝**先頭にヒーロー＋既存本文**（全書き換えせず）／リリースノート＝ヒーロー＋改善2点。**日英とも**。ヒーロー（日）=「このカフェ、2年ぶりだったんだ」／地図で行った場所を〇で囲むと写真が年をまたいで時系列に並ぶ／忘れていた時間がそっとよみがえる／ほかは触って見つかる。英=「This café — it had been two years.」…。**方針＝操作(囲む)でなく payoff(久しぶりがよみがえる)を主役に**＝汎用地図アプリ化を避けコアと地続き。
> - **スクショ差し替え**: café「囲んだ場所/Encircled area」を**日英とも1枚目(ヒーロー)**に（既存4枚は後ろ＝各5枚）。元 960×2079→**1284×2778 にリサイズ**（6.5"スロット）。⚠️**ASC の file_upload はセッション外パス不可**（scratchpad も request_directory 済み Desktop も弾かれる）→ **ユーザーがドラッグ&ドロップ**、Claude は寸法調整＋配置/保存確認。次回も同じ段取り。
> - **web(v152・push 済・BUILD `phase3.105`)**: `intervalLabel` を tr() 化＝英語UIの「2.0年 apart」→「2.0 years apart」（複数形・思い出ログのギャップが唯一の呼び出し）。preview で en/ja 両方確認・console 0。**1.3(34) には未反映＝次の Codemagic ビルドで乗る**。
> - **▶ 次の一手**: ①**App Store への公開反映を確認**（〜24h・café ヒーロー版がストアに出るか・"is now available" メール／apps.apple.com/jp/app/id6784557053）。②**Android を Google Play へ**（本命・下のチェックリスト）。③v152 ほか web 改善は次の Codemagic ビルドで iOS/Android に乗る（1.3(34) には未反映）。
> - **📋 Android → Google Play 着手チェックリスト**（済＝v150 背景位置ロガー実機E2E・v141/142 クリーン起動＋写真全件 MediaStore エミュE2E・[[post-launch-roadmap]]／[[madeleine-product-repo]]。**方針＝初回から位置入り(v150)で出す＝クリーン版に戻さない**）:
>   - (a) **Google Play Console 登録**（$25・一度きり）／(b) アプリ作成（パッケージ名 `io.github.yutsutke.madeleine`＝iOS と同じ・要現状確認）
>   - (c) **署名＝Play App Signing**（アップロード鍵で署名→Play が配布鍵管理。Codemagic の署名情報を Android 用に）／(d) **AAB を release ビルド**（現状の Codemagic `android-debug` は debug APK＝**release/AAB workflow が要る**・要現状確認）
>   - (e) **掲載素材**＝アイコン／スクショ（café ヒーロー流用可・Android サイズ）／掲載文（日英・iOS の店頭コピー流用＝「囲む→時系列」ヒーロー）
>   - (f) **データセーフティ＝「収集なし・端末内のみ」**＋**FGS location の宣言フォーム**（背景位置"権限"は不使用＝`ACCESS_BACKGROUND_LOCATION` なし＝重審査/デモ動画は回避見込み）
>   - (g) **実機で GPS/大量スループット確認**（エミュは位置を redact＝中古 Android 検討中／知人の写真17,097枚は取り込まない判断）。地図(Leaflet)・体験ロジック・i18n は web 共通で乗る。
>
> ## 🗺 セッション22（2026-07-05・web）＝地図に「📍現在地」ボタン ＋ 🫧囲む→ログ(タイムライン)で開く（v151）
> - **ユーザー要望2点（実機＝思い出の場所ログ画面を触って）**: ①地図に現在地へ飛ぶボタンを増やしたい ②「囲んで思い出を見る」を、その段階で**思い出の場所ログのような画面**に遷移できないか（＋「挙動が重くなる？」）。→ 重くならない旨を回答（既定📖ログは1日数枚のサムネ・🖼写真グリッドも既存一覧と同じ `thumbUrl`＋`loading=lazy`＝全画像を1枚ずつ出す deck より軽い）。
> - **やったこと（web のみ・native 契約不変・push 済・BUILD `phase3.104`）**: ①`#mcLocate`（コントロールバー行2・`LOCATION_AVAILABLE` 時のみ＝位置オフ/`?shot` では非表示）＋`flyToCurrentLocation`＝前面 `navigator.geolocation.getCurrentPosition` で `map.setView(zoom≥15)`＋青ドット `.here-marker`（パルス）。②囲む完了（onUp）を `openAnnivDeck`→`openEncircleTimeline`＝一時的な思い出の場所（`_transient`・`visitsFromPhotos`）を作り `openAnnivTimeline` を再利用。未登録は編集系（メモ/基準日/代表写真/＋続き）を隠し「📍思い出の場所に登録」で `registerAnniversary`→本物ログ（編集可）に昇格。詳細 CHANGELOG v151。
> - **検証（preview・実コードパス／合成データ）**: 現在地スタブ→地図移動（36.2,138.2→34.7,135.5・zoom15）＋青ドット描画（スクショ確認）／囲む→「🫧囲んだ場所（5枚）」・登録+地図ボタンのみ・基準日/メモ/代表写真/＋続き 無し・📖ログ2日+サムネ5・🖼写真グリッド（月見出し1・⇆比べる）／**📍登録→名前入力→📖本物ログ（メモ/基準日/＋続き/一覧/別の場所 復活・`transient=false`）に遷移**＝昇格パス動作（テストレコードは検証後削除）・console エラー0。**実機の手触りは残**（iOS 1.2 公開版には未反映＝次の native ビルドで自動で乗る・挙動は web 分岐のみ）。
> - **▶ 次の一手（native トラックは据え置き）**: 下のセッション21の候補（**iOS 1.3(33) 審査提出** ／ **Android を Google Play へ**）はそのまま有効。web v151 は次の Codemagic ビルドで iOS/Android に自動で乗る。
>
> ## 🎉 セッション21＝借り物 S21 実機デビュー ＋ v150 Android 位置ロガー（背景・iOS 同等）実装＆実機 E2E YES
> - **実機接続**: 知人の Galaxy S21 (SC-51A・Android 13) を USB デバッグで接続（ワイヤレスペアリングは PC 側がゲスト Wi-Fi でサブネット分断→USB に切替。SAMSUNG ドライバは端末側「ファイル転送モード＋USB デバッグ許可」で解決・デバイス RFCN304BBFE）。最新 APK を install→起動 YES。**知人の写真 17,097 枚はプライバシー配慮で取り込まない判断（ユーザー）**＝写真の GPS/大量スループット検証は未実施のまま（自分の端末 or 知人の同意を待つ）。
> - **v150（このセッションの本体・コミット済）**: ユーザー「スマホで位置ロガーがぬけているんだけど、くわえることできる？」→ scope 質問に「**最初から B（背景・iOS 同等）まで**」→ `local-plugins/background-location/android/` を Kotlin で新規実装。**方式＝FGS(type=location)＋fused・ACCESS_BACKGROUND_LOCATION 不使用**（while-in-use だけで「閉じても記録」が成立＝Play の背景位置審査（デモ動画）を構造的に回避）。iOS と同契約＝JS 無改修。**S21 実機 E2E 全クリア**（権限2段→記録2点→drain→swipe-away 生存→再起動復元→清掃）。詳細 CHANGELOG v150。
> - **▶ 次の一手（候補）**:
>   1. **iOS 1.3(33) を審査提出**（v149＋v148 入り・1.3 train 開・最終ボタンはユーザー）＝セッション20からの持ち越し。
>   2. **Android を Google Play へ**＝登録($25)→署名(Play App Signing)→AAB→掲載素材。**✅決定（2026-07-05・ユーザー）＝クリーン版に戻さず「位置入り（v150）」で初回提出する**（旧「Android v1 はクリーン」方針を上書き＝Android も最初から iOS 1.2 と同じ「位置あり・広告なし」でデビュー）。背景位置「権限」は無いので重審査は回避見込みだが、**FGS location の申告フォーム（Play Console）＋データセーフティ「収集なし・端末内のみ」の記載は必要**。
>   3. 実移動での軌跡/電池の確認（ユーザーの実使用 or 散歩テスト）。
> - **借り物 S21 の後始末**: アプリはユーザーがいつでも端末から削除可（長押し→アンインストール、または `adb uninstall io.github.yutsutke.madeleine`）。現在アプリ内はテスト JPEG 4枚（IndexedDB）とロガーオフ・記録0の状態。
>
> ### 📍（参考）セッション20 の再開ポイント（2026-07-04・iOS 1.3 提出は上記に持ち越し）
>
> ## ✅ v149 で修正済み＝地図タイムラインの「🖼写真 / ⇆比べる」が絞り込みを無視して全期間を出すバグ
> - **対処（2026-07-04 セッション20）**: `buildTimeline` 内に小関数 **`timelineFilteredPhotos(list)`**（`updateTimelineUI` と同一の pickStarts/inR 判定）を追加し、写真グリッド（`buildTimelinePhotos(container, timelineFilteredPhotos(all))`）と比べる（`timelineFilteredPhotos(all).slice().reverse()`）に適用。**📋一覧の `updateTimelineUI` は無変更＝回帰なし**（動作している一覧機構に触れない外科的修正）。
> - **検証**: preview の page context で修正した述語と同一ロジックを合成データ5ケースで検証＝**全 pass**（pick1日/pick2日/range単日/range期間/無フィルタ）。特に pickDays の 0-based 月と `dayStartOf` の整合を確認。inline 0エラー・BUILD `phase3.102`・GitHub Pages push 済。詳細 CHANGELOG v149。
> - **✅ 実機確認済み**: ユーザーが **TestFlight 1.3 build 33** で確認＝**「修正されてました」**（絞り込んだ範囲だけになる）。preview の述語検証が実機挙動と一致。
> - **▶ 次の一手**: **1.3(33) を審査提出**（v149 バグ修正＋v148 の思い出の場所改修が乗る・1.3 train は開いている＝`MARKETING_VERSION` 据え置き可）。ASC で 1.3 バージョンページ作成→ビルド33紐付け→What's New→審査提出（最終ボタンはユーザー）。
> - **習慣（この回で実施）**: Gmail 確認（[[session-start-gmail-check]]）＝**1.2 が審査通過（提出ID `e14c7e4c…`・7/4 00:47JST）→ ユーザーが公開済み**（位置ロガー版・ロードマップ②）を把握。TestFlight 1.3(32) も配信済み。
>
> ## 今セッション（19・2026-07-04）でやったこと＝思い出の場所まわり大改修 ＋ iOS 1.3
> - **web（全て GitHub Pages 反映済み・preview 検証 green）**: v143 思い出の場所を**マルチ地点対応**（登録時に重なったら自動吸収せず選ばせる／移転で `circles[]` に別の場所を積む＝場所の同一性が続く／詳細に📍別の場所・場所数）→ v144 同日バグ修正（`mergeVisits` が同じ日を丸ごとスキップ→写真を統合）→ **v145 機能名を「記念日」→「思い出の場所」に改名**（UI 日英全置換＝英語 Memory place・**DBストア名 `'anniversaries'`/コード識別子/`.anniv-*` は不変＝データ互換**）→ v146 地図「囲んで比べる」→**「囲んで思い出を見る」**→ v147 **思い出ログに 📖ログ/🖼写真 切替**（写真＝全写真を時系列グリッド・月見出し・小/大・タップで全画面）→ v148 **タイムライン統一**（思い出ログの⇆比べるを写真タブへ移動／**地図タイムラインにも 📋一覧/🖼写真＋⇆比べる**を追加＝一覧の 位置を直す/🗑/フィルタは据え置き）。← **v148 の地図写真/比べるに上記バグ**。
> - **iOS**: web 改善を載せて Codemagic ビルド→TestFlight。**Publishing が 90186/90062 で失敗（1.2 train 締切）→ `MARKETING_VERSION` を 1.2→1.3 に上げて再ビルド＝アップロード成功**（build 32 相当・1.3）。＝TestFlight 1.3 に今日の web 改善が乗っている。
> - **⚠️ GitHub Pages の deploy が一時的にコケる事象**が数回（`Deployment failed, try again later.`／Actions は緑で deploy 段だけ失敗）。**空コミット push（`git commit --allow-empty` → push）で再トリガーすると通る**。反映確認は `curl …/index.html?cb=RANDOM | grep phaseX.Y`。
>
> ## 状態（2026-07-04 セッション20）
> - **web** = GitHub Pages `phase3.102`（v149 まで・v148 のタイムライン絞り込みバグ修正済み）。
> - **iOS** = **1.2 公開済み**（位置ロガー版・ロードマップ②／配信は 1.1 を継承＝ex-EU 148地域）。**1.3(33) を Codemagic でビルド済み＝実機でタイムライン修正確認 YES**（TestFlight）。＝**次は 1.3(33) を審査提出**（v149＋v148 思い出の場所改修入り・1.3 train は開いている＝MARKETING_VERSION 据え置きで可・最終提出ボタンはユーザー）。
> - **Android** = v142 まで（クリーン起動＋写真全件アクセス MediaStore/Kotlin＝エミュ E2E YES）。**実機で GPS/大量スループット**が残（中古 Android 検討中）。
>
> ### 📍（参考）セッション18 の再開ポイント（2026-07-03・Android。上記に統合済み）
>
> ## 🎉🎉 Android＝クリーン起動（v141）＋本丸・写真全件アクセス（v142）まで完了 → 次は実機で GPS/大量スループット確認
> - **🎉🎉 この回の到達点（2つ）**: ①**クリーン版が Android Studio エミュレータ（Pixel7/API36.1）で起動 YES**（v141）。②**本丸＝写真全件アクセスの Android 実装（photo-library に MediaStore/Kotlin プラグイン）＝エミュ E2E YES**（v142）＝テストJPEG7枚を adb で流し込み→Android14 の実権限ダイアログ→📚全体取り込み7枚成功→**カード「2023/07/03 Captured」で保存日→EXIF撮影日の格上げを実動確認**。UI（deck/スワイプ/Map for this day/On This Day）も機能。**ボトルネック①（久しぶり＝全ライブラリ）の Android 版クローズ**。
> - **⚠️ 実機で確認すべき残り2点**: (a) **GPS/足跡地図** — emulator は EXIF 位置を redact する（コードは正しい＝`setRequireOriginal`＋`ACCESS_MEDIA_LOCATION` 付与済み・同 EXIF から日時は読める・取り込んだ実ファイルに GPS は残存を確認済み）＝**実機で通るはずだが未検証**。(b) **大量ライブラリ（数千枚）の実機スループット/体感**（サムネ512px）。→ 中古 Android（Android14+）入手後の最初の確認項目。
> - **やったこと（v141・コミット/push 済）**: `npx cap add android` で `android/` 生成（native 本体コミット・生成物のみ ignore＝ios 同流儀）。`NATIVE_LOCATION` を固定 true→`getPlatform()==='ios'`（iOS のみ位置あり＝**審査中 1.2 不変**／Android・web は false）。iOS 専用プラグイン2つは `capacitor.ios` のみ宣言＝Android 非登録→**AndroidManifest 権限は INTERNET のみ**（完全クリーン）。applicationId=iOS と同じ・minSdk24/target36・app名 en"A Past Day"＋ja「あの日 — 写真と足跡」。Codemagic に **android-debug workflow**（mac_mini_m2・java21・gradlew assembleDebug→APK artifact）。**ビルドの壁3つ**＝①instance linux_x2→mac_mini_m2（課金プラン外）②SDK Platform36/Build-Tools35 は Mac イメージ自動DL（compileSdk36 OK）③java 17→21（capacitor-android は Java21ソース＝"invalid source release: 21"）。
> - **開発ループが確立**: Windows に **Android Studio＋SDK＋エミュレータ**（Pixel7/API36.1・Google Play x86_64）導入済＝以降は**手元でビルド→エミュ起動**（`android/` を Android Studio で開いて Run、or `npx cap run android`）＝Codemagic 待ちを卒業＝**本丸の Kotlin を高速イテレートできる**。SDK パス `C:\Users\yutsu\AppData\Local\Android\Sdk`。
> - **やったこと（v142・コミット/push 済）**: `local-plugins/photo-library/android/` に Kotlin プラグイン新規＝`PhotoLibraryPlugin.kt`（iOS と同契約 requestAccess/enumerate/thumbnail）＋`build.gradle`（com.android.library・JavaVERSION_21・androidx.exifinterface）＋`AndroidManifest.xml`（写真権限をプラグイン側に隔離＝app 本体クリーン維持）。`package.json` に `capacitor.android`。web glue＝`importOneNative` に GPS/exifDate マージ3行・`dateSource` 継承・権限文言の Android 分岐（`NATIVE_PLATFORM==='android'`）。`cap sync android` で自動配線（settings.gradle include・plugins.json classpath・手動 registerPlugin 不要）。ローカルビルド＝Android Studio JBR21 で `gradlew -p android assembleDebug` 通過（`JAVA_HOME`＋`android/local.properties` の sdk.dir）。
> - **▶ 次セッションの最初の一手**: ①（習慣）Gmail で **Apple 1.2 の審査結果**を確認（[[session-start-gmail-check]]）。②**実機（中古 Android 入手後）で GPS/足跡地図と大量スループットを確認**（上記の残り2点）。③その後＝Google Play 登録($25)・署名(Play App Signing)・AAB・ストア掲載（アイコン/スクショ/掲載文＝日本のクリーン版デビュー）。地図(Leaflet)・体験ロジックは web 共通で乗る。
> - **エミュ再現手順（次セッションで役立つ）**: `emulator -avd Pixel_7`／`adb push <jpg> /sdcard/Pictures/...`＋`content call --uri content://media --method scan_volume --arg external_primary`／権限は `pm grant ... READ_MEDIA_IMAGES` ＋ `appops set ... ACCESS_MEDIA_LOCATION allow`／`gradlew -p android assembleDebug`＋`adb install -r`。テスト画像生成 py はセッションの scratchpad（gen_test_photos.py）。**注意＝エミュは DATE_TAKEN を NULL にし GPS を redact する**（保存日フォールバック検証には好都合／GPS は実機で）。
> - **⚠️ Android v1 でやらない**: 背景位置・広告（＝Apple 1.2 の背景位置審査の結果を待ってから Android v2 で）。
> - **実機の検討（ユーザー）**: **背景位置ロガー・写真の GPS/足跡・大量スループット・体感速度**はエミュで測れない＝**中古 Android（Android14+・RAM6GB+・SIMフリー・ネットワーク利用制限○）**を購入検討中（ロガー用途と兼用）。買い物ブリーフは会話に作成済。
>
> - **前提**: Windows に Android Studio＋SDK＋エミュ導入済（SDK パス `C:\Users\yutsu\AppData\Local\Android\Sdk`・JBR21）＝ローカル開発ループ確立＝Codemagic 待ちゼロ。
>
> ### 📍（参考）セッション17 の再開ポイント（2026-07-03・上記 scaffold で消化開始）
>
> ## 🤖 次セッション＝Android 着手（Apple 審査を待たずに進める）
> - **決定（この回・ユーザー）**: 1.2(位置) を審査提出した直後、「次は Android／Apple が却下でも待つべきか」→ **待たない・進めてよい**。Apple(iOS) と Google Play は別審査、共有は web(index.html) のみ。唯一のリスク共有面＝**バックグラウンド位置**（Google の方が厳格）。**対策＝Android v1 はクリーン版（位置なし・広告なし・`NATIVE_LOCATION=false`・背景位置プラグインなし）**＝iOS 1.0 のクリーン起動成功パターンを踏襲＝リスク共有面が Android v1 に入らない→Apple 1.2 の結果を待つ必要が構造的に消える。背景位置は Android v2（Apple 1.2 の結果を学びに）。詳細＝[[post-launch-roadmap]]。
> - **▶ 次セッションの最初の一手**: ①（習慣）Gmail で **Apple 1.2 の審査結果**を確認（[[session-start-gmail-check]]）＝承認なら手動リリース／却下なら理由を Android にも反映。②**Android 最小スパイク**＝`npx cap add android` → エミュ/実機で**クリーン版が起動**するか（Codemagic は Android ビルド対応）。③次に**写真全件アクセスの Android ネイティブ実装＝MediaStore を Kotlin で書き直し**（iOS の自前 Swift `photo-library` は移植不可・本丸の工数・[[native-photo-access-works]] の Android 版・Apple 審査と無関係に de-risk 可）。④Google Play 登録($25・公開時のみ)・署名(Play App Signing)。地図(Leaflet)・体験ロジックは web 共通で乗る。
> - **⚠️ Android v1 でやらない**: 背景位置・広告（＝Apple 1.2 の背景位置審査の結果を待ってから Android v2 で）。
>
> ## 📤 1.2(29)＝位置ロガー版を審査提出＝「審査待ち」（2026-07-03・完了）
> - **提出**: バージョン **1.2** / ビルド **29**（Codemagic `$BUILD_NUMBER` 自動採番で 27/28/29 の3本→最新29を選択）/ **手動リリース**。提出ID `e14c7e4c-32e5-44d6-aec7-9bea43241a0b`・9:25。中身＝v138 位置ロガー native 復活＋v139 マージ線＋v140 自動位置確定/由来バッジ/日別・全削除。**位置のみ・広告なし**。
> - **ASC でやったこと（Chrome 拡張）**: ①1.2 バージョンページ新規作成（メタは 1.1 引き継ぎ）②What's New 日英入力＆保存 ③ビルド29(1.2)紐付け ④**審査メモを 1.2 用に差し替え**（旧 1.1 メモを ctrl+a→delete で消して、背景位置×Data Not Collected の説明＋テスト手順を入力）⑤手動リリース確認⑥「審査用に追加」→提出物の下書きへ→**「審査へ提出」はユーザーが押下**（輸出/IDFA/コンテンツ権利＝従来どおり）。
> - **✅ 審査対応（v138 で仕込み済み・触らない）**: 権限文言 ja/en（端末内のみ・外部送信なし・ロガーONの時だけ）／`UIBackgroundModes=location`／**PrivacyInfo.xcprivacy＝Data Not Collected＋UserDefaults CA92.1**。App プライバシー・スクショ・連絡先は 1.1 引き継ぎ。
> - **▶ 次**: 審査結果待ち（[[session-start-gmail-check]]）→承認後 ASC で手動リリース（1.2 は日本＋ex-EU 148地域＝1.1 の配信設定を継承）。**却下されても Android は待たない**（上記）。
> - **状態**: native=**1.2(29) 審査待ち**／1.1(26) 公開中（ex-EU 148）／web=GitHub Pages `phase3.93`（v140）。
>
> ## 🎉🎉🌍 世界版デビュー！ 1.1(26)＝英語版を承認→リリース＋配信 ex-EU 拡大（2026-07-03）
> - **完了（この回・ユーザー明示「リリース→直後に ex-EU 拡大をセットで」）**: ①1.1(26)（英語(US)ローカライズ入り・v135込み）が**審査通過**（メール確認）→②ASC「このバージョンをリリース」→「配信準備完了」（日本・最大24hで反映）→③**配信地域を ex-EU に拡大**＝「価格および配信状況」→「配信状況を管理」→「すべて」(175)→**EU27 のチェックを外す**（アイルランド/イタリア/エストニア/オーストリア/オランダ/キプロス/ギリシャ/クロアチア/スウェーデン/スペイン/スロバキア/スロベニア/チェコ/デンマーク/ドイツ/ハンガリー/フィンランド/フランス/ブルガリア/ベルギー/ポーランド/ポルトガル/マルタ/ラトビア/リトアニア/ルクセンブルク/ルーマニア）→**148地域**で保存（175−27・ヨーロッパ枠は残す非EU 15＝アイスランド/イギリス/ノルウェー/スイス/セルビア/トルコ/ウクライナ/ベラルーシ/ボスニア/モルドバ/モンテネグロ/アルバニア/コソボ/北マケドニア/ロシア＝総数148で検算一致）。**チェック外しはユーザー手動・Claude は27か国リスト提示＋照合**。
> - **段取りが正しかった点**: 配信拡大は「英語1.1が公開される瞬間」に（公開中 build21 は日本語のみ＝先に広げると英語圏に日本語onlyが出るのを回避）。EU除外＝DSA業者住所公開＋広告GDPR同意(CMP)を回避（[[post-launch-roadmap]]）。言語出し分け＝端末言語（navigator.language: ja→日本語/他→英語）＝1アプリ両言語・地域とは別軸。
> - **▶ 次セッションの最初の一手**: ①各国 App Store への公開反映を確認（数h〜24h・"is now available"）②**ロードマップ② 位置＋広告版**へ＝**位置は 1.2＝v138 で NATIVE_LOCATION 復活済**（Info.plist NSLocation×2＋UIBackgroundModes・InfoPlist.strings ja/en 用途文言・v139 で写真点＋ロガー点を時刻順1本のマージ線）→ **Codemagic 1.2 ビルドをユーザーが手動トリガー→ TestFlight 実機確認**（位置ダイアログ/🛰️3モード/背景記録/v139 マージ線・v136 ピンチズームも同時確認可）→ **1.1公開反映後に 1.2 を審査提出**。広告(AdMob 外周のみ・[[monetization-v1-adfree]]）は続けて。
> - **状態**: native=**1.1(26) リリース済（配信準備完了・ex-EU 148地域）**／1.0(21) から差し替わり／web=GitHub Pages `phase3.93`（v140 まで）。1.2 は未ビルド（**v138-140 は push 済・要 Codemagic トリガー**＝位置ロガー復活＋写真の自動位置確定＋由来バッジ＋日別/全削除が 1.2 に載る）。
>
> ### 📍（参考）セッション15 の再開ポイント（2026-07-02・上記で消化済み）
>
> ## 🎉 1.1(26)＝世界版（英語）を審査へ提出＝「審査待ち」（2026-07-02）
> - **やったこと（Chrome 拡張で ASC 操作・全ステップ完了）**: ①**1.1 バージョンページ作成**（日本語メタデータは 1.0 から引き継ぎ）②**英語(US)ローカライズ追加**＝ドラフト済みコピーを入力（説明/プロモ文/キーワード99字/What's New "A Past Day now speaks English…"）。※新規ローカライズは主言語の値が既定コピーされる＝上書きでOK・**日本語ロケールは無傷を確認済み** ③**日本語 What's New** も記入（「英語表示に対応…」）④**アプリ情報**＝EN 名前 `A Past Day`＋サブタイトル `Photos and footprints, revived`（30字ちょうど・**重複エラーなし＝名前確保**）⑤**ビルド 26**（1.1・v135込み＝ユーザー実機英語スクショと同内容）紐付け ⑥**英語スクショ5枚**（ユーザー撮影→1284×2778 に変換→ユーザーがドラッグUP・並び順もユーザー調整）⑦**「審査用に追加」→「審査へ提出」＝「1項目が提出されました」**（ユーザー明示承認済み・手動リリース設定維持）。
> - **⚠️ ハマった罠（次回のため）**: (a) **スクショ枠は 6.5インチ**（1.0 の日本語スクショが 6.5" のため）＝受付は 1242×2688/1284×2778 のみ。6.9" サイズ 1290×2796 は「寸法が正しくありません」で弾かれる。(b) **Chrome 拡張の file_upload はチャット添付ファイルのみ可**（「フォルダーを追加」した Desktop のファイルは不可）→ ASC へのファイルUPは**ユーザーのドラッグ&ドロップが最速**。(c) アプリ情報の名前/サブタイトル欄は React 制御で form_input が巻き戻る→**クリック＋ctrl+a＋type** で入力。(d) この日の朝、Anthropic 側の安全判定サービス障害で全ネットワーク系ツールが約40分ブロック（Monitor で復旧待ちリトライ）。
> - **▶ 次セッションの最初の一手**: ①Gmail で審査結果確認（[[session-start-gmail-check]]）。**承認なら → ユーザーに公開可否を確認 → ASC「このバージョンをリリース」→ 🌍 公開と同時に配信地域を ex-EU に拡大**（「特定」で全チェック→EU27外す・手順検証済み・[[post-launch-roadmap]]）＝**この2つはセット**（公開前に広げない＝公開中 build21 は日本語のみ）。リジェクトなら理由対応。②**v136 ピンチズームの実機確認**（GitHub Pages phase3.89・拡大ビューで2本指ズーム/パン/スワイプ共存の手触り）→ OK なら次の native ビルド(1.1.x or 1.2)に載る（審査中の 26 には入っていない＝提出後の web 変更）。
> - **状態**: native=**1.1(26) 審査待ち**（英語ローカライズ入り・日本のみ配信のまま）／1.0(21) は公開中（日本）／web=GitHub Pages `phase3.88`（v135 まで）。スクショ素材＝`Desktop\anohi\`（1284×2778 版・orig-backup に元縮小版）。
>
> ### 📍（参考）セッション14 の再開ポイント（2026-07-02・上記で消化済み）
>
> ## 🌍 英語圏デビュー準備（進行中）＝ロードマップ① 世界版。1.1 として ex-EU に出す
> - **▶ 次セッションの最初の一手**: ①（習慣）Gmail 確認（[[session-start-gmail-check]]・公開済なので要対応は少ないが Apple/App Store 系を一応）②**新 1.1 ビルド（v135 込み）が TestFlight/ASC に「1.1・終了」で出ているか確認**（ビルドは**ユーザーが手動トリガー**・前ビルドで pbxproj 成功実証済＝コンパイルは通るはず）。**出ていれば → 下の残タスク 3〜6**（ASC 英語ローカライズ入力→1.1 版作成＋ビルド紐付け→配信 ex-EU→審査提出）。**まだなら** ユーザーに Codemagic でビルドを回してもらう。※英語コピー全文はこの下＆CHANGELOG/会話にドラフト済。
> - **決定**: 英語 i18n（v130-133）＋世界地図が入ったので英語圏へ拡大。**ユーザー選択＝「きちんと英語で出す」**（英語店頭情報＋英語スクショ＋端末名も英語）。バージョンは国際化アップデートとして **1.0.1→1.1**。**英語アプリ名＝A Past Day**（ストア名30字制限でフル "A Past Day — Photos and Footprints" は入らず→名前=A Past Day／サブタイトルで Photos and footprints を表現）。
> - **⚠️ 最重要の段取り**: 公開中の **build21 は英語化前＝日本語のみ**（英語i18nはv130以降）。だから**配信地域を今 ex-EU に広げると英語圏に日本語onlyのbuild21が出てしまう**→NG。正しくは **英語の1.1を審査通過→公開する瞬間に配信地域を ex-EU へ**。
> - **✅ この回でやったこと（native・コミット済）**: 端末表示名をロケール別に＝`ios/App/App/{en,ja}.lproj/InfoPlist.strings`（en=A Past Day / ja=あの日 — 写真と足跡）を新設し `project.pbxproj` に variant group 登録（BuildFile/FileRef×2/Appグループ/Resources/VariantGroup/knownRegions に ja／PrivacyInfo と同流儀・Python でアンカー置換）。`MARKETING_VERSION` 1.0.1→**1.1**（Debug/Release）。Info.plist の stale コメント（en=Madeleine）を修正。→ **push で Codemagic が 1.1 ビルド生成中**（英語端末名入り）。**要・ビルド成功確認**（Mac無し手編集のため）。
> - **✅ 追記（この会話の続き）**: ① Codemagic 1.1 ビルド＝Publishing 通過→「App Store distribution」後処理まで到達＝**IPA のビルド/アップロード成功＝手編集 pbxproj は無事**（"Did not find build... waiting" は Apple 処理待ちで正常）。②ユーザーが**英語スクショ撮影**→ トップ/On This Day は英語OK・**地図とタイムラインに未訳が残存**（全期間/日付をタップで絞り込み/✕解除）。③**v135 で未訳を一括 tr() 化**（地図バー・封印パネル・取り込み進捗/エラー・記念日確認・ヒント等／CHANGELOG v135・preview green ?lang=en）。→ **この web 修正を載せた新ビルド1本が必要**（native 1.1・build 番号は上がる）。**ビルドは手動トリガー**（ユーザー実施）。
> - **▶ 残タスク（次の一手）**:
>   1. **v135 込みの新 1.1 ビルドをユーザーがトリガー → TestFlight/ASC に「1.1・終了」で出るか確認**（前ビルドで pbxproj は成功実証済なのでコンパイルは通るはず）。
>   2. **英語スクリーンショット**（ユーザーが TestFlight を端末=英語で開いて撮影→私が規定サイズ調整）。
>   3. **ASC 英語ローカライズ入力**（英語コピーはこの会話でドラフト済＝名前 A Past Day／サブタイトル Photos and footprints, revived／説明／キーワード）。
>   4. **1.1 バージョンページ作成＋ビルド紐付け**。
>   5. **配信地域を ex-EU に**（除外手順は前回検証済み＝「特定」で全チェック→EU27外す・[[post-launch-roadmap]]）。
>   6. **審査へ提出**（最終ボタンはユーザー確認）→承認→公開＋地域拡大。
> - **📝 英語ストアコピー（ドラフト・ASC 英語(US)ローカライズにそのまま入力可・ユーザー "A Past Day" 承認済）**:
>   - **Name (≤30)**: `A Past Day`
>   - **Subtitle (≤30)**: `Photos and footprints, revived`
>   - **Promotional text**: `Rediscover a day you'd forgotten. From your own photos, A Past Day brings a long-lost memory back — by chance, on this day, along the footprints you left. All on-device.`
>   - **Keywords (≤100)**: `memories,nostalgia,reminiscence,on this day,throwback,footprints,album,diary,map,remember,forgotten`
>   - **Description**:
>     ```
>     A Past Day quietly brings back memories you'd long forgotten.
>
>     Not the photos you look at all the time — the ones you'd forgotten you had. A Past Day draws from your own photo library and, by chance, surfaces a single picture from a day you haven't thought about in years. One tap follows the thread — to nearby places, the same season in other years, a color, a feeling — and a memory you didn't know you still had comes quietly back.
>
>     • By chance — you don't search; a forgotten day finds you.
>     • Long ago — not yesterday's photos, but the ones from years back.
>     • Revived — one picture pulls a whole day back into focus.
>
>     On This Day
>     See this date across every year you have photos for. Feel how much time has passed — and imagine where the same stretch of time ahead might take you.
>
>     Footprints on the map
>     Photos with a location connect into the path you walked that day. Retrace where you went — and remember things the photos alone don't show.
>
>     Wander
>     From any photo, take an associative walk through your library — by place, time, or color. Each step is a small, involuntary discovery.
>
>     Everything stays on your device
>     Your photos are never uploaded to the cloud or any server. Storage and on-device AI analysis all happen locally on your iPhone. Nothing leaves your device.
>
>     Start with about 20–30 photos, or bring in your whole library.
>     ```
>   - スクショ＝ヒーロー(A Past Day/滝)と On This Day は英語OK・**地図とタイムラインは v135 込みビルドで撮り直し**。
>
> ### 📍 次セッションの再開ポイント（2026-07-01 セッション13 更新・まずここを読む）
>
> ## 🎉🎉🎉 App Store 一般公開を実行＝デビュー！（2026-07-01）承認済みビルド1.0(21)を手動リリース → 公開反映待ち
> - **やったこと**: セッション開始の挨拶 → DOCMAP/TODO ＋ Gmail 確認で **承認メール2通**発見（提出ID別）＝①`c4451f04…`（Jun29提出＝ビルド16・公開しない旧UI版）②`56430048…`（Jun30提出＝**ビルド21・v111-129サクサクUI＝デビュー版**）**両方 eligible for distribution**（リジェクト/追加質問ゼロ・21の審査は約2.5hで完了）。→ ユーザー「今すぐ公開する」判断 → **Chrome 拡張で ASC 操作**: 配信ページで公開前点検（①状態=Pending Developer Release ②乗っているビルド=**21**（バージョン1.0・APP CLIP なし・玉ボケアイコン）③契約=有料アプリ契約/無料アプリ契約とも**有効**〔2026-06-25〜2027-06-25・全地域〕・銀行〔三井住友8154〕有効・納税〔W-8BEN 等〕有効・保留/警告なし）→ **「このバージョンをリリース」→ 確認ダイアログ「1個の国または地域(日本)でリリースしますか？」→ 確定**。状態が **🟡デベロッパによるリリース待ち → ✅配信準備完了** に遷移（URL `inflight→deliverable`・リリースボタン消失＝受理・再読込後も確定）。**日本の App Store 向け(1地域)に最大24hで一般公開**。
> - **▶ 次セッションの最初の一手＝公開反映の確認**（[[session-start-gmail-check]]）: ①**Gmail で "is now available on the App Store" メール**着を確認 ②`https://apps.apple.com/jp/app/id6784557053` or App Store 検索「あの日 写真と足跡」で実際に出るか（反映は最大24h）。出ていたら**ユーザーに一報＝デビュー完了**。
> - **▶ その後＝[[post-launch-roadmap]]**: ①**世界版（英語）**＝web の英語 i18n は v130-133 でほぼ完了（dev以外）→ 残り＝ASC 英語ローカライズ名（A Past Day — Photos and Footprints）＋`InfoPlist.strings` en＋実機QA（?lang=en 取りこぼし拾い）＋配信地域拡大（**EU27除外**＝DSA住所公開回避）。②**位置＋広告版**＝位置ロガー復活（`NATIVE_LOCATION=true` 等）＋AdMob 外周のみ（[[monetization-v1-adfree]]）。③Android。**公開後の次版は 1.0.1/1.1** として Codemagic 新ビルド→審査。
> - **⚠️ 公開直後の落とし穴＝新ビルド upload 失敗（対応済み・2026-07-02）**: 公開後の Codemagic ビルド22/23 が Publishing で失敗＝Apple `90186 train '1.0' is closed`／`90062 CFBundleShortVersionString must be higher than 1.0`。**1.0 が公開された＝1.0 train 締切**なので新ビルドは番号を上げる必要あり → [project.pbxproj](ios/App/App.xcodeproj/project.pbxproj) の `MARKETING_VERSION` を **1.0→1.0.1**（Debug/Release 両方）。**以降の Codemagic ビルドから 1.0.1 で upload 成功**・公開版21には無影響。詳細 CHANGELOG（native fix 2026-07-02）。
> - **状態**: native=**ビルド1.0(21) 公開処理中（日本のみ・最大24hで反映）／新ビルドの marketing version=1.0.1**（1.0 train は公開で締切）／web=GitHub Pages `phase3.87`（v133 英語 i18n dev以外ほぼ完了 ＋ v134 初期画面ボタンに（おすすめ））。承認済み16は非公開のまま放棄。
>
> ### 📍 次セッションの再開ポイント（2026-07-01 セッション12 更新・まずここを読む）
>
> ## 🌍 セッション12＝英語 i18n を web に実装（v130-133・dev以外ほぼ全画面が英語）＋海外地図フォールバック。native 1.0(21) は審査待ちのまま・非接触
> - **▶ 次セッションの最初の一手＝Gmail で審査結果を確認**（[[session-start-gmail-check]]）。native **1.0(21) が審査中**→ ①承認ならユーザーに公開可否を確認→ ASC「このバージョンをリリース」でデビュー ②リジェクトなら理由を読んで対応。**web の英語 i18n は一段落**（dev以外ほぼ完全）＝審査が動くまでは待ちで OK（手を動かすなら「英語の実機 QA」＝?lang=en で各画面を開き取りこぼし/不自然を拾う、または ロードマップ②以降）。
> - **native の状態は不変**＝**ビルド1.0(21) 審査待ち**（デビュー審査・2回目）。**Gmail 監視継続**（[[session-start-gmail-check]]）＝承認→ユーザーに公開可否を確認→ ASC「このバージョンをリリース」＝デビュー／リジェクト→理由対応。配信地域＝日本のみ。契約(Agreements/Tax/Banking)有効も承認後に確認。
> - **この回でやったこと（v130）**: 審査待ちの間に**ロードマップ①の基盤を web に先行投入**（[[post-launch-roadmap]]）。ユーザー選択＝「基盤＋主要導線」「世界地図＝CARTO 3枚」。①**i18n 基盤**＝`LANG`（navigator.language・`?lang=` で上書き）＋`t(ja,en)`（ja は第1引数そのまま＝**日本語は完全に不変**、非日本語は第2引数・未指定は ja フォールバック＝段階移行）＋`applyStaticI18n`（静的ヘッダは en のみ書き換え）。②**主要導線を英語化**＝ヘッダ（A Past Day / Photos & Footprints / On This Day / On Tomorrow / ⚙メニュー / タブ名）・空状態・取り込み進捗・情報モーダル見出し/プライバシー/閉じる・枚数。③**世界地図**＝`BASE_LAYERS` を LANG 出し分け（ja=地理院のまま／非日本語=CARTO Voyager(標準)/Positron(淡色)・key 共通で localStorage 互換・描画/保存ロジック無改修）。詳細 CHANGELOG v130。
> - **検証**: preview green＝`?lang=en` 全面英語（ヘッダ/空状態/タブ名）・`?lang=ja` 現状と完全一致（回帰なし）・両モード console エラー0。**実機（GitHub Pages）＝英語ブラウザ or `…/index.html?lang=en` で英語＋世界地図を確認できる**。
> - **続けて v131（ユーザー実機フィードバック2点）**: ①**海外地図フォールバック**＝日本語モードの地図に世界の下地(CARTO)を常時敷き、日本人が海外旅行の写真を見ても地理院の外(海外)が真っ白にならずシームレス（`worldUnderlay`・ja のみ・切替時も最背面維持）＝v130「言語で地図を切替」の穴（**言語≠見る場所**）を塞いだ。②**主要導線の残りを英語化**＝Explore で未訳UI約150箇所を網羅→触る主要画面（フィルタチップ/連想ウォーク=Wander/写真詳細/地図ツールバー・popup・フィルタ・⋯・タイムライン・囲む比較/ヘッダtitle・機能名統一 Wander/Footprints/Anniversary/Seal）を t() 化。**二次画面（記念日詳細/封印/情報モーダル本文/確認ダイアログ/位置ロガー説明/dev）は次段**。preview green（?lang=en 全面英語・?lang=ja 不変・console 0）。詳細 CHANGELOG v131。**実機で英語各画面＋海外地図の下地を確認待ち**。
> - **続けて v132（ユーザー指示「二次画面も英語表示を」）**: 残りの二次画面を tr() 化＝記念日(一覧/ログ/基準日/登録/予定/代表写真)・封印パネル・ⓘヘルプ・AIモーダル(β説明含む)・削除写真一覧・確認ダイアログ(位置ロガー記録削除/記念日削除/全削除)・各トースト(除外/取り込み/SNS zip)・トップ「N枚から」件数・フィルタ結果ゼロ時。⚠️**i18n関数 `t()` が既存の `t` ローカル変数(20箇所・時刻/要素/一時変数)と衝突→「t is not a function」の潜在バグ（v130/131 で作り込み・空状態 preview では出ず画面を開くとクラッシュ）→ `t()`→`tr()` に全面改名**（sed `\bt(`→`tr(` 一括・Grep で既存 t( 関数呼び出しゼロを確認してから）＝一挙に解消。教訓＝1文字の汎用名をグローバル関数にしない。preview green（?lang=en 空状態英語・console 0）。詳細 CHANGELOG v132。**実機で各二次画面を英語確認待ち**。
> - **続けて v133（ユーザー実機「全部英語対応に」）**: 実機で発見の残り＋長文を tr() 化＝写真詳細の🗺ボタン（**3箇所に重複実装**・showFullImageはv132済／トップカードL4138・中心カードL4576が未訳だった）・スワイプヒント・取り込みドロップダウン(ファイル/フォルダ/詳細設定)・詳細設定モーダル・**情報モーダル本文6段落**・自動取り込みプロンプト・**位置ロガーパネル全体**・**日時ソースタグ(撮影日/保存日/投稿日=dateSourceLabel)**。**dev(🧪 #dev)のみ日本語**＝開発者専用でユーザー非表示。**これで dev 以外ほぼ完全英語化**。教訓＝同一ラベルの重複実装は静的網羅1回で拾えず**実機確認が最終網羅**（実機FB 3連続で新未訳を発掘・[[preview-raf-not-firing]] 系）。preview green。詳細 CHANGELOG v133。
> - **▶ 次の一手（英語版を実際に出す時＝ロードマップ①の本番。今は審査待ち優先）**: ①**UI英語化はほぼ完了**（残るは dev のみ・ユーザー非表示なので実質不要）→ 次は英語の細部QA（実機で各画面を英語で開き取りこぼし/文脈の不自然を拾う）②ASC 英語ローカライズ名（A Past Day — Photos and Footprints）＋`InfoPlist.strings` en③**配信地域を広げる際 EU27 は除外**（DSA 業者住所公開回避・[[post-launch-roadmap]]）。native はこの web を包むので次版で自動的に i18n が乗る。
>
> ### 📍 次セッションの再開ポイント（2026-07-01 セッション11 更新・まずここを読む）
>
> ## ✅ ビルド1.0(21) を ASC で再提出完了 → 「審査待ち」（デビュー審査・2回目）
> - **やったこと（Chrome 拡張で ASC 操作）**: 承認済み 1.0(16) を **「リリースをキャンセル」**（配信ページのバナー・Pending Developer Release→「提出準備中」に戻る／スクショ・説明・キーワード・サポートURL・プライバシー・連絡先など**メタデータ全保持**／16の承認は放棄）→ ビルド欄で 16 を削除 → **21 を追加**（v111-129 の仕上げUI入り・Codemagic 生成・「確認済み」）→ 審査メモを更新（「承認済み16と同一＋UI/レイアウト改善のみ・機能/データ収集/権限に変更なし・端末内完結」）→ 「審査用に追加」→ 提出物の下書き**ブロッカーゼロ** → **「審査へ提出」（輸出/DSA 追加質問ゼロ）→ 送信成功＝ステータス「審査待ち」**。**手動リリース設定は維持**（承認後 ASC で「このバージョンをリリース」を押した時に公開）。
> - **⚠️ ハマった罠（次回のため）**: 16 を外して「ビルドを追加」を開くと**ダイアログに 16 しか出ない**（差し替え先の 17-21 が見えない）。原因＝キャンセル直後のクライアント側キャッシュ。**対処＝一旦「保存」してページを再読み込み**すると全ビルド[16-21]が表示され 21 を選べる。
> - **▶ 次セッションの一手**: **審査結果待ち（最大48h・完了でメール）**。セッション開始時に Gmail で確認（[[session-start-gmail-check]]）。①**承認→ユーザーに公開可否を確認→ ASC のバージョンページで「このバージョンをリリース」を押す＝一般公開デビュー**（最大24hで App Store に反映）。契約（Agreements/Tax/Banking）有効も確認。②**リジェクト→理由を読んで対応**（16 は同じメタデータで承認済み＝バイナリ差分は UI のみなので大きな指摘は考えにくいが、出たら個別に）。
> - **✅ v1 配信地域＝日本のみに確定（2026-07-01・デビュー用）**: ASC 配信地域＝全175だった → 一旦 EU27除外(148) にした後、**「日本のみ(1地域)」に確定**（ASC「配信状況（1つの国または地域）」・日本=配信可能／他174=配信不可）。理由＝v1 は日本語UIのみ→**非日本語圏に「読めない日本語アプリ」を見せない**＝第一印象を守る（16を出さない判断と同精神）。**言語の出し分け方針＝ストア地域でなく端末言語**（WebViewなので `navigator.language`：ja→日本語／それ以外→英語フォールバック＝**1アプリに両言語**・日本語端末は不変）。地域と言語は別軸（地域=どこでDL可／言語=何語表示）。**拡大はロードマップ①**＝英語 i18n＋世界地図＋ASC英語ローカライズを積んだ更新で配信地域を広げる（その際 **EU27は除外**＝DSA業者住所公開回避／除外手順は今回検証済み＝「特定」で全チェック→EU27外す・[[post-launch-roadmap]]）。地域変更は審査をリセットしない。
> - **状態**: native=**ビルド1.0(21) 審査待ち**（16は承認済みだが公開せず放棄）。web=GitHub Pages `phase3.82`（v129 まで反映）。TestFlight は 1.0(21) が最新（Jul 1 13:44・以降の Codemagic 自動ビルドが積み増す可能性）。**公開後＝次版**で英語名 A Past Day／iPad／位置ロガー復活／広告（[[monetization-v1-adfree]]）／未来=カレンダー書き出し(EventKit)。
>
> ### 📍 次セッションの再開ポイント（2026-07-01 セッション10 更新）
>
> ## 🔀 方針転換＝承認済み 1.0(16) は **release しない**。UI をブラッシュアップして再提出しデビュー版にする
> - **理由（ユーザー・07-01）**: 審査に出した 1.0(16) は**旧UI**（v110 名称修正時点＝縦リスト等）で、v111-127 の「サクサク」UI より明確に見劣り。**最初の一般公開＝第一印象**なので良いUIでデビューさせたい。→ **ASC の公開ボタンは押さない**。UI を仕上げた Codemagic 新ビルドを再提出し、それをデビュー版にする。
> - **▶ 次セッションの一手**: ①**v129（＋あの日の続き・記念日の基準日 N日前/N日後）を実機（GitHub Pages `phase3.82`）で確認**（v128 の3点は実機「いいかんじ」YES済）→ 続けたい UI 直しがあれば対応 ②UI が固まったら **ASC（Chrome 拡張）を開いて実際のボタンで再提出手順を確定**（承認済み 16→新ビルドへ差し替え。Pending Developer Release 状態からの差し替えは挙動が変わるので憶測で操作せず現物を見る）。TestFlight は 1.0(20) まで自動蓄積（v111-129 系）。**Gmail 監視は継続**（承認は受領済・以後の新着＝リリース/契約系を見る）。
> - **Gmail 確認済（07-01）**: 「Welcome to the App Store」「Review complete＝eligible for distribution」受領＝**承認確定**。リジェクト/契約警告なし。「is now available」メール未着＝手動 release 未実施のため想定どおり。
>
> **この回でやったこと（v128＝UI微調整3点・ユーザー実機フィードバック）**:
> - **① 地図「📖記念日のログ」の無反応を解消**（[index.html](index.html)）: 症状＝通常の「この日の地図へ」から入った地図の⋯表示で「📖記念日のログ」をタップしても無反応。原因＝`.mc-menu-item{display:block}` が UA の `[hidden]{display:none}` を上書き→`#mmAnnivLog` は常時表示なのにハンドラが `mapState.anniv` の時しか動かなかった（**v127 の「anniv 時だけ表示」は実際は非成立**）。対処＝ハンドラに `else openAnnivList()`（通常地図→**記念日一覧**）を追加、`hidden` 属性と elMoreBtn のトグルを撤去（常時表示に正直化）。記念日由来→そのログ（`openAnnivTimeline`）は維持（ユーザー要望）。一覧→行タップ→そのログ、と自然にドリルイン。L3010/L3033/L3068付近。
> - **② 地図の種類チップを1行に**: `#mmLayers` に限定して `flex-wrap:nowrap`＋各ボタン `flex:1 1 0;min-width:0;font-size:11.5px;white-space:nowrap`＝均等3分割。`.mc-pop` 幅300px→各≈85px で「地理院(標準)」もクリップせず。CSS L320付近。preview 実測＝3つ同一行・clipped:false。
> - **③ ヘッダを2段に圧縮**: `#count`/`#bgStatus` を `<h1>` 内へ移動＝枚数がタイトル行に乗り、⚙が2段目に収まる（モバイルで 3段→2段）。JS は getElementById 参照なので無改修。header HTML L446付近。preview（mobile）＝枚数がタイトル行・⚙が2段目、desktop も1行で回帰なし。
> - **教訓**: `hidden` 属性は要素に `display:...` が当たっていると効かない（author 規則が UA `[hidden]` に勝つ）。属性で表示制御するなら明示 display を持たせない or `[hidden]{display:none!important}`。今回は「常時表示＋分岐動作」に倒して解消。
> - 検証＝inline script vm.Script **0エラー**／preview で②③実測 green・①はロジック（分岐＋既存 openAnnivList・空表示対応・anniv-z z5000 で地図の上）で担保。**🎉実機（GitHub Pages）で3点とも「いいかんじ」YES**。
>
> **続けて v129＝記念日ログの表現＋基準日（ユーザー要望2点）**:
> - **① 表現「未来の予定」→「あの日の続き」**（[index.html](index.html)）: 入口ボタン `＋あの日の続き`・追加モーダル見出し `🗓あの日の続き`・未来バッジ `予定→これから`。**"予定/計画"の作業感を外し核＝記憶の続きへ寄せる**。`planned` データは不変＝表示だけの変更。
> - **② 記念日に任意の基準日→各日に「N日前／N日後」**: `a.baseDate`（任意・**IndexedDB スキーマ変更なし**＝annivPut が object 丸ごと保存）。ログ上部に**基準日バー**（設定/変更/解除・既定候補=最古訪問日=あの日）、各日付の横に `N日後／N日前／基準日(tealピル)`＝`Math.round((dateMs(v.date)-dateMs(baseDate))/86400000)`・`toLocaleString()` 千位区切り。関数 `setAnnivBaseDate(a)`／render に baseBar・offset／CSS `.atl-basebar`・`.atl-offset(.base)`。詳細 CHANGELOG v129。
> - 検証＝vm.Script 0エラー／preview でテスト記念日注入＝バッジ「これから」/ボタン「＋あの日の続き」/基準日=2019-04-01 で 2021-08-10 が **862日後（検算一致）**・中間基準日(2024)で過去=N日前/未来=N日後の両方向・色 inspect（offset=teal #7fe3d6・基準日ピル=#33c1b0）。スクショ機構はタイムアウトだが eval/inspect は正常応答＝描画は正しい。**実機（GitHub Pages phase3.82）で確認待ち**。
>
> ### 📍 次セッションの再開ポイント（2026-06-30〜07-01 セッション9 更新）
>
> ## 🎉🎉 App Store 承認！（2026-07-01）  ⚠️**この節の「公開」方針は セッション10 で覆った＝16 は release しない（上の再開ポイント参照）。以下は当時の記録**
> - **状態**: ビルド **1.0(16)** が審査通過＝「Welcome to the App Store / It is now eligible for distribution」。**手動リリース設定**（v109）なので **ASC のバージョンページで「このバージョンをリリース」を押すまで非公開**。押すと最大24hで App Store に公開。
> - ~~**▶ 次セッションの最初の一手**: ユーザーに「公開しますか？」を確認 → ASC で「このバージョンをリリース」を押す~~ ← **無効（セッション10で「16は公開せず、UI をブラッシュアップして再提出」に変更）**。契約（Agreements/Tax/Banking）有効の確認だけは再提出時にも有用。
> - **公開したら**: v111-127 の web 改善は**審査中1.0(16)とは別トラック**（Codemagic の新ビルド 17-19+ に既に乗っている）。公開後に **次版 1.0.1 / 1.1** としてまとめて審査へ。次版で英語名 A Past Day／未来=カレンダー書き出し／位置ロガー復活／iPad／広告 も検討。
> - web は GitHub Pages `phase3.80`（v119-v127 反映・実機で触れる）。
>
> **この回でやったこと（v119 自動差分取り込み〜v125 微調整、v126 記念日📖ログ、v127 ログ作り込み）**:
> - セッション開始の挨拶 → DOCMAP/TODO + Gmail 確認。**審査結果の新着メールはまだ無し**（再提出後は TestFlight 通知のみ＝1.0(16)→(17)→(18)。(16)=再提出ビルド／(17)(18)=UI改修 push の Codemagic 自動ビルド）。**審査結果待ち（2回目）継続**。✅ **ASC の審査トラックは 16 のまま**（ユーザーが ASC スクショで確認＝17/18 に差し替わっていない＝審査リセットなし）。
> - **v119 実装＝起動時の自動差分取り込み（native）**。ユーザー仕様＝①ライブラリ取り込み済みの人に起動時プロンプト「自動で取り込む？」②はい→以後開くたび差分③いいえでも設定で ON 可④後から OFF 可。
>   - 設計: **静かな背景取り込み**（`runAutoImport()`＝enumerate→差分→`processNativeRest` 再利用・フル画面にしない・新規ゼロなら無音）／**対象判定**＝`collectImportedAssetIds().size>0`（ライブラリ取り込み済みのみ・新規ユーザーは煩わせない）／**初回プロンプトは次の起動で**（boot `maybeAutoImportOnLaunch`・`pms-autoImportAsked` で1回だけ）／**設定トグル**＝取り込み→⚙️詳細設定に native のみチェックボックス（`pms-autoImport`）／**前面復帰でも差分**＝`visibilitychange` に `maybeAutoImportOnResume`（ON時のみ・60秒throttle・iOS は cold launch 稀なので）。全経路 `IS_NATIVE && PhotoLib` ガード＝web 完全無改修。
>   - 関数（[index.html](index.html)）: `autoImportEnabled/setAutoImport/autoImportAsked/markAutoImportAsked`（L1071-）・`runAutoImport`（L1080）・`maybeAutoImportOnLaunch`（L1107）・`maybeAutoImportOnResume`（L1116）・`promptAutoImport`（L1124）。配線＝boot(L4776)・visibilitychange(L2177)・設定モーダル(L4460,4472)。詳細 CHANGELOG v119。
>   - 検証＝inline script を `vm.Script` parse＝**0エラー**。web 観測面（取り込み設定モーダル）は native ブロックが空文字で既存と同一・他は IS_NATIVE 早期 return＝**web preview で再現不可の native 機能のためブラウザ検証はスキップ（preview ガイドライン準拠）**。
> - **v120 実装＝地図「囲んで比べる」**（ユーザーの詳細ハンドオフ）。コア「この場所の、あの頃と今」。地図で一帯を円で囲む（中心を押して外へドラッグ=半径）→円内の写真を**左右2リール（縦 scroll-snap）**で見比べる。
>   - 設計（ハンドオフ順守）: **左右並置**（重ねない＝画角ズレ自体が年月の証拠）／**半径=手が決める「ここ」の粒度**（座標厳密一致で同一地点を定義しない・`haversine ≤ r`）／**スクラブは全期間・時季自由**（純度版作らない＝偶然のドライブ）／**発見は不随意・操作だけ随意**（左右独立スクロール＝片方止めて送る）／初期 **左=最古・右=最新**で対面／既存部品の組合せ（`.full-slide` の scroll-snap を縦にミラー[[ui-swipe-scroll-snap-works]]・年昇順は pickToday 前例）。入口は **⋯表示メニュー「🫧 囲んで比べる」**（ツールバーは v97 バランスを崩さない・当たれば昇格）。GPS 必須。
>   - 関数（[index.html](index.html)）: `photosInCircle`・`startEncircleMode`/`endEncircleMode`（透明 `.encircle-layer` で pointer→`containerPointToLatLng`・`map.dragging` 等を一時停止）・`openCompareView`/`closeCompareView`（`compareState`）。CSS=`.encircle-*`/`.compare-overlay`/`.cmp-reel(縦snap)`/`.cmp-slide`。⋯表示に項目追加・`closeMapView` で後始末。詳細 CHANGELOG v120。
>   - 検証＝preview green（構文0・boot 0・メニュー項目表示・円モード[レイヤが地図被覆/touch-action none/バナー+やめる/件数/end後始末]・比較ビュー[2リール×5・`scroll-snap y mandatory`・スライド全高・左=最古]・`photosInCircle`[3km→5/50m→1・昇順・遠方除外]・haversine[東京⇄大阪402km]）。⚠️**右リール初期最新寄せ(rAF内)は headless preview で rAF 停止のため確認不可＝手動 scrollTop は固定確認・実機で動作（v118 showFullImage と同パターン）**。**ユーザー実機（GitHub Pages）＝「いいかんじ」YES**。
> - **v121 実装＝記念日（過去半分）**（ハンドオフ「記念日 & カレンダー書き出し」）。指示＝**web でもう一段発展→native を試す**。両ハンドオフの推奨順＝過去半分(ローカル)を先に・未来半分(EventKit)は後。
>   - 設計: **円プリミティブ(v120)を保存して使い回す器**＝「この場所に訪問が積もる」。⚠️**curation 境界(⭐お気に入り作らない)を自覚的にまたぐ**(Notion WHY に決定記録)。過去/未来を割る＝過去だけ あの日 に住む・未来は持たず後で iOS カレンダーへ一方向書き出し(native)。データ＝`{id,name,center,radius,created,visits:[{dayKey,date,memo,photoIds}]}`(写真本体は持たず参照のみ・memo は将来UI/器は今)。**IndexedDB v2→v3** に `anniversaries` を `!contains()` ガードで追加(track と同パターン・非破壊)。登録=比較ビューの「📍記念日に登録」(中心が既存円内ならマージ=積もる/無ければ命名し新規)。ページ=⚙️→📍記念日 一覧→詳細(訪問 昔→今)→タップで `openMapView(その日の写真)`=目次。
>   - 関数（[index.html](index.html)）: DB=`annivPut/annivGetAll/annivDelete`＋`ANNIV_STORE`＋onupgradeneeded／`visitsFromPhotos`/`registerAnniversary`/`promptAnnivName`/`openAnnivList`/`openAnnivDetail`/`closeAnnivModals`／比較ビューに `circleInfo` を受け渡し＋登録ボタン／⚙️に `btnAnniv`。CSS=`.anniv-*`/`.ai-modal.anniv-z{z5000}`/`.cmp-anniv`。詳細 CHANGELOG v121。
>   - 検証＝**web で完全動作**（native 限定でない）preview green: 構文0・boot 0・**DB v3 アップグレード(stores=anniversaries/photos/track＝追加のみ)**・visitsFromPhotos(同日2枚→1訪問・昔→今)・登録の新規＋**近接マージ**(総数1のまま訪問追加)・永続化・一覧/詳細UI・**訪問タップ→その日の地図へ**(目次)・登録ボタン表示。途中で z-index 詳細度バグ(`.anniv-z`が`.ai-modal`に負ける)を発見→`.ai-modal.anniv-z`で修正。
> - **v122 実装＝記念日の詳細を作り替え**（ユーザー修正指示）。記念日タップの詳細を「訪問リスト」→**①写真1枚大表示＋スワイプ→②左右で比べる→③足跡の地図**の入れ子に。
>   - 設計（既存部品の組合せ）: ①=**showFullImage(deck) 再利用**（写真リストは `photosInCircle` で再計算=live／無ければ photoId 解決）。**showFullImage を `annivCtx`/`onClose` で拡張**（既存呼び出しは無改修＝全部 annivCtx ガード）＝cap を「⇆左右で比べる」「🗺足跡の地図」に差し替え（汎用🗺/🚶隠す・📅日付は残す）・遷移時は onClose 無効化・閉じたら一覧へ。②=`openCompareView(list, circleInfo)`。③=**`openMapView(null,{pickDays})` 新設**＝全訪問日を days モードで動線表示。**focusedDay をタイムラインにも反映**（`updateTimelineUI` に `.tl-dim`・動線クリック/背景タップで呼ぶ）＝「動線1本→その日だけ明るく＋タイムラインも」。削除は一覧の各行に🗑移設（行を button→div 化）。
>   - 関数（[index.html](index.html)）: `showFullImage`(annivCtx/onClose)・`openMapView`(opts.pickDays)・`openAnnivDetail`(deck化)・`annivPhotoList`・`openAnnivMap`・`updateTimelineUI`(tl-dim)・`openAnnivList`(行に🗑)。CSS=`.tl-dim`/`.anniv-row-main`/`.anniv-row-del`。詳細 CHANGELOG v122。
>   - 検証＝preview green(end-to-end): 一覧行(main＋🗑)→deck(3スライド・⇆/🗺/📅・汎用🗺抑制)→⇆で比較2リール(一覧再オープンなし)→🗺で地図days「📅2日を重ねて」→**動線パスクリックで focused 日は明るいまま・他 picked 日 `tl-dim`・非picked日 tl-out**。⚠️テスト不備メモ: `window.mapState` は let スコープで常に falsy→検証は `window.closeMapView()` を無条件で（[[preview-raf-not-firing]] 系）。
> - **v123 修正＝動線タップの実機「無反応」**（ユーザー報告）。原因＝クリック判定が見た目の細い線(4px)に付いており実機の指では当たらない（preview は座標ピッタリの合成クリックで効いていた＝見落とし）。→ **透明で太い当たり判定線(weight24/opacity0/`bubblingMouseEvents:false`)を最前面に重ねハンドラをそこへ移動**・見た目線(pushCased)は `interactive:false` 化。pickDays の複数日トリップ全般（今日/偶然3日/記念日の足跡の地図）で効く。検証＝hit 線2本(width24/opacity0/interactive)生成・見た目線 非interactive・透明線タップで dim トグル `2→0→2→0`・誤解除なし。詳細 CHANGELOG v123。教訓＝**preview の合成クリック命中は実機タップを保証しない＝ヒット領域は指基準で**（[[preview-raf-not-firing]] 同系）。
> - **v124 微調整4点**（ユーザー実機フィードバック「いい感じ」＋4点）: ①**比較のいきなりスワイプで動かない→即応**（原因＝リールの `<img>` が iOS のタッチを掴む≈1秒／対処＝`.cmp-slide img{pointer-events:none;…}`＋`.cmp-reel{touch-action:pan-y;overscroll-behavior:contain}`）②**比較ビューに「🗺足跡の地図」**（`closeCompareView→showTripsMap(list の distinct dayKey)`）③**ヘッダに「🗺過去の明日」**（`openMapView(null,{todayOffset:1})`＝明日の月日を数年分・別思想「計画の前に過去の明日を知る」・◀▶ はそこから相対）④**囲む後も記念日タップと同じ deck 動線に統一**（共通 `openAnnivDeck(list,circleInfo,canRegister,onClose)`・encircle onUp を `openCompareView`→`openAnnivDeck(...,true)`・`showFullImage` annivCtx に `canRegister`→登録ボタン・`openCompareView` 第3引数 canRegister・`openAnnivMap`→map対応 `showTripsMap`＝差分は📍登録ボタンだけ）。検証＝preview green（過去の明日「📆明日7/2（1年分）」／deck に ⇆/🗺/📍/📅／deck→比較に🗺＋📍／比較→🗺で days モード／比較img pointer-events=none／canRegister=false で📍非表示）。詳細 CHANGELOG v124。
> - **v125 微調整6点**（ユーザー実機フィードバック）: ①**足跡の地図の⋯表示に「📍この辺りを記念日に登録」**（`mmRegister`＝`map.getCenter()`＋中心→画面隅 radius を円化→`photosInCircle`→`registerAnniversary`／登録入口が増えても全部 registerAnniversary に集約）②**🎲 3日→1日**（`pickRandom3`=slice(0,1)・mode名は 'random3' 据置・ラベル各所を1日に）③**足跡拡大「ここから歩く」→「🔮連想ウォーク」**（マーカーpopupと統一）④**位置なし写真はタイムラインのみ、の説明**（ⓘに1行＋`.tl-gpshint`＝GPSなし写真がある時だけタイムライン先頭に注記）⑤**位置ある写真の拡大をタイムライン順スワイプに**（マーカーpopup 実GPS/推定 both のサムネタップ→`showFullImage(p,{list:timelineOrderPhotos(),index})`・新ヘルパ `timelineOrderPhotos()`）⑥**過去の今日は写真ゼロでもサイコロに逃がさない**（`openMapView` 既定を常に `setPick(pickToday(),'today')`＝「今日(0年分)」を正直に・偶然は🎲に一本化）。検証＝preview green（①3訪問登録 ②ランダムな1日 ③🔮連想ウォーク ④ⓘ＋tl-gpshint ⑤deck4枚「1/4」⑥今日7/1 0年分）。詳細 CHANGELOG v125。
> - **v126（ユーザー実機フィードバック3点）**: ①**地図 popup を暗く**（原因＝Leaflet 既定=白背景に `.mp-date`=#eee 白字で撮影日時が読めなかった→`.leaflet-popup-content-wrapper` を #1c1c1c・日時 #fff/太字）②**記念日一覧おしゃれ**（`.anniv-row` カード化＝微グラデ+枠+影・カバー62px・訪問数 teal）③**記念日「📖ログ」＝日別タイムライン**（deck cap の `to-annivlog`・`openAnnivTimeline`）＝各訪問を昔→今→未来で 日付＋代表写真＋コメント。過去訪問は「🖼代表写真を選ぶ」(`openVisitPhotoPicker`＝その日の円内写真をトグル最大6)／コメントは `memo` textarea を change で `annivPut`／「＋未来の予定」(`addPlannedEntry`)で未来日に `{planned:true}` を足しコメント。**v121 の visits 器(memo/photoIds)にそのまま乗る・`planned` フラグのみ追加**。関数 `openAnnivTimeline`/`renderAnnivTimeline`/`addPlannedEntry`/`openVisitPhotoPicker`/`saveAnniv`/`todayDateStr`・CSS `.atl-*`・`openAnnivDeck` に 5th 引数 anniv・annivCtx に anniv。**未来メモは pull のみ/通知なし＝「push しない」原則は維持**（重い予定管理はしない）。検証＝preview green（popup bg rgb28/日時白・一覧 thumb62/name700/sub teal・タイムライン開く/コメント保存/未来2026-09-15 予定追加(バッジ・削除)/代表写真2→1手動保存/deck→📖ログ）。詳細 CHANGELOG v126。
> - **v127（記念日ログの作り込み・ユーザー実機フィードバック5点）**: ①**時系列を逆に**（`renderAnnivTimeline` 降順＝現在→過去へ遡る・新しいが上）②**訪問の間隔で「間」を広げる**（隣の訪問との日付差で `.atl-gap` の高さを log スケール＋45日超は「N年/Nヶ月のあいだ」＝時間の長さを縦の長さで感じる）③**一覧タップ→ログへ直行**（deck→`openAnnivTimeline`・`openAnnivDetail` 削除）④**ログの代表写真タップ→拡大＋記念日内を日にち横断スワイプ**（`openAnnivPhotoZoom`＝`photosInCircle` を list に `showFullImage`・onClose でログへ）⑤**相互動線**＝ログ header に「← 一覧／⇆ 比べる／🗺 足跡の地図」＋逆向き（比較→`.cmp-log`／足跡の地図の⋯表示→`#mmAnnivLog`）。鍵＝`anniv` を `openCompareView`(4th)/`showTripsMap`(2nd)/`openMapView`(opts.anniv→`mapState.anniv`)/`compareState.anniv` に伝播＝どのビューからもログ（物語）へ戻れる。検証＝preview green（降順[2024,2021,2019]・間隔106px「3.2年」/98px「2.4年」・一覧→ログ直行・写真拡大3枚「3/3」日にち横断・ログ↔比較↔ログ・ログ→地図(⋯📖記念日のログ・通常地図では非表示)→ログ）。詳細 CHANGELOG v127。
> - **▶ 次セッションはここから**（★詳細は上の「🎉 App Store 承認！」を先に）:
>   1. **🎉 公開（手動リリース）**＝Gmail 確認後、ユーザーに公開可否を確認 → ASC で「このバージョンをリリース」（Pending Developer Release なら押す）。契約有効も確認。
>   2. **公開後：次版 1.0.1/1.1 の準備**＝v111-127 の web 改善（別トラック）を native 次版へ。同時に 英語名 A Past Day／iPad／位置ロガー復活／広告（[[monetization-v1-adfree]]）／**未来半分＝iOS カレンダー書き出し**（EventKit・write-only `NSCalendarsWriteOnlyAccessUsageDescription`・fire-and-forget・planned entry から「🗓 カレンダーに追加」で接続）を検討。
>   3. **v119-v127 を実機確認**（v120-v127 は **GitHub Pages で今すぐ web 実機可**・v119 は native）: v127=ログ(逆時系列/間隔で時間/一覧→ログ/写真拡大スワイプ/相互動線)／v126=popup日時・一覧見た目・ログ／v125以前=地図から登録・🎲1日・比較スワイプ 等／v119=起動プロンプト・差分・電池。
>   - **状態**: web=GitHub Pages `phase3.80`（v119-v127 反映）／native=**ビルド16 承認・公開待ち**（Codemagic 新ビルド 17-19+ に v111-127 相当が乗っている＝次版候補）。**Gmail 監視継続**。受信確認リマインド＝anohiapp@gmail.com。**📝 Notion: WHY に curation 境界／HOW にカレンダー書き出し方針＋planned entry は pull のみ を記載済(v121/v126)**。
>
> ### 📍 次セッションの再開ポイント（2026-06-30 セッション8 更新・まずここを読む）
>
> **この回でやったこと（初回審査リジェクトに対応＝v110。指摘2点を修正、再提出の一歩手前まで）**:
> - セッション開始の挨拶 → DOCMAP/TODO + Gmail 確認。**🟡 審査結果＝リジェクト（Changes needed）**（2026-06-29 受領・Apple メール2通）。レビュー環境 iPhone 17 Pro Max・1.0(15)・Jun 29・Submission ID `c4451f04…`。**朗報＝最も警戒した 4.2（web ラッパー薄）は出ず**・機能/プライバシー/位置/クラッシュ指摘ゼロ＝native 要素が効いて実質アプリと認定と読める。
> - **指摘① Guideline 2.3.8（名前不一致）**: ストア名「あの日 — 写真と足跡」⇄ デバイス名「Madeleine」。→ ✅ [Info.plist](ios/App/App/Info.plist) の `CFBundleDisplayName` を Madeleine→**「あの日 — 写真と足跡」**に（ストア名と完全一致）。**バイナリ変更＝要再ビルド 1.0(16)**。
> - **指摘② Guideline 1.5（Support URL）**: ASC の Support URL がアプリ本体 URL でサポートページでない。→ ✅ **[support.html](support.html) 新設**（使い方/FAQ・privacy.html と同トーン・ja/en 切替）。公開 URL=`https://yutsutke.github.io/photo-memory-spike/support.html`。連絡先メールは個人 gmail 直公開を避け **support.html・privacy.html とも `anohiapp@gmail.com` に統一**（要受信確認）。**メタデータのみ＝再ビルド不要**。
> - **英語名＝A Past Day — Photos and Footprints に決定（2026-06-30）**: 日本語「あの日 — 写真と足跡」と完全対応。名前はロケール別に出し分け可＝アプリ `InfoPlist.strings`(ja=あの日 — 写真と足跡 / en=A Past Day — Photos and Footprints)＋ASC 英語ローカライズ名 A Past Day — Photos and Footprints。各地域で device⇄store 一致で 2.3.8 OK。旧英名 Madeleine は内部識別子にのみ残置。**英語素材一式が要るので実装は v1.1**（v1 は日本語単独で最短承認）。次：App Store で英語名の空き確認。
> - **✅ 再提出 完了（2026-06-30・Chrome 拡張で ASC 操作）**: ① Codemagic ビルド 1.0(16) 生成（CFBundleDisplayName 修正入り）② ASC で Support URL を `…/support.html` に更新（ユーザー実施）③ ビルドを 16 に差し替え ④ App Review 情報のメモ欄に対応説明（英文）を保存 ⑤「App Review に再提出」→ **ステータス「審査待ち」**（輸出/DSA 追加質問ゼロ）。
>
> **この回の後半＝UI 大改修（v111-118・実機フィードバックで連続改善・🎉実機で全部「めっちゃサクサク」YES）**:
> - **v111-113**: トップを「3枚縦リスト→1枚大表示＋横スワイプ(scroll-snap・ランダム順・タップで連想ウォーク)」化／🎲もう一度引き廃止→スワイプ説明／写真に「🗺この日の地図へ」／連想ウォークの戻りを「← トップに戻る」／**ヘッダ整理＝🗺過去の今日を主役・残り(取り込み/AI/復元/削除/情報)を⚙️ドロップダウンへ**。実機「完璧」「すっきり」。
> - **v114**: 地図の足取りの線を見やすく（暗い縁取り casing＋太め・`drawTrips` の `pushCased`）。
> - **v115→117→118（足跡・タイムラインの拡大スワイプ）**: `showFullImage` を拡張。v115=list 対応(JS スワイプ)→ v117=overlay 再利用 paint→ **v118 で「トップと同じ CSS scroll-snap 横スクロール deck」に作り直して本物のサクサクに**（img=thumbUrl キャッシュ／`.full-deck{position:absolute;inset:0}`）。足跡の拡大に「🚶ここから歩く」で辿り直しも両立。
> - **v116**: 連想ウォークの「近く6枚グリッド」も 1枚大表示＋横スワイプ(deck/makeBigCard 流用・タップで歩く維持)。
> - **🎉 教訓**＝サクサクの正体は CSS scroll-snap（ネイティブ横スクロール）、JS のスワイプ判定は本質的に引っかかる（[[ui-swipe-scroll-snap-works]]）。**これら UI は審査中の 1.0(16) とは別トラック＝承認後の次版（1.0.1/1.1）に乗る**（審査中ビルドは差し替えない）。
>
> - **▶ 次セッションはここから（2つの待ち＋次の一手）**:
>   1. **審査結果待ち（2回目）**＝最大48h・完了でメール。**セッション開始時に Gmail で結果確認**（[[session-start-gmail-check]]）。①承認→ASC バージョンページで「公開」を押すと App Store に出る（手動・v109 設定済）②再リジェクト→理由を読んで個別対応。
>   2. **承認後 v1.1**: 英語名 A Past Day — Photos and Footprints 実装（InfoPlist.strings ja/en＋ASC 英語ローカライズ・App Store で空き確認）／iPad 対応／位置ロガー復活／広告（[[monetization-v1-adfree]]）。**UI 改修(v111-118)も承認後の次版に乗る**。
>   3. **📝 次段の機能＝起動時の自動差分取り込み（native）**＝「全部取り込み→以後アプリを開くたびに差分」を自動化（ユーザーの想定利用・取り込み操作がほぼ不要に）。UI が一段落したのでこれが有力。
>   - **状態**: web=GitHub Pages `phase3.71`（UI 改修すべて反映・support.html 公開・連絡先 anohiapp@gmail.com）／native=ビルド16 が審査中。**Gmail 監視継続**（[[session-start-gmail-check]]）。受信確認リマインド＝anohiapp@gmail.com。

> ### 📍 次セッションの再開ポイント（2026-06-28 セッション7 更新・まずここを読む）
>
> **この回でやったこと（案B＝App Store 提出フローを Chrome でほぼ最後まで実行）**:
> - 前セッションで ASC のメタデータ（スクショ5枚=6.5"スロット・サブタイトル/プロモ/説明/キーワード/サポートURL/著作権・カテゴリ写真/ビデオ・年齢4+・ビルド14・審査連絡先）を全入力済。本セッションは**提出フローの実行**。
> - ✅ **App プライバシー「公開」実行**＝「データの収集なし（Data Not Collected）」ラベルを公開済みに（提出の前提）。
> - 「審査用に追加」を押す→**3ブロッカー**判明: ①13インチ iPad スクショ必要 ②コンテンツ配信権 未設定 ③価格帯 未選択。
> - ✅ **②解消**＝コンテンツ配信権「はい（必要な権利を保有）」（地図の第三者タイル=CARTO/OSM/地理院を attribution 付きで表示するため）→保存済。
> - ✅ **③解消**＝価格「無料」（基準 US $0.00／全174地域 $0.00 自動算出）→確認済。
> - ✅ **①の方針決定（AskUserQuestion）＝iPhone 専用にする**（[project.pbxproj](ios/App/App.xcodeproj/project.pbxproj) の `TARGETED_DEVICE_FAMILY` を Debug/Release とも `"1,2"`→`"1"`）。理由＝コアは電話前提・iPad 未設計レイアウトを審査/ユーザーに見せるリスク回避。BUILD も v109 に。詳細 CHANGELOG v109。
> - 🎉 **提出完了**: push → Codemagic が iPhone 専用ビルド **15** 生成（ユーザー「終了」確認）→ ASC でビルドを 14→15 に差し替え保存 → 「審査用に追加」で3ブロッカー全消（iPad はビルド 15 で消滅）→「提出物の下書き」で 1.0(15) 提出準備完了 →（最終ボタン直前でユーザー確認＝「提出する」）→「審査へ提出」→ **輸出/DSA 追加質問ゼロで送信成功 → ステータス「審査待ち」**。
> - **▶ 次セッションはここから**:
>   - **A＝審査結果待ち**（最大48h・完了でメール）。**セッション開始時に Gmail で App Review の結果を確認**（[[session-start-gmail-check]]）。①承認→**リリースは手動設定済**＝ASC のバージョンページで「公開（リリース）」を押すと App Store に出る（押すまで非公開・タイミングは任意）②リジェクト→ガイドライン理由を読んで対応（薄い web ラッパー＝4.2 が出たら native 要素を増やす等）。審査中の新ビルド差し替えは「このバージョンを審査から削除」が要る点に注意。
>   - **B＝承認後 v1.1**: iPad 対応に戻す（`"1"`→`"1,2"`＋iPad スクショ・要 iPad レイアウト確認）／位置ロガー復活（① `NATIVE_LOCATION=true` ② package.json に background-location を戻して npm install ③ Info.plist の NSLocation*/UIBackgroundModes）／広告 AdMob npa=1＋¥300 除去（[[monetization-v1-adfree]]）。まとめて1版で出すのが効率的。
>   - **状態**: web=GitHub Pages `phase3.61`／native=ビルド15（iPhone専用）が審査中。ASBP 承認は有効化済（2026-06-25）。

> ### 📍 次セッションの再開ポイント（2026-06-27 セッション6 更新・まずここを読む）
>
> **この回でやったこと（案B 提出準備を大きく前進＝v104–v107。位置 v1 完全オフ＋Privacy Manifest＋アイコン。1.0(11) で警告ゼロを実機確認）**:
> - セッション開始の挨拶 → DOCMAP/TODO + Gmail 確認。**Gmail 要対応の新着なし**: ASBP は「申請受領（6/26）」止まりで**承認メール未着＝承認待ち継続**／TestFlight 1.0(9)（6/27）が最新（既知・1.0(4-9) は CI 連続ビルドのルーチン）／App Review からの連絡・却下なし。
> - **位置スコープを AskUserQuestion で確認 → ユーザー選択＝「完全に外す」**（v1 は前面ロガー含め位置ゼロ・プライバシーラベルも位置なし）。
> - ✅ **v104 実装（最も復元しやすい形）**: 単一フラグ **`NATIVE_LOCATION = false`** ＋ 派生 `LOCATION_AVAILABLE = !IS_NATIVE || NATIVE_LOCATION` で位置機能を一括スイッチ。承認後の更新で **`true` に戻すだけ**で native のロガー/軌跡が復活。**プラグインは npm uninstall せず repo に残置**（…と当初判断したが **⚠️ v106 で訂正**＝JS で「呼ばない」だけでは Apple 静的スキャンを欺けず ITMS-90683 が出た。下の v106 参照）。
>   - ゲート（全て `LOCATION_AVAILABLE` 分岐・[index.html](index.html)）: `BgLoc` を位置オフ build では null／`startLogger()` 先頭で早期 return（最終チョークポイント）／🛰️ ヘッダボタン非表示＋クリック未配線／boot の前回モード自動再開を停止／地図「⋯表示」の「🛰️ 自分の軌跡」「📍 GPSなし写真を軌跡から配置」トグルを非表示（null ガード追加）。
>   - **[Info.plist](ios/App/App/Info.plist) から位置宣言を削除**＝`NSLocationWhenInUseUsageDescription` / `NSLocationAlwaysAndWhenInUseUsageDescription` / `UIBackgroundModes=location`。写真用途文言と `ITSAppUsesNonExemptEncryption=false` は維持。削除箇所に「承認後に戻す」コメント残置。
>   - **検証**: web 実行 `IS_NATIVE=false/NATIVE_LOCATION=false/LOCATION_AVAILABLE=true/BgLoc=null`・🛰️ 表示維持・boot エラー0・`vm.Script` parse 0エラー・native 位置オフ経路のメニュー描画シミュレーションで軌跡トグル消滅＆null ガード安全。詳細 CHANGELOG v104。
> - ✅ **v105 実装＝Privacy Manifest（提出必須を充足）**: [PrivacyInfo.xcprivacy](ios/App/App/PrivacyInfo.xcprivacy) を App ターゲットに追加し [project.pbxproj](ios/App/App.xcodeproj/project.pbxproj) へ4部位登録（FileRef/BuildFile/App グループ/Resources フェーズ・UUID `AA0104A1…`・`cap sync` は App のリソース一覧を再生成しないので永続）。中身＝**トラッキングなし／収集データ空（Data Not Collected＝ローカル完結で外部送信ゼロ）／required-reason は `UserDefaults`(CA92.1) のみ**（自前2プラグインの Swift を実読で確定＝photo-library は対象 API なし・background-location が UserDefaults 使用。位置オフ build でもバイナリにコンパイルされる＝宣言必須）。検証＝`node`+`plist` で manifest/Info.plist を機械パース（構造一致・位置キー全消去）。pbxproj は再読で4部位整合を目視。詳細 CHANGELOG v105。
> - ✅ **v106 実装＝ITMS-90683 解消（位置 v1 完全オフを徹底）**: 1.0(10)（v104+v105）アップロードで警告 **ITMS-90683「Missing purpose string」**（When-In-Use/Always）。原因＝background-location の `CLLocationManager` がバイナリに残り、Apple 静的スキャンが purpose string を要求。**v104「プラグイン残置」は不十分だったと判明**→ [package.json](package.json) から `background-location` を削除（CI の `cap sync ios` が [Package.swift](ios/App/CapApp-SPM/Package.swift) を photo-library のみで再生成＝委ねられた Package.swift は CLI 管理で package.json から注入する作り）。ソースは `local-plugins/background-location/` に残置（復活用）。`package-lock.json` 再生成（extraneous 除去）。JS は無改修で安全（`NATIVE_LOCATION=false` で BgLoc 三項が短絡）。検証＝node_modules/lock から消失・photo-library 維持・`vm.Script` 0エラー。**真の確認は次ビルド 1.0(11)**。詳細 CHANGELOG v106。
> - ✅ **v107 実装＝アプリアイコン差し替え（案B ストア素材を前進）**: ユーザー作の玉ボケ案（`Desktop/anohi.jpg`・暗い背景＋暖色の玉ボケ＝写真/記憶のあたたかさ）を提供。受領版は「角丸＋影＋背景枠つきプレビュー」だったので **full-bleed 1024 正方形に再構成**（本体 `[140..893]` をクロップ→1024 全面・角丸クリップ175＋ビネット縦グラデで角の背景枠除去・**RGB/アルファ無し**＝Apple 要件）。System.Drawing(.NET) 生成→node で IHDR 検証→ユーザー原寸 OK→[AppIcon-512@2x.png](ios/App/App/Assets.xcassets/AppIcon.appiconset/AppIcon-512@2x.png) 上書き（Contents.json は単一 1024 universal のまま無改修）。詳細 CHANGELOG v107。
> - ✅ **1.0(11) 実機確認＝警告ゼロ**（ユーザー ASC スクショ）: 1.0(10) の黄色⚠️（ITMS-90683/91053）が 1.0(11) で消滅＝**v104 位置オフ＋v105 Privacy Manifest＋v106 位置プラグイン除外が効いた**。native の提出要件はクリーン。
> - 📝 **決定（2026-06-27）**: ① **「足跡」は v1 でも成立** ＝ 位置＋日時のある写真が地図で時系列に線（`tripLayers`）でつながる（ロガーと独立・位置許可不要・コードで確認）。名前「写真と足跡」据え置き。説明文もこの意味で記述。② **収益化＝v1 は広告なし**（「データを収集していません」ラベル維持・クリーン審査）→ **広告は v1.1**（AdMob npa=1＋¥300 で除去＋購入を復元）。配置はコア（ウォーク/全画面/過去の今日/地図）に出さず**外周（入口バナー／区切りのインタースティシャル・頻度上限厳しく）のみ**。memory [[monetization-v1-adfree]]。
> - 📝 **ストア掲載テキスト 日本語ドラフト済**（この会話に記載）: サブタイトル「偶然よみがえる、久しぶりの記憶」／プロモ文／キーワード／説明文（偶然に出会う・久しぶりに戻る・よみがえる・足跡をたどる・端末内だけで）。足跡の節は v1 仕様（写真由来の線）に修正済。微修正のうえ Notion(HOW ストア素材) or ASC へ。
> - ✅ **v108 実装＝版表示の移動＋スクショ用モード**: ①ヘッダの BUILD 版文字列を **ℹ️ 情報モーダル末尾＋`console.log`** に移動（ヘッダすっきり・本番/スクショ向け。CLAUDE.md の運用も更新＝ヘッダ再表示禁止）②**`?shot`** で web を **v1見え（位置 UI 🛰️/地図トグルを非表示）** に＝App Store スクショを提出物（位置オフ）と一致。preview 検証 green（通常 web=位置UI表示／?shot=非表示／ℹ️に版表示）。詳細 CHANGELOG v108。
> - **▶ 次セッションはここから**（案B ストア素材の続き）:
>   - **A＝本命: 残りストア素材** ① **スクショ**＝**web を `…/index.html?shot` で開き**（位置UIオフ＝v1見え）自分の写真を入れて 6.7"/6.9" サイズで撮る（撮る画面案: 連想ウォーク/過去の今日/地図の足跡/全画面）→ こちらで規定サイズに調整。★ユーザー作業（写真が要る）②掲載テキスト＝ドラフト済→微修正で確定 ③ **ASC プライバシーラベル＝「データを収集していません」**（v1 広告なしで確定・manifest と一致）④ 年齢レーティング 4+。← ✅ アイコン(v107)・✅ 版表示すっきり(v108)・✅ プライバシーポリシー URL・✅ カテゴリ/主言語(ja) は済。ASC 操作は Chrome 拡張で一緒に。
>   - **B＝審査 submit**: 上記が揃ったら App Store 審査へ（**次ビルド 1.0(12) に v107 アイコンが乗る**＝アイコン反映を実機/ASC で確認してから submit。外部テストは不要・やるなら Test Information 入力）。
>   - **C＝承認後（v1.1〜）**: 位置を復活（① `NATIVE_LOCATION=true` ② package.json に background-location を戻して npm install ③ Info.plist の NSLocation*/UIBackgroundModes を戻す）でロガーを投入／**広告（AdMob npa=1＋¥300 除去）を設計済みスロットに追加し実ユーザーで配置/頻度を計測**。
>   - **メモ**: 万一 photo-library の `import CoreLocation`（PHAsset.location データ型）で位置警告が再発したら KVC 化で外す（CHANGELOG v106 残課題・1.0(11) では出ていない）。
>   - **状態**: web=GitHub Pages `phase3.59`／native=次 Codemagic ビルド 1.0(12) に v107（アイコン）が乗る（1.0(11)=v104+v105+v106＝警告ゼロ確認済）。**ASBP 承認待ち**（セッション開始時に Gmail 確認＝[[session-start-gmail-check]]）。**外部テスト提出の赤（Test Information 未入力）は内部テスト/審査に無関係＝無視でOK**。

> ### 📍 次セッションの再開ポイント（2026-06-27 セッション5 更新）
>
> **この回でやったこと（地図/ウォーク UI の仕上げ v97-103＋spike撤去＝実機/ユーザー確認済み「すっきり」「完璧」・位置ロガーも実機 YES）**:
> 1. ✅ **地図ツールバーを1種類に統一（v97）**＝モードで変わっていた二重UI（native=◀▶無しの4アイコン／web=6アイコン／全期間=テキストボタン）を解消。**どのモードでも固定2段**＝1段目 `◀ 📆 ▶`（過去/未来の「今日」・📆=今日へ戻る）／2段目 `🎲 偶然 ／ 📅全期間 ／ ⋯表示`。`✕`のみ（旧「✕ 閉じる」）・`ⓘ`は⋯表示メニューへ統合・`↩︎全部を見る`は廃止（📅全期間が吸収）。🎲偶然(核①)と📆今日リセットの置き場所は **AskUserQuestion でユーザー確認**（2段目に残す／◀📆▶中央）。CSS は `.map-ctrlbar{width:max-content}` で2段とも1行（折り返すと pop と重なる罠を回避）。**ブラウザ geometry 検証 green**（5モード全てボタン不消失・pop非重なり mobile/desktop・⋯→ⓘで凡例）。**実機確認待ち**（GitHub Pages / TestFlight）。詳細 CHANGELOG v97。
> 2. ✅ **「地図だけ全画面（没入表示）」を追加・整え（v98→v99）**＝ユーザー要望「地図と動線と写真だけ／タイムラインの出ない全画面／解除アイコン1つ」。**右上の常設 `⛶` トグル**（タップで全画面／もう一度で解除）で、ツールバー/タイムライン/ズーム/✕ を隠し地図+動線+写真だけに（attribution はライセンス上 残す）。desktop は `#mapEl` を 320px→0 に広げ `invalidateSize`。overlay の `.immersive` クラス1枚＝CSS で実装（状態集約）。**v99 で位置交換**＝全画面が高頻度なので ⛶ を右上常設に昇格、set-once な「地図の種類(ダーク/地理院)」は Leaflet layers コントロールを撤去して ⋯表示メニューへ（chip・localStorage 保存）。ブラウザ検証 green（トグルON/OFF・各 chrome 開閉・地図種類 chip 切替・desktop で ⛶ がサイドバー非重なり/没入 right:10）。詳細 CHANGELOG v98-99。
> 3. ✅ **連想ウォーク中央の写真をタップで全画面（v100）**＝実機要望「中央（いま見ている写真）は長押しせずタップで拡大」。当初「タップ=全画面に全面変更」だったが AskUserQuestion で現状の表に落とし、**変えるのは中央写真のタップだけ**に確定（初期3枚=タップでウォーク開始／近くの6枚=タップで歩く、は不変＝核のウォーク操作は触らない）。中央カード `attachPhotoGestures` の onTap=null→`showFullImage` の1点。ブラウザ検証 green（テスト写真注入→中央タップで full-overlay 表示・地図ボタンは誤爆なし）。詳細 CHANGELOG v100。
> 4. ✅ **spike 撤去（v101）**＝🧪「写真スパイク」診断パネル（`#spk-*`・写真全件アクセス A/B 実機計測の自己診断・v92-93）を本体から削除。B は v94 で本体統合済み＝役目終了。block 全体（HTMLコメント＋`<style>`＋`<script>` IIFE・約227行）を末尾から除去、main app の `</script>` が直接 `</body>` に。ブラウザ検証 green（#spk-fab/#spk-overlay 消滅・boot 正常・コンソールエラー0・inline script 2→1）。**✅ `@capacitor-community/media`（A テスト専用・未使用化）を v102 で削除**（deps は @capacitor/core・ios＋自前 photo-library・background-location の4つに・cap sync は CI）。
> 5. ✅ **ウォークは毎回上端へスクロール（v102→v103・不具合修正）**＝実機「初期3枚タップ→ウォークに来ると6枚グリッドにスクロールされている／入場時は画面トップを見せて」。原因＝スクロール制御コードが無く `render()` が #main を入れ替えても window が旧スクロール位置を保持。修正＝`showExplore` で `enteringWalk=(state!=='explore')` を取り render() 後 `if(enteringWalk) window.scrollTo(0,0)`＝**毎回上端へ（入場・近くの6枚タップ・足跡タップすべて）＝歩いた先の中央写真を先頭に**（v102 は入場のみ→v103 で再中心化も統一＝ユーザー要望「6枚から選んだ後の遷移も入場と同じに」）。ブラウザ検証 green（spy: 入場・再中心化とも [0,0]）。詳細 CHANGELOG v102-103。
> 6. 📝 **App Store 提出は案B＝コア先行→後で Always 追加（決定・記録のみ／実行はあと）**。初回審査は位置を外して写真コアを先に通し、Always 背景ロガー（v96 実装済）は実績後のアップデートで追加。フォールバックは `if(BgLoc)`/track 隔離で綺麗。「常に許可で通る前例」＝Arc/OwnTracks（端末内ライフログ）等。memory [[location-logger]] に記録。
> - **▶ 次セッションはここから**（このセッション＝地図/ウォーク UI 仕上げ v97-103＋spike撤去＋ロガー実機 YES が全部 done・push 済み）:
>   - **A＝本命: App Store 提出準備（案B・コア先行）** ① v1 は位置を見せない方針＝Info.plist の Always 用途文言／`UIBackgroundModes=location`（必要なら background-location プラグイン自体）をどこまで外すか判断（ロガー実装は `if(BgLoc)`/track 隔離なので宣言だけ落とせる・後追いアップデートで復活）② ストア素材（アイコン/スクショ/説明文/プライバシーポリシー）③ Privacy Manifest（PrivacyInfo.xcprivacy）
>   - **B＝任意の検証**: 位置ロガー 完全終了(force-quit)→SLC 復帰（残り1点・背景動作=4分は確認済）／こまめ(25m)モードの密度・電池
>   - **C＝その先**: 収益化（AdMob/IAP）・起動時 自動差分同期・原寸 fullImage（512px で実機満足のため保留中）
>   - **状態**: web=GitHub Pages `phase3.55`／native=次 TestFlight ビルドに今回分（v97-103＋spike撤去＋@capacitor-community/media 削除＋ウォーク入場スクロール）がまとまって乗る。直近 TestFlight は 1.0(9)=Jun 27。


> ### 📍 次セッションの再開ポイント（2026-06-26 セッション4 更新・まずここを読む）
>
> **🎉 Phase 1 の本丸＝B（自前 PhotoLibrary）をアプリ本体に統合。ネイティブで「全ライブラリ取り込み」→ 既存の連想ウォーク/地図/タイムライン/On This Day/色・意味の近傍が全ライブラリでそのまま動く配線が入った（BUILD `phase3.46`・push 済み）。**
> - **やったこと（案1＝既存パイプラインに流し込む・軽量版）**: enumerate で全件メタ→`thumbnail({id,size:512})` のサムネ Blob をそのまま thumb に保存（再エンコード無し）＋色は 16×16 で抽出→**web と同一形の record を dbPut**＝downstream 無改修。写真キーは UUID 維持・**assetId(localIdentifier) を dedup `asset|<id>` と差分同期に使用**（機種変更耐性）。再実行は**差分（新しく撮った写真）だけ追加**。導線は空状態「📚 ライブラリ全体から始める」＋取り込みメニュー「📚 ライブラリ全体（おすすめ）」、**全て `isNativePlatform()` 分岐＝web は従来どおり**。fast-track→背景の二段は web と同じ。新規コード＝`importNativeLibrary`/`importNativeAsset`/`importOneNative`/`processNativeRest`/`collectImportedAssetIds`/`colorFromBlob`/`dataUrlToBlob`（index.html・importOne 直後に集約）。
> - **検証済み（実機前にブラウザで固めた）**: ①全 inline script を `vm.Script` で parse＝構文事故ゼロ ②web は IS_NATIVE=false でネイティブ UI を隠し従来描画・コンソールエラー無し ③**記録生成パイプライン本体を page context で関数テスト**＝ダミー JPEG→record が web と同一形（id/assetId/`dedup='asset|…'`/datetime=Date/thumb=Blob/color=F32(48)/dateSource）・色抽出が実色と一致・差分集合回収・dedup→duplicate・no-datetime→skip。
> - **✅ 実機 TestFlight で本物の PhotoLibrary を通した＝🎉強い YES**（ユーザー:「完璧だね。すごい速さで写真を取り込んだ。しかも止まらなかった。2000枚もあったのに」）。web/iPad の raw JPEG デコード OOM（v35-37）が native では起きない＝案1軽量版の狙いどおり。**ボトルネック①（写真全件）完全クローズ**。
> - **🎉 v95＝全ライブラリでの reminiscence も実機で全項目 強い YES**: On This Day「あのときから何年…同じ年数の未来の自分はどこに」（過去↔未来）／連想ウォーク「あーなつかしい、あれどこだっけ、そうそう」（偶然×久しぶり）／地図「そんな道歩いたな、**写真に無い記憶も思い出す**」（軌跡が記憶の鍵）／**タイムラインから位置を直す行為自体が想起**（[[editing-triggers-reminiscence]] 再確認）／拡大512px「めっちゃきれい」（原寸 fullImage は保留）／差分同期「完璧」。**+ 拡大表示の旧 iOS 回避策「📷写真アプリで開く」「日付コピー」を撤去**（native でアプリ内完結・preview 確認済）。
> - **🛰️ v96＝位置ロガーをネイティブ背景記録に（アプリを閉じても記録）**: 自前 `BackgroundLocation` プラグイン。重要な移動=SLC（低電池・完全終了でも iOS が背景再起動して配送）／こまめ=背景更新（高精度・電池使う）。点はネイティブが UserDefaults にバッファ→JS が起動/前面復帰/8秒で drain して track へ合流（既存の地図軌跡・GPSなし補完は無改修）。Info.plist に Always 文言＋`UIBackgroundModes=location`。web は前面のみのまま（全分岐 `if(BgLoc)`）。**cap sync で SPM 名一致確認済・web preview 無傷。🎉実機 YES（2026-06-27・重要のみ/SLC）＝1日歩行で地図に軌跡・電池ほぼ無コスト（Madeleine 背景4分/5%・夕方66%残）・15点/日＝SLC配送と電池を実機確認**。残＝完全終了(force-quit)からの復帰・こまめ計測・Always 許可フロー（案B で初回提出は位置オフ）。詳細 CHANGELOG v96・memory [[location-logger]]。
> - **次段**: ストア素材（アイコン/スクショ/説明文）or 収益化（AdMob/IAP）or 起動時 自動差分同期。拡大=原寸 fullImage は保留（512 で実機満足）。提出前 Privacy Manifest（PrivacyInfo.xcprivacy）。診断 block（#spk-*）撤去は ✅v101 完了（`@capacitor-community/media` も ✅v102 で削除）。詳細 CHANGELOG v94-v96・memory [[native-photo-access-works]] [[location-logger]]。
> - **セッション開始時**: Gmail で Apple/App Store 関連を確認（[[session-start-gmail-check]]）。下は前セッション（セッション3＝v93）の記録。
>
> ### 📍 次セッションの再開ポイント（2026-06-26 セッション3 更新）
>
> **🎉 初 TestFlight ビルドが実機 iPhone で起動。Mac なし署名（ボトルネック②）を de-risk 完了。**
> - **この回（セッション3）でやったこと**:
>   1. ✅ **v90 の archive 失敗（exit 65）を解消**＝真因は use-profiles ではなく **`fetch-signing-files --create` に証明書の秘密鍵が無かった**こと。ローカルで RSA 鍵生成→Codemagic の **Secure 環境変数 `CERTIFICATE_PRIVATE_KEY`（グループ `signing`）** に登録＋`codemagic.yaml` に `--certificate-key @env:CERTIFICATE_PRIVATE_KEY` 追加（commit `21d81e3`）。
>   2. ✅ **再ビルド成功**＝署名 3s / IPA 45s / App.ipa 1.40MB / App Store Connect アップロード＆Apple 処理完了。
>   3. ✅ **暗号化コンプライアンス回答**（標準暗号=HTTPS のみ→免除・仏配信いいえ）→ ビルド 1.0(3)「提出準備完了」。
>   4. ✅ **内部テストグループ「自分」に追加→iPhone の TestFlight でインストール・起動成功**。外部テスト submit だけ Test Information 未入力で赤（内部テストはレビュー不要なので無関係）。
> - **🟢 強い signal**: 実機で **200枚をピック→アプリ画面復帰まで約17秒**＝web/Safari より明確に速い（WKWebView）。※まだ web の `<input>` ピッカー経由＝native の「全ライブラリ一括」は未着手。
> - **✅ 写真全件アクセス スパイク（v92・Approach A）＝実機で強い YES**＝「久しぶり=全ライブラリ」の生命線 de-risk 完了。`@capacitor-community/media` の getMedias を実機 iPhone で叩いた結果:
>   - **2000枚を約6秒**取得・**日時 2000/2000**・GPS 215/2000・**範囲 2021/8〜2026/6（約5年分）**＝古い写真まで届く＝核「久しぶり」実証。300枚が9秒だったのは許可ダイアログ待ち込みで、2回目以降が真の速度＝十分速い。サムネ OS から描画 OK。プラグインは初回ビルドで一発配線（`platform: ios / Media: 検出 ✅`）。
>   - **GPS は直近リッチ・古いほど希薄**（直近71%／全体11%）→ 古い写真は日時軸（100%）が普遍の頼り（On This Day・GPSなし写真の軌跡補完 v74-75 と整合）。
>   - **ボトルネック①（写真全件）＋②（Mac なし署名・v91）が両方消えた＝native の二大リスク解消**。詳細 CHANGELOG v92・memory [[native-photo-access-works]]。
> - **✅ 本命 B（自前 PhotoLibrary プラグイン）の第一歩＝実機で YES（v93）**＝Mac なしで初の自前ネイティブコードがビルド・実機動作。`local-plugins/photo-library`（`file:`→cap sync が SPM 配線）／API＝`requestAccess`/`enumerate`/`thumbnail`。実機結果:
>   - **③ 全件数 2054 枚を即時（往復180ms / native158ms）**＋メタ2000・日時2000/2000・GPS215/2000・範囲2021/8〜2026/6。**④ オンデマンドサムネ 48枚＝7ms/枚**。
>   - **A 比＝約33倍速＋メモリ軽**（A は全サムネ base64 一括で約6秒／B は列挙とサムネを分離）。本命アーキの優位を実機確認。
>   - **first-build の罠2つを解消**: SPM 名不一致（exit 74）→package/product/target 名を `PhotoLibrary` に統一／Swift import 漏れ→`UIKit`・`CoreLocation` 追加。教訓は CHANGELOG v93・memory [[native-photo-access-works]]。
> - **🔴 次の本丸＝B をアプリ本体の取り込み動線に統合（Phase 1）**。下に**一気にやる用の実装プラン**を用意済み（2026-06-26・Explore でコードをマップして作成）。次セッションはこれを読めば探索ゼロで着手できる。
>
> ### 🛠️ 次セッション実装プラン — B をアプリ本体に統合（一気にやる用）
> **ゴール**: ネイティブで「全ライブラリ取り込み」→ 既存の連想ウォーク/地図/タイムライン/On This Day が**そのまま全ライブラリで動く**。web のピッカーは残す（両対応・`Capacitor.isNativePlatform()` で分岐）。
>
> **★ 唯一の設計判断（最初に決める）＝サムネをどう持つか**:
> - **(推奨) 案1: 既存パイプラインに流し込む**＝ネイティブ enumerate で全件メタ→各写真の512pxサムネを `PhotoLibrary.thumbnail({id,size:512})` で取得し、**既存と同一形の record（thumb=Blob）を `dbPut`**。downstream（表示/色/CLIP/地図/walk）は**完全に無改修**。storage は512サムネを IndexedDB に持つ＝[[storage-tradeoffs-accepted]] で受容済み。低リスク・最速。ユーザーは2054枚＝余裕。
> - 案2: 参照＋オンデマンド（メタのみ保存・`thumbUrl` を非同期化）＝超省メモリ・数万枚向きだが、`thumbUrl()`(同期・L1344)と全表示経路の大改修。**スケールが問題化してから**。
> - → **案1で実装**。enumerate の「即時全件」は取り込み進捗UI/差分同期に活かす（B の利点は死なない）。
>
> **写真 record の形（再導出不要・index.html L757-768）**: `{id:crypto.randomUUID(), name, datetime:Date, lat, lng, blob:null, thumb:Blob(512px), color:Float32Array(48), dedup:string, dateSource, importedAt, ＋assetId(新規=localIdentifier)}`。runtime: excluded(永続)/_sealed/embedding。DB=`photos`(keyPath id・index datetime/dedup)＋`track`(v2)。
>
> **統合の継ぎ目（関数:行）**:
> - 取り込み入口 `$picker.change`(L3970)→`importFiles()`(L3480) … ここにネイティブ分岐を足す
> - File→record `importOne()`(L718) … ネイティブは EXIF/HEIC 不要（enumerate が日時/GPS・thumbnail がサムネ）→ **別関数 `importNativeAsset(meta, thumbBlob)` を新設**して record 直組み
> - 表示 `thumbUrl()`(L1344)=`URL.createObjectURL(photo.thumb)` … **案1なら無改修**
> - 色 `createThumbnail()`(L653)内 `extractColorFeature()`(L599) … 512サムネを canvas 描画して color 抽出
> - CLIP `extractEmbeddingForPhoto()`(L3691)=`p.thumb` 読む … **無改修**
> - dedup `dbHasDedup()` … ネイティブは **assetId をキー**に（再取り込みで重複しない）
>
> **実装ステップ（小さく・各段で実機確認できる順）**:
> 1. `importNativeAsset(meta, thumbBlob)` 新設: id=UUID, datetime=`new Date(meta.date)`, lat/lng, thumb=(512 dataURL→`await (await fetch(d)).blob()`), color=512から抽出, dedup=assetId, assetId=meta.id → `dbPut`+`addPhotos`
> 2. 「📚 全ライブラリを取り込む」導線（ネイティブ時の空状態/onboarding）: 押下→`requestAccess()`→`enumerate({limit:0})`→DB既存 assetId と diff→新規だけ背景で `thumbnail({id,size:512})`→`importNativeAsset`。進捗は既存の📥`updateBgStatus` に乗せる
> 3. 背景取り込みは既存 `runBackgroundImport()`/`enqueueBackground()`(L3528) の枠＋スロットル流用
> 4. 色/CLIP backfill はそのまま動く（record.thumb があるため）
> 5. 差分同期: 起動 or ボタンで enumerate→未取り込み assetId だけ追加（新しく撮った写真が入る）
> 6. 拡大=原寸: プラグインに `fullImage({id})` を足してオンデマンド（512即出し→裏で原寸差替え）※第一歩は enumerate/thumbnail で足りる
> 7. ✅ 診断 block（#spk-*）を撤去（v101・B 統合済みで役目終了）
>
> **テスト**: build→TestFlight→「全ライブラリ取り込み」→数百枚入ったら 連想ウォーク/地図/On This Day が全ライブラリで動くか・色/意味の近傍・拡大画質を実機確認。
> **注意/宿題**: ①写真キーは UUID 維持（assetId は属性・機種変更耐性 §⑥）②提出前に Photos の Privacy Manifest（PrivacyInfo.xcprivacy・required-reason API）を plugin/app に追加（TestFlight は警告止まり）③onboarding は「数枚で試す(既存picker)／全ライブラリ」の段階導線（核＝偶然/久しぶり/よみがえる・[[ui-minimalism-works]]）。詳細マップは会話の Explore 結果・memory [[native-photo-access-works]]。
> - **任意の後始末**: `Info.plist` に `ITSAppUsesNonExemptEncryption=false`（暗号化質問を恒久スキップ）／外部テストするなら Test Information 入力。
> - **セッション開始時**: Gmail で Apple/App Store 関連を確認（[[session-start-gmail-check]]）。詳細は CHANGELOG v91。下は前セッション（セッション2＝v90）の記録。
>
> ### 📍 次セッションの再開ポイント（2026-06-26 セッション2 更新・archive 失敗→v91 で解決済み）
>
> **🚦 いまの最前線＝Codemagic で初ビルドを TestFlight へ。署名は通過、archive で「provisioning profile が無い」で2回目失敗中。**
> - **この回（セッション2）でやったこと（Chrome 拡張で一緒に操作）**:
>   1. ✅ **Apple Developer で App ID 登録**: `io.github.yutsutke.madeleine`（Explicit・Team `25TM5C27YT`）。
>   2. ✅ **App Store Connect でアプリレコード作成**: 名前 **「あの日 — 写真と足跡」**（「あの日」単独は商標で使用済み→改名・公開前に再変更可）／primary lang 日本語／SKU `madeleine-001`／iOS 1.0 提出準備中。
>   3. ✅ **App Store Connect API キー発行**（Users and Access→Integrations）: 名前 `Codemagic CI`・権限 **App Manager**。**Issuer ID `cc160ccb-f80f-4f15-acdd-3d6b6b333c96`／Key ID `FNMWA45D94`／.p8 は DL済み（1回限り・ユーザー保管）**。
>   4. ✅ **Codemagic 接続**: Personal Account（無料 Individual）・GitHub OAuth（All repositories）でサインアップ→リポ `photo-memory-spike` 追加（Codemagic app id `6a3e20ec4f57d6c27a45f181`）→ **Developer Portal integration を「MadeleineASC」で登録**（Key `FNMWA45D94`・yaml の `integrations.app_store_connect` と一致）。
>   5. 🔁 **ビルド実行2回**: ①commit `48e6945` → 署名で即失敗「No matching profiles found」→ **v90 で `app-store-connect fetch-signing-files --create` に切替**（commit `fe1a572`）→ ②再実行で**署名ファイル作成は通過**したが **Step 6「IPA をビルド」で失敗**＝`error: "App" requires a provisioning profile ... (target 'App')`／archive 行に `CODE_SIGN_STYLE=Manual`／exit 65。
> - **🔴 次セッションの最優先＝この archive 失敗を直す**。署名ファイル(--create)は出来たのに `xcode-project use-profiles` がプロファイルを App ターゲットに適用できていない（Manual なのに PROVISIONING_PROFILE_SPECIFIER / DEVELOPMENT_TEAM 未設定）。
>   - **まず: 失敗ビルドの「署名ファイルを取得/作成し Xcode に適用」ステップのログを読む**（fetch-signing-files が profile を何個・どこに保存し、use-profiles が何個適用したか）。Codemagic build URL = `codemagic.io/app/6a3e20ec4f57d6c27a45f181`。
>   - **試す修正候補（順に）**: ①`fetch-signing-files` に `--platform IOS` も付ける ②`use-profiles` の直後で同一スクリプト内に `build-ipa` を置く（今は別ステップ）③ build-ipa に team を明示：プロジェクトに `DEVELOPMENT_TEAM = 25TM5C27YT` を入れる or `xcode-project use-profiles --warn-only` で適用結果を確認 ④最終手段＝Codemagic UI の「Code signing identities」に手動で配布証明書＋プロファイルをアップロードする方式へ切替。
>   - 参考: Codemagic「Building a native iOS app / Signing」「fetch-signing-files」「use-profiles」の各 docs。Capacitor 8 は SPM（Podfile 無し）。
> - **🔴 これと完全独立で進められる de-risk＝ネイティブ写真全件アクセスの最小スパイク**（`@capacitor-community/media` 等で PHAsset 全件列挙＋OSサムネ/EXIF が取れるか調査→設計メモ。「久しぶり=全ライブラリ」の生命線）。署名で詰まったらこちらを先に進めてもよい。
> - **セッション開始時**: Gmail で「ASBP 承認メール」を確認（[[session-start-gmail-check]]・前回は受領止まりで承認待ち）。
> - 詳細は CHANGELOG v90。下は前セッション（v79–v89）の記録。
>
> ### 📍 次セッションの再開ポイント（2026-06-26 セッション1 更新）
> **🧭 最短サマリ（次に開いたらまずこれ）**: web 強化（v87/v88）＋ **Apple の課金土台を一気に整えた**回（**v79–v88 / BUILD `phase3.38` / 全て push 済み・git clean**）。web の軸＝「過去の今日（On This Day）」習慣の入口育成（v87=📆を◀▶で前後の日にずらす＝過去の昨日・明日・明後日…／v88=タイムライン下端ハンドルを safe-area 分持ち上げ＋ⓘアイコン説明）。**実機で強い YES**＝v83「線が無くてもタイムラインに数年分出るだけで時間の経過を感じ心が動く」（[[on-this-day-daily-entry]]）。**🍎 Apple＝メンバーシップ有効化（2026-06-25）＋ App Store Connect 初期設定を 2026-06-26 に一気に完了**（有料アプリ契約署名・W-8BEN 租税条約0%・銀行口座・**ASBP 申請送信＝承認待ち**・Team ID `25TM5C27YT`）。**唯一のブロッカーは解消済み・native は前進フェーズ（下の「▶ 次の一手 (3)〜(5)」）**。
> **▶ 次の3択（どれでもOK・前提情報は全部このすぐ下にある）**:
> - 〔A〕**実機（iPhone Safari）で v79–v88 をまとめて触る** ＝唯一の未確認。観察: **v88=①タイムライン下端ハンドルが safe-area 分持ち上がって掴める位置に出るか（v87 実機で「消えた・下すぎて触れない」指摘の対処・`viewport-fit=cover`）②ⓘで開くアイコン凡例が役立つか**／過去の今日チップ／🗺今日（数年分の重なり・写真が少ない日の寂しさ）／📆◀▶で前後の日にずらす＝過去の昨日/明日/明後日（矢印の発見しやすさ・相対ラベル・予定立て/振り返り・写真ゼロの日「0年分」）／位置なし写真→その日の地図→タイムラインから位置を直す＝想起（[[editing-triggers-reminiscence]]）。`?v=338` でキャッシュ回避。**ハンドルがまだ掴みにくければ「📆入口でシートを少し開いて出す」案あり**（CHANGELOG v88 残課題）。
> - 〔B〕**web 機能の続き** ＝下の「将来案」や新しい思いつき（毎日の習慣まわりが当たっているので、その周辺＝今日±数日／偶然通知との接続／「N年前の今日」見出し 等が有力）。
> - 〔C〕**native / Apple** ＝**✅ メンバーシップ有効化＋App Store Connect 初期設定完了（2026-06-26）・ここが本命の前進先**。済＝(1) サインイン確認・(2) **ASBP 申請送信（承認待ち）**＋有料アプリ契約署名・税務(W-8BEN 0%)・銀行口座・**✅(4) リポ側パイプライン足場（v89＝cap add ios・Info.plist・スキーム・codemagic.yaml）**。**次セッション開始時に Gmail で「ASBP 承認メール」を確認**（[[session-start-gmail-check]]）。残り → 🔵(3) **Codemagic 接続**（App Store Connect API キー発行 → Codemagic に integration 登録＋リポ接続＋App レコード作成）→ (5) 初ビルド実行 → TestFlight。**ガイドは下の v89 残課題＆この回の会話に詳細**。🔴 並行で最優先 de-risk＝**ネイティブ写真全件アクセスの最小スパイク**（核「久しぶり=全ライブラリ」の生命線・ASBP 承認と無関係に進められる）。⚠️ EU 配信＝DSA トレーダー申告が別途（自宅住所公開回避＝私書箱 or 当面 EU 除外・配信設定時に判断）。
>
> **2026-06-19 やったこと**: 🆕 **v79→v80→v81 = 写真ページ（連想ウォークの中心カード／フル画像）から、位置情報が無くても「🗺 この日の地図へ」で日時からその日の地図に飛べる**（GPS ありは従来「🗺 地図でこの場所を見る」）。v79 は「同じ日に GPS 写真がある時だけ」出す仕様だったが、実機でユーザーが「見つからない」（対象の日が GPS 皆無だった）→ **v80 でゲート撤去・日時さえあれば常に出す**（GPS 皆無の日でも飛べる・飛び先はその日のタイムライン索引・エラーなし）。日時不明の写真のみ非表示。**v81 = no-GPS ボタン直下に注意書き**（飛び先に軌跡が無い時の期待値合わせ・GPS ありには出さない）→ **v82 = その注意書きを「次の一手＝想起のきっかけ」まで拡張**「位置のある写真が無い日でも、その日の写真一覧（タイムライン）が開きます。並びを眺めたり、写真を選んで位置を直すのも想起のきっかけに。」（タイムライン確認→位置を直す＝[[editing-triggers-reminiscence]] へ誘導）。
> **v83 = ヘッダ 🗺 ＝「今日（On This Day・数年分の同じ月日を重ねる）」を毎日の習慣入口に**（例: 今日 6/19 → 2026/6/19・2025/6/19・2023/6/19… 写真がある年だけ昔→今で重ねる＝核②久しぶり×習慣）。既定を `today` に変更し、従来の偶然ランダム3日（核①偶然）は**地図内の🎲に退避**（コントロール＝🎲偶然の3日 / 📆今日 / ↩︎全部・地図を閉じず往復可）。今日の写真ゼロは偶然3日→ブラウズに二段フォールバック。**実機で強い YES**「線が無くてもタイムラインに数年分出るだけで時間の経過を感じ心が動く」（[[on-this-day-daily-entry]]）。
> **v84 = トップ画面に「過去の今日」フィルタチップ**（全期間→過去の今日→この時季→…）。`pickPool('today')`＝今日とちょうど同じ月日（年問わず・地図 🗺 today と同条件）。チップから引く／🗺 で重ねて地図化、の対。preview E2E green・**チップから引く実機手触りは未確認**。
> **v85 = 地図入口の 🎲偶然3日 / 📆今日 / ↩︎全部 を「アイコンのみ」に簡素化**（情報過多→[[ui-minimalism-works]]。説明は title と、タイムライン上部の見出し「📆今日 M/D（N年分）」に一本化）。
> **v86 = 地図のベースタイル選択を記憶**（前回選んだ地図で開く・`localStorage['pms-mapLayer']`・`baselayerchange` で保存／未保存は既定ダーク）。毎日の入口の習慣摩擦を削減。preview E2E green。詳細 CHANGELOG v79–v86・memory [[on-this-day-daily-entry]]。
> **2026-06-18 やったこと**: ① Apple Developer **登録を申込・支払い済み**（既存 Apple ID 使用・注文 W1884878174・12,980円・**有効化待ち**）② アプリ名を **あの日（🇯🇵）／ Madeleine（🇬🇧 ブランド据え置き）** に改名（web `index.html`・Notion WHY/HOW・DOCMAP・memory 全整合／英語タグライン「A Certain Day: Revived through Photographs and Footprints」確定＝v76）③ **CDN vendoring**（exifr/heic2any/fflate/Leaflet→`vendor/`＝v77）④ **プライバシーポリシー 日英**（`privacy.html`・連絡先 Tanaka Yusuke / yutsutke@gmail.com）⑤ **Capacitor 足場**（8.4.0・appId `io.github.yutsutke.madeleine`・webDir `www`・`scripts/sync-web.mjs`・GitHub Pages はルート据え置き＝v78）。
> **✅ ブロッカー解決＝Apple メンバーシップ有効化（2026-06-25）**。支払い済みなのに「保留中」で約1週間詰まっていたが、サポート（ケース `102921793205`／担当 野村氏）が手動で有効化。**メンバーシップ＝有効・種類＝個人・有効期限 2027-06-25（米国太平洋時）**、登録ID `F6U34X83VU`／注文 `W1884878174`。**[App Store Connect](https://appstoreconnect.apple.com) 利用可能**（6/25「Welcome to App Store Connect」受領）。遅延理由は先方明記「多数の問い合わせ」＝2026年の Developer 登録 backlog（ネット評判と一致）。二重課金なし・追加手続き不要。**経緯メモ**: 6/17 にカード ¥12,980 引き落とし済み→6/22 にサポート問い合わせ（登録ページが再支払いを要求していたが「お支払いに進む」は押さず＝二重課金回避が正解だった）→6/25 有効化。**もう待ちは無い。下の「▶ 次の一手 (1)〜(5)」へ進める。**
> **2026-06-26 やったこと（App Store Connect 初期設定を一気に）**: ✅ App Store Connect サインイン確認 ✅ **有料アプリ契約に署名**（法人情報の郵便番号を 192-0023 に直して通過）✅ **税務フォーム**＝米国 W-8BEN ＋ Certificate of Foreign Status 両方「有効」（**租税条約 Part II＝Article 7(1) / 0.00% / Income from the sale of applications**＝米国源泉0%）✅ **銀行口座**＝三井住友銀行(8154)・JPY受取・「有効」 ✅ **ASBP（App Store Small Business Program）申請を送信**（"Thank you for your submission"・**承認待ち**）。**Team ID = 25TM5C27YT**。
> **▶ 次の一手**: ✅(1) App Store Connect サインイン＝済 → ✅(2) **ASBP 申請＝送信済（承認待ち・数営業日／承認後 翌会計月末+15日で15%）** → ✅(4) **リポ側パイプライン足場＝完了（v89）**＝`cap add ios`（**Capacitor 8 は SPM・Podfile 無し＝`pod install` 不要**）／Info.plist 用途文言（写真/位置 When-In-Use）／共有スキーム `App.xcscheme`／`apple-generic` versioning／**`codemagic.yaml`**（mac_mini_m2・xcode latest・自動署名→TestFlight）。**残りはユーザー操作の CI 接続のみ** → 🔵(3) **Codemagic 接続**＝① App Store Connect で **API キー発行**（Issuer ID / Key ID / .p8）② Codemagic サインアップ＆リポ接続＆API キーを integration 登録（名前を yaml の `MadeleineASC` に合わせる or yaml 書換）③ **App レコード作成**（bundle `io.github.yutsutke.madeleine`）→ (5) **初ビルド実行 → TestFlight**（署名/SPM 解決/スキーム検出は初ビルドでしか真の検証不可＝だから早く回す）。🔴 並行で最優先 de-risk＝**ネイティブ写真全件アクセスの最小スパイク**（パイプラインと独立）。⚠️ **EU 配信＝DSA トレーダー申告が別途未対応**（自宅住所が EU の App Store ページに公開される→私書箱で回避 or 当面 EU 除外。有料契約/ASBP/申請は止めない・配信テリトリー設定時に判断）。
> **まだ有効化待ちなら今できること**: (A) `codemagic.yaml` ドラフト（CI 設定・接続後にテスト）／ (C) i18n 文字列抽出（`index.html`・en/ja 辞書化＝Phase 1）。
> **環境メモ**: Node v24 / npm OK・Capacitor 8.4.0 導入済み・PC=**Windows**（Mac なし→Codemagic 自動署名）。`npm install` → `npm run sync:web` で `www/` 再生成（www と node_modules は gitignore）。詳細は下の Phase 0-6 チェックリスト＋ CHANGELOG v76–v78。

> ### 🚀 製品化決定（2026-06-17）— このリポを「製品本体」にして native 化（App Store 申請）
> - spike は **v75** で reminiscence + 地図/位置の体験検証を終え卒業。コードを 2 リポ（spike/madeleine）で**コピー二重管理**していたのをやめ、**このリポに統合＝今後の web 編集・Capacitor・Apple 申請は全部ここで**。`madeleine` リポは畳む（凍結・参照のみ。GitHub 削除は別途ユーザー確認）。public のまま製品化（ローカル完結で秘密ロジック無し）。
> - **最初の3手**（並行可）: ① ✅ **Apple Developer 登録を申込＝完了**（2026-06-18 申込・支払い済み、注文 W1884878174・12,980円／**承認・有効化待ち**＝唯一のクリティカルパスが走り出した）② ✅ **Node + Capacitor 確認＝完了**（Node v24/npm OK・**Capacitor 8.4.0 導入＝足場完了**: appId `io.github.yutsutke.madeleine`/appName Madeleine/webDir=`www`/ルート→www 同期スクリプト・v78）③ 🔴 **ネイティブ写真全件アクセスの最小スパイク**（PHAsset 全件列挙 + OS サムネ/EXIF＝「久しぶり=全ライブラリ」の生命線・最優先 de-risk）。
> - **⛰ ボトルネック（リスク順＝先に潰す順）**: ①🔴🔴写真全件アクセス（公式 Camera はピッカー止まり→`@capacitor-community/media` or カスタムプラグイン）②✅**Mac なし署名＝解決（v91・実機起動まで確認）**（Codemagic の鍵＝Secure env `CERTIFICATE_PRIVATE_KEY`＋`fetch-signing-files --create`）③🟠Apple 承認待ち ④🟠審査4.2（web ラッパー薄さ→native 要素で実質）⑤🟡IndexedDB→SQLite 移行（**track 含む**・写真キーは OS id にせず UUID 維持＝[[seal-protects-core]] でなく §⑥）⑥🟡AI on-device の持ち方 ⑦🟢マネタイズ/i18n/ストア素材。
> - **Phase 0-6**（戦略の正＝Notion HOW「アプリ化・ストア公開 方針メモ」。以下はリポ実行チェックリスト）:
>   - **Phase 0 登録・準備**: [x] Apple Developer 登録＝**有効化済み 2026-06-25**(個人・有効期限2027-06-25・Team ID 25TM5C27YT) [~] Small Business Program(手数料15%)＝**申請送信済 2026-06-26・承認待ち** [x] **有料アプリ契約署名＋税務(W-8BEN 租税条約 Art.7(1) 0%)＋銀行口座(三井住友 JPY)＝全て有効 2026-06-26** [~] Node+Capacitor 確認(Node v24/npm OK・Capacitor CLI 未導入) [x] プライバシーポリシー **ドラフト**(`privacy.html`・日英トグル・自己完結=外部依存なし／公開URL=yutsutke.github.io/photo-memory-spike/privacy.html／連絡先記入済み=Tanaka Yusuke / yutsutke@gmail.com／広告・IAP 節は実装時に最終確定)
>   - **Phase 1 Capacitor化+native要素+i18n**: [~] 🟢写真全件アクセス＝**capability 実機 YES（v92）＋本命 B プラグイン実機 YES（v93）＋アプリ本体へ統合（v94・案1軽量版・web 検証済／実機 TestFlight 待ち）** [x] **Capacitor 足場**(package.json/capacitor.config.json/webDir=www/sync スクリプト・8.4.0・v78・GitHub Pages はルート維持) [x] **`cap add ios`＝完了(v89・`ios/` コミット・Capacitor 8 は SPM＝Podfile 無し)＋Info.plist 用途文言(写真/位置 When-In-Use)＋共有スキーム＋apple-generic versioning** [x] **CDN vendoring**(exifr/heic2any/fflate/Leaflet→ローカル同梱・4.2対策 ＝v77 完了・`vendor/`／地図タイルとCLIPは対象外) [ ] IndexedDB→SQLite(track 含む) [~] onboarding(許可+「数枚→全ライブラリ」段階導線＝v94 で空状態/取り込みメニューに実装・実機確認待ち) [ ] アイコン/スプラッシュ/i18n(en/ja) [ ] AI(CLIP)の持ち方決定 [ ] 拡大=原寸オンデマンド/写真アプリ動線見直し/外部画像は実体保持（下記📷方針）
>     - **📷 画質・写真表示・外部画像の方針（2026-06-17 相談で確定）**:
>       - **拡大表示＝フル解像度**: ライブラリ写真は PHAsset 参照で**原寸をオンデマンド取得**（PHImageManager・サムネ即出し→裏でフル差替え）。512px 止まりは web/IndexedDB の容量制約（spike v23・[[storage-tradeoffs-accepted]]）→ **native で解消**。
>       - **「写真アプリで開く」は品質目的としては撤去**（アプリ内フル表示で完結＝reminiscence を切らさない）。残すなら system 共有シートの二次動線のみ。「この1枚を Apple Photos で開く」綺麗な公式 API は native でも無いが、もう不要。
>       - **外部画像（IG/X/フォルダ/スクショ）は非対称**: 写真ライブラリに居ない＝PHAsset 無し → **アプリが実体をフル/高解像度で自前保存**（巨大物だけ縮小）。「写真アプリで開く」は対象外。任意で取り込み時「写真アプリに保存」して PHAsset 化も可（**強制しない**＝カメラロールを汚したくない人向け）。
>       - **容量**: ライブラリ＝サムネ＋参照で軽い／外部＝実体保持だが枚数少で許容。コア拡張「取り込んだ画像も一級市民」（[[external-import-savedate-works]]）と整合。
>   - **Phase 2 Codemagic 自動署名**: [x] App Store Connect API キー(Issuer/Key ID/.p8)＝発行済 [x] **証明書/プロファイル自動生成＝`fetch-signing-files --create`＋秘密鍵 `CERTIFICATE_PRIVATE_KEY`（v91 で突破）** [x] **最初のアーカイブ→TestFlight＝完了（実機 iPhone 起動 2026-06-26）**
>   - **Phase 3 定常ビルド**: [x] **codemagic.yaml 作成＝完了(v89・mac_mini_m2/xcode latest・SPM なので pod 無し・agvtool で連番)** [x] **ビルド&TestFlight 自動＝v91 で疎通（署名→IPA→アップロード→内部テスト）**
>   - **Phase 4 収益化**: [ ] AdMob 非パーソナライズ(npa=1) [ ] ¥300 IAP(RevenueCat)+「購入を復元」必須 [ ] 広告は外周のみ(reminiscence 画面に出さない)
>   - **Phase 5 ストア素材・申請**: [ ] スクショ/説明文/年齢レーティング(日英) [ ] プライバシーラベル+ポリシー公開 [ ] TestFlight 最終確認 [ ] 申請(4.2 来たら native 要素足して再提出)
>   - **Phase 6 公開後→Android**（同じ Capacitor。12人/14日クローズドテストはこの段階）
> - **🆕 計画(2026-05-30)後に増えた native 論点**: 背景位置記録は native プラグイン（位置ロガー v73 の本命・Always 許可は審査注意・電池・**post-v1**／[[location-logger]]）／地図の世界対応 **Leaflet→MapLibre**（§①・**launch blocker でない**・地理院/世界タイルの出し分けは**写真位置基準**で MapLibre 周回に畳む・**言語トリガーは不採用**）／track も SQLite へ／GPSなし補完(v74-75)は web ロジックがそのまま乗る。
> - **整理の順番**（いきなり綺麗にしない）: ①最初の1ビルドを TestFlight まで通す（**今のコードで**・パイプライン de-risk）→ ②vendoring → ③web ハック撤去 → ④ネイティブ置換（写真全件/SQLite）。
> - **リポ統合の実務（native 着手時にやる・まだ未実行）**: このリポに Capacitor を足す（`cap init` / webDir）／`madeleine` の Capacitor 設定(appId `io.github.yutsutke.madeleine`)を移植 or 作り直し／madeleine リポは「→ photo-memory-spike に統合」と書いて凍結（GitHub 削除は確認後）。

> ### ▶ web の現状（製品本体の中身）+ 実機手触り確認
> - **状態**: BUILD `phase3.26`（v75）。git clean & push 済み。**v49〜v75 は全て preview E2E green。ただし v56〜v75 はまとめて実機 (iPhone Safari) 未確認**（GitHub Pages で web のまま随時確認可）。
> - **🛰️ 位置ロガー（v73）＋ GPSなし写真の位置補完（v74）**: あとからその日の軌跡を振り返るロガー。**ヘッダ 🛰️ パネルで3モード**＝オフ(既定)/重要な移動のみ(500m以上で1点・疎)/こまめに(25m or 20秒・密)。記録は **`track` ストア（DB v2、座標+時刻のみ＝写真キー非依存＝機種変更で移せる＝Notion §⑥）**。**振り返り＝地図に青緑の点線で重ねる**（⋯「🛰️ 自分の軌跡」トグル）。**v74＝GPSなし写真を「その写真の時刻に軌跡上のどこにいたか」で配置**（青緑の破線枠＝推定／挟めれば線形補間・片側30分以内ならsnap・記録の時間帯の外は出さない／非破壊・popup「📍この場所で確定」で永続化／⋯「📍GPSなし写真を軌跡から配置」トグル・既定ON）＝Notion §③(b)。**web の制約＝前面（アプリを開いている間）でしか記録できない**（背景常時は native の宿題＝§④）。詳細 CHANGELOG v73-v74。
> - **最有力の次手 = 実機 (iPhone Safari) で①ロガー（v73-74）②地図周回（v56〜v72）を触る**（手元作業）。ロガー観察: 散歩中アプリを開いて軌跡が貯まるか／前面復帰の1点が効くか／重要vsこまめの粒度・電池感／**GPSなし写真が軌跡の時刻位置に「だいたい合って」配置されるか（補間/snap 閾値1時間/30分が実軌跡に合うか・破線枠で推定と伝わるか・確定まで使うか）**／その日の軌跡の振り返りが想起を呼ぶか（[[editing-triggers-reminiscence]] と同型で効くか）。
> - **いまの地図モデル（v64〜v72 で確定。旧 v62 の seed/意味echo は撤去済み）**:
>   - 表示は**入口で決まる**。状態は `mapState.pickDays`（表示する日の集合）。**🗺ヘッダ＝ランダム3日**（各日の全GPSを別色の軌跡に・🎲もう一度引く・軌跡1本タップで他を暗く）/ **📷写真入口＝その写真の日**。
>   - **非連続の複数日は「全期間（ブラウズ）」で**。日を「重ねる」入口は **📅パネルの「＋ 追加」**（v71・数年前もスクロール不要）と**ブラウズ（全期間）の日付タップ**（1日から開始）。`pickDays`/`days` モードに集約。📅「適用」＝連続範囲だけ表示は別モード（排他）。
>   - **タイムライン＝地図に出ている日だけ（v72）**：選択モード（random3/day/days）はタイムラインに**選んだ日だけ**を出す（候補日は出さない＝[[ui-minimalism-works]]）。**日見出しタップ＝折りたたみ**（その日の写真を畳む▾/▸・**地図の線もマーカーも消える**・集合には残り再タップで戻る）。畳んでも残った日の色 index は安定。日を完全に外すのは ✕全部を見る or 組み直し。
>   - 線の色: 複数日=パレット（昔→今, v58）/ 単一日=時刻→空の色（v63, `skyColor`）。markercluster クラッシュは `removeOutsideVisibleBounds:false` で根治（v66）。
>   - 設計判断・ハマり詳細は CHANGELOG v49〜v72 と memory [[map-view-unparked]] に集約。
> - **最有力の次手 = 実機 (iPhone Safari) で地図周回（v56〜v72）を触る**（手元作業）。観察: ①ランダム3日の軌跡から想起できるか／3日が離れすぎないか②**📅「＋追加」で数年前の日に一発で届く快適さ／多日範囲が各日別色の独立軌跡で旅として読めるか**③**スッキリしたタイムライン（表示中の日だけ）＋ days の折りたたみ（▸ が「戻せる」と伝わるか／完全に外したい時の不便はないか）**④（v56-61 積み残し）封印の範囲×期間・位置/日付編集の操作感。
> - **大きく動くなら**: プロダクト化 = HOW Phase 0-1（Capacitor 実機 + ネイティブ写真ライブラリ全件アクセスの de-risk）。足場リポ `madeleine` は parked。
> - **将来案（要望が固まれば）**: 📅「＋追加」で足した期間を unit（trip 単位）でラベル表示・削除／多日範囲を v58 風の1本連続線で（`pickRanges` 化）／畳んだ日に枚数「(N枚)」表示／（不便なら）days に「日を外す」動線を戻す。
> - **運用リマインド**: 実装したら BUILD を上げ / preview で検証 / TODO・CHANGELOG 更新 / commit→push まで通す（[[feedback-commit-push-default]]）。情報を増やす提案は毎回 [[ui-minimalism-works]] で吟味。

**「完成の定義」(reminiscence walk が手で動かせる) は達成済み。** spike の主目的=手触り確認は強い手応え:
「めっちゃいい」(v4) →「色似たやつもいい / 夜祭と夜景のセット」(v5) →「より歩きたくなる」(v12) →「混ざったほうがおもしろい、単体だと退屈」(v19) →**1000枚規模で「開始も遷移も軽い・写真の豊富さが満足度を上げる・めっちゃいい・昔から今の流れを考えられて現在地と未来を想像できる」(v38 後)**。
→ **核心の検証問いに強い YES**: 気持ちいい=YES / やめにくい=YES / 「次どこ行く/旅のヒント」=YES (「現在地と未来を想像できる」)。さらに当初想定の一段深い価値=**時間的連続性 (昔→今→未来) の物語**が立ち上がった ([[reminiscence-at-scale-works]])。

> **🗺 直近の周回 (v49-v61, 2026-06-14〜15) = 地図ビュー一式 + 取り込み簡素化 + 記憶の封印 + 線/UI整理 + 位置/日付の後付け + 削除動線。** 軌跡+タイムライン / **線=つなぎ1日固定、複数日は1本の連続線で日ごとに色が変わる（昔→今が色で読める、v58）** / 日付・期間フィルタ（地図の📅パネル↔タイムラインの日付タップを双方向同期、絞ると一覧もその期間だけ）/ 連想ウォーク↔地図ピンの双方向リンク / 取り込み1本化（📷取り込む + ⚙️詳細設定）+ 日時の出所表示（撮影日/保存日/投稿日）/ **記憶の封印（地図🔒 で「見えている範囲×期間」を条件保存して非表示・いつでも解除、v56）** / **地図上部を「📅 期間 + ⋯ 表示（中身は🔒封印）」に整理（v57→v58）** / **写真の位置を後から地図ピンのドラッグで設定/修正（GPS無し写真の救済、近接GPSから初期値、v59）** / **写真の日付もフル画像から後から直せる（保存日のズレを本当の日時に、v60）** / **地図のマーカー・タイムラインから画像を削除（既存の非破壊除外を流用、♻️で復元可、v61）**。
> **🧭 強い signal（v59 実機）:「位置を直す行為自体が当時の記憶を想起させる」**（[[editing-triggers-reminiscence]]）= 編集（能動的関与）がコア「よみがえる」の表面。実用機能が裏でコアを強化。v60（日付）はこの発想の横展開。
> **preview で全 E2E green、次の一手 = 実機 (iPhone Safari) で手触り確認**（[[map-view-unparked]]）。
> **🔒 封印 = core を「守る」機能。** 偶然よみがえる体験は「よみがえってほしくない記憶の不意打ち」と裏表 → 封印で安心して全ライブラリ投入（久しぶり=全件）を後押しできる。隠す判定は `visible(p)=!excluded && !_sealed` に集約済み（次の隠し軸もここに足す）。
> **🧭 強い design signal（ユーザー言）:「必要最低限を見せ、余計な情報を削ると記憶が想起されやすい」** — 座標(📍)/♻️件数/フィルタ圏外(淡色→非表示) の削ぎ落としが効いた手応え。地図でも [[ui-minimalism-works]] が再確認された。情報を増やす提案は毎回この軸で吟味。
>
> **🚀 次の周回 = プロダクト化 (App Store / Google Play)。** spike は手触り検証を終え卒業。方針は Notion に集約 (2026-05-29 全面整理: **WHY/WHAT「プロダクト化方針 & フェーズ」↔ HOW「アプリ化・ストア公開 方針メモ」**の2枚、フェーズは HOW **Phase 0-6** に統一)。
> **プロダクト化の足場 = 別リポ `madeleine`** (2026-05-31 起こした。Capacitor で www/ に web を移植、GitHub private に push)。ただし native は parked、ユーザーは「またこのプロトタイプ (spike) に戻る」意向 (→ 次候補「タップ履歴」)。spike はプロトタイプ兼検証アーカイブとして当面いじりうる。
> **コア確定**: 「**偶然よみがえった久しぶりの記憶**」(3要素 = 偶然 / 久しぶり / よみがえる)。体験コピー「**偶然あの時に久しぶりに戻る**」。機能・広告・AI・名前はこの 3 要素で判断。
> **確定事項**: Capacitor / iOS 先行 / v1 ローカル完結 (同期なし) / マネタイズ = 無料 + 非パーソナライズ広告 + ¥300 除去 / AI = on-device 維持 / 機種変更 = **SQLite → OS 引き継ぎ自動** (お客さん負担ゼロ) / 通知 = ランダム偶然通知 opt-in (定期通知は不可) / 署名 = **Codemagic 自動署名で Mac 不要**。
> **次の一歩 = HOW Phase 0-1** (Capacitor で実機 + **ネイティブ写真ライブラリ全件アクセス**の de-risk = 「久しぶり = 全ライブラリ」の生命線)。この repo の **web 体験ロジックが資産として乗る** (取り込み系の web ハックはネイティブで一掃)。

### ✅ クローズ済み (各 vN の詳細は CHANGELOG)
- **Phase 0** 前提検証 (v1-3) — 日時/GPS/HEIC が実機で読める、Go 判定
- **Phase 1 MVP** (v4) — 取り込み / IndexedDB / ランダム3枚 / 連想ウォーク / エクスポート + 条件付ランダム(フィルタチップ)
- **Phase 1.5** 色トーン展開・純粋色モード (v5) / **Phase 1 拡大表示** 長押しフル画像 (v6)
- **Phase 1.8** Progressive Indexing + 取り込み待ちの計測・注意書き (v7-11) — 待ちは **iOS ピッカー側で web 不可侵**と計測で確定
- **explore ミックス既定**(時空+色をランダム混合)+ モード開閉トグル (v12)
- **ワンタップ除外 + 即補充**(✕ で非破壊除外、取り消し可、消したら次が繰り上がる)(v13-14)
- **時間軸フィルタを密に**(3ヶ月前 / 3年前 を追加、計 5 段階)(v15)
- **取り消しの赤エラー固定化を修正**(catch を null-safe に + 描画先行 + log タップで消去)(v16)
- **Phase 2 pilot: 「意味の近傍」を 50 枚スケールで検証**(opt-in、🧠 ボタン、CLIP base patch32、撤退可)(v17)
- **Phase 2 実機 GO → 全件 backfill 化**(50枚 cap 撤廃、中断ボタン + ヘッダ 🧠 進捗)(v18)。「意味での手触り最高」「~300枚まで待てる」signal
- **意味軸を mix プールに合流**(v19) — `mixedNeighbors` を 時空+色+意味 の純シャッフルに拡張。embedding 無しは自然 degrade。pilot で分離していた意味を既定 explore に統合。**実機で「混ざったほうがおもしろい、単体だと退屈」= 検証問い (AI 完璧すぎ既視感→ノイズで連想ドライブ) に YES で決着**
- **新規取り込みに embedding 自動相乗り**(v20) — `maybeAutoExtractEmbeddings`。AI opt-in 済み (embedding ≥1) の人だけ、新規取り込み後に色 backfill → 自動 embedding 抽出。未 opt-in には何もしない (100MB DL 強制なし)。mix の意味軸を新しい写真でも生かし続ける
- **AI 解析後のサムネ「?」化を修正**(v21) — `runEmbeddingExtraction` 完了時に `revokeAllThumbUrls()`+再描画の後始末が無く、CLIP 推論で iOS が無効化した blob URL が残っていた (v5/v7 と同じ罠)。`backfillColors` と同じ後始末を追加
- **差分取り込みフィードバック**(v22) — 取り込み完了時に「✓ 新規 N 枚 / 既にある分はスキップ M」を緑ログで表示。重複判定 (`dedup` index) は元々あったが黙ってスキップしていた → 可視化。「アプリを開くと自動で差分 UP」は **web 不可侵**なので「+ で選び直す → 既存はスキップ」を見える化する形が上限
- **1万枚スケール対応**(v23) — 原画 (3〜5MB/枚=10k で ~30GB) の保存をやめ **thumb のみ保存** (`KEEP_ORIGINAL=false`)。10k で数百MB に収め IndexedDB quota 超過/消去を回避。`THUMB_PX` を 512 に上げグリッドと長押し拡大を兼用 (原画は持たない tradeoff、ユーザー合意済)。起動時 `navigator.storage.persist()` で消去耐性 + usage/quota を console 出力
  - **実機: 1000枚で iOS ピッカー 2-3分、その後の解析は「UI 触りながら裏で回る分には気にならない」**。原画割り切りも受容「鮮明なのは純正写真アプリで日時で探せばいい」([[storage-tradeoffs-accepted]])
- **純正写真アプリへの動線**(v24-25) — フル表示 (長押し) に **日時を大きく表示**、「📷 写真アプリで開く」(`photos-redirect://`)。実機3点OK。**Web は写真アプリの特定写真を直接開けない**ので「アプリを開く+日付で自分で辿る」が上限
  - **v25 修正: コピーは日付のみ** (和暦 `2021年6月5日`)。写真アプリ検索は時刻入りだと不一致 (実機確認)。時刻は表示だけ残し同日内の目視特定に使う。**実機OK: コピー→写真アプリ検索で該当写真が出る**
- **クラウド非アップロードの注意書き**(v26) — 「🔒 写真はこの端末の中だけ、クラウドにアップロードされません」を空状態 (信頼判断の場) / 準備中・取り込み中 (UP してる感の瞬間) / AI モーダル (外部送信の不安) に明示。**reminiscence 画面 (random/explore) には出さない** (写真から意識を逸らさない原則)。signal「枚数が増えるほど満足度が上がる」= 全ライブラリ投入を後押しするための安心材料
- **意味解析のランダム間引き上限**(v27、ユーザー案) — 30k 目標。**重いのは CLIP 解析だけ**なのでそこだけ `MEANING_SAMPLE_CAP=10000` のランダム標本に上限 (自動解析は残り枠だけ、モーダルも整合)。色/時空/ランダム/フィルタは全件のまま (= 満足度の源泉)。「全部を重くする」でなく「重い1点だけ間引く」
- **起動の段階読み込み**(v28、ユーザー案) — 起動時の全件読み込みを撤廃。**Phase 1: `INITIAL_LOAD=3000` で即 UI / Phase 2: 残りを背景チャンク (`REST_CHUNK=2000`) 追い読み**。id が UUID なのでキー順=ランダム → 明示グルーピング不要。`addPhotos`/`loadedIds` で取り込み並行でも二重化なし。取り込みも全件 dbGetAll をやめ追記式に
- **初期プールのランダム回転**(v29、ユーザー指摘) — v28 は Phase 1 が先頭固定で「毎起動で同じ初期プール (開いた瞬間の最初の3枚が常に同じ部分集合)」だった → **UUID 空間のランダム開始点から N 件取る回転式**に。毎起動で違う代表サンプル。足りなければ先頭から補完 (リング回り込み)。Phase 2 は先頭 walk + dedup
- **dev ダミー大量生成**(v30) — 手持ち2000枚で 30k を試したい + ネット画像は EXIF 日時が無く入らない → **`#dev` でヘッダに 🧪**。日時/色/GPS/色つきサムネ付きダミーを IndexedDB に直接 N 枚生成→リロードで実起動経路を実数検証。通常 UI は汚さない (hash gate)。中身はダミー=機械の検証用、感じの検証とは別。**実機: 起動の遅れは数秒で許容、📥 裏読みも確認**
- **ダミーだけ削除**(v31) — 🗑 全削除は**本物も消える**ので、`#dev` に 🧹「ダミーだけ削除」を追加。実写真とダミーが同じ IDB に混在するため掃除はダミー限定で
- **ダミー削除を堅牢化**(v32) — 実機で「🧹 押しても消えない」: 全件1トランザクションのカーソル削除が 3万件規模の iOS で不安定だった → **`dedup` インデックスの 'dummy-' 範囲でキーだけ収集 + 1000件ずつ小トランザクション削除**に。連打ガードも追加。教訓: iOS IndexedDB は巨大1トランザクションが不安定 → インデックスで絞り小バッチで書く
- **Wake Lock で長時間処理を継続**(v33) — 実機で 6000件削除した所で画面が暗くなり停止 (iOS は画面オフで setTimeout を止める) → 生成/削除中だけ `navigator.wakeLock` で画面点灯維持 (`withWakeLock`、非対応は無視)、削除チャンクも 2000 に。削除は元々**冪等で再開可能**なので止まっても 🧹 再押しで続きが消える (安全網)
- **取り込み準備が長い時のガイド**(v34) — iPad 第8世代で「準備中」スピナーが長い時に「少なめに分けて」とガイド表示
- **取り込みの per-photo タイムアウト**(v35) — **「3枚でもダメ」= wedge と判明**: 1枚の処理が固まり `importFiles` が返らず `importingNow` が詰まる → 以降の取り込み全ブロック。→ 各 `importOne` に `withTimeout(30s)`、固まった写真はスキップして完走させ詰まり根絶
- **取り込みのメモリ改善**(v36) — iPad 47枚で OOM タブ再読込。`createThumbnail` finally で img/canvas 明示解放 + `BG_IMPORT_DELAY` 120→220。だが**v36 でも 47 で落ちた** → 本命は heic2any
- **HEIC変換を OS に任せる**(v37) — `<input accept>` から `.heic` を外し iOS に JPEG 変換させる + `isHeic` を type 優先に + サマリに「(うち HEIC変換 N)」診断。**実機で確定: 5枚→HEIC変換0 = heic2any 無実、accept 変更成功**。だが iPad 47枚は依然 OOM → **真因は raw JPEG デコードのメモリ (3GB 端末のハード限界)**
  - **結論**: メイン=iPhone14 (問題なし)、iPad は 20枚以下の小バッチ運用。**iPad の深追いはここで打ち切り** (spike 目的は iPhone で達成)。深追いするなら createImageBitmap 縮小デコード (iOS で不安定なので保留)
- **使い方・注意をまとめる**(v38) — 散らばっていた注意書きを ℹ️「使い方・注意」モーダルに集約 (プライバシー / 取り込み(差分・小バッチ) / 鮮明な写真は純正アプリ / 連想の歩き方 / 意味AI)。reminiscence 画面 (random) の常時 caveat は撤去して写真に集中。取り込み時の privacy 行など contextual なものは残す

### ✅ タップ履歴「足跡」(v39→v42, 2026-05-31 クローズ)
- random→tap→explore→tap… で**辿った写真の足跡**を中心カードの下にサムネ帯で表示。足跡タップで過去地点へ戻る。**v40 = モード切替と同じ開閉式** (既定畳み・opener に枚数表示)。**v41 = 永続化**: ウォークをまたいで貯まり**リロードしても残る** (id 列を localStorage 保存)、**30枚リングバッファ** (`MAX_TRAIL=30`)。**v42 = リロード時の入口はランダム3枚** (足跡の記憶は保持しつつ画面はリセット、中心保存は廃止)。🗑 全削除でのみクリア。詳細は CHANGELOG v39〜v42。実機の手触り観察は次セッション

### ✅ Instagram エクスポート (zip) 取り込み (v43, 2026-06-06)
- インスタからダウンロードした画像は **EXIF 日時が剥がれていて取り込めなかった** (`importOne` が `no-datetime` で無言スキップ)。→ Instagram「情報をダウンロード」(**JSON 形式**) の zip から**投稿日時を復元**して取り込めるように。
- **📦 ボタン → zip 1個を選ぶだけ**。端末内で `fflate` ストリーミング解凍 (**2パス**: パス1=JSON だけ展開し「画像→日時(+GPS)」マップ作成 / パス2=画像を1枚ずつ展開→サムネ化→保存→即破棄。iOS OOM 回避)。
- `creation_timestamp` (**Unix 秒** → ×1000)、構造ゆれ (単一投稿オブジェクト / `{ig_stories:[...]}` ラップ / media優先・root フォールバック)、uri パス正規化 (日付トップフォルダ・`content/` と `media/` 配下差を `media/` 起点で吸収 → 相対パス一致→basename フォールバック)、動画/JSONの除外に対応。GPS が `media_metadata.exif_data` にあれば拾う (時空軸の部分復活)。HTML 形式の誤出力は「日時ゼロ件」で検出し JSON 再出力を案内。
- `importOne(file, override)` に外部日時を渡せるよう拡張し EXIF 必須ゲートをバイパス (既存の写真/バックアップ取り込みは無変更)。preview で配列版・単一投稿版を **dbPut スタブで実DB非汚染**のまま E2E 検証済 (日時復元・サムネ生成・除外すべて green)。詳細 CHANGELOG v43。
- **残: 実機 (iPhone Safari) で実エクスポート zip を取り込み**、(a) 大きい zip の解凍/メモリ、(b) 投稿日時が「久しぶり」の手触りに合うか (撮影日でなく投稿日である点)、を観察。

### ✅ X(Twitter) アーカイブ (zip) 取り込み (v45, 2026-06-06)
- ユーザー「ツイッターもできるかな?」→ **画像投稿だけ対応**で実装(動画=サムネ化が要るので見送り、文字投稿=写真核に乗らないので対象外)。
- 📦 を **Instagram / X 自動判別**に一般化(`importIgZip`→`importArchiveZip`)。パス1のメタ読み取りで JSON(IG) と `tweets.js`(X) を振り分け、パス2の画像取り込み・サムネ・日時付与・入口 UI は共通。進捗カウントを画像ベース(`stats.images`)に統一。
- X 形式の確定事項(3視点調査): `.js` ラッパー(`window.YTD.tweets.partN = [...]` → 先頭`[`〜末尾`]`を JSON.parse)/ 各要素 `{tweet:{}}` 1段ネスト(`el.tweet ?? el`)/ `created_at` は英語固定UTC文字列(`Date.parse` 可)/ メディアは `extended_entities.media[]` の **`type==='photo'` のみ**(動画/GIF除外)/ ローカル実体は `data/tweets_media/<tweet.id_str>-<media_url の basename>`。突合は既存 byBase(ファイル名一致)にそのまま乗る。
- preview で合成アーカイブを E2E 検証(dbPut スタブ): 単一/複数画像取り込み・動画/テキスト/mp4 除外・命名規則一致・**IG 回帰**すべて green。詳細 CHANGELOG v45。
- **残: 実機で実 X アーカイブを取り込み**。注意点は IG と同じ(投稿日時=ツイート日時、巨大アーカイブのメモリ/時間、リツイートのローカル実体欠落は skip)。

### ✅ フォルダごと取り込み + 保存日フォールバック (v46, 2026-06-07)
- ユーザー「撮影時間も投稿時間も無い画像も扱いたい。**保存日時を時間として扱う**のは? 保存という行為自体が思い出すきっかけ。色/意味検索は日時に依存しないので**画像の思い出に拡張**したい。**フォルダごとアップ**も」。
- **📁 フォルダ取り込み**を新設(`<input webkitdirectory>`、PC 向け)。フォルダ内の画像をまとめて取り込み、EXIF 日時が無ければ **`file.lastModified`(保存日)** で日時補完。
- **適用は opt-in(Option A)**: フォルダ取り込みだけ保存日フォールバック(`importFiles(files,{fallbackMtime:true})` → fast-track / background queue 経由で `importOne(file,{fallbackMtime:true})` に伝播)。**「+」通常ピッカーは従来どおり EXIF 厳格** → 既定タイムラインの純度を維持([[ui-minimalism-works]] の「使う時だけ広がる」)。
- 根拠: 色ジャンプ/意味ウォークは日時非依存で刺さる軸。日時が不正確でも入れて連想に乗せる方が核(よみがえる)を強める。注意: まとめてコピー直後の画像は保存日が「今」に固まりうる(時間軸の久しぶり感は弱まるが色/意味は健在)→実機観察。
- preview E2E(dbPut スタブ): 8枚(fast6+bg2)が保存日付きで取り込み・両キュー経路で flag 伝播・「+」相当は date-less スキップ(0件)を確認。詳細 CHANGELOG v46。
- **残: 実機で保存日タイムラインの手触り**(「今」への固まりが連想を阻害しないか / スクショの保存日がよい起点になるか)。

### ✅ 取り込み口を2系統に整理 + バックアップ撤去 (v47, 2026-06-07)
- ユーザー「機能を整理。＋=日時ありのまま+フォルダも対応 / 📁=日時なしのまま+個別ファイルも対応 / ↑(復元)はいらない / ＋と📁を分かりやすく」。実機フィードバック「**保存日だけでも記憶の扉が開いた。その時のことを思い出した**」=v46 方向の強い肯定。
- **取り込み口を2系統に再編**(各々ファイル/フォルダ両対応):
  - **📷写真** = EXIF 日時のある写真だけ(厳格、`fallbackMtime:false`)
  - **🖼️画像** = 日時の無い画像も保存日で(寛容、`fallbackMtime:true`)
  - 押すと「📄ファイル / 📁フォルダ」の小メニュー(`openImportMenu`)。web は1入力で file+folder を兼ねられないため、`importFallback` フラグ + 既存 #picker(file)/#folder(folder) を共用して 2×2 を実現。
- **JSON バックアップ(⇩書き出し/⇧復元)を撤去**(要望)。`exportAll`/`importBackup`/`blobToB64`/`b64ToBlob`/#importer/ハンドラを削除。写真は IndexedDB に残る。
- **ヘッダ折り返し対応**: テキスト付きボタンで崩れないよう `flex-wrap` + 狭幅(≤600px)はタイトル/BUILD を独立行に(BUILD は iOS キャッシュ確認の生命線なので必ず表示)。desktop は1行維持。
- preview 検証(mobile375 / desktop1280): 4組合せ(📷/🖼️ × file/folder)が正しい fallback で発火・メニュー開閉・空画面からも動作・ヘッダ overflow 無し・BUILD 可視・コンソールエラー無し。詳細 CHANGELOG v47。

### ✅ 削除一覧から復活 (v48, 2026-06-07)
- ユーザー「間違えて削除した時用に、削除一覧から戻せるように」。
- 既存の ✕除外(`excluded` 非破壊フラグ)+ `undoExclude` を流用し、**「削除した写真」一覧モーダル**を追加。各サムネに「戻す」、複数なら「すべて戻す」。新ストア/新フラグ不要、CSS 最小(`.trash-grid`)。
- 入口 = ヘッダの **♻️N**(除外数バッジ。0 なら隠す=使う時だけ広がる)。ℹ️ の説明にも導線。🗑 全削除(`dbClear`)は別物のまま。
- 復元は `undoExclude`(excluded=false→render→dbPut)1本。allPhotos/loadedIds/thumbUrl/足跡 は後始末不要(マッピング調査で確認)。thumb は `thumbUrl()` で都度取得し URL を握らない(iOS revoke 対策)。
- preview E2E(dbPut スタブ): ♻️N 表示・一覧・戻す/すべて戻す・空状態・ヘッダ更新・モーダル開閉・コンソールエラー無しを確認。詳細 CHANGELOG v48。

### ✅ 地図ビュー + 取り込み簡素化の周回 (v49-v55, 2026-06-14)
- ユーザー「`owntracks-supabase-notion` を参考に地図機能を。位置情報から日本地図に撮った場所を配置・撮影順に線でつなぐ。サイドバーのタイムライン。位置情報無しは地図に出さずタイムラインで。**主眼は想起**（軌跡を見て想起を促す）」。
- 長く「今は作らない（次周回）」に置いていたが、但し書きは「検証が当たれば次の周回」。reminiscence 検証は達成済み（[[reminiscence-at-scale-works]]）→ **明示要求 = 発火**として un-park。
- ヘッダ **🗺** → フルスクリーン地図 overlay（`openMapView`/`closeMapView`、`render()` の外で命令的 build）。**Leaflet は遅延ロード**（`loadLeaflet`）。CARTO ダーク + 地理院タイル（無料/APIキー不要）。
- **線は旅/日ごとに自動分割**（`segmentTrips`, `TRIP_GAP_MS=12h`、色分け + 進行方向矢印）。**マーカー popup → 🔮 連想ウォーク**で既存 `showExplore` に接続（核に合流）。markercluster で大量対応。
- **モバイル=下部シート / PC=右サイドバー**（CSS `@media`）。タイムラインは全写真を日付降順、GPS 有=地図フォーカス / 無=フル画像。**逆ジオコーディングは不採用**（座標を外部に送らない）。
- preview E2E green（詳細 CHANGELOG v49）。**残: 実機で想起の手触り / 分割粒度 / 大量 GPS の負荷**。
- **v50 追記**: ①**線の日数を選択可**（地図上部チップ `日ごと/3日/1週間/ぜんぶ`。日ごと=暦日1本、3日/1週間=その範囲内は連結、ぜんぶ=全部1本。既定=日ごと）。②**連想ウォーク→地図ピン**（explore 中心カードに GPS があれば「🗺 地図でこの場所を見る」→ そのピンへ寄せて popup）。地図↔連想が双方向に。preview E2E green（日ごと3本/3日2本/ぜんぶ1本）。
- **v51 追記**: ①**日付/期間フィルタ**（タイムラインの日付見出しタップで地図を「その日/期間」に絞る。別の日タップで期間レンジ、`✕ 解除`、圏外は淡色。マーカー/動線/bounds が追従。「線の日数」と合成可）。②**連想ウォーク中心カードの座標(📍lat,lng)を撤去**（🗺ボタンが位置を担うので raw 座標は余計）。preview E2E green（単日2マーカー/1線・期間4マーカー・圏外タップでフル画像）。
- **v52 追記**: ①**マップ上にフィルタ/線UI**（地図上部に `📅{絞り込み}` `〰{線モード}` の2ボタン→タップでパネル。📅=開始/終了 date入力+適用/全期間、〰=線チップ。タイムラインの日付タップと状態を共有し相互同期）。②**ヘッダ ♻️ の件数を撤去**（アイコンのみ。削除一覧モーダルの件数は維持）。preview E2E green。
- **v53 追記**: ①**取り込みを初心者向けに1本化**（📷写真+🖼️画像→「📷取り込む」1ボタン。メニュー=ファイル/フォルダー/⚙️詳細設定。詳細設定は「すべて取り込み(撮影日優先・無ければ保存日)＝既定」/「撮影日(EXIF)のある画像だけ」の2択、localStorage永続）。②**日時の出所を表示**（`dateSource`=撮影日/保存日/投稿日 を記録し、カード/中心/フル/マーカーpopup の日時に小タグ。旧データは非表示）。preview E2E green。
- **v54 fix**: 地図のタイムライン「位置情報なし」項目（やマーカーのサムネ）タップでフル画像が出なかった（`.full-overlay` z-index:100 < 地図 150 で裏に隠れていた）→ フル画像を最前面 2000/2001 に。preview で最前面表示を確認。
- **v55 追記**: 地図で期間を絞ると**タイムラインもその期間だけ表示**（圏外は淡色でなく非表示。空期間は「写真はありません」注記）。📅パネル/日付タップどちらでも同期。

### ✅ 記憶の封印 (v56, 2026-06-14)
- ユーザー要望「**記憶の封印** — 場所・時期を指定して非表示にできる」。コアと表裏（偶然よみがえる体験は、よみがえってほしくない記憶の不意打ちと裏表）→ 封印で安心して核を楽しめる = core を**守る**機能。
- **地図 🔒 から「いま見えている範囲（`map.getBounds()`）× 期間（`mapState.range`）」を条件に封印**。条件は `{from,to,bounds,label}` を localStorage 保存（ルール型）→ 以後の取り込みにも自動適用 / 一覧から条件ごと解除 / ラベルで何を封じたか残る。
- **削除(excluded)と別概念**の独立フラグ `_sealed`。隠す判定を **`visible(p)=!p.excluded && !p._sealed`** に集約し random/explore プール・地図・タイムライン・足跡・件数・AI候補の `!p.excluded` を一括差し替え（約14ヶ所）。封印は消さない（blob 残る）ので解除で戻る。
- GPS無し写真は「📍見えている範囲だけ」OFF（場所問わず＝期間だけ）で封印可。全期間×範囲なし（＝全部封印）は禁止。
- preview E2E green（件数判定 time/place/複合・プール除外・パネル操作・全期間でも非表示・解除復活）。詳細 CHANGELOG v56。
- **残: 実機で① 地図の実 bounds による範囲封印が直感に合うか（preview は地図 0×0 で範囲封印を実検証できず）② 封印の安心感が体験を良くするか ③ 解除動線の分かりやすさ。**

### ✅ 線=旅行ごと1本 + 地図UI整理 (v57→v58 で再調整, 2026-06-15)
- **v57**: ユーザー「つなぐ線も1日単位で選びたい / 動静をすべて一本でつなぎたい」+「日ごとと🔒は基本使わない、上部は期間＋もう一つのアイコンに」→ 線モードを「旅行の区切り＝日数」(1日〜ぜんぶ+日ごと, 既定2日, gap分割) のセレクタ化 + 地図上部を「📅 期間 + ⋯ 表示」に整理 (⋯ から線/封印を選ぶ)。
- **v58 で再調整**: ユーザー「**線のつなぎ方は複雑になるだけ。つなぎは1日固定。複数日は線の色がかわる方式に。封印が表示の中にある UI は継続**」。
  - **線のつなぎを「1日」固定**（`TRIP_GAP_MS`、1日以上あいたら別の線）。線モードセレクタ（`LINE_MODES`/チップ/`pms-lineMode` 等）を全廃。
  - **複数日の旅行は1本の連続線で、日が変わるごとに色が変わる**（`drawTrips`: 旅行内を暦日でサブ線に割り、前日の終点から橋渡しして連続に見せ、色だけ日替わり。旅行ごとに色リセット）。= 昔→今が色で読める。
  - **⋯ 表示メニューは「🔒 記憶の封印」のみ**に（線項目を撤去、封印は表示の中に継続）。
- preview E2E green（3日連続旅=🔴🔵🟡の連続3サブ線で始点が前日終点と一致 / 別旅行は🔴から / UI=📅+⋯、⋯メニュー=🔒のみ）。詳細 CHANGELOG v58。
- **残: 実機で日替わり色が「昔→今」として読めるか / 8日超で色一巡の見え方 / 1日固定区切りの体感 / ⋯ メニューの発見性。**

### ✅ 写真の位置を後から設定/修正 (v59, 2026-06-15)
- ユーザー「タイムラインの画像の位置を修正・追加できるように。撮ったのに位置がたまたま入ってない写真がある。**地図のピンを動かして位置入力**できると」。
- **タイムライン項目＋マーカーpopup に「📍 位置を設定／位置を直す」**。押すと地図に**ドラッグ可能なピン**（サムネ入り）が出て、ドラッグ or 地図タップで位置指定→保存で `lat/lng`+`geoManual` を `dbPut` 永続→マーカー追加＆タイムライン更新（`mapState.refreshData`）。
- **初期位置を賢く**（`nearestGps`）：既存GPS→なければ時間的に近いGPS写真(3日以内)→なければ地図中心。旅行中に1枚抜けたケースを「ほぼ正しい位置から微調整」で救う。場所封印に該当しうるので保存時 `_sealed` 再計算。
- preview E2E green（GPS無しだけ「位置を設定」/ 編集ピンが近接GPSから出る / 移動→保存で marker 追加＆ボタン変化 / popup編集 / キャンセルは不変・dbPutなし）。詳細 CHANGELOG v59。
- **残: 実機でピンのドラッグ操作感 / 近接初期値の当たり率 / タップ移動とドラッグの使い分け。将来: 複数枚まとめて同じ場所 / `geoManual` の表示。**
- **実機フィードバック「位置を直す行為自体が当時の記憶を想起させる」**（[[editing-triggers-reminiscence]]）= 実用機能が裏でコア（よみがえる）を強化。編集を「作業」でなく「想起の入口」として設計する signal。

### ✅ 写真の日付も後から直せる (v60, 2026-06-15)
- 上記フィードバックを受け、ユーザー「同じ発想を**日付**にも。日付がおかしい/抜けてる写真を直せるように」。
- **フル画像ビューに「📅 日付を直す」**（`datetime-local` で日時編集→保存で `datetime`+`dateSource='manual'` を `dbPut`）。地図に依存しないのでフル画像（random/explore長押し・タイムライン・popup から到達）が入口。保存で `mapState.refreshData()`（タイムライン再ソート・線の日ごと色再計算）+ `render()` + フル画像を新日付で開き直す。
- 出所タグに **"手動"** を新設。`toDatetimeLocalVal`/`fromDatetimeLocalVal` でローカル時刻往復。
- preview E2E green（保存日写真に「日付を直す」/ プリフィル/ 保存で datetime+manual・dbPut / 新日付+手動タグで開き直す / **地図を開いた状態で「今」寄りの保存日写真を直すとタイムラインが日付順に並び替わる**）。詳細 CHANGELOG v60。
- **残: 実機で日時ピッカーの操作感 / 直す→そのまま連想ウォークへ繋ぐ案（「思い出した→辿る」）。将来: 名前/キャプション編集。**

### ✅ 地図/タイムラインから画像を削除 (v61, 2026-06-15)
- ユーザー「地図を開いている時に削除動線がない。マーカーの連想ウォーク/位置を直すの下に『画像を削除』を。タイムラインにも削除ボタンを」。
- **既存 `excludePhoto`（excluded・非破壊・トースト取消・♻️復元）を流用**。マーカーpopupに「🗑 画像を削除」、タイムライン各項目に「🗑」を追加（どちらも `excludePhoto(p)` を呼ぶだけ）。
- **地図同期を `excludePhoto`/`undoExclude` に集約**（末尾で `mapState.refreshData()`）→ 除外/取消で地図のマーカー・タイムラインが即消える/戻る。トースト z-index 200→1700（地図を開いたままでも「取り消す」が押せる）。
- preview E2E green（タイムライン🗑・popup🗑 で即消去&dbPut&トースト / z-index=1700 / 取り消しで地図に復活）。詳細 CHANGELOG v61。
- **残: 実機でタイムライン項目のボタン過密（位置を直す+🗑）が窮屈でないか。将来: 複数選択して一括削除 / フル画像にも削除導線。**

### ✅ 入り口で変わる地図 — 偶然の1スライス / 時空+意味 (v62, 2026-06-15)
- ユーザーの問い「位置写真が**増えたときに、どこまで・どういう基準で表示するか**」。現状=「期間内の全 GPS を撒く」は増えると破綻（クラッタ/線が国を貫く/fitBounds 無意味）。
- 結論=**入口で基準を変える**。ヘッダ🗺＝**偶然の1スライス**（ランダム写真の時空近傍）/ 写真からの入口＝その写真の**時空近傍＋意味echo（時空無視で似た場所）**。→ 両方「seed＋近傍」を出すだけ＝**全件を撒かず「どこまで」が自動で有界**。
- **既存 `spacetimeNeighbors`/`meaningNeighbors` を地図に投影**（色は混ぜない＝ユーザー指定）。視覚文法: 起点=緑リング / 時空=通常ピン＋**その日の線** / 意味echo=**線なし破線紫の単独ピン**（線あり=足跡 / 線なし=木霊）。
- **開く広さ=(A) 段階的**: 開幕は時空にタイト → `🌍 似た場所 N` で意味echoを足し bounds が広がり散らばりが見える。AI 未 opt-in は echo 空で自然 degrade。
- コントロール入れ替え（seed 時は 📅⋯ を隠し `🌍`/`↩︎全部を見る`）。タイムラインは全件のまま（次の起点を選ぶ索引）、日付見出しタップで seed を抜ける。**`btnMap` の click 配線を `()=>openMapView()` に修正**（Event が seed 化する事故を回避）。
- preview E2E green（合成11枚: 京都旅5+遠方echo3+無関係2+GPS無し1、DB 非汚染）。起点で 時空6/GPS5・線5、🌍展開で 10ピン/線5維持、全部を見るで復帰、ヘッダ経路でランダム seed。詳細 CHANGELOG v62。
- **残: 実機で①偶然の1スライスの楽しさ/lone pin ②意味echoが「似てる」と感じるか（embedding 要）③タイト開幕 vs 全体俯瞰のバランス。将来: popup から地図内 re-seed / 意味echoの閾値 / 「直す→連想ウォーク」合流。**

### ✅ 一日のみ表示の線を時刻→空の色に (v63, 2026-06-16)
- ユーザー案「一日のみ表示したとき、線の色を空の色に寄せる」+ 9 時間帯→色の対応表。単一日の動線を**その日の光（夜明け藍→朝焼けオレンジ→青空→夕焼け茜→夜濃紺）**で塗る。
- **適用は単一日だけ**（`lineGps` の暦日数==1）。複数日は **v58 パレット（昔→今）据え置き**。判定1行で seed単一日 / 📅単日 / 日付タップ の全入口に自動で効く。`skyColor(時刻)` は 9 バンドの hour 分岐。塗りは per-leg（出発点の時刻色）、矢印は薄い白、単日は線を太く（3→4）。
- preview E2E green: 9バンド正値・京都1日7点→6レグが朝→夕の空色・連続2日は palette のまま（回帰なし）・📅単日でも切替。詳細 CHANGELOG v63。
- **残: 実機で①空色が「その日」を蘇らせるか②階段状に見えないか（補間案）③凡例なしで気づくか④14-17時は `#9DB8C9`/`#E8C97A` どちらが合うか。**

### ✅ 地図=軌跡から想起: ランダム3日 / 写真→その日 (v64, 2026-06-16)
- ユーザーが地図を再定義: **メイン＝過去の軌跡から想起 / サブ＝写真の場所から想起**。単位を「写真の近傍」→**「日（その日の軌跡）」**へ。v62 の seed/意味echo を置き換え（**echo は外して軌跡に集中**＝ユーザー選択、`meaningNeighbors` は explore で残置）。
- **🗺ヘッダ＝ランダム3日**: GPSのある日から3日→各日の全GPSを軌跡に（その日の他の写真もマーカー、1枚の日は線なし）。**3本は色分け／1本タップで他を暗く（`focusedDay`）／🎲もう一度引くで別の3日**。**📷写真入口＝その写真の日**（`setPick([dayKeyOf(photo)],'day')`＝単一日→v63空色）。
- 状態を **`mapState.pickDays`（表示する日の集合）に集約**（`pickMode` random3/day、`focusedDay`）。`drawTrips` を「pickDays=各日独立軌跡（色分け/dim/単一日空色）」と「ブラウズ=v58/v63」に再構成。pickDays 時は 📅⋯ を隠し 🎲/↩︎全部を見る。
- **堅牢化**: `fitBounds` を `animate:false`（🎲連打など連続再描画でアニメ中フィットが重なる markercluster クラッシュを根絶）。
- preview E2E green（合成10枚: 京都A3/東京B3/大阪C2/札幌D1＋GPS無し1）。3本パレット色分け・Bフォーカスで他0.15・D(1枚)線なし・写真入口で空色2レグ・全部を見るで v58 復帰・🎲連打8回＋即close クラッシュ無し。詳細 CHANGELOG v64。
- **残: 実機で①ランダム3日が「軌跡から想起」になるか/3日が離れすぎて軌跡が小さくないか②1本タップの操作感（線が細くないか）③写真入口の手触り。次の周回: 飛び飛び複数日フィルタ（2旅行を重ねる、`pickDays`の土台あり）。**

### ✅ タイムラインも「表示中の日」に対応 (v65, 2026-06-16)
- v64 のヘッダ/写真入口とも好評。ユーザー「どちらの入口から入ってもタイムラインもその日に対応させて」。v64 は pickDays モードでもタイムライン全件だった。
- `updateTimelineUI` の pickDays ブランチを「**選ばれた日（3日 / その日）だけ表示**」に（pickDays 文字列→`dayStart(ms)` 変換で `itemEls/dayHeaders` と突合）。**その日の GPS 無し写真も一覧に出る**（地図=GPSのみ / タイムライン=その日の全部）。
- fitBounds を try/catch で握り潰し（🎲連打×開閉が極端に重なる時の markercluster moveend クラッシュ保険。`animate:false` は同期化のみで回避にならない）。
- preview E2E green: ヘッダ(A,B,C)→TL 8項目/日見出し3、写真入口(A)→TL 4項目/日見出し1、全部を見るで9項目。詳細 CHANGELOG v65。
- **残: 実機で絞ったタイムライン（特に3日が日付降順で離れて並ぶ）の使い勝手。**

### ✅ 飛び飛びの複数日を重ねる + markercluster クラッシュ根治 (v66, 2026-06-16)
- ユーザー要望「2回同じ地域に旅行。その2つの旅行の軌跡を重ねたい」。土台は v64 の `pickDays`（日の集合）。
- **タイムラインの日付見出しタップを「pickDays に出し入れ（集合トグル）」に変更**（連続レンジ計算から）。離れた2日を足すと**各日が別色の軌跡で重なる**。`pickMode='days'`（🎲 なし）。最後の1日を外すとブラウズへ。連続範囲は📅パネルに残置。
- タイムラインは「全日の見出しを残し、選んだ日だけ ✓ 強調＋写真、他は見出しだけ畳む」＝足す候補が見える（v65 の完全非表示から調整）。`.tl-day-on`。トグルは `applyFilter` のみ（DOM 非再構築＝スクロール維持）。
- **markercluster クラッシュ根治**: 近接マーカー＋fitBounds/zoomToShowLayer の moveend で `_leaflet_id in undefined`（非同期＝try/catch 無力）→ **`removeOutsideVisibleBounds:false`** で回避。効かなかった対処は CHANGELOG v66 参照（同じ罠を踏まない）。
- preview E2E green: 京都2旅行を重ねて軌跡2本(#ff6b6b/#feca57)・✓2日・写真4枚、外す/全部を見る/random3→days、**クラッシュ経路すべて非同期エラー0**。詳細 CHANGELOG v66。
- **残: 実機で離れた2旅行の軌跡の見分け／大量の日付見出しから足す日を探せるか／1万枚で全マーカー保持の負荷。**

### ✅ 非連続選択の置き場所を整理 (v67, 2026-06-16)
- ユーザー整理「もう一度引くは3日だけ／全期間アイコンのほうで飛び飛び日（期間）も選べるように」。v66 は非連続トグルが入口（random3）にも漏れ全日付が並んでいた。
- **入口モード（random3/day）はタイムラインを「その日だけ」に戻す＋日タップ無効**（`updateTimelineUI` を pickMode 分岐、`toggleDayFilter` 先頭 return）。**非連続選択は browse 由来の `days` モードに集約**＝全期間の文脈（📅/⋯ を残し 📅 は「📅 N日」、解除は ✕全部を見る）。📅 連続範囲と日タップ非連続は排他（封印 range は無影響）。
- preview E2E green: もう一度引き=TL3日だけ、全部を見る→browseで日タップ→「📅 1日」→「📅 2日」で軌跡2本が重なる、✕全部を見る/📅連続範囲適用で解除、写真入口は日タップ無効、フォーカス非同期エラー0。詳細 CHANGELOG v67。
- **残: 実機で全期間の日タップ非連続の発見性／足す日を探す手間。将来: 📅パネルで「期間を追加」して非連続な複数期間（trip単位）。**

### ✅ もう一度引く/写真入口でタイムライン折りたたみ (v68, 2026-06-16)
- ユーザー「もう一度引くの3日だけのタイムラインでも折りたたむ機能があると便利」。
- **入口モード（random3/day）で日見出しタップ＝その日の写真を折りたたむ/開く**（▾/▸）。v67 で入口モードの日タップは選択無効にしていたので、そこに折りたたみを乗せた。browse/days は従来どおり日タップ＝選択。
- 折りたたみは**タイムラインだけ**（地図のマーカー/軌跡は変えない）。`mapState.foldedDays`＋`applyFold`（`.tl-foldable`/`.tl-folded`/`.tl-fold`）。再構築でリセット（新しい3日は全開）。
- preview E2E green: 3見出し ▾・畳むと写真隠れ15→10→5・再タップで戻る・🎲でリセット・browse は ▾無しで日タップ＝選択・非同期エラー0。詳細 CHANGELOG v68。
- **残: 実機で折りたたみの手触り（畳んだ日に「(N枚)」を出すか）。days/browse でも折りたたみが欲しくなるか。**

### ✅ 折りたたんだ日は地図の線も非表示 (v69, 2026-06-16)
- ユーザー「タイムラインで折りたたんだ日の線は地図でも非表示にしてほしい」。
- **`drawTrips` が `foldedDays` の日の線（polyline＋矢印）を描かない**（dk→dayStart 変換で突合）。**マーカーは残す**（「線」だけ消す）。`toggleFold` を `applyFilter({skipFit:true})` に変え、地図とタイムラインを一括更新・ビュー維持。
- preview E2E green: もう一度引く3本→1日畳むと線2本・マーカー9枚のまま→もう1日で1本→開くと戻る・非同期エラー0。詳細 CHANGELOG v69。
- **残: 実機で「線だけ消しマーカーは残す」が直感に合うか（マーカーも消したくなるか）。** → **v70 でマーカーも消すに変更（ユーザー要望）。**

### ✅ 折りたたんだ日は地図の写真(マーカー)も非表示 (v70, 2026-06-16)
- ユーザー「線だけじゃなく、折りたたんだ日の写真も同様に非表示に」。畳んだ日を地図から丸ごと消す。
- **マーカー（`filteredGps`）から畳んだ日を除外**。色 index 安定のため **`lineGps` は全日のまま**にし drawTrips が畳んだ日の線をスキップ（残った日の色が入れ替わらない／1日残しでも空色化しない）。`toggleFold`＝`applyFilter({skipFit:true})`。
- preview E2E green: 線3色🔴🟡🔵/マーカー9→京都(中間)畳むと線2(🔴🔵)/マーカー6で残色不変→東京も畳むと線1(🔴)/3→全部畳むと0/0→開くと戻る・非同期エラー0。詳細 CHANGELOG v70。
- **残: 実機で「畳んだ日が地図から丸ごと消える」手触り。**

### ✅ 📅パネルから飛び飛びの日/期間を追加 (v71, 2026-06-16)
- ユーザー「飛び飛びの日もカレンダーから期間で選べるように。全期間・適用に加えて『追加』ボタン。**タイムラインからだけだと数年前の日を選ぶのが大変**」。将来案だった「📅で期間を追加」を明示要求で un-park。
- 📅パネルに **`＋ 追加`** を新設。入力中の日／期間（開始のみ=1日 / 両方=範囲）を**写真のある日だけ**に展開し `pickDays` にマージ → `pickMode='days'`。**タイムラインの日付タップと同じ状態に集約**＝片方で足した日をもう片方で外せる（相互運用を実機 E2E で確認）。何度でも足せて離れた2旅行を重ねられる。
- `適用`＝連続範囲だけ表示（range モード）/ `追加`＝重ねる（各日が別色の独立軌跡）と役割分離。追加後もパネルは開いたまま・入力クリア・status「✓ N日を追加（いま M日）」、再オープンで「📅 いま N日を重ねて表示中」。
- 多日範囲を v58 風1本連続線で描くのは見送り（既存 `days`＝各日独立軌跡を流用、ヒントで明記）。
- preview E2E green（合成12枚＝京都2019旅2日＋東京2022旅2日＋孤立日＋GPS無し、DB 非汚染）: 追加で2旅行重ね（4日10マーカー）・タイムライン✓同期・タイムラインから外せる・エッジ（空/写真なし期間/重複/開始のみ1日）・適用/全期間・再オープン status・コンソールエラー0。詳細 CHANGELOG v71。
- **残: 実機で①数年前の日にカレンダーで一発で届く快適さ②多日範囲が各日別色の独立軌跡で旅として読めるか（読めなければ `pickRanges` で期間を unit 化）③追加→閉じて確認の往復。**

### ✅ タイムラインは「表示中の日」だけ + days も折りたたみ (v72, 2026-06-16)
- ユーザー「タイムラインも、地図に表示されているのだけで十分。トグルで畳んで地図のライン・ピンが消える折りたたみはそのまま使いたい」。日の追加が v71 でカレンダーに移ったので、タイムラインに全候補日を出す必要が消えた（余計を削ると想起されやすい）。
- **`days` モードを入口モード（random3/day）と完全統一**（3つの条件式に `'days'` を足すだけ）: ①`updateTimelineUI`＝選択モードは常に「選んだ日（地図に出ている日）だけ表示・他は見出しごと隠す」に一本化（候補日 `multi` 分岐＋`tl-day-on`✓ 撤去）②日見出しタップ＝`days` も折りたたみ（**ブラウズだけ日選択を始める**）③`applyFold`/`useFold` に `'days'` 追加→畳んだ日は線もマーカーも消える。
- **畳む≠外す**: 折りたたみは pickDays に残し地図から隠す（再タップで戻る・「📅 N日」は減らない）。完全に外すのは ✕全部を見る／カレンダー組み直し。バーの「足す/外す」ヒントは撤去（タップ＝折りたたみのため、▾/▸ に委ねる）。
- preview E2E green（合成12枚、DB 非汚染、BUILD `phase3.23`）: 2旅行を組む→タイムラインは選んだ4日だけ・候補2日非表示→京都5/2 畳む＝▸＋写真collapse・マーカー10→8＋線も消える・pickDays は4日維持→再タップ復元→ブラウズ日タップ＝1日選択・✕全部を見る＝全6日。コンソールエラー0。詳細 CHANGELOG v72。
- **残: 実機で①スッキリしたタイムラインの手触り②▸ が「戻せる」と伝わるか③日を完全に外したい時に不便がないか（不便なら days に外す動線を戻す）。**

### ✅ 位置ロガー: 開いている間だけ軌跡を記録 (v73, 2026-06-17)
- ユーザー「ロガーを作る。あとから振り返ったときのその日の軌跡を振り返れるように。モードは3つ＝オフ／重要な移動のみ／こまめに」。Notion §③（位置の現在地/履歴）＝直前の現状照合で ⬜ 未着手だった部分を、明示要求で un-park。
- **web の根本制約を先に確認（AskUserQuestion）**: ブラウザは前面（アプリを開いている間）でしか位置を取れない。背景常時記録は native の宿題（§④）。それでも spike で作る＝データ構造＋振り返り UI を先に確定する価値、を選択。
- **記録＝`track` ストア（DB v1→v2）** `{id,t,lat,lng,acc,mode}`。**座標+時刻のみ＝写真キー非依存＝機種変更で移せる（§⑥）**。`t` index で日/期間取得。**3モード**（localStorage）＝off / important(500m以上で1点) / frequent(25m or 20秒)。`getCurrentPosition`(開いた瞬間1点)＋`watchPosition`、`haversine` で閾値フィルタ、`visibilitychange` で前面のみ記録＋復帰1点、記録中 `wakeLock`。
- **振り返り＝地図に重ねる**（別ビュー作らず）。`refreshTrack` が表示中の日/期間ぶんだけ青緑 `#2ee6c0` の点線＋点で描画（写真動線と別レイヤ・畳んだ日は消える・`applyFilter` 末尾で追従）。⋯「🛰️ 自分の軌跡」トグル（既定ON）。全削除は track も消す／パネルから記録だけ消去も。
- preview E2E green（合成4点を注入→検証後 `trackClear` で完全撤去・usage 0MB）: 4点→1本点線＋4ドット＝5 paths／トグル ON=5・OFF=0・再ON=5／パネル3モード・既定オフ・切替で永続＆🛰️点灯＆今日N点／**フィルタ important(500m)→2点・frequent(25m/20s)→2点**。エラー0。詳細 CHANGELOG v73。
- **残: 実機（iPhone Safari）で①散歩中アプリを開いて軌跡が貯まるか・前面復帰1点②重要vsこまめの粒度/電池③その日の軌跡の振り返りが想起を呼ぶか。将来: 滞在地クラスタ／再訪検出／ロガー軌跡→連想ウォーク／軌跡を空色に。本命の背景常時は native。**

### ✅ GPSなし写真を「軌跡の時刻位置」に配置 (v74, 2026-06-17)
- ユーザー「軌跡の記録がある時間帯の画像の扱い: ①位置情報あり＝今まで通り②位置情報なし＝写真の時刻と軌跡から、その時刻に軌跡上のどこにいたかの場所。このロジックに?」。Notion §③(b)「GPSなし写真の位置補完」＝ロガー(v73)が入って初めて成立する次の一手。
- **`estimateFromTrack(timeMs, pts)`**: 写真時刻を挟む前後点が1時間以内→**線形補間**、挟めなければ30分以内の片側に**snap**、記録の穴/外は**null＝補完しない**（「記録のある時間帯のみ」を厳密化）。二分探索。
- **非破壊（derived）が既定**: 推定は写真に保存せず地図表示のみ（軌跡が増えれば自動改善／実GPSと取り違えない）。**実GPS=白実線枠／推定=青緑の破線枠**で区別（`.photo-marker-est`）。popup「📍この場所で確定」(`confirmEstimatedPos`)で `lat/lng`+`geoFromTrack` を `dbPut`＝以後は通常GPS扱い（v59 流儀）。
- 描画は `refreshTrack` に相乗り（同 window/同 track 点で軌跡線＋推定配置を一度に）。⋯「📍GPSなし写真を軌跡から配置」トグル既定ON・畳んだ日は出さない・GPS写真ゼロでも収まる fitBounds フォールバック。
- preview E2E green（DB 非汚染・後始末で写真0/track0）: 単体（補間/穴null/snap/圏外null/空null）／描画（軌跡3点＋圏内1・圏外1→推定マーカー1のみ・トグルOFF0/ON1）／確定（中点35.695/139.7675＋geoFromTrack→実マーカー昇格）。エラー0。詳細 CHANGELOG v74。
- **残: 実機で①補間/snap 閾値（1時間/30分）が実軌跡に合うか②推定が「だいたい合ってる」と感じ破線枠で推定と伝わるか③確定まで使うか。将来: タイムラインGPSなし写真→推定マーカーへフォーカス／推定を連想の時空軸に参加／複数枚一括確定／別日軌跡からの補完。**
- **v75 追記**: 推定 popup に「✏️ 位置を直して確定」を追加（ユーザー要望）。v59 `startLocationEdit` に `seedLatLng` 引数を足し、**推定座標からドラッグ編集を開始**（ズレてても微調整で確定できる）。「📍この場所で確定」（推定のまま）と2択。preview green（編集ピンが中点 35.695/139.7675 から起動→保存で実マーカー昇格）。詳細 CHANGELOG v75。

### ▶ 次の候補 (未着手)
- **(封印の実機手触り)** — 上記 v56 の残（範囲封印 × ズーム量の関係 / 安心感 / 解除動線）。将来案: 封印エリアを地図に薄く塗る可視化 / 期間プリセット（この年・この季節）から封印 / 本体ヘッダからの封印一覧入口（今は地図🔒のみ）。
- **(地図の実機手触り)** — 実写真の GPS 分布で軌跡が想起を促すか / `TRIP_GAP_MS=12h` の分割が旅感覚に合うか（多日旅が割れすぎないか）/ 大量 GPS のクラスタ・描画負荷。色を時間グラデーションにして昔→今を補強する案。
- **(保存日タイムラインの手触り)** — フォルダ取り込みした date-less 画像が連想に馴染むか / 「今」への固まりが時間軸を壊さないか観察(上記 v46 の残)
- **(Instagram取り込み)** — ✅ **PC で実取り込み成功**(31枚 / 2023-05〜2025-04、最初の試行は HTML形式+メディア未同梱で空 → JSON形式+投稿込みで再エクスポートして成功)。残: iPhone Safari で実機。
- **(X取り込みの実機確認)** — 実 X アーカイブで取り込み、容量/メモリ・命名規則の版差(tweet_media 等)・ツイート日時の手触りを観察。
- **(X 動画対応?)** — 要望が出たら mp4 の1フレームをサムネ化(iOS デコード可否の確認込み)。今は画像のみ。
- **(足跡の実機観察)** — iPhone Safari で「辿り直したくなるか / 開閉の手触りはよいか」
- **触り込み + 検証問い観察** (最重要次手) — 実装ゼロで手触り深堀り。判定問い: やめにくいか / 旅のヒントになるか / 除外が馴染むか / **自動抽出が「気づいたら新しい写真も意味で繋がる」体験として馴染むか (発熱・バッテリーは許容範囲か)** / **大量 (1万枚) で連想の手触りがどう変わるか (リッチになるか / ノイズが増えるか)**
- **(実機計測待ち) 30k に向けた実挙動** — 取り込み所要・発熱・実 quota・dedup の遅さ (10k で 10k トランザクション)。重ければ: dedup を一括 `getAllKeys` で Set 化して in-memory 判定 / CLIP 自動抽出に低優先間隔 / thumb 品質 (512 で十分か)
- ~~**(30k 弱点) 起動時の全件読み込み**~~ → **v28 で段階読み込みで解決**。さらに重ければ次段: thumb を別ストアに分け、起動はメタデータだけ読みサムネは表示分だけ都度読むレイジー化 (schema 移行を伴う、まだ不要)
- **(将来) グルーピング/層化サンプリング** — v27 はランダム標本。ユーザー案の「事前タグ付け/グルーピング」で時期・イベント・場所ごとに**満遍なく**標本を取れば、意味解析の偏りを減らせる。ランダムで足りなければ着手
- **(保留) mix 配分の調整** — 今は時空/色/意味が均等期待値の純シャッフル。実機で意味が薄すぎ/濃すぎと感じたら配分を触る (v19 時点では純シャッフルで満足の signal)
- **(保留) 取り込み短縮** — 「ファイル」アプリ経由で HEIC 原本を受け取り iOS 変換を回避し背景で自前変換 (fast-track が重くなる + ピッキング手順が変わる tradeoff)

---

## Phase 0 — 前提検証（クローズ済み）

- [x] 最小HTMLに `<input type="file" multiple accept="image/*">` を置く
- [x] 選んだ写真を exifr で読み、**DateTimeOriginal** と **GPS** がコンソール/画面に出るか確認
- [x] スマホ対応 (UMD + 可視 input + 起動ログ)
- [x] スマホからアクセスする手段を決める → **GitHub Pages** (リポ public 化、`https://yutsutke.github.io/photo-memory-spike/`)
- [x] 自分の写真で日時取得率と GPS 取得率を数える → 日時 100% / GPS (元画像にあれば) 100%
- [x] HEIC が混じった時の挙動 → **exifr full ビルド単体で EXIF 抽出 OK**、heic2any は不要

**判定: Go** — 日時もGPSも完全に読める。Phase 1 へ進む。

**Phase 0 で固まった選定:**
- **exifr は full ビルド固定** (mini は parse(f, true) で JFIF 欠如エラー)
- **HEIC は exifr 直読みでEXIF抽出**。heic2any は Phase 1 のサムネ描画で必要なら導入
- iOS Safari は HTML を強烈にキャッシュ → HTML に no-cache メタ必須
- iOS Safari のフォトピッカーは HEIC を JPEG 変換して渡す → HEIC を HEIC のまま渡したい時は「ファイル」アプリ経由で選ぶ
- iOS のフォトピッカー経由でも GPS は保持される (元画像に GPS があれば)。strip はしていない

---

## Phase 1 — Tier 0 本体 (v4 + v6 で完全クローズ)

### 取り込み
- [x] 複数ファイル選択 → exifr で日時＋GPS（あれば）読む
- [x] HEIC は heic2any で JPEG 変換 (サムネ描画用、EXIF 抽出は exifr 単体で OK)
- [x] サムネ生成 (canvas、300px、EXIF orientation 補正)
- [x] **IndexedDB に保存**: `{id, name, datetime, lat, lng, blob, thumb, color, dedup, importedAt}`

### ランダム表示
- [x] 起動時／「もう一度引く」ボタンで、索引から **ランダムに3枚** のサムネをカード表示
- [x] 各カードに日時を表示（場所が取れていれば一緒に）

### 連想展開
- [x] カードをタップ → そのカードを中心に、**時空の近傍 6枚程度** をグリッド表示
- [x] 「近傍」の初期定義:
  - 同じ日 → 足りなければ ±3日 → さらに足りなければ ±7日
  - GPS が両方の写真にあれば、半径〜5km以内を優先
- [x] **展開先のサムネをタップ → そこを新しい中心に再展開**（連想ウォーク）
- [x] 「ランダム3枚に戻る」ボタン (履歴スタックは持たない、シンプル化)

### 拡大表示 (v6 クローズ)
- [x] サムネをロングタップ (500ms) でフル画像表示 (原画 blob を fixed overlay で contain)
- [x] タップで閉じる + ESC でも閉じる (desktop)

### 永続化＋エクスポート
- [x] リロードで索引と画像が復元される（IndexedDB）
- [x] **エクスポート / インポート**: JSON + base64 inline でバックアップファイル

---

## Phase 1 追加: 条件付ランダム (フィルタチップ) — v4 クローズ

ランダム3枚画面の上にチップ1行。タップで即フィルタ + ランダム再描画。デフォルト経験 (完全ランダム) は変えない。

### フィルタ定義 (鉛筆書きパラメータ、触ってみて調整)
- [x] **全期間** — デフォルト、全件から
- [x] **この時季** — 今日の月日 ±15日 (年は問わない、季節感を引く)
- [x] **3ヶ月前** — 今日から3ヶ月 ±15日 (v15 追加)
- [x] **1年前** — 今日から1年 ±15日
- [x] **3年前** — 今日から3年 ±30日 (v15 追加、中間スケール)
- [x] **10年前** — 今日から10年 ±60日 (サンプル薄想定で幅広く)
- [x] **久しぶり** — 撮影日が古い側 20% プール (最低10枚、`OLDEST_POOL_RATIO=0.2`, `OLDEST_POOL_MIN=10`)
- [x] **たくさん撮った日** — 同日に5枚以上撮ってる日のプール (`EVENT_DAY_MIN=5`)
- [x] **近場 📍** — 全GPSつき写真の重心から 30km 以内 (Geolocation 不要)
- [x] **遠出 📍** — 重心から 30km 以遠
- [x] チップ下に母数脚注 (`12 枚から` / `48 枚から (GPS付き 48 / 135 枚)`)
- [x] 該当 0 件時の専用メッセージ (GPS 不在ケースも区別)

---

## 技術メモ（ハマりどころ）
- **HEIC**: 拡張子か MIME type で判定、heic2any で変換してから IndexedDB へ
- **EXIF orientation**: サムネ生成時に補正、これを怠ると横倒し表示
- **サムネ運用**: 表示は常にサムネ。フル画像は拡大時のみ読む
- **性能**: 100〜300枚程度なら全部サムネ並べても問題ない想定
- **「近傍」のチューニング**: 最初の定義は鉛筆書き。触ってみて違和感あれば調整
- **Web制約**: 選んだ写真への永続参照は持てない → blob を IndexedDB にコピーする方式（理解の上で採用）
- **iOS Safari**: ESM 動的 import や `display:none` した file input は不安定。UMD + 可視 input が安全（v2 教訓）

## スタック
exifr / heic2any / canvas（サムネ＋orientation補正） / IndexedDB / プレーンHTML+CSS+JS
**地図ビューは v49 で追加: Leaflet + markercluster + polylineDecorator + CARTO/地理院タイル（遅延ロード・無料/APIキー不要）。バックエンドなし。**

---

## 完成の定義（この spike のゴール）
開く → 3枚出てくる → 1枚タップ → 時空の近くの写真が並ぶ → そのうちの1枚をタップ → さらに連想で広がる → 別の1枚でもまた歩ける → 閉じて開き直しても保持されている。
**この reminiscence walk が手で動かせる = ゴール。**

## 検証したい問い（作った後に自問する）
- 引いた3枚を見て、**気持ちいいか**（reminiscence の仮説）
- 連想で歩いていると、**やめにくいか**（associative walk の手触り）
- 「次どこ行く？」のヒントに**実際になるか**（旅の計画の軸）
- 悪い写真がランダムに出てきて気分が落ちたか／何回起きたか（curation 必要量の signal）

---

## Phase 1.5 — 色彩パレットによるトーン展開 (AI なし・超軽量) — v5 クローズ

**目的:** 「夕焼け→別の夕焼け」「緑→別の緑」のような **画面のトーンによる連想ウォーク** を最速で検証する。

### 取り込み時の処理拡張
- [x] サムネ生成時 (canvas 共有) に色彩特徴量を計算 (4×4 グリッドの平均 RGB = 48次元 Float32Array)
- [x] IndexedDB スキーマに `color` フィールドを追加して保存 (互換性: 既存レコードは null)
- [x] 既存写真は起動時に **非同期バックフィル** (1枚 50ms 遅延、メイン操作止めない、16x16 縮小経由でメモリ抑制)

### 連想展開のアップデート
- [x] explore 画面に **「⏳ 時空 / 🎨 カラー」トグル** を置き、即切り替え
- [x] ~~時空近傍 30枚を色順~~ → **純粋色 (時空無視) に振り直し** — 全件 (color 持ち) から中心との色距離順で上位 6 枚
  - 「夜祭と夜景のセット」のような色ワープを優先 (詳細: `memory/color-jump-works.md`, CHANGELOG v5)

### 検証問いの観察結果
- ✅ 「色の近傍」は時空とは別軸として reminiscence に強く効く (確認済み)
- ✅ 4x4=48次元の粗い特徴量でも色ジャンプは成立 (CLIP 512次元が必須とは限らない signal)
- ⏳ 「意味の近傍」(Phase 2 CLIP) との比較は次の検証問い

---

## Phase 1.8 — プログレッシブ・インデキシング（バックグラウンド非同期パイプライン） — v7 クローズ

**目的:** 大量 (数百〜数千) 投入時に「待ち時間ゼロ」でメイン体験に入る。ユーザーが遊んでる裏でじわじわインデックス化。**Phase 2 (CLIP) の前提条件**。

### アーキテクチャ (実装版)
```
投入 N枚
  ├─ openPicker: 開いた瞬間に preparing 画面へ (iOS 準備ギャップ中も画面が変わる)
  ├─ フェーズA: 先頭 FAST_TRACK_COUNT=6 枚を直列処理 → 完了即 random 画面へ
  └─ フェーズB: 残りを enqueueBackground → 1本のループが BG_IMPORT_DELAY=120ms 間隔で消化
        ※ 1枚完了で dbPut + allPhotos.push (revoke しない=表示中 URL を壊さない)
        ※ 再インポートはキューに合流 (多重ループを作らない)
```
※ 「先頭を並行処理」案は iOS の canvas/Blob メモリ脆弱性を避け直列に変更 (詳細 CHANGELOG v7)。

### TODO
- [x] インポート・キューと進捗ステート (`bgQueue` / `importStatus` / `importBusy` / `importingNow`)
- [x] フェーズA: `FAST_TRACK_COUNT` 枚を処理 → 完了で random 画面へ遷移
- [x] フェーズB: 残りを低優先度ループ、`setTimeout(120ms)` (Safari は requestIdleCallback 非対応のため setTimeout)
- [x] 控えめな進捗インジケータ (ヘッダに `🌀 12 / 30`、完了で消える)
- [x] 連打詰まり対策 (2 段ガード) + キャンセル復帰 (`cancel` イベント + 「やめる」ボタン)

### ハマりどころ (実装で踏んだ)
- IDB 読み書きはトランザクション安全に (バックグラウンド中もユーザー操作と競合させない)
- `URL.createObjectURL` は表示で再利用キャッシュ。**背景取り込み中に `revokeAllThumbUrls()` すると表示中サムネが ?化** (v5 の罠) → 背景は in-memory push のみ
- **`nextPaint()` の二重 rAF は背景タブ/ヘッドレスで発火せずハング** → `setTimeout` 保険を必ず付ける

---

## Phase 2 — ローカル AI (Transformers.js / CLIP) による意味展開

**目的:** 外部サーバなし、端末 WebGPU/WASM だけで「同じもの (猫、山、食べ物)」を認識して **意味空間でワープ**。

**現状: v17 で pilot 投入済み** (opt-in、50 枚スケール)。本実装するかは pilot の手触り判定後。

### pilot (v17 クローズ)
- [x] 🧠 ヘッダボタンから opt-in モーダル
- [x] `Xenova/clip-vit-base-patch32` を ESM 動的 import で lazy load (起動時には DL しない)
- [x] 50 枚ランダムサンプルだけ embedding 抽出 → IndexedDB に sparse 保存
- [x] explore のモード切替に「🧠 意味」を追加 (embedding ≥3 枚で出現)
- [x] 中心が embedding 未保持なら「未解析」フォールバック表示

### 本実装 (pilot で「意味での手触り最高」と実機検証 → GO)
- [x] 既存写真の全件 backfill (50枚 cap 撤廃、🧠 から「N 枚を解析」、中断可、ヘッダ 🧠 進捗) (v18)
- [x] mix プールに meaning を合流 (時空 / 色 / 意味 をシャッフル混合) — pilot は比較のため分離、刺さり確認済なので統合 (v19)。実機「混ざったほうがおもしろい、単体だと退屈」で検証問いに決着
- [x] v7 背景パイプラインに embedding 抽出を相乗り (新規取り込み時に色 backfill の後で自動抽出、AI opt-in 済みの人だけ) (v20)
- [ ] WebGPU/WASM の自動切替明示 (iOS 18+ で WebGPU、それ未満は WASM)。今は transformers.js デフォルト任せ
- [ ] (要検討) embedding だけ消す手段 (今は 🗑 全削除のみ)。本採用したので撤退用途より「やり直し」用途で

### ハマりどころ
- **直列実行禁止** — 100枚以上のときフリーズ。`Promise.all` で 5〜10 枚チャンク
- **WebGPU フォールバック** — 使えない環境は WASM (CPU) に自動切替を transformers.js オプションで明記
- **メモリ管理** — 上同様

### 検証したい問い
- 「色」vs「意味」、どちらが脳の報酬系に効くか？
- AI の意味ワープが完璧すぎると「標準フォトアプリの AI アルバム」と既視感が出ないか？
  → あえて時空や色のノイズを混ぜたほうが連想がドライブするか？

---

## 今は作らない（B：設計意図のメモだけ。建てない）
- ~~**地図ビュー**~~ → **v49 で un-park して実装**（軌跡+タイムライン、核＝連想ウォークへ合流）。検証問いが YES になりユーザーが明示要求したため昇格。
- **プラットフォームの器**（点／線＋カテゴリ＋意味の汎用モデル）。今日のランダム展開も、将来そのカーネルの一表面になりうる、という認識だけ持つ
- **積極的 curation**（⭐ つけ、お気に入りリスト）
- ~~**negative curation**（「これは引かないで」の除外印）~~ → **v13 で実装** (ワンタップ ✕ 除外、非破壊フラグ)。検証中に必要性が顕在化したため昇格
- **二軸切替**（"場所つながり" / "時期つながり" の手動切替）
- **proactive 通知**（"on this day" 等のプッシュ）— これは pull、push にはしない
- 認証、クラウド同期、シェア／OGP
- 他レイヤー（車・温泉・登山）の取り込み
- **Discord 取り込み**（2026-06-07 parked）— 公式エクスポートに画像実体が無く（CDN リンクのみ・短時間で期限切れ）、IG/X 方式（zip 一発で日時つき取り込み）が使えない。保存済み画像なら 🖼️画像（フォルダ・保存日）で取り込み可。要望が強まれば DiscordChatExporter（メディア込み JSON 出力）対応を検討。
- **TikTok 取り込み**（2026-06-07 parked）— 動画ファースト + 公式エクスポートに実体非同梱で、写真アプリの import 源として不適。代わりに発想を反転し「**動画版 madeleine（TikTok 投稿が偶然よみがえる）**」を**別プロダクト**として Notion に起こした（🎬 動画版 madeleine（仮）ページ）。この spike（写真）には足さない。
