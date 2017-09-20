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
import com.quickblox.q_municate.utils.helpers.MediaPickHelper;
import com.quickblox.q_municate.utils.helpers.ServiceManager;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.MediaUtils;
import com.quickblox.q_municate.utils.listeners.OnMediaPickedListener;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.Subscriber;

public class MyProfileActivity extends BaseLoggableActivity implements OnMediaPickedListener {

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
    private MediaPickHelper mediaPickHelper;
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
    }

    @OnTextChanged(R.id.full_name_edittext)
    void onTextChangedFullName(CharSequence text) {
        fullNameTextInputLayout.setError(null);
    }

    @OnClick(R.id.change_photo_view)
    void changePhoto(View view) {
        fullNameTextInputLayout.setError(null);
        mediaPickHelper.pickAnMedia(this, MediaUtils.IMAGE_REQUEST_CODE);
    }

    @Override
    public void onMediaPicked(int requestCode, Attachment.Type attachmentType, Object attachment) {
        if (Attachment.Type.IMAGE.equals(attachmentType)) {
            canPerformLogout.set(true);
            startCropActivity(MediaUtils.getValidUri((File) attachment, this));
        }
    }

    @Override
    public void onMediaPickError(int requestCode, Exception e) {
        canPerformLogout.set(true);
        ErrorUtils.showError(this, e);
    }

    @Override
    public void onMediaPickClosed(int requestCode) {
        canPerformLogout.set(true);
    }

    private void initFields() {
        title = getString(R.string.profile_title);
        mediaPickHelper = new MediaPickHelper();
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
        if (userCustomData != null && !TextUtils.isEmpty(userCustomData.getAvatarUrl())) {
            ImageLoader.getInstance().displayImage(userCustomData.getAvatarUrl(),
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
        String extensionOriginalUri = originalUri.getPath().substring(originalUri.getPath().lastIndexOf(".")).toLowerCase();

        canPerformLogout.set(false);
        imageUri = MediaUtils.getValidUri(new File(getCacheDir(), extensionOriginalUri), this);
        Crop.of(originalUri, imageUri).asSquare().start(this);
    }

    private QBUser createUserForUpdating() {
        QBUser newUser = new QBUser();
        newUser.setId(qbUser.getId());
        newUser.setPassword(qbUser.getPassword());
        newUser.setOldPassword(qbUser.getOldPassword());
        qbUser.setFullName(currentFullName);
        newUser.setFullName(currentFullName);
        newUser.setFacebookId(qbUser.getFacebookId());
        newUser.setTwitterId(qbUser.getTwitterId());
        newUser.setTwitterDigitsId(qbUser.getTwitterDigitsId());
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
        if (new ValidationUtils(this).isFullNameValid(fullNameTextInputLayout, currentFullName.trim())) {
            showProgress();

            QBUser newUser = createUserForUpdating();
            File file = null;
            if (isNeedUpdateImage && imageUri != null) {
                file = MediaUtils.getCreatedFileFromUri(imageUri);
            }

            Observable<QMUser> qmUserObservable;

            if (file != null) {
                qmUserObservable = ServiceManager.getInstance().updateUser(newUser, file);
            } else {
                qmUserObservable = ServiceManager.getInstance().updateUser(newUser);
            }

            qmUserObservable.subscribe(new Subscriber<QMUser>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    hideProgress();

                    if (e != null) {
                        ToastUtils.longToast(e.getMessage());
                    }

                    resetUserData();
                }

                @Override
                public void onNext(QMUser qmUser) {
                    hideProgress();
                    AppSession.getSession().updateUser(qmUser);
                    updateOldData();
                }
            });
        }
    }
}