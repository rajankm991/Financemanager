# Publishing Finance Ledger on Google Play (with GitHub)

How it works: you build and sign the app on GitHub (`Release to Google Play` workflow), and either upload the file by hand
(the very first time) or let GitHub upload it for you (every update after that).

---------------------------------------------------------------------------------------------------

## 0. Read this first

| Topic | What to know |
|---|---|
| **Target Android version** | Since 31 Aug 2026 Google Play only accepts new apps and updates that target Android 16 (API 36). Your project used Capacitor 6 (API 34), so it is now on **Capacitor 8** (API 36, Node 22, Java 21). Already done in this project. |
| **Personal vs organization account** | A personal Play account created after 13 Nov 2023 must run a **closed test with at least 12 testers, opted in for 14 days in a row**, before you can apply for production. Organization accounts do not have this rule. Plan about 3 weeks for the first release. |
| **Finance apps** | Any app with financial features must complete Google's **Financial features declaration**. Google's policy also says developers of *financial products and services* must register as an **Organization**. A personal ledger that does not lend, hold or move money should not count, but check the declaration form in your account: if it insists on an Organization account, that is a Google decision and I cannot change it. |
| **Notification access** | Google allows it but requires clear consent first and a privacy policy. The app now shows a consent screen before it opens Android's notification-access page, and the privacy policy page is in `docs/`. |
| **Your existing phone app** | The APK you installed from GitHub Releases is signed with a different key from the Play version. Android will not install one over the other if the app id is the same. See section 10. |
| **App id** | `com.financeledger.app` (in `capacitor.config.json`). Package names are unique across all of Google Play and **can never be changed after the first upload**. If you want your own, change it now (section 1). |

---------------------------------------------------------------------------------------------------

## 1. Choose the app id (once, before anything is uploaded)
Open `capacitor.config.json` on GitHub and set `appId` to something unique to you, for example `com.yourname.financeledger`
(lower case, dots, no spaces). Commit. Nothing else needs to change, the build scripts read it from this file.
A different id also means the Play app and your current phone app can sit side by side, which makes moving your data easy (section 10).

## 2. Create the Play Console account and the app
1. Go to play.google.com/console and sign up as a developer (one-time fee, about US$25). Choose **Personal** or **Organization**.
2. **Create app** -> name *Finance Ledger*, language, **App**, **Free**, accept the declarations.
3. Leave it for now. You will fill it in at section 6.

## 3. Make your upload key and store it as GitHub secrets
You need this once. Do it on a computer with Java installed (the same one Android Studio uses is fine).

1. Create the key (type this in a terminal / command prompt, in a folder you will remember):
   ```
   keytool -genkeypair -v -keystore upload.jks -alias upload -keyalg RSA -keysize 2048 -validity 10000
   ```
   It asks for a password (use a long one), your name, and so on. Remember the **password** and the **alias** (`upload`).
2. **Back up `upload.jks` and the passwords** somewhere safe that is not GitHub (password manager, private cloud folder).
   Play App Signing lets you ask Google to reset a lost upload key, but that takes time, so do not lose it.
3. Turn the file into text for GitHub:
   * Mac: `base64 -i upload.jks | pbcopy`   (copies it)
   * Linux: `base64 -w0 upload.jks`
   * Windows PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("upload.jks")) | Set-Clipboard`
4. In your GitHub repo: **Settings -> Secrets and variables -> Actions -> New repository secret**. Add these four:

| Secret name | Value |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | the long text from step 3 |
| `ANDROID_KEYSTORE_PASSWORD` | the keystore password |
| `ANDROID_KEY_ALIAS` | `upload` |
| `ANDROID_KEY_PASSWORD` | the key password (the same as the keystore password if you were not asked for a separate one) |

Never put `upload.jks` in the repo. The `.gitignore` already blocks it.

## 4. Publish the privacy policy page
1. Open `docs/privacy-policy.html` on GitHub (pencil icon). Replace `[DATE]`, `[YOUR NAME OR COMPANY]` and the two `[YOUR EMAIL]`. Commit.
2. **Settings -> Pages -> Build and deployment -> Deploy from a branch -> main / docs -> Save.**
3. After a minute your page is at `https://YOURNAME.github.io/REPONAME/privacy-policy.html`. Open it to check. This is the **privacy policy URL** Play asks for.
   (A private repo needs a paid GitHub plan for Pages. If yours is private, make a small separate public repo just for this one page.)

