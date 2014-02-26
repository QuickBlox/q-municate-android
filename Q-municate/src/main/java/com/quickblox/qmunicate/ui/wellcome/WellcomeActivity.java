package com.quickblox.qmunicate.ui.wellcome;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.FacebookActivity;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.ui.registration.RegistrationActivity;
import com.quickblox.qmunicate.ui.utils.Consts;

public class WellcomeActivity extends FacebookActivity {

    private static final String TAG = WellcomeActivity.class.getSimpleName();

    private View registrationButton;
    private View registrationFacebookButton;
    private View loginButton;

    public static void startAtivity(Context context) {
        Intent intent = new Intent(context, WellcomeActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellcome);
        useDoubleBackPressed = true;

        registrationButton = findViewById(R.id.signUpEmailButton);
        registrationFacebookButton = findViewById(R.id.connectFacebookButton);
        loginButton = findViewById(R.id.loginButton);

        initListeners();

        initVersionName();

        saveWellcomeShown();
    }

    private void initListeners() {
        registrationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegistrationActivity.startActivity(WellcomeActivity.this);
                finish();
            }
        });
        registrationFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithFacebook();
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginActivity.startActivity(WellcomeActivity.this);
                finish();
            }
        });
    }

    private void initVersionName() {
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView versionView = (TextView) findViewById(R.id.version);
            versionView.setText("v. " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Cannot obtain version number from Manifest", e);
        }
    }

    private void saveWellcomeShown() {
        SharedPreferences prefs = App.getInstance().getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Consts.PREF_WELLCOME_SHOWN, true);
        editor.commit();
    }
}