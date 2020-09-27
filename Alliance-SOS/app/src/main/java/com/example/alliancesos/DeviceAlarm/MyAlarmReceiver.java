package com.example.alliancesos.DeviceAlarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.alliancesos.MainActivity;
import com.example.alliancesos.R;
import com.example.alliancesos.SendNotificationPack.NotificationResponseActivity;
import com.example.alliancesos.Utils.AlarmType;
import com.example.alliancesos.Utils.MessageType;
import com.google.android.gms.common.api.internal.IStatusCallback;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MyAlarmReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID = "10002";
    Context mContext;
    Ringtone mRingtone;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("loggg", "reach3");
        mContext = context;
        new Thread(new Runnable() {
            @Override
            public void run() {
                makeNotification();
            }
        }).start();

        int ringOrNotify = intent.getIntExtra("ringEnable", AlarmType.NOTIFICATION);
        if (ringOrNotify == AlarmType.RING) {
            playRingtone();
        }

    }

    private void playRingtone() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mRingtone = RingtoneManager.getRingtone(mContext, alert);
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


    private void makeNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext, "CHANNEL_ID2")
                        .setContentTitle("You have Event...")
                        .setSmallIcon(R.drawable.sos_icon)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentText("Please Check your App You Have event ...")
                        .setVibrate(new long[]{500, 200, 300, 400, 500, 1000})
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Please Check your App You Have event ..."));

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new
                    NotificationChannel(NOTIFICATION_CHANNEL_ID, "notificationChannel_id", importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Notification notification = builder.build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        Log.v("log", "reach1");
        notificationManager.notify(0, notification);
    }
}
