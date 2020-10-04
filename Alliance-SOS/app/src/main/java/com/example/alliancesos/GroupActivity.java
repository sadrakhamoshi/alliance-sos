package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.Adapters.showEvents;
import com.example.alliancesos.GroupSetting.GroupProfileActivity;
import com.example.alliancesos.SendNotificationPack.DataToSend;
import com.example.alliancesos.SendNotificationPack.SOSLogActivity;
import com.example.alliancesos.SendNotificationPack.SendingNotification;
import com.example.alliancesos.Utils.MessageType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class GroupActivity extends AppCompatActivity {

    private String mCurrentUserName, mCurrentUserId;

    private ProgressBar mProgress;

    //database
    private DatabaseReference mGroupRef;

    private String mCurrentGroupId, mCurrentGroupName;

    private Button mSOS_btn;
    private TextView mSchedule;

    private RecyclerView mRecyclerView;
    private showEvents mShowEventAdapter;
    private ArrayList<Event> mEventList;

    private ChildEventListener mEventListener;

    private StorageReference mSOSImagesRef;

    private DatabaseReference mRootRef;


    //appbar
    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;

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
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mGroupRef = mRootRef.child("groups");
        mSOSImagesRef = FirebaseStorage.getInstance().getReference().child("sos_images");

        //recycle view
        mRecyclerView = findViewById(R.id.event_list_rv);
        mEventList = new ArrayList<>();
        mShowEventAdapter = new showEvents(GroupActivity.this, mEventList, mCurrentGroupId);
        mRecyclerView.setAdapter(mShowEventAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(GroupActivity.this));

        mProgress = findViewById(R.id.progress_group_act);

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
                MakeAlertDialog();
            }
        });

        //appbar
        mToolbar = findViewById(R.id.group_toolbar);
        mAppBarLayout = findViewById(R.id.group_appbar);
        setSupportActionBar(mToolbar);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(mCurrentGroupName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.add(0, 1, 1, menuIconWithText(getResources().getDrawable(R.drawable.members_vector, getTheme()), "Add Member"));
        menu.add(0, 2, 2, menuIconWithText(getResources().getDrawable(R.drawable.setting_vector, getTheme()), "Group Setting"));
        menu.add(0, 3, 3, menuIconWithText(getResources().getDrawable(R.drawable.help_vector, getTheme()), "Help Us"));
        return true;
    }

    private CharSequence menuIconWithText(Drawable r, String title) {
        r.setBounds(0, 0, r.getIntrinsicWidth(), r.getIntrinsicHeight());
        SpannableString sb = new SpannableString("    " + title);
        ImageSpan imageSpan = new ImageSpan(r, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case 3:
                Toast.makeText(this, "It Will Works Soon ...", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            case 1:
                Intent toMember = new Intent(getApplicationContext(), MemberActivity.class);
                toMember.putExtra("groupId", mCurrentGroupId);
                toMember.putExtra("groupName", mCurrentGroupName);
                toMember.putExtra("currUsername", mCurrentUserName);
                toMember.putExtra("currUserId", mCurrentUserId);
                startActivity(toMember);
                break;
            case 2:
                Intent goToGroupProfile = new Intent(getApplicationContext(), GroupProfileActivity.class);
                goToGroupProfile.putExtra("groupId", mCurrentGroupId);
                goToGroupProfile.putExtra("groupName", mCurrentGroupName);
                goToGroupProfile.putExtra("userId", mCurrentUserId);
                startActivity(goToGroupProfile);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void MakeAlertDialog() {
        final int[] whichOption = {2};
        String[] options = {"Write Own Message", "Send Picture", "Send Preset Message"};
        final EditText message = new EditText(GroupActivity.this);
        message.setHint("Write Your message...");
        final AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this, R.style.AlertDialog);
        builder.setSingleChoiceItems(options, 2, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                whichOption[0] = which;
                if (which == 0) {
                    if (message.getParent() != null)
                        ((ViewGroup) message.getParent()).removeView(message);
                    builder.setView(message);
                    builder.create().show();
                }
            }
        });
        builder.setCancelable(true);
        builder.setPositiveButton("send sos", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (whichOption[0]) {
                    case 0:
                        sendOwnMessage(message);
                        break;
                    case 1:
                        pickAndSendPicture();
                        break;
                    case 2:
                        ChoseFromPreset();
                        break;
                }
            }
        });
        builder.setNegativeButton("cancel", null);
        builder.create().show();
    }

    private void ChoseFromPreset() {
        mGroupRef.child(mCurrentGroupId).child("preset_message").child("message").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String message = snapshot.getValue().toString();
                    Toast.makeText(GroupActivity.this, message, Toast.LENGTH_SHORT).show();
                    if (message != null) {
                        String key = mRootRef.child("sos").child(mCurrentGroupId).push().getKey();
                        DataToSend data = new DataToSend(mCurrentUserName, mCurrentGroupName, mCurrentGroupId, MessageType.SOS_TYPE);
                        data.setSosMessage(message);
                        data.setSosId(key);
                        SendingNotification sender = new SendingNotification(mCurrentGroupId, mCurrentGroupName,
                                mCurrentUserName, mCurrentUserId, GroupActivity.this, data);
                        sender.isInSendMode = true;
                        sender.Send();
                        AddSOSToDB addSOSToDB = new AddSOSToDB(data);
                        addSOSToDB.execute();
                    } else {
                        Toast.makeText(GroupActivity.this, "No message is exist", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(GroupActivity.this, "not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupActivity.this, "error in chose From preset " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickAndSendPicture() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(GroupActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        else {
            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                final Uri photo = result.getUri();
                final StorageReference photoPath = mSOSImagesRef.child(mCurrentUserId + "sos.jpg");
                photoPath.putFile(photo).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.v("taskk", "Successful");
                            photoPath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Log.v("taskk", "Successful");
                                        String key = mRootRef.child("sos").child(mCurrentGroupId).push().getKey();
                                        DataToSend dataToSend = new DataToSend(mCurrentUserName, mCurrentGroupName, mCurrentGroupId, MessageType.SOS_TYPE);
                                        dataToSend.setPhotoUrl(task.getResult().toString());
                                        dataToSend.setSosId(key);
                                        SendingNotification sender = new SendingNotification(mCurrentGroupId, mCurrentGroupName,
                                                mCurrentUserName, mCurrentUserId, GroupActivity.this, dataToSend);
                                        sender.isInSendMode = true;
                                        sender.Send();
                                        AddSOSToDB addSOSToDB = new AddSOSToDB(dataToSend);
                                        addSOSToDB.execute();

                                    } else {
                                        Toast.makeText(GroupActivity.this, "Could'nt get download url", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(GroupActivity.this, "Erorr in put photo...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }


    private void sendOwnMessage(final EditText message) {
        if (!TextUtils.isEmpty(message.getText())) {
            String key = mRootRef.child("sos").child(mCurrentGroupId).push().getKey();
            DataToSend data = new DataToSend(mCurrentUserName, mCurrentGroupName, mCurrentGroupId, MessageType.SOS_TYPE);
            data.setSosMessage(message.getText().toString());
            data.setSosId(key);
            SendingNotification sender = new SendingNotification(mCurrentGroupId, mCurrentGroupName,
                    mCurrentUserName, mCurrentUserId, GroupActivity.this, data);
            sender.isInSendMode = true;
            sender.Send();
            AddSOSToDB addSOSToDB = new AddSOSToDB(data);
            addSOSToDB.execute();

        } else {
            Toast.makeText(GroupActivity.this, "Message Is Empty...", Toast.LENGTH_SHORT).show();
        }
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
            final Date date = new Date();
            final long time = date.getTime() - 1000 * 60;
            mEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    try {
                        Event event = snapshot.getValue(Event.class);
                        Long value = event.getTimeInMillisecond() * -1;
                        if (time < value) {
                            mShowEventAdapter.add(event);
                        } else {
                            //delete task from data base
                        }
                        mProgress.setVisibility(View.GONE);
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

    public void goToSOSLog(View view) {
        Intent intent = new Intent(GroupActivity.this, SOSLogActivity.class);
        intent.putExtra("groupId", mCurrentGroupId);
        startActivity(intent);
    }

    private class AddSOSToDB extends AsyncTask<Void, Void, Void> {

        private DataToSend dataToSend;
        private String message;

        public AddSOSToDB(DataToSend obj) {
            dataToSend = obj;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgress.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(message))
                Toast.makeText(GroupActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            mRootRef.child("sos").child(mCurrentGroupId).child(dataToSend.getSosId()).setValue(dataToSend).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (!task.isSuccessful()) {
                        message = task.getException().getMessage();
                    }
                }
            });
            return null;
        }
    }
}