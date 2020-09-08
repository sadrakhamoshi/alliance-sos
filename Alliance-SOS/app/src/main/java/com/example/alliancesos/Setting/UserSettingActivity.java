package com.example.alliancesos.Setting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.alliancesos.R;
import com.example.alliancesos.UserObject;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class UserSettingActivity extends AppCompatActivity {

    private EditText mEmail, mPass, mUsername, mTime, mLanguage;

    private String mUserId;
    private UserObject mNewUserInfo, mOldUserInfo;

    private DatabaseReference mRootRef;

    private ViewDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        loadingDialog = new ViewDialog(this);

        Intent fromGroup = getIntent();
        if (fromGroup != null) {
            mUserId = fromGroup.getStringExtra("userId");
        }
        Initialize();
    }

    private void Initialize() {
        mRootRef = FirebaseDatabase.getInstance().getReference();
        UIInit();
    }

    public void UpdateUserProfile(View view) {
        Toast.makeText(getApplicationContext(), mNewUserInfo.getUserName(), Toast.LENGTH_SHORT).show();
        //setInfoToUi();
    }

    public void selectLanguage(View view) {
    }

    private void getInfoOfCurrentUser() {
        loadingDialog.showDialog();

        mRootRef.child("users").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String id = snapshot.child("id").getValue().toString();
                    String userName = snapshot.child("userName").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    String token = snapshot.child("token").getValue().toString();
                    String pass = snapshot.child("password").getValue().toString();
                    String language = snapshot.child("language").getValue().toString();
                    mNewUserInfo = new UserObject(id, userName, email, pass, token);
                    mNewUserInfo.setLanguage(language);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setInfoToUi();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserSettingActivity.this, error.getMessage() + "\n" + error.getDetails(), Toast.LENGTH_SHORT).show();
                loadingDialog.hideDialog();
            }
        });
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                loadingDialog.hideDialog();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 1500);
    }

    private void setInfoToUi() {
        mEmail.setText(mNewUserInfo.getEmail());
        mPass.setText(mNewUserInfo.getPassword());
        mUsername.setText(mNewUserInfo.getUserName());
        mTime.setText(mNewUserInfo.getTimeZone());
        mLanguage.setText(mNewUserInfo.getLanguage());
    }

    private void UIInit() {
        mEmail = findViewById(R.id.email_setting);
        mPass = findViewById(R.id.password_setting);
        mUsername = findViewById(R.id.username_setting);
        mTime = findViewById(R.id.timeZone_setting);
        mLanguage = findViewById(R.id.language_setting);
    }

    public void getTimeZone(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(UserSettingActivity.this);
        View searchView = layoutInflater.inflate(R.layout.set_time_zone, null, false);

        setAllZoneIds(searchView);

        builder.setTitle("pick Time Zone");
        builder.setView(searchView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (TextUtils.isEmpty(mTime.getText().toString())) {
                    Toast.makeText(UserSettingActivity.this, "You should Chose on Item", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void setAllZoneIds(View root) {
        ListView ZoneIds = root.findViewById(R.id.time_zone_listView);

        SearchView searchView = root.findViewById(R.id.time_zone_search_view);
        final String[] tmp = TimeZone.getAvailableIDs();
        final ArrayList<String> allZonesIds = new ArrayList<>();
        allZonesIds.add("_");
        allZonesIds.addAll(Arrays.asList(tmp));
        final ArrayAdapter<String> mAllZonesId_adapter = new ArrayAdapter<>(root.getContext(), android.R.layout.simple_list_item_1, allZonesIds);
        ZoneIds.setAdapter(mAllZonesId_adapter);
        ZoneIds.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        ZoneIds.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mTime.setText(allZonesIds.get(position));
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (allZonesIds.contains(query)) {
                    mAllZonesId_adapter.getFilter().filter(query);
                } else {
                    Toast.makeText(UserSettingActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAllZonesId_adapter.getFilter().filter(newText);
                return false;
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getInfoOfCurrentUser();
    }

    private class GetInfoTask extends AsyncTask<Void, Void, Void> {

        private boolean successful;
        private UserObject userObject;

        public GetInfoTask() {
            userObject = null;
            successful = true;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.showDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getInfoOfCurrentUser();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }

}