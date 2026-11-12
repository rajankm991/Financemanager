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
        WATCHED.put("com.android.mms", "SMS");                       // default SMS app on many Xiaomi, Oppo, Vivo, Realme phones
        WATCHED.put("in.org.npci.upiapp", "BHIM");
        WATCHED.put("com.snapwork.hdfc", "HDFC Bank");
        WATCHED.put("com.axis.mobile", "Axis Mobile");
        WATCHED.put("com.msf.kbank.mobile", "Kotak");
        // Check each id at play.google.com/store/apps/details?id=<id> before you ship; a wrong id is simply never matched.
    }

    private static final Pattern HAS_AMOUNT = Pattern.compile("(\\b(rs|inr)\\.?|\u20B9)\\s*[\\d,]*\\d", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_VERB = Pattern.compile(
            "\\b(debited|credited|spent|paid|sent|received|deposited|withdrawn|purchased?|refund(ed)?|transferred|successful|successfully)\\b", Pattern.CASE_INSENSITIVE);
    // one-time passwords are never payments, and must never be kept. A debit alert that merely ends with
    // "never share OTP" is still a payment, so only a message with the code, or an OTP word and no debited/credited, is dropped.
    private static final Pattern OTP_WORD = Pattern.compile(
            "\\b(otp|one[- ]time password|verification code)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern OTP_CODE = Pattern.compile(
            "\\b(otp|one[- ]time password|verification code)\\b\\W{0,12}(is\\W{1,3})?\\d{4,8}\\b"
            + "|\\b\\d{4,8}\\s+is\\s+(your|the)\\s+(otp|one[- ]time password|verification code)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern POSTED = Pattern.compile("\\b(debited|credited)\\b", Pattern.CASE_INSENSITIVE);

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
            if (OTP_CODE.matcher(all).find()) return;
            if (OTP_WORD.matcher(all).find() && !POSTED.matcher(all).find()) return;
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

    @Override
    public void onListenerDisconnected() {
        // Some phones stop the listener to save battery; ask Android to bind it again.
        try {
            if (Build.VERSION.SDK_INT >= 24) NotificationListenerService.requestRebind(new ComponentName(this, NotifListener.class));
        } catch (Exception ignored) {
        }
    }

    private static String str(CharSequence c) { return c == null ? "" : c.toString(); }
}
