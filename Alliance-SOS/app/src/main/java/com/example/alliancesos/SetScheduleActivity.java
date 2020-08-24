package com.example.alliancesos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SetScheduleActivity extends AppCompatActivity {

    private String mDate_str;
    private String mTime_str;

    private EditText mTitle;
    private EditText mDate_edt;
    private EditText mTime_edt;
    private EditText mDescribe;
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

        Initialize();

    }

    private void Initialize() {
        //ui
        InitializeUI();
        setupButtons();

        mCalendar = Calendar.getInstance();
        mTime = Calendar.getInstance();

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDate();
            }
        };

        time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mTime_str = hourOfDay + ":" + minute;
                mTime_edt.setText(mTime_str);
                Toast.makeText(SetScheduleActivity.this, "Time Is " + hourOfDay + " : " + minute, Toast.LENGTH_LONG).show();
            }
        };
    }

    private void updateDate() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        mDate_edt.setText(sdf.format(mCalendar.getTime()));
    }


    private void InitializeUI() {
        mTitle = findViewById(R.id.schedule_title);
        mDate_edt = findViewById(R.id.schedule_date);
        mTime_edt = findViewById(R.id.schedule_time);
        mDescribe = findViewById(R.id.schedule_description);
        mSet_Send = findViewById(R.id.set_schedule_btn);
    }

    private void setupButtons() {
        //time
        mTime_edt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(SetScheduleActivity.this, time, mTime
                        .get(Calendar.HOUR_OF_DAY), mTime.get(Calendar.MINUTE), false).show();
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

            }
        });
    }
}