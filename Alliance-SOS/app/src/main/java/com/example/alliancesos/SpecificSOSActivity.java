package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.SendNotificationPack.DataToSend;
import com.example.alliancesos.SendNotificationPack.SOSLogActivity;
import com.example.alliancesos.Setting.ViewDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class SpecificSOSActivity extends AppCompatActivity {

    private String mGroupId, mSosId;
    private DatabaseReference mSosRef;
    private ViewDialog viewDialog;
    private TextView name, group, time, message;
    private ImageView image;
    private long sentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_s_o_s);
        getExtras();
        Init();
    }

    private void Init() {
        mSosRef = FirebaseDatabase.getInstance().getReference().child("sos").child(mGroupId);
        name = findViewById(R.id.specific_make_by);
        time = findViewById(R.id.specific_time);
        group = findViewById(R.id.specific_group_name);
        image = findViewById(R.id.specific_image);
        message = findViewById(R.id.specific_message);
        viewDialog = new ViewDialog(this);
    }

    private void getExtras() {
        Intent fromSOSMessage = getIntent();
        if (fromSOSMessage != null) {
            mGroupId = fromSOSMessage.getStringExtra("groupId");
            mSosId = fromSOSMessage.getStringExtra("sosId");
            sentTime = fromSOSMessage.getLongExtra("time", System.currentTimeMillis());
        }
    }

    public void BackToMainPage(View view) {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();
        return;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSosRef.child(mSosId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    showDetailTask showDetailTask = new showDetailTask(snapshot);
                    showDetailTask.execute();

                } else {
                    Toast.makeText(SpecificSOSActivity.this, "Not Exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SpecificSOSActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public class showDetailTask extends AsyncTask<Void, Void, Void> {

        DataSnapshot snapshot;
        DataToSend dataToSend;

        public showDetailTask(DataSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            image.setVisibility(View.VISIBLE);
            viewDialog.showDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Date date = new Date(sentTime);
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy hh:mm", Locale.ENGLISH);
            String sent = format.format(date);
            time.setText("Created time :  " + sent);
            group.setText("Group Name  : " + dataToSend.getGroupName());
            name.setText("Make By  : " + dataToSend.getMakeBy());
            if (dataToSend.getSosMessage() == null) {
                Picasso.get().load(dataToSend.getPhotoUrl()).into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        viewDialog.hideDialog();
                    }

                    @Override
                    public void onError(Exception e) {
                        viewDialog.hideDialog();
                        Toast.makeText(SpecificSOSActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                message.setText("Message  :  " + dataToSend.getSosMessage());
                viewDialog.hideDialog();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            dataToSend = snapshot.getValue(DataToSend.class);
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        return;
    }
}