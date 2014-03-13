package com.quickblox.qmunicate.ui.signup;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBRegistrationTask;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.ui.utils.DialogUtils;
import com.quickblox.qmunicate.ui.utils.ImageHelper;

import java.io.File;
import java.io.IOException;

public class SignUpActivity extends BaseActivity {

    private static final String TAG = SignUpActivity.class.getSimpleName();

    private EditText password;
    private ImageView avatarImageView;
    private EditText fullname;
    private EditText email;

    private ImageHelper imageHelper;
    private boolean isNeedUpdateAvatar;

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

        imageHelper = new ImageHelper(this);
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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
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

    public void signUpOnClickListener(View view) throws IOException {
        String fullNameText = fullname.getText().toString();
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();

        boolean isFullNameEntered = !TextUtils.isEmpty(fullNameText);
        boolean isEmailEntered = !TextUtils.isEmpty(emailText);
        boolean isPasswordEntered = !TextUtils.isEmpty(passwordText);

        if (isFullNameEntered && isEmailEntered && isPasswordEntered) {
            new Thread(new Runnable() {
                public void run() {
                    final QBUser user = new QBUser();
                    user.setFullName(fullname.getText().toString());
                    user.setEmail(email.getText().toString());
                    user.setPassword(password.getText().toString());
                    File image = null;
                    if (isNeedUpdateAvatar) {
                        try {
                            image = imageHelper.getFileFromImageView(avatarImageView);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    new QBRegistrationTask(SignUpActivity.this).execute(user, image);
                }
            }).start();
        } else {
            DialogUtils.show(SignUpActivity.this, getString(R.string.dlg_not_all_fields_entered));
        }
    }
}