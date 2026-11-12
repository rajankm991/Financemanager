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

    /** Returns everything queued (as a JSON array string) WITHOUT removing it. Call ack() once the items are saved. */
    public static synchronized String peek(Context ctx) {
        String s = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "[]");
        return s == null ? "[]" : s;
    }

    /** Removes the first count items (the ones peek() returned). Alerts that arrived since then stay queued. */
    public static synchronized void ack(Context ctx, int count) {
        if (count <= 0) return;
        SharedPreferences p = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        try {
            JSONArray arr = new JSONArray(p.getString(KEY, "[]"));
            JSONArray rest = new JSONArray();
            for (int i = count; i < arr.length(); i++) rest.put(arr.get(i));
            p.edit().putString(KEY, rest.toString()).apply();
        } catch (JSONException e) {
            p.edit().putString(KEY, "[]").apply();
        }
    }
}
