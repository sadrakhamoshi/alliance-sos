package com.kaya.alliancesos.DeviceAlarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.kaya.alliancesos.Event;
import com.kaya.alliancesos.MainActivity;
import com.kaya.alliancesos.R;
import com.kaya.alliancesos.SpecificEventActivity;
import com.kaya.alliancesos.Utils.AlarmType;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.NOTIFICATION_SERVICE;


public class MyAlarmReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_CHANNEL_ID = "10002";
    Context mContext;
    public static Ringtone ringtone;
    private Event event;
    private String groupId;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Gson gson = new Gson();
        event = gson.fromJson(intent.getStringExtra("myjson"), Event.class);
        groupId = intent.getStringExtra("groupId");
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
        ringtone = RingtoneManager.getRingtone(mContext, alert);
        if (ringtone.isPlaying())
            ringtone.stop();
        ringtone.play();
        Log.v("log", "reach2");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (ringtone.isPlaying()) {
                    ringtone.stop();
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 100000);
    }


    private void makeNotification() {
        String msg = "Please Check your App You Have event named " + event.getScheduleObject().getTitle() + " from " + groupId;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext, "CHANNEL_ID2")
                        .setContentTitle("You have Event...")
                        .setSmallIcon(R.drawable.sos_icon)
                        .setOngoing(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(PendingIntent.getActivity(mContext, 1, toSpecificEvent(), PendingIntent.FLAG_UPDATE_CURRENT))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(msg)
                .setVibrate(new long[]{500, 200, 300, 400, 500, 1000, 200, 220, 100, 250})
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg));

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
        notificationManager.notify(0, notification);
    }

    private Intent toSpecificEvent() {
        Intent intent = new Intent(mContext, SpecificEventActivity.class);
        intent.putExtra("groupId", groupId);
        intent.putExtra("event", event);
        return intent;
    }
}
