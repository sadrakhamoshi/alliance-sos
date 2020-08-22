package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

    private String mCurrentUserId;

    //database
    private DatabaseReference mRoot, mGroupsRef, mUsersRef;

    private ListView groups;
    private ArrayList<String> listOfAllGroups;
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

        InitializeUI();

    }

    private void InitializeUI() {
        groups = findViewById(R.id.list_view);
        listOfAllGroups = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listOfAllGroups);
        groups.setAdapter(arrayAdapter);

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

                    RefToCurrentUser(groupId);

                    showGroups();
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

    private void showGroups() {
        mRoot.child("groups").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();

                Set<String> all_groups = new HashSet<>();
                while (iterator.hasNext()) {

                    String name = ((DataSnapshot) iterator.next()).child("groupName").getValue().toString();

                    all_groups.add(name);
                }
                listOfAllGroups.clear();
                listOfAllGroups.addAll(all_groups);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void CreateNewGroup(final String groupName, final String groupId) {

        Groups groups = new Groups(groupName, groupId);

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

    private void RefToCurrentUser(String groupId) {
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

    @Override
    protected void onStart() {
        super.onStart();
        showGroups();
    }
}