package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsOAuthSigning;
import com.digits.sdk.android.DigitsSession;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.main.MainActivity;
import com.quickblox.q_municate.ui.fragments.dialogs.UserAgreementDialogFragment;
import com.quickblox.q_municate.utils.AuthUtils;
import com.quickblox.q_municate.utils.helpers.FlurryAnalyticsHelper;
import com.quickblox.q_municate.utils.helpers.GoogleAnalyticsHelper;
import com.quickblox.q_municate.utils.helpers.FacebookHelper;
import com.quickblox.q_municate.utils.helpers.TwitterDigitsHelper;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate.utils.helpers.ServiceManager;
import com.quickblox.users.model.QBUser;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.Map;

import butterknife.Bind;
import butterknife.OnTextChanged;
import rx.Observer;

public abstract class BaseAuthActivity extends BaseActivity {

    private static String TAG = BaseAuthActivity.class.getSimpleName();

    protected static final String STARTED_LOGIN_TYPE = "started_login_type";

    @Nullable
    @Bind(R.id.email_textinputlayout)
    protected TextInputLayout emailTextInputLayout;

    @Nullable
    @Bind(R.id.email_edittext)
    protected EditText emailEditText;

    @Nullable
    @Bind(R.id.password_textinputlayout)
    protected TextInputLayout passwordTextInputLayout;

    @Nullable
    @Bind(R.id.password_edittext)
    protected EditText passwordEditText;

    protected FacebookHelper facebookHelper;
    protected TwitterDigitsHelper twitterDigitsHelper;
    protected LoginType loginType = LoginType.EMAIL;
    protected Resources resources;

    private TwitterDigitsAuthCallback twitterDigitsAuthCallback;
    private ServiceManager serviceManager;

    public static void start(Context context) {
        Intent intent = new Intent(context, BaseAuthActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields(savedInstanceState);
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
        outState.putSerializable(STARTED_LOGIN_TYPE, loginType);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    @OnTextChanged(R.id.email_edittext)
    void onTextChangedEmail(CharSequence text) {
        emailTextInputLayout.setError(null);
    }

    @Nullable
    @OnTextChanged(R.id.password_edittext)
    void onTextChangedPassword(CharSequence text) {
        passwordTextInputLayout.setError(null);
    }

    private void initFields(Bundle savedInstanceState) {
        resources = getResources();
        if (savedInstanceState != null && savedInstanceState.containsKey(STARTED_LOGIN_TYPE)) {
            loginType = (LoginType) savedInstanceState.getSerializable(STARTED_LOGIN_TYPE);
        }
        facebookHelper = new FacebookHelper(this);
        twitterDigitsHelper = new TwitterDigitsHelper();
        twitterDigitsAuthCallback = new TwitterDigitsAuthCallback();
        failAction = new FailAction();
        serviceManager = ServiceManager.getInstance();
    }

    protected void startSocialLogin() {
        if (!appSharedHelper.isShownUserAgreement()) {
            UserAgreementDialogFragment
                    .show(getSupportFragmentManager(), new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            appSharedHelper.saveShownUserAgreement(true);
                            loginWithSocial();
                        }
                    });
        } else {
            loginWithSocial();
        }
    }

    private void loginWithSocial() {
        appSharedHelper.saveFirstAuth(true);
        appSharedHelper.saveSavedRememberMe(true);
        if (loginType.equals(LoginType.FACEBOOK)) {
            facebookHelper.login(new FacebookLoginCallback());
        } else if (loginType.equals(LoginType.TWITTER_DIGITS)) {
            twitterDigitsHelper.login(twitterDigitsAuthCallback);
        }
    }

    protected void startMainActivity(QBUser user) {
        AppSession.getSession().updateUser(user);
        startMainActivity();
    }

    protected void startMainActivity() {
        MainActivity.start(BaseAuthActivity.this);
        finish();
    }

    protected void login(String userEmail, final String userPassword) {
        appSharedHelper.saveFirstAuth(true);
        appSharedHelper.saveSavedRememberMe(true);
        appSharedHelper.saveUsersImportInitialized(true);
        QBUser user = new QBUser(null, userPassword, userEmail);

        serviceManager.login(user).subscribe(new Observer<QBUser>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError" + e.getMessage());
                hideProgress();
                AuthUtils.parseExceptionMessage(BaseAuthActivity.this, e.getMessage());
            }

            @Override
            public void onNext(QBUser qbUser) {
                performLoginSuccessAction(qbUser);
            }
        });
    }

    protected void startLandingScreen() {
        LandingActivity.start(this);
        finish();
    }

    private void performLoginSuccessAction(QBUser user) {
        startMainActivity(user);

        // send analytics data
        GoogleAnalyticsHelper.pushAnalyticsData(this, user, "User Sign In");
        FlurryAnalyticsHelper.pushAnalyticsData(this);
    }


    private Observer<QBUser> socialLoginObserver = new Observer<QBUser>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Log.d(TAG, "onError " + e.getMessage());
            hideProgress();
            AuthUtils.parseExceptionMessage(BaseAuthActivity.this, e.getMessage());
        }

        @Override
        public void onNext(QBUser qbUser) {
            performLoginSuccessAction(qbUser);
        }
    };

    private class FacebookLoginCallback implements FacebookCallback<LoginResult> {

        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(TAG, "+++ FacebookCallback call onSuccess from BaseAuthActivity +++");
            showProgress();
            serviceManager.login(QBProvider.FACEBOOK, loginResult.getAccessToken().getToken(), null)
                          .subscribe(socialLoginObserver);
        }

        @Override
        public void onCancel() {
            Log.d(TAG, "+++ FacebookCallback call onCancel from BaseAuthActivity +++");
            hideProgress();
        }

        @Override
        public void onError(FacebookException error) {
            Log.d(TAG, "+++ FacebookCallback call onCancel BaseAuthActivity +++");
            hideProgress();
        }
    }

    private class TwitterDigitsAuthCallback implements AuthCallback {

        @Override
        public void success(DigitsSession session, String phoneNumber) {
            Log.d(TAG, "Success login by number: " + phoneNumber);

            showProgress();

            TwitterAuthConfig authConfig = TwitterCore.getInstance().getAuthConfig();
            TwitterAuthToken authToken = session.getAuthToken();
            DigitsOAuthSigning authSigning = new DigitsOAuthSigning(authConfig, authToken);
            Map<String, String> authHeaders = authSigning.getOAuthEchoHeadersForVerifyCredentials();

            serviceManager.login(QBProvider.TWITTER_DIGITS, authHeaders.get(TwitterDigitsHelper.PROVIDER),authHeaders.get(TwitterDigitsHelper.CREDENTIALS))
                          .subscribe(socialLoginObserver);
        }

        @Override
        public void failure(DigitsException error) {
            Log.d(TAG, "Failure!!!! error: " + error.getLocalizedMessage());
            hideProgress();
        }
    }

}