package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupActivity extends AppCompatActivity {

    //database
    private DatabaseReference mGroupRef;

    private String mCurrentGroupId;

    private Button mMembersList;
    private Button mGroupList;

    private TextView mSchedule;

    private ListView listView;
    private ArrayList<String> mScheduleList;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        //String groupName = getIntent().getStringExtra("groupName");
        mCurrentGroupId = getIntent().getStringExtra("groupId");

        InitializeUI();
    }

    private void InitializeUI() {
        //database
        mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");

        listView = findViewById(R.id.schedule_list_view);
        mScheduleList = new ArrayList<>();
        arrayAdapter = new ArrayAdapter<>(GroupActivity.this, android.R.layout.simple_list_item_1, mScheduleList);


        TextView nameGroup = findViewById(R.id.group_name_txt);
        nameGroup.setText(mCurrentGroupId);

        mSchedule = findViewById(R.id.schedule_event);
        mSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSetScheduleEvent();
            }
        });

        Button b1 = findViewById(R.id.help_us_btn);
        Button b2 = findViewById(R.id.user_setting_btn);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GroupActivity.this, "Will go to Help Us Activity ...", Toast.LENGTH_SHORT).show();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GroupActivity.this, "Will go to Users Setting Activity ...", Toast.LENGTH_SHORT).show();
            }
        });


        mMembersList = findViewById(R.id.member_list_btn);
        mMembersList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toMember = new Intent(getApplicationContext(), MemberActivity.class);
                toMember.putExtra("groupId", mCurrentGroupId);
                startActivity(toMember);
            }
        });
        mGroupList = findViewById(R.id.groups_list_btn);
        mGroupList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            }
        });

    }

    private void goToSetScheduleEvent() {
        Intent intent = new Intent(getApplicationContext(), SetScheduleActivity.class);
        intent.putExtra("groupId", mCurrentGroupId);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showAllEvent();
    }

    private void showAllEvent() {
        mGroupRef.child(mCurrentGroupId).child("events").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterator iterator = snapshot.getChildren().iterator();
                    Set<String> set = new HashSet<>();
                    while (iterator.hasNext()) {
                        try {
                            Message message = ((DataSnapshot) (iterator.next())).getValue(Message.class);
                            if (message != null) {
                                String tmp = "";
                                tmp += message.getScheduleObject().getTitle() + "\n";
                                tmp += message.getScheduleObject().getDescription() + "\n";
                                tmp += message.getCreatedBy() + "\n";
                                tmp += message.getCreatedTime() ;
                                set.add(tmp);
                            }
                        } catch (Exception e) {
                            Toast.makeText(GroupActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        mScheduleList.clear();
                        mScheduleList.addAll(set);
                        listView.setAdapter(arrayAdapter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupActivity.this, "error occured in showAllEvent " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}