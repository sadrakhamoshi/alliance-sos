package com.kaya.alliancesos.StripePayment;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;
import com.stripe.android.EphemeralKeyProvider;
import com.stripe.android.EphemeralKeyUpdateListener;


import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class FirebaseEphemeralKeyProvider implements EphemeralKeyProvider {

    private FirebaseFunctions mFunctions;

    @Override
    public void createEphemeralKey(@NotNull String apiVersion, @NotNull EphemeralKeyUpdateListener ephemeralKeyUpdateListener) {
        mFunctions = FirebaseFunctions.getInstance();
        HashMap<String, String> data = new HashMap<>();
        data.put("api_version", apiVersion);
        mFunctions.getHttpsCallable("createEphemeralKey")
                .call(data)
                .continueWith(new Continuation<HttpsCallableResult, Object>() {
                    @Override
                    public Object then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        if (!task.isSuccessful()) {
                            Log.e("striperror", task.getException() + "");
                            return null;
                        }
                        String key = task.getResult().getData().toString();
                        Log.e("striperror", "Successfully " + key);
                        ephemeralKeyUpdateListener.onKeyUpdate(key);
                        return null;
                    }
                });
    }
}