## 5. Build the first signed bundle
1. Repo -> **Actions -> Release to Google Play -> Run workflow**. Leave the defaults and run it.
2. Wait for the green tick (about 6-10 minutes). Under **Artifacts** download **FinanceLedger-AAB**, unzip it: inside is `FinanceLedger.aab`.
   (The run says "No PLAY_SERVICE_ACCOUNT_JSON secret ... built but not uploaded". That is expected for now.)
3. If a step is red, open it and send me the last lines.

## 6. Fill in the Play Console (the "App content" and store listing)
Copy text and graphics from the `store-listing/` folder (`LISTING.md` plus the PNG files).

**Store listing:** name, short description, full description, app icon, feature graphic, at least 2 phone screenshots, category **Finance**, contact email, privacy policy URL.

**App content** (Policy and programs -> App content). Answer honestly, these are my suggested answers for this app as built:

| Form | Suggested answer |
|---|---|
| Privacy policy | your Pages URL from section 4 |
| Ads | No ads |
| App access | All functionality is available without sign-in or special access |
| Target audience | 18 and over only (not for children) |
| Content rating | Fill the questionnaire: no violence, no user-generated content shared with others, no gambling. Category Finance/Utility. Expect "Everyone" |
| Data safety | See the table below |
| Financial features | Declare that the app has financial features. Choose the option that matches a personal finance / budget tracker. It does not lend money, hold funds, move money or give investment advice. Form wording changes, read each option. |
| News / government / health / COVID | No |
| Advertising ID | The app does not use it |

