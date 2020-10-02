package com.example.alliancesos.GroupSetting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.alliancesos.Member;
import com.example.alliancesos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupProfileActivity extends AppCompatActivity {


    private ImageView mEdit_image, mExit_image, mBackGroupImage;
    private TextView mGroupName_txt, mChosePhoto_txt;
    private CheckBox mNoDisturb;
    private EditText mUsername_edt, mPreset;
    private Button mUpdate_btn, mSavePreset;
    private CircleImageView mGroupImage;

    private String mGroupId, mGroupName, mUserId;

    private String mUpdatedUsername;
    private boolean mUpdatedNotDisturb, mCanChangeGroupImage;

    private DatabaseReference mRootRef;
    private StorageReference mGroupProfileRef;

    private boolean edit_mode;

    private ProgressBar mProgress;


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
        mGroupProfileRef = FirebaseStorage.getInstance().getReference().child("group-profile-Images");
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
        mChosePhoto_txt.setVisibility(View.GONE);
        mPreset.setEnabled(false);
        mPreset.setText("");
        mPreset.setHint("Write new Preset message...");
        mSavePreset.setEnabled(false);
        edit_mode = false;
    }

    private void goToEditMode() {
        mUpdate_btn.setVisibility(View.VISIBLE);
        mNoDisturb.setEnabled(true);
        mUsername_edt.setEnabled(true);
        mExit_image.setVisibility(View.VISIBLE);
        mEdit_image.setVisibility(View.GONE);
        mChosePhoto_txt.setVisibility(View.VISIBLE);
        mPreset.setEnabled(true);
        mSavePreset.setEnabled(true);
        edit_mode = true;
    }

    private void InitUI() {
        mProgress = findViewById(R.id.loading_bar_group_setting);
        mSavePreset = findViewById(R.id.save_preset_group_setting);
        mEdit_image = findViewById(R.id.edit_group_setting);
        mExit_image = findViewById(R.id.exit_edit_group_setting);
        mBackGroupImage = findViewById(R.id.back_groupImage_group_setting);

        mGroupName_txt = findViewById(R.id.groupName_group_setting);
        mGroupName_txt.setText(mGroupName);

        mChosePhoto_txt = findViewById(R.id.chose_photo_group_txt);

        mNoDisturb = findViewById(R.id.disturb_group_setting);
        mUsername_edt = findViewById(R.id.username_group_setting);
        mGroupImage = findViewById(R.id.groupImage_group_setting);
        mUpdate_btn = findViewById(R.id.update_group_setting);
        mPreset = findViewById(R.id.preset_message_group_setting);
    }


    public void updateGroupProfile(View view) {
        String newUsername = mUsername_edt.getText().toString();
        boolean isCheck = mNoDisturb.isChecked();
        if (TextUtils.isEmpty(newUsername)) {

            Toast.makeText(this, "username can't be empty...", Toast.LENGTH_SHORT).show();
        } else {
            final Member newMember = new Member(mUserId, newUsername, isCheck);
            newMember.setCanChangeGroupImage(mCanChangeGroupImage);
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
            if (!mCanChangeGroupImage) {
                Toast.makeText(this, "You Don't have Admin Permission to change Image", Toast.LENGTH_LONG).show();
            } else {
                pickImageFromGallery();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (edit_mode) {
            exitEditMode();
        }
        super.onBackPressed();
    }

    private void pickImageFromGallery() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(GroupProfileActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            final Uri photo = result.getUri();
            mProgress.setVisibility(View.VISIBLE);

            final StorageReference filepath = mGroupProfileRef.child(mGroupId + ".jpg");
            filepath.putFile(photo).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {

                        filepath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    //get download url
                                    String downloadURL = task.getResult().toString();

                                    //add to image of data base
                                    mRootRef.child("groups").child(mGroupId).child("image").setValue(downloadURL).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Glide.with(getApplicationContext())
                                                        .load(photo)
                                                        .into(mBackGroupImage);

                                                Glide.with(getApplicationContext())
                                                        .load(photo)
                                                        .into(mGroupImage);
                                                mProgress.setVisibility(View.GONE);

                                            } else {
                                                mProgress.setVisibility(View.GONE);
                                                Toast.makeText(GroupProfileActivity.this, "Error :" + task.getException(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    mProgress.setVisibility(View.GONE);
                                    Toast.makeText(GroupProfileActivity.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        mProgress.setVisibility(View.GONE);
                        Toast.makeText(GroupProfileActivity.this, "Error: " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!edit_mode) {
            forTestAlertDialog();
            getBasicInfo();
            getImage();
        }
    }

    private void forTestAlertDialog() {
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
                    mCanChangeGroupImage = snapshot.child("canChangeGroupImage").getValue(Boolean.class);
                    if (mCanChangeGroupImage) {
                        mPreset.setVisibility(View.VISIBLE);
                        mSavePreset.setVisibility(View.VISIBLE);
                    }
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

    private void getImage() {
        mProgress.setVisibility(View.VISIBLE);
        mRootRef.child("groups").child(mGroupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String url = snapshot.child("image").getValue().toString();
                if (!TextUtils.isEmpty(url)) {
                    mProgress.setVisibility(View.VISIBLE);
                    Callback callback = new Callback() {
                        @Override
                        public void onSuccess() {
                            mProgress.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Toast.makeText(GroupProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    };
                    RequestCreator requestCreator = Picasso.get().load(url);
                    requestCreator.into(mBackGroupImage);
                    requestCreator.into(mGroupImage, callback);
                } else {
                    mProgress.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GroupProfileActivity.this, "error :" + error.getMessage() + error.getDetails(), Toast.LENGTH_SHORT).show();
                mProgress.setVisibility(View.GONE);
            }
        });
    }

    public void addNewPresetMessage(View view) {
        if (mCanChangeGroupImage && edit_mode) {
            mProgress.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mRootRef.child("groups").child(mGroupId).child("preset_message").child("message").setValue(mPreset.getText().toString()).
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(GroupProfileActivity.this, "error " + task.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                    mProgress.setVisibility(View.GONE);
                                }
                            });
                }
            }).start();
        }
    }
}