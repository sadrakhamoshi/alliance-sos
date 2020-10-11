package com.example.alliancesos.Payment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.alliancesos.R;

public class PaymentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
    }

    public void goBackPayment(View view) {
        finish();
        return;
    }

    public void moreOptionPayment(View view) {
    }

    public void PayOff(View view) {
    }
}