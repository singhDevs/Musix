package com.example.musix.handlers;

import android.content.Context;

import com.example.musix.R;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class GoogleSignInHelper {
    private static GoogleSignInOptions gso;

    public static GoogleSignInOptions getSignInOptions(Context context) {
        if (gso == null) {
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.web_client_id))
                    .requestEmail()
                    .build();
        }
        return gso;
    }
}
