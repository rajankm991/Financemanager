package __APP_ID__;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Small queue in SharedPreferences, so alerts that arrive while the app is closed are not lost. */
public class NotifStore {
    private static final String PREFS = "notif_inbox";
    private static final String KEY = "queue";
    private static final int MAX = 200;

    public static synchronized void add(Context ctx, JSONObject item) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        try {
            JSONArray arr = new JSONArray(p.getString(KEY, "[]"));
            arr.put(item);
            JSONArray keep = new JSONArray();
            for (int i = Math.max(0, arr.length() - MAX); i < arr.length(); i++) keep.put(arr.get(i));
            p.edit().putString(KEY, keep.toString()).apply();
        } catch (JSONException e) {
            // corrupt queue: start again with just this item
            p.edit().putString(KEY, new JSONArray().put(item).toString()).apply();
        }
    }

    /** Returns everything queued (as a JSON array string) and empties the queue. */
    public static synchronized String drain(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String s = p.getString(KEY, "[]");
        p.edit().putString(KEY, "[]").apply();
        return s == null ? "[]" : s;
    }
}
