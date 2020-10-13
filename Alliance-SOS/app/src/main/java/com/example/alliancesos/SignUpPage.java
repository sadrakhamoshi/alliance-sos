package com.example.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.alliancesos.DbForRingtone.AppDatabase;
import com.example.alliancesos.DbForRingtone.ChoiceApplication;
import com.example.alliancesos.DbForRingtone.ringtone;
import com.example.alliancesos.Payment.PaymentObject;
import com.example.alliancesos.SendNotificationPack.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.TimeZone;

public class SignUpPage extends AppCompatActivity {

    private EditText mEmail;
    private TextInputEditText mPassword, mConfirmPassword;
    private EditText mUsername;
    private Button mSignUp;

    private Token mToken;
    private String userId;

    //authentication
    private FirebaseAuth mFirebaseAuth;

    //database
    private DatabaseReference mRootDatabase;
    private DatabaseReference mUserDatabaseRef;

    private ChoiceApplication mChoiceDB;
    private Uri ring;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mChoiceDB = new ChoiceApplication(SignUpPage.this);
            }
        }).start();

        findViewById(R.id.google_sign_up_btn).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                String[] tzIds = TimeZone.getAvailableIDs();
                Toast.makeText(SignUpPage.this, tzIds.length + "", Toast.LENGTH_SHORT).show();
            }
        });

        InitializeComp();
    }

    @Override
    protected void onPause() {
        progressBar.setVisibility(View.GONE);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        progressBar.setVisibility(View.GONE);
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
        mConfirmPassword = findViewById(R.id.confirm_pass_sign_up_page_edt_text);
        progressBar = ((ProgressBar) findViewById(R.id.progress_signUp_page));

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
        if (checkSignUpCondition()) {
            progressBar.setVisibility(View.VISIBLE);
            mFirebaseAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Signed in...", Toast.LENGTH_LONG).show();
                                pushDataInDatabase pushDataInDatabase = new pushDataInDatabase();
                                pushDataInDatabase.execute();

                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private boolean checkSignUpCondition() {
        if (TextUtils.isEmpty(mEmail.getText()) || TextUtils.isEmpty(mPassword.getText()) || TextUtils.isEmpty(mUsername.getText()) || TextUtils.isEmpty(mConfirmPassword.getText())) {
            MakeAlertDialogForInput(SignUpPage.this, "You Have to fill All Blanks...");
            return false;
        }
        if (!mConfirmPassword.getText().toString().equals(mPassword.getText().toString())) {
            MakeAlertDialogForInput(SignUpPage.this, "Password is Not Correct...");
            return false;
        }
        return true;
    }

    public void MakeAlertDialogForInput(Context mContext, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.AlertDialog);
        builder.setTitle("Alert");
        builder.setIcon(R.drawable.sos_icon);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setNegativeButton("okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void getTokenAndSignUp() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(SignUpPage.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                mToken = new Token(newToken);
                String key = mFirebaseAuth.getCurrentUser().getUid();
                userId = key;
                UserObject userObject = new UserObject(key, mUsername.getText().toString(), mEmail.getText().toString(), mPassword.getText().toString(), mToken.getToken());
                mUserDatabaseRef.child(key).setValue(userObject);
                mRootDatabase.child("payment").child(key).setValue(new PaymentObject(false, ""));
            }
        });
    }

    public class pushDataInDatabase extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);
            startActivity(new Intent(SignUpPage.this, MainActivity.class));
            finish();
            return;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            getTokenAndSignUp();
            ring = Uri.parse("android.resource://" + getPackageName() + "/raw/game");
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            ringtone ringtone = new ringtone(userId, ring.toString());
            mChoiceDB.appDatabase.dao().insert(ringtone);
            return null;
        }
    }

}