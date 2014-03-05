package com.quickblox.qmunicate.ui.welcome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBSocialLoginTask;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.ui.registration.RegistrationActivity;
import com.quickblox.qmunicate.ui.utils.FacebookHelper;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

public class WelcomeActivity extends BaseActivity {

    private static final String TAG = WelcomeActivity.class.getSimpleName();

    private View registrationButton;
    private View registrationFacebookButton;
    private View loginButton;

    private FacebookHelper facebookHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellcome);
        useDoubleBackPressed = true;

        registrationButton = _findViewById(R.id.signUpEmailButton);
        registrationFacebookButton = _findViewById(R.id.connectFacebookButton);
        loginButton = _findViewById(R.id.loginButton);

        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());

        initListeners();
        initVersionName();
        saveWellcomeShown();
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.onActivityStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        facebookHelper.onActivityStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        facebookHelper.onSaveInstanceState(outState);
    }

    private void initListeners() {
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegistrationActivity.start(WelcomeActivity.this);
                finish();
            }
        });
        registrationFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookHelper.loginWithFacebook();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.start(WelcomeActivity.this);
                finish();
            }
        });
    }

    private void initVersionName() {
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView versionView = _findViewById(R.id.version);
            versionView.setText("v. " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot obtain version number from Manifest", e);
        }
    }

    private void saveWellcomeShown() {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_WELLCOME_SHOWN, true);
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                new QBSocialLoginTask(WelcomeActivity.this).execute(QBProvider.FACEBOOK, session.getAccessToken(), null);
            }
        }
    }
}