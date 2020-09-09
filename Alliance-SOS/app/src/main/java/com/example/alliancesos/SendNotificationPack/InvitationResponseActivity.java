package com.example.alliancesos.SendNotificationPack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alliancesos.Member;
import com.example.alliancesos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;

public class InvitationResponseActivity extends AppCompatActivity {

    private EditText mName_edt;
    private String mCurrUserId;
    private String mGroupId;

    DatabaseReference mGroupRef;

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
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        mName_edt = findViewById(R.id.username_for_group_edt);
        mCurrUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mGroupRef = FirebaseDatabase.getInstance().getReference().child("groups");
    }

    public void addUserToGroup(View view) {
        String username = mName_edt.getText().toString();
        if (TextUtils.isEmpty(username)) {
            MakeAlertDialog("Attention", "You Have to Set Name...");
        } else {
            Member member = new Member(mCurrUserId, username);
            mGroupRef.child(mGroupId).child("members").child(mCurrUserId).setValue(member).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(InvitationResponseActivity.this, "You Added to Group", Toast.LENGTH_SHORT).show();
                    } else {
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
                }, 1000);
            }
        });
        builder.create().show();
    }
}