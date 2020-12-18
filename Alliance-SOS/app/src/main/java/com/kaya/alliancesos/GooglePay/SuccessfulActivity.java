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
//            String network = intent.getStringExtra("network");
//            String details = intent.getStringExtra("details");
            String info = intent.getStringExtra("info");
            String map = intent.getStringExtra("map");
            String[] tmp = info.replace("\"", "").replace("}", "").replace("{", "").split(":|,");
            textView.setText("Your " + " with Google Pay" + "\n" + tmp[1] + "\n" + tmp[3] +
                    "\n" + info + "\n" + map);
        } else {
            finish();
        }
    }

    public void paymentCompleted(View view) {
        finish();
        return;
    }
}