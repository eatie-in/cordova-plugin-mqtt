package in.vishnu.mqttdemo;

import android.content.ContentResolver;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

public class Utils {
    private static Utils utils;
    private Context context;
    private Vibrator mVibrator;
    private Ringtone mRingtone;
    Utils(Context context){
        this.context = context;
        getServices();
    }
    private void getServices(){
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        getSound();
    }
    private void getSound() {
        String mPackageName = context.getPackageName();
        String mAudioName = "";
        int checkExistence = context.getResources().getIdentifier("audio", "raw", mPackageName);
        if (checkExistence != 0) {
            Uri soundPath = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mPackageName + "/raw/" + mAudioName);
            mRingtone = RingtoneManager.getRingtone(context, soundPath);
        } else {
            // ringtone
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mRingtone = RingtoneManager.getRingtone(context, notification);
        }
    }
    public  void vibrate(long seconds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVibrator.vibrate(VibrationEffect.createOneShot(seconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            mVibrator.vibrate(seconds);
        }
    }
    public void stopVibration(){
        mVibrator.cancel();
    }
    public void stopRingtone(){
        mRingtone.stop();
    }
    public void playRingtone(){
        mRingtone.play();
    }
    public static Utils getUtils(Context context){
        if(utils==null){
            utils = new Utils(context);
        }
        return utils;
    }



}
