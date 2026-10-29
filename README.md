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
6. **Download the APK.** Open your repo -> **Releases** (right-hand side) -> the latest *Finance Ledger build N*. It has two files:
   * **`FinanceLedger.apk` (Lite): install this one from your phone.** Everything works. In the Inbox you paste a payment message and it fills in the date, amount and merchant.
   * **`FinanceLedger-Full.apk` (Full):** also reads payment notifications by itself. Google blocks installing it from a download, see "App blocked to protect your device" below.
   (Or: Actions -> the run -> **Artifacts**, one zip per file.)
7. **Install**: open the downloaded file. Android asks to allow *Install unknown apps* for your browser/file manager -> allow -> **Install**.

### "App blocked to protect your device" (Google Play Protect)
**What it means:** Google Play Protect refuses to install an app from a download (browser, WhatsApp, file manager) when the app asks to read notifications, SMS or the screen. This protects people from fraud apps. It is live in India and some other countries, and there is no "Install anyway" button. Your app is not harmful: automatic payment capture is exactly the permission Google blocks.

**Ways to deal with it, easiest first**
1. **Install the Lite APK** (`FinanceLedger.apk`). It has no listener, so it installs normally. You paste payment messages into the Inbox instead of automatic capture.
2. **Full APK from a computer with a USB cable** (reported not to be blocked, because it does not come from a download): turn on *Developer options -> USB debugging* on the phone, then run `adb install FinanceLedger-Full.apk`. If an older copy is installed, `adb install -r` replaces it and keeps your data.
3. **Full APK with Play Protect scanning switched off for a few minutes:** Play Store -> your profile picture -> **Play Protect** -> gear icon -> turn off **Scan apps with Play Protect** -> install -> turn it back on. Not possible on phones where a work or school policy forces Play Protect on.
4. **Install from Google Play** (internal testing), see `PLAY-STORE-GUIDE.md`. Apps installed by Play are not subject to this block, and this is the way to share the automatic version with family or friends.

Lite and Full use the same signing key and the same data, so you can switch between them by installing one over the other (nothing is lost). If Android ever refuses, export to Excel first (Settings -> Export), uninstall, install the other one and import.

### Option B – build on your own computer
Needs Node 22+, Java 21 and Android Studio (for the SDK).
```
npm install
npm run apk
```
The file appears at `android/app/build/outputs/apk/debug/app-debug.apk`.
This builds the Full app. For the Lite one (no notification listener) run `NOTIF_CAPTURE=0 npm run apk` on Mac/Linux, or in PowerShell `$env:NOTIF_CAPTURE=0; npm run apk`.

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
  This needs an https address, so publish the web version with GitHub Pages (below).

### Publishing the web version (GitHub Pages)
The workflow `Publish web version (GitHub Pages)` puts `www/` online by itself, no copying of files.
1. Repo -> **Settings -> Pages -> Build and deployment -> Source: GitHub Actions** (not "Deploy from a branch").
2. Repo -> **Actions -> Publish web version -> Run workflow** (it also runs on every commit). Wait for the green tick.
3. Your address is shown on that run, and is `https://YOURNAME.github.io/REPONAME/`. The privacy policy is at `.../privacy-policy.html`.
   Use the address (without the file name) as the *Authorised JavaScript origin* in Google Cloud (Settings -> Drive setup shows the steps).
* A private repository needs a paid GitHub plan for Pages. On a free plan make the repository public (your data is never in it, it lives on your phone).
* The run stops with a clear error if `www/index.html` is missing or damaged. A page copied out of a chat window loses its `$` signs and shows blank, so always upload the file from the zip.
* **Page blank?** Open it, press **F12 -> Console**: a red error there says what is wrong. Also check the file size of `www/index.html` on GitHub: about 172 KB. Much smaller, or no `${esc(` anywhere in it, means it is damaged.

### Keeping the web app and the Android app the same
Both are made from **one set of files, the `www/` folder**. There is no second copy to keep up to date.
* **Edit only `www/index.html`** (and the other files in `www/`). Never keep a copy of the page in `docs/` (an old `docs/index.html` is ignored, and the Pages run warns you if one exists).
* On every commit three things run from that same commit: the Android build, the web publish, and (when you ask for it) the Play build. Each one stamps the same **build id** into the app, such as `c57-ab12cd3` (commit number and commit code).
* **Check it yourself:** open **Settings -> About this version** in the web app and in the Android app. Same build id = same text and same styles. Different ids mean one of the two has not been rebuilt yet: open the **Actions** tab and look for a red cross.
* **Web app looks old?** Settings -> About -> **Load the newest version** clears the web app's stored copy and reloads it. (The Android app updates by installing the new APK.)
* A new build also gives the web app's offline cache a new name, so it can never keep showing an older copy.

**What is the same:** every screen, wording, colour, font and layout. The tab names are the same on a phone, the Android app and a computer (Dashboard, Transactions, Inbox, Cashflow, Salary, Settings), and every table shows the same columns everywhere. On a narrow screen a wide table scrolls sideways inside its card instead of hiding columns. In my comparison of the web and the Android rendering, the Dashboard, Transactions, Cashflow and Salary screens came out identical down to the last pixel.

**What differs on purpose** (it describes the platform, so it cannot be identical):
* Inbox, first panel: on the web it says capture works only in the Android app; on Android it shows whether notification access is on.
* Settings, Google Drive: on Android Drive works through the share sheet, so the "Drive setup" button and its line are not shown; on the web they are.
* Settings, About: says "Web app" or "Android app".
* On Android the page leaves room for the status bar and the gesture bar.

