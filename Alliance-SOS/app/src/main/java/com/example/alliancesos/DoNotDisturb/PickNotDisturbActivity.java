package com.example.alliancesos.DoNotDisturb;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.alliancesos.DbForRingtone.ChoiceApplication;
import com.example.alliancesos.R;
import com.example.alliancesos.SetScheduleActivity;

import java.util.Calendar;

public class PickNotDisturbActivity extends AppCompatActivity {
    private ProgressBar mProgress;
    private EditText mStart, mEnd, mDate;
    private CheckBox mDaily, mRepeat;
    private ChoiceApplication mChoiceDB;
    private DatePickerDialog.OnDateSetListener date;
    private TimePickerDialog.OnTimeSetListener time;
    private boolean isStartTurn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_not_disturb);
        Init();
    }

    private void Init() {

        mProgress = findViewById(R.id.progress_pick_disturb);
        mChoiceDB = new ChoiceApplication(PickNotDisturbActivity.this);
        mStart = findViewById(R.id.pick_disturb_start);
        mEnd = findViewById(R.id.pick_disturb_end);
        mDate = findViewById(R.id.pick_disturb_date);
        mDaily = findViewById(R.id.pick_disturb_daily);
        mRepeat = findViewById(R.id.pick_disturb_repeat);

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                String format = year + "/" + monthOfYear + "/" + dayOfMonth;
                mDate.setText(format);
            }
        };
        time = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = hourOfDay + ":" + minute;
                if (isStartTurn) {
                    mStart.setText(time);
                } else {
                    mEnd.setText(time);
                }
            }
        };
    }

    public void saveNOtDisturbRule(View view) {
        Long id = (System.currentTimeMillis() / 1000);
        notDisturbObject newObject = new notDisturbObject(id.intValue() + "", mDate.getText().toString()
                , mStart.getText().toString(), mEnd.getText().toString());
        newObject.repeated = mRepeat.isChecked();
        newObject.daily = mDaily.isChecked();
        addToDatabaseTask newTask = new addToDatabaseTask(newObject);
        newTask.execute();
    }

    public void goBack(View view) {
        finish();
        return;
    }

    public void pickEnd(View view) {
        isStartTurn = false;
        new TimePickerDialog(PickNotDisturbActivity.this, time, 12, 30, true).show();
    }

    public void pickStart(View view) {
        isStartTurn = true;
        new TimePickerDialog(PickNotDisturbActivity.this, time, 12, 30, true).show();

    }

    public void pickDate(View view) {
        Calendar mCalendar = Calendar.getInstance();
        new DatePickerDialog(PickNotDisturbActivity.this, date, mCalendar
                .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public class addToDatabaseTask extends AsyncTask<Void, Void, Void> {

        notDisturbObject newObj;

        public addToDatabaseTask(notDisturbObject obj) {
            newObj = obj;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgress.setVisibility(View.GONE);
            Toast.makeText(PickNotDisturbActivity.this, "Successfully saved", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mChoiceDB.appDatabase.disturbDao().insert(newObj);
            return null;
        }
    }
}