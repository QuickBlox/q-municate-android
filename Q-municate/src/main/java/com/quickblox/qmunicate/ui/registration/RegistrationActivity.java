package com.quickblox.qmunicate.ui.registration;

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
import android.widget.Button;
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

public class RegistrationActivity extends BaseActivity {

    private static final String TAG = RegistrationActivity.class.getSimpleName();

    private Button registerButton;
    private View avatarLayout;
    private EditText password;
    private ImageView avatarImageView;
    private EditText fullname;
    private EditText email;

    private String pathToImage;
    private ImageHelper imageHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, RegistrationActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        useDoubleBackPressed = true;

        fullname = _findViewById(R.id.fullnameEdit);
        avatarLayout = _findViewById(R.id.avatarLayout);
        email = _findViewById(R.id.emailEdit);
        password = _findViewById(R.id.password);
        avatarImageView = _findViewById(R.id.avatarImageView);

        registerButton = _findViewById(R.id.signUpButton);

        imageHelper = new ImageHelper(this);

        initListeners();
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
            if (requestCode == imageHelper.GALLERY_KITKAT_INTENT_CALLED) {
                pathToImage = imageHelper.getPath(originalUri, data.getFlags());
            } else if (requestCode == imageHelper.GALLERY_INTENT_CALLED) {
                pathToImage = imageHelper.getPath(originalUri);
            }
            avatarImageView.setImageURI(originalUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initListeners() {
        avatarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageHelper.getImage();
            }
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    private void register() {
        String fullNameText = fullname.getText().toString();
        String emailText = email.getText().toString();
        String passwordText = password.getText().toString();

        boolean isFullNameEntered = !TextUtils.isEmpty(fullNameText);
        boolean isEmailEntered = !TextUtils.isEmpty(emailText);
        boolean isPasswordEntered = !TextUtils.isEmpty(passwordText);

        if (isFullNameEntered && isEmailEntered && isPasswordEntered) {
            final QBUser user = new QBUser();
            user.setFullName(fullname.getText().toString());
            user.setEmail(email.getText().toString());
            user.setPassword(password.getText().toString());

            final File image = new File(pathToImage);

            new QBRegistrationTask(RegistrationActivity.this).execute(user, image);
        } else {
            DialogUtils.show(RegistrationActivity.this, getString(R.string.dlg_not_all_fields_entered));
        }
    }
}
