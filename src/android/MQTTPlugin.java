package in.eatie;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.provider.Settings;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class MQTTPlugin extends CordovaPlugin {
    public static Activity mActivity;
    protected static CallbackContext mCallbackContext;
    public static Context mApplicationContext;



    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mActivity = cordova.getActivity();
        mApplicationContext = mActivity.getApplicationContext();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        mCallbackContext = callbackContext;
        if (action.equals("connect")) {
            this.connect(args);
            return true;
        }
        if (action.equals("subscribe")) {
            this.subscribe(args);
            return true;
        }
        if (action.equals("disconnect")) {
            this.disconnect();
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }



    private Boolean isIgnoringBatteryOptimizations(){
        Boolean isIgnoringBatteryOptimizations = true;
        if(SDK_INT >= M){
            String packageName = mActivity.getPackageName();
            PowerManager powerManager = (PowerManager) mActivity.getSystemService(Context.POWER_SERVICE);
            isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName);
        }
        return isIgnoringBatteryOptimizations;
    }

    private void _connect(JSONArray args){
        Boolean canDrawOverlays = false;
        Boolean isIgnoringBatteryOptimizations = true;
        if (SDK_INT >= M) {
            canDrawOverlays = Settings.canDrawOverlays(mApplicationContext);
            isIgnoringBatteryOptimizations = this.isIgnoringBatteryOptimizations();
        }
        if(!canDrawOverlays ||!isIgnoringBatteryOptimizations ){
            mCallbackContext.error("Please enable overlay");
            return;
        }
       MQTTService.startService(args);
        mCallbackContext.success("OK");
    }

    private void connect(JSONArray args){
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                _connect(args);
            }
        });
    }

    public void disconnect(){
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                MQTTService.stopService();
            }
        });
    }

    private void _subscribe(JSONArray args) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        String topic = options.getString("topic");
        int qos = options.getInt( "qos");
        MQTTService.subscribe(topic,qos);
    }

    private void subscribe(JSONArray args){
        cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    _subscribe(args);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
