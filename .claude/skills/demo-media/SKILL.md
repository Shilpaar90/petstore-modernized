---
name: demo-media
description: Regenerate the storefront demo GIF and screenshots by driving the running app end-to-end in a real browser (Playwright + installed Chrome) and assembling the frames (Pillow). Use after UI/template changes, a version bump, or when asked to refresh docs/images/demo.gif or the README screenshots.
---

# Regenerate the demo GIF & screenshots

Everything here is a real capture of the running app — never mock up screenshots.

## Prerequisites
- The app must be running on `http://localhost:8080` (use the `run` skill; default H2 profile is fine).
- Node + a one-time `playwright-core` install; Python 3 with Pillow; Google Chrome installed.
  Playwright uses the installed Chrome via `channel: 'chrome'` — no browser download.

```bash
# one-time, in a scratch dir (keeps node_modules out of the repo):
cd "${TMPDIR:-/tmp}" && npm init -y >/dev/null 2>&1 && PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 npm i playwright-core
export NODE_PATH="$(npm root)"   # so the capture script can require playwright-core
```

## Capture the journey → build the GIF
Run from the repo root with the app up:
```bash
node .claude/skills/demo-media/capture.js       # writes PNG frames to target/demo-frames/
python3 .claude/skills/demo-media/build_gif.py  # autocrops + writes docs/images/demo.gif (+ stills)
```

`capture.js` drives: home → catalog → category → product (EN) → product (日本語, via the language
bar) → register → login → add-to-cart → checkout → order confirmation. It uses a unique username
per run (the app's in-memory store persists across runs) and selects buttons **by name**
(`Register` / `Log in` / `Place order`) — do NOT use a generic `button[type=submit]`, because the
top bar's "Log out" button is first in the DOM and would be clicked instead.

`build_gif.py` autocrops whitespace, contains+centers each frame in a 960×620 box, and writes
`docs/images/demo.gif` plus `order-confirmation.png` and `cart.png` stills.

## Verify before committing
Open `docs/images/demo.gif` and confirm the confirmation frame shows the signed-in user, order id,
`SUBMITTED`, and total — not the home/logged-out page (the classic symptom of the wrong submit
button). Then commit the images with the `ship` skill.

## Tuning
- Frame order + per-frame hold times live in the `SEQ` list in `build_gif.py`.
- Viewport/scale in `capture.js` (`deviceScaleFactor: 2` for crisp 2× images).
