package __APP_ID__;

import android.app.Notification;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.json.JSONObject;

/**
 * Keeps only payment alerts from the apps listed below. Anything else is ignored at once and never stored.
 * To watch another app, add its package name (the id at the end of its Play Store URL) to the static block.
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
        WATCHED.put("com.google.android.apps.messaging", "SMS");     // bank SMS alerts
        WATCHED.put("com.samsung.android.messaging", "SMS");         // bank SMS alerts
    }

    private static final Pattern HAS_AMOUNT = Pattern.compile("(rs\\.?|inr|\u20B9)\\s*[\\d,]+", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_VERB = Pattern.compile(
            "\\b(debited|credited|spent|paid|sent|received|deposited|withdrawn|purchased?|refund(ed)?|transferred|successful|successfully)\\b", Pattern.CASE_INSENSITIVE);
    // one-time passwords are never payments, and must never be kept
    private static final Pattern IS_OTP = Pattern.compile(
            "\\b(otp|one[- ]time password|verification code|do not share|never share)\\b", Pattern.CASE_INSENSITIVE);

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if (sbn == null) return;
            String app = WATCHED.get(sbn.getPackageName());
            if (app == null) return;
            Notification n = sbn.getNotification();
            if (n == null || n.extras == null) return;
            if ((n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;

            Bundle ex = n.extras;
            String title = str(ex.getCharSequence(Notification.EXTRA_TITLE));
            CharSequence big = ex.getCharSequence(Notification.EXTRA_BIG_TEXT);
            String text = str(big != null ? big : ex.getCharSequence(Notification.EXTRA_TEXT));
            String all = title + " " + text;
            if (IS_OTP.matcher(all).find()) return;
            if (!HAS_AMOUNT.matcher(all).find() || !HAS_VERB.matcher(all).find()) return;

            JSONObject o = new JSONObject();
            o.put("app", app);
            o.put("title", title);
            o.put("text", text);
            o.put("ts", sbn.getPostTime());
            NotifStore.add(getApplicationContext(), o);
        } catch (Exception ignored) {
            // never let one odd notification crash the listener
        }
    }

    private static String str(CharSequence c) { return c == null ? "" : c.toString(); }
}
