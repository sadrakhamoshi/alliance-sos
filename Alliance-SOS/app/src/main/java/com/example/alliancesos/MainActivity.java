package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.alliancesos.Adapters.ShowGroup;
import com.example.alliancesos.Adapters.SwipeToDeleteCallback;
import com.example.alliancesos.Payment.PaymentActivity;
import com.example.alliancesos.Payment.PaymentObject;
import com.example.alliancesos.SendNotificationPack.Token;
import com.example.alliancesos.Setting.UserSettingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private LinearLayout mLinearLayout;
    private static final int CREATING_GROUP = 886;

    private ProgressBar progressBar, progressBar_group_show;

    private String mCurrentUserId, mCurrentUserName;

    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseAuth mFirebaseAuth;

    //database
    private DatabaseReference mRoot, mGroupsRef, mUsersRef;

    private ArrayList<String> listOfGroupName, listOfGroupId;
    private ArrayList<UpComingEvent> listOfUpcomingEvents;

    private ShowGroup mGroupAdapter;
    private RecyclerView mGroup_rv;
    private ChildEventListener mGroupChangeListener;
    private ValueEventListener mValueEventListenerGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();
    }

    private void Initialize() {
        //layout
        mLinearLayout = findViewById(R.id.linear_layout_main);

        //progress
        progressBar = findViewById(R.id.progress_main);
        progressBar_group_show = findViewById(R.id.progress_main_group_show);

        //auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        if (mFirebaseAuth == null || mFirebaseAuth.getCurrentUser() == null)
            gotToLoginPage();
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    gotToLoginPage();
                } else {
                }
            }
        };
        UpdateToken();

        //database
        mRoot = FirebaseDatabase.getInstance().getReference();
        mGroupsRef = mRoot.child("groups");
        mUsersRef = mRoot.child("users");
        getCurrentUserName();
        InitializeUI();
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
                CheckForTrial(CREATING_GROUP);
            }
        });
        enableSwipeToDeleteAndUndo();
    }

    private void CheckForTrial(final int reqCode) {
        //free version
        if (listOfGroupName.size() < 1) {
            if (reqCode == CREATING_GROUP)
                RequestNewGroup();
        }
        //trial
        else {
            progressBar.setVisibility(View.VISIBLE);
            mRoot.child("payment").child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        PaymentObject object = snapshot.getValue(PaymentObject.class);
                        if (object.expired()) {
                            ExpiredDialog();
                        } else {
                            if (reqCode == CREATING_GROUP)
                                RequestNewGroup();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Not Exist Id", Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "Error in Checking " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
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

    private void attachValueListener() {
        if (mValueEventListenerGroup == null) {
            Log.v("progress", "start");
            progressBar_group_show.setVisibility(View.VISIBLE);
            final int[] count = {0};
            mValueEventListenerGroup = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        mGroupAdapter.clearAll();
                        final long groupCount = snapshot.getChildrenCount();

                        checkTrial(groupCount);

                        Iterator iterator = snapshot.getChildren().iterator();
                        while (iterator.hasNext()) {
                            DataSnapshot data = (DataSnapshot) iterator.next();
                            final String name = data.child("groupName").getValue().toString();
                            final String id = data.child("groupId").getValue().toString();
                            mGroupsRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    UpComingEvent event = snapshot.child("upComingEvent").getValue(UpComingEvent.class);
                                    mGroupAdapter.add(name, event, id);
                                    count[0]++;
                                    if (count[0] >= groupCount) {
                                        Log.v("progress", "end" + " " + count[0] + " " + snapshot.getChildrenCount());
                                        progressBar_group_show.setVisibility(View.GONE);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.v("progress", "end");
                                    progressBar_group_show.setVisibility(View.GONE);
                                    Toast.makeText(MainActivity.this, "error in reading upComing: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Not Exist", Toast.LENGTH_SHORT).show();
                        Log.v("progress", "end");
                        progressBar_group_show.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar_group_show.setVisibility(View.GONE);
                    Log.v("progress", "end");

                }
            };
        }
        mUsersRef.child(mCurrentUserId).child("Groups").addValueEventListener(mValueEventListenerGroup);
    }

    private void checkTrial(long count) {
        if (count > 1)
            mRoot.child("payment").child(mCurrentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        PaymentObject object = snapshot.getValue(PaymentObject.class);
                        if (object.expired()) {
                            ExpiredDialog();
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Snapshot not Exist", Toast.LENGTH_SHORT).show();
                        progressBar_group_show.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar_group_show.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, "error in checktrial " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
        mUsersRef.child(mCurrentUserId).child("Groups").child(groupId).setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
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
    protected void onStart() {
        super.onStart();
        attachValueListener();
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
    protected void onStop() {
        detachmentValueListener();
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
        mGroupAdapter.clearAll();
        detachmentValueListener();
        attachValueListener();
    }

    private void detachmentValueListener() {
        if (mValueEventListenerGroup != null) {
            progressBar_group_show.setVisibility(View.VISIBLE);
            mUsersRef.child(mCurrentUserId).child("Groups").removeEventListener(mValueEventListenerGroup);
            mValueEventListenerGroup = null;
        }
    }

    private void getCurrentUserName() {
        progressBar.setVisibility(View.VISIBLE);
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

    public class getCurrentUsernameTask extends AsyncTask<Void, Void, Void> {

        private DataSnapshot mSnapShot;

        public getCurrentUsernameTask(DataSnapshot snapshot) {
            mSnapShot = snapshot;
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

    private void ExpiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setIcon(R.drawable.attention_icon);
        builder.setTitle("You Are Out Of Trial");
        builder.setMessage("You Are Out Of Trial . Please Go To Payment Page and Submit New One ... ");
        builder.setCancelable(false);
        builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
//                finish();
//                return;
            }
        });

        builder.setPositiveButton("okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toPaymentPage();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthStateListener != null) {
            mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        }
    }

    public void gotoPaymentPage(View view) {
        toPaymentPage();
    }

    private void toPaymentPage() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("userId", mCurrentUserId);
        startActivity(intent);
    }

    private void enableSwipeToDeleteAndUndo() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(this) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

                final int position = viewHolder.getAdapterPosition();
                final String name = mGroupAdapter.getData().get(position);
                final String id = mGroupAdapter.getData().get(position);
//                mGroupAdapter.removeItem(position);
                clearGroupsForUser(position, id);

                Snackbar snackbar = Snackbar
                        .make(mLinearLayout, "Item was removed from the list.", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mGroupAdapter.restoreItem(name, id, position);
                        mGroup_rv.scrollToPosition(position);
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(mGroup_rv);
    }

    private void clearGroupsForUser(final int position, final String id) {
        progressBar.setVisibility(View.VISIBLE);
        mGroupsRef.child(id).child("members").child(mCurrentUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mUsersRef.child(mCurrentUserId).child("Groups").child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                mGroupAdapter.removeItem(position);
                            } else {
                                Toast.makeText(MainActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(MainActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}