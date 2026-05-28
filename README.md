# 写真思い出スパイク

自分の写真を読み込み、ランダムに3枚カードを引いて、タップで時空の近傍に展開する、ローカル完結の reminiscence 試作。

「思い出すのは気持ちいいか」「次の旅のヒントになるか」を *自分の手で確かめる* spike。

捨ててよい spike。きれいさより「あの感じ」に最速で触ることを優先。

## 起動

`index.html` をブラウザで直接開く（ローカルファイル ok。ES Modules を CDN から取るので要オンライン）。

または:
```
npx serve .
```

## フェーズ

### Phase 0 — 前提検証（今ここ）
- `index.html`: file input → exifr で DateTimeOriginal / GPS を抽出してログ表示
- 自分の写真20〜30枚で日時取得率を確認
- Go判定: 日時がほぼ100% → Phase 1 へ

### Phase 1 — Tier 0 本体
- HEIC は heic2any で JPEG 変換
- サムネ生成（canvas, ~200px, EXIF orientation 補正）
- IndexedDB に `{id, blob, thumb, datetime, lat?, lng?}` 保存
- ランダム3枚カード表示
- カードタップで時空の近傍6枚に展開（同日 → ±3日 → ±7日、GPS あれば 5km 以内優先）
- 連想ウォーク（展開先タップで再展開）
- フル画像表示、エクスポート

## スコープの境界
- 作るのは「ランダム引き＋連想展開」。地図ビューも、プラットフォームの器も作らない
- 写真は端末から出さない。バックエンド／認証なし
- 迷ったら「A（今すぐ）か？」で判定

## スタック
exifr / heic2any / canvas / IndexedDB / プレーンHTML+CSS+JS

## 検証したい問い（作った後に自問）
- 引いた3枚を見て、気持ちいいか（reminiscence の仮説）
- 連想で歩いていると、やめにくいか（associative walk の手触り）
- 「次どこ行く？」のヒントに実際になるか（旅の計画の軸）
- 悪い写真が出てきて気分が落ちたか／何回起きたか（curation 必要量の signal）

## 今は作らない
- 地図ビュー / プラットフォームの器
- 積極的 curation（⭐、お気に入り）
- negative curation（除外印）— Phase 1 検証中に必要なら追加検討
- 二軸切替（場所つながり / 時期つながり 手動切替）
- proactive 通知（pull のみ、push にしない）
- 認証、クラウド同期、シェア
