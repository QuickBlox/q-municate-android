package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.StringObfuscator;
import com.quickblox.q_municate_core.legacy.models.LoginType;

import butterknife.Bind;
import butterknife.OnClick;

public class LandingActivity extends BaseAuthActivity {

    @Bind(R.id.app_version_textview)
    TextView appVersionTextView;

    @Bind(R.id.twitter_digits_connect_button)
    Button twitterDigitsConnectButton;

    public static void start(Context context) {
        Intent intent = new Intent(context, LandingActivity.class);
        context.startActivity(intent);
    }

    public static void start(Context context, Intent intent) {
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_landing;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initVersionName();
    }

    @OnClick(R.id.login_button)
    void login(View view) {
        LoginActivity.start(LandingActivity.this);
        finish();
    }

    @OnClick(R.id.twitter_digits_connect_button)
    void twitterDigitsConnect(View view) {
        if (checkNetworkAvailableWithError()) {
            loginType = LoginType.TWITTER_DIGITS;
            startSocialLogin();
        }
    }

    @Override
    public void checkShowingConnectionError() {
        // nothing. Toolbar is missing.
    }

    private void startSignUpActivity() {
        SignUpActivity.start(LandingActivity.this);
        finish();
    }

    private void initVersionName() {
        appVersionTextView.setText(StringObfuscator.getAppVersionName());
    }
}