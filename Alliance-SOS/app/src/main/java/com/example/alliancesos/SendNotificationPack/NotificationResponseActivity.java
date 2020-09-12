package com.example.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.alliancesos.DeviceAlarm.MyAlarmService;
import com.example.alliancesos.MainActivity;
import com.example.alliancesos.R;
import com.example.alliancesos.ScheduleObject;
import com.example.alliancesos.Utils.AlarmType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class NotificationResponseActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> mNames;
    private ArrayAdapter<String> adapter;

    private String mGroupId, mEventId;

    private String mCurrUsername, mCurrUserId;

    private Integer mRingOrNotify;

    private ScheduleObject scheduleObject;

    private String mFrom_TimeZoneId, mCurrent_TimezoneId;

    private DatabaseReference mGroupRef, mRootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_response);
        Initialize();
        if (!TextUtils.isEmpty(mEventId)) {
            getCurrentTimezone();
        }
    }

    private void Initialize() {

        //database
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mGroupRef = mRootRef.child("groups");
        mCurrent_TimezoneId = "";
        getExtra();
        InitUI();
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

    private void getExtra() {
        Bundle bundle = getIntent().getExtras();
        mEventId = bundle.getString("eventId");
        if (TextUtils.isEmpty(mEventId)) {
            finish();
            return;
        }
        mGroupId = bundle.getString("groupId");
        mCurrUserId = bundle.getString("toId");
        mCurrUsername = bundle.getString("toName");
        getRingOrNotify();
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

    private void showAttendingMembers() {
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
//                        String name = dataSnapshot.child("name").getValue().toString();
//                        names.add(name);
                    }
//                    mNames.clear();
//                    mNames.addAll(names);
//                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationResponseActivity.this, "Error in ShowAttending members " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, MyAlarmService.class);
            intent.setAction("com.example.helloandroid.alarms");
            intent.putExtra("ringEnable", mRingOrNotify);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 101, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar calendar = ConvertTime();
            Toast.makeText(this, "Alarm set Successfully ....", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, calendar.get(Calendar.HOUR_OF_DAY) + " " + calendar.get(Calendar.MINUTE) + " " + calendar.get(Calendar.SECOND), Toast.LENGTH_SHORT).show();
            alarmManager.setExact(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        } else {
            Toast.makeText(this, "schedule object is null ", Toast.LENGTH_SHORT).show();
        }
    }

    private Calendar ConvertTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(scheduleObject.getDateTime().getYear()));
        calendar.set(Calendar.MONTH, Integer.parseInt(scheduleObject.getDateTime().getMonth()));
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(scheduleObject.getDateTime().getDay()));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(scheduleObject.getDateTime().getHour()));
        calendar.set(Calendar.MINUTE, Integer.parseInt(scheduleObject.getDateTime().getMinute()));
        calendar.set(Calendar.SECOND, 0);

        String from = TimeZone.getTimeZone(mFrom_TimeZoneId).getDisplayName(true, TimeZone.SHORT);
        from = from.replace("GMT", "");
        String[] h_m_seperated = from.split(":");

        Integer h_from = 0, m_from = 0;
        try {
            h_from = Integer.parseInt(h_m_seperated[0]);
        } catch (Exception e) {
        }
        try {
            m_from = Integer.parseInt(h_m_seperated[1]);
        } catch (Exception e) {
        }
        if (from.contains("-")) {
            m_from *= -1;
//            h_from *= -1;
        }
        Date targetTime_in_GMT = new Date(calendar.getTimeInMillis() - (h_from * 60 * 60 * 1000 + m_from * 60 * 1000));

        if (TextUtils.isEmpty(mCurrent_TimezoneId)) {
            mCurrent_TimezoneId = TimeZone.getDefault().getID();
        }
        String target = TimeZone.getTimeZone(mCurrent_TimezoneId).getDisplayName(true, TimeZone.SHORT);
        target = target.replace("GMT", "");
        String[] h_m_spereated2 = target.split(":");
        Integer h_target = 0, m_target = 0;
        try {
            h_target = Integer.parseInt(h_m_spereated2[0]);
        } catch (Exception e) {
        }
        try {
            m_target = Integer.parseInt(h_m_spereated2[1]);
        } catch (Exception e) {
        }
        if (target.contains("-")) {
            m_target *= -1;
//            h_target *= -1;
        }
        Date newDate = new Date(targetTime_in_GMT.getTime() + (h_target * 60 * 60 * 1000 + m_target * 60 * 1000));
        calendar.setTime(newDate);
        return calendar;
    }

    private void getCurrentTimezone() {
//        mCurrent_TimezoneId = TimeZone.getDefault().getID();

        //for test
        mRootRef.child("users").child(mCurrUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    mCurrent_TimezoneId = snapshot.child("timeZone").getValue().toString();
                } else {
                    Toast.makeText(NotificationResponseActivity.this, "not exists user (timezone)...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationResponseActivity.this, "error onCreate getting timezone " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void InitUI() {
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