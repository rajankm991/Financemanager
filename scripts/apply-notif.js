// Adds the notification-capture code to the generated Android project (run after `npx cap add android`).
// Copies the Java files, replaces MainActivity so it registers the plugin, and declares the listener service.
const fs = require('fs'), path = require('path');
// NOTIF_CAPTURE=0 builds the "Lite" app: no notification listener at all. Google Play Protect blocks sideloaded apps
// that declare one ("App blocked to protect your device"), so the Lite APK is the one that installs from a download.
if (process.env.NOTIF_CAPTURE === '0') { console.log('Lite build: notification capture NOT added (installs without the Play Protect block).'); process.exit(0); }
const root = path.join(__dirname, '..');
const appId = JSON.parse(fs.readFileSync(path.join(root, 'capacitor.config.json'), 'utf8')).appId;
const main = path.join(root, 'android', 'app', 'src', 'main');
if (!fs.existsSync(main)) { console.error('android/ project not found - run "npx cap add android" first'); process.exit(1); }

const dest = path.join(main, 'java', ...appId.split('.'));
fs.mkdirSync(dest, { recursive: true });
const src = path.join(root, 'resources', 'android', 'java');
for (const f of fs.readdirSync(src)) {
  fs.writeFileSync(path.join(dest, f), fs.readFileSync(path.join(src, f), 'utf8').replace(/__APP_ID__/g, appId));
}

const mf = path.join(main, 'AndroidManifest.xml');
let xml = fs.readFileSync(mf, 'utf8');
if (!xml.includes('.NotifListener')) {
  const svc = `
        <service
            android:name=".NotifListener"
            android:label="Finance Ledger"
            android:exported="true"
            android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
            <intent-filter>
                <action android:name="android.service.notification.NotificationListenerService" />
            </intent-filter>
        </service>
    </application>`;
  if (!xml.includes('</application>')) { console.error('AndroidManifest.xml has no </application>'); process.exit(1); }
  xml = xml.replace('</application>', svc);
  fs.writeFileSync(mf, xml);
}
console.log('notification capture added for ' + appId);
