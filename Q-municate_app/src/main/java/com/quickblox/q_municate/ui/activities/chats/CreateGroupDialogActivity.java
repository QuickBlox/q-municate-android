package com.quickblox.q_municate.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.others.BaseFriendsListActivity;
import com.quickblox.q_municate.ui.activities.profile.UserProfileActivity;
import com.quickblox.q_municate.ui.adapters.friends.FriendsAdapter;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.helpers.ImagePickHelper;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.utils.listeners.OnImagePickedListener;
import com.quickblox.q_municate.utils.listeners.simple.SimpleOnRecycleItemClickListener;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.chat.QBCreateGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class CreateGroupDialogActivity extends BaseFriendsListActivity implements OnImagePickedListener {

    private static final String EXTRA_FRIENDS_LIST = "friends_list";

    @Bind(R.id.photo_imageview)
    RoundedImageView photoImageView;

    @Bind(R.id.group_name_edittext)
    EditText groupNameEditText;

    @Bind(R.id.participants_count_textview)
    TextView participantsCountTextView;

    private QBFile qbFile;
    private List<User> friendsList;
    private ImagePickHelper imagePickHelper;
    private Uri imageUri;

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

    @Override
    protected void onStart() {
        super.onStart();
        setUpActionBarWithUpButton();
    }

    @Override
    protected FriendsAdapter getFriendsAdapter() {
        return new FriendsAdapter(this, friendsList, false);
    }

    @Override
    protected void initRecyclerView() {
        super.initRecyclerView();
        friendsAdapter.setOnRecycleItemClickListener(new SimpleOnRecycleItemClickListener<User>() {

            @Override
            public void onItemClicked(View view, User entity, int position) {
                UserProfileActivity.start(CreateGroupDialogActivity.this, entity.getUserId());
            }
        });
    }

    @OnClick(R.id.photo_imageview)
    void selectPhoto(View view) {
        imagePickHelper.pickAnImage(this, ImageUtils.IMAGE_REQUEST_CODE);
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
            checkForCreatingGroupChat();
        } else {
            groupNameEditText.setError(getString(R.string.create_group_empty_group_name));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        title = getString(R.string.create_group_title);
        friendsList = (List<User>) getIntent().getExtras().getSerializable(EXTRA_FRIENDS_LIST);
        participantsCountTextView.setText(getString(R.string.create_group_participants, friendsList.size()));
        imagePickHelper = new ImagePickHelper();
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

    private void checkForCreatingGroupChat() {
        if (isChatInitializedAndUserLoggedIn()) {
            if (friendsList != null && !friendsList.isEmpty()) {
                showProgress();

                if (imageUri != null) {
                    QBLoadAttachFileCommand.start(CreateGroupDialogActivity.this, ImageUtils.getCreatedFileFromUri(imageUri));
                } else {
                    createGroupChat();
                }
            }
        } else {
            ToastUtils.longToast(R.string.chat_service_is_initializing);
        }
    }

    private void createGroupChat() {
        String photoUrl = qbFile != null ? qbFile.getPublicUrl() : null;
        QBCreateGroupDialogCommand.start(this, groupNameEditText.getText().toString(), (ArrayList<User>) friendsList, photoUrl);
    }

    private void startCropActivity(Uri originalUri) {
        canPerformLogout.set(false);
        imageUri = Uri.fromFile(new File(getCacheDir(), Crop.class.getName()));
        Crop.of(originalUri, imageUri).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            if (imageUri != null) {
                photoImageView.setImageURI(imageUri);
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            ToastUtils.longToast(Crop.getError(result).getMessage());
        }
        canPerformLogout.set(true);
    }

    private class LoadAttachFileSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            qbFile = (QBFile) bundle.getSerializable(QBServiceConsts.EXTRA_ATTACH_FILE);
            createGroupChat();
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