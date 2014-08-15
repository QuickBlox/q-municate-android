package com.quickblox.q_municate.ui.authorization.signup;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.qb.commands.QBSignUpCommand;
import com.quickblox.q_municate.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.agreements.UserAgreementActivity;
import com.quickblox.q_municate.ui.authorization.base.BaseAuthActivity;
import com.quickblox.q_municate.ui.authorization.landing.LandingActivity;
import com.quickblox.q_municate.ui.cropper.ImageCropperActivity;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.ImageHelper;
import com.quickblox.q_municate.utils.PrefsHelper;
import com.quickblox.q_municate.utils.ReceiveFileListener;
import com.quickblox.q_municate.utils.ReceiveImageFileTask;
import com.quickblox.q_municate.utils.ValidationUtils;

import java.io.File;

public class SignUpActivity extends BaseAuthActivity implements ReceiveFileListener {

    private RoundedImageView avatarImageView;
    private EditText fullnameEditText;
    private TextView userAgreementTextView;
    private ImageHelper imageHelper;
    private boolean isNeedUpdateAvatar;
    private Bitmap avatarBitmapCurrent;
    private QBUser user;
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

        user = new QBUser();
        imageHelper = new ImageHelper(this);
        validationUtils = new ValidationUtils(SignUpActivity.this,
                new EditText[]{fullnameEditText, emailEditText, passwordEditText},
                new String[]{resources.getString(R.string.dlg_not_fullname_field_entered), resources
                        .getString(R.string.dlg_not_email_field_entered), resources.getString(
                        R.string.dlg_not_password_field_entered)});

        addActions();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageCropperActivity.ACTIVITY_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                isNeedUpdateAvatar = true;
                String filePath = data.getStringExtra(QBServiceConsts.EXTRA_FILE_PATH);
                avatarBitmapCurrent = BitmapFactory.decodeFile(filePath);
                imageHelper.removeFile(filePath);
                avatarImageView.setImageBitmap(avatarBitmapCurrent);
            }
        } else if (requestCode == ImageHelper.GALLERY_INTENT_CALLED) {
            if (resultCode == RESULT_OK) {
                Uri originalUri = data.getData();
                if (originalUri != null) {
                    startCropActivity(originalUri);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    private void startCropActivity(Uri originalUri) {
        ImageCropperActivity.start(this, originalUri);
    }

    public void changeAvatarOnClickListener(View view) {
        imageHelper.getImage();
    }

    public void signUpOnClickListener(View view) {
        String fullNameText = fullnameEditText.getText().toString();
        String emailText = emailEditText.getText().toString();
        String passwordText = passwordEditText.getText().toString();

        if (validationUtils.isValidUserDate(fullNameText, emailText, passwordText)) {
            user.setFullName(fullNameText);
            user.setEmail(emailText);
            user.setPassword(passwordText);

            showProgress();

            if (isNeedUpdateAvatar) {
                new ReceiveImageFileTask(this).execute(imageHelper, avatarBitmapCurrent, true);
            } else {
                App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);
                QBSignUpCommand.start(SignUpActivity.this, user, null);
            }
        }
    }

    public void onCachedImageFileReceived(File imageFile) {
        QBSignUpCommand.start(SignUpActivity.this, user, imageFile);
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
        userAgreementTextView = _findViewById(R.id.user_agreement_textview);
    }

    private void initListeners() {
        userAgreementTextView.setOnClickListener(new View.OnClickListener() {

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
            startMainActivity(SignUpActivity.this, user, true);
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