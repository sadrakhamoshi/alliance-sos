package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class SpecificEventActivity extends AppCompatActivity {

    private ProgressBar mProgress;

    private String mUserId, mUsername;
    private Event mCurrentEvent;
    private TextView mTitle, mCreated, mDate, mDescribe;

    private ValueEventListener mMemberListener;

    private ListView mListView;
    private ArrayList<String> mNamesList;
    private ArrayAdapter<String> adapter;

    //database
    private String mGroupId;
    private DatabaseReference mGroupRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_event);
        Intent fromGroup = getIntent();
        if (fromGroup != null) {
            mCurrentEvent = (Event) fromGroup.getSerializableExtra("event");
            mGroupId = fromGroup.getStringExtra("groupId");
        }
        Init();
    }

    private void Init() {

        InitUI();
    }

    private void InitUI() {
        mListView = findViewById(R.id.list_view_event_page);
        mNamesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(SpecificEventActivity.this, android.R.layout.simple_list_item_1, mNamesList);
        mListView.setAdapter(adapter);

        mProgress = findViewById(R.id.progress_event_page);
        mTitle = findViewById(R.id.title_event_page);
        mDescribe = findViewById(R.id.describe_event_page);
        mDate = findViewById(R.id.date_event_page);
        mCreated = findViewById(R.id.created_event_page);
        mTitle.setText(mCurrentEvent.getScheduleObject().getTitle());
        mCreated.setText(mCurrentEvent.getCreatedBy());
        mDescribe.setText(mCurrentEvent.getScheduleObject().getDescription());
        mDate.setText(mCurrentEvent.getScheduleObject().GetDate());
    }

    public void joinEventPage(View view) {
        if (!mNamesList.contains(mUsername)) {
            mProgress.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HashMap<String, String> newUid = new HashMap<>();
                    newUid.put("id", mUserId);
                    mGroupRef.child("events").child(mCurrentEvent.getEventId()).child("members").child(mUserId).setValue(newUid);
                    mProgress.setVisibility(View.GONE);
                }
            }).start();
        } else {
            Toast.makeText(this, "You already in the event...", Toast.LENGTH_SHORT).show();
        }
    }

    public void backEventPage(View view) {
        finish();
        return;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mUserId == null) {
            mUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        if (mGroupRef == null)
            mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups").child(mGroupId);
        showMembersOfEvent();
        if (mUsername == null)
            getCurrentUsername();
    }

    private void getCurrentUsername() {
        mGroupRef.child("members").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mUsername = snapshot.child("userName").getValue().toString();
                } else {
                    Toast.makeText(SpecificEventActivity.this, "not exist ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SpecificEventActivity.this, "error in get current username " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMemberListener != null) {
            mGroupRef.child("events").child(mCurrentEvent.getEventId()).child("members").removeEventListener(mMemberListener);
        }
    }

    private void showMembersOfEvent() {
        if (mMemberListener == null) {
            mMemberListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mProgress.setVisibility(View.VISIBLE);
                    Iterator iterator = snapshot.getChildren().iterator();
                    mNamesList.clear();
                    while (iterator.hasNext()) {
                        String id = ((DataSnapshot) iterator.next()).getKey().toString();
                        goToGroupMembers(id);
                    }
                    mProgress.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    mProgress.setVisibility(View.GONE);
                    Toast.makeText(SpecificEventActivity.this, "Error in show :" + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            Toast.makeText(this, mCurrentEvent.getEventId(), Toast.LENGTH_SHORT).show();
            mGroupRef.child("events").child(mCurrentEvent.getEventId()).child("members").addValueEventListener(mMemberListener);
        }
    }

    private void goToGroupMembers(String id) {
        mGroupRef.child("members").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    String username = snapshot.child("userName").getValue().toString();
                    mNamesList.add(username);
                    adapter.notifyDataSetChanged();
                } else {
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SpecificEventActivity.this, "error in gotoGroupMembers :" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}