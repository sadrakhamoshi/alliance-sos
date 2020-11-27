package com.freelancers.alliancesos;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.freelancers.alliancesos.GooglePay.PaymentsUtil;
import com.freelancers.alliancesos.Payment.PayPalObject;
import com.freelancers.alliancesos.Utils.DonationOption;
import com.freelancers.alliancesos.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Optional;

public class HelpActivity extends AppCompatActivity {
    private static final String DESCT_EMAIL = "help@sosmail.me";
    EditText subject, message;
    private static final int PAYMENT_REQ = 295;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private static final int PAY_WITH_PAYPAL = 615;
    private static final int PAY_WITH_GPAY = 250;
    private ImageButton paypal_btn;

    private View googlePaymentButton;
    private PaymentsClient paymentsClient;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        InitUi();
        // PAYPAL
        StartPayPalService();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(this, "You Can't Use Google Pay....", Toast.LENGTH_LONG).show();
        } else {
            paymentsClient = PaymentsUtil.createPaymentsClient(this);
            possiblyShowGooglePayButton();
        }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void possiblyShowGooglePayButton() {
        final Optional<JSONObject> isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
        if (!isReadyToPayJson.isPresent()) {
            Toast.makeText(this, "not show", Toast.LENGTH_SHORT).show();
            return;
        }
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(this,
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), " show", Toast.LENGTH_SHORT).show();
                        } else {
                            googlePaymentButton.setVisibility(View.GONE);
                            paypal_btn.setVisibility(View.GONE);
                            Toast.makeText(HelpActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //paypal
    private void StartPayPalService() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalObject.config);
        startService(intent);
    }

    //paypal
    private void PayingWithPayPalFunc(String val) {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(val), "USD", " Donation",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalObject.config);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYMENT_REQ);
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        if (paymentData == null)
            return;

        final String paymentInfo = paymentData.toJson();
        if (paymentInfo == null) {
            Toast.makeText(this, "Null", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject paymentMethodData = new JSONObject(paymentInfo).getJSONObject("paymentMethodData");
            // If the gateway is set to "example", no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".

            final JSONObject tokenizationData = paymentMethodData.getJSONObject("tokenizationData");
            final String token = tokenizationData.getString("token");
            final JSONObject info = paymentMethodData.getJSONObject("info");
            Toast.makeText(
                    this, "getString(R.string.payments_show_name, billingName)",
                    Toast.LENGTH_LONG).show();

            // Logging token string.
            Log.d("Google Pay token: ", token);

        } catch (JSONException e) {
            throw new RuntimeException("The selected garment cannot be parsed from the list of elements");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_FIRST_USER) {
            Toast.makeText(this, "not Okay " + resultCode, Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            Toast.makeText(this, "Make ....", Toast.LENGTH_SHORT).show();
            PaymentData paymentData = PaymentData.getFromIntent(data);
            handlePaymentSuccess(paymentData);

        } else if (resultCode == RESULT_OK && requestCode == PAYMENT_REQ) {
            PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                Toast.makeText(this, "Thanks for Your Donation ...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Something is wrong", Toast.LENGTH_SHORT).show();
            }

        } else if (requestCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
            Toast.makeText(this, "Invalid Code Try again", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

    private void InitUi() {
        paypal_btn = findViewById(R.id.paypal_btn);
        googlePaymentButton = findViewById(R.id.googlePayButton);
        googlePaymentButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                try {
                    makeDialog(PAY_WITH_GPAY, v);
                } catch (Exception e) {
                    Toast.makeText(HelpActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        subject = findViewById(R.id.subject_help);
        message = findViewById(R.id.message_help);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public void requestPayment(View view, String price) {

        try {
            double priceCents = Double.parseDouble(price);
            Optional<JSONObject> paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents);
            if (!paymentDataRequestJson.isPresent()) {
                return;
            }
            PaymentDataRequest request =
                    PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
            // Since loadPaymentData may show the UI asking the user to select a payment method, we use
            // AutoResolveHelper to wait for the user interacting with it. Once completed,
            // onActivityResult will be called with the result.
            if (request != null) {
                AutoResolveHelper.resolveTask(
                        paymentsClient.loadPaymentData(request),
                        this, LOAD_PAYMENT_DATA_REQUEST_CODE);
            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show();
        }
    }

    public void exitHelp(View view) {
        finish();
        return;
    }

    public void Donation(View view) {
        makeDialog(PAY_WITH_PAYPAL, view);
    }

    private void makeDialog(final int code, final View v) {
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
                if (code == PAY_WITH_PAYPAL) {
                    Toast.makeText(HelpActivity.this, "Donation for " + value, Toast.LENGTH_SHORT).show();
                    PayingWithPayPalFunc(value);
                } else if (code == PAY_WITH_GPAY) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        requestPayment(v, value);
                        Toast.makeText(HelpActivity.this, "Pay by GPay", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.create().show();
    }
}