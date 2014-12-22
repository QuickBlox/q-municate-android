package com.quickblox.q_municate.ui.authorization.signup;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.agreements.UserAgreementActivity;
import com.quickblox.q_municate.ui.authorization.base.BaseAuthActivity;
import com.quickblox.q_municate.ui.authorization.landing.LandingActivity;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.AnalyticsUtils;
import com.quickblox.q_municate.utils.ImageUtils;
import com.quickblox.q_municate.utils.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate.utils.ReceiveUriScaledBitmapTask;
import com.quickblox.q_municate.utils.ValidationUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.QBSignUpCommand;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import java.io.File;

public class SignUpActivity extends BaseAuthActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener, ReceiveUriScaledBitmapTask.ReceiveUriScaledBitmapListener {

    private RoundedImageView avatarImageView;
    private EditText fullnameEditText;
    private TextView userAgreementTextView;
    private ImageUtils imageUtils;
    private boolean isNeedUpdateAvatar;
    private Bitmap avatarBitmapCurrent;
    private QBUser user;
    private Resources resources;
    private Uri outputUri;

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
        imageUtils = new ImageUtils(this);
        validationUtils = new ValidationUtils(SignUpActivity.this,
                new EditText[]{fullnameEditText, emailEditText, passwordEditText},
                new String[]{resources.getString(R.string.dlg_not_fullname_field_entered), resources
                        .getString(R.string.dlg_not_email_field_entered), resources.getString(
                        R.string.dlg_not_password_field_entered)});

        addActions();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        } else if (requestCode == ImageUtils.GALLERY_INTENT_CALLED && resultCode == RESULT_OK) {
            Uri originalUri = data.getData();
            if (originalUri != null) {
                showProgress();
                new ReceiveUriScaledBitmapTask(this).execute(imageUtils, originalUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            avatarBitmapCurrent = imageUtils.getBitmap(outputUri);
            avatarImageView.setImageBitmap(avatarBitmapCurrent);
        } else if (resultCode == Crop.RESULT_ERROR) {
            DialogUtils.showLong(this, Crop.getError(result).getMessage());
        }
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
        outputUri = Uri.fromFile(new File(getCacheDir(), Crop.class.getName()));
        new Crop(originalUri).output(outputUri).asSquare().start(this);
    }

    @Override
    public void onUriScaledBitmapReceived(Uri originalUri) {
        hideProgress();
        startCropActivity(originalUri);
    }

    public void changeAvatarOnClickListener(View view) {
        imageUtils.getImage();
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
                new ReceiveFileFromBitmapTask(this).execute(imageUtils, avatarBitmapCurrent, true);
            } else {
                PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);
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

            AnalyticsUtils.pushAnalyticsData(SignUpActivity.this, user, "User Sign Up");
        }
    }

    private class SignUpFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            int errorCode = bundle.getInt(QBServiceConsts.EXTRA_ERROR_CODE);
            parseExceptionMessage(exception);
        }
    }

    private class SignUpSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            File image = (File) bundle.getSerializable(QBServiceConsts.EXTRA_FILE);
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_SIGN_UP_INITIALIZED, true);
            QBUpdateUserCommand.start(SignUpActivity.this, user, image);
        }
    }
}