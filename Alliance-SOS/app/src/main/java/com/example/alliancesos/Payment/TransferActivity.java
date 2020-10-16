package com.example.alliancesos.Payment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alliancesos.R;
import com.example.alliancesos.Utils.MonthOption;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class TransferActivity extends AppCompatActivity {

    private String mUserId;
    private String mOtherUsername, mOtherUID;
    private TextView mName, mEmail, mAmount;
    private EditText mT_Email, mMonthCount;
    private ProgressBar progressBar;
    private DatabaseReference mRoot;

    private PaymentObject paymentObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        mUserId = getIntent().getStringExtra("userId");
        mRoot = FirebaseDatabase.getInstance().getReference();
        InitUi();
    }

    private void InitUi() {
        progressBar = findViewById(R.id.progress_transfer);
        mName = findViewById(R.id.transfer_name);
        mEmail = findViewById(R.id.transfer_email);
        mAmount = findViewById(R.id.transfer_amount);
        mMonthCount = findViewById(R.id.transfer_month);
        mT_Email = findViewById(R.id.transfer_target_email);
    }

    @Override
    protected void onStart() {
        getInfo();
        super.onStart();
    }

    private void getInfo() {
        progressBar.setVisibility(View.VISIBLE);
        mRoot.child("users").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    onCancelDatabase("notExist");
                } else {
                    String username = snapshot.child("userName").getValue().toString();
                    String email = snapshot.child("email").getValue().toString();
                    mEmail.setText(email);
                    mName.setText(username);
                    mRoot.child("payment").child(mUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                paymentObject = snapshot.getValue(PaymentObject.class);
                                if (!TextUtils.isEmpty(paymentObject.month))
                                    mAmount.setText("Charged in " + paymentObject.chargeDate + "for   " + paymentObject.month + "  Months");
                                else
                                    mAmount.setText("You Don't Have Credit...");
                                progressBar.setVisibility(View.GONE);

                            } else {
                                onCancelDatabase("snapshot not exist");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            onCancelDatabase("Error " + error.getMessage());
                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onCancelDatabase("Error " + error.getMessage());
            }
        });
    }

    private void onCancelDatabase(String message) {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void Transfer(View view) {
        if (TextUtils.isEmpty(mMonthCount.getText())) {
            Toast.makeText(this, "Fill month", Toast.LENGTH_SHORT).show();
        } else {
            if (paymentObject.expired()) {
                Toast.makeText(this, "You Can't Donate Credits because you don't have some", Toast.LENGTH_SHORT).show();
            }
            Integer month = Integer.parseInt(mMonthCount.getText().toString().trim());
            if (paymentObject.donateExpired(month))
                Toast.makeText(this, "The input month is out of your range", Toast.LENGTH_SHORT).show();
            else
                checkForTransfer(month);
        }
    }

    private void checkForTransfer(final Integer month) {
        progressBar.setVisibility(View.VISIBLE);
        mRoot.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Iterator iterator = snapshot.getChildren().iterator();
                mOtherUsername = null;
                while (iterator.hasNext()) {
                    DataSnapshot curr = (DataSnapshot) iterator.next();
                    String email = curr.child("email").getValue().toString();
                    if (email.equals(mT_Email.getText().toString())) {
                        mOtherUsername = curr.child("userName").getValue().toString();
                        mOtherUID = curr.getKey();
                        break;
                    }
                }
                progressBar.setVisibility(View.GONE);
                if (mOtherUsername != null) {
                    DialogForSubmit(month);
                } else {
                    Toast.makeText(TransferActivity.this, "There is No One With this Email", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onCancelDatabase(error.getMessage());
            }
        });
    }

    private void DialogForSubmit(final Integer month) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setIcon(R.drawable.sos_icon);
        builder.setTitle("Attention");
        builder.setCancelable(false);
        String message = "Mont : " + mMonthCount.getText().toString() + "\n";
        String email = mT_Email.getText().toString();
        if (mOtherUsername == null) {
            Toast.makeText(this, "There isn't user with this email ", Toast.LENGTH_SHORT).show();
            return;
        }
        message += ("Email : " + email + "\n");
        message += ("Username : " + mOtherUsername + "\n");
        builder.setMessage(message);
        builder.setPositiveButton("I agree", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Transfering(month);
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

    private void Transfering(Integer month) {
        mRoot.child("payment").child(mOtherUID).setValue(new PaymentObject(true, month + ""));
        mRoot.child("payment").child(mUserId).child("month").setValue((Integer.parseInt(paymentObject.month) - month) + "").addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(TransferActivity.this, "Successfully Transfer", Toast.LENGTH_SHORT).show();
            }
        });

    }
}