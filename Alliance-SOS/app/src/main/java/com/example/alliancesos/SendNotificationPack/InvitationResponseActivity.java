package com.example.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.alliancesos.Member;
import com.example.alliancesos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Time;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class InvitationResponseActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private EditText mName_edt;
    private String mCurrUserId;
    private String mGroupId, mGroupName;
    DatabaseReference mGroupRef, mUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitation_response);
        Init();
    }

    private void Init() {
        Bundle bundle = getIntent().getExtras();
        try {
            mGroupId = bundle.getString("groupId");
            mGroupName = bundle.getString("groupName");
            mCurrUserId = bundle.getString("toId");
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        progressBar = findViewById(R.id.progress_invitation);

        mName_edt = findViewById(R.id.username_for_group_edt);
        mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");
        mUserRef = FirebaseDatabase.getInstance().getReference().child("users");
    }

    public void addUserToGroup(View view) {
        progressBar.setVisibility(View.VISIBLE);
        String username = mName_edt.getText().toString();
        if (TextUtils.isEmpty(username)) {
            MakeAlertDialog("Attention", "You Have to Set Name...");
        } else {
            Member member = new Member(mCurrUserId, username);
            mGroupRef.child(mGroupId).child("members").child(mCurrUserId).setValue(member).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        HashMap<String, String> groupInfo = new HashMap<>();
                        groupInfo.put("groupName", mGroupName);
                        groupInfo.put("groupId", mGroupId);
                        mUserRef.child(mCurrUserId).child("Groups").push().setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    MakeAlertDialog("Attention", "You Added to " + mGroupName);
                                } else {
                                    Toast.makeText(InvitationResponseActivity.this, "error in second complete " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(InvitationResponseActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

    public void notAddToGroup(View view) {

        MakeAlertDialog("Attention", "Okay Have Nice Day...");
    }

    private void MakeAlertDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(InvitationResponseActivity.this, R.style.AlertDialog);
        builder.setTitle(title);
        builder.setCancelable(false);
        builder.setIcon(R.drawable.sos_icon);
        builder.setMessage(message);
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        finish();
                        return;
                    }
                }, 600);
            }
        });
        builder.create().show();
    }
}