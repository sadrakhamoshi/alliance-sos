package com.example.alliancesos.DeviceAlarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.widget.Toast;

public class MyAlarmService extends BroadcastReceiver {

    Vibrator mVibrator;
    Context mContext;
    Ringtone ringtone;
    String title;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtone = RingtoneManager.getRingtone(mContext, alert);
        ringtone.play();
        title = "title";
        Toast.makeText(context, "OnReceive alarm test", Toast.LENGTH_SHORT).show();

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(300);
        Toast.makeText(context, title, Toast.LENGTH_LONG).show();
    }
}
