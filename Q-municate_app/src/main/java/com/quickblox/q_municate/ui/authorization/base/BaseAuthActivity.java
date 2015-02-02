package com.quickblox.q_municate.ui.authorization.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.QBLoginRestWithSocialCommand;
import com.quickblox.q_municate.ui.authorization.landing.UserAgreementDialog;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.main.MainActivity;
import com.quickblox.q_municate.utils.FacebookHelper;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate.utils.ValidationUtils;

public class BaseAuthActivity extends BaseActivity {

    protected static final String STARTED_LOGIN_TYPE = "started_login_type";

    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected FacebookHelper facebookHelper;
    protected LoginType startedLoginType = LoginType.EMAIL;
    protected ValidationUtils validationUtils;
    protected Resources resources;

    protected UserAgreementDialog userAgreementDialog;

    protected DialogInterface.OnClickListener positiveUserAgreementOnClickListener;
    protected DialogInterface.OnClickListener negativeUserAgreementOnClickListener;

    public static void start(Context context) {
        Intent intent = new Intent(context, BaseAuthActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resources = getResources();

        if (savedInstanceState != null && savedInstanceState.containsKey(STARTED_LOGIN_TYPE)) {
            startedLoginType = (LoginType) savedInstanceState.getSerializable(STARTED_LOGIN_TYPE);
        }

        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());
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
        outState.putSerializable(STARTED_LOGIN_TYPE, startedLoginType);
        facebookHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    public void connectFacebookOnClickListener(View view) {
        startedLoginType = LoginType.FACEBOOK;
        if (!isUserAgreementShown()) {
            positiveUserAgreementOnClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveUserAgreementShowing();
                    loginWithFacebook();
                }
            };
            showUserAgreement(positiveUserAgreementOnClickListener, negativeUserAgreementOnClickListener);
        } else {
            loginWithFacebook();
        }
    }

    private void loginWithFacebook() {
        FacebookHelper.logout(); // clearing old data
        facebookHelper.loginWithFacebook();
    }

    protected void showUserAgreement(DialogInterface.OnClickListener positiveClickListener,
                                     DialogInterface.OnClickListener negativeClickListener) {
        if (userAgreementDialog == null) {
            userAgreementDialog = UserAgreementDialog.newInstance(positiveClickListener, negativeClickListener);
        }
        userAgreementDialog.show(getFragmentManager(), null);
    }

    protected void startMainActivity(Context context, QBUser user, boolean saveRememberMe) {
        ChatDatabaseManager.clearAllCache(context);
        AppSession.getSession().updateUser(user);
        AppSession.saveRememberMe(saveRememberMe);
        MainActivity.start(context);
        finish();
    }

    protected void saveUserAgreementShowing() {
        PrefsHelper prefsHelper = PrefsHelper.getPrefsHelper();
        prefsHelper.savePref(PrefsHelper.PREF_USER_AGREEMENT, true);
    }

    protected boolean isUserAgreementShown() {
        PrefsHelper prefsHelper = PrefsHelper.getPrefsHelper();
        return prefsHelper.getPref(PrefsHelper.PREF_USER_AGREEMENT, false);
    }

    protected void parseExceptionMessage(Exception exception) {
        String errorMessage = exception.getMessage();

        hideProgress();

        // TODO: temp decision
        if (exception.getMessage().equals(resources.getString(R.string.error_bad_timestamp))) {
            errorMessage = resources.getString(R.string.error_bad_timestamp_from_app);
        } else if (exception.getMessage().equals(resources.getString(
                R.string.error_email_already_taken)) && startedLoginType.equals(LoginType.FACEBOOK)) {
            errorMessage = resources.getString(R.string.error_email_already_taken_from_app);
            DialogUtils.showLong(BaseAuthActivity.this, errorMessage);
            return;
        }

        validationUtils.setError(errorMessage);
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                showProgress();
                QBLoginRestWithSocialCommand.start(BaseAuthActivity.this, QBProvider.FACEBOOK,
                        session.getAccessToken(), null);
            }
        }
    }
}