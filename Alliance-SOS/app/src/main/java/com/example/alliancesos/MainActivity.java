package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


import com.example.alliancesos.DeviceAlarm.MyAlarmService;
import com.example.alliancesos.SendNotificationPack.NotificationResponseActivity;
import com.example.alliancesos.SendNotificationPack.Token;
import com.example.alliancesos.Setting.UserSettingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String mCurrentUserId, mCurrentUserName;

    private UserObject mCurrentUser;

    //database
    private DatabaseReference mRoot, mGroupsRef, mUsersRef;

    private ListView groups_listView;
    private ArrayList<String> listOfGroupName, listOfGroupId;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Initialize();
    }

    private void Initialize() {

        //auth
        mCurrentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //database
        mRoot = FirebaseDatabase.getInstance().getReference();
        mGroupsRef = mRoot.child("groups");
        mUsersRef = mRoot.child("users");

        getCurrentUserName();

        InitializeUI();

    }

    private void getCurrentUserName() {
        mUsersRef.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mCurrentUserName = snapshot.child("userName").getValue().toString();
                    Toast.makeText(MainActivity.this, "Current User Name Is " + mCurrentUserName, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void InitializeUI() {
        groups_listView = findViewById(R.id.list_view);
        listOfGroupId = new ArrayList<>();
        listOfGroupName = new ArrayList<>();

//        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfGroupId);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfGroupName);
        groups_listView.setAdapter(arrayAdapter);

        Button logOut = findViewById(R.id.log_out_btn);
        Button createGroup = findViewById(R.id.create_group_btn);

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestNewGroup();
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LogInPage.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
        });
        groups_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent toGroupActivity = new Intent(getApplicationContext(), GroupActivity.class);

                toGroupActivity.putExtra("groupName", listOfGroupName.get(position));
                toGroupActivity.putExtra("groupId", listOfGroupId.get(position));
                toGroupActivity.putExtra("currUserName", mCurrentUserName);
                toGroupActivity.putExtra("currUserId", mCurrentUserId);

                startActivity(toGroupActivity);
            }
        });
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter the Group name and Id :");

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
                    addGroupToUserSubset(groupName, groupId);

                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void showCurrentUserGroups() {
        mUsersRef.child(mCurrentUserId).child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();

                List<String> all_groups_name = new ArrayList<>();
                List<String> all_groups_id = new ArrayList<>();
                try {
                    while (iterator.hasNext()) {
                        DataSnapshot dataSnapshot = ((DataSnapshot) iterator.next());
                        String name = dataSnapshot.child("groupName").getValue().toString();
                        String id = dataSnapshot.child("groupId").getValue().toString();

                        all_groups_name.add(name);
                        all_groups_id.add(id);
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "There is No group yet..." + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
                listOfGroupName.clear();
                listOfGroupId.clear();
                listOfGroupId.addAll(all_groups_id);
                listOfGroupName.addAll(all_groups_name);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void CreateNewGroup(final String groupName, final String groupId) {

        Groups groups = new Groups(groupName, groupId, mCurrentUserId);

        mGroupsRef.child(groupId).setValue(groups).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, groupId + "  created Successfully ...", Toast.LENGTH_SHORT).show();
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGroupsRef.child(groupId).child("members").child(mCurrentUserId).setValue(new Member(mCurrentUserId, mCurrentUserName));
        Toast.makeText(this, "admin added to group ... ", Toast.LENGTH_SHORT).show();
    }

    private void addGroupToUserSubset(String groupName, String groupId) {
        HashMap<String, String> groupInfo = new HashMap<>();
        groupInfo.put("groupName", groupName);
        groupInfo.put("groupId", groupId);

        Toast.makeText(this, mCurrentUserName, Toast.LENGTH_SHORT).show();

        mUsersRef.child(mCurrentUserId).child("Groups").push().setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "add to users Groups", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                    mCurrentUser = new UserObject(id, userName, email, pass, token);
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

    private void UpdateToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                Token newToken = new Token(instanceIdResult.getToken());
                mUsersRef.child(mCurrentUserId).setValue(newToken);

                changeGroupMemberToken(newToken.getToken());
            }
        });
    }

    private void changeGroupMemberToken(final String s) {
        mUsersRef.child(mCurrentUserId).child("Groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterator iterator = snapshot.getChildren().iterator();
                    while (iterator.hasNext()) {

                        DataSnapshot dataSnapshot = (DataSnapshot) (iterator.next());
                        String groupId = dataSnapshot.child("groupId").getValue().toString();
                        groupMemberToken(s, groupId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "error in changeMessageMember " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void groupMemberToken(String s, String groupId) {
        mGroupsRef.child(groupId).child("members").child(mCurrentUserId).child("token").setValue(s);
    }


    @Override
    protected void onStart() {
        super.onStart();
        showCurrentUserGroups();
        getCurrentUserInfo();
        //permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, 200);
            }
        }
    }

    public void gotoUserProfile(View view) {
        Intent intent = new Intent(getApplicationContext(), UserSettingActivity.class);
        intent.putExtra("userId", mCurrentUserId);
        startActivity(intent);
    }
}