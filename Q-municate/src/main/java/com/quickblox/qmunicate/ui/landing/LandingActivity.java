package com.quickblox.qmunicate.ui.landing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.qb.commands.QBLoginRestWithSocialCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.ui.signup.SignUpActivity;
import com.quickblox.qmunicate.utils.FacebookHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;
import com.quickblox.qmunicate.utils.Utils;

public class LandingActivity extends BaseActivity {

    private static final String TAG = LandingActivity.class.getSimpleName();

    private FacebookHelper facebookHelper;
    private UserAgreementDialog userAgreementDialog;
    private boolean userAgreementShown;

    private DialogInterface.OnClickListener positiveUserAgreementOnClickListener;
    private DialogInterface.OnClickListener negativeUserAgreementOnClickListener;

    public static void start(Context context) {
        Intent intent = new Intent(context, LandingActivity.class);
        context.startActivity(intent);
    }

    public void signUpOnClickListener(View view) {
        if(!userAgreementShown) {
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

    public void connectFacebookOnClickListener(View view) {
        if(!userAgreementShown) {
            positiveUserAgreementOnClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveUserAgreementShowing();
                    facebookHelper.loginWithFacebook();
                }
            };
            showUserAgreement(positiveUserAgreementOnClickListener, negativeUserAgreementOnClickListener);
        } else {
            facebookHelper.loginWithFacebook();
        }
    }

    public void saveUserAgreementShowing() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        prefsHelper.savePref(PrefsHelper.PREF_USER_AGREEMENT, true);
    }

    public boolean isUserAgreementShown() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        return prefsHelper.getPref(PrefsHelper.PREF_USER_AGREEMENT, false);
    }

    public void loginOnClickListener(View view) {
        LoginActivity.start(LandingActivity.this);
        finish();
    }

    private void showUserAgreement(DialogInterface.OnClickListener positiveClickListener,
                                   DialogInterface.OnClickListener negativeClickListener) {
        userAgreementDialog = UserAgreementDialog.newInstance(positiveClickListener, negativeClickListener);
        userAgreementDialog.show(getFragmentManager(), null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        useDoubleBackPressed = true;

        addActions();

        userAgreementShown = isUserAgreementShown();
        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());

        initVersionName();
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        facebookHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    private void startMainActivity(QBUser user) {
        AppSession.getSession().updateUser(user);
        AppSession.saveRememberMe(true);
        MainActivity.start(LandingActivity.this);
        finish();
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                showProgress();
                QBLoginRestWithSocialCommand.start(LandingActivity.this, QBProvider.FACEBOOK,
                        session.getAccessToken(), null);
            }
        }
    }

    private class SocialLoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            startMainActivity(user);
        }
    }
}