package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.AlarmRequest.RequestCode;
import com.example.alliancesos.DbForRingtone.ChoiceApplication;
import com.example.alliancesos.DeviceAlarm.MyAlarmReceiver;
import com.example.alliancesos.Utils.AlarmType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

public class SpecificEventActivity extends AppCompatActivity {

    private ProgressBar mProgress;

    private String mUserId, mUsername;
    private Event mCurrentEvent;
    private TextView mTitle, mCreated, mDate, mDescribe;

    private ValueEventListener mMemberListener;

    private ListView mListView;
    private ArrayList<String> mNamesList;
    private ArrayAdapter<String> adapter;

    //database
    private String mGroupId;
    private DatabaseReference mGroupRef;

    private ChoiceApplication mChoiceDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_event);


        Intent fromGroup = getIntent();
        if (fromGroup != null) {
            mCurrentEvent = (Event) fromGroup.getSerializableExtra("event");
            mGroupId = fromGroup.getStringExtra("groupId");
        }
        Init();
    }

    private void Init() {
        InitUI();
    }

    private void InitUI() {
        mListView = findViewById(R.id.list_view_event_page);
        mNamesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(SpecificEventActivity.this, android.R.layout.simple_list_item_1, mNamesList);
        mListView.setAdapter(adapter);

        mProgress = findViewById(R.id.progress_event_page);
        mTitle = findViewById(R.id.title_event_page);
        mDescribe = findViewById(R.id.describe_event_page);
        mDate = findViewById(R.id.date_event_page);
        mCreated = findViewById(R.id.created_event_page);
        mTitle.setText(mCurrentEvent.getScheduleObject().getTitle());
        mCreated.setText(mCurrentEvent.getCreatedBy());
        mDescribe.setText(mCurrentEvent.getScheduleObject().getDescription());
        try {
            mDate.setText(mCurrentEvent.getScheduleObject().GetDate(mCurrentEvent.getCreatedTimezoneId(), TimeZone.getDefault().getID()));
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void joinEventPage(View view) {
        if (!mNamesList.contains(mUsername)) {
            mProgress.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HashMap<String, String> newUid = new HashMap<>();
                    newUid.put("id", mUserId);
                    mGroupRef.child("events").child(mCurrentEvent.getEventId()).child("members").child(mUserId).setValue(newUid)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SpecificEventActivity.this, "Can Not join " + task.getException(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        setAlarmOn();
                                    }
                                    mProgress.setVisibility(View.GONE);
                                }
                            });

                }
            }).start();
        } else {
            Toast.makeText(this, "You already in the event...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mChoiceDB == null) {
            mProgress.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mChoiceDB = new ChoiceApplication(SpecificEventActivity.this);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProgress.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        }

        if (mUserId == null) {
            mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        if (mGroupRef == null)
            mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups").child(mGroupId);
        showMembersOfEvent();
        if (mUsername == null)
            getCurrentUsername();
    }

    private void getCurrentUsername() {
        mGroupRef.child("members").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mUsername = snapshot.child("userName").getValue().toString();
                } else {
                    Toast.makeText(SpecificEventActivity.this, "not exist ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SpecificEventActivity.this, "error in get current username " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMemberListener != null) {
            mGroupRef.child("events").child(mCurrentEvent.getEventId()).child("members").removeEventListener(mMemberListener);
        }
    }

    private void showMembersOfEvent() {
        if (mMemberListener == null) {
            mMemberListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Iterator iterator = snapshot.getChildren().iterator();
                    mNamesList.clear();
                    while (iterator.hasNext()) {
                        String id = ((DataSnapshot) iterator.next()).getKey().toString();
                        goToGroupMembers(id);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SpecificEventActivity.this, "Error in show :" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            Toast.makeText(this, mCurrentEvent.getEventId(), Toast.LENGTH_SHORT).show();
            mGroupRef.child("events").child(mCurrentEvent.getEventId()).child("members").addValueEventListener(mMemberListener);
        }
    }

    private void goToGroupMembers(String id) {
        mGroupRef.child("members").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String username = snapshot.child("userName").getValue().toString();
                    mNamesList.add(username);
                    adapter.notifyDataSetChanged();
                } else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SpecificEventActivity.this, "error in gotoGroupMembers :" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void leaveEventPage(View view) {
        if (mNamesList.contains(mUsername)) {
            mProgress.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mGroupRef.child("events").child(mCurrentEvent.getEventId()).child("members").child(mUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(SpecificEventActivity.this, "Task Not Done", Toast.LENGTH_SHORT).show();
                            } else {
                                mNamesList.remove(mUsername);
                                adapter.notifyDataSetChanged();
                                setAlarmOff();
                            }
                            mProgress.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();

        } else {
            Toast.makeText(this, "You are not in the group", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAlarmOn() {
        ScheduleObject mScheduleObject = mCurrentEvent.getScheduleObject();
        setAlarm(mScheduleObject, mCurrentEvent.getCreatedTimezoneId());
    }

    public void setAlarm(ScheduleObject scheduleObject, String createdZoneId) {

        if (scheduleObject != null) {
            AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
            intent.putExtra("ringEnable", AlarmType.RING);
            Random r = new Random();
            Integer random = r.nextInt(1000);
            addRequestCodeToDb(random);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), random, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar calendar = ConvertTime(scheduleObject, createdZoneId, TimeZone.getDefault().getID());
            Toast.makeText(this, calendar.get(Calendar.HOUR_OF_DAY) + " " + calendar.get(Calendar.MINUTE) + " " + calendar.get(Calendar.SECOND), Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    Toast.makeText(this, "set Successfully", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "schedule object is null ", Toast.LENGTH_SHORT).show();
        }
    }

    private void addRequestCodeToDb(final Integer id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mChoiceDB.appDatabase.requestDao().insert(new RequestCode(mCurrentEvent.getEventId(), id + ""));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SpecificEventActivity.this, "added to Database ...", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    private Calendar ConvertTime(ScheduleObject scheduleObject, String mFrom_TimeZoneId, String mCurrent_TimezoneId) {
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

    private void setAlarmOff() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mChoiceDB != null) {
                    RequestCode target = mChoiceDB.appDatabase.requestDao().getById(mCurrentEvent.getEventId());
                    mChoiceDB.appDatabase.requestDao().deleteRule(target);
                    int ringType = Integer.parseInt(target.reqCode) % 2;
                    AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                    Intent myIntent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
                    myIntent.putExtra("ringEnable", ringType);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            SpecificEventActivity.this, Integer.parseInt(target.reqCode), myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.cancel(pendingIntent);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SpecificEventActivity.this, "alarm set Off", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(SpecificEventActivity.this, "Waiting...", Toast.LENGTH_SHORT).show();
                }
            }
        }).start();

    }

    public void backEventPage(View view) {
        finish();
        return;
    }
}