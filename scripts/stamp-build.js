// Gives the web app and the Android app the SAME build id, taken from the git commit.
// Run it on a CI checkout before building (it edits www/ only in that temporary copy, nothing is committed).
// Usage: node scripts/stamp-build.js [folder, default www]
const fs = require('fs'), path = require('path'), cp = require('child_process');
const dir = path.join(__dirname, '..', process.argv[2] || 'www');
const sh = c => { try { return cp.execSync(c, {encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore']}).trim(); } catch (e) { return ''; } };
const count = sh('git rev-list --count HEAD'), sha = (process.env.GITHUB_SHA || sh('git rev-parse HEAD')).slice(0, 7);
const id = count && sha ? 'c' + count + '-' + sha : 'local';

const idx = path.join(dir, 'index.html');
let html = fs.readFileSync(idx, 'utf8');
if (!/<meta name="build" content="[^"]*">/.test(html)) { console.error('index.html has no <meta name="build"> tag'); process.exit(1); }
html = html.replace(/(<meta name="build" content=")[^"]*(">)/, '$1' + id + '$2');
fs.writeFileSync(idx, html);

const swf = path.join(dir, 'sw.js');
if (fs.existsSync(swf)) {            // a new build id means a new cache name, so the web app never serves an old copy
  let sw = fs.readFileSync(swf, 'utf8');
  sw = sw.replace(/const CACHE = '[^']*'/, "const CACHE = 'finance-ledger-" + id + "'");
  fs.writeFileSync(swf, sw);
}
console.log('build id ' + id + ' written to ' + path.relative(process.cwd(), dir));
