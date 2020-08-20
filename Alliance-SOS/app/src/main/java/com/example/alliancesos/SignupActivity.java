package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPass;
    private Button mSignIn;
    private Button mSignUp;


    //authentication
    private FirebaseAuth mFirebaseAuth;

    private static final int RC_SIGN_IN = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Initialized();

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currUser = mFirebaseAuth.getCurrentUser();
        if (currUser != null) {
            Toast.makeText(this, "You already logged in", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You have to sign up first", Toast.LENGTH_SHORT).show();
        }
    }

    private void Initialized() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mEmail = findViewById(R.id.email_edt_text);
        mPass = findViewById(R.id.pass_edt_text);
        mSignIn = findViewById(R.id.login_btn);
        mSignUp = findViewById(R.id.signup_btn);
        SetUpButtons();
    }

    private void SetUpButtons() {
        mSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogInWithEmail();
            }
        });
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAccount();
            }
        });
    }

    void LogInWithEmail(){
        mFirebaseAuth.signInWithEmailAndPassword(mEmail.getText().toString(),mPass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Successfully Logged In", Toast.LENGTH_LONG).show();
                            Intent main=new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(main);
                            finish();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    void CreateAccount() {
        mFirebaseAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            Toast.makeText(getApplicationContext(), user.getEmail(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("ondestroy","OnDestroy");
        if (mFirebaseAuth != null) {
            FirebaseAuth.getInstance().signOut();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFirebaseAuth != null) {
            FirebaseAuth.getInstance().signOut();
        }
        Log.v("onstop","onstop");

    }
}