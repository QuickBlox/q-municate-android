package com.quickblox.q_municate.ui.authorization;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.quickblox.q_municate.R;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        activateButterKnife();

        initVersionName();
    }

    @OnClick(R.id.login_button)
    public void login(View view) {
        LoginActivity.start(LandingActivity.this);
        finish();
    }

    @OnClick(R.id.facebook_connect_button)
    public void facebookConnect(View view) {
        facebookConnect();
    }

    @OnClick(R.id.sign_up_email_button)
    public void signUp(View view) {
        loginType = LoginType.EMAIL;

        if (!appSharedHelper.isShownUserAgreement()) {
            showUserAgreement(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    appSharedHelper.saveShownUserAgreement(true);
                    startSignUpActivity();
                }
            }, null);
        } else {
            startSignUpActivity();
        }
    }

    private void startSignUpActivity() {
        SignUpActivity.start(LandingActivity.this);
        finish();
    }

    private void initVersionName() {
        appVersionTextView.setText(getString(R.string.lnd_version, Utils.getAppVersionName(this)));
    }
}