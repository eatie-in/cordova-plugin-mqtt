package in.eatie;

import androidx.appcompat.app.AppCompatActivity;

import android.app.KeyguardManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class AlertActivity extends AppCompatActivity {
    private Context mContext;
    private static final String mLayoutName = "activity_alert";
    private static Utils utils = Utils.getUtils(App.getAppContext());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mContext = App.getAppContext();
        super.onCreate(savedInstanceState);
        wakeup();
        setContentView(getLayout());
    }

    public int getLayout() {
        int layout = mContext.getResources().getIdentifier(mLayoutName, "layout", mPackageName);
        return layout;
    }

    public void wakeup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setTurnScreenOn(true);
            setShowWhenLocked(true);
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(this.KEYGUARD_SERVICE);
            keyguardManager.requestDismissKeyguard(this, null);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    public void viewOrder(View view) {
        stopAll();
    }

    public void stopAll() {
        utils.stopRingtone();
        utils.stopVibration();
    }

    @Override
    protected void onDestroy() {
        stopAll();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        stopAll();
        super.onBackPressed();
    }
}
