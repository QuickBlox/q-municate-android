package com.quickblox.qmunicate.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.utils.DialogUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ProfileActivity extends BaseActivity {
    private static final int REQUEST_CODE = 1;

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

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        findViewById(this);

        linearLayoutChangeAvatar.setOnClickListener(linearLayoutChangeAvatarOnClickListener);
        textViewChangeFullName.setOnClickListener(textViewChangeFullNameOnClickListener);
        textViewChangeEmail.setOnClickListener(textViewChangeEmailOnClickListener);
        textViewChangeStatusMessage.setOnClickListener(textViewChangeStatusMessageOnClickListener);

        actionBar.setDisplayHomeAsUpEnabled(true);
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
            getImage();
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

    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.private_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_audio_call:
                // TODO add audio call
                DialogUtils.show(this, getString(R.string.comming_soon));
                return true;
            case R.id.action_video_call:
                // TODO add video call
                DialogUtils.show(this, getString(R.string.comming_soon));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
            try {
                pathToImage = getPath(data.getData());
                if (bitmap != null) {
                    bitmap.recycle();
                }
                InputStream stream = getContentResolver().openInputStream(data.getData());
                bitmap = BitmapFactory.decodeStream(stream);
                stream.close();
                imageViewAvatar.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String getPath(Uri uri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
}