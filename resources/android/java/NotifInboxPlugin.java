package __APP_ID__;

import android.content.Intent;
import android.provider.Settings;
import androidx.core.app.NotificationManagerCompat;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONException;

@CapacitorPlugin(name = "NotifInbox")
public class NotifInboxPlugin extends Plugin {

    @PluginMethod
    public void status(PluginCall call) {
        boolean on = NotificationManagerCompat.getEnabledListenerPackages(getContext()).contains(getContext().getPackageName());
        JSObject r = new JSObject();
        r.put("enabled", on);
        call.resolve(r);
    }

    @PluginMethod
    public void openSettings(PluginCall call) {
        Intent i = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getContext().startActivity(i);
        call.resolve();
    }

    @PluginMethod
    public void drain(PluginCall call) {
        JSObject r = new JSObject();
        try {
            r.put("items", new JSArray(NotifStore.drain(getContext())));
        } catch (JSONException e) {
            r.put("items", new JSArray());
        }
        call.resolve(r);
    }
}
