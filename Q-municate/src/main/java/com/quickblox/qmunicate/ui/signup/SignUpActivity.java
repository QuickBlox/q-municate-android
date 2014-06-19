package com.quickblox.qmunicate.ui.signup;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.model.LoginType;
import com.quickblox.qmunicate.qb.commands.QBSignUpCommand;
import com.quickblox.qmunicate.qb.commands.QBUpdateUserCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.landing.LandingActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.ImageHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;
import com.quickblox.qmunicate.utils.ReceiveFileListener;
import com.quickblox.qmunicate.utils.ReceiveImageFileTask;

import java.io.File;
import java.io.FileNotFoundException;

public class SignUpActivity extends BaseActivity implements ReceiveFileListener {

    private static final String TAG = SignUpActivity.class.getSimpleName();
    private EditText passwordEditText;
    private RoundedImageView avatarImageView;
    private EditText fullnameEditText;
    private EditText emailEditText;
    private ImageHelper imageHelper;
    private boolean isNeedUpdateAvatar;
    private Bitmap avatarBitmapCurrent;
    private QBUser qbUser;

    public static void start(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initUI();

        canPerformLogout.set(false);
        useDoubleBackPressed = true;
        qbUser = new QBUser();
        imageHelper = new ImageHelper(this);

        initListeners();
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

        boolean isFullNameEntered = !TextUtils.isEmpty(fullNameText);
        boolean isEmailEntered = !TextUtils.isEmpty(emailText);
        boolean isPasswordEntered = !TextUtils.isEmpty(passwordText);

        if (isFullNameEntered && isEmailEntered && isPasswordEntered) {
            qbUser.setFullName(fullNameText);
            qbUser.setEmail(emailText);
            qbUser.setPassword(passwordText);

            showProgress();

            if (isNeedUpdateAvatar) {
                new ReceiveImageFileTask(this).execute(imageHelper, avatarBitmapCurrent, true);
            } else {
                QBSignUpCommand.start(SignUpActivity.this, qbUser, null);
            }
        } else {
            DialogUtils.showLong(SignUpActivity.this, getString(R.string.dlg_not_all_fields_entered));
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
    }

    private void initListeners() {
        emailEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                super.onTextChanged(charSequence, start, before, count);
                clearErrors();
            }
        });
        fullnameEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                super.onTextChanged(charSequence, start, before, count);
                clearErrors();
            }
        });
    }

    private void clearErrors() {
        fullnameEditText.setError(null);
        emailEditText.setError(null);
    }

    private void setErrors(String errors) {
        emailEditText.setError(errors);
        fullnameEditText.setError(errors);
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
            AppSession.startSession(LoginType.EMAIL, user);
            MainActivity.start(SignUpActivity.this);
            finish();
        }
    }

    private class SignUpFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            setErrors(exception.getMessage());
            hideProgress();
        }
    }

    private class SignUpSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            File image = (File) bundle.getSerializable(QBServiceConsts.EXTRA_FILE);
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_SIGN_UP_INITIALIZED, true);
            QBUpdateUserCommand.start(SignUpActivity.this, user, image, null);
        }
    }
}