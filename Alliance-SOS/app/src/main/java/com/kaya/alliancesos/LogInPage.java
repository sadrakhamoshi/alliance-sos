package com.kaya.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

public class LogInPage extends AppCompatActivity {

    private ProgressBar progressBar;

    private EditText mEmail;
    private TextInputEditText mPass;
    private Button mLogIn;

    //authentication
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in_page);
        Initialized();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currUser = mFirebaseAuth.getCurrentUser();
        if (currUser != null) {
            SharedPreferences sharedPreferences = getSharedPreferences("User_Ring", Context.MODE_PRIVATE);
            String path = sharedPreferences.getString(currUser.getUid(), null);
            if (path == null) {
                Uri ring = Uri.parse("android.resource://" + getPackageName() + "/raw/game");
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(currUser.getUid(), ring.toString());
                editor.apply();
            }
            Toast.makeText(this, "You already logged in", Toast.LENGTH_SHORT).show();
            GoToMainPage();
        } else {
            Toast.makeText(this, "You have to sign up first", Toast.LENGTH_SHORT).show();
        }
    }

    private void Initialized() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.email_edt_text);
        mPass = findViewById(R.id.pass_edt_text);
        mLogIn = findViewById(R.id.login_btn);
        progressBar = findViewById(R.id.progress_logIn);
        SetUpButtons();
    }

    private void SetUpButtons() {
        mLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogInWithEmail();
            }
        });
    }

    void LogInWithEmail() {
        if (checkLogInCondition()) {
            progressBar.setVisibility(View.VISIBLE);
            mFirebaseAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPass.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Successfully Logged In", Toast.LENGTH_LONG).show();
                                GoToMainPage();
                                return;
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private boolean checkLogInCondition() {

        if (TextUtils.isEmpty(mEmail.getText()) || TextUtils.isEmpty(mPass.getText())) {
            new SignUpPage().MakeAlertDialogForInput(LogInPage.this, "You Have Fill the Blanks...");
            return false;
        }
        return true;
    }

    private void GoToMainPage() {
        progressBar.setVisibility(View.GONE);
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(main);
        finish();
        return;
    }

    public void goToSignUpPage(View view) {
        startActivity(new Intent(getApplicationContext(), SignUpPage.class));
    }

    public void logInViaGoogle(View view) throws JSONException {
        startActivity(new Intent(this, HelpActivity.class));
    }
}