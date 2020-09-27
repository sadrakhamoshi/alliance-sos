package com.example.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.alliancesos.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class SOSResponseActivity extends AppCompatActivity {

    private ListView mListView;
    private ArrayList<String> mSosList;
    private ArrayAdapter<String> mAdapter;

    private String mGroupId, mSosId;

    private DatabaseReference mSosRef;
    private ValueEventListener mSosListener;

    private ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_s_o_s_response);
        getExtras();
        InitUi();
        Initialize();
    }

    private void Initialize() {
        mSosRef = FirebaseDatabase.getInstance().getReference().child("sos").child(mGroupId);
        attachedListenerToSosList();
    }

    private void attachedListenerToSosList() {
        if (mSosListener == null) {
            mSosListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        showSosTask showSosTask = new showSosTask(snapshot);
                        showSosTask.execute();
                    } else {
                        Toast.makeText(SOSResponseActivity.this, "Not Exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SOSResponseActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            mSosRef.addValueEventListener(mSosListener);
        }
    }

    private void InitUi() {
        mProgress = findViewById(R.id.progress_sos_log);
        mListView = findViewById(R.id.sos_list_view);
        mSosList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mSosList);
        mListView.setAdapter(mAdapter);
    }

    private void getExtras() {
        Intent fromSOSMessage = getIntent();
        if (fromSOSMessage != null) {
            mGroupId = fromSOSMessage.getStringExtra("groupId");
            mSosId = fromSOSMessage.getStringExtra("sosId");
        }
    }

    public void exitFromSOSLog(View view) {
        finish();
        return;
    }

    public class showSosTask extends AsyncTask<Void, Void, Void> {

        DataSnapshot snapshot;

        public showSosTask(DataSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgress.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Iterator iterator = snapshot.getChildren().iterator();
            mSosList.clear();

            while (iterator.hasNext()) {
                DataToSend dataSnapshot = ((DataSnapshot) iterator.next()).getValue(DataToSend.class);
                mSosList.add("Make By  :   " + dataSnapshot.getMakeBy());
            }
            return null;
        }
    }
}