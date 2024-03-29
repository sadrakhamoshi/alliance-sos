package com.kaya.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AlertDialog;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.kaya.alliancesos.AlarmRequest.RequestCode;
import com.kaya.alliancesos.DbForRingtone.ChoiceApplication;
import com.kaya.alliancesos.DeviceAlarm.MyAlarmReceiver;
import com.kaya.alliancesos.SendNotificationPack.DataToSend;
import com.kaya.alliancesos.SendNotificationPack.SendingNotification;
import com.kaya.alliancesos.Utils.AlarmType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class SetScheduleActivity extends AppCompatActivity {

    private String mYear, mMonth, mDay, mHour, mMinute;

    private String mAuthorUserName, mAuthorId;
    private String mAuthorTimezone;

    private Event mEvent;

    private String mGroupId, mGroupName;

    //database
    private DatabaseReference mGroupsRef, mRootRef;

    private EditText mTitle_edt;
    private EditText mDate_edt;
    private EditText mTime_edt;
    private EditText mDescribe_edt;
    private Button mSet_Send;

    //date
    private Calendar mCalendar;
    private DatePickerDialog.OnDateSetListener date;

    //ApplicationChoicer
    private ChoiceApplication mChoiceDB;

    //valueEventListener
    private ValueEventListener mEventListener;


    //time
    private TimePickerDialog.OnTimeSetListener time;
    private Calendar mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_schedule);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mChoiceDB = new ChoiceApplication(SetScheduleActivity.this);
            }
        }).start();
        Intent fromGroupAct = getIntent();
        if (fromGroupAct != null) {
            mGroupId = fromGroupAct.getStringExtra("groupId");
            mAuthorId = fromGroupAct.getStringExtra("currUserId");
            mAuthorUserName = fromGroupAct.getStringExtra("currUserName");
            mGroupName = fromGroupAct.getStringExtra("groupName");
        }
        Initialize();
        getCurrentTimezone();
    }

    private void getCurrentTimezone() {
        mAuthorTimezone = TimeZone.getDefault().getID();
    }

    private void Initialize() {

        //database
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mGroupsRef = mRootRef.child("groups");

        mCalendar = Calendar.getInstance();
        mTime = Calendar.getInstance();

        //ui
        InitializeUI();
        setupButtons();

        InitializeTime_Date();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mEventListener == null) {
            mEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        long min = Long.MAX_VALUE;
                        Event upcome = null;
                        Iterator iterator = snapshot.getChildren().iterator();
                        while (iterator.hasNext()) {
                            DataSnapshot dataSnapshot = (DataSnapshot) iterator.next();
                            Event curr_event = dataSnapshot.getValue(Event.class);
                            long milisecond = curr_event.getTimeInMillisecond() * -1;
                            if (milisecond < min) {
                                min = milisecond;
                                upcome = curr_event;
                            }
                        }
                        if (upcome != null) {
                            mGroupsRef.child(mGroupId).child("upComingEvent").setValue(new UpComingEvent(upcome.getScheduleObject().getTitle(),
                                    upcome.getScheduleObject().getDateTime())).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(SetScheduleActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                    } else {
                        Toast.makeText(SetScheduleActivity.this, "No Event Exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SetScheduleActivity.this, "Error " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            mGroupsRef.child(mGroupId).child("events").addValueEventListener(mEventListener);
        }
    }

    private void setupButtons() {
        //time
        mTime_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(SetScheduleActivity.this, time, mTime
                        .get(Calendar.HOUR_OF_DAY), mTime.get(Calendar.MINUTE), true).show();
            }
        });


        //date
        mDate_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SetScheduleActivity.this, date, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        mSet_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = mTitle_edt.getText().toString();
                String date = mDate_edt.getText().toString();
                String time = mTime_edt.getText().toString();
                String description = mDescribe_edt.getText().toString();
                if (TextUtils.isEmpty(title) || TextUtils.isEmpty(date) || TextUtils.isEmpty(time)) {
                    Toast.makeText(SetScheduleActivity.this, "some fields are empty ...", Toast.LENGTH_SHORT).show();
                } else {
                    makeNotificationAlert(title, description);


                }
            }
        });
    }

    private void makeNotificationAlert(final String title, final String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SetScheduleActivity.this, R.style.AlertDialog);
        builder.setTitle("Are You Sure To Create Schedule and  Send to Other ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                DateTime dateTime = new DateTime(mYear, mMonth, mDay, mHour, mMinute);
                ScheduleObject scheduleObject = new ScheduleObject(title, description);
                scheduleObject.setDateTime(dateTime);
                try {
                    long milliSecond = computeScheduleInMilliSecond(scheduleObject.getDateTime());

                    mEvent = new Event("", mAuthorUserName, milliSecond * (-1), scheduleObject, mAuthorTimezone);
                    sendMessage();
                } catch (Exception e) {
                    Toast.makeText(SetScheduleActivity.this, "Error in Parsing catch : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private long computeScheduleInMilliSecond(DateTime dateTime) throws ParseException {
        int Month = Integer.parseInt(dateTime.getMonth()) + 1;
        String dt = dateTime.getYear() + "-" + Month + "-" + dateTime.getDay() + "T" + dateTime.getHour() + ":" + dateTime.getMinute() + ":0Z";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        int offset = TimeZone.getDefault().getOffset(Calendar.ZONE_OFFSET);
        Date date = format.parse(dt);
        return date.getTime() - offset;
    }

    private void sendMessage() {

        sendMessageToDB();
        sendNotificationToOtherDevice();
    }

    private void sendMessageToDB() {
        Random random = new Random();
        int id = random.nextInt(1000);
        if (id % 2 == 0) {
            id++;
        }
        final int finalId = id;
        Date calendar = convertSchToCalendar(mEvent.getScheduleObject().getDateTime());
        SetAlarmForMySelf(finalId, calendar);

        mEvent.setCreatedBy(mAuthorUserName);
        final String key = mGroupsRef.child(mGroupId).child("events").push().getKey();
        mEvent.setEventId(key);

        AddToLocalDatabase(finalId);
        mGroupsRef.child(mGroupId).child("events").child(key).setValue(mEvent).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", mAuthorId);
                    //adding event creator to member of event
                    ((ProgressBar) findViewById(R.id.progress_set_schedule)).setVisibility(View.GONE);

                    mGroupsRef.child(mGroupId).child("events").child(key).child("members").child(mAuthorId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                            } else {
                                Toast.makeText(SetScheduleActivity.this, "Can't add Author to event members...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    Toast.makeText(SetScheduleActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void AddToLocalDatabase(final int finalId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                RequestCode code = new RequestCode(mEvent.getEventId(), finalId + "");
                mChoiceDB.appDatabase.requestDao().insert(code);
            }
        }).start();
    }

    private Date convertSchToCalendar(DateTime dateTime) {
        int Month = Integer.parseInt(dateTime.getMonth()) + 1;
        String dateTime_combine = dateTime.getYear() + "/" + Month + "/" + dateTime.getDay() + "T"
                + dateTime.getHour() + "/" + dateTime.getMinute() + "/00Z";
        String pattern = "yyyy/MM/dd'T'HH/mm/ss'Z'";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            Date result = dateFormat.parse(dateTime_combine);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private void sendNotificationToOtherDevice() {
        DataToSend data = new DataToSend(mAuthorUserName, mGroupName, mGroupId, mEvent.getEventId());

        SendingNotification sendingNotification = new SendingNotification(mGroupId, mGroupName
                , mAuthorUserName, mAuthorId, getApplicationContext(), data);
        sendingNotification.isInSendMode = true;
        sendingNotification.Send();
    }

    private void InitializeUI() {
        mTitle_edt = findViewById(R.id.schedule_title);
        mDate_edt = findViewById(R.id.schedule_date);
        mTime_edt = findViewById(R.id.schedule_time);
        mDescribe_edt = findViewById(R.id.schedule_description);
        mSet_Send = findViewById(R.id.set_schedule_btn);
    }

    private void InitializeTime_Date() {
        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                mYear = year + "";
                mMonth = monthOfYear + "";
                mDay = dayOfMonth + "";
                Toast.makeText(SetScheduleActivity.this, mYear + " " + mMonth + " " + mDay, Toast.LENGTH_SHORT).show();
                updateDate();
            }
        };

        time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String minute_str = minute + "";
                if (minute < 10)
                    minute_str = "0" + minute_str;
                mTime_edt.setText(hourOfDay + ":" + minute_str);
                mHour = hourOfDay + "";
                mMinute = minute + "";
                Toast.makeText(SetScheduleActivity.this, "Time Is " + mHour + " : " + mMinute, Toast.LENGTH_LONG).show();
            }
        };
    }

    private void updateDate() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        mDate_edt.setText(sdf.format(mCalendar.getTime()));
    }

    public void backOnSetSchedule(View view) {
        finish();
        return;
    }

    private void SetAlarmForMySelf(Integer id, Date calendar) {
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), MyAlarmReceiver.class);
        intent.putExtra("ringEnable", AlarmType.RING);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Toast.makeText(this, id + " " + calendar, Toast.LENGTH_SHORT).show();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (calendar.getTime() > System.currentTimeMillis()) {
                Toast.makeText(this, "set alarm", Toast.LENGTH_SHORT).show();
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTime(), pendingIntent);
            }
        }
    }
}