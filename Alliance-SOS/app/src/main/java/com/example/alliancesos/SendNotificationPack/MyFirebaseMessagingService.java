package com.example.alliancesos.SendNotificationPack;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import com.example.alliancesos.DbForRingtone.AppDatabase;
import com.example.alliancesos.R;
import com.example.alliancesos.Utils.MessageType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.example.alliancesos.DoNotDisturb.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    private DatabaseReference mRootRef;
    private String mUserId;

    private Context mContext;

    private Ringtone mRingtone;

    Integer type;
    Integer notificationIcon;
    Integer notificationColor;

    String toName, toId;
    String title, message;
    String groupName, groupId, makeBy;
    String fromTimezoneId;
    Uri uri;
    String eventId;
    private AppDatabase appDatabase2;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        type = Integer.valueOf(remoteMessage.getData().get("type"));

        mContext = getApplicationContext();
        appDatabase2 = Room.databaseBuilder(mContext, AppDatabase.class, "ringtone").build();

        if (checkDoNotDisturb()) {
            Initialize(remoteMessage);

            buildNotification(mContext);

            if (type == MessageType.SOS_TYPE) {
                playRingtone();
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        try {
            mRootRef = FirebaseDatabase.getInstance().getReference();
            mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            changeUserToken(s);

        } catch (Exception e) {
//            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void changeUserToken(String s) {
        Token token = new Token(s);
        mRootRef.child("users").child(mUserId).child("token").setValue(token.getToken()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialog);
                    builder.setTitle("Alert");
                    builder.setMessage("The token didn't updated ... check please" + "\n" + task.getException());
                    builder.setIcon(R.drawable.sos_icon);
                    builder.setNegativeButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.create().show();
                }
            }
        });
    }

    private void Initialize(RemoteMessage remoteMessage) {
        eventId = toName = toId = "";
        groupName = remoteMessage.getData().get("groupName");
        groupId = remoteMessage.getData().get("groupId");
        makeBy = remoteMessage.getData().get("makeBy");
        toId = remoteMessage.getData().get("toId");

//        String path = appDatabase.dao().currentPath(toId).path;
        if (type == MessageType.SOS_TYPE) {

            notificationColor = Color.RED;
            notificationIcon = R.drawable.sos_icon;

            title = "Emergency Moment !!!!";
            message = "SOS button has clicked by " + makeBy + " from " + groupName;

        } else if (type == MessageType.NOTIFICATION_TYPE) {

            eventId = remoteMessage.getData().get("eventId");
            toName = remoteMessage.getData().get("toName");
            notificationColor = Color.YELLOW;
            notificationIcon = R.drawable.notif_icon;
            title = "New Schedule ...";
            message = makeBy + " Created New Schedule in " + groupName + " . " + "\n" + "Are You going to join the group?";

        } else if (type == MessageType.INVITATION_TYPE) {
            title = "Invitation";
            message = "You Added to " + groupName + " By " + makeBy + ". \n" + "Click Here to Set Your Name.";
            notificationColor = Color.RED;
            notificationIcon = R.drawable.add_group_icon;
        }
    }

    private void buildNotification(Context context) {

        PendingIntent pendingIntent = getPendingIntent(context);

        //Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic3);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), "CHANNEL_ID")
                        .setColorized(true)
                        .setColor(notificationColor)
                        .setContentTitle(title)
                        .setSmallIcon(notificationIcon)
                        .setContentText(message)
                        .setVibrate(new long[]{500, 200, 300, 400, 500, 200, 400, 300, 200, 400, 200})
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
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

    private PendingIntent getPendingIntent(Context context) {
        if (type == MessageType.NOTIFICATION_TYPE || type == MessageType.SOS_TYPE) {
            Intent intent = new Intent(context, NotificationResponseActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("eventId", eventId);
            intent.putExtra("toName", toName);
            intent.putExtra("toId", toId);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent intent = new Intent(context, InvitationResponseActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("groupName", groupName);
            intent.putExtra("toId", toId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void playRingtone() {
        Uri alert = Uri.parse(appDatabase2.dao().currentPath(toId).path);
        if (alert != null) {
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
            timer.schedule(task, 9000);
        } else {
            throw new NullPointerException();
        }
    }

    public boolean checkDoNotDisturb() {
//        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
//        Calendar calendar = Calendar.getInstance();
//        Date dateTime = calendar.getTime();
//        String dayOfWeek = sdf.format(dateTime);
//
//        List<notDisturbObject> allRules = appDatabase.disturbDao().getAllRules();
//        for (notDisturbObject object :
//                allRules) {
//
//            String day = object.day;
//            if (day.equals(dayOfWeek)) {
//                mRingtone = RingtoneManager.getRingtone(getBaseContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
//                mRingtone.play();
//                TimerTask task = new TimerTask() {
//                    @Override
//                    public void run() {
//                        if (mRingtone.isPlaying()) {
//                            mRingtone.stop();
//                        }
//                    }
//                };
//                Timer timer = new Timer();
//                timer.schedule(task, 9000);
//
//                return false;
//
//            } else {
//                return true;
//            }
//
//        }
        return true;
    }
}
