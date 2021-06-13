package com.kaya.alliancesos.Payment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.ktx.Firebase;
import com.kaya.alliancesos.Adapters.PriceAdapter;
import com.kaya.alliancesos.GooglePay.PaymentsUtil;
import com.kaya.alliancesos.GooglePay.SuccessfulActivity;
import com.kaya.alliancesos.R;
import com.kaya.alliancesos.Setting.ViewDialog;
import com.kaya.alliancesos.StripePayment.FirebaseEphemeralKeyProvider;
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
import com.stripe.android.CustomerSession;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.PaymentSession;
import com.stripe.android.PaymentSessionConfig;
import com.stripe.android.PaymentSessionData;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentMethod;
import com.stripe.android.view.BillingAddressFields;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PaymentActivity extends AppCompatActivity {

    public static HashMap<String, Integer> brandICON;

    static {
        brandICON = new HashMap<>();
        brandICON.put("jbc", R.drawable.jbc);
        brandICON.put("visa", R.drawable.visa);
        brandICON.put("mastercard", R.drawable.mastercard);
        brandICON.put("amex", R.drawable.amex);
        brandICON.put("discover", R.drawable.discover);
    }

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
    private static final int PAY_WITH_STRIPE = 254;
    private RadioGroup mMonthOption;

    private String[] mMonth_Option;
    private String mWhom;
    public static int mMonthIdx;
    private ProgressBar progressBar;
    private String published_key = "pk_live_51IPF0AJZo4v0lsceuBXoC3FdOjfxC50XQxUj5l8SHuqUPRscxJoZrS1tChxm5j9UGaCUCAgKG5gz5On6nFgsdUHz0006OtASXx";


    private PaymentSession mPaymentSession;
    private PaymentMethod selectedPaymentMethod;
    private Stripe mStripe;
    private Button payButton, payment_selection;
    private ViewDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        PaymentConfiguration.init(getApplicationContext(), published_key);
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
        InitializePaymentStuff();
        InitUI();
        SetupPaymentSession();
        payment_selection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPaymentSession.presentPaymentMethodSelection(null);
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
//        startActivityForResult(intent, PAYMENT_REQ_PAYPAL);
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
        loadingDialog = new ViewDialog(this);
        payment_selection = findViewById(R.id.payment_selection);
        payButton = findViewById(R.id.payment_button);
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
            mMonth_Option[i] = MonthOption.months[i] + " Month , " + MonthOption.discount[i] + " $";
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
        mPaymentSession.handlePaymentData(requestCode, resultCode, data);
//        if (resultCode != RESULT_OK) {
//            Toast.makeText(this, "not Okay " + resultCode, Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (requestCode == LOAD_PAYMENT_DATA_REQUEST_CODE) {
//            Toast.makeText(this, "Make ....", Toast.LENGTH_SHORT).show();
//            PaymentData paymentData = PaymentData.getFromIntent(data);
//            handlePaymentSuccess(paymentData);
//
//        } else if (resultCode == RESULT_OK && requestCode == PAYMENT_REQ_PAYPAL) {
//            PaymentConfirmation confirm = data.getParcelableExtra(com.paypal.android.sdk.payments.PaymentActivity.EXTRA_RESULT_CONFIRMATION);
//            if (confirm != null) {
//                ProofOfPayment tmp = confirm.getProofOfPayment();
//                PaymentDialog(true, "Successfully done, Thanks for Donation :D \n" +
//                        "State : " + tmp.getState() + "\n" + "Time : " + tmp.getCreateTime() + "\n" + "TransactionId : " + tmp.getTransactionId() + "\n");
//                //add to database
//                if (mWhom == MINE)
//                    attachToDatabase(mUserId);
//            } else {
//                PaymentDialog(false, "Something went wrong ...");
//            }
//        } else if (requestCode == com.paypal.android.sdk.payments.PaymentActivity.RESULT_EXTRAS_INVALID) {
//            Toast.makeText(this, "Invalid Code Try again", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Not Code " + requestCode + " " + resultCode, Toast.LENGTH_SHORT).show();
//        }
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
//        message += ("For : " + mWhom + "\n");
        assert selectedPaymentMethod.card != null;
        message += ("Card  :  **** " + selectedPaymentMethod.card.last4 + "  " + selectedPaymentMethod.card.brand + "\n");
        message += ("Card Expired  :  " + selectedPaymentMethod.card.expiryMonth + "\\" + selectedPaymentMethod.card.expiryYear + "\n");
        builder.setMessage(message);
        builder.setPositiveButton("Let's PAY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
//        progressBar.setVisibility(View.VISIBLE);
        loadingDialog.showDialog();
        mRoot.child("payment").child(uId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    PaymentObject paymentObject = snapshot.getValue(PaymentObject.class);
                    if (!paymentObject.expired()) {
//                        progressBar.setVisibility(View.GONE);
                        loadingDialog.hideDialog();
                        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this, R.style.AlertDialog);
                        builder.setTitle("Attention!!!");
                        builder.setIcon(R.drawable.close_icon);
                        builder.setMessage("Your Charge Has not Finished Yet. Are You Sure You Want to Recharge Your Account ?");
                        builder.setNegativeButton("Cancel", null);
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                loadingDialog.showDialog();
                                if (code == PAY_WITH_STRIPE) {
                                    confirmPayment(selectedPaymentMethod.id);
                                }
                            }
                        });
                        builder.create().show();
