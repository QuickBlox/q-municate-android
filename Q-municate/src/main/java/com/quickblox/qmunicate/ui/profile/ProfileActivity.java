package com.quickblox.qmunicate.ui.profile;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBLoadImageTask;
import com.quickblox.qmunicate.qb.QBUpdateUserTask;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.utils.ImageHelper;

import java.io.File;

public class ProfileActivity extends BaseActivity {
    private ImageView imageViewAvatar;
    private EditText editTextFullName;
    private EditText editTextEmail;
    private EditText editTextStatusMessage;

    private String pathToImage;
    private ImageHelper imageHelper;
    private Bitmap bitmapAvatarOld;
    private String fullnameOld;
    private String emailOld;
    private QBUser qbUser;
    private boolean isNeedUpdateAvatar;

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

        initUsersData();
    }

    @Override
    public void onBackPressed() {
        Bitmap avatar = ((BitmapDrawable) imageViewAvatar.getDrawable()).getBitmap();
        String fullname = editTextFullName.getText().toString();
        String email = editTextEmail.getText().toString();

        if (isUserDataChanges(fullname, email)) {
            saveChanges(avatar, fullname, email);
        } else {
            super.onBackPressed();
        }
    }

    private void initUI() {
        imageViewAvatar = _findViewById(R.id.imageViewAvatar);
        editTextFullName = _findViewById(R.id.editTextFullName);
        editTextEmail = _findViewById(R.id.editTextEmail);
        editTextStatusMessage = _findViewById(R.id.editTextStatusMessage);
    }

    public void onClickChangeAvatar(View view) {
        imageHelper.getImage();
    }

    public void onClickChangeFullName(View view) {
        initChangingEditText(editTextFullName);
    }

    public void onClickChangeEmail(View view) {
        initChangingEditText(editTextEmail);
    }

    public void onClickChangeStatusMessage(View view) {
        initChangingEditText(editTextStatusMessage);
    }

    private void initChangingEditText(EditText editText) {
        editText.setText("");
        editText.setEnabled(true);
        editText.requestFocus();
    }

    private void initUsersData() {
        displayAvatar(qbUser.getFileId(), imageViewAvatar);
        editTextFullName.setText(qbUser.getFullName());
        editTextEmail.setText(qbUser.getEmail());

        bitmapAvatarOld = ((BitmapDrawable) imageViewAvatar.getDrawable()).getBitmap();
        fullnameOld = editTextFullName.getText().toString();
        emailOld = editTextEmail.getText().toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            bitmapAvatarOld = ((BitmapDrawable) imageViewAvatar.getDrawable()).getBitmap();
            Uri originalUri = data.getData();
            if (requestCode == imageHelper.GALLERY_KITKAT_INTENT_CALLED) {
                pathToImage = imageHelper.getPath(originalUri, data.getFlags());
            } else if (requestCode == imageHelper.GALLERY_INTENT_CALLED) {
                pathToImage = imageHelper.getPath(originalUri);
            }
            imageViewAvatar.setImageURI(originalUri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void saveChanges(Bitmap avatar, String fullname, String email) {
        if (isUserDataChanges(fullname, email)) {
            File image = null;

            qbUser.setFullName(fullname);
            qbUser.setEmail(email);

            if (isAvatarChanges(avatar) && isNeedUpdateAvatar) {
                image = new File(pathToImage);
            }

            new QBUpdateUserTask(ProfileActivity.this).execute(qbUser, image);
        }
    }

    private boolean isAvatarChanges(Bitmap avatar) {
        return !imageHelper.equalsBitmaps(bitmapAvatarOld, avatar);
    }

    private boolean isUserDataChanges(String fullname, String email) {
        return isNeedUpdateAvatar || !fullname.equals(fullnameOld) || !email.equals(emailOld);
    }

    private void displayAvatar(Integer fileId, ImageView imageView) {
        new QBLoadImageTask(this).execute(fileId, imageView);
    }
}