**Your saved data is not synced between the two.** Each keeps its own storage on its own device or browser. Move data across with **Settings -> Export to Excel** and **Import**.

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
| Top menu tabs (names, order, add a tab) | `const TABS` | One name per tab, used on every screen size and on both the web and Android app (and the same names as the Excel sheets). Add an entry, write a `vYourTab()` function that returns HTML, and add it to the view list inside `function render()`. The tabs share the screen width and the text size follows it, so a very long name makes every tab label smaller |
| Dashboard cards and charts | `function vHome` | Charts are plain SVG helpers in the *charts* section |
| Transactions list, filters, default filter | `function vLedger`, `function drawTxn`, `f:{year:` | Default = current month/year; reset button is `resetTx` |
| Add/edit transaction form | `function txForm` | Payment methods list: `const PAY` |
| Cashflow (people balances, ledger) | `function vLending`, `function drawCf`, `function people`, `function lendForm` | |
| Salary tab, add month, auto carry-forward | `function vSalary`, `function salAddForm`, `function salUpdateForm`, `const SAL_FIELDS`, `function salBackfill` | `ymPrev()` is the "up to last month" rule |
| Budget & categories | `function vBudget`, `function catForm` | |
| Default categories, currency, empty start | `const SEED` | Only used for a brand-new install |
| Inbox tab (captured payments waiting for approval) | `function vInbox`, `function pCard`, `function pApprove`, `function pApproveReady` | Approve creates a normal transaction in `S.transactions`. Waiting items live in `S.pending` |
| How a notification is read (date, amount, merchant) | `function parseNotif` | Test by pasting a message in Inbox. Category guess: `function nGuessCat` |
| Notification consent screen | `function niDisclosure` | Google Play requires it, keep it clear and shown before access |
| Which apps are watched | `WATCHED.put(` in `resources/android/java/NotifListener.java` | Add a line with the app's package name |
| "About this version" panel and the build id | `function vAbout`, `<meta name="build">` | The id is filled in by `scripts/stamp-build.js` on every build; `dev` / `local` means an unstamped copy |
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

**Needs the Full build** (see "App blocked to protect your device" in section 1). The Lite build has no automatic capture; the paste box in the Inbox works in both.

**Turn it on (once per phone):** install the app → open **Inbox** → **Open notification access** → read and accept the consent screen → switch *Finance Ledger* on.
* If the switch is greyed out (APK installed outside the Play Store, Android 13 or newer): Settings → Apps → Finance Ledger → three dots → **Allow restricted settings**, then try again. Not needed for the Play Store version.
* Xiaomi / Oppo / Vivo / Samsung and similar: set the app's battery usage to **Unrestricted**, or the system may stop the listener in the background.

**How it is built into the APK:** the `android/` folder is created fresh on every build, so the native code lives in `resources/android/java/`
and `scripts/apply-notif.js` copies it in (and declares the service in the manifest) right after `cap add android`. You do not edit anything in `android/`.

**What is kept:** only notifications from the listed apps that contain an amount (Rs / INR / rupee sign) and a real payment word such as debited, credited, paid, spent, sent or received. One-time-password messages, bill-due and EMI reminders and offers are ignored. Everything else is ignored and never stored.
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
| **"App blocked to protect your device" / "can request access to sensitive data"** | Google Play Protect blocks apps that read notifications when they are installed from a download. Install `FinanceLedger.apk` (Lite), or put the Full APK on with a USB cable (`adb install`), or switch Play Protect scanning off briefly, or install through Google Play. Details in section 1 |
| "App not installed" on the phone | Different signing key or a different `appId` from the installed copy. Export to Excel, uninstall, install the new APK, import |
| Export does nothing in the app | Android shows a share sheet; choose **Drive** or **Files**. If nothing opens, update *Android System WebView* in Play Store |
| Data vanished | App data is deleted on uninstall or "Clear storage". Keep a regular Excel export in Drive |
| Inbox says capture is **Off** | Open Inbox → *Open notification access* and switch Finance Ledger on (section 5 has the greyed-out switch fix) |
| Payments are not appearing in Inbox | The app must be in the watched list, its own notifications must be on, and the alert must show an amount and a payment word (debited, credited, paid ...). Reminders and OTPs are skipped on purpose. Check `WATCHED` in `NotifListener.java` |
| Top menu hidden under the status bar | Needs the `--sat` / `--sab` rules in `:root{` and `body{padding-top:calc(48px + var(--sat))}`. Keep them if you edit the styles |
| Page is blank (on your computer or online) | The app now shows a **red box with the reason** instead of staying blank. If the box says the file is damaged or cut off, download `index.html` again from the zip (never copy it out of a chat window, that removes the `$` signs) and replace the file. If you see a **"This screen could not be drawn"** panel, use *Download my data* then *Start fresh*. Saved entries that are damaged are skipped automatically and a raw copy is kept in the browser |
| Web page (GitHub Pages) is blank or 404 | Settings -> Pages -> Source must be **GitHub Actions**. Run *Publish web version*. A 404 on a `/docs` source means that folder has no `index.html` (it only holds the privacy policy). A blank page means a damaged `index.html`: re-upload it from the zip |
| Web app and Android app look different | Compare the build ids in Settings -> About this version. If they differ, one was not rebuilt: check the Actions tab for a red cross and re-run it. On the web use *Load the newest version*. Any remaining difference should be only the platform lines listed in section 3 |
| Want it on the Play Store | Follow `PLAY-STORE-GUIDE.md`. The debug build from `Build Android APK` is for your own phones only |
