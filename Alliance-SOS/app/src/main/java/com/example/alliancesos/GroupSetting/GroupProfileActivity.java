package com.example.alliancesos.GroupSetting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.CropPackage.CropOption;
import com.example.alliancesos.CropPackage.CropOptionAdapter;
import com.example.alliancesos.Member;
import com.example.alliancesos.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupProfileActivity extends AppCompatActivity {

    private static final int PICK_FROM_GALLERY = 1;

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
    private Uri mImageCaptureUri;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE = 3;

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
            pickImageFromGallery();
        }
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
        if (resultCode == RESULT_OK && requestCode == PICK_FROM_GALLERY && data != null) {
            Uri photo = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(GroupProfileActivity.this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!edit_mode) {
            forTestAlertDialog();
            getBasicInfo();
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

    private void doCrop() {
        final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

        int size = list.size();

        if (size == 0) {
            Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();

            return;
        } else {
            intent.setData(mImageCaptureUri);

            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);

            if (size == 1) {
                Intent i = new Intent(intent);
                ResolveInfo res = list.get(0);

                i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                startActivityForResult(i, CROP_FROM_CAMERA);
            } else {
                for (ResolveInfo res : list) {
                    final CropOption co = new CropOption();

                    co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
                    co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
                    co.appIntent = new Intent(intent);

                    co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                    cropOptions.add(co);
                }

                CropOptionAdapter adapter = new CropOptionAdapter(this, cropOptions);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Choose Crop App");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        startActivityForResult(cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
                    }
                });

                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        if (mImageCaptureUri != null) {
                            getContentResolver().delete(mImageCaptureUri, null, null);
                            mImageCaptureUri = null;
                        }
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }

    private void ImageOrCameraDialog() {
        final String[] items = new String[]{"Take from camera", "Select from gallery"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Select Image");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) { //pick from camera
                if (item == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),
                            "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

                    try {
                        intent.putExtra("return-data", true);

                        startActivityForResult(intent, PICK_FROM_CAMERA);
                    } catch (ActivityNotFoundException e) {
                        e.printStackTrace();
                    }
                } else { //pick from file
                    Intent intent = new Intent();

                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(intent, "Complete action using"), PICK_FROM_FILE);
                }
            }
        });

        builder.create().show();
    }
}