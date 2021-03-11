package com.kaya.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kaya.alliancesos.Adapters.EventLogAdapter;

import java.util.ArrayList;
import java.util.Iterator;

public class EventLogActivity extends AppCompatActivity {

    private DatabaseReference mRef;
    private ArrayList<Event> mLists;
    private EventLogAdapter mAdapter;
    private ListView listView;
    private String mGroupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_log);
        Intent fromgroup = getIntent();
        if (fromgroup != null) {
            mGroupId = fromgroup.getStringExtra("groupId");
        }
        initUi();
    }

    private void initUi() {
        mRef = FirebaseDatabase.getInstance().getReference().child("groups").child(mGroupId).child("events");
        listView = findViewById(R.id.event_log_listView);
        mLists = new ArrayList<>();
        mAdapter = new EventLogAdapter(this, mLists);
        listView.setAdapter(mAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.clear();
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterator iterator = snapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        DataSnapshot dataSnapshot = (DataSnapshot) iterator.next();
                        Event event = dataSnapshot.getValue(Event.class);
                        mAdapter.add(event);
                    }
                } else {
                    Toast.makeText(EventLogActivity.this, "messages : No Events yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EventLogActivity.this, "messages:" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void exitFromEventLog(View view) {
        finish();
        return;
    }
}