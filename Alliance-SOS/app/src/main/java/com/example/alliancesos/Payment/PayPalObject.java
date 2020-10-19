package com.example.alliancesos.Payment;

import com.paypal.android.sdk.payments.PayPalConfiguration;

public class PayPalObject {
    private static final String  CLIENT_ID = "AWwyx4t0OVMd83fjJS_RYOO4lEMsfFjzGVwLWjwGl-5mVj_CRGouIRQiVnBxTC8MKKVYXcoqlYm9-lHP";
    public static PayPalConfiguration config = new PayPalConfiguration()

            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(CLIENT_ID);
}
