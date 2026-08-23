#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""App Store Connect に出すスクリーンショットを、決まったピクセル数にそろえる。

なぜ要るか: ASC は枠ごとに**ちょうどのピクセル数**しか受け付けない（1px でも違うと弾かれる）。
            iPhone の機種によって撮れる大きさが違うので、出す前にここを通す。

使い方（PowerShell / Bash どちらでも）:
    python receipt-spike/scripts/asc-shots.py <入力フォルダ> [出力フォルダ]

    入力フォルダの .png/.jpg を**ファイル名順**に読み、01_, 02_ … と番号を付けて出す。
    順番を変えたい時は、入力側のファイル名を 1_, 2_ … にしておく（ASC は D&D で並べ替えもできる）。
    枠は既定が **6.5インチ（1284×2778）**。6.9インチ（1290×2796）にしたい時だけ `--69` を付ける。
    ⚠ ASC 側で開いているスロットと合っていないと「寸法が正しくありません」で弾かれる。

やること 3つ:
  ① ASC の枠（既定＝6.5インチ 1284×2778）ちょうどにする
     ⚠ 縦横の比が少しでも違う時は **はみ出すぶんを切る**（余白の帯を足さない）＝
        黒帯が入ると「画面の一部」に見えず、審査でも見栄えでも損をする。
  ② 透過（アルファ）を外す ＝ ASC は透過 PNG を受け付けない
  ③ 何をしたかを1行ずつ出す（黙って変えない）
"""
import sys, os, re

# ⚠ Windows のコンソールは既定が cp932 ＝ 日本語や ⚠ を出そうとすると**そこで落ちる**
#   （加工は終わっているのに最後の1行で例外＝失敗したように見える）。先に UTF-8 に寄せておく。
for _s in (sys.stdout, sys.stderr):
    try:
        _s.reconfigure(encoding='utf-8', errors='replace')
    except Exception:
        pass

try:
    from PIL import Image
except ImportError:
    sys.exit('Pillow が要ります: python -m pip install Pillow')

# ASC の枠。⚠ **ASC の画面で開いているスロットに合わせる**＝合っていないと
#   「1個以上のスクリーンショットの寸法が正しくありません」で弾かれる（2026-08-23 に実際に出た）。
#   既定は 6.5インチ＝あの日もこのスロットで出している。6.9 にしたい時は `--69` を付ける。
SIZES = {
    '65': (1284, 2778),   # iPhone 6.5インチ（11 Pro Max・XS Max など）※1242×2688 でも可
    '69': (1290, 2796),   # iPhone 6.9インチ（16 Pro Max など）
}
DEFAULT_SIZE = '65'
EXTS = ('.png', '.jpg', '.jpeg')


def fit(im, target):
    """はみ出すぶんを切って、ちょうど target にする（余白を足さない）。"""
    tw, th = target
    sw, sh = im.size
    if (sw, sh) == target:
        return im, 'そのまま（すでにちょうど）'
    # 短い辺に合わせて拡大 → 長い辺の余りを中央で切る
    scale = max(tw / sw, th / sh)
    nw, nh = round(sw * scale), round(sh * scale)
    im2 = im.resize((nw, nh), Image.LANCZOS)
    left, top = (nw - tw) // 2, (nh - th) // 2
    out = im2.crop((left, top, left + tw, top + th))
    cut = f'・上下を {nh - th}px 切りました' if nh > th else (f'・左右を {nw - tw}px 切りました' if nw > tw else '')
    return out, f'{sw}×{sh} → {tw}×{th}（{scale:.3f}倍{cut}）'


def main():
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    flags = {a.lstrip('-') for a in sys.argv[1:] if a.startswith('--')}
    if not args:
        sys.exit(__doc__)
    key = '69' if '69' in flags else DEFAULT_SIZE
    target = SIZES[key]
    src = args[0]
    dst = args[1] if len(args) > 1 else os.path.join(src, 'asc')
    if not os.path.isdir(src):
        sys.exit(f'入力フォルダが見つかりません: {src}')
    files = sorted(f for f in os.listdir(src) if f.lower().endswith(EXTS))
    if not files:
        sys.exit(f'画像がありません（{"/".join(EXTS)}）: {src}')
    os.makedirs(dst, exist_ok=True)

    print(f'枠: {target[0]}×{target[1]}（iPhone {key[0]}.{key[1]}インチ）／{len(files)}枚')
    for i, f in enumerate(files, 1):
        im = Image.open(os.path.join(src, f))
        im = im.convert('RGB')          # ② 透過を外す（ASC は透過を受け付けない）
        out, how = fit(im, target)      # ① ちょうどの大きさに
        # ⚠ 入力が「1_receipt.jpg」のように番号付きでも `01_1_receipt.png` と二重にしない
        #   （並べるために番号を付けて渡すのが普通の使い方なので、そこで見苦しくならないように）
        stem = re.sub(r'^\d+[_-]', '', os.path.splitext(f)[0])
        name = f'{i:02d}_{stem}.png'
        out.save(os.path.join(dst, name), 'PNG')
        print(f'  {i:02d}. {f} → {name} ／ {how}')   # ③ 何をしたか言う
    print(f'できました: {dst}')
    print('⚠ ASC には この順（01→）で入れてください。並べ替えは ASC 側でも D&D でできます。')


if __name__ == '__main__':
    main()
