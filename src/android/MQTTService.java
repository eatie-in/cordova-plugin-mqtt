package in.eatie;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.orhanobut.logger.Logger;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class MQTTService extends Service {
    public static MQTT mMqTTClient;
    private String CHANNEL_ID = "MQTT";
    private String defaultSmallIconName = "notification_icon";
    private String defaultNotificationTitle = "Ready to accept orders";
    private  static Utils utils =  Utils.getUtils(App.getAppContext());

    public static Intent getServiceIntent(final JSONArray args) throws JSONException {
        JSONObject options = args.getJSONObject(0);
        String broker = options.getString("broker");
        String clientId = options.getString("clientId");
        JSONObject connectionOptions = options.getJSONObject("options");
        Intent intent = new Intent(App.getAppContext(), MQTTService.class);
        intent.putExtra("options", connectionOptions.toString());
        intent.putExtra("broker", broker);
        intent.putExtra("clientId", clientId);
        return intent;
    }

    public static void startService(final JSONArray args) {
        try {
            Intent intent = getServiceIntent(args);
            Context context = App.getAppContext();
            ContextCompat.startForegroundService(context, intent);
            setScheduleRestarts(intent);
        } catch (JSONException e) {
            Logger.e(e.getMessage());
            e.printStackTrace();
        }
    }


    public static void setScheduleRestarts(Intent intent) {
        Context context = App.getAppContext();
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int interval = 5 * 1000;
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), interval, pendingIntent);
    }

    public static void stopService() {
        if (mMqTTClient == null) {
            Logger.w("service not running ");
            return;
        }
        try {
            mMqTTClient.disconnect();
            mMqTTClient = null;
        } catch (MqttException e) {
            Logger.e(e.getMessage());
            e.printStackTrace();
        }
        Context context = App.getAppContext();
        Intent intent = new Intent(context, MQTTService.class);
        clearScheduleRestarts(intent);
        context.stopService(intent);
    }

    public static void clearScheduleRestarts(Intent intent) {
        Context context = App.getAppContext();
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Logger.i("onCreate");
        createNotificationChannel();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.i("onStartCommand");
        try {
            connect(intent);
        } catch (JSONException | MqttException e) {
            e.printStackTrace();
            Logger.e(e.getMessage());
        }
        Notification notification = this.createNotification("connected");
        startForeground(2, notification);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Logger.w("onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Logger.w("onDestroy");
        super.onDestroy();
    }

    private void connect(Intent intent) throws JSONException, MqttException {
        if (mMqTTClient != null) {
            Logger.w("Already connected");
            return;
        }
        String broker = intent.getStringExtra("broker");
        String clientId = intent.getStringExtra("clientId");
        String optionsString = intent.getStringExtra("options");
        JSONObject options = new JSONObject(optionsString);
        mMqTTClient = new MQTT(broker, clientId);
        MqttConnectOptions connectOptions = mMqTTClient.getConnectionOptions(options);
        mMqTTClient.connect(connectOptions);
        this.onData(mMqTTClient.getClient());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Order Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    public Notification createNotification(String text) {
        Context context = getApplicationContext();
        PackageManager pm = context.getPackageManager();
        Intent notificationIntent = pm.getLaunchIntentForPackage(context.getPackageName());
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(this.defaultNotificationTitle)
                .setContentText(text)
                .setOnlyAlertOnce(true)
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        int defaultSmallIconResID = context.getResources().getIdentifier(defaultSmallIconName, "drawable", context.getPackageName());
        if (defaultSmallIconResID != 0) {
            notification.setSmallIcon(defaultSmallIconResID);
        } else {
            notification.setSmallIcon(context.getApplicationInfo().icon);
        }
        notification.setOngoing(true);
        return notification.build();
    }

    public void updateNotification(String text) {
        Context context = getApplicationContext();
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        Notification notification = createNotification(text);
        managerCompat.notify(2, notification);
    }

    private void launchAlertActivity() {
        Context context = getApplicationContext();
        String mPackageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        Intent notificationIntent = pm.getLaunchIntentForPackage(mPackageName);
        Intent intent = new Intent(context, AlertActivity.class);
        utils.vibrate(5000);
        utils.playRingtone();
        startActivities(new Intent[]{notificationIntent,intent});
    }

    private void onData(MqttClient client) {
        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                Logger.w("connectComplete reconnect" + reconnect);
                updateNotification("connected");
            }

            @Override
            public void connectionLost(Throwable cause) {
                Logger.w("connectionLost" + cause.toString());
                updateNotification("disconnected");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Logger.i("messageArrived from " + topic);
                launchAlertActivity();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Logger.i("deliveryComplete" + token.toString());
            }
        });
    }

public  static void subscribe(String topic,int qos){
    try {
        mMqTTClient.subscribe(topic,qos);
    } catch (MqttException e) {
        e.printStackTrace();
        Logger.e(e.getMessage());
    }
}
}
