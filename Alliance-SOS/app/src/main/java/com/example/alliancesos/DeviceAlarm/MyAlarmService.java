package com.example.alliancesos.DeviceAlarm;

import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Timer;
import java.util.TimerTask;

public class MyAlarmService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        playRingtone();
        return super.onStartCommand(intent, flags, startId);
    }

    private void playRingtone() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        final Ringtone mRingtone = RingtoneManager.getRingtone(this, alert);
        mRingtone.play();
        Log.v("log", "reach2");
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
}
