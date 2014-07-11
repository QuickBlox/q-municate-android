package com.quickblox.qmunicate.ui.signup;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.qb.commands.QBSignUpCommand;
import com.quickblox.qmunicate.qb.commands.QBUpdateUserCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.agreements.UserAgreementActivity;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.landing.LandingActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.ImageHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;
import com.quickblox.qmunicate.utils.ReceiveFileListener;
import com.quickblox.qmunicate.utils.ReceiveImageFileTask;
import com.quickblox.qmunicate.utils.ValidationUtils;

import java.io.File;
import java.io.FileNotFoundException;

public class SignUpActivity extends BaseActivity implements ReceiveFileListener {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    private EditText passwordEditText;
    private RoundedImageView avatarImageView;
    private EditText fullnameEditText;
    private EditText emailEditText;
    private TextView policyTextView;
    private ImageHelper imageHelper;
    private boolean isNeedUpdateAvatar;
    private Bitmap avatarBitmapCurrent;
    private QBUser qbUser;
    private ValidationUtils validationUtils;
    private Resources resources;

    public static void start(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        resources = getResources();

        initUI();
        initListeners();

        useDoubleBackPressed = true;
        qbUser = new QBUser();
        imageHelper = new ImageHelper(this);

        addActions();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        LandingActivity.start(this);
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            Uri originalUri = data.getData();
            try {
                ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(originalUri, "r");
                avatarBitmapCurrent = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor());
            } catch (FileNotFoundException e) {
                ErrorUtils.showError(this, e);
            }
            ImageLoader.getInstance().displayImage(originalUri.toString(), avatarImageView,
                    Consts.UIL_AVATAR_DISPLAY_OPTIONS);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void changeAvatarOnClickListener(View view) {
        imageHelper.getImage();
    }

    public void signUpOnClickListener(View view) {
        String fullNameText = fullnameEditText.getText().toString();
        String emailText = emailEditText.getText().toString();
        String passwordText = passwordEditText.getText().toString();

        if (validationUtils.isValidUserDate(fullNameText, emailText, passwordText)) {
            qbUser.setFullName(fullNameText);
            qbUser.setEmail(emailText);
            qbUser.setPassword(passwordText);

            showProgress();

            if (isNeedUpdateAvatar) {
                new ReceiveImageFileTask(this).execute(imageHelper, avatarBitmapCurrent, true);
            } else {
                App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);
                QBSignUpCommand.start(SignUpActivity.this, qbUser, null);
            }
        }
    }

    public void onCachedImageFileReceived(File imageFile) {
        QBSignUpCommand.start(SignUpActivity.this, qbUser, imageFile);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {

    }

    private void initUI() {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        fullnameEditText = _findViewById(R.id.fullname_edittext);
        emailEditText = _findViewById(R.id.email_textview);
        passwordEditText = _findViewById(R.id.password_edittext);
        avatarImageView = _findViewById(R.id.avatar_imageview);
        avatarImageView.setOval(true);
        validationUtils = new ValidationUtils(SignUpActivity.this,
                new EditText[]{fullnameEditText, emailEditText, passwordEditText},
                new String[]{resources.getString(R.string.dlg_not_fullname_field_entered), resources
                        .getString(R.string.dlg_not_email_field_entered), resources.getString(
                        R.string.dlg_not_password_field_entered)}
        );
        policyTextView = _findViewById(R.id.policy_textview);
    }

    private void initListeners() {

        policyTextView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UserAgreementActivity.start(SignUpActivity.this);
            }
        });
    }

    private void addActions() {
        addAction(QBServiceConsts.SIGNUP_SUCCESS_ACTION, new SignUpSuccessAction());
        addAction(QBServiceConsts.SIGNUP_FAIL_ACTION, new SignUpFailAction());
        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, new UserUpdateSuccessAction());
        updateBroadcastActionList();
    }

    private class UserUpdateSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            AppSession.getSession().updateUser(user);
            AppSession.saveRememberMe(true);
            MainActivity.start(SignUpActivity.this);
            finish();
        }
    }

    private class SignUpFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            int errorCode = bundle.getInt(QBServiceConsts.EXTRA_ERROR_CODE);
            validationUtils.setError(exception.getMessage());
            hideProgress();
        }
    }

    private class SignUpSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            File image = (File) bundle.getSerializable(QBServiceConsts.EXTRA_FILE);
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_SIGN_UP_INITIALIZED, true);
            QBUpdateUserCommand.start(SignUpActivity.this, user, image);
        }
    }
}