package com.example.alliancesos.DeviceAlarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MyAlarmService extends BroadcastReceiver {

    Vibrator mVibrator;
    Context mContext;
    Ringtone mRingtone;


    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        playRingtone();
        Toast.makeText(context, "OnReceive alarm test", Toast.LENGTH_SHORT).show();
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(1000);
    }

    private void playRingtone() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(mContext, alert);
        mRingtone.play();
        TimerTask task=new TimerTask() {
            @Override
            public void run() {
                if(mRingtone.isPlaying()){
                    mRingtone.stop();
                }
            }
        };
        Timer timer=new Timer();
        timer.schedule(task,4000);
    }
}
