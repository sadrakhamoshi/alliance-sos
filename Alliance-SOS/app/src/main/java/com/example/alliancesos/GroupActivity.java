package com.example.alliancesos;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class GroupActivity extends AppCompatActivity {

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        String groupName=getIntent().getStringExtra("groupName");
        InitializeUI(groupName);
    }

    private void InitializeUI(String groupName) {
        TextView nameGroup=findViewById(R.id.group_name_txt);
        nameGroup.setText(groupName);


    }
}