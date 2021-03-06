package com.kaya.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kaya.alliancesos.Member;
import com.kaya.alliancesos.Payment.PaymentActivity;
import com.kaya.alliancesos.Payment.PaymentObject;
import com.kaya.alliancesos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class InvitationResponseActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private String mInviteId;
    private EditText mName_edt;
    private String mCurrUserId;
    private String mGroupId, mGroupName;
    DatabaseReference mGroupRef, mUserRef;
    private DatabaseReference mRoot;

    private boolean mIsFirstTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsFirstTime = false;
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
        mRoot = FirebaseDatabase.getInstance().getReference();
        mGroupRef = mRoot.child("groups");
        mUserRef = mRoot.child("users");
    }

    public void addUserToGroup(View view) {
        mUserRef.child(mCurrUserId).child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild(mGroupId)) {
                        Toast.makeText(InvitationResponseActivity.this, "You have been in this group", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        addMember();
                    }
                } else {
                    addMember();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InvitationResponseActivity.this, "onCancle " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void addMember() {
        final String username = mName_edt.getText().toString();

        if (TextUtils.isEmpty(username)) {
            MakeAlertDialog("Attention", "You Have to Set Name...");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            mUserRef.child(mCurrUserId).child("Groups").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        //has group
                        if (snapshot.getChildrenCount() > 0)
                            CheckTrial(username);

                    } else {
                        //don't have any group
                        mIsFirstTime = true;
                        addingMemberFunction(username);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(InvitationResponseActivity.this, "Error " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            CheckTrial(username);
        }
    }

    private void addingMemberFunction(String username) {
        Member member = new Member(mCurrUserId, username);
        mGroupRef.child(mGroupId).child("members").child(mCurrUserId).setValue(member).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    HashMap<String, String> groupInfo = new HashMap<>();
                    groupInfo.put("groupName", mGroupName);
                    groupInfo.put("groupId", mGroupId);
                    mUserRef.child(mCurrUserId).child("Groups").child(mGroupId).setValue(groupInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    private void CheckTrial(final String username) {
        mRoot.child("payment").child(mCurrUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    PaymentObject object = snapshot.getValue(PaymentObject.class);
                    if (object.expired()) {
                        progressBar.setVisibility(View.GONE);
                        ExpiredDialog();
                    } else {
                        addingMemberFunction(username);
                    }

                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(InvitationResponseActivity.this, "notExist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(InvitationResponseActivity.this, "Error in " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ExpiredDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setIcon(R.drawable.attention_icon);
        builder.setTitle("You Are Out Of Trial");
        builder.setMessage("You Are Out Of Trial .If You Wanna Accept This Invitation, Please Go To Payment Page and Submit New One ... ");
        builder.setCancelable(false);
        builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();
                return;
            }
        });

        builder.setPositiveButton("okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                toPaymentPage();
            }
        });
        if (!mIsFirstTime)
            builder.create().show();
    }

    private void toPaymentPage() {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("userId", mCurrUserId);
        startActivity(intent);
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