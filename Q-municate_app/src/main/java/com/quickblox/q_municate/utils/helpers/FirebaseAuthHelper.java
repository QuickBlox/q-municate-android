package com.quickblox.q_municate.utils.helpers;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.qb.helpers.BaseThreadPoolHelper;

import java.util.Collections;

public class FirebaseAuthHelper extends BaseThreadPoolHelper {

    private static final String TAG = FirebaseAuthHelper.class.getSimpleName();

    public static final int RC_SIGN_IN = 456;
    public static final String EXTRA_FIREBASE_ACCESS_TOKEN = "extra_firebase_access_token";

    public FirebaseAuthHelper(Context context) {
        super(context);
    }

    public void loginByPhone(Activity activity) {
        activity.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build()))
                        .setTheme(R.style.FirebaseStyle)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    public static FirebaseUser getCurrentFirebaseUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public void refreshInternalFirebaseToken(final RequestFirebaseIdTokenCallback callback) {
        if (getCurrentFirebaseUser() == null) {
            Log.v(TAG, "Getting Token error. ERROR = Current Firebse User is null");
            SharedHelper.getInstance().saveFirebaseToken(null);
            callback.onError(new IllegalStateException("Current Firebase User is null"));
            return;
        }

        threadPoolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                getCurrentFirebaseUser().getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String accessToken = task.getResult().getToken();
                            Log.v(TAG, "Token got successfully. TOKEN = " + accessToken);
                            SharedHelper.getInstance().saveFirebaseToken(accessToken);
                            callback.onSuccess(accessToken);
                        } else {
                            Log.v(TAG, "Getting Token error. ERROR = " + task.getException().getMessage());
                            callback.onError(task.getException());
                        }
                    }
                });
            }
        });
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        SharedHelper.getInstance().saveFirebaseToken(null);
    }

    public interface RequestFirebaseIdTokenCallback {

        void onSuccess(String accessToken);

        void onError(Exception e);
    }
}
