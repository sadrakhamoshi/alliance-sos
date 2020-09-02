package com.example.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.alliancesos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class NotificationResponseActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> mNames;
    private ArrayAdapter<String> adapter;

    private String mGroupId, mEventId;

    private String mCurrUsername, mCurrUserId;

    private DatabaseReference mGroupRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_response);
        Initialize();

    }

    private void Initialize() {

        //database
        mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");

        getExtra();

        InitUI();
    }


    private void getExtra() {
        Bundle bundle = getIntent().getExtras();
        mGroupId = bundle.getString("groupId");
        mEventId = bundle.getString("eventId");
        if (TextUtils.isEmpty(mEventId))
            finish();
        else {
            mCurrUserId = bundle.getString("toId");
            mCurrUsername = bundle.getString("toName");
            Toast.makeText(this, mEventId + " " + mCurrUsername + " " + mCurrUserId, Toast.LENGTH_SHORT).show();
        }
    }

    public void JoinEvent(View view) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("name", mCurrUsername);
        hashMap.put("userId", mCurrUserId);
        mGroupRef.child(mGroupId).child("events").child(mEventId).child("members").push().setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(NotificationResponseActivity.this, "Member added to event members", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(NotificationResponseActivity.this, "error in joinEvent" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void showAttendingMembers() {
        mGroupRef.child(mGroupId).child("events").child(mEventId).child("members").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    Iterator iterator = snapshot.getChildren().iterator();
                    List<String> names = new ArrayList<>();
                    while (iterator.hasNext()) {
                        DataSnapshot dataSnapshot = (DataSnapshot) (iterator.next());
                        String name = dataSnapshot.child("name").getValue().toString();
                        names.add(name);
                    }
                    mNames.clear();
                    mNames.addAll(names);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationResponseActivity.this, "Error in ShowAttending members " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void InitUI() {
        mNames = new ArrayList<>();
        listView = findViewById(R.id.event_member_listView);
        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, mNames);
        listView.setAdapter(adapter);
    }

    public void NotJoining(View view) {
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showAttendingMembers();
    }


}