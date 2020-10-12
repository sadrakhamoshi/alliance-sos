package com.example.alliancesos.Payment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.alliancesos.R;
import com.example.alliancesos.Utils.MonthOption;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

class PaymentObject {
    public boolean enable;
    public String month;
    public String chargeDate;

    public PaymentObject(boolean enable, String month) {
        this.enable = enable;
        this.month = month;
        convertToString(new Date());
    }

    public void convertToString(Date date) {
        String pattern = "MM/dd/yyyy hh:mm";
        SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        this.chargeDate = format.format(date);
    }
}

public class PaymentActivity extends AppCompatActivity {

    private String mUserId;
    private DatabaseReference mRoot;

    private String mOtherUsername, mOtherUID;
    private final String MINE = "For My Self";
    private final String OTHER = "For My Friend";

    private RadioGroup mMonthOption, mWhomOption;
    private EditText mEmail;

    private String[] mMonth_Option;
    private String mWhom;
    private int mMonthIdx;

    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        } else {
            mUserId = intent.getStringExtra("userId");
            mRoot = FirebaseDatabase.getInstance().getReference();
        }
        mWhom = MINE;
        mMonthIdx = 1;
        InitUI();

        mWhomOption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.myself_radio) {
                    mEmail.getText().clear();
                    mEmail.setEnabled(false);
                    mWhom = MINE;
                } else {
                    mEmail.setEnabled(true);
                    mWhom = OTHER;
                }
            }
        });

        mMonthOption.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_1month:
                        mMonthIdx = 0;
                        break;
                    case R.id.radio_6month:
                        mMonthIdx = 1;
                        break;
                    case R.id.radio_12month:
                        mMonthIdx = 2;
                        break;
                }
            }
        });
    }

    private void DialogForMonth() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setIcon(R.drawable.time_zone_icon);
        builder.setTitle("Pick Option");
        builder.setSingleChoiceItems(mMonth_Option, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMonthIdx = which;
            }
        });
        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }

    private void InitUI() {
        progressBar = findViewById(R.id.progress_payment);
        mWhomOption = findViewById(R.id.radio_group_whom);
        mMonthOption = findViewById(R.id.radio_group_which);
        mEmail = findViewById(R.id.email_payment);

        mMonth_Option = new String[13];
        for (int i = 0; i < mMonth_Option.length; i++) {
            mMonth_Option[i] = MonthOption.months[i] + " Month , " + MonthOption.prices[i] + " $";
        }
    }

    public void goBackPayment(View view) {
        finish();
        return;
    }

    public void PayOffButton(View view) {

        if (mWhom == MINE) {
            DialogForSubmit();
        } else {
            VerifyEmail();
        }
    }

    private void DialogForSubmit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setIcon(R.drawable.sos_icon);
        builder.setTitle("Attention");
        builder.setCancelable(false);
        String message = "Option : " + MonthOption.months[mMonthIdx] + " Month" + "\n";
        message += ("Price : " + mMonth_Option[mMonthIdx] + "\n");
        message += ("For : " + mWhom + "\n");
        if (!TextUtils.isEmpty(mEmail.getText())) {
            String email = mEmail.getText().toString();
            if (mOtherUsername == null) {
                Toast.makeText(this, "There isn't user with this email ", Toast.LENGTH_SHORT).show();
                return;
            }
            message += ("Email : " + email + "\n");
            message += ("Username : " + mOtherUsername + "\n");
        }
        builder.setMessage(message);
        builder.setPositiveButton("I agree", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PayFunc(mWhom);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    private void VerifyEmail() {
        progressBar.setVisibility(View.VISIBLE);
        mRoot.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                mOtherUsername = null;
                while (iterator.hasNext()) {
                    DataSnapshot curr = (DataSnapshot) iterator.next();
                    String email = curr.child("email").getValue().toString();
                    if (email.equals(mEmail.getText().toString())) {
                        mOtherUsername = curr.child("userName").getValue().toString();
                        mOtherUID = curr.getKey();
                        break;
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (mOtherUsername != null) {
                    DialogForSubmit();
                } else {
                    Toast.makeText(PaymentActivity.this, "There is No One With this Email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PaymentActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void PayFunc(String mWhom) {
        //successful
        if (mWhom.equals(MINE))
            attachToDatabaseMy();
        else
            attachToDatabaseOther();
    }

    private void attachToDatabaseOther() {
        if (TextUtils.isEmpty(mOtherUID)) {
            Toast.makeText(this, "Uid is Null", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            PaymentObject object = new PaymentObject(true, MonthOption.months[mMonthIdx] + "");
            mRoot.child("payment").child(mOtherUID).setValue(object).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful())
                        Toast.makeText(PaymentActivity.this, "Payment Was Successful", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(PaymentActivity.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void attachToDatabaseMy() {
        PaymentObject object = new PaymentObject(true, MonthOption.months[mMonthIdx] + "");
        progressBar.setVisibility(View.VISIBLE);
        mRoot.child("payment").child(mUserId).setValue(object).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(PaymentActivity.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PaymentActivity.this, "Payment Was Successful", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void clickOnOtherOption(View view) {
        DialogForMonth();
    }

    private class VerifyEmailTask extends AsyncTask<Void, Void, Void> {

        DataSnapshot dataSnapshot;

        public VerifyEmailTask(DataSnapshot snapshot) {
            this.dataSnapshot = snapshot;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Iterator iterator = dataSnapshot.getChildren().iterator();

            while (iterator.hasNext()) {
                DataSnapshot curr = (DataSnapshot) iterator.next();
                String email = curr.child("email").getValue().toString();
                if (email.equals(mEmail.getText().toString())) {
                    mOtherUsername = curr.child("userName").getValue().toString();
                    mOtherUID = curr.getKey();
                    return null;
                }
            }
            return null;
        }
    }
}