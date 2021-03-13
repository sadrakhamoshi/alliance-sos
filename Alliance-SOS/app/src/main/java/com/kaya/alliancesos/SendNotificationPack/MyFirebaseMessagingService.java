package com.kaya.alliancesos.SendNotificationPack;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.kaya.alliancesos.DbForRingtone.ChoiceApplication;
import com.kaya.alliancesos.DbForRingtone.ringtone;
import com.kaya.alliancesos.R;
import com.kaya.alliancesos.SpecificSOSActivity;
import com.kaya.alliancesos.Utils.MessageType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.kaya.alliancesos.DoNotDisturb.*;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final long DELAY_TIME = 2 * 60 * 1000;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";

    private Bitmap notificationPhoto;
    private String inviteId;

    private DatabaseReference mRootRef;
    private String mUserId;

    private Context mContext;

    private Ringtone mRingtone;

    Integer type;
    Integer notificationIcon;
    Integer notificationColor;
    private boolean isMissed;
    String toName, toId;
    String title, message;
    String groupName, groupId, makeBy;
    String eventId;
    private ChoiceApplication mChoiceDB;
    private long sentTime;
    private String mSosId;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        type = Integer.valueOf(remoteMessage.getData().get("type"));
        sentTime = remoteMessage.getSentTime();
        isMissed = System.currentTimeMillis() - sentTime > DELAY_TIME;
        mContext = getApplicationContext();
        mChoiceDB = new ChoiceApplication(mContext);

        if (type == MessageType.INVITATION_TYPE) {
            try {
                Initialize(remoteMessage);
                buildNotification(mContext);
            } catch (Exception e) {
            }
        } else {
            boolean isNotInRules = checkDoNotDisturb();
            if (isNotInRules) {
                try {
                    Initialize(remoteMessage);
                    buildNotification(mContext);
                    if (type == MessageType.SOS_TYPE) {
                        if (!isMissed)
                            playRingtone1();
                    }
                } catch (Exception e) {
                    Log.v("Init", e.getMessage());
                }
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

    private void Initialize(RemoteMessage remoteMessage) throws IOException {
        eventId = toName = toId = "";
        groupName = remoteMessage.getData().get("groupName");
        groupId = remoteMessage.getData().get("groupId");
        makeBy = remoteMessage.getData().get("makeBy");
        toId = remoteMessage.getData().get("toId");

        if (type == MessageType.SOS_TYPE) {
            String sos_message = remoteMessage.getData().get("sosMessage");
            String photo_url = remoteMessage.getData().get("photoUrl");
            mSosId = remoteMessage.getData().get("sosId");

            notificationColor = Color.RED;
            notificationIcon = R.drawable.sos_icon;

            title = "Emergency Moment !!!!";
            if (!isMissed) {
                if (sos_message == null) {
                    message = "SOS button has clicked by " + makeBy + " from " + groupName;
                    RequestCreator creator = Picasso.get().load(photo_url);
                    notificationPhoto = creator.get();
                } else {
                    message = sos_message;
                }

            } else {
                message = "You Have One Missed SOS Emergency Moment from " + groupName;
                title = "MISSED SOS !!!";
            }

        } else if (type == MessageType.NOTIFICATION_TYPE) {

            eventId = remoteMessage.getData().get("eventId");
            toName = remoteMessage.getData().get("toName");
            notificationColor = Color.YELLOW;
            notificationIcon = R.drawable.notif_icon;
            title = "New Schedule ...";
            message = makeBy + " Created New Schedule in " + groupName + " . " + "\n" + "Are You going to join the group?";

        } else if (type == MessageType.INVITATION_TYPE) {
            title = "Invitation";
            inviteId = remoteMessage.getData().get("inviteId");
            message = "You Added to " + groupName + " By " + makeBy + ". \n" + "Click Here to Set Your UserName";
            notificationColor = Color.RED;
            notificationIcon = R.drawable.add_group_icon;
        }
    }

    private void buildNotification(Context context) {

        PendingIntent pendingIntent = getPendingIntent(context);
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

        if (type == MessageType.NOTIFICATION_TYPE || type == MessageType.INVITATION_TYPE) {
            builder.setSound(alarmSound);
        }
        if (type == MessageType.SOS_TYPE && notificationPhoto != null) {
            builder.setLargeIcon(notificationPhoto);
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(notificationPhoto)
                    .bigLargeIcon(null));
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.notificationChannel_name), importance);
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        notificationManager.notify(id, notification);
        notificationPhoto = null;
    }

    private PendingIntent getPendingIntent(Context context) {
        if (type == MessageType.NOTIFICATION_TYPE) {
            Intent intent = new Intent(context, NotificationResponseActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("eventId", eventId);
            intent.putExtra("toName", toName);
            intent.putExtra("toId", toId);

            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else if (type == MessageType.SOS_TYPE) {
            Intent intent = new Intent(context, SpecificSOSActivity.class);
            intent.putExtra("sosId", mSosId);
            intent.putExtra("groupId", groupId);
            intent.putExtra("time", sentTime);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            Intent intent = new Intent(context, InvitationResponseActivity.class);
            intent.putExtra("groupId", groupId);
            intent.putExtra("groupName", groupName);
            intent.putExtra("toId", toId);
            intent.putExtra("inviteId", inviteId);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void playRingtone1() {
        SharedPreferences sharedPreferences = getSharedPreferences("User_Ring", Context.MODE_PRIVATE);
        String path = sharedPreferences.getString(toId, null);
        if (path != null) {
            mRingtone = RingtoneManager.getRingtone(mContext, Uri.parse(path));
        } else {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            mRingtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
        }

//        ringtone ring = mChoiceDB.appDatabase.dao().currentPath(toId);
//        Uri alert = Uri.parse(ring.path);
        if (!mRingtone.isPlaying())
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
        timer.schedule(task, 60000);

    }

    private void playRingtone() {
        ringtone ring = mChoiceDB.appDatabase.dao().currentPath(toId);
        Uri alert = Uri.parse(ring.path);
        if (alert != null) {
            mRingtone = RingtoneManager.getRingtone(getBaseContext(), alert);
            if (!mRingtone.isPlaying())
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
            timer.schedule(task, 60000);
        } else {
            throw new NullPointerException();
        }
    }

    public boolean checkDoNotDisturb() {
        List<notDisturbObject> allRules = mChoiceDB.appDatabase.disturbDao().getAllRules();

        Date now = new Date();

        for (final notDisturbObject object :
                allRules) {

            boolean isIn = checkTime(object, now);
            if (isIn) {
                return false;
            }
        }
        return true;
    }

    private boolean checkTime(notDisturbObject target, Date now) {
        Date start = createInstance(target, notDisturbObject.splitDate(target.day), notDisturbObject.splitTime(target.from));
        Date end = createInstance(target, notDisturbObject.splitDate(target.day), notDisturbObject.splitTime(target.until));
        Log.v("compair", start + " " + now + " " + end);
        return now.after(start) && now.before(end);
    }

    public Date createInstance(notDisturbObject object, String[] dates, String[] time) {
        Calendar calendar = Calendar.getInstance();

        if (object.daily) {

        } else if (object.repeated) {
            Integer dayOfWeek = notDisturbObject.getDayOfWeek(dates);
            if (dayOfWeek == calendar.get(Calendar.DAY_OF_WEEK)) {
                //nothing to do
            } else {
                //set iligal value
                calendar.set(Calendar.YEAR, 2000);
            }
        } else {
            calendar.set(Calendar.YEAR, Integer.parseInt(dates[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(dates[1]) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dates[2]));
        }
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(time[1]));
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTime();
    }

}
