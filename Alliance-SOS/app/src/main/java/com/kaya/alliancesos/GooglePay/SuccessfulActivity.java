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
        HashMap<String, Object> text;
        String text_str;
        Intent intent = getIntent();
        if (intent != null) {
//            text = (HashMap<String, Object>) intent.getSerializableExtra("info");
//            StringBuilder builder = new StringBuilder();
//            for (String key : text.keySet()) {
//                builder.append(key + " : " + text.get(key) + " ,");
//            }
//            textView.setText(text + "" + "\n" + "\n" + builder.toString());
            text_str = intent.getStringExtra("info");
            textView.setText(text_str);
        } else {
            finish();
        }
    }

    public void paymentCompleted(View view) {
        finish();
        return;
    }
}