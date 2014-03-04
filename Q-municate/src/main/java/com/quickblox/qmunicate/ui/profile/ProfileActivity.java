package com.quickblox.qmunicate.ui.profile;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBLoadImageTask;
import com.quickblox.qmunicate.qb.QBUpdateUserTask;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.utils.ImageHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends BaseActivity {
    private LinearLayout linearLayoutChangeAvatar;
    private ImageView imageViewAvatar;
    private EditText editTextFullName;
    private TextView textViewChangeFullName;
    private EditText editTextEmail;
    private TextView textViewChangeEmail;
    private EditText editTextStatusMessage;
    private TextView textViewChangeStatusMessage;

    private Bitmap bitmap;
    private String pathToImage;
    private ImageHelper imageHelper;
    private Bitmap bitmapAvatarOld;
    private String fullnameOld;
    private String emailOld;
    private Activity thisActivity;
    private QBUser qbUser;
    private boolean useDoubleBackPressed;
    private boolean doubleBackToExitPressedOnce;
    private boolean isNeedUpdateAvatar;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(this);

        thisActivity = this;
        qbUser = App.getInstance().getUser();
        imageHelper = new ImageHelper(this);
        useDoubleBackPressed = true;

        linearLayoutChangeAvatar.setOnClickListener(linearLayoutChangeAvatarOnClickListener);
        textViewChangeFullName.setOnClickListener(textViewChangeFullNameOnClickListener);
        textViewChangeEmail.setOnClickListener(textViewChangeEmailOnClickListener);
        textViewChangeStatusMessage.setOnClickListener(textViewChangeStatusMessageOnClickListener);

        initUsersData();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (this.doubleBackToExitPressedOnce || !this.useDoubleBackPressed) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;

        Bitmap avatar = ((BitmapDrawable) imageViewAvatar.getDrawable()).getBitmap();
        String fullname = editTextFullName.getText().toString();
        String email = editTextEmail.getText().toString();

        if (isUserDataChanges(fullname, email)) {
            saveChanges(avatar, fullname, email);
        } else {
            super.onBackPressed();
        }
    }

    private void findViewById(Activity activity) {
        linearLayoutChangeAvatar = (LinearLayout) activity.findViewById(R.id.linearLayoutChangeAvatar);
        imageViewAvatar = (ImageView) findViewById(R.id.imageViewAvatar);
        editTextFullName = (EditText) findViewById(R.id.editTextFullName);
        textViewChangeFullName = (TextView) findViewById(R.id.textViewChangeFullName);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        textViewChangeEmail = (TextView) findViewById(R.id.textViewChangeEmail);
        editTextStatusMessage = (EditText) findViewById(R.id.editTextStatusMessage);
        textViewChangeStatusMessage = (TextView) findViewById(R.id.textViewChangeStatusMessage);
    }

    View.OnClickListener linearLayoutChangeAvatarOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imageHelper.getImage();
        }
    };

    View.OnClickListener textViewChangeFullNameOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            initChangingEditText(editTextFullName);
        }
    };

    View.OnClickListener textViewChangeEmailOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            initChangingEditText(editTextEmail);
        }
    };

    View.OnClickListener textViewChangeStatusMessageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            initChangingEditText(editTextStatusMessage);
        }
    };

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
        try {
            if (resultCode == RESULT_OK) {
                isNeedUpdateAvatar = true;
                bitmapAvatarOld = ((BitmapDrawable) imageViewAvatar.getDrawable()).getBitmap();
                Uri originalUri = data.getData();
                InputStream stream = getContentResolver().openInputStream(originalUri);
                if (requestCode == imageHelper.GALLERY_KITKAT_INTENT_CALLED) {
                    pathToImage = imageHelper.getPath(originalUri, data.getFlags());
                } else if (requestCode == imageHelper.GALLERY_INTENT_CALLED) {
                    pathToImage = imageHelper.getPath(originalUri);
                }
                setSelectedAvatar(stream);
                stream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setSelectedAvatar(InputStream stream) {
        if (bitmap != null) {
            bitmap.recycle();
        }
        bitmap = BitmapFactory.decodeStream(stream);
        imageViewAvatar.setImageBitmap(bitmap);
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
        new QBLoadImageTask(thisActivity).execute(fileId, imageView);
    }
}