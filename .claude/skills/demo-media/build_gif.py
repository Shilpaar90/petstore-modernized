#!/usr/bin/env python3
"""Assemble captured PNG frames into docs/images/demo.gif (+ cart/order-confirmation stills).

Run after capture.js has written frames to target/demo-frames/. See
.claude/skills/demo-media/SKILL.md for the full workflow.
"""
import sys
from pathlib import Path

from PIL import Image, ImageChops

FRAMES_DIR = Path("target/demo-frames")
OUT_DIR = Path("docs/images")
CANVAS = (960, 620)
BG = (255, 255, 255)

# (frame label, hold time in ms). This list is the GIF's playback order and
# per-frame timing — reorder/retime here without touching capture.js.
SEQ = [
    ("home", 1400),
    ("catalog", 1200),
    ("category", 1200),
    ("product-en", 1400),
    ("product-ja", 1600),
    ("register", 1200),
    ("account", 1000),
    ("cart", 1400),
    ("checkout", 1400),
    ("confirmation", 2200),
]

# Frames that also get saved as full-res autocropped stills (used in the README table).
STILLS = {
    "cart": "cart.png",
    "confirmation": "order-confirmation.png",
}


def find_frame(label: str) -> Path:
    matches = sorted(FRAMES_DIR.glob(f"*-{label}.png"))
    if not matches:
        sys.exit(f"no frame found for '{label}' in {FRAMES_DIR}")
    return matches[-1]


def autocrop(img: Image.Image) -> Image.Image:
    rgb = img.convert("RGB")
    bg = Image.new("RGB", rgb.size, BG)
    bbox = ImageChops.difference(rgb, bg).getbbox()
    return img.crop(bbox) if bbox else img


def fit_to_canvas(img: Image.Image) -> Image.Image:
    img = img.convert("RGB")
    scale = min(CANVAS[0] / img.width, CANVAS[1] / img.height, 1.0)
    new_size = (max(1, round(img.width * scale)), max(1, round(img.height * scale)))
    resized = img.resize(new_size, Image.LANCZOS)
    canvas = Image.new("RGB", CANVAS, BG)
    offset = ((CANVAS[0] - new_size[0]) // 2, (CANVAS[1] - new_size[1]) // 2)
    canvas.paste(resized, offset)
    return canvas


def main():
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    frames, durations = [], []

    for label, hold_ms in SEQ:
        cropped = autocrop(Image.open(find_frame(label)))
        if label in STILLS:
            cropped.convert("RGB").save(OUT_DIR / STILLS[label])
            print("wrote", OUT_DIR / STILLS[label])
        frames.append(fit_to_canvas(cropped))
        durations.append(hold_ms)

    frames[0].save(
        OUT_DIR / "demo.gif",
        save_all=True,
        append_images=frames[1:],
        duration=durations,
        loop=0,
        optimize=True,
    )
    print("wrote", OUT_DIR / "demo.gif", f"({len(frames)} frames)")


if __name__ == "__main__":
    main()
