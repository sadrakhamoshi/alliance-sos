package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.Adapters.showEvents;
import com.example.alliancesos.GroupSetting.GroupProfileActivity;
import com.example.alliancesos.SendNotificationPack.DataToSend;
import com.example.alliancesos.SendNotificationPack.SendingNotification;
import com.example.alliancesos.Utils.MessageType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Iterator;

public class GroupActivity extends AppCompatActivity {

    private String mCurrentUserName, mCurrentUserId;

    private ProgressBar mProgress;

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

    private StorageReference mSOSImagesRef;


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
        mSOSImagesRef = FirebaseStorage.getInstance().getReference().child("sos_images");

        //recycle view
        mRecyclerView = findViewById(R.id.event_list_rv);
        mEventList = new ArrayList<>();
        mShowEventAdapter = new showEvents(GroupActivity.this, mEventList, mCurrentGroupId);
        mRecyclerView.setAdapter(mShowEventAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(GroupActivity.this));

        mProgress = findViewById(R.id.progress_group_act);
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
                MakeAlertDialog();
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

    private void MakeAlertDialog() {
        final int[] whichOption = {0};
        String[] options = {"Write Own Message", "Send Picture", "Send Preset Message"};
        final EditText message = new EditText(GroupActivity.this);
        message.setHint("Write Your message...");
        final AlertDialog.Builder builder = new AlertDialog.Builder(GroupActivity.this, R.style.AlertDialog);
        builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
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
        mGroupRef.child(mCurrentGroupId).child("preset_message").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String message = snapshot.child("message").getValue().toString();
                    Toast.makeText(GroupActivity.this, message, Toast.LENGTH_SHORT).show();
                    if (message != null) {
                        DataToSend data = new DataToSend(mCurrentUserName, mCurrentGroupName, mCurrentGroupId, MessageType.SOS_TYPE);
                        data.setSosMessage(message);
                        SendingNotification sender = new SendingNotification(mCurrentGroupId, mCurrentGroupName,
                                mCurrentUserName, mCurrentUserId, GroupActivity.this, data);
                        sender.isInSendMode = true;
                        sender.Send();
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
                                        DataToSend dataToSend = new DataToSend(mCurrentUserName, mCurrentGroupName, mCurrentGroupId, MessageType.SOS_TYPE);
                                        dataToSend.setPhotoUrl(task.getResult().toString());
                                        SendingNotification sender = new SendingNotification(mCurrentGroupId, mCurrentGroupName,
                                                mCurrentUserName, mCurrentUserId, GroupActivity.this, dataToSend);
                                        sender.isInSendMode = true;
                                        sender.Send();
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    DataToSend data = new DataToSend(mCurrentUserName, mCurrentGroupName, mCurrentGroupId, MessageType.SOS_TYPE);
                    data.setSosMessage(message.getText().toString());
                    SendingNotification sender = new SendingNotification(mCurrentGroupId, mCurrentGroupName,
                            mCurrentUserName, mCurrentUserId, GroupActivity.this, data);
                    sender.isInSendMode = true;
                    sender.Send();
                }
            }).start();
        } else {
            Toast.makeText(GroupActivity.this, "Message Is Empty...", Toast.LENGTH_SHORT).show();
        }
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
                        if (event.getTimeInMillisecond() * -1 >= System.currentTimeMillis()) {
                            mShowEventAdapter.add(event);
                        } else {
                            //delete task fro data base
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
}