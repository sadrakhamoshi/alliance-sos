package com.example.alliancesos.GroupSetting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.Member;
import com.example.alliancesos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupProfileActivity extends AppCompatActivity {

    private ImageView mEdit_image, mExit_image, mBackGroupImage;
    private TextView mGroupName_txt;
    private CheckBox mNoDisturb;
    private EditText mUsername_edt;
    private Button mUpdate_btn;
    private CircleImageView mGroupImage;

    private String mGroupId, mGroupName, mUserId;

    private String mUpdatedUsername;
    private boolean mUpdatedNotDisturb;

    private DatabaseReference mRootRef;

    private boolean edit_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        Intent fromGroupAct = getIntent();

        if (fromGroupAct != null) {
            mGroupId = fromGroupAct.getStringExtra("groupId");
            mGroupName = fromGroupAct.getStringExtra("groupName");
            mUserId = fromGroupAct.getStringExtra("userId");
        }
        Init();
    }

    private void Init() {
        edit_mode = false;

        mRootRef = FirebaseDatabase.getInstance().getReference();
        InitUI();
        mEdit_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEditMode();
            }
        });
        mExit_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitEditMode();
            }
        });
    }

    private void exitEditMode() {
        mUsername_edt.setText(mUpdatedUsername);
        mNoDisturb.setChecked(mUpdatedNotDisturb);
        mUpdate_btn.setVisibility(View.GONE);
        mNoDisturb.setEnabled(false);
        mUsername_edt.setEnabled(false);
        mExit_image.setVisibility(View.GONE);
        mEdit_image.setVisibility(View.VISIBLE);
        edit_mode = false;
    }

    private void goToEditMode() {
        mUpdate_btn.setVisibility(View.VISIBLE);
        mNoDisturb.setEnabled(true);
        mUsername_edt.setEnabled(true);
        mExit_image.setVisibility(View.VISIBLE);
        mEdit_image.setVisibility(View.GONE);
        edit_mode = true;
    }

    private void InitUI() {
        mEdit_image = findViewById(R.id.edit_group_setting);
        mExit_image = findViewById(R.id.exit_edit_group_setting);
        mBackGroupImage = findViewById(R.id.back_groupImage_group_setting);

        mGroupName_txt = findViewById(R.id.groupName_group_setting);
        mGroupName_txt.setText(mGroupName);

        mNoDisturb = findViewById(R.id.disturb_group_setting);
        mUsername_edt = findViewById(R.id.username_group_setting);
        mGroupImage = findViewById(R.id.groupImage_group_setting);
        mUpdate_btn = findViewById(R.id.update_group_setting);
    }


    public void updateGroupProfile(View view) {
        String newUsername = mUsername_edt.getText().toString();
        boolean isCheck = mNoDisturb.isChecked();
        if (TextUtils.isEmpty(newUsername)) {
            Toast.makeText(this, "username can't be empty...", Toast.LENGTH_SHORT).show();
        } else {
            final Member newMember = new Member(mUserId, newUsername, isCheck);
            mRootRef.child("groups").child(mGroupId).child("members").child(mUserId).setValue(newMember).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        mUpdatedUsername = newMember.getUserName();
                        mUpdatedNotDisturb = newMember.isNotDisturb();
                        Toast.makeText(GroupProfileActivity.this, "added to database...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(GroupProfileActivity.this, "can't add to database", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void pickImageForGroup(View view) {
        if (edit_mode) {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        forTestAlertDiaolag();
        getBasicInfo();
    }

    private void forTestAlertDiaolag() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupProfileActivity.this, R.style.AlertDialog);
        builder.setTitle("Attention!!");
        builder.setMessage("For Edit please click on edit button top right. after update click on check to exit from edit mode.");
        builder.setIcon(R.drawable.sos_icon);
        builder.setCancelable(false);
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void getBasicInfo() {
        mRootRef.child("groups").child(mGroupId).child("members").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mUpdatedNotDisturb = snapshot.child("notDisturb").getValue(Boolean.class);
                    mUpdatedUsername = snapshot.child("userName").getValue().toString();
                    mUsername_edt.setText(mUpdatedUsername);
                    mNoDisturb.setChecked(mUpdatedNotDisturb);
                } else {
                    Toast.makeText(GroupProfileActivity.this, "snapshot not exist...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupProfileActivity.this, "error in onStart " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}