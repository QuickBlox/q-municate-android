package com.quickblox.q_municate.ui.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.ValidationUtils;
import com.quickblox.q_municate.utils.helpers.ImagePickHelper;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.utils.listeners.OnImagePickedListener;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class MyProfileActivity extends BaseLoggableActivity implements OnImagePickedListener {

    private static String TAG = MyProfileActivity.class.getSimpleName();

    @Bind(R.id.photo_imageview)
    RoundedImageView photoImageView;

    @Bind(R.id.full_name_textinputlayout)
    TextInputLayout fullNameTextInputLayout;

    @Bind(R.id.full_name_edittext)
    EditText fullNameEditText;

    private QBUser qbUser;
    private boolean isNeedUpdateImage;
    private UserCustomData userCustomData;
    private String currentFullName;
    private String oldFullName;
    private ImagePickHelper imagePickHelper;
    private Uri imageUri;

    public static void start(Context context) {
        Intent intent = new Intent(context, MyProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_my_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();

        initData();
        addActions();
        updateOldData();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                if (checkNetworkAvailableWithError()) {
                    updateUser();
                }
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @OnTextChanged(R.id.full_name_edittext)
    void onTextChangedFullName(CharSequence text) {
        fullNameTextInputLayout.setError(null);
    }

    @OnClick(R.id.change_photo_view)
    void changePhoto(View view) {
        fullNameTextInputLayout.setError(null);
        imagePickHelper.pickAnImage(this, ImageUtils.IMAGE_REQUEST_CODE);
    }

    @Override
    public void onImagePicked(int requestCode, File file) {
        canPerformLogout.set(true);
        startCropActivity(Uri.fromFile(file));
    }

    @Override
    public void onImagePickError(int requestCode, Exception e) {
        canPerformLogout.set(true);
        ErrorUtils.showError(this, e);
    }

    @Override
    public void onImagePickClosed(int requestCode) {
        canPerformLogout.set(true);
    }

    private void initFields() {
        title = getString(R.string.profile_title);
        imagePickHelper = new ImagePickHelper();
        qbUser = AppSession.getSession().getUser();
    }

    private void initData() {
        currentFullName = qbUser.getFullName();
        initCustomData();
        loadAvatar();
        fullNameEditText.setText(currentFullName);
    }

    private void initCurrentData() {
        currentFullName = fullNameEditText.getText().toString();
        initCustomData();
    }

    private void initCustomData() {
        userCustomData = Utils.customDataToObject(qbUser.getCustomData());
        if (userCustomData == null) {
            userCustomData = new UserCustomData();
        }
    }

    private void loadAvatar() {
        if (userCustomData != null && !TextUtils.isEmpty(userCustomData.getAvatar_url())) {
            ImageLoader.getInstance().displayImage(userCustomData.getAvatar_url(),
                    photoImageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
        }
    }

    private void updateOldData() {
        oldFullName = fullNameEditText.getText().toString();
        isNeedUpdateImage = false;
    }

    private void resetUserData() {
        qbUser.setFullName(oldFullName);
        isNeedUpdateImage = false;
        initCurrentData();
    }

    private void addActions() {
        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, new UpdateUserSuccessAction());
        addAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION, new UpdateUserFailAction());

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION);
        removeAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateImage = true;
            photoImageView.setImageURI(imageUri);
        } else if (resultCode == Crop.RESULT_ERROR) {
            ToastUtils.longToast(Crop.getError(result).getMessage());
        }
        canPerformLogout.set(true);
    }

    private void startCropActivity(Uri originalUri) {
        canPerformLogout.set(false);
        imageUri = Uri.fromFile(new File(getCacheDir(), Crop.class.getName()));
        Crop.of(originalUri, imageUri).asSquare().start(this);
    }

    private QBUser createUserForUpdating() {
        QBUser newUser = new QBUser();
        newUser.setId(qbUser.getId());
        newUser.setPassword(qbUser.getPassword());
        newUser.setOldPassword(qbUser.getOldPassword());
        qbUser.setFullName(currentFullName);
        newUser.setFullName(currentFullName);
        newUser.setCustomData(Utils.customDataToString(userCustomData));
        return newUser;
    }

    private boolean isDataChanged() {
        return isNeedUpdateImage || !oldFullName.equals(currentFullName);
    }

    private boolean isFullNameNotEmpty() {
        return !TextUtils.isEmpty(currentFullName.trim());
    }

    private void updateUser() {
        initCurrentData();

        if (isDataChanged()) {
            saveChanges();
        } else {
            fullNameTextInputLayout.setError(getString(R.string.profile_full_name_and_photo_not_changed));
        }
    }

    private void saveChanges() {
        if (isFullNameNotEmpty()) {
            showProgress();

            if (isNeedUpdateImage && imageUri != null) {
                QBUser newUser = createUserForUpdating();
                QBUpdateUserCommand.start(this, newUser, ImageUtils.getCreatedFileFromUri(imageUri));
            } else {
                QBUser newUser = createUserForUpdating();
                QBUpdateUserCommand.start(this, newUser, null);
            }
        } else {
            new ValidationUtils(this).isFullNameValid(fullNameTextInputLayout, oldFullName, currentFullName);
        }
    }

    public class UpdateUserFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();

            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            if (exception != null) {
                ToastUtils.longToast(exception.getMessage());
            }

            resetUserData();
        }
    }

    private class UpdateUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();

            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            AppSession.getSession().updateUser(user);
            updateOldData();
        }
    }
}