package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.fragments.dialogs.UserAgreementDialogFragment;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.utils.Utils;

import butterknife.Bind;
import butterknife.OnClick;

public class LandingActivity extends BaseAuthActivity {

    @Bind(R.id.app_version_textview)
    TextView appVersionTextView;

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

    @OnClick(R.id.facebook_connect_button)
    void facebookConnect(View view) {
        if (checkNetworkAvailableWithError()) {
            facebookConnect();
        }
    }

    @OnClick(R.id.sign_up_email_button)
    void signUp(View view) {
        loginType = LoginType.EMAIL;

        if (!appSharedHelper.isShownUserAgreement()) {
            UserAgreementDialogFragment
                    .show(getSupportFragmentManager(), new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            appSharedHelper.saveShownUserAgreement(true);
                            startSignUpActivity();
                        }
                    });
        } else {
            startSignUpActivity();
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
        appVersionTextView.setText(getString(R.string.landing_version, Utils.getAppVersionName(this)));
    }
}