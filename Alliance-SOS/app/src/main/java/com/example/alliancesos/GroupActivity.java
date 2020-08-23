package com.example.alliancesos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GroupActivity extends AppCompatActivity {

    private Button mMembersList;
    private Button mGroupList;

    private TextView mSchedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        //String groupName = getIntent().getStringExtra("groupName");
        String groupId = getIntent().getStringExtra("groupId");
        InitializeUI(groupId);
    }

    private void InitializeUI(final String groupId) {
        TextView nameGroup = findViewById(R.id.group_name_txt);
        nameGroup.setText(groupId);

        mSchedule = findViewById(R.id.schedule_event);
        mSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSetScheduleEvent();
            }
        });


        Button b1 = findViewById(R.id.help_us_btn);
        Button b2 = findViewById(R.id.user_setting_btn);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GroupActivity.this, "Will go to Help Us Activity ...", Toast.LENGTH_SHORT).show();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(GroupActivity.this, "Will go to Users Setting Activity ...", Toast.LENGTH_SHORT).show();
            }
        });


        mMembersList = findViewById(R.id.member_list_btn);
        mMembersList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toMember = new Intent(getApplicationContext(), MemberActivity.class);
                toMember.putExtra("groupId", groupId);
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

    private void goToSetScheduleEvent() {

    }
}