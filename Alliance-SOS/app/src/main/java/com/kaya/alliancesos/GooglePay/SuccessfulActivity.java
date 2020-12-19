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
        Intent intent = getIntent();
        if (intent != null) {
            String info = intent.getStringExtra("info");
            String[] tmp = info.replace("\"", "").replace("}", "").replace("{", "").split(":|,");
            textView.setText("Your " + tmp[1] + " **** " + tmp[3] + " with Google Pay");
        } else {
            finish();
        }
    }

    public void paymentCompleted(View view) {
        finish();
        return;
    }
}