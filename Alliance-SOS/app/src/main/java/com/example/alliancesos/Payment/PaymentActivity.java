package com.example.alliancesos.Payment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.Iterator;

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

        StartPayPalService();
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

    private void StartPayPalService() {
        Intent intent = new Intent(this, PayPalService.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalObject.config);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
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
        PayPalPayment payment = new PayPalPayment(new BigDecimal(MonthOption.prices[mMonthIdx]), "USD", ",   " +MonthOption.months[mMonthIdx]+" Month",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalObject.config);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        else if (requestCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
            Toast.makeText(this, "Invalid Code Try again", Toast.LENGTH_SHORT).show();
        } else {
            PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {

                if (mWhom == MINE) {
                    DialogForSubmit();
                } else {
                    VerifyEmail();
                }
            }
        }
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


    private void PayFunc(String mWhom) {

        //successful
        if (mWhom.equals(MINE)) {
            checkIfExpired(mUserId);
            //attachToDatabaseMy();
        } else {
            checkIfExpired(mOtherUID);
            //attachToDatabaseOther();
        }
    }

    private void checkIfExpired(final String uId) {
        progressBar.setVisibility(View.VISIBLE);
        mRoot.child("payment").child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    PaymentObject paymentObject = snapshot.getValue(PaymentObject.class);
                    if (!paymentObject.expired()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PaymentActivity.this, "Your Charge Has not Finished Yet!!!", Toast.LENGTH_SHORT).show();
                    } else {
                        attachToDatabase(uId);
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(PaymentActivity.this, "User Not Valid", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PaymentActivity.this, "Error on CheckExpired: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attachToDatabase(String uId) {
        if (TextUtils.isEmpty(uId)) {
            Toast.makeText(this, "Uid is Null", Toast.LENGTH_SHORT).show();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            PaymentObject object = new PaymentObject(true, MonthOption.months[mMonthIdx] + "");
            mRoot.child("payment").child(uId).setValue(object).addOnCompleteListener(new OnCompleteListener<Void>() {
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