package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.app.AlertDialog;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.alliancesos.SendNotificationPack.DataToSend;
import com.example.alliancesos.SendNotificationPack.SendingNotification;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

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


    //time
    private TimePickerDialog.OnTimeSetListener time;
    private Calendar mTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_schedule);
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
        mRootRef.child("users").child(mAuthorId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mAuthorTimezone = snapshot.child("timeZone").getValue().toString();
                } else {
                    Toast.makeText(SetScheduleActivity.this, "not exist user...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SetScheduleActivity.this, "onCancelled " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                    Date formattedDate = computeScheduleInMilliSecond(scheduleObject.getDateTime());
                    mEvent = new Event("", mAuthorUserName, formattedDate.getTime() * -1, scheduleObject, mAuthorTimezone);
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

    private Date computeScheduleInMilliSecond(DateTime dateTime) throws ParseException {

//        String dtStart = "2010-10-15T09:27:37Z";
        String dt = dateTime.getYear() + "-" + dateTime.getMonth() + "-" + dateTime.getDay() + "T" + dateTime.getHour() + ":" + dateTime.getMinute() + ":0Z";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        Date date = format.parse(dt);
        return date;
    }

    private void sendMessage() {

        sendMessageToDB();
        sendNotificationToOtherDevice();
    }

    private void sendMessageToDB() {
        mEvent.setCreatedBy(mAuthorUserName);
        final String key = mGroupsRef.child(mGroupId).child("events").push().getKey();
        mEvent.setEventId(key);
        mGroupsRef.child(mGroupId).child("events").child(key).setValue(mEvent).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("id", mAuthorId);
                    //adding event creator to member of event
                    ((ProgressBar) findViewById(R.id.progress_set_schedule)).setVisibility(View.VISIBLE);

                    mGroupsRef.child(mGroupId).child("events").child(key).child("members").child(mAuthorId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SortUpcomingEventTask sort = new SortUpcomingEventTask();
                                sort.execute();
//                                Toast.makeText(SetScheduleActivity.this, "Message Successfully added to Database...", Toast.LENGTH_SHORT).show();
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

    private void sendNotificationToOtherDevice() {
        DataToSend data = new DataToSend(mAuthorUserName, mGroupName, mGroupId, mEvent.getEventId());

        SendingNotification sendingNotification = new SendingNotification(mGroupId, mGroupName
                , mAuthorUserName, mAuthorId, getApplicationContext(), data);

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
                mTime_edt.setText(hourOfDay + ":" + minute);
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

    public class SortUpcomingEventTask extends AsyncTask<Void, Void, Void> {

        public String errorMessage;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((ProgressBar) findViewById(R.id.progress_set_schedule)).setVisibility(View.GONE);
            if (!TextUtils.isEmpty(errorMessage)) {
                Toast.makeText(SetScheduleActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(SetScheduleActivity.this, "successfully  done ...", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            errorMessage = "";
            ((ProgressBar) findViewById(R.id.progress_set_schedule)).setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            final Query query = mGroupsRef.child(mGroupId).child("events").orderByChild("timeInMillisecond");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child(mEvent.getEventId()).child("scheduleObject").child("title").getValue().toString();
                    DateTime date = snapshot.child(mEvent.getEventId()).child("scheduleObject").child("dateTime").getValue(DateTime.class);
                    String time = date.DisplayTime();
                    mGroupsRef.child(mGroupId).child("upComingEvent").setValue(new UpComingEvent(name, time)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                errorMessage = "error in sortUpcoming " + task.getException();
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    errorMessage = "error in sortUpcoming " + error.getMessage();
                }
            });
            return null;
        }
    }
}