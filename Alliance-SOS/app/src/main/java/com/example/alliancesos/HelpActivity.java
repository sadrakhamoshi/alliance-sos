package com.example.alliancesos;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.alliancesos.Payment.PayPalObject;
import com.example.alliancesos.Utils.DonationOption;
import com.example.alliancesos.Utils.MonthOption;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import java.math.BigDecimal;

public class HelpActivity extends AppCompatActivity {
    private static final String DESCT_EMAIL = "help@sosmail.me";
    EditText subject, message;
    private static final int PAYMENT_REQ = 295;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        StartPayPalService();
        InitUi();
    }

    private void StartPayPalService() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalObject.config);
        startService(intent);
    }

    private void PayingWithPayPalFunc(String val) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(val), "USD", " Donation",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalObject.config);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYMENT_REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;

        } else if (requestCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
            Toast.makeText(this, "Invalid Code Try again", Toast.LENGTH_SHORT).show();

        } else if (resultCode == RESULT_OK && requestCode == PAYMENT_REQ) {
            PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                Toast.makeText(this, "Thanks for Your Donation ...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Something is wrong", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
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

    public void Donation(View view) {
        final int[] idx = {0};
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Chose Option");
        builder.setCancelable(true);
        builder.setIcon(R.drawable.money_icon);
        builder.setSingleChoiceItems(DonationOption.options, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                idx[0] = which;
            }
        });
        final EditText message = new EditText(HelpActivity.this);
        message.setHint("other amount ...");
        message.setInputType(InputType.TYPE_CLASS_NUMBER);
        message.setTextDirection(View.TEXT_DIRECTION_LTR);
        builder.setView(message);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Donate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = "";
                if (!TextUtils.isEmpty(message.getText())) {
                    value = message.getText().toString();
                } else {

                    value = DonationOption.options[idx[0]].replace("$ ", "");
                }
                Toast.makeText(HelpActivity.this, "Donation for " + value, Toast.LENGTH_SHORT).show();
                PayingWithPayPalFunc(value);
            }
        });
        builder.create().show();
    }
}