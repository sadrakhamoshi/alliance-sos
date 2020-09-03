package com.example.alliancesos.DeviceAlarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.alliancesos.MainActivity;
import com.example.alliancesos.R;
import com.example.alliancesos.SendNotificationPack.NotificationResponseActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MyAlarmService extends BroadcastReceiver {

    Context mContext;
    Ringtone mRingtone;

    private void playRingtone() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(mContext, alert);
        mRingtone.play();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mRingtone.isPlaying()) {
                    mRingtone.stop();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 4000);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        if (intent.getAction().equals("com.example.helloandroid.alarms")) {
            Toast.makeText(context, "time is up!!!!.",
                    Toast.LENGTH_LONG).show();
            // Vibrate the mobile phone
            Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(2000);
            playRingtone();
        }
    }
}
