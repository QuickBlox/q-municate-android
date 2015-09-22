package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.agreements.UserAgreementActivity;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.AnalyticsUtils;
import com.quickblox.q_municate.utils.ImageUtils;
import com.quickblox.q_municate.utils.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate.utils.ReceiveUriScaledBitmapTask;
import com.quickblox.q_municate.utils.ValidationUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.QBSignUpCommand;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;

public class SignUpActivity extends BaseAuthActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener,
        ReceiveUriScaledBitmapTask.ReceiveUriScaledBitmapListener {

    @Bind(R.id.email_edittext)
    EditText emailEditText;

    @Bind(R.id.password_edittext)
    EditText passwordEditText;

    @Bind(R.id.fullname_edittext)
    EditText fullNameEditText;

    @Bind(R.id.avatar_imageview)
    RoundedImageView avatarImageView;

    private ImageUtils imageUtils;
    private boolean isNeedUpdateAvatar;
    private Bitmap avatarBitmapCurrent;
    private QBUser qbUser;
    private Uri outputUri;

    private SignUpSuccessAction signUpSuccessAction;
    private UpdateUserSuccessAction updateUserSuccessAction;

    public static void start(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        activateButterKnife();

        initActionBar();
        initFields(savedInstanceState);
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initFields(Bundle bundle) {
        qbUser = new QBUser();
        imageUtils = new ImageUtils(this);
        validationUtils = new ValidationUtils(SignUpActivity.this,
                new EditText[]{ fullNameEditText, emailEditText, passwordEditText },
                new String[]{ resources.getString(R.string.dlg_not_fullname_field_entered), resources
                        .getString(R.string.dlg_not_email_field_entered), resources.getString(
                        R.string.dlg_not_password_field_entered) });
        signUpSuccessAction = new SignUpSuccessAction();
        updateUserSuccessAction = new UpdateUserSuccessAction();
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

    private void addActions() {
        addAction(QBServiceConsts.SIGNUP_SUCCESS_ACTION, signUpSuccessAction);
        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, updateUserSuccessAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.SIGNUP_SUCCESS_ACTION);
        removeAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION);

        updateBroadcastActionList();
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

    @OnClick(R.id.change_avatar_linearlayout)
    public void selectAvatar(View view) {
        imageUtils.getImage();
    }

    @OnClick(R.id.sign_up_email_button)
    public void signUp(View view) {
        loginType = LoginType.EMAIL;

        String fullNameText = fullNameEditText.getText().toString();
        String emailText = emailEditText.getText().toString();
        String passwordText = passwordEditText.getText().toString();

        if (validationUtils.isValidUserDate(fullNameText, emailText, passwordText)) {
            qbUser.setFullName(fullNameText);
            qbUser.setEmail(emailText);
            qbUser.setPassword(passwordText);

            showProgress();

            if (isNeedUpdateAvatar) {
                new ReceiveFileFromBitmapTask(this).execute(imageUtils, avatarBitmapCurrent, true);
            } else {
                appSharedHelper.saveUsersImportInitialized(false);
                startSignUp(null);
            }
        }
    }

    @OnClick(R.id.user_agreement_textview)
    public void openUserAgreement(View view) {
        UserAgreementActivity.start(SignUpActivity.this);
    }

    public void onCachedImageFileReceived(File imageFile) {
        startSignUp(imageFile);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    private void startSignUp(File imageFile) {
        DataManager.getInstance().clearAllTables();
        AppSession.getSession().closeAndClear();
        QBSignUpCommand.start(SignUpActivity.this, qbUser, imageFile);
    }

    protected void performUpdateUserSuccessAction(Bundle bundle) {
        QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
        appSharedHelper.saveFirstAuth(true);
        appSharedHelper.saveSavedRememberMe(true);
        startMainActivity(user);

        // send analytics data
        AnalyticsUtils.pushAnalyticsData(SignUpActivity.this, user, "User Sign Up");
    }

    private void performSignUpSuccessAction(Bundle bundle) {
        File image = (File) bundle.getSerializable(QBServiceConsts.EXTRA_FILE);
        QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
        QBUpdateUserCommand.start(SignUpActivity.this, user, image);
    }

    private class SignUpSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            performSignUpSuccessAction(bundle);
        }
    }

    private class UpdateUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            performUpdateUserSuccessAction(bundle);
        }
    }
}