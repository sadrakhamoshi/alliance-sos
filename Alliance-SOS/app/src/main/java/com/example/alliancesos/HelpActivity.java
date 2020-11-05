package com.example.alliancesos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class HelpActivity extends AppCompatActivity {
    private static final String DESCT_EMAIL = "help@sosmail.me";
    EditText subject, message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        InitUi();
    }

    private void InitUi() {
        subject = findViewById(R.id.subject_help);
        message = findViewById(R.id.message_help);
    }

    public void exitHelp(View view) {
        finish();
        return;
    }

    public void SendEmail(View view) {

        if (TextUtils.isEmpty(message.getText())) {
            Toast.makeText(this, "Please Write Something ...", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent it = new Intent(Intent.ACTION_SEND);
        it.putExtra(Intent.EXTRA_EMAIL, new String[]{DESCT_EMAIL});
        it.putExtra(Intent.EXTRA_SUBJECT, subject.getText().toString());
        it.putExtra(Intent.EXTRA_TEXT, message.getText());
        it.setType("message/rfc822");
        startActivity(Intent.createChooser(it, "Choose Mail App"));
    }
}