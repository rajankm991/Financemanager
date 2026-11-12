// Copies the app icons into the generated Android project (run after `npx cap add android`).
const fs = require('fs'), path = require('path');
const from = path.join(__dirname, '..', 'resources', 'android', 'res'), to = path.join(__dirname, '..', 'android', 'app', 'src', 'main', 'res');
const copy = (a, b) => { fs.mkdirSync(b, { recursive: true }); for (const f of fs.readdirSync(a)) { const s = path.join(a, f), d = path.join(b, f); fs.statSync(s).isDirectory() ? copy(s, d) : fs.copyFileSync(s, d); } };
if (!fs.existsSync(to)) { console.error('android/ project not found - run "npx cap add android" first'); process.exit(1); }
copy(from, to); console.log('icons applied');
