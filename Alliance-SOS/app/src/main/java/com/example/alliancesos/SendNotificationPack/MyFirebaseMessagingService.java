package com.example.alliancesos.SendNotificationPack;

import android.content.Context;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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

        Initialize(remoteMessage);

        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.vibrate(1000);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), groupName)
                        .setContentTitle(title)
                        .setSmallIcon(notificationIcon)
                        .setColorized(true)
                        .setColor(notificationColor)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.InboxStyle()
                                .addLine(message)
                                .addLine(message));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());

        if (type == MessageType.SOS_TYPE) {
            playRingtone();
        }
    }

    private void Initialize(RemoteMessage remoteMessage) {
        groupName = remoteMessage.getData().get("groupName");
        makeBy = remoteMessage.getData().get("makeBy");

        if (type == MessageType.SOS_TYPE) {

            notificationIcon = R.drawable.sos_icon;
            notificationColor = Color.RED;
            title = "Emergency Moment !!!!";
            message = "SOS button has clicked by " + makeBy + " from " + groupName;

        } else if (type == MessageType.NOTIFICATION_TYPE) {

            notificationIcon = R.drawable.notif_icon;
            notificationColor = Color.YELLOW;
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
