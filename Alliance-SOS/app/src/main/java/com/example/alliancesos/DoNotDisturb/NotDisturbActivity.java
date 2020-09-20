package com.example.alliancesos.DoNotDisturb;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
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

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_not_disturb);
        Init();
        new showAllRulesTask().execute();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void Init() {
        mProgressBar = findViewById(R.id.progress_notDisturb);
        mChoiceDB = new ChoiceApplication(this);
        mRecyclerView = findViewById(R.id.not_disturbs_rv);
        mRulesList = new ArrayList<>();
        mRulesAdapter = new notDisturbRules(NotDisturbActivity.this, mRulesList, mChoiceDB);
        mRecyclerView.setAdapter(mRulesAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(NotDisturbActivity.this));
    }

    public void addNewNotDisturbRule(View view) {
        startActivity(new Intent(NotDisturbActivity.this, PickNotDisturbActivity.class));
    }

    public void refreshNotDisturb(View view) {
        mRulesAdapter.RemoveAll();
        new showAllRulesTask().execute();
    }


    public class showAllRulesTask extends AsyncTask<Void, Void, Void> {

        List<notDisturbObject> allRules;

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (allRules != null) {
                for (notDisturbObject obj :
                        allRules) {
                    mRulesAdapter.add(obj);
                }
            }
//            mProgressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            allRules = mChoiceDB.appDatabase.disturbDao().getAllRules();
//            mChoiceDB.appDatabase.disturbDao().deleteAll();
            return null;
        }
    }

    public void goBackToUserSetting(View view) {
        finish();
        return;
    }
}