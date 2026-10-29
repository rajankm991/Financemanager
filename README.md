# Finance Ledger – Android app: build, use and change guide

Everything about the app lives in **one file: `www/index.html`**. This folder wraps it in an Android app with Capacitor, and GitHub builds the APK for you every time you commit a change.
Nothing is stored on a server: all data stays on the phone, and Excel export/import works through the Android share sheet / file picker.

Publishing on Google Play has its own guide: **`PLAY-STORE-GUIDE.md`** (signing key, GitHub secrets, store listing, Google's testing rules, automatic uploads). Store text and graphics are in `store-listing/`, the privacy policy page is in `docs/`.

**Contents:** 1 Get the APK · 2 Updating · 3 Google Drive · 4 Changing features · 5 Notification Inbox · 6 Good to know · 7 Troubleshooting

---------------------------------------------------------------------------------------------------

## 1. Get the APK

### Option A – GitHub, no software needed (first time, about 10 minutes)
1. **Sign in** at github.com (a free account is enough).
2. **New repository** → name it `finance-ledger` → choose **Private** (recommended: it holds your app) → leave "Add a README" unchecked → **Create repository**.
3. **Upload the files**
   * On the empty repo page click **uploading an existing file**.
   * Unzip `FinanceLedger-APK.zip`, open the folder `FinanceLedger-APK`, select **everything inside it** (not the folder itself) and drag it into the browser.
   * Make sure the hidden folder **`.github`** went up too. (Mac: press `Cmd+Shift+.` to show hidden folders. If it still does not upload: click **Add file → Create new file**, type the name `.github/workflows/build-apk.yml`, paste the content of that file, commit.)
   * Click **Commit changes**.
4. Open the **Actions** tab. If GitHub asks, click **"I understand my workflows, go ahead and enable them"**.
   The workflow **Build Android APK** starts by itself (or open it → **Run workflow**).
5. Wait 6–10 minutes until it shows a **green tick**.
6. **Download the APK**
   * Easiest on a phone: open your repo → **Releases** (right-hand side) → latest *Finance Ledger build N* → tap **FinanceLedger.apk**.
   * Or: Actions → the run → **Artifacts → FinanceLedger-APK** (a zip) → unzip → `app-debug.apk`.
7. **Install**: open the downloaded file. Android asks to allow *Install unknown apps* for your browser/file manager → allow → **Install**.

### Option B – build on your own computer
Needs Node 22+, Java 21 and Android Studio (for the SDK).
```
npm install
npm run apk
```
The file appears at `android/app/build/outputs/apk/debug/app-debug.apk`.

### Option C – no APK at all: install it as an app from Chrome
Put the contents of `www/` on any HTTPS host (GitHub Pages, Netlify, Cloudflare Pages), open the link in Chrome on Android → ⋮ → **Add to Home screen / Install app**.
(Automatic notification capture does not work this way, only in the Android app. See section 5.)

---------------------------------------------------------------------------------------------------

## 2. Updating the app later
Change `www/index.html` (section 4), commit, wait for the green tick, download the new APK and install it **over** the old one. Your data stays, because every build is signed with the same key (`resources/debug.keystore`) and the version number goes up by itself.
*If Android says "App not installed", the old copy was signed with a different key: export to Excel first (Settings → Export), uninstall, install the new APK, then import.*

The APK from this workflow is **debug-signed**: fine for your own phones. For the Play Store use the separate `Release to Google Play` workflow (see `PLAY-STORE-GUIDE.md`).

---------------------------------------------------------------------------------------------------

## 3. Google Drive
* **In the Android app:** Settings → *Save to Google Drive* opens the phone's share sheet. Choose **Drive**, pick a folder, done. *Import from Google Drive* opens the file picker, which lists Drive.
* **In a browser:** the same buttons work, and with a Google OAuth Client ID (Settings → ⚙ Drive setup) the file uploads straight into a Drive folder in one click.
  This needs an https address. Optional web version on GitHub Pages: copy the contents of `www/` into a folder named `docs` in the repo → **Settings → Pages → Deploy from a branch → main / docs**. Your address is `https://YOURNAME.github.io/finance-ledger/`. Use it as the *Authorised JavaScript origin* in Google Cloud (Settings → ⚙ Drive setup shows the steps). Keep `docs/index.html` and `www/index.html` the same.
  (`docs/` also holds `privacy-policy.html` for the Play Store, leave that file there.)

---------------------------------------------------------------------------------------------------

## 4. Changing features: where to edit

### How to edit
* Quickest: on GitHub open `www/index.html` → pencil icon ✏️ → change → **Commit changes**. The APK rebuilds by itself.
* To **test before building**: save `index.html` on a computer and double-click it (Chrome). Press **F12 → Console** to see errors. Type test data, export to Excel, import it back.
* The file has three parts: `<style>` (looks) at the top, the page skeleton, and one big `<script>` (all logic). In the script, press **Ctrl+F** and search for the names below.

### Find-it table (search for the text in the left column)
| I want to change… | Search for | Notes |
|---|---|---|
| Colours, fonts, spacing | `:root{` | Variables like `--ink`, `--brass`, `--green`. Top menu look: `nav{` |
| Top menu tabs (names, order, add a tab) | `const TABS` | Add an entry, write a `vYourTab()` function that returns HTML, and add it to the view list inside `function render()`. Tabs share the screen width, so keep labels short |
| Dashboard cards and charts | `function vHome` | Charts are plain SVG helpers in the *charts* section |
| Transactions list, filters, default filter | `function vLedger`, `function drawTxn`, `f:{year:` | Default = current month/year; reset button is `resetTx` |
| Add/edit transaction form | `function txForm` | Payment methods list: `const PAY` |
| Cashflow (people balances, ledger) | `function vLending`, `function drawCf`, `function people`, `function lendForm` | |
| Salary tab, add month, auto carry-forward | `function vSalary`, `function salAddForm`, `function salUpdateForm`, `const SAL_FIELDS`, `function salBackfill` | `ymPrev()` is the "up to last month" rule |
| Budget & categories | `function vBudget`, `function catForm` | |
| Default categories, currency, empty start | `const SEED` | Only used for a brand-new install |
| Inbox tab (captured payments waiting for approval) | `function vInbox`, `function pCard`, `function pApprove` | Approve creates a normal transaction in `S.transactions` |
| How a notification is read (date, amount, merchant) | `function parseNotif` | Test by pasting a message in Inbox. Category guess: `function nGuessCat` |
| Notification consent screen | `function niDisclosure` | Google Play requires it, keep it clear and shown before access |
| Which apps are watched | `WATCHED.put(` in `resources/android/java/NotifListener.java` | Add a line with the app's package name |
| Settings page, Clear all data | `function vSettings`, `function resetAll` | |
| Where data is saved | `const KEY`, `function save`, `function migrate` | Saved in the phone/browser storage. **When you add a new field to saved data, give it a default inside `migrate()`**, otherwise old saved data will not have it |
| Excel export (sheets, columns, formulas) | `function xDash`, `xTx`, `xCf`, `xSal`, `xSet`, `function buildXlsx` | Row limit `const XR`; first category row `SET_CAT_FIRST` |
| Excel import | `function xParse`, `function xApply` | Reads the sheets by **header name** |
| Google Drive | `async function driveExport`, `function driveSetup`, `driveImport` | |
| Android-only behaviour (save/share file) | `async function download` | The `Capacitor` branch |
| App name / package id | `capacitor.config.json` (`appName`, `appId`) | Changing `appId` makes Android treat it as a *different* app. Decide it before the first Play upload (`PLAY-STORE-GUIDE.md`, section 1) |
| App icon | `resources/android/res/…` and `www/icon-*.png` | Keep file names and sizes |
| Status bar / edge-to-edge spacing | `--sat`, `--sab` in `:root{` | Space for the status and navigation bars on Android 15+ |

### Rules that save you from broken backups
1. **A new column or field = four places**: the form (add/edit), the screen that shows it, the Excel **export** sheet function, and `xParse` + `xApply` (import). Miss one and Excel round-trips lose it.
2. **Never rename an Excel header** in the export without renaming it in `xParse` too – import finds columns by their header text.
3. Keep the headers of the Transactions / Cashflow / Salary / Settings tables exactly as they are in the template.
4. After any change do the **round trip test**: add a few entries → Export to Excel → Clear all data → Import the file → everything must be back.
5. Change one thing at a time and commit each, so a problem is easy to undo (GitHub → file → History → revert).

### Asking me for changes
Open `www/index.html` on GitHub, copy it (or upload it to a chat), and describe the change in plain words – e.g. "add a Notes field to cashflow entries and carry it through Excel export and import". I will return the updated file; paste it over `www/index.html` and commit.

---------------------------------------------------------------------------------------------------

## 5. Notification Inbox (automatic capture of payments)

**What it does:** payment alerts from Google Pay, PhonePe, Paytm, CRED, ICICI iMobile, YONO SBI, BHIM SBI Pay, slice and your SMS app
are read by the Android app and listed in the **Inbox** tab. Nothing counts in your totals until you press **Approve**. Each item has
date, amount, merchant and a blank notes box, plus type, category and payment mode (a transaction needs a category).

**Turn it on (once per phone):** install the app → open **Inbox** → **Open notification access** → read and accept the consent screen → switch *Finance Ledger* on.
* If the switch is greyed out (APK installed outside the Play Store, Android 13 or newer): Settings → Apps → Finance Ledger → three dots → **Allow restricted settings**, then try again. Not needed for the Play Store version.
* Xiaomi / Oppo / Vivo / Samsung and similar: set the app's battery usage to **Unrestricted**, or the system may stop the listener in the background.

**How it is built into the APK:** the `android/` folder is created fresh on every build, so the native code lives in `resources/android/java/`
and `scripts/apply-notif.js` copies it in (and declares the service in the manifest) right after `cap add android`. You do not edit anything in `android/`.

**What is kept:** only notifications from the listed apps that contain an amount (Rs / INR / rupee sign) and a payment word. Everything else is ignored and never stored.
Alerts that arrive while the app is closed wait in a small queue and appear in the Inbox next time you open the app.
The same payment announced twice within 10 minutes (bank SMS + UPI app) with the same amount, type and date shows once.

**Watch another app:** open `resources/android/java/NotifListener.java`, add a line like `WATCHED.put("com.example.bank", "My Bank");` in the static block.
The package name is at the end of the app's Play Store address (`...details?id=THIS`). Commit and rebuild.

**Good to know**
* Capture works only in the Android app. In Chrome / the installed web version you can still paste a message into the Inbox to add it by hand.
* Inbox items waiting for approval are stored in the app but are **not** in the Excel export. Approved ones are normal transactions and are exported as usual.
* The web version and the Android app keep separate data. Move data between them with Excel export / import.
* If an amount or merchant is read wrongly, correct it on the card before approving. Send me the original message (account numbers blanked) and I will improve `parseNotif`.

---------------------------------------------------------------------------------------------------

## 6. Good to know
* Data lives inside the app. **Uninstalling the app deletes it**, so use *Settings → Export to Excel* now and then and keep the file.
* To change the app name, id or icon edit `capacitor.config.json` and `resources/android/res`.
* After changing `www/index.html` rebuild (Option A: commit again; Option B: `npm run apk`).
* Keep the signing key and passwords for Google Play out of the repo. Only the debug key in `resources/` belongs there.

---------------------------------------------------------------------------------------------------

## 7. Troubleshooting
| Problem | Fix |
|---|---|
| No workflow appears under Actions | The `.github/workflows/build-apk.yml` file did not upload. Create it with *Add file → Create new file* |
| Red ✗ on a step | Open the run → click the red step → read the last lines. Copy them to me and I will fix it |
| Red ✗ on step *Add notification capture* | Open the step and copy the last lines to me |
| "App not installed" on the phone | Different signing key or a different `appId` from the installed copy. Export to Excel, uninstall, install the new APK, import |
| Export does nothing in the app | Android shows a share sheet; choose **Drive** or **Files**. If nothing opens, update *Android System WebView* in Play Store |
| Data vanished | App data is deleted on uninstall or "Clear storage". Keep a regular Excel export in Drive |
| Inbox says capture is **Off** | Open Inbox → *Open notification access* and switch Finance Ledger on (section 5 has the greyed-out switch fix) |
| Payments are not appearing in Inbox | The app must be in the watched list, its own notifications must be on, and the alert must show an amount. Check `WATCHED` in `NotifListener.java` |
| Top menu hidden under the status bar | Needs the `--sat` / `--sab` rules in `:root{` and `body{padding-top:calc(48px + var(--sat))}`. Keep them if you edit the styles |
| Want it on the Play Store | Follow `PLAY-STORE-GUIDE.md`. The debug build from `Build Android APK` is for your own phones only |
