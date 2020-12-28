package com.kaya.alliancesos.Payment;

import com.paypal.android.sdk.payments.PayPalConfiguration;

//AeQK2pqMHePzuUWGatHyrNeLVzRcTAi3ysTK82Sf0tYC3usBxUjDzqU4s-fV4OCYGkZ02oMyx2iKqze0 production
//AWwyx4t0OVMd83fjJS_RYOO4lEMsfFjzGVwLWjwGl-5mVj_CRGouIRQiVnBxTC8MKKVYXcoqlYm9-lHP test
public class PayPalObject {
    private static final String CLIENT_ID = "AWwyx4t0OVMd83fjJS_RYOO4lEMsfFjzGVwLWjwGl-5mVj_CRGouIRQiVnBxTC8MKKVYXcoqlYm9-lHP";
    public static PayPalConfiguration config = new PayPalConfiguration()

            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(CLIENT_ID);
}
