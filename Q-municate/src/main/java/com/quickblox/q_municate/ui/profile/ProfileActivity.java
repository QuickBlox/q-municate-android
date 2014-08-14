package com.quickblox.q_municate.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
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
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.uihelper.SimpleActionModeCallback;
import com.quickblox.q_municate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DialogUtils;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.ImageHelper;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate.utils.ReceiveFileListener;
import com.quickblox.q_municate.utils.ReceiveImageFileTask;

import java.io.File;
import java.io.FileNotFoundException;

public class ProfileActivity extends BaseLogeableActivity implements ReceiveFileListener, View.OnClickListener {

    private static String fullnameOld;
    private static String phoneOld;
    private static String statusOld;
    private LinearLayout changeAvatarLinearLayout;
    private RoundedImageView avatarImageView;
    private LinearLayout emailLinearLayout;
    private RelativeLayout changeFullNameRelativeLayout;
    private RelativeLayout changePhoneRelativeLayout;
    private RelativeLayout changeStatusRelativeLayout;
    private TextView emailTextView;
    private EditText fullNameEditText;
    private EditText phoneEditText;
    private EditText statusEditText;
    private ImageHelper imageHelper;
    private Bitmap avatarBitmapCurrent;
    private String fullnameCurrent;
    private String phoneCurrent;
    private String statusCurrent;
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
        user = AppSession.getSession().getUser();
        imageHelper = new ImageHelper(this);

        initUI();
        initListeners();
        initUIWithUsersData();
        initBroadcastActionList();
        initTextChangedListeners();
        updateOldUserData();
    }

    private void initUI() {
        changeAvatarLinearLayout = _findViewById(R.id.change_avatar_linearlayout);
        avatarImageView = _findViewById(R.id.avatar_imageview);
        avatarImageView.setOval(true);
        emailLinearLayout = _findViewById(R.id.email_linearlayout);
        changeFullNameRelativeLayout = _findViewById(R.id.change_fullname_relativelayout);
        changePhoneRelativeLayout = _findViewById(R.id.change_phone_relativelayout);
        changeStatusRelativeLayout = _findViewById(R.id.change_status_relativelayout);
        emailTextView = _findViewById(R.id.email_textview);
        fullNameEditText = _findViewById(R.id.fullname_edittext);
        phoneEditText = _findViewById(R.id.phone_edittext);
        statusEditText = _findViewById(R.id.status_edittext);
    }

    private void initListeners() {
        changeAvatarLinearLayout.setOnClickListener(this);
        avatarImageView.setOnClickListener(this);
        changeFullNameRelativeLayout.setOnClickListener(this);
        changePhoneRelativeLayout.setOnClickListener(this);
        changeStatusRelativeLayout.setOnClickListener(this);
    }

    private void initUIWithUsersData() {
        loadAvatar();
        fullnameOld = user.getFullName();
        fullNameEditText.setText(fullnameOld);
        if (TextUtils.isEmpty(user.getEmail())) {
            emailLinearLayout.setVisibility(View.GONE);
        } else {
            emailLinearLayout.setVisibility(View.VISIBLE);
            emailTextView.setText(user.getEmail());
        }
        phoneOld = user.getPhone();
        phoneEditText.setText(phoneOld);
        statusOld = user.getCustomData();
        statusEditText.setText(statusOld);
    }

    private void initBroadcastActionList() {
        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, new UpdateUserSuccessAction());
        addAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION, new UpdateUserFailAction());
    }

    private void loadAvatar() {
        String url = user.getWebsite();
        ImageLoader.getInstance().displayImage(url, avatarImageView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    private void initTextChangedListeners() {
        TextWatcher textWatcherListener = new TextWatcherListener();
        fullNameEditText.addTextChangedListener(textWatcherListener);
        phoneEditText.addTextChangedListener(textWatcherListener);
        statusEditText.addTextChangedListener(textWatcherListener);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.avatar_imageview:
            case R.id.change_avatar_linearlayout:
                changeAvatarOnClick();
                break;
            case R.id.change_fullname_relativelayout:
                changeFullNameOnClick();
                break;
            case R.id.change_phone_relativelayout:
                changePhoneOnClick();
                break;
            case R.id.change_status_relativelayout:
                changeStatusOnClick();
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            resetUserData();
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
                finish();
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
            ImageLoader.getInstance().displayImage(originalUri.toString(), avatarImageView,
                    Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
            startAction();
        }
        canPerformLogout.set(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startAction() {
        if (actionMode != null) {
            return;
        }
        actionMode = startActionMode(new ActionModeCallback());
    }

    public void changeAvatarOnClick() {
        canPerformLogout.set(false);
        imageHelper.getImage();
    }

    public void changeFullNameOnClick() {
        initChangingEditText(fullNameEditText);
    }

    private void initChangingEditText(EditText editText) {
        editText.setEnabled(true);
        editText.requestFocus();
        KeyboardUtils.showKeyboard(this);
    }

    private void stopChangingEditText(EditText editText) {
        editText.setEnabled(false);
        KeyboardUtils.hideKeyboard(this);
    }

    public void changePhoneOnClick() {
        initChangingEditText(phoneEditText);
    }

    public void changeStatusOnClick() {
        initChangingEditText(statusEditText);
    }

    @Override
    public void onCachedImageFileReceived(File imageFile) {
        QBUpdateUserCommand.start(this, user, imageFile);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    private void updateCurrentUserData() {
        fullnameCurrent = fullNameEditText.getText().toString();
        phoneCurrent = phoneEditText.getText().toString();
        statusCurrent = statusEditText.getText().toString();
    }

    private void updateUserData() {
        updateCurrentUserData();
        if (isUserDataChanged()) {
            saveChanges();
        }
    }

    private boolean isUserDataChanged() {
        return isNeedUpdateAvatar || !fullnameCurrent.equals(fullnameOld) || !phoneCurrent.equals(
                phoneOld) || !statusCurrent.equals(statusOld);
    }

    private void saveChanges() {
        if (!isUserDataCorrect()) {
            DialogUtils.showLong(this, getString(R.string.dlg_not_all_fields_entered));
            return;
        }

        showProgress();

        user.setFullName(fullnameCurrent);
        user.setPhone(phoneCurrent);
        user.setCustomData(statusCurrent);

        if (isNeedUpdateAvatar) {
            new ReceiveImageFileTask(this).execute(imageHelper, avatarBitmapCurrent, true);
        } else {
            QBUpdateUserCommand.start(this, user, null);
        }

        stopChangingEditText(fullNameEditText);
        stopChangingEditText(phoneEditText);
        stopChangingEditText(statusEditText);
    }

    private boolean isUserDataCorrect() {
        return fullnameCurrent.length() > Consts.ZERO_INT_VALUE;
    }

    private void updateOldUserData() {
        fullnameOld = fullNameEditText.getText().toString();
        phoneOld = phoneEditText.getText().toString();
        statusOld = statusEditText.getText().toString();
        isNeedUpdateAvatar = false;
    }

    private void resetUserData() {
        user.setFullName(fullnameOld);
        user.setPhone(phoneOld);
        user.setCustomData(statusOld);
        isNeedUpdateAvatar = false;
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
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            DialogUtils.showLong(ProfileActivity.this, exception.getMessage());
            resetUserData();
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
                updateUserData();
            }
            actionMode = null;
        }
    }

    private class UpdateUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            AppSession.getSession().updateUser(user);
            updateOldUserData();
            hideProgress();
        }
    }
}