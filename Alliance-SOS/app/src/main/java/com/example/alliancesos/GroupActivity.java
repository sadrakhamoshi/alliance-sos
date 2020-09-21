package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.Adapters.showEvents;
import com.example.alliancesos.GroupSetting.GroupProfileActivity;
import com.example.alliancesos.SendNotificationPack.DataToSend;
import com.example.alliancesos.SendNotificationPack.SendingNotification;
import com.example.alliancesos.Setting.UserSettingActivity;
import com.example.alliancesos.Utils.MessageType;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class GroupActivity extends AppCompatActivity {

    private String mCurrentUserName, mCurrentUserId;

    //database
    private DatabaseReference mGroupRef;

    private String mCurrentGroupId, mCurrentGroupName;

    private Button mMembersList;
    private Button mGroupList;
    private Button mSOS_btn;

    private TextView mSchedule;

    private RecyclerView mRecyclerView;
    private showEvents mShowEventAdapter;
    private ArrayList<Event> mEventList;

    private ChildEventListener mEventListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        mCurrentGroupId = getIntent().getStringExtra("groupId");
        mCurrentUserId = getIntent().getStringExtra("currUserId");
        mCurrentUserName = getIntent().getStringExtra("currUserName");
        mCurrentGroupName = getIntent().getStringExtra("groupName");

        InitializeUI();
    }

    private void InitializeUI() {
        //database
        mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");

        //recycle view
        mRecyclerView = findViewById(R.id.event_list_rv);
        mEventList = new ArrayList<>();
        mShowEventAdapter = new showEvents(GroupActivity.this, mEventList, mCurrentGroupId);
        mRecyclerView.setAdapter(mShowEventAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(GroupActivity.this));

        TextView nameGroup = findViewById(R.id.group_name_txt);
        nameGroup.setText(mCurrentGroupName);

        mSchedule = findViewById(R.id.schedule_event);
        mSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSetScheduleEvent();
            }
        });

        mSOS_btn = findViewById(R.id.sos_btn);
        mSOS_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataToSend data = new DataToSend(mCurrentUserName, mCurrentGroupName, mCurrentGroupId, MessageType.SOS_TYPE);
                SendingNotification sender = new SendingNotification(mCurrentGroupId, mCurrentGroupName,
                        mCurrentUserName, mCurrentUserId, GroupActivity.this, data);
                sender.isInSendMode = true;
                sender.Send();
            }
        });

        mMembersList = findViewById(R.id.member_list_btn);
        mMembersList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toMember = new Intent(getApplicationContext(), MemberActivity.class);
                toMember.putExtra("groupId", mCurrentGroupId);
                toMember.putExtra("groupName", mCurrentGroupName);
                toMember.putExtra("currUsername", mCurrentUserName);
                toMember.putExtra("currUserId", mCurrentUserId);
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

    public void goToHelpUs(View view) {
        Toast.makeText(this, "It Will Works Soon ...", Toast.LENGTH_SHORT).show();
    }

    public void gotoGroupSetting(View view) {
        Intent goToGroupProfile = new Intent(getApplicationContext(), GroupProfileActivity.class);
        goToGroupProfile.putExtra("groupId", mCurrentGroupId);
        goToGroupProfile.putExtra("groupName", mCurrentGroupName);
        goToGroupProfile.putExtra("userId", mCurrentUserId);

        startActivity(goToGroupProfile);
    }

    private void goToSetScheduleEvent() {
        Intent intent = new Intent(getApplicationContext(), SetScheduleActivity.class);

        intent.putExtra("groupId", mCurrentGroupId);
        intent.putExtra("currUserName", mCurrentUserName);
        intent.putExtra("currUserId", mCurrentUserId);
        intent.putExtra("groupName", mCurrentGroupName);

        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showAllEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mEventListener != null) {
            mGroupRef.child(mCurrentGroupId).child("events").removeEventListener(mEventListener);
            mEventListener = null;
        }
    }

    private void attachListener() {
        if (mEventListener == null) {
            mEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    try {
                        Event event = snapshot.getValue(Event.class);
                        mShowEventAdapter.add(event);
                    } catch (Exception e) {
                        Toast.makeText(GroupActivity.this, "Error in cast :" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
            mGroupRef.child(mCurrentGroupId).child("events").addChildEventListener(mEventListener);
        }
    }

    private void showAllEvent() {
        attachListener();
    }
}