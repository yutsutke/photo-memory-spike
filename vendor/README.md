# vendor/ — ローカル同梱した外部ライブラリ (v77, 2026-06-17)

CDN 依存を**ローカル同梱**に切り替えたもの。狙い:
- **App Store 審査 4.2 対策**（外部 CDN に頼る薄い web ラッパーに見えないように）
- **オフライン耐性 / CDN 障害耐性**（web spike としても堅牢化）
- Capacitor で `www/` に同梱すれば実行時ネットワーク不要で動く

すべて MIT ライセンス（再配布・商用利用可。ライセンス全文は各 npm パッケージ参照）。

| ファイル | パッケージ | 版 | 取得元 | ライセンス |
|---|---|---|---|---|
| `exifr/full.umd.js` | exifr | 7.x | cdn.jsdelivr.net/npm/exifr@7/dist/full.umd.js | MIT |
| `heic2any/heic2any.min.js` | heic2any | 0.0.4 | cdn.jsdelivr.net/npm/heic2any@0.0.4 | MIT |
| `fflate/index.js` | fflate | 0.8.2 | cdn.jsdelivr.net/npm/fflate@0.8.2/umd/index.js | MIT |
| `leaflet/leaflet.js` + `leaflet.css` + `images/` | leaflet | 1.9.4 | unpkg.com/leaflet@1.9.4/dist/ | BSD-2-Clause |
| `leaflet.markercluster/*` | leaflet.markercluster | 1.5.3 | unpkg.com/leaflet.markercluster@1.5.3/dist/ | MIT |
| `leaflet-polylinedecorator/*` | leaflet-polylinedecorator | 1.6.0 | unpkg.com/leaflet-polylinedecorator@1.6.0/dist/ | MIT |

`leaflet/images/` はマーカー/レイヤーアイコン。`leaflet.css` の `.leaflet-default-icon-path` から相対参照されるので **leaflet.css と同じ階層に置く**こと。

## まだ vendoring していないもの（意図的）

- **地図タイル**（CARTO dark / 地理院）= 実行時に取得する地図データ。ローカル同梱の対象外。
- **`@huggingface/transformers@3.0.2`（CLIP）** = TODO Phase 1「AI(CLIP)の持ち方決定」で別途。JS だけ同梱してもモデル重みは実行時 DL なので意味が薄い。on-device 化の設計と一緒に決める。

## 更新するとき

版を上げたら、対応する URL から再取得して同じパスに置き、`index.html` の参照（head の `<script>` と `loadLeaflet()`）は**パスを変えない**ので差し替え不要。この表の版も更新する。
