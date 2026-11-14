// OPTIONAL. Lets you keep the Google sign-in IDs out of the GitHub repo.
//
// Put the three values in GitHub -> Settings -> Secrets and variables -> Actions, with these names:
//   GOOGLE_WEB_CLIENT_ID        (OAuth client of type "Web application", used by the website)
//   GOOGLE_DEVICE_CLIENT_ID     (OAuth client of type "TVs and Limited Input devices", used by the Android app)
//   GOOGLE_DEVICE_CLIENT_SECRET (the secret of that same client)
// The workflows run this script on their own copy of the app before building. It replaces the values of the
// `const GOOGLE = {...}` line in www/index.html with the secrets and changes nothing in your repository.
//
// If none of the three secrets is set, the script does nothing and the values written in index.html are used,
// so the app keeps working exactly as before. To rotate the client: create a new OAuth client in Google Cloud,
// store the new values as secrets, and then empty the three values in index.html.
const fs = require('fs');
const file = 'www/index.html';
const wanted = {
  webClientId: process.env.GOOGLE_WEB_CLIENT_ID,
  deviceClientId: process.env.GOOGLE_DEVICE_CLIENT_ID,
  deviceClientSecret: process.env.GOOGLE_DEVICE_CLIENT_SECRET,
};
const given = Object.entries(wanted).filter(([, v]) => v && v.trim());
if (!given.length) { console.log('inject-google: no GOOGLE_* secrets set, keeping the values written in index.html'); process.exit(0); }

let html = fs.readFileSync(file, 'utf8');
for (const [key, raw] of given) {
  const value = raw.trim();
  if (!/^[A-Za-z0-9._~-]+$/.test(value)) { console.error('inject-google: the value for ' + key + ' has characters that are not allowed (use only letters, digits . _ ~ -)'); process.exit(1); }
  const re = new RegExp("(\\b" + key + "\\s*:\\s*)'[^']*'");
  if (!re.test(html)) { console.error('inject-google: could not find ' + key + ' in ' + file); process.exit(1); }
  html = html.replace(re, (m, head) => head + "'" + value + "'");
}
fs.writeFileSync(file, html);
console.log('inject-google: replaced ' + given.length + ' value(s) from GitHub Secrets (' + given.map(([k]) => k).join(', ') + ')');
