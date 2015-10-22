package com.quickblox.q_municate.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.tasks.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate.ui.activities.others.BaseFriendsListActivity;
import com.quickblox.q_municate.ui.adapters.friends.FriendsAdapter;
import com.quickblox.q_municate.ui.fragments.dialogs.ImageSourcePickDialogFragment;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.image.ImageSource;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.utils.listeners.OnImageSourcePickedListener;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.chat.QBCreateGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_db.models.User;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class CreateGroupDialogActivity extends BaseFriendsListActivity implements OnImageSourcePickedListener,
        ReceiveFileFromBitmapTask.ReceiveFileListener {

    private static final String EXTRA_FRIENDS_LIST = "friends_list";

    @Bind(R.id.photo_imageview)
    RoundedImageView photoImageView;

    @Bind(R.id.group_name_edittext)
    EditText groupNameEditText;

    @Bind(R.id.participants_count_textview)
    TextView participantsCountTextView;

    private QBFile qbFile;
    private List<User> friendsList;
    private ImageUtils imageUtils;
    private Bitmap photoBitmap;

    public static void start(Context context, List<User> selectedFriendsList) {
        Intent intent = new Intent(context, CreateGroupDialogActivity.class);
        intent.putExtra(EXTRA_FRIENDS_LIST, (Serializable) selectedFriendsList);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_create_group_dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFields();

        addActions();
    }

    private void initFields() {
        friendsList = (List<User>) getIntent().getExtras().getSerializable(EXTRA_FRIENDS_LIST);
        participantsCountTextView.setText(getString(R.string.create_group_participants, friendsList.size()));
        imageUtils = new ImageUtils(this);
    }

    @Override
    protected FriendsAdapter getFriendsAdapter() {
        return new FriendsAdapter(this, friendsList, false);
    }

    @OnClick(R.id.photo_imageview)
    void selectPhoto(View view) {
        canPerformLogout.set(false);
        ImageSourcePickDialogFragment.show(getSupportFragmentManager(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @Override
    protected void performDone() {
        groupNameEditText.setError(null);

        if (!TextUtils.isEmpty(groupNameEditText.getText())) {
            createGroupChat();
        } else {
            groupNameEditText.setError(getString(R.string.create_group_empty_group_name));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        canPerformLogout.set(true);
        if ((imageUtils.isGalleryCalled(requestCode) || imageUtils.isCaptureCalled(requestCode)) && resultCode == RESULT_OK) {
            if (data.getData() == null) {
                onFileSelected((Bitmap) data.getExtras().get("data"));
            } else {
                onFileSelected(data.getData());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onFileSelected(Uri originalUri) {
        Bitmap bitmap = imageUtils.getBitmap(originalUri);
        onFileSelected(bitmap);
    }

    private void onFileSelected(Bitmap bitmap) {
        photoBitmap = bitmap;
        new ReceiveFileFromBitmapTask(this).execute(imageUtils, bitmap, true);
    }

    private void addActions() {
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION, new LoadAttachFileSuccessAction());
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION, new CreateGroupChatSuccessAction());
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);

        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void createGroupChat() {
        if (friendsList != null && !friendsList.isEmpty()) {
            showProgress();
            String photoUrl = qbFile != null ? qbFile.getPublicUrl() : null;
            QBCreateGroupDialogCommand.start(this, groupNameEditText.getText().toString(), (ArrayList<User>) friendsList, photoUrl);
        }
    }

    @Override
    public void onImageSourcePicked(ImageSource source) {
        switch (source) {
            case GALLERY:
                imageUtils.getImage();
                break;
            case CAMERA:
                imageUtils.getCaptureImage();
                break;
        }
    }

    @Override
    public void onImageSourceClosed() {
        canPerformLogout.set(true);
    }

    @Override
    public void onCachedImageFileReceived(File imageFile) {
        startLoadAttachFile(imageFile);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    private void startLoadAttachFile(final File file) {
        TwoButtonsDialogFragment.show(
                getSupportFragmentManager(),
                R.string.create_group_confirm_selecting_photo,
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        showProgress();
                        QBLoadAttachFileCommand.start(CreateGroupDialogActivity.this, file);
                    }
                });
    }

    private class LoadAttachFileSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            qbFile = (QBFile) bundle.getSerializable(QBServiceConsts.EXTRA_ATTACH_FILE);
            photoImageView.setImageBitmap(photoBitmap);
            hideProgress();
        }
    }

    private class CreateGroupChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            QBDialog dialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);

            if (dialog != null) {
                GroupDialogActivity.start(CreateGroupDialogActivity.this, ChatUtils.createLocalDialog(dialog));
                finish();
            } else {
                ErrorUtils.showError(CreateGroupDialogActivity.this, getString(R.string.dlg_fail_create_groupchat));
            }
        }
    }
}