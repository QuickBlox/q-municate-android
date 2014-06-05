package com.quickblox.qmunicate.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.LoginType;
import com.quickblox.qmunicate.qb.commands.QBUpdateUserCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.uihelper.SimpleActionModeCallback;
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

public class ProfileActivity extends BaseActivity implements ReceiveFileListener, View.OnClickListener {

    private LinearLayout changeAvatarLinearLayout;
    private RelativeLayout changeFullNameRelativeLayout;
    private RelativeLayout changeEmailRelativeLayout;
    private LinearLayout changeStatusLinearLayout;
    private RoundedImageView avatarImageView;
    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText statusMessageEditText;
    private TextView avatarTextView;

    private ImageHelper imageHelper;

    private Bitmap avatarBitmapCurrent;
    private String fullnameCurrent;
    private String emailCurrent;
    private String statusCurrent;
    private static String fullnameOld;
    private static String emailOld;
    private static String statusOld;
    private static boolean isCurrentMailIncorrect;

    private QBUser user;
    private boolean isNeedUpdateAvatar;
    private Object actionMode;
    private boolean closeActionMode;

    public static void start(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        useDoubleBackPressed = false;
        user = App.getInstance().getUser();
        imageHelper = new ImageHelper(this);

        initUI();
        initListeners();
        initUIWithUsersData();
        initBroadcastActionList();
        initTextChangedListeners();
    }

    private void initUI() {
        changeAvatarLinearLayout = _findViewById(R.id.changeAvatarLinearLayout);
        avatarTextView = _findViewById(R.id.avatar_textview);
        changeFullNameRelativeLayout = _findViewById(R.id.changeFullNameRelativeLayout);
        changeEmailRelativeLayout = _findViewById(R.id.changeEmailRelativeLayout);
        changeStatusLinearLayout = _findViewById(R.id.changeStatusLinearLayout);
        avatarImageView = _findViewById(R.id.avatar_imageview);
        avatarImageView.setOval(true);
        fullNameEditText = _findViewById(R.id.fullNameEditText);
        emailEditText = _findViewById(R.id.emailEditText);
        statusMessageEditText = _findViewById(R.id.statusMessageEditText);
    }

    private void initListeners() {
        avatarTextView.setOnClickListener(this);
        changeAvatarLinearLayout.setOnClickListener(this);
        avatarImageView.setOnClickListener(this);
        changeFullNameRelativeLayout.setOnClickListener(this);
        changeEmailRelativeLayout.setOnClickListener(this);
        changeStatusLinearLayout.setOnClickListener(this);
    }

