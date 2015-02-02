package com.quickblox.q_municate.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate.utils.ImageUtils;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate.utils.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate.utils.ReceiveUriScaledBitmapTask;
import com.soundcloud.android.crop.Crop;

import java.io.File;

public class ProfileActivity extends BaseLogeableActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener,
        View.OnClickListener, ReceiveUriScaledBitmapTask.ReceiveUriScaledBitmapListener {

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
    private View actionView;
    private View actionCancelView;
    private View actionDoneView;

    private ImageUtils imageUtils;
    private Bitmap avatarBitmapCurrent;
    private String fullNameCurrent;
    private String phoneCurrent;
    private String statusCurrent;
    private String fullNameOld;
    private String phoneOld;
    private String statusOld;
    private QBUser user;
    private boolean isNeedUpdateAvatar;
    private Uri outputUri;
    private UserCustomData userCustomData;

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
        imageUtils = new ImageUtils(this);

        initUI();
        initListeners();
        initUIWithUsersData();
        initBroadcastActionList();
        initTextChangedListeners();
        updateOldUserData();
        hideAction();
    }

    private void initUI() {
        actionView = _findViewById(R.id.action_view);
        actionCancelView = _findViewById(R.id.action_cancel_view);
        actionDoneView = _findViewById(R.id.action_done_view);
        changeAvatarLinearLayout = _findViewById(R.id.change_avatar_linearlayout);
        avatarImageView = _findViewById(R.id.avatar_imageview);
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

        actionCancelView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetUserData();
                initUIWithUsersData();
                hideAction();
            }
        });

        actionDoneView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateUserData();
            }
        });
    }

    private void initCustomData() {
        userCustomData = Utils.customDataToObject(user.getCustomData());

        if (userCustomData == null) {
            userCustomData = new UserCustomData();
        }
    }

    private void initUIWithUsersData() {
        initCustomData();

        loadAvatar();
        fullNameOld = user.getFullName();
        fullNameEditText.setText(fullNameOld);

        if (TextUtils.isEmpty(user.getEmail())) {
            emailLinearLayout.setVisibility(View.GONE);
        } else {
            emailLinearLayout.setVisibility(View.VISIBLE);
            emailTextView.setText(user.getEmail());
        }

        phoneOld = user.getPhone();
        phoneEditText.setText(phoneOld);

        statusOld = userCustomData.getStatus();

        statusEditText.setText(statusOld);
    }

    private void initBroadcastActionList() {
        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, new UpdateUserSuccessAction());
        addAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION, new UpdateUserFailAction());
    }

    private void loadAvatar() {
        if (userCustomData != null && !TextUtils.isEmpty(userCustomData.getAvatar_url())) {
            ImageLoader.getInstance().displayImage(userCustomData.getAvatar_url(),
                    avatarImageView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
        }
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            showAction();
        } else if (resultCode == Crop.RESULT_ERROR) {
            DialogUtils.showLong(this, Crop.getError(result).getMessage());
        }
        canPerformLogout.set(true);
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

    public void changeAvatarOnClick() {
        canPerformLogout.set(false);
        imageUtils.getImage();
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
        QBUser newUser = createUserForUpdating();
        QBUpdateUserCommand.start(this, newUser, imageFile);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    private void updateCurrentUserData() {
        fullNameCurrent = fullNameEditText.getText().toString();
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
        return isNeedUpdateAvatar || !fullNameCurrent.equals(fullNameOld) || !phoneCurrent.equals(
                phoneOld) || !statusCurrent.equals(statusOld);
    }

    private void saveChanges() {
        if (!isUserDataCorrect()) {
            DialogUtils.showLong(this, getString(R.string.dlg_not_all_fields_entered));
            return;
        }

        showProgress();

        QBUser newUser = createUserForUpdating();

        if (isNeedUpdateAvatar) {
            new ReceiveFileFromBitmapTask(this).execute(imageUtils, avatarBitmapCurrent, true);
        } else {
            QBUpdateUserCommand.start(this, newUser, null);
        }

        stopChangingEditText(fullNameEditText);
        stopChangingEditText(phoneEditText);
        stopChangingEditText(statusEditText);
    }

    private QBUser createUserForUpdating() {
        QBUser newUser = new QBUser();
        newUser.setId(user.getId());

        if (isFieldValueChanged(fullNameCurrent, fullNameOld)) {
            user.setFullName(fullNameCurrent);
            newUser.setFullName(fullNameCurrent);
        }

        if (isFieldValueChanged(phoneCurrent, phoneOld)) {
            user.setPhone(phoneCurrent);
            newUser.setPhone(phoneCurrent);
        }

        if (isFieldValueChanged(statusCurrent, statusOld) || isNeedUpdateAvatar) {
            userCustomData.setStatus(statusCurrent);
            user.setCustomData(Utils.customDataToString(userCustomData));
        }

        newUser.setCustomData(Utils.customDataToString(userCustomData));

        return newUser;
    }

    private boolean isFieldValueChanged(String fieldValue, String oldFieldValue) {
        return !fieldValue.equals(oldFieldValue);
    }

    private boolean isUserDataCorrect() {
        return fullNameCurrent.length() > ConstsCore.ZERO_INT_VALUE;
    }

    private void updateOldUserData() {
        fullNameOld = fullNameEditText.getText().toString();
        phoneOld = phoneEditText.getText().toString();
        statusOld = statusEditText.getText().toString();
        isNeedUpdateAvatar = false;
    }

    private void resetUserData() {
        user.setFullName(fullNameOld);
        user.setPhone(phoneOld);
        initCustomData();

        isNeedUpdateAvatar = false;
    }

    private boolean isActionViewVisible() {
        return actionView.getVisibility() == View.VISIBLE;
    }

    private void showAction() {
        actionView.setVisibility(View.VISIBLE);
    }

    private void hideAction() {
        actionView.setVisibility(View.GONE);
    }

    private class TextWatcherListener extends SimpleTextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!isActionViewVisible()) {
                showAction();
            }
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

    private class UpdateUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            AppSession.getSession().updateUser(user);
            updateOldUserData();
            hideAction();
            hideProgress();
        }
    }
}