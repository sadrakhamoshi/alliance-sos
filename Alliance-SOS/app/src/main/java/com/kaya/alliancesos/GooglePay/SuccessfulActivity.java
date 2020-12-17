package com.kaya.alliancesos.GooglePay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.kaya.alliancesos.R;

import java.util.HashMap;

public class SuccessfulActivity extends AppCompatActivity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_successful);
        textView = findViewById(R.id.green_check_txt);
        String network, details;
        Intent intent = getIntent();
        if (intent != null) {
            network = intent.getStringExtra("network");
            details = intent.getStringExtra("details");
            textView.setText("Your " + network + " **** " + details + " with Google Pay");
        } else {
            finish();
        }
    }

    public void paymentCompleted(View view) {
        finish();
        return;
    }
}