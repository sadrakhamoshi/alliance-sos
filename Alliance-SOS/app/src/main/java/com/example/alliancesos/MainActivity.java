package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private String mCurrentUserId, mCurrentUserName;

    private UserObject mCurrentUser;

    //database
    private DatabaseReference mRoot, mGroupsRef, mUsersRef;

    private ListView groups_listView;
    private ArrayList<String> listOfAllGroups, listOfGroupId;
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
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfGroupId);
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
                    addGroupToUserSubset(groupId);

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

                //Set<String> all_groups = new HashSet<>();
                Set<String> all_groups_id = new HashSet<>();
                while (iterator.hasNext()) {

                    DataSnapshot dataSnapshot = ((DataSnapshot) iterator.next());
                    //String name = dataSnapshot.child("groupName").getValue().toString();
                    String id = dataSnapshot.getValue().toString();

                    //all_groups.add(name);
                    all_groups_id.add(id);
                }
                //listOfAllGroups.clear();
                listOfGroupId.clear();
                listOfGroupId.addAll(all_groups_id);
                //listOfAllGroups.addAll(all_groups);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void CreateNewGroup(final String groupName, final String groupId) {

        Groups groups = new Groups(groupName, groupId, mCurrentUserName);

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
    }

    private void addGroupToUserSubset(String groupId) {
        Toast.makeText(this, mCurrentUserId, Toast.LENGTH_SHORT).show();
        mUsersRef.child(mCurrentUserId).child("Groups").push().setValue(groupId).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    @Override
    protected void onStart() {
        super.onStart();
        showCurrentUserGroups();
        //getCurrentUserInfo();
    }
}