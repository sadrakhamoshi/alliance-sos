package com.example.alliancesos.GroupSetting;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alliancesos.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupProfileActivity extends AppCompatActivity {

    private ImageView mEdit_image, mExit_image, mBackGroupImage;
    private TextView mGroupName_txt;
    private CheckBox mNoDisturb;
    private EditText mUsername_edt;
    private Button mUpdate_btn;
    private CircleImageView mGroupImage;

    private boolean edit_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);

        Init();
    }

    private void Init() {
        edit_mode = false;
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
        mNoDisturb = findViewById(R.id.disturb_group_setting);
        mUsername_edt = findViewById(R.id.username_group_setting);
        mGroupImage = findViewById(R.id.groupImage_group_setting);
        mUpdate_btn = findViewById(R.id.update_group_setting);
    }


    public void updateGroupProfile(View view) {

    }

    public void pickImageForGroup(View view) {
        if (edit_mode) {

        }
    }
}