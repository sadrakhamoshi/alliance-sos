package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alliancesos.SendNotificationPack.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class SignUpPage extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private EditText mUsername;
    private Button mSignUp;

//    private String mToken;

    private Token mToken;

    //authentication
    private FirebaseAuth mFirebaseAuth;

    //database
    private DatabaseReference mRootDatabase;
    private DatabaseReference mUserDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        findViewById(R.id.google_sign_up_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Not Working Yet", Toast.LENGTH_SHORT).show();
            }
        });

        InitializeComp();
    }

    private void InitializeComp() {

        //database
        mRootDatabase = FirebaseDatabase.getInstance().getReference();
        mUserDatabaseRef = mRootDatabase.child("users");


        //auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        //views
        mEmail = findViewById(R.id.email_sign_up_page_edt_text);
        mPassword = findViewById(R.id.pass_sign_up_page_edt_text);
        mUsername = findViewById(R.id.username_sign_up_page_edt_text);
        mSignUp = findViewById(R.id.sign_up_btn);
        mSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateAccount();
            }
        });
    }

    void CreateAccount() {
        mFirebaseAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Signed in...", Toast.LENGTH_LONG).show();

                            getTokenAndSignUp();

                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void addTokenToDatabase(String key) {

        mRootDatabase.child("Tokens").child(key).setValue(mToken).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(SignUpPage.this, "Token successfully added ...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUpPage.this, "Erorr in addTokenToDatabase ...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getTokenAndSignUp() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpPage.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();

                mToken = new Token(newToken);

                UserObject userObject = new UserObject(mUsername.getText().toString(), mEmail.getText().toString(), mPassword.getText().toString(), mToken.getToken());

                Toast.makeText(getApplicationContext(), "Token is " + mToken.getToken(), Toast.LENGTH_SHORT).show();

                String key = mFirebaseAuth.getCurrentUser().getUid();

                mUserDatabaseRef.child(key).setValue(userObject);

                addTokenToDatabase(key);
            }
        });
    }
}