    private void initUIWithUsersData() {
        tryLoadAvatar();
        fullNameEditText.setText(user.getFullName());
        emailEditText.setText(user.getEmail());
        String status = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_STATUS, "");
        statusMessageEditText.setText(status);
        if (!isCurrentMailIncorrect) {
            updateOldUserData();
        } else {
            recoverUserData();
        }
    }

    private void initBroadcastActionList() {
        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, new UpdateUserSuccessAction());
        addAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION, new UpdateUserFailAction());
    }

    private void tryLoadAvatar() {
        loadAvatar();
    }

    private void loadAvatar() {
        String url = null;
        if (getLoginType() == LoginType.FACEBOOK) {
            changeAvatarLinearLayout.setClickable(false);
            url = getString(R.string.inf_url_to_facebook_avatar, user.getFacebookId());
        } else if (getLoginType() == LoginType.EMAIL) {
            url = user.getWebsite();
        }
        ImageLoader.getInstance().displayImage(url, avatarImageView, Consts.UIL_AVATAR_DISPLAY_OPTIONS);
    }

    private void initTextChangedListeners() {
        TextWatcher textWatcherListener = new TextWatcherListener();
        fullNameEditText.addTextChangedListener(textWatcherListener);
        emailEditText.addTextChangedListener(textWatcherListener);
    }

    private LoginType getLoginType() {
        int defValue = LoginType.EMAIL.ordinal();
        int value = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_LOGIN_TYPE, defValue);
        return LoginType.values()[value];
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar_textview:
                changeAvatarOnClick();
                break;
            case R.id.changeAvatarLinearLayout:
                changeAvatarOnClick();
                break;
            case R.id.avatar_imageview:
                changeAvatarOnClick();
                break;
            case R.id.changeFullNameRelativeLayout:
                changeFullNameOnClick();
                break;
            case R.id.changeEmailRelativeLayout:
                changeEmailOnClick();
                break;
            case R.id.changeStatusLinearLayout:
                changeStatusOnClick();
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            fullNameEditText.setText(user.getFullName());
            emailEditText.setText(user.getEmail());
            closeActionMode = true;
            ((ActionMode) actionMode).finish();
            return true;
        } else {
            closeActionMode = false;
        }
        return super.dispatchKeyEvent(event);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            Uri originalUri = data.getData();
            try {
                ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(originalUri, "r");
                avatarBitmapCurrent = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor());
            } catch (FileNotFoundException e) {
                ErrorUtils.logError(e);
            }
            ImageLoader.getInstance().displayImage(originalUri.toString(), avatarImageView, Consts.UIL_AVATAR_DISPLAY_OPTIONS);
            startAction();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startAction() {
        if (actionMode != null) {
            return;
        }
        actionMode = startActionMode(new ActionModeCallback());
    }

    public void changeAvatarOnClick() {
        imageHelper.getImage();
    }

    public void changeFullNameOnClick() {
        initChangingEditText(fullNameEditText);
    }

    private void initChangingEditText(EditText editText) {
        editText.setEnabled(true);
        editText.requestFocus();
    }

    public void changeEmailOnClick() {
        isCurrentMailIncorrect = false;
        initChangingEditText(emailEditText);
    }

    public void changeStatusOnClick() {
        initChangingEditText(statusMessageEditText);
    }

    @Override
    public void onCachedImageFileReceived(File imageFile) {
        String status = statusMessageEditText.getText().toString();
        QBUpdateUserCommand.start(this, user, imageFile, status);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {

    }

    private void updateCurrentUserData() {
        fullnameCurrent = fullNameEditText.getText().toString();
        emailCurrent = emailEditText.getText().toString();
        statusCurrent = statusMessageEditText.getText().toString();
    }

    private void updateUserData() {
        if (isUserDataChanged(fullnameCurrent, emailCurrent, statusCurrent)) {
            saveChanges(fullnameCurrent, emailCurrent);
        }
    }

    private boolean isUserDataChanged(String fullname, String email, String status) {
        return isNeedUpdateAvatar || !fullname.equals(fullnameOld) || !email.equals(emailOld) || !status
                .equals(statusOld);
    }

    private void saveChanges(final String fullname, final String email) {
        if (!isUserDataCorrect()) {
            DialogUtils.showLong(this, getString(R.string.dlg_not_all_fields_entered));
            return;
        }
        showProgress();
        user.setFullName(fullname);
        user.setEmail(email);

        if (isNeedUpdateAvatar) {
            new ReceiveImageFileTask(this).execute(imageHelper, avatarBitmapCurrent, true);
        } else {
            String status = statusMessageEditText.getText().toString();
            QBUpdateUserCommand.start(this, user, null, status);
        }
    }

    private boolean isUserDataCorrect() {
        return fullnameCurrent.length() > Consts.ZERO_INT_VALUE && emailCurrent
                .length() > Consts.ZERO_INT_VALUE;
    }

    private void updateOldUserData() {
        fullnameOld = fullNameEditText.getText().toString();
        emailOld = emailEditText.getText().toString();
        statusOld = statusMessageEditText.getText().toString();
    }

    private void recoverUserData() {
        fullNameEditText.setText(fullnameOld);
        emailEditText.setText(emailOld);
        statusMessageEditText.setText(statusOld);
    }

    private class TextWatcherListener extends SimpleTextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            startAction();
        }
    }

    public class UpdateUserFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            emailEditText.setTextColor(Color.RED);
            isCurrentMailIncorrect = true;
            hideProgress();
        }
    }

    private class ActionModeCallback extends SimpleActionModeCallback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (!closeActionMode) {
                updateCurrentUserData();
                updateUserData();
            }
            actionMode = null;
        }
    }

    private class UpdateUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            App.getInstance().setUser(user);
            updateOldUserData();
            hideProgress();
        }
    }
}