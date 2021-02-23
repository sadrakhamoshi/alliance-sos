package com.kaya.alliancesos;

import android.app.Activity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class googleSignInClient {
    private static String ClientID = "688735100332-a5a9ipcij7nrj6ro104rditckhodedba.apps.googleusercontent.com";

    private Activity mActivity;

    public googleSignInClient(Activity activity) {
        this.mActivity = activity;
    }

    public GoogleSignInClient getmGoogleSignInClient() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ClientID)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this.mActivity, gso);
        return mGoogleSignInClient;
    }
}
