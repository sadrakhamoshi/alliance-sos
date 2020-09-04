package com.example.alliancesos.SendNotificationPack;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.text.style.StyleSpan;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.alliancesos.Event;
import com.example.alliancesos.MainActivity;
import com.example.alliancesos.R;
import com.example.alliancesos.ScheduleObject;
import com.example.alliancesos.Utils.MessageType;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Timer;
import java.util.TimerTask;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    private Vibrator mVibrator;
    private Ringtone mRingtone;

    Integer type;
    Integer notificationIcon;
    Integer notificationColor;

    String toName, toId;
    String title, message;
    String groupName, groupId, makeBy;
    String eventId;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        type = Integer.valueOf(remoteMessage.getData().get("type"));

        Context context = getApplicationContext();
        Initialize(remoteMessage);

//        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
//        mVibrator.vibrate(2000);

        Intent intent = new Intent(context, NotificationResponseActivity.class);

        intent.putExtra("groupId", groupId);
        intent.putExtra("eventId", eventId);
        intent.putExtra("toName", toName);
        intent.putExtra("toId", toId);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic3);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID")
                        .setColorized(true)
                        .setColor(notificationColor)
                        .setContentTitle(title)
                        .setSmallIcon(notificationIcon)
                        .setContentText(message)
                        .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        if (type == MessageType.NOTIFICATION_TYPE) {
            builder.setSound(alarmSound);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new
                    NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.notificationChannel_name), importance);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
        notificationManager.notify(0, builder.build());

        if (type == MessageType.SOS_TYPE) {
            playRingtone();
        }
    }

    private void Initialize(RemoteMessage remoteMessage) {
        eventId = toName = toId = "";
        groupName = remoteMessage.getData().get("groupName");
        groupId = remoteMessage.getData().get("groupId");
        makeBy = remoteMessage.getData().get("makeBy");

        if (type == MessageType.SOS_TYPE) {

            notificationColor = Color.RED;
            notificationIcon = R.drawable.sos_icon;

            title = "Emergency Moment !!!!";
            message = "SOS button has clicked by " + makeBy + " from " + groupName;

        } else if (type == MessageType.NOTIFICATION_TYPE) {

            eventId = remoteMessage.getData().get("eventId");
            toId = remoteMessage.getData().get("toId");
            toName = remoteMessage.getData().get("toName");


            notificationColor = Color.YELLOW;
            notificationIcon = R.drawable.notif_icon;
            title = "New Schedule ...";
            message = makeBy + " Created New Schedule in " + groupName + " . " + "\n" + "Are You going to join the group?";

        } else {
            title = "title";
            message = "message";
            notificationColor = Color.GRAY;
            notificationIcon = R.drawable.un_known;
        }
    }

    private void playRingtone() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mRingtone = RingtoneManager.getRingtone(getBaseContext(), alert);
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
}
