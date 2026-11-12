// Sets versionCode / versionName in the generated Android project.
// Usage: VERSION_CODE=12 VERSION_NAME=1.12 node scripts/set-version.js
const fs = require('fs'), path = require('path');
const code = process.env.VERSION_CODE, name = process.env.VERSION_NAME;
if (!/^\d+$/.test(code || '') || !name) { console.error('Set VERSION_CODE (number) and VERSION_NAME'); process.exit(1); }
const f = path.join(__dirname, '..', 'android', 'app', 'build.gradle');
let g = fs.readFileSync(f, 'utf8');
const a = /versionCode\s*=?\s*\d+/, b = /versionName\s*=?\s*["'][^"']*["']/;
if (!a.test(g) || !b.test(g)) { console.error('versionCode / versionName not found in android/app/build.gradle'); console.error(g.split('\n').filter(l => /version/i.test(l)).join('\n')); process.exit(1); }
g = g.replace(a, 'versionCode ' + code).replace(b, 'versionName "' + name + '"');
fs.writeFileSync(f, g);
console.log('versionCode ' + code + ', versionName ' + name);
