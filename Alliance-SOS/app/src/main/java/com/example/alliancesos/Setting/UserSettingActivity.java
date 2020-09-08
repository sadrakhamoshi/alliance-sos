package com.example.alliancesos.Setting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.alliancesos.R;
import com.example.alliancesos.UserObject;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class UserSettingActivity extends AppCompatActivity {

    private boolean passChange, emailChange;

    private EditText mEmail, mPass, mUsername, mTime, mLanguage;
    private CheckBox mRingEnable;

    private String mUserId;
    private UserObject mCurrUserInfo;

    private DatabaseReference mUserRef;

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
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");

        passChange = emailChange = false;
        UIInit();
    }

    public void UpdateUserProfile(View view) {
        UserObject newInfo = getNewUserInfoFromText();
        checkUpdateCondition(newInfo);
    }

    private void checkUpdateCondition(UserObject newInfo) {
        if (!newInfo.getEmail().equals(mCurrUserInfo.getEmail())) {
            emailChange = true;
            updateEmailAddress(newInfo);
        } else {
            Toast.makeText(getApplicationContext(), "Email Has Updated...", Toast.LENGTH_SHORT).show();
        }
        if (!newInfo.getPassword().equals(mCurrUserInfo.getPassword())) {
            if (emailChange) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
                builder.setTitle("Alert !");
                builder.setMessage("You Can Not change email and password together" + "\n" + "Your Email has changed!!!");
                builder.setCancelable(false);
                builder.setIcon(R.drawable.sos_icon);
                builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            } else {
                updatePassword(newInfo);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Password Has Updated...", Toast.LENGTH_SHORT).show();
        }
        Updating(mUserId, "userName", mCurrUserInfo.getUserName(), newInfo.getUserName());
        Updating(mUserId, "ringEnable", mCurrUserInfo.isRingEnable(), newInfo.isRingEnable());
        Updating(mUserId, "timeZone", mCurrUserInfo.getTimeZone(), newInfo.getTimeZone());
        Updating(mUserId, "language", mCurrUserInfo.getLanguage(), newInfo.getLanguage());

        emailChange = false;
    }

    private void updateEmailAddress(final UserObject newInfo) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.updateEmail(newInfo.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    user.reauthenticate(EmailAuthProvider.getCredential(newInfo.getEmail(), newInfo.getPassword()))
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Updating(mUserId, "email", mCurrUserInfo.getEmail(), newInfo.getEmail());
                                        Toast.makeText(UserSettingActivity.this, "reAuthentication adn Update email database..", Toast.LENGTH_SHORT).show();
                                    } else {
                                        user.updateEmail(mCurrUserInfo.getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(UserSettingActivity.this, "turn back the email", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(UserSettingActivity.this, "Could'nt turn back email", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                        Toast.makeText(UserSettingActivity.this, "error in second onComplete update email", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(UserSettingActivity.this, task.getException().getMessage() + " error in Update email", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updatePassword(final UserObject newInfo) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.reauthenticate(EmailAuthProvider.getCredential(mCurrUserInfo.getEmail(), mCurrUserInfo.getPassword())).addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.updatePassword(newInfo.getPassword()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Updating(mUserId, "password", mCurrUserInfo.getPassword(), newInfo.getPassword());
                                    Toast.makeText(UserSettingActivity.this, "Update password database..", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(UserSettingActivity.this, task.getException().getMessage() + " error in Update password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
        );

    }

    private <Type> void Updating(String userId, String field, Type oldVal, Type newVal) {
        if (!oldVal.equals(newVal)) {
            mUserRef.child(userId).child(field).setValue(newVal);
            mCurrUserInfo.updateObject(field, newVal);
            Toast.makeText(getApplicationContext(), field + " Updated", Toast.LENGTH_SHORT).show();
        }
    }

    private UserObject getNewUserInfoFromText() {
        UserObject userObject = new UserObject();
        userObject.setEmail(mEmail.getText().toString());
        userObject.setPassword(mPass.getText().toString());
        userObject.setUserName(mUsername.getText().toString());
        userObject.setTimeZone(mTime.getText().toString());
        userObject.setLanguage(mLanguage.getText().toString());
        userObject.setRingEnable(mRingEnable.isChecked());

        userObject.setToken(mCurrUserInfo.getToken());
        userObject.setId(mCurrUserInfo.getId());
        userObject.setNotDisturb(false);
        return userObject;
    }

    private void getInfoOfCurrentUser() {
        loadingDialog.showDialog();

        mUserRef.child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String id = snapshot.child("id").getValue().toString();
                    String userName = snapshot.child("userName").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    String token = snapshot.child("token").getValue().toString();
                    String pass = snapshot.child("password").getValue().toString();
                    String language = snapshot.child("language").getValue().toString();
                    mCurrUserInfo = new UserObject(id, userName, email, pass, token);
                    mCurrUserInfo.setLanguage(language);
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
        mEmail.setText(mCurrUserInfo.getEmail());
        mPass.setText(mCurrUserInfo.getPassword());
        mUsername.setText(mCurrUserInfo.getUserName());
        mTime.setText(mCurrUserInfo.getTimeZone());
        mLanguage.setText(mCurrUserInfo.getLanguage());
        mRingEnable.setChecked(true);
    }

    private void UIInit() {
        mEmail = findViewById(R.id.email_setting);
        mPass = findViewById(R.id.password_setting);
        mUsername = findViewById(R.id.username_setting);
        mTime = findViewById(R.id.timeZone_setting);
        mLanguage = findViewById(R.id.language_setting);
        mRingEnable = findViewById(R.id.ring_before_event_setting);
        NotAllowedUseSpace();
    }

    public void selectLanguage(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(UserSettingActivity.this);
        View searchView = layoutInflater.inflate(R.layout.zones_languages_pattern, null, false);
        setAllLanguages(searchView);

        builder.setTitle("pick Language");
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

    private void setAllLanguages(View root) {
        ListView languages_listView = root.findViewById(R.id.time_zone_listView);
        final SearchView searchView = root.findViewById(R.id.time_zone_search_view);
        final ArrayList<String> allLanguages = new ArrayList<>();
        String[] isoLanguages = Locale.getISOLanguages();
        for (int i = 0; i < isoLanguages.length; i++) {
            Locale loc = new Locale(isoLanguages[i]);
            allLanguages.add(loc.getDisplayLanguage());
        }
        final ArrayAdapter<String> languages_adapter = new ArrayAdapter<>(root.getContext(), android.R.layout.simple_list_item_1, allLanguages);
        languages_listView.setAdapter(languages_adapter);
        languages_listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        languages_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(UserSettingActivity.this, languages_adapter.getItem(position), Toast.LENGTH_SHORT).show();
                mLanguage.setText(languages_adapter.getItem(position));
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (allLanguages.contains(query)) {
                    languages_adapter.getFilter().filter(query);
                } else {
                    Toast.makeText(UserSettingActivity.this, "Not Found", Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                languages_adapter.getFilter().filter(newText);
                return false;
            }
        });
    }

    public void setTimeZone(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(UserSettingActivity.this);
        LayoutInflater layoutInflater = LayoutInflater.from(UserSettingActivity.this);
        View searchView = layoutInflater.inflate(R.layout.zones_languages_pattern, null, false);

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
        final ListView zoneIds_listView = root.findViewById(R.id.time_zone_listView);
        final SearchView searchView = root.findViewById(R.id.time_zone_search_view);
        final String[] tmp = TimeZone.getAvailableIDs();
        final ArrayList<String> allZonesIds = new ArrayList<>();
        allZonesIds.addAll(Arrays.asList(tmp));
        final ArrayAdapter<String> mAllZonesId_adapter = new ArrayAdapter<>(root.getContext(), android.R.layout.simple_list_item_1, allZonesIds);
        zoneIds_listView.setAdapter(mAllZonesId_adapter);
        zoneIds_listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        zoneIds_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(UserSettingActivity.this, mAllZonesId_adapter.getItem(position), Toast.LENGTH_SHORT).show();
                mTime.setText(mAllZonesId_adapter.getItem(position) + "");
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

    private void NotAllowedUseSpace() {
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    if (Character.isWhitespace(source.charAt(i))) {
                        Toast.makeText(getApplicationContext(), "Not Space For this Field", Toast.LENGTH_SHORT).show();
                        return "";
                    }
                }
                return null;
            }
        };
        InputFilter[] filter1 = new InputFilter[]{filter};
        mEmail.setFilters(filter1);
        mUsername.setFilters(filter1);
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
