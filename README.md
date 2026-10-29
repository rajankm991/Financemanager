# Finance Ledger – Android app (APK)

This folder wraps `www/index.html` (your web app) in an Android app using Capacitor.
Nothing is stored on a server: all data stays on the phone, and Excel export/import works through the Android share sheet / file picker.

## Option A – get the APK with no software installed (GitHub, ~5 minutes)
1. Create a free account at github.com and make a **new empty repository**.
2. Upload everything in this folder (drag and drop in the browser, keep the `.github` folder).
3. Open the **Actions** tab → *Build Android APK* → it starts by itself (or press **Run workflow**).
4. When the run turns green (about 5–8 minutes) open it and download **FinanceLedger-APK** at the bottom → unzip → `app-debug.apk`.
5. Send the file to your phone and tap it. Android will ask to allow "Install unknown apps" for the app you opened it from.

## Option B – build on your own computer
Needs Node 18+, Java 17 and Android Studio (for the SDK).
```
npm install
npm run apk
```
The file appears at `android/app/build/outputs/apk/debug/app-debug.apk`.

## Option C – no APK at all: install it as an app from Chrome
Put the contents of `www/` on any HTTPS host (GitHub Pages, Netlify, Cloudflare Pages), open the link in Chrome on Android → ⋮ → **Add to Home screen / Install app**.

## Google Drive
* **In the Android app:** Settings → *Save to Google Drive* opens the phone's share sheet. Choose **Drive**, pick a folder, done. *Import from Google Drive* opens the file picker, which lists Drive.
* **In a browser:** the same buttons work, and with a Google OAuth Client ID (Settings → ⚙ Drive setup) the file uploads straight into a Drive folder in one click.

## Good to know
* The APK from this project is **debug-signed**: perfectly fine to install on your own phones. For the Play Store you need a release build signed with your own key (`./gradlew bundleRelease` plus a keystore).
* Data lives inside the app. **Uninstalling the app deletes it**, so use *Settings → Export to Excel* now and then and keep the file.
* To change the app name, id or icon edit `capacitor.config.json` and `resources/android/res`.
* After changing `www/index.html` rebuild (Option A: push again; Option B: `npm run apk`).
