package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.alliancesos.Adapters.ShowGroup;
import com.example.alliancesos.SendNotificationPack.Token;
import com.example.alliancesos.Setting.UserSettingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    private String mCurrentUserId, mCurrentUserName;

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mFirebaseAuth;

    private UserObject mCurrentUserObject;

    //database
    private DatabaseReference mRoot, mGroupsRef, mUsersRef;

    private ArrayList<String> listOfGroupName, listOfGroupId;
    private ArrayList<UpComingEvent> listOfUpcomingEvents;

    private ShowGroup mGroupAdapter;
    private RecyclerView mGroup_rv;
    private ChildEventListener mGroupChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();
    }

    private void Initialize() {
        //progress
        progressBar = findViewById(R.id.progress_main);

        //auth
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    gotToLoginPage();
                }
            }
        };
        UpdateToken();

        //database
        mRoot = FirebaseDatabase.getInstance().getReference();
        mGroupsRef = mRoot.child("groups");
        mUsersRef = mRoot.child("users");
        showCurrentUserGroups();

        InitializeUI();
        getCurrentUserName();
    }

    private void InitializeUI() {
        listOfGroupId = new ArrayList<>();
        listOfGroupName = new ArrayList<>();
        listOfUpcomingEvents = new ArrayList<>();

        mGroupAdapter = new ShowGroup(MainActivity.this, listOfGroupName, listOfGroupId, listOfUpcomingEvents, mCurrentUserId);
        mGroup_rv = findViewById(R.id.group_pattern_recycle);
        mGroup_rv.setAdapter(mGroupAdapter);
        mGroup_rv.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        Button createGroup = findViewById(R.id.create_group_btn);

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestNewGroup();
            }
        });
    }

    private void getCurrentUserName() {
        mUsersRef.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    getCurrentUsernameTask getCurrentUsernameTask = new getCurrentUsernameTask(snapshot);
                    getCurrentUsernameTask.execute();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter the Group name and Id :");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.add_vector);
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("example ");

        final EditText groupIdField = new EditText(MainActivity.this);
        groupIdField.setHint("write Id group (Must be Unique) ");

        LinearLayout createGroupLayout = new LinearLayout(getApplicationContext());
        createGroupLayout.setOrientation(LinearLayout.VERTICAL);

        createGroupLayout.addView(groupIdField);
        createGroupLayout.addView(groupNameField);

        builder.setView(createGroupLayout);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();
                String groupId = groupIdField.getText().toString();
                if (TextUtils.isEmpty(groupName) || TextUtils.isEmpty(groupId)) {
                    Toast.makeText(MainActivity.this, "None of them should not be Empty...", Toast.LENGTH_SHORT).show();
                } else {
                    CreateNewGroup(groupName, groupId);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showCurrentUserGroups() {
        attachedGroupListener();
    }

    private void attachedGroupListener() {
        progressBar.setVisibility(View.VISIBLE);
        if (mGroupChangeListener == null) {
            mGroupChangeListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    if (progressBar.getVisibility() != View.VISIBLE)
                        progressBar.setVisibility(View.VISIBLE);

                    final String name = snapshot.child("groupName").getValue().toString();
                    final String id = snapshot.child("groupId").getValue().toString();
                    mGroupsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UpComingEvent event = snapshot.child("upComingEvent").getValue(UpComingEvent.class);
                            mGroupAdapter.add(name, event, id);
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, "error in reading upComing: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    String name = snapshot.child("groupName").getValue().toString();
                    String id = snapshot.child("groupId").getValue().toString();
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error in attach child to database :" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            mUsersRef.child(mCurrentUserId).child("Groups").addChildEventListener(mGroupChangeListener);
        }
    }

    private void CreateNewGroup(final String groupName, final String groupId) {
        progressBar.setVisibility(View.VISIBLE);
        final Groups groups = new Groups(groupName, groupId, mCurrentUserId);
        mGroupsRef.child(groupId).setValue(groups).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Member admin = new Member(mCurrentUserId, mCurrentUserName);
                    admin.setCanChangeGroupImage(true);
                    mGroupsRef.child(groupId).child("members").child(mCurrentUserId).setValue(admin).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                addGroupToUserSubset(groupName, groupId);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "admin didn't add to group " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                } else {
                    progressBar.setVisibility(View.GONE);
                    String message = task.getException().toString();
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addGroupToUserSubset(String groupName, String groupId) {
        HashMap<String, String> groupInfo = new HashMap<>();
        groupInfo.put("groupName", groupName);
        groupInfo.put("groupId", groupId);
        mUsersRef.child(mCurrentUserId).child("Groups").push().setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "error in add admin " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void UpdateToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Token newToken = new Token(instanceIdResult.getToken());
                mUsersRef.child(mCurrentUserId).child("token").setValue(newToken.getToken()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "couldn't update token :" + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onPause() {
        progressBar.setVisibility(View.GONE);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        progressBar.setVisibility(View.GONE);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 200);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthStateListener != null) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            progressBar.setVisibility(View.GONE);
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    public void gotoUserProfile(View view) {
        Intent intent = new Intent(getApplicationContext(), UserSettingActivity.class);
        intent.putExtra("userId", mCurrentUserId);
        startActivity(intent);
    }

    public void logOut(View view) {
        if (mGroupChangeListener != null) {
            mGroupsRef.removeEventListener(mGroupChangeListener);
        }
        FirebaseAuth.getInstance().signOut();
        gotToLoginPage();
        return;
    }

    private void gotToLoginPage() {
        Intent intent = new Intent(getApplicationContext(), LogInPage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return;
    }

    public void RefreshPage(View view) {
        if (mGroupChangeListener != null) {
            mGroupAdapter.clearAll();
            mUsersRef.child(mCurrentUserId).child("Groups").removeEventListener(mGroupChangeListener);
        }
//        mUsersRef.child(mCurrentUserId).child("Groups").addChildEventListener(mGroupChangeListener);
        RefreshTask task = new RefreshTask();
        task.execute();
    }

    public class getCurrentUsernameTask extends AsyncTask<Void, Void, Void> {

        private DataSnapshot mSnapShot;

        public getCurrentUsernameTask(DataSnapshot snapshot) {
            mSnapShot = snapshot;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressBar.getVisibility() != View.VISIBLE)
                progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((TextView) findViewById(R.id.main_username)).setText(mCurrentUserName);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mCurrentUserName = mSnapShot.child("userName").getValue().toString();
            mGroupAdapter.setUserName(mCurrentUserName);
            return null;
        }
    }

    public class RefreshTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mGroupChangeListener != null) {
                mUsersRef.child(mCurrentUserId).child("Groups").addChildEventListener(mGroupChangeListener);
            }
            return null;
        }
    }


    private void getCurrentUserInfo() {
        mUsersRef.child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String id = snapshot.child("id").getValue().toString();
                    String userName = snapshot.child("userName").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    String token = snapshot.child("token").getValue().toString();
                    String pass = snapshot.child("password").getValue().toString();
                    mCurrentUserObject = new UserObject(id, userName, email, pass, token);
                    Toast.makeText(MainActivity.this, "admin info gotten...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Main activity not exist...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Main activity " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}