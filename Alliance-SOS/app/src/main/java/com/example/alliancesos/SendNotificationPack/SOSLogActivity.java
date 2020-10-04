package com.example.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class SOSLogActivity extends AppCompatActivity {

    private ListView mListView;
    private ArrayList<String> mSosList;
    private ArrayList<DataToSend> mDataToSendList;
    private ArrayAdapter<String> mAdapter;

    private String mGroupId;

    private DatabaseReference mSosRef;
    private ValueEventListener mSosListener;


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
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                        Toast.makeText(SOSLogActivity.this, "Not Exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(SOSLogActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            };
            mSosRef.addValueEventListener(mSosListener);
        }
    }

    private void InitUi() {
        mListView = findViewById(R.id.sos_list_view);
        mSosList = new ArrayList<>();
        mDataToSendList = new ArrayList<>();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mSosList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                DialogDetailSos(position);
            }
        });
    }

    private void DialogDetailSos(int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Detail ");
        builder.setIcon(R.drawable.time_zone_icon);
        builder.setCancelable(true);
        final View detailView = CreateDetailView(position);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setView(detailView);
            }
        });
        builder.setNegativeButton("Ok", null);
        builder.create().show();
    }

    private View CreateDetailView(final int position) {
        View root = getLayoutInflater().inflate(R.layout.sos_detail, null, false);
        TextView group, name, message;
        final ImageView image;
        final ProgressBar progressBar = root.findViewById(R.id.progress_sos_log);
        group = root.findViewById(R.id.detail_group_name);
        name = root.findViewById(R.id.detail_make_name);
        message = root.findViewById(R.id.detail_message);
        image = root.findViewById(R.id.detail_image);
        group.setText("Group Name  : " + mDataToSendList.get(position).getGroupName());
        name.setText("Make By  : " + mDataToSendList.get(position).getMakeBy());
        if (mDataToSendList.get(position).getSosMessage() == null) {
            progressBar.setVisibility(View.VISIBLE);
            Picasso.get().load(mDataToSendList.get(position).getPhotoUrl()).into(image, new Callback() {
                @Override
                public void onSuccess() {
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(SOSLogActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            mAdapter.notifyDataSetChanged();

        } else {
            message.setText("Message  : " + mDataToSendList.get(position).getSosMessage());
            progressBar.setVisibility(View.GONE);
        }
        return root;
    }

    private void getExtras() {
        Intent fromSOSMessage = getIntent();
        if (fromSOSMessage != null) {
            mGroupId = fromSOSMessage.getStringExtra("groupId");
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
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Iterator iterator = snapshot.getChildren().iterator();
            mSosList.clear();
            mDataToSendList.clear();
            while (iterator.hasNext()) {
                DataToSend dataSnapshot = ((DataSnapshot) iterator.next()).getValue(DataToSend.class);
                mDataToSendList.add(dataSnapshot);
                mSosList.add("Make By  :   " + dataSnapshot.getMakeBy());
            }
            Collections.reverse(mDataToSendList);
            return null;
        }
    }
}