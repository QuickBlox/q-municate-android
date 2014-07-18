package com.quickblox.qmunicate.ui.authorization.landing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.authorization.base.BaseAuthActivity;
import com.quickblox.qmunicate.ui.authorization.login.LoginActivity;
import com.quickblox.qmunicate.ui.authorization.signup.SignUpActivity;
import com.quickblox.qmunicate.utils.Utils;

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
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, failAction);
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
}