package com.kaya.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kaya.alliancesos.AlarmRequest.RequestCode;
import com.kaya.alliancesos.DateTime;
import com.kaya.alliancesos.DbForRingtone.ChoiceApplication;
import com.kaya.alliancesos.DeviceAlarm.MyAlarmReceiver;
import com.kaya.alliancesos.Event;
import com.kaya.alliancesos.MainActivity;
import com.kaya.alliancesos.R;
import com.kaya.alliancesos.ScheduleObject;
import com.kaya.alliancesos.Utils.AlarmType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    private ZonedDateTime mConvertedDate;

    private Event mCurrEvent;

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
        getEvent();
    }

    public void getEvent() {
        mRootRef.child("groups").child(mGroupId).child("events").child(mEventId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mCurrEvent = snapshot.getValue(Event.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationResponseActivity.this, "error " + error, Toast.LENGTH_SHORT).show();
            }
        });
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
                        setTime_Detail(scheduleObject.getTitle(), scheduleObject.getDescription());
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

    private void setTime_Detail(String title, String description) {
        mConvertedDate = ConvertTime();
        ((TextView) findViewById(R.id.noti_response_title)).setText("Title : " + title);
        ((TextView) findViewById(R.id.noti_response_description)).setText("Description : " + description);
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
            int random = (int) System.currentTimeMillis();
            Intent intent = new Intent(this, MyAlarmReceiver.class);
            Gson gson = new Gson();
            String myJson = gson.toJson(mCurrEvent);
            intent.putExtra("myjson", myJson);
            intent.putExtra("groupId", mGroupId);
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
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, random, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            Toast.makeText(this, mConvertedDate + "", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mConvertedDate.isAfter(ZonedDateTime.now(ZoneId.systemDefault()))) {
                    Toast.makeText(this, "set Successfully", Toast.LENGTH_LONG).show();
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, mConvertedDate.toEpochSecond() * 1000, pendingIntent);
                }
            }
        } else {
            Toast.makeText(this, "schedule object is null ", Toast.LENGTH_SHORT).show();
        }
    }

    private void addRequestCodeToDb(final long id) {
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

    private ZonedDateTime ConvertTime() {
        DateTime tmp = scheduleObject.getDateTime();
        Instant instant = Instant.now();
        ZonedDateTime source = instant.atZone(ZoneId.of(mFrom_TimeZoneId)).
                withYear(Integer.parseInt(tmp.getYear())).
                withMonth(Integer.parseInt(tmp.getMonth()) + 1).
                withDayOfMonth(Integer.parseInt(tmp.getDay())).
                withHour(Integer.parseInt(tmp.getHour())).
                withMinute(Integer.parseInt(tmp.getMinute()))
                .withSecond(0);
        ZonedDateTime target = source.toInstant().atZone(ZoneId.systemDefault());

        String text = "Date : " + DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm").format(target);
        ((TextView) findViewById(R.id.noti_response_group_time)).setText(text);
        return target;
    }

    private void InitUI() {
        mProgressBar = findViewById(R.id.progress_notif_response);
        mNames = new ArrayList<>();
        listView = findViewById(R.id.event_member_listView);
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, mNames) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTextColor(Color.RED);
                return view;
            }
        };
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