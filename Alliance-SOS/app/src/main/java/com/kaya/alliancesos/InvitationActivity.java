package com.kaya.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.kaya.alliancesos.Adapters.InvitePageAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class InvitationActivity extends AppCompatActivity {

    private DatabaseReference mRef;
    private TextView textView;
    private String mUserId;
    private ListView mListView;
    private InvitePageAdapter mAdapter;
    private List<InviteObject> mInviteObjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation);
        Intent intent = getIntent();
        if (intent != null) {
            mUserId = intent.getStringExtra("userId");
        } else {
            finish();
            return;
        }
        initialize();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mRef.child("invite").child(mUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Iterator iterator = snapshot.getChildren().iterator();
                    ArrayList<InviteObject> objects = new ArrayList<>();
                    while (iterator.hasNext()) {
                        DataSnapshot data = (DataSnapshot) iterator.next();
                        InviteObject objs = data.getValue(InviteObject.class);
                        objects.add(objs);
                    }
                    mAdapter.clear();
                    if (objects.size() == 0) {
                        textView.setVisibility(View.VISIBLE);
                    } else {
                        textView.setVisibility(View.GONE);
                    }
                    mAdapter.addAll(objects);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InvitationActivity.this, "Errors " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initialize() {
        mRef = FirebaseDatabase.getInstance().getReference();
        textView = findViewById(R.id.invite_page_txt);
        mListView = findViewById(R.id.invite_page_listview);
        mInviteObjects = new ArrayList<>();
        mAdapter = new InvitePageAdapter(this, mInviteObjects, mRef);
        mListView.setAdapter(mAdapter);
    }

    public void exitInvitation(View view) {
        finish();
        return;
    }
}