**Data safety** (my reading of Google's rules: data that stays on the device is not "collected"):

| Question | Suggested answer |
|---|---|
| Does your app collect or share any required user data types? | **No** |
| Is data encrypted in transit / can users request deletion? | not applicable when nothing is collected |
Two things to be careful about. (a) The app loads fonts from Google Fonts, which sends the phone's IP address to Google. If you want the cleanest "No data" answer, ask me to bundle the fonts inside the app. (b) You are the one who signs these forms, so read Google's definitions and check the answers still fit if you add features later.

**Notification access.** Play does not have a special form for it, but reviewers look for (1) a clear in-app disclosure before access, which the Inbox now shows, and (2) the privacy policy. If a reviewer or form asks, say: *"Core feature: reads payment alert notifications from selected finance apps on-device to pre-fill transactions the user approves. Nothing leaves the device. Consent is requested in-app before access."*

## 7. First release (by hand) and the testing period
1. **Testing -> Internal testing -> Create new release.** Accept **Play App Signing** when asked.
2. Upload `FinanceLedger.aab`, add release notes, **Save -> Review release -> Start rollout to internal testing.**
3. **Testers** tab: add your own Gmail address, open the opt-in link on your phone, install from Play. Test it, including the notification access switch.
4. **Personal account only:** create a **Closed testing** release with the same file and add **12 or more testers** (a Google Group or a list of emails). They must opt in and stay opted in for **14 days**. Then **Dashboard -> Apply for production**, answer the questions, and wait for review (a few days up to 7).
   Organization accounts can go straight to **Production -> Create new release**.
5. After approval, **Production -> Create new release**, upload (or promote) the bundle, and roll out.

## 8. Let GitHub upload every later update
Do this after the app exists in Play Console and the first release is in.

1. Go to console.cloud.google.com, create a project (any name), then **APIs & Services -> Library -> Google Play Android Developer API -> Enable**.
2. **IAM & Admin -> Service accounts -> Create service account** (name it `play-publisher`, skip the optional steps).
3. Open it -> **Keys -> Add key -> Create new key -> JSON**. A file downloads. This file is a password, keep it private.
4. Play Console -> **Users and permissions -> Invite new users**. Paste the service account's email (ends in `iam.gserviceaccount.com`).
   Under **App permissions** add Finance Ledger, and tick **Release apps to testing tracks** (and **Release to production** if you want that too). Send the invite.
5. GitHub -> **Settings -> Secrets -> New repository secret:** name `PLAY_SERVICE_ACCOUNT_JSON`, value = the **entire contents** of the JSON file.
6. It can take up to a few hours for Google to start accepting the new service account.

## 9. Releasing an update
1. Change `www/index.html` (or anything else) and commit.
2. **Actions -> Release to Google Play -> Run workflow**, pick:
   * **track**: `internal` (just you), `alpha` (closed testing), `beta` (open testing) or `production`
   * **status**: `draft` means the file lands in Play Console and you press *Release* yourself. `completed` releases it straight away. Use `draft` until you trust the flow. Play only accepts `draft` while the app is still unpublished.
   * **version_name**: optional, shown to users
3. The version number goes up automatically with each run, which Play requires.
You can also push a tag such as `v1.1.0` to start the same workflow.

The debug workflow (`Build Android APK`) still runs on every commit and gives you a quick installable APK for your own phone. Only `Release to Google Play` builds the Play version.

## 10. Moving from your current sideloaded app to the Play version
Your data lives inside each app. Move it with the Excel export:
* **If you chose a new app id (section 1):** install the Play version next to the old app. In the old app: Settings -> Export to Excel. In the new one: Settings -> Import. Then delete the old app.
* **If you kept `com.financeledger.app`:** Android will refuse to install over a differently-signed copy. Export to Excel first, **uninstall** the old app, install from Play, then import.
Inbox items that are still waiting are not in the Excel file, so approve or reject them before you export.
Bonus: the Play version does not get the "risky app" warning that Play Protect can show for a sideloaded app that reads notifications.

## 11. Troubleshooting

| Message | Fix |
|---|---|
| Workflow stops at "Check the signing secrets exist" | One of the four `ANDROID_...` secrets is missing or misspelled (section 3) |
| `jarsigner: ... password was incorrect` | Wrong `ANDROID_KEYSTORE_PASSWORD` / `ANDROID_KEY_PASSWORD` / alias |
| Play: "Your app targets API level 34..." | You are uploading an old file. Use the one from the latest run. `variables.gradle` must show 36 in the "Show the SDK levels" step |
| Play: "Version code N has already been used" | Run the workflow again (the number increases each run), or upload from a newer run |
| Play: "Package name already exists / is taken" | Choose another `appId` (section 1) before the first upload |
| Play: "The Android App Bundle was signed with the wrong key" | The upload key must be the same one every time. Use the same four secrets. If lost, ask Play Console -> App integrity -> upload key reset |
| Upload step: "The caller does not have permission" / 403 | Service account not invited with release permission yet (section 8, step 4), or wait a few hours |
| Upload step: "Only releases with status draft may be created on draft app" | Run with status `draft` until the app is published |
| Red X at "Add notification capture" or "Set the version" | Open the step and send me the last lines |
| Notification switch is greyed out (sideloaded APK only) | Settings -> Apps -> Finance Ledger -> three dots -> Allow restricted settings. Not needed for the Play version |

## 12. What I could and could not check
I ran the new Inbox, consent screen and tab bar in a browser with simulated Android insets, tested the version and notification scripts against mock project files, validated both workflow files, and checked the Java by hand. I could **not** run the GitHub build or the Play Console here, so the first run on your repo is the real test. If any step fails, send me the log lines and I will fix it.
