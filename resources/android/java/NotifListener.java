package __APP_ID__;

import android.app.Notification;
import android.content.ComponentName;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 * Captures payment notifications from supported banking/payment/SMS apps.
 *
 * The listener intentionally reads all common Android notification text fields
 * because Samsung Messages / Google Messages may expose the SMS body through
 * different extras depending on the One UI / Android version.
 */
public class NotifListener extends NotificationListenerService {

    private static final Map<String, String> WATCHED = new HashMap<>();

    static {
        WATCHED.put("com.google.android.apps.nbu.paisa.user", "Google Pay");
        WATCHED.put("com.phonepe.app", "PhonePe");
        WATCHED.put("net.one97.paytm", "Paytm");
        WATCHED.put("com.dreamplug.androidapp", "CRED");
        WATCHED.put("com.csam.icici.bank.imobile", "ICICI iMobile");
        WATCHED.put("com.sbi.lotusintouch", "YONO SBI");
        WATCHED.put("com.sbi.SBIFreedomPlus", "YONO Lite SBI");
        WATCHED.put("com.sbi.upi", "BHIM SBI Pay");
        WATCHED.put("indwin.c3.shareapp", "slice");

        // SMS / Messages notification sources.
        WATCHED.put("com.google.android.apps.messaging", "SMS");
        WATCHED.put("com.samsung.android.messaging", "SMS");
        WATCHED.put("com.android.mms", "SMS");

        WATCHED.put("in.org.npci.upiapp", "BHIM");
        WATCHED.put("com.snapwork.hdfc", "HDFC Bank");
        WATCHED.put("com.axis.mobile", "Axis Mobile");
        WATCHED.put("com.msf.kbank.mobile", "Kotak");
    }

    private static final Pattern HAS_AMOUNT = Pattern.compile(
            "(\\b(rs|inr)\\.?|\\u20B9)\\s*[\\d,]*\\d",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern HAS_VERB = Pattern.compile(
            "\\b(debited|credited|spent|paid|sent|received|deposited|withdrawn|"
                    + "purchased?|refund(ed)?|transferred|successful|successfully)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OTP_WORD = Pattern.compile(
            "\\b(otp|one[- ]time password|verification code)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OTP_CODE = Pattern.compile(
            "\\b(otp|one[- ]time password|verification code)\\b\\W{0,12}"
                    + "(is\\W{1,3})?\\d{4,8}\\b"
                    + "|\\b\\d{4,8}\\s+is\\s+(your|the)\\s+"
                    + "(otp|one[- ]time password|verification code)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern POSTED = Pattern.compile(
            "\\b(debited|credited)\\b",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if (sbn == null) return;

            String packageName = sbn.getPackageName();
            String app = WATCHED.get(packageName);
            if (app == null) return;

            Notification notification = sbn.getNotification();
            if (notification == null || notification.extras == null) return;

            // Ignore notification group summary rows. The actual child
            // notification contains the transaction text we need.
            if ((notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;

            Bundle extras = notification.extras;

            String title = str(
                    extras.getCharSequence(Notification.EXTRA_TITLE)
            );

            /*
             * Samsung Messages and Google Messages can put the SMS body in
             * different fields. Read ALL common fields rather than selecting
             * only EXTRA_BIG_TEXT or EXTRA_TEXT.
             */
            StringBuilder body = new StringBuilder();

            appendText(body, extras.getCharSequence(Notification.EXTRA_TEXT));
            appendText(body, extras.getCharSequence(Notification.EXTRA_BIG_TEXT));

            // Multi-line notifications.
            CharSequence[] textLines = extras.getCharSequenceArray(
                    Notification.EXTRA_TEXT_LINES
            );

            if (textLines != null) {
                for (CharSequence line : textLines) {
                    appendText(body, line);
                }
            }

            // MessagingStyle notifications may expose individual messages.
            // This is especially useful for Google/Samsung Messages on some
            // Android/One UI versions.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    android.os.Parcelable[] messages =
                            extras.getParcelableArray(Notification.EXTRA_MESSAGES);

                    if (messages != null) {
                        for (android.os.Parcelable item : messages) {
                            if (item instanceof Bundle) {
                                Bundle message = (Bundle) item;

                                appendText(
                                        body,
                                        message.getCharSequence("text")
                                );

                                appendText(
                                        body,
                                        message.getCharSequence("sender")
                                );
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // Some notification providers use a different parcelable
                    // representation. Other fields are still sufficient.
                }
            }

            String text = body.toString().trim();
            String all = (title + " " + text).trim();

            if (all.isEmpty()) return;

            // OTPs are never stored as transactions.
            if (OTP_CODE.matcher(all).find()) return;

            // A payment SMS may contain words such as "OTP" in the warning
            // section (e.g. "do not share OTP"). Keep it only when the same
            // notification is clearly a debit/credit alert.
            if (OTP_WORD.matcher(all).find() && !POSTED.matcher(all).find()) {
                return;
            }

            // Transaction must contain both an amount and a payment verb.
            if (!HAS_AMOUNT.matcher(all).find()) return;
            if (!HAS_VERB.matcher(all).find()) return;

            JSONObject o = new JSONObject();
            o.put("app", app);
            o.put("title", title);
            o.put("text", text);
            o.put("package", packageName);
            o.put("ts", sbn.getPostTime());

            NotifStore.add(getApplicationContext(), o);

        } catch (Exception ignored) {
            // Never allow one malformed notification to kill the listener.
        }
    }

    /**
     * Append a notification field without adding null/empty values.
     */
    private static void appendText(StringBuilder builder, CharSequence value) {
        if (value == null) return;

        String valueString = value.toString().trim();
        if (valueString.isEmpty()) return;

        // EXTRA_TEXT, EXTRA_BIG_TEXT and the message list often carry the same words; keep each piece once.
        if (builder.indexOf(valueString) >= 0) return;

        if (builder.length() > 0) {
            builder.append(" ");
        }

        builder.append(valueString);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
    }

    @Override
    public void onListenerDisconnected() {
        // Some phones stop notification listeners to save battery.
        // Ask Android to bind this listener again.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                NotificationListenerService.requestRebind(
                        new ComponentName(this, NotifListener.class)
                );
            }
        } catch (Exception ignored) {
        }
    }

    private static String str(CharSequence c) {
        return c == null ? "" : c.toString();
    }
}
