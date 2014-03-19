package com.quickblox.qmunicate.ui.profile;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.qb.QBGetFileCommand;
import com.quickblox.qmunicate.qb.QBUpdateUserCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.uihelper.SimpleActionModeCallback;
import com.quickblox.qmunicate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.qmunicate.ui.utils.ImageHelper;

import java.io.File;
import java.io.IOException;

public class ProfileActivity extends BaseActivity {
    private ImageView avatarImageView;
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText statusMessageEditText;

    private ImageHelper imageHelper;
    private Bitmap avatarCurrentBitmap;
    private String fullnameCurrent;
    private String emailCurrent;
    private Bitmap avatarOldBitmap;
    private String fullnameOld;
    private String emailOld;
    private QBUser qbUser;
    private boolean isNeedUpdateAvatar;
    private Object actionMode;

    public static void start(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        useDoubleBackPressed = false;

        initUI();
        qbUser = App.getInstance().getUser();
        imageHelper = new ImageHelper(this);

        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, new UpdateUserSuccessAction());
        addAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION, new FailAction(this));
        addAction(QBServiceConsts.GET_FILE_SUCCESS_ACTION, new GetFileSuccessAction());
        addAction(QBServiceConsts.GET_FILE_FAIL_ACTION, new FailAction(this));
        updateBroadcastActionList();

        initUsersData();
        initTextChangedListeners();
    }

    @Override
    public void onBackPressed() {
        updateUsersData();
        if (isUserDataChanges(fullnameCurrent, emailCurrent)) {
            try {
                saveChanges(avatarCurrentBitmap, fullnameCurrent, emailCurrent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            super.onBackPressed();
        }
    }

    private void initUI() {
        avatarImageView = _findViewById(R.id.avatarImageView);
        fullNameEditText = _findViewById(R.id.fullNameEditText);
        emailEditText = _findViewById(R.id.emailEditText);
        statusMessageEditText = _findViewById(R.id.statusMessageEditText);
    }

    private void initTextChangedListeners() {
        TextWatcher textWatcherListener = new TextWatcherListener();
        fullNameEditText.addTextChangedListener(textWatcherListener);
        emailEditText.addTextChangedListener(textWatcherListener);
    }

    public void changeAvatarOnClick(View view) {
        imageHelper.getImage();
    }

    public void changeFullNameOnClick(View view) {
        initChangingEditText(fullNameEditText);
    }

    public void changeEmailOnClick(View view) {
        initChangingEditText(emailEditText);
    }

    private void initChangingEditText(EditText editText) {
        editText.setEnabled(true);
        editText.requestFocus();
    }

    private void initUsersData() {
        QBGetFileCommand.start(this, qbUser.getFileId());
        fullNameEditText.setText(qbUser.getFullName());
        emailEditText.setText(qbUser.getEmail());

        avatarOldBitmap = ((BitmapDrawable) avatarImageView.getDrawable()).getBitmap();
        fullnameOld = fullNameEditText.getText().toString();
        emailOld = emailEditText.getText().toString();
    }

    private void startAction() {
        if (actionMode != null) {
            return;
        }
        actionMode = startActionMode(new ActionModeCallback());
    }

    private void updateUsersData() {
        avatarCurrentBitmap = ((BitmapDrawable) avatarImageView.getDrawable()).getBitmap();
        fullnameCurrent = fullNameEditText.getText().toString();
        emailCurrent = emailEditText.getText().toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            avatarOldBitmap = ((BitmapDrawable) avatarImageView.getDrawable()).getBitmap();
            Uri originalUri = data.getData();
            avatarImageView.setImageURI(originalUri);
            startAction();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // TODO SF refactor
    private void saveChanges(final Bitmap avatar, final String fullname, final String email) throws IOException {
        if (isUserDataChanges(fullname, email)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final File[] image = {null};
                    qbUser.setFullName(fullname);
                    qbUser.setEmail(email);
                    if (isAvatarChanges(avatar) && isNeedUpdateAvatar) {
                        try {
                            image[0] = imageHelper.getFileFromImageView(avatarImageView);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        showProgress();
                        QBUpdateUserCommand.start(ProfileActivity.this, qbUser, image[0]);
                    }
                }
            }).start();
        }
    }

    private boolean isAvatarChanges(Bitmap avatar) {
        return !imageHelper.equalsBitmaps(avatarOldBitmap, avatar);
    }

    private boolean isUserDataChanges(String fullname, String email) {
        return isNeedUpdateAvatar || !fullname.equals(fullnameOld) || !email.equals(emailOld);
    }

    private class TextWatcherListener extends SimpleTextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            startAction();
        }
    }

    private class ActionModeCallback extends SimpleActionModeCallback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(getResources().getText(R.string.prf_done));
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            updateUsersData();
            if (isUserDataChanges(fullnameCurrent, emailCurrent)) {
                try {
                    saveChanges(avatarCurrentBitmap, fullnameCurrent, emailCurrent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                finish();
            }
            actionMode = null;
        }
    }

    private class UpdateUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            App.getInstance().setUser(user);
            hideProgress();
        }
    }

    private class GetFileSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBFile file = (QBFile) bundle.getSerializable(QBServiceConsts.EXTRA_FILE);
            ImageLoader.getInstance().displayImage(file.getPublicUrl(), avatarImageView);
        }
    }
}