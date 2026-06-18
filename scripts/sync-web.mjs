// web 本体をルート（GitHub Pages 配信用）から Capacitor の webDir（www/）へコピーする。
// Capacitor にバンドルさせる前に実行する（`cap sync` / `cap copy` の前）。
// 狙い: ルートを GitHub Pages の配信元として維持しつつ、native ビルドは www/ を使う。
// → web の実機確認運用（yutsutke.github.io/photo-memory-spike/）を壊さない。
import { rmSync, mkdirSync, cpSync, existsSync } from 'node:fs';
import { join } from 'node:path';

const root = process.cwd();
const out = join(root, 'www');

// バンドルに含める web アセット（必要なものだけ。docs/native プロジェクト等は含めない）。
const ASSETS = ['index.html', 'privacy.html', 'vendor'];

rmSync(out, { recursive: true, force: true });
mkdirSync(out, { recursive: true });

for (const a of ASSETS) {
  const src = join(root, a);
  if (!existsSync(src)) { console.warn('skip (missing):', a); continue; }
  cpSync(src, join(out, a), { recursive: true });
  console.log('copied', a);
}

console.log('web assets synced ->', out);
