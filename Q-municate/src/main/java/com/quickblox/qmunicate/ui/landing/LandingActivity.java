package com.quickblox.qmunicate.ui.landing;

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
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.qb.QBSocialLoginCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.ui.signup.SignUpActivity;
import com.quickblox.qmunicate.ui.utils.FacebookHelper;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

public class LandingActivity extends BaseActivity {

    private static final String TAG = LandingActivity.class.getSimpleName();

    private FacebookHelper facebookHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, LandingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        useDoubleBackPressed = true;

        addAction(QBServiceConsts.LOGIN_SUCESS_ACTION, new SocialLoginSuccessAction());
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, new FailAction(this));
        updateBroadcastActionList();

        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());

        initVersionName();
        saveLandingShown();
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

    public void signUpOnClickListener(View view) {
        SignUpActivity.start(LandingActivity.this);
        finish();
    }

    public void connectFacebookOnClickListener(View view) {
        facebookHelper.loginWithFacebook();
    }

    public void loginOnClickListener(View view) {
        LoginActivity.start(LandingActivity.this);
        finish();
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

    private void saveLandingShown() {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_LANDING_SHOWN, true);
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                showProgress();
                QBSocialLoginCommand.start(LandingActivity.this, QBProvider.FACEBOOK, session.getAccessToken(), null);
            }
        }
    }

    private class SocialLoginSuccessAction implements Command {
        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            App.getInstance().setUser(user);
            MainActivity.start(LandingActivity.this);
            finish();
        }
    }
}