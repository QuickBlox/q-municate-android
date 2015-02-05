package com.quickblox.q_municate.ui.authorization.landing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.authorization.base.BaseAuthActivity;
import com.quickblox.q_municate.ui.authorization.login.LoginActivity;
import com.quickblox.q_municate.ui.authorization.signup.SignUpActivity;
import com.quickblox.q_municate_core.utils.Utils;

public class LandingActivity extends BaseAuthActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, LandingActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        useDoubleBackPressed = true;

        initVersionName();
        addActions();
    }

    public void signUpOnClickListener(View view) {
        if (!isUserAgreementShown()) {
            positiveUserAgreementOnClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveUserAgreementShowing();
                    startSignUpActivity();
                }
            };
            showUserAgreement(positiveUserAgreementOnClickListener, negativeUserAgreementOnClickListener);
        } else {
            startSignUpActivity();
        }
    }

    private void startSignUpActivity() {
        SignUpActivity.start(LandingActivity.this);
        finish();
    }

    public void loginOnClickListener(View view) {
        LoginActivity.start(LandingActivity.this);
        finish();
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION, new SocialLoginSuccessAction());
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, new SocialLoginFailAction());
        updateBroadcastActionList();
    }

    private void initVersionName() {
        TextView versionView = _findViewById(R.id.version);
        versionView.setText(getString(R.string.lnd_version, Utils.getAppVersionName(this)));
    }

    private class SocialLoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            startMainActivity(LandingActivity.this, user, true);
        }
    }

    private class SocialLoginFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            int errorCode = bundle.getInt(QBServiceConsts.EXTRA_ERROR_CODE);
            parseExceptionMessage(exception);
        }
    }
}