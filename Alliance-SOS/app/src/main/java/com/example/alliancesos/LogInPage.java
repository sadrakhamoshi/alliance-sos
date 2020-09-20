package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

public class LogInPage extends AppCompatActivity {

    private EditText mEmail;
    private TextInputEditText mPass;
    private Button mLogIn;
    private String ClientID = "648692468099-fgpjgivrk64lmp71murg2rbs7aik02hu.apps.googleusercontent.com";
    
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
            Toast.makeText(this, "You already logged in", Toast.LENGTH_SHORT).show();
            GoToMainPage();
            return;
        } else {
            Toast.makeText(this, "You have to sign up first", Toast.LENGTH_SHORT).show();
        }
    }

    private void Initialized() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.email_edt_text);
        mPass = findViewById(R.id.pass_edt_text);
        mLogIn = findViewById(R.id.login_btn);
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
            ((ProgressBar) findViewById(R.id.progress_logIn)).setVisibility(View.VISIBLE);
            mFirebaseAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPass.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Successfully Logged In", Toast.LENGTH_LONG).show();
                                ((ProgressBar) findViewById(R.id.progress_logIn)).setVisibility(View.GONE);

                                GoToMainPage();

                                return;
                            } else {
                                ((ProgressBar) findViewById(R.id.progress_logIn)).setVisibility(View.GONE);
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
        Intent main = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(main);
        finish();
    }

    public void goToSignUpPage(View view) {
        startActivity(new Intent(getApplicationContext(), SignUpPage.class));
    }

    public void logInViaGoogle(View view) {
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.EN)
                        .setTargetLanguage(FirebaseTranslateLanguage.ID)
                        .build();

        final FirebaseTranslator englishGermanTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        englishGermanTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                englishGermanTranslator.translate("how Are You .i Fine Thanks").addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        Toast.makeText(LogInPage.this, s, Toast.LENGTH_LONG).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(LogInPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Toast.makeText(LogInPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

    }
}