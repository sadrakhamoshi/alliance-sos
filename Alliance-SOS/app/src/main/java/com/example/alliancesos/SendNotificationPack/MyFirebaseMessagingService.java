package com.example.alliancesos.SendNotificationPack;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.alliancesos.MainActivity;
import com.example.alliancesos.R;
import com.example.alliancesos.Utils.MessageType;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Timer;
import java.util.TimerTask;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private Vibrator mVibrator;
    private Ringtone mRingtone;

    Integer type;
    Integer notificationIcon;
    Integer notificationColor;
    String title, message;
    String groupName, makeBy;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        type = Integer.valueOf(remoteMessage.getData().get("type"));
        //?BaseContext
        Context context = getApplicationContext();
        Initialize(remoteMessage);

        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(2000);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), groupName)
                        .setColorized(true)
                        .setColor(notificationColor)
                        .setContentTitle(title)
                        .setSmallIcon(notificationIcon)
                        .setContentText(message)
                        .setContentIntent(pendingIntent)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);

        if (type == MessageType.SOS_TYPE) {
            playRingtone();
        }
    }

    private void Initialize(RemoteMessage remoteMessage) {
        groupName = remoteMessage.getData().get("groupName");
        makeBy = remoteMessage.getData().get("makeBy");

        if (type == MessageType.SOS_TYPE) {

            notificationColor = Color.RED;
            notificationIcon = R.drawable.sos_icon;

            title = "Emergency Moment !!!!";
            message = "SOS button has clicked by " + makeBy + " from " + groupName;

        } else if (type == MessageType.NOTIFICATION_TYPE) {

            notificationColor = Color.YELLOW;
            notificationIcon = R.drawable.notif_icon;
            title = "New Schedule ...";
            message = makeBy + " Created New Schedule in " + groupName + ". "
                    + "Are You going to join the group?";

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
