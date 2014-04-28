package com.quickblox.qmunicate.ui.signup;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.qb.commands.QBSignUpCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.GetImageFileTask;
import com.quickblox.qmunicate.utils.ImageHelper;
import com.quickblox.qmunicate.utils.OnGetImageFileListener;
import com.quickblox.qmunicate.utils.PrefsHelper;

import java.io.File;

public class SignUpActivity extends BaseActivity implements OnGetImageFileListener {

    private static final String TAG = SignUpActivity.class.getSimpleName();
    private EditText password;
    private ImageView avatarImageView;
    private EditText fullname;
    private EditText email;
    private ImageHelper imageHelper;
    private boolean isNeedUpdateAvatar;
    private QBUser qbUser;

    public static void start(Context context) {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        useDoubleBackPressed = true;

        fullname = _findViewById(R.id.fullnameEdit);
        email = _findViewById(R.id.emailEdit);
        password = _findViewById(R.id.password);
        avatarImageView = _findViewById(R.id.avatarImageView);

        qbUser = new QBUser();
        imageHelper = new ImageHelper(this);

        addAction(QBServiceConsts.SIGNUP_SUCCESS_ACTION, new SignUpSuccessAction());
        addAction(QBServiceConsts.SIGNUP_FAIL_ACTION, failAction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.registration_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_login:
                LoginActivity.start(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri originalUri = data.getData();
            isNeedUpdateAvatar = true;
            avatarImageView.setImageURI(originalUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void changeAvatarOnClickListener(View view) {
        imageHelper.getImage();
    }

    public void signUpOnClickListener(View view) {
        String fullNameText = fullname.getText().toString();
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();

        boolean isFullNameEntered = !TextUtils.isEmpty(fullNameText);
        boolean isEmailEntered = !TextUtils.isEmpty(emailText);
        boolean isPasswordEntered = !TextUtils.isEmpty(passwordText);

        if (isFullNameEntered && isEmailEntered && isPasswordEntered) {
            qbUser.setFullName(fullname.getText().toString());
            qbUser.setEmail(email.getText().toString());
            qbUser.setPassword(password.getText().toString());

            showProgress();

            if (isNeedUpdateAvatar) {
                new GetImageFileTask(this).execute(imageHelper, avatarImageView);
            } else {
                QBSignUpCommand.start(SignUpActivity.this, qbUser, null);
            }
        } else {
            DialogUtils.show(SignUpActivity.this, getString(R.string.dlg_not_all_fields_entered));
        }
    }

    public void onGotImageFile(File imageFile) {
        QBSignUpCommand.start(SignUpActivity.this, qbUser, imageFile);
    }

    private class SignUpSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            App.getInstance().setUser(user);
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_SIGN_UP_INITIALIZED, true);
            MainActivity.start(SignUpActivity.this);
            finish();
        }
    }
}