//                        loadingDialog.hideDialog();
//                        Toast.makeText(PaymentActivity.this, "Your Charge Has not Finished Yet!!!", Toast.LENGTH_LONG).show();
                    } else {
//                        progressBar.setVisibility(View.GONE);
                        if (code == PAY_WITH_PAYPAL)
                            PayingWithPayPalFunc();
                        else if (code == PAY_WITH_GPAY) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                requestPayment();
                                Toast.makeText(PaymentActivity.this, "Pay by GPay", Toast.LENGTH_SHORT).show();
                            }
                        } else if (code == PAY_WITH_STRIPE) {
                            confirmPayment(selectedPaymentMethod.id);
                        }
                    }
                } else {
//                    progressBar.setVisibility(View.GONE);
                    loadingDialog.hideDialog();
                    Toast.makeText(PaymentActivity.this, "User Not Valid", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
//                progressBar.setVisibility(View.GONE);
                loadingDialog.hideDialog();
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

    private void attachToDatabase() {
        PaymentObject object = new PaymentObject(true, MonthOption.months[mMonthIdx] + "");
        mRoot.child("payment").child(mUserId).setValue(object).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    Intent toSuccess = new Intent(getApplicationContext(), SuccessfulActivity.class);
                    toSuccess.putExtra("last4", selectedPaymentMethod.card.last4);
                    toSuccess.putExtra("brand", selectedPaymentMethod.card.brand);
                    startActivity(toSuccess);
                } else
                    Toast.makeText(PaymentActivity.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                payButton.setEnabled(true);
                loadingDialog.hideDialog();
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

    private void InitializePaymentStuff() {
        mStripe = new Stripe(getApplicationContext(), published_key);
    }

    public void Handle_Payment(View view) {
        if (selectedPaymentMethod != null) {
            DialogForSubmit(PAY_WITH_STRIPE);
        }
    }

    private void SetupPaymentSession() {
        CustomerSession.initCustomerSession(getApplicationContext(), new FirebaseEphemeralKeyProvider());
        mPaymentSession = new PaymentSession(this, new PaymentSessionConfig.Builder().setShippingInfoRequired(false)
                .setShippingMethodsRequired(false)
                .setBillingAddressFields(BillingAddressFields.None)
                .setShouldShowGooglePay(true)
                .build());
        mPaymentSession.init(new PaymentSession.PaymentSessionListener() {
            @Override
            public void onCommunicatingStateChanged(boolean b) {
                Log.e("striperror", "is communicating  : " + b);
            }

            @Override
            public void onError(int i, @NotNull String s) {
                Log.e("striperror", s);
            }

            @Override
            public void onPaymentSessionDataChanged(@NotNull PaymentSessionData paymentSessionData) {
                Log.e("striperror", "On PaymentSession DataChange");
                if (paymentSessionData.isPaymentReadyToCharge()) {
                    Toast.makeText(PaymentActivity.this, "Enabled ", Toast.LENGTH_SHORT).show();
                    Log.e("striperror", "On PaymentSession DataChange : ready to charge");
                    payButton.setEnabled(true);
                    if (paymentSessionData.getPaymentMethod() != null) {
                        selectedPaymentMethod = paymentSessionData.getPaymentMethod();
                        assert selectedPaymentMethod.card != null;
                        Log.e("striperror", selectedPaymentMethod.card + "\n" + selectedPaymentMethod);
                        String card = "**** " + selectedPaymentMethod.card.last4;
                        payment_selection.setText(card);
                        Drawable img = getApplicationContext().getResources().getDrawable(brandICON.
                                get(selectedPaymentMethod.card.brand.toLowerCase()), null);
                        img.setBounds(0, 0, 150, 150);
                        payment_selection.setCompoundDrawables(img, null, null, null);
                    } else {
                        Log.e("striperror", "On PaymentSession DataChange : Error : paymentMethod is null");
                    }
                } else {
                    payButton.setEnabled(false);
                    Toast.makeText(PaymentActivity.this, "Not Enabled + " + paymentSessionData.getPaymentMethod(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void confirmPayment(String selectedPaymentMethodId) {
        String price = MonthOption.discount[mMonthIdx];
        double priceCents = Double.parseDouble(price);

        payButton.setEnabled(false);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference paymentCollection = db.collection("stripe_customers").document(mUserId).collection("payments");
        HashMap<String, Object> object = new HashMap<>();
        object.put("amount", (int) (priceCents * 100));
        object.put("currency", "usd");
        paymentCollection.add(object).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
//                            progressBar.setVisibility(View.GONE);
                            loadingDialog.hideDialog();
                            payButton.setEnabled(true);
                            Toast.makeText(PaymentActivity.this, "Firebase_Firestoree Error " + error, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (value != null && value.exists()) {
                            if (value.getData().get("client_secret") != null) {
                                String client_secret = Objects.requireNonNull(value.getData().get("client_secret")).toString();
                                mStripe.confirmPayment(PaymentActivity.this, ConfirmPaymentIntentParams.createWithPaymentMethodId(
                                        selectedPaymentMethodId, client_secret
                                ));
                                attachToDatabase();
                            }
//                            payButton.setEnabled(true);
//                            loadingDialog.hideDialog();
                        } else {
//                            progressBar.setVisibility(View.GONE);
                            loadingDialog.hideDialog();
                            payButton.setEnabled(true);
                            Toast.makeText(PaymentActivity.this, "Current payment intent : null", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                payButton.setEnabled(true);
//                progressBar.setVisibility(View.GONE);
                loadingDialog.hideDialog();
                Toast.makeText(PaymentActivity.this, "ON Failure " + e, Toast.LENGTH_LONG).show();
            }
        });
    }

}