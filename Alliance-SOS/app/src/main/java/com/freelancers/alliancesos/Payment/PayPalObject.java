package com.freelancers.alliancesos.Payment;

import com.paypal.android.sdk.payments.PayPalConfiguration;

public class PayPalObject {
    private static final String  CLIENT_ID = "AeQK2pqMHePzuUWGatHyrNeLVzRcTAi3ysTK82Sf0tYC3usBxUjDzqU4s-fV4OCYGkZ02oMyx2iKqze0";
    public static PayPalConfiguration config = new PayPalConfiguration()

            .environment(PayPalConfiguration.ENVIRONMENT_PRODUCTION)
            .clientId(CLIENT_ID);
}
