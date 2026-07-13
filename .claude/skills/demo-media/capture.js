// Drives the running storefront through the full journey with Playwright (using the installed
// Chrome via channel 'chrome' — no browser download), screenshotting each step for the demo GIF.
// Run from the repo root with the app up on http://localhost:8080. Frames -> target/demo-frames/.
//
//   export NODE_PATH="$(cd ${TMPDIR:-/tmp} && npm root)"   # where playwright-core was installed
//   node .claude/skills/demo-media/capture.js
const { chromium } = require('playwright-core');
const path = require('path');
const fs = require('fs');

const BASE = process.env.BASE_URL || 'http://localhost:8080';
const OUT = process.env.FRAMES_DIR || path.join(process.cwd(), 'target', 'demo-frames');
const USER = 'demo-' + Date.now(); // unique per run (app's in-memory store persists across runs)
const PASS = 'petstore123';

async function launch() {
  try {
    return await chromium.launch({ channel: 'chrome', headless: true });
  } catch (e) {
    return await chromium.launch({
      executablePath: '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
      headless: true,
    });
  }
}

(async () => {
  fs.mkdirSync(OUT, { recursive: true });
  const browser = await launch();
  const ctx = await browser.newContext({ viewport: { width: 1180, height: 820 }, deviceScaleFactor: 2 });
  const page = await ctx.newPage();
  let n = 0;
  const shot = async (label) => {
    const file = path.join(OUT, `${String(++n).padStart(2, '0')}-${label}.png`);
    await page.screenshot({ path: file, fullPage: true });
    console.log('shot', file);
  };
  const go = async (url) => { await page.goto(BASE + url, { waitUntil: 'networkidle' }); };

  await go('/'); await shot('home');
  await go('/categories'); await shot('catalog');
  await go('/categories/FISH'); await shot('category');
  await go('/products/FI-SW-01'); await shot('product-en');

  // Switch locale to Japanese via the language bar — i18n highlight, then back to English.
  await page.getByRole('link', { name: '日本語' }).click();
  await page.waitForLoadState('networkidle');
  await shot('product-ja');
  await page.getByRole('link', { name: 'EN' }).click();
  await page.waitForLoadState('networkidle');

  // Register (select the button BY NAME — the top bar's Log out button is first in the DOM).
  await go('/register');
  await page.fill('#username', USER);
  await page.fill('#password', PASS);
  await shot('register');
  await page.getByRole('button', { name: 'Register' }).click();
  await page.waitForLoadState('networkidle');

  // Log in
  await page.fill('#username', USER);
  await page.fill('#password', PASS);
  await page.getByRole('button', { name: 'Log in' }).click();
  await page.waitForLoadState('networkidle');
  await shot('account');

  // Add to cart (Angelfish, EST-1)
  await go('/products/FI-SW-01');
  await page.locator('form[action$="/cart/add"]').first()
    .getByRole('button', { name: 'Add to cart' }).click();
  await page.waitForLoadState('networkidle');
  await shot('cart');

  // Checkout
  await page.getByRole('link', { name: /Proceed to checkout/ }).click();
  await page.waitForLoadState('networkidle');
  await page.fill('#name', 'Demo Shopper');
  await page.fill('#addressLine', '1 Blueprint Way');
  await page.fill('#city', 'Palo Alto');
  await page.fill('#email', 'demo@example.com');
  await shot('checkout');
  await page.getByRole('button', { name: 'Place order' }).click();
  await page.waitForLoadState('networkidle');
  await shot('confirmation');

  await browser.close();
  console.log('DONE', n, 'frames ->', OUT);
})().catch((e) => { console.error('FAILED', e); process.exit(1); });
