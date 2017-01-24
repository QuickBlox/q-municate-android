package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
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
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.main.MainActivity;
import com.quickblox.q_municate.ui.fragments.dialogs.UserAgreementDialogFragment;
import com.quickblox.q_municate.utils.helpers.FlurryAnalyticsHelper;
import com.quickblox.q_municate.utils.helpers.GoogleAnalyticsHelper;
import com.quickblox.q_municate.utils.helpers.FacebookHelper;
import com.quickblox.q_municate.utils.helpers.TwitterDigitsHelper;
import com.quickblox.q_municate_auth_service.QMAuthService;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBLoginCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBSocialLoginCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.managers.DataManager;
//import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;

import java.util.Map;

import butterknife.Bind;
import butterknife.OnTextChanged;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

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

    protected LoginSuccessAction loginSuccessAction;
    protected SocialLoginSuccessAction socialLoginSuccessAction;
    protected FailAction failAction;
    private TwitterDigitsAuthCallback twitterDigitsAuthCallback;
    private QMAuthService authService;
    private QMUserService userService;

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
    protected void onResume() {
        super.onResume();
        addActions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeActions();
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
        loginSuccessAction = new LoginSuccessAction();
        socialLoginSuccessAction = new SocialLoginSuccessAction();
        failAction = new FailAction();
        authService = QMAuthService.getInstance();
        userService = QMUserService.getInstance();
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
        if (loginType.equals(LoginType.FACEBOOK)){
            facebookHelper.login(new FacebookLoginCallback());
        } else if (loginType.equals(LoginType.TWITTER_DIGITS)){
            twitterDigitsHelper.login(twitterDigitsAuthCallback);
        }
    }

    protected void startMainActivity(QBUser user) {
        AppSession.getSession().updateUser(user);
        startMainActivity();
    }

    protected void startMainActivity(boolean importInitialized) {
        appSharedHelper.saveUsersImportInitialized(importInitialized);
        startMainActivity();
    }

    protected void startMainActivity() {
        MainActivity.start(BaseAuthActivity.this);
        finish();
    }

    protected void parseExceptionMessage(Exception exception) {
        hideProgress();

        String errorMessage = exception.getMessage();

        if (errorMessage != null) {
            if (errorMessage.equals(getString(R.string.error_bad_timestamp))) {
                errorMessage = getString(R.string.error_bad_timestamp_from_app);
            } else if (errorMessage.equals(getString(R.string.error_login_or_email_required))) {
                errorMessage = getString(R.string.error_login_or_email_required_from_app);
            } else if (errorMessage.equals(getString(R.string.error_email_already_taken))
                    && loginType.equals(LoginType.FACEBOOK)) {
                errorMessage = getString(R.string.error_email_already_taken_from_app);
            } else if (errorMessage.equals(getString(R.string.error_unauthorized))) {
                errorMessage = getString(R.string.error_unauthorized_from_app);
            }

            ErrorUtils.showError(this, errorMessage);
        }
    }

    protected void parseFailException(Bundle bundle) {
        Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
        int errorCode = bundle.getInt(QBServiceConsts.EXTRA_ERROR_CODE);
        parseExceptionMessage(exception);
    }

    protected void login(String userEmail, final String userPassword) {
        appSharedHelper.saveFirstAuth(true);
        appSharedHelper.saveSavedRememberMe(true);
        appSharedHelper.saveUsersImportInitialized(true);
        QBUser user = new QBUser(null, userPassword, userEmail);
        //AppSession.getSession().closeAndClear();


        ServiceManager serviceManager = new ServiceManager(this);
        serviceManager.login(user);


        //QBLoginCompositeCommand.start(this, user);
//        authService.login(user).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<QBUser>() {
//            @Override
//            public void onCompleted() {
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                    Log.d(TAG, "onError" + e.getMessage());
//            }
//
//            @Override
//            public void onNext(QBUser qbUser) {
//
//                //String password = qbUser.getPassword();
//                String password = userPassword;
//
//                if (!hasUserCustomData(qbUser)) {
//                    qbUser.setOldPassword(password);
//                    try {
//                        updateUser(qbUser);
//                    } catch (QBResponseException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                String token = QBSessionManager.getInstance().getToken();
//                qbUser.setPassword(password);
//
//                saveOwnerUser(qbUser);
//
//                AppSession.startSession(LoginType.EMAIL, qbUser, token);
//
//                startMainActivity(qbUser);
//
//                // send analytics data
//                GoogleAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this, qbUser, "User Sign In");
//                FlurryAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this);
//            }
//        });
    }

    protected void performLoginSuccessAction(Bundle bundle) {
//        QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
//        startMainActivity(user);
//
//        // send analytics data
//        GoogleAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this, user, "User Sign In");
//        FlurryAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this);
    }

    protected void performLoginSuccessAction(QBUser user) {
        startMainActivity(user);

        // send analytics data
        GoogleAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this, user, "User Sign In");
        FlurryAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this);
    }


    protected boolean isLoggedInToServer() {
        return AppSession.getSession().isLoggedIn();
    }


    private void addActions() {
        addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION, loginSuccessAction);
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION, socialLoginSuccessAction);
        addAction(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.SIGNUP_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.LOGIN_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOGIN_FAIL_ACTION);

        removeAction(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION);
        removeAction(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION);

        removeAction(QBServiceConsts.SIGNUP_FAIL_ACTION);

        updateBroadcastActionList();
    }

    protected void startLandingScreen() {
        LandingActivity.start(this);
        finish();
    }

    private boolean hasUserCustomData(QBUser user) {
        if (TextUtils.isEmpty(user.getCustomData())) {
            return false;
        }
        UserCustomData userCustomData = Utils.customDataToObject(user.getCustomData());
        return userCustomData != null;
    }

    private QBUser updateUser(QBUser inputUser) throws QBResponseException {
        QBUser user;

        String password = inputUser.getPassword();

        UserCustomData userCustomDataNew = getUserCustomData(inputUser);
        inputUser.setCustomData(Utils.customDataToString(userCustomDataNew));

        inputUser.setPassword(null);
        inputUser.setOldPassword(null);

        user = QBUsers.updateUser(inputUser).perform();

        if (LoginType.EMAIL.equals(AppSession.getSession().getLoginType())) {
            user.setPassword(password);
        } else {
            user.setPassword(QBAuth.getSession().perform().getToken());
        }

        return user;
    }

    private void saveOwnerUser(QBUser qbUser) {
        QMUser user = QMUser.convert(qbUser);
        QMUserService.getInstance().getUserCache().createOrUpdate(user);
    }
    private UserCustomData getUserCustomData(QBUser user) {
        if (TextUtils.isEmpty(user.getCustomData())) {
            return new UserCustomData();
        }

        UserCustomData userCustomData = Utils.customDataToObject(user.getCustomData());

        if (userCustomData != null) {
            return userCustomData;
        } else {
            return new UserCustomData();
        }
    }


    private class LoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            performLoginSuccessAction(bundle);
        }
    }

    private class SocialLoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            QBUpdateUserCommand.start(BaseAuthActivity.this, user, null);

            performLoginSuccessAction(bundle);
        }
    }

    private class FailAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            parseFailException(bundle);
        }
    }

    private class FacebookLoginCallback implements FacebookCallback<LoginResult> {

        @Override
        public void onSuccess(LoginResult loginResult) {
            Log.d(TAG, "+++ FacebookCallback call onSuccess from BaseAuthActivity +++");
                showProgress();

            //QBSocialLoginCommand.start(BaseAuthActivity.this, QBProvider.FACEBOOK, loginResult.getAccessToken().getToken(), null);

            authService.login(QBProvider.FACEBOOK, loginResult.getAccessToken().getToken(),null).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<QBUser>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Log.d(TAG, "onError" + e.getMessage());
                }

                @Override
                public void onNext(QBUser qbUser) {
                    QBUpdateUserCommand.start(BaseAuthActivity.this, qbUser, null);

                    performLoginSuccessAction(qbUser);

                }
            });


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

            //            QBSocialLoginCommand.start(BaseAuthActivity.this, QBProvider.TWITTER_DIGITS,
//                    authHeaders.get(TwitterDigitsHelper.PROVIDER),
//                    authHeaders.get(TwitterDigitsHelper.CREDENTIALS));

            authService.login(QBProvider.TWITTER_DIGITS, authHeaders.get(TwitterDigitsHelper.PROVIDER),authHeaders.get(TwitterDigitsHelper.CREDENTIALS)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<QBUser>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    Log.d(TAG, "onError" + e.getMessage());
                }

                @Override
                public void onNext(QBUser qbUser) {
                    QBUpdateUserCommand.start(BaseAuthActivity.this, qbUser, null);

                    performLoginSuccessAction(qbUser);

                }
            });



        }

        @Override
        public void failure(DigitsException error) {
            Log.d(TAG, "Failure!!!! error: " + error.getLocalizedMessage());
            hideProgress();
        }
    }
}