package com.kaya.alliancesos.Payment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.kaya.alliancesos.Adapters.PriceAdapter;
import com.kaya.alliancesos.GooglePay.PaymentsUtil;
import com.kaya.alliancesos.GooglePay.SuccessfulActivity;
import com.kaya.alliancesos.R;
import com.kaya.alliancesos.Utils.MonthOption;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.paypal.android.sdk.payments.ProofOfPayment;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PaymentActivity extends AppCompatActivity {

    private String mUserId;
    private DatabaseReference mRoot;
    private ImageButton paypal_btn;
    private View googlePaymentButton;
    private PaymentsClient paymentsClient;

    private final String MINE = "For My Self";

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 991;
    private static final int PAYMENT_REQ_PAYPAL = 295;

    private static final int PAY_WITH_PAYPAL = 615;
    private static final int PAY_WITH_GPAY = 250;
    private RadioGroup mMonthOption;

    private String[] mMonth_Option;
    private String mWhom;
    public static int mMonthIdx;
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(this, "You Can't Use Google Pay....", Toast.LENGTH_LONG).show();
        } else {
            paymentsClient = PaymentsUtil.createPaymentsClient(this);
            possiblyShowGooglePayButton();
        }
    }

    private void possiblyShowGooglePayButton() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Toast.makeText(this, "You Can't Use Google Pay....", Toast.LENGTH_LONG).show();
        } else {
            final Optional<JSONObject> isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
            if (!isReadyToPayJson.isPresent()) {
                Toast.makeText(this, "not show", Toast.LENGTH_SHORT).show();
                return;
            }
            IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.get().toString());
            Task<Boolean> task = paymentsClient.isReadyToPay(request);
            task.addOnCompleteListener(this,
                    new OnCompleteListener<Boolean>() {
                        @Override
                        public void onComplete(@NonNull Task<Boolean> task) {
                            if (task.isSuccessful()) {
//                                Toast.makeText(getApplicationContext(), " show", Toast.LENGTH_SHORT).show();
                            } else {
                                googlePaymentButton.setVisibility(View.GONE);
                                paypal_btn.setVisibility(View.GONE);
                                Toast.makeText(PaymentActivity.this, task.getException() + "", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void StartPayPalService() {
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalObject.config);
        startService(intent);
    }

    private void PayingWithPayPalFunc() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(MonthOption.prices[mMonthIdx]), "USD", ",   " + MonthOption.months[mMonthIdx] + " Month",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, com.paypal.android.sdk.payments.PaymentActivity.class);

        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalObject.config);
        intent.putExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_PAYMENT, payment);
        startActivityForResult(intent, PAYMENT_REQ_PAYPAL);
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
        final PriceAdapter adapter = new PriceAdapter(PaymentActivity.this);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mMonthIdx = which;
            }
        });

        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }

    private void InitUI() {
        paypal_btn = findViewById(R.id.paypal_button);
        googlePaymentButton = findViewById(R.id.google_button);

        googlePaymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogForSubmit(PAY_WITH_GPAY);
            }
        });

        progressBar = findViewById(R.id.progress_payment);
        mMonthOption = findViewById(R.id.radio_group_which);
        mMonth_Option = new String[13];
        for (int i = 0; i < mMonth_Option.length; i++) {
            mMonth_Option[i] = MonthOption.months[i] + " Month , " + MonthOption.prices[i] + " $";
        }
    }

    public void PayOffButton_PayPal(View view) {
        if (mWhom == MINE) {
            DialogForSubmit(PAY_WITH_PAYPAL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "not Okay " + resultCode, Toast.LENGTH_SHORT).show();
            return;
        }
        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
            Toast.makeText(this, "Make ....", Toast.LENGTH_SHORT).show();
            PaymentData paymentData = PaymentData.getFromIntent(data);
            handlePaymentSuccess(paymentData);

        } else if (resultCode == RESULT_OK && requestCode == PAYMENT_REQ_PAYPAL) {
            PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                ProofOfPayment tmp = confirm.getProofOfPayment();
                PaymentDialog(true, "Successfully done, Thanks for Donation :D \n" +
                        "State : " + tmp.getState() + "\n" + "Time : " + tmp.getCreateTime() + "\n" + "TransactionId : " + tmp.getTransactionId() + "\n");
                //add to database
                if (mWhom == MINE)
                    attachToDatabase(mUserId);
            } else {
                PaymentDialog(false, "Something went wrong ...");
            }
        } else if (requestCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
            Toast.makeText(this, "Invalid Code Try again", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Not Code " + requestCode + " " + resultCode, Toast.LENGTH_SHORT).show();
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        // Token will be null if PaymentDataRequest was not constructed using fromJson(String).
        if (paymentData == null) {
            PaymentDialog(false, "Something went wrong ...");
            return;
        }

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
//            Toast.makeText(this, "getString(R.string.payments_show_name, billingName)", Toast.LENGTH_LONG).show();
//            PaymentDialog(true, "Successfully done, Thanks for Donation :D\n" +
//                    "Your token is " + token + "\n" + retMap);
            PaymentPage(info.toString());

        } catch (JSONException e) {
            PaymentDialog(false, "Something went wrong ...");
            throw new RuntimeException("The selected garment cannot be parsed from the list of elements");
        }
    }

    private void DialogForSubmit(final int code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setIcon(R.drawable.sos_icon);
        builder.setTitle("Attention");
        builder.setCancelable(false);
        String message = "Option : " + MonthOption.months[mMonthIdx] + " Month" + "\n";
        message += ("Price : " + mMonth_Option[mMonthIdx] + "\n");
        message += ("For : " + mWhom + "\n");
        builder.setMessage(message);
        builder.setPositiveButton("Let's PAY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //ExpiredFunc(mWhom, code);
                checkIfExpired(mUserId, code);
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


    private void checkIfExpired(final String uId, final int code) {
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
                        progressBar.setVisibility(View.GONE);
                        if (code == PAY_WITH_PAYPAL)
                            PayingWithPayPalFunc();
                        else if (code == PAY_WITH_GPAY) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                requestPayment();
                                Toast.makeText(PaymentActivity.this, "Pay by GPay", Toast.LENGTH_SHORT).show();
                            }
                        }
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void requestPayment() {
        try {
            String price = MonthOption.prices[mMonthIdx];
            double priceCents = Double.parseDouble(price);
            Optional<JSONObject> paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents);
            if (!paymentDataRequestJson.isPresent()) {
                return;
            }
            PaymentDataRequest request = PaymentDataRequest.fromJson(paymentDataRequestJson.get().toString());
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

    private void attachToDatabase(String uId) {
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

    public void clickOnOtherOption(View view) {
        DialogForMonth();
    }

    public void goBackPayment(View view) {
        finish();
        return;
    }

    private void PaymentDialog(boolean success, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
        builder.setTitle("Result");
        builder.setIcon(R.drawable.check_icon);
        String message = msg;
        if (!success) {
            builder.setIcon(R.drawable.close_icon);
        }
        builder.setMessage(message);
        builder.create().show();
    }

    private void PaymentPage(String info) {
        Intent intent = new Intent(getApplicationContext(), SuccessfulActivity.class);
        intent.putExtra("info", info);
        startActivity(intent);
    }
}