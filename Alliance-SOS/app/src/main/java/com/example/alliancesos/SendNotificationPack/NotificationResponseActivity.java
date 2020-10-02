package com.example.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.AlarmRequest.RequestCode;
import com.example.alliancesos.DbForRingtone.ChoiceApplication;
import com.example.alliancesos.DeviceAlarm.MyAlarmReceiver;
import com.example.alliancesos.MainActivity;
import com.example.alliancesos.R;
import com.example.alliancesos.ScheduleObject;
import com.example.alliancesos.Utils.AlarmType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.TimeZone;

public class NotificationResponseActivity extends AppCompatActivity {
    private ProgressBar mProgressBar;

    private ListView listView;
    private ArrayList<String> mNames;
    private ArrayAdapter<String> adapter;

    private String mGroupId, mEventId;

    private String mCurrUserId;

    private Integer mRingOrNotify;

    private ScheduleObject scheduleObject;

    private String mFrom_TimeZoneId;

    private DatabaseReference mGroupRef, mRootRef;
    private ChoiceApplication mChoiceDB;

    private Date mConvertedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_response);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mChoiceDB = new ChoiceApplication(NotificationResponseActivity.this);
            }
        }).start();
        Initialize();
    }

    private void Initialize() {

        //database
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mGroupRef = mRootRef.child("groups");
        getExtra();
        InitUI();
    }

    private void getExtra() {
        Bundle bundle = getIntent().getExtras();
        mEventId = bundle.getString("eventId");
        if (TextUtils.isEmpty(mEventId)) {
            finish();
            return;
        }
        mGroupId = bundle.getString("groupId");
        setGroupId();
        mCurrUserId = bundle.getString("toId");
        getRingOrNotify();
    }

    private void getRingOrNotify() {
        mRingOrNotify = AlarmType.NOTIFICATION;
        mRootRef.child("users").child(mCurrUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    boolean type = snapshot.child("ringEnable").getValue(Boolean.class);
                    if (type) {
                        mRingOrNotify = AlarmType.RING;
                    }
                } else {
                    Toast.makeText(NotificationResponseActivity.this, "current user not found...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationResponseActivity.this, "canceled getting ringable.. " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setGroupId() {
        ((TextView) findViewById(R.id.notif_response_groupname)).setText("The Event Is Created From : " + mGroupId);
    }

    public void JoinEvent(View view) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", mCurrUserId);
        mGroupRef.child(mGroupId).child("events").child(mEventId).child("members").child(mCurrUserId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NotificationResponseActivity.this, "Member added to event members", Toast.LENGTH_SHORT).show();
                    setAlarm();
                    AlertDialogToMainActivity();

                } else {
                    Toast.makeText(NotificationResponseActivity.this, "error in joinEvent" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getEventFromDatabase() {
        mGroupRef.child(mGroupId).child("events").child(mEventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        scheduleObject = snapshot.child("scheduleObject").getValue(ScheduleObject.class);
                        mFrom_TimeZoneId = snapshot.child("createdTimezoneId").getValue().toString();
                        setTime();
                        Toast.makeText(NotificationResponseActivity.this, "get schedule object", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(NotificationResponseActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationResponseActivity.this, "Error in getEventfromDatabase" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTime() {
        mConvertedDate = ConvertTime();
        ((TextView) findViewById(R.id.noti_response_group_time)).setText("Date : " + mConvertedDate);
    }

    private void showAttendingMembers() {
        mProgressBar.setVisibility(View.VISIBLE);
        mGroupRef.child(mGroupId).child("events").child(mEventId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    Iterator iterator = snapshot.getChildren().iterator();
                    //List<String> names = new ArrayList<>();
                    mNames.clear();
                    while (iterator.hasNext()) {
                        DataSnapshot dataSnapshot = (DataSnapshot) (iterator.next());
                        String id = dataSnapshot.child("id").getValue().toString();
                        goToGroupMembersRef(id);
                    }
                    mProgressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationResponseActivity.this, "Error in ShowAttending members " + error.getMessage(), Toast.LENGTH_SHORT).show();
                mProgressBar.setVisibility(View.GONE);
            }
        });
    }

    private void goToGroupMembersRef(String id) {
        mGroupRef.child(mGroupId).child("members").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("userName").getValue().toString();
                    mNames.add(name);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(NotificationResponseActivity.this, "not exist memberId", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationResponseActivity.this, error.getMessage() + error.getDetails(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setAlarm() {
        if (scheduleObject != null) {
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Random r = new Random();
            int random = r.nextInt(1000);
            Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
            intent.putExtra("ringEnable", mRingOrNotify);

            if (mRingOrNotify == AlarmType.NOTIFICATION) {
                if (random % 2 == 1) {
                    random++;
                }
            } else {
                if (random % 2 == 0)
                    random++;
            }

            addRequestCodeToDb(random);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), random, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Toast.makeText(this, mConvertedDate + "", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mConvertedDate.getTime() > new Date().getTime()) {
                    Toast.makeText(this, "set Successfully", Toast.LENGTH_SHORT).show();
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mConvertedDate.getTime(), pendingIntent);
                }
            }
        } else {
            Toast.makeText(this, "schedule object is null ", Toast.LENGTH_SHORT).show();
        }
    }

    private void addRequestCodeToDb(final Integer id) {
        mProgressBar.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestCode newCode = new RequestCode(mEventId, id + "");
                mChoiceDB.appDatabase.requestDao().insert(newCode);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).start();
    }

    private Date ConvertTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(scheduleObject.getDateTime().getYear()));
        calendar.set(Calendar.MONTH, Integer.parseInt(scheduleObject.getDateTime().getMonth()));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(scheduleObject.getDateTime().getDay()));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(scheduleObject.getDateTime().getHour()));
        calendar.set(Calendar.MINUTE, Integer.parseInt(scheduleObject.getDateTime().getMinute()));
        calendar.set(Calendar.SECOND, 0);
        String mCurrent_TimezoneId = TimeZone.getDefault().getID();

        TimeZone from = TimeZone.getTimeZone(mFrom_TimeZoneId);
        TimeZone to = TimeZone.getTimeZone(mCurrent_TimezoneId);
        int from_offset = from.getOffset(Calendar.ZONE_OFFSET);
        int to_offset = to.getOffset(Calendar.ZONE_OFFSET);

        int diff = to_offset - from_offset;

        long time = calendar.getTimeInMillis() + diff;
        Date newDate = new Date(time);
        return newDate;
    }

    private void InitUI() {
        mProgressBar = findViewById(R.id.progress_notif_response);
        mNames = new ArrayList<>();
        listView = findViewById(R.id.event_member_listView);
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, mNames);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getEventFromDatabase();
        showAttendingMembers();
    }

    public void NotJoining(View view) {
        finish();
        return;
    }

    private void AlertDialogToMainActivity() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Alert !");
        builder.setMessage("Do you Want to go to the App ?");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.sos_icon);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent toMain = new Intent(NotificationResponseActivity.this, MainActivity.class);
                startActivity(toMain);
                finish();
                return;
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
            }
        });
        builder.create().show();
    }
}