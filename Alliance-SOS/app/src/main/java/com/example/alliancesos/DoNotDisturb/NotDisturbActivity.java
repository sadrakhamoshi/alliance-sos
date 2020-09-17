package com.example.alliancesos.DoNotDisturb;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.alliancesos.DbForRingtone.ChoiceApplication;
import com.example.alliancesos.R;
import com.example.alliancesos.Adapters.*;
import com.example.alliancesos.Utils.WeekDay;

import java.util.ArrayList;
import java.util.List;

public class NotDisturbActivity extends AppCompatActivity {

    private notDisturbRules mRulesAdapter;
    private RecyclerView mRecyclerView;
    private ArrayList<notDisturbObject> mRulesList;

    private ChoiceApplication mChoiceDB;
    boolean isStartTurn;
    private TimePickerDialog.OnTimeSetListener time;

    private EditText mStart, mEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_disturb);
        Init();
        new showAllRulesTask().execute();
    }


    private void Init() {

        mChoiceDB = new ChoiceApplication(this);
        mRecyclerView = findViewById(R.id.not_disturbs_rv);
        mRulesList = new ArrayList<>();
        mRulesAdapter = new notDisturbRules(NotDisturbActivity.this, mRulesList);
        mRecyclerView.setAdapter(mRulesAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(NotDisturbActivity.this));

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


    public void addNewNotDisturbRule(View view) {
        final View root = getLayoutInflater().inflate(R.layout.create_new_rules, null);

        setupNewRulesView(root);

        AlertDialog.Builder builder = new AlertDialog.Builder(NotDisturbActivity.this, R.style.AlertDialog);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Long curr = System.currentTimeMillis() / 1000;
                String id = curr.intValue() + "";
                String day = ((EditText) root.findViewById(R.id.pick_day_rules)).getText().toString();
                String start = mStart.getText().toString();
                String end = mEnd.getText().toString();
                final notDisturbObject obj = new notDisturbObject(id, day, start, end);
                obj.repeat = ((CheckBox) root.findViewById(R.id.check_rule)).isChecked();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRulesAdapter.add(obj);
                    }
                });
                new addToDatabaseTask(obj).execute();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setView(root);
        builder.create().show();
    }

    private void setupNewRulesView(final View root) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(root.getContext(), android.R.layout.select_dialog_singlechoice, WeekDay.DAYS);
        AutoCompleteTextView dayView = root.findViewById(R.id.pick_day_rules);
        dayView.setThreshold(1);
        dayView.setAdapter(adapter);

        mStart = root.findViewById(R.id.start_rule);
        mEnd = root.findViewById(R.id.end_rule);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartTurn = true;
                new TimePickerDialog(root.getContext(), time, 12, 30, true).show();
            }
        });
        mEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartTurn = false;
                new TimePickerDialog(root.getContext(), time, 12, 30, true).show();
            }
        });
    }

    public void goBackToUserSetting(View view) {
        finish();
        return;
    }

    public class addToDatabaseTask extends AsyncTask<Void, Void, Void> {

        private notDisturbObject object;

        public addToDatabaseTask(notDisturbObject notDisturbObject) {
            object = notDisturbObject;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(NotDisturbActivity.this, "finish", Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute() {
            Toast.makeText(NotDisturbActivity.this, "start", Toast.LENGTH_SHORT).show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mChoiceDB.appDatabase.disturbDao().insert(object);
            return null;
        }
    }

    public class showAllRulesTask extends AsyncTask<Void, Void, Void> {

        List<notDisturbObject> allRules;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (allRules != null) {
                        for (notDisturbObject object :
                                allRules) {
                            mRulesAdapter.add(object);
                        }
//                        mRulesAdapter.addAll(allRules);
                    }
                }
            });
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            allRules = mChoiceDB.appDatabase.disturbDao().getAllRules();
//            appDatabase.disturbDao().deleteAll();
            return null;
        }
    }
}