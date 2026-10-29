# Finance Ledger – build & change guide

Everything about the app lives in **one file: `www/index.html`**. GitHub turns that file into an Android APK for you, every time you commit a change.

---------------------------------------------------------------------------------------------------

## PART 1 – Make the APK with GitHub (first time, about 10 minutes)

1. **Sign in** at github.com (free account is enough).
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
   * Or: Actions → the run → **Artifacts → FinanceLedger-APK** (a zip).
7. **Install**: open the downloaded file. Android asks to allow *Install unknown apps* for your browser/file manager → allow → **Install**.

### Updating the app later
Change `www/index.html` (Part 2), commit, wait for the green tick, download the new APK and install it **over** the old one. Your data stays, because every build is signed with the same key (`resources/debug.keystore`) and the version number goes up by itself.
*If Android says "App not installed", the old copy was signed with a different key: export to Excel first (Settings → Export), uninstall, install the new APK, then import.*

### Optional – a web version on GitHub Pages
Needed only for one-click Google Drive upload from a browser (it requires an https address).
Copy the contents of `www/` into a folder named `docs` in the repo → **Settings → Pages → Deploy from a branch → main / docs**. Your address is `https://YOURNAME.github.io/finance-ledger/`. Use that address as the *Authorised JavaScript origin* in Google Cloud (Settings → ⚙ Drive setup shows the steps). Keep `docs/index.html` and `www/index.html` the same.

---------------------------------------------------------------------------------------------------

## PART 2 – Changing features: where to edit

### How to edit
* Quickest: on GitHub open `www/index.html` → pencil icon ✏️ → change → **Commit changes**. The APK rebuilds by itself.
* To **test before building**: save `index.html` on a computer and double-click it (Chrome). Press **F12 → Console** to see errors. Type test data, export to Excel, import it back.
* The file has three parts: `<style>` (looks) at the top, the page skeleton, and one big `<script>` (all logic). In the script, press **Ctrl+F** and search for the names below.

### Find-it table (search for the text in the left column)
| I want to change… | Search for | Notes |
|---|---|---|
| Colours, fonts, spacing | `:root{` | Variables like `--ink`, `--brass`, `--green`. Top menu look: `nav{` |
| Top menu tabs (names, order, add a tab) | `const TABS` | Add an entry, write a `vYourTab()` function that returns HTML, and add it to the view list inside `function render()` |
| Dashboard cards and charts | `function vHome` | Charts are plain SVG helpers in the *charts* section |
| Transactions list, filters, default filter | `function vLedger`, `function drawTxn`, `f:{year:` | Default = current month/year; reset button is `resetTx` |
| Add/edit transaction form | `function txForm` | Payment methods list: `const PAY` |
| Cashflow (people balances, ledger) | `function vLending`, `function drawCf`, `function people`, `function lendForm` | |
| Salary tab, add month, auto carry-forward | `function vSalary`, `function salAddForm`, `function salUpdateForm`, `const SAL_FIELDS`, `function salBackfill` | `ymPrev()` is the "up to last month" rule |
| Budget & categories | `function vBudget`, `function catForm` | |
| Default categories, currency, empty start | `const SEED` | Only used for a brand-new install |
| Settings page, Clear all data | `function vSettings`, `function resetAll` | |
| Where data is saved | `const KEY`, `function save`, `function migrate` | Saved in the phone/browser storage. **When you add a new field to saved data, give it a default inside `migrate()`**, otherwise old saved data will not have it |
| Excel export (sheets, columns, formulas) | `function xDash`, `xTx`, `xCf`, `xSal`, `xSet`, `function buildXlsx` | Row limit `const XR`; first category row `SET_CAT_FIRST` |
| Excel import | `function xParse`, `function xApply` | Reads the sheets by **header name** |
| Google Drive | `async function driveExport`, `function driveSetup`, `driveImport` | |
| Android-only behaviour (save/share file) | `async function download` | The `Capacitor` branch |
| App name / package id | `capacitor.config.json` (`appName`, `appId`) | Changing `appId` makes Android treat it as a *different* app |
| App icon | `resources/android/res/…` and `www/icon-*.png` | Keep file names and sizes |

### Rules that save you from broken backups
1. **A new column or field = four places**: the form (add/edit), the screen that shows it, the Excel **export** sheet function, and `xParse` + `xApply` (import). Miss one and Excel round-trips lose it.
2. **Never rename an Excel header** in the export without renaming it in `xParse` too – import finds columns by their header text.
3. Keep the headers of the Transactions / Cashflow / Salary / Settings tables exactly as they are in the template.
4. After any change do the **round trip test**: add a few entries → Export to Excel → Clear all data → Import the file → everything must be back.
5. Change one thing at a time and commit each, so a problem is easy to undo (GitHub → file → History → revert).

### Asking me for changes
Open `www/index.html` on GitHub, copy it (or upload it to a chat), and describe the change in plain words – e.g. "add a Notes field to cashflow entries and carry it through Excel export and import". I will return the updated file; paste it over `www/index.html` and commit.

---------------------------------------------------------------------------------------------------

## Troubleshooting
| Problem | Fix |
|---|---|
| No workflow appears under Actions | The `.github/workflows/build-apk.yml` file did not upload. Create it with *Add file → Create new file* |
| Red ✗ on a step | Open the run → click the red step → read the last lines. Copy them to me and I will fix it |
| "App not installed" on the phone | Different signing key or a different `appId` from the installed copy. Export to Excel, uninstall, install the new APK, import |
| Export does nothing in the app | Android shows a share sheet; choose **Drive** or **Files**. If nothing opens, update *Android System WebView* in Play Store |
| Data vanished | App data is deleted on uninstall or "Clear storage". Keep a regular Excel export in Drive |
| Want it on the Play Store | Needs a release build signed with your own private key and a Play Console account (one-time fee). The debug build here is for your own phones |
