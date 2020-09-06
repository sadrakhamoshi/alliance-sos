package com.example.alliancesos.Setting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alliancesos.R;
import com.example.alliancesos.UserObject;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        GetInfoTask getInfoTask = new GetInfoTask();
        getInfoTask.execute();
    }

    private void Initialize() {
        mRootRef = FirebaseDatabase.getInstance().getReference();
        UIInit();
    }

    public void UpdateUserProfile(View view) {
        
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