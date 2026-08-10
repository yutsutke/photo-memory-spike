// receipt-spike の web 本体を Capacitor の webDir（www/）へコピーする。
// あの日（リポ直下 scripts/sync-web.mjs）と同じ流儀＝GitHub Pages の配信元を壊さずに native へ載せる。
// 参照するのは receipt-spike/ 配下だけ（あの日の index.html には触らない）。
import { rmSync, mkdirSync, cpSync, existsSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = dirname(dirname(fileURLToPath(import.meta.url))); // receipt-spike/
const out = join(root, 'www');

// vendor/ = 地図（Leaflet）を同梱。⚠ あの日の vendor/ を参照しない＝ここへコピーを置く
//    （sync-web は receipt-spike/ 配下しか見ないので、`../vendor` 参照だとアプリ版で地図が消える）
const ASSETS = ['index.html', 'vendor'];

rmSync(out, { recursive: true, force: true });
mkdirSync(out, { recursive: true });

for (const a of ASSETS) {
  const src = join(root, a);
  if (!existsSync(src)) { console.warn('skip (missing):', a); continue; }
  cpSync(src, join(out, a), { recursive: true });
  console.log('copied', a);
}

console.log('web assets synced ->', out);
