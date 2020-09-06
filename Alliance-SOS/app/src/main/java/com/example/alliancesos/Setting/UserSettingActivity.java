package com.example.alliancesos.Setting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TimeZone;

public class UserSettingActivity extends AppCompatActivity {

    private EditText mEmail, mPass, mUsername, mTime;


    private String mUserId;
    private UserObject mUserInfo;

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
//        GetInfoTask getInfoTask = new GetInfoTask();
//        getInfoTask.execute();
    }

    private void Initialize() {
        mRootRef = FirebaseDatabase.getInstance().getReference();
        UIInit();
    }

    public void UpdateUserProfile(View view) {

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


    private class GetInfoTask extends AsyncTask<Void, Void, Void> {

        private String successful;

        public GetInfoTask() {
            successful = "Successfully Done";
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            loadingDialog.hideDialog();
            if (successful.equals("Successfully Done")) {
                Toast.makeText(UserSettingActivity.this, successful, Toast.LENGTH_SHORT).show();
                setInfoToUi();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog.showDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mRootRef.child("users").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String id = snapshot.child("id").getValue().toString();
                        String userName = snapshot.child("userName").getValue().toString();
                        String email = snapshot.child("email").getValue().toString();
                        String token = snapshot.child("token").getValue().toString();
                        String pass = snapshot.child("password").getValue().toString();
                        mUserInfo = new UserObject(id, userName, email, pass, token);
                    } else {
                        successful = "no Snapshot";
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    successful = error.getMessage();
                }
            });
            return null;
        }
    }

    private void setInfoToUi() {
        mEmail.setText(mUserInfo.getEmail());
        mPass.setText(mUserInfo.getPassword());
        mUsername.setText(mUserInfo.getUserName());
        mTime.setText("TimeZone");
    }

    private void UIInit() {
        mEmail = findViewById(R.id.email_setting);
        mPass = findViewById(R.id.password_setting);
        mUsername = findViewById(R.id.username_setting);
        mTime = findViewById(R.id.timeZone_setting);
    }
}