package com.quickblox.q_municate.ui.chats;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.model.GroupDialog;
import com.quickblox.q_municate.qb.commands.QBLeaveGroupDialogCommand;
import com.quickblox.q_municate.qb.commands.QBLoadGroupDialogCommand;
import com.quickblox.q_municate.qb.commands.QBUpdateGroupDialogCommand;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.dialogs.ConfirmDialog;
import com.quickblox.q_municate.ui.friends.FriendDetailsActivity;
import com.quickblox.q_municate.ui.profile.ProfileActivity;
import com.quickblox.q_municate.ui.uihelper.SimpleActionModeCallback;
import com.quickblox.q_municate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DialogUtils;
import com.quickblox.q_municate.utils.FriendUtils;
import com.quickblox.q_municate.utils.ImageUtils;
import com.quickblox.q_municate.utils.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate.utils.ReceiveUriScaledBitmapTask;
import com.soundcloud.android.crop.Crop;

import java.io.File;

public class GroupDialogDetailsActivity extends BaseLogeableActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener, AdapterView.OnItemClickListener, ReceiveUriScaledBitmapTask.ReceiveUriScaledBitmapListener {

    public static final int UPDATE_DIALOG_REQUEST_CODE = 100;
    public static final int RESULT_LEAVE_GROUP = 2;

    private EditText groupNameEditText;
    private TextView participantsTextView;
    private ListView friendsListView;
    private TextView onlineParticipantsTextView;
    private RoundedImageView avatarImageView;

    private String dialogId;
    private GroupDialog groupDialog;

    private Object actionMode;
    private boolean closeActionMode;
    private boolean isNeedUpdateAvatar;
    private Uri outputUri;

    private Bitmap avatarBitmapCurrent;
    private QBDialog dialogCurrent;
    private String groupNameCurrent;

    private String photoUrlOld;
    private String groupNameOld;

    private ImageUtils imageUtils;
    private GroupDialogOccupantsAdapter groupDialogOccupantsAdapter;

    public static void start(Activity context, String dialogId) {
        Intent intent = new Intent(context, GroupDialogDetailsActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        context.startActivityForResult(intent, UPDATE_DIALOG_REQUEST_CODE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_dialog_details);
        dialogId = (String) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG_ID);
        dialogCurrent = DatabaseManager.getDialogByDialogId(this, dialogId);
        groupDialog = new GroupDialog(dialogCurrent);
        imageUtils = new ImageUtils(this);
        initUI();
        initUIWithData();
        addActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLoadGroupDialog();
    }

    @Override
    public void onUriScaledBitmapReceived(Uri originalUri) {
        hideProgress();
        startCropActivity(originalUri);
    }

    private void startLoadGroupDialog() {
        QBLoadGroupDialogCommand.start(this, dialogId, groupDialog.getRoomJid());
    }

    public void changeAvatarOnClick(View view) {
        canPerformLogout.set(false);
        imageUtils.getImage();
    }

    private void initUI() {
        avatarImageView = _findViewById(R.id.avatar_imageview);
        groupNameEditText = _findViewById(R.id.name_textview);
        participantsTextView = _findViewById(R.id.participants_textview);
        friendsListView = _findViewById(R.id.chat_friends_listview);
        onlineParticipantsTextView = _findViewById(R.id.online_participants_textview);
    }

    private void initUIWithData() {
        groupNameEditText.setText(groupDialog.getName());
        participantsTextView.setText(getString(R.string.gdd_participants, groupDialog.getOccupantsCount()));
        onlineParticipantsTextView.setText(getString(R.string.gdd_online_participants,
                groupDialog.getOnlineOccupantsCount(), groupDialog.getOccupantsCount()));
        loadAvatar(groupDialog.getPhotoUrl());
        updateOldGroupData();
    }

    private void loadAvatar(String photoUrl) {
        ImageLoader.getInstance().displayImage(photoUrl, avatarImageView,
                Consts.UIL_GROUP_AVATAR_DISPLAY_OPTIONS);
    }

    private void initListView() {
        groupDialogOccupantsAdapter = getFriendsAdapter();
        friendsListView.setAdapter(groupDialogOccupantsAdapter);
        friendsListView.setOnItemClickListener(this);
    }

    private void addActions() {
        UpdateGroupFailAction updateGroupFailAction = new UpdateGroupFailAction();
        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_SUCCESS_ACTION, new LoadGroupDialogSuccessAction());
        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_SUCCESS_ACTION, new LeaveGroupDialogSuccessAction());
        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.UPDATE_GROUP_NAME_SUCCESS_ACTION, new UpdateGroupNameSuccessAction());
        addAction(QBServiceConsts.UPDATE_GROUP_NAME_FAIL_ACTION, updateGroupFailAction);
        addAction(QBServiceConsts.UPDATE_USER_SUCCESS_ACTION, new UpdateGroupPhotoSuccessAction());
        addAction(QBServiceConsts.UPDATE_USER_FAIL_ACTION, updateGroupFailAction);
        updateBroadcastActionList();
    }

    protected GroupDialogOccupantsAdapter getFriendsAdapter() {
        return new GroupDialogOccupantsAdapter(this, groupDialog.getOccupantList());
    }

    private void showLeaveGroupDialog() {
        ConfirmDialog dialog = ConfirmDialog.newInstance(R.string.dlg_leave_group, R.string.dlg_confirm);
        dialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBLeaveGroupDialogCommand.start(GroupDialogDetailsActivity.this, groupDialog.getRoomJid());
            }
        });
        dialog.show(getFragmentManager(), null);
    }

    private void initTextChangedListeners() {
        groupNameEditText.addTextChangedListener(new GroupNameTextWatcherListener());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            groupNameEditText.setText(groupDialog.getName());
            closeActionMode = true;
            ((ActionMode) actionMode).finish();
            return true;
        } else {
            closeActionMode = false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_dialog_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            case R.id.action_add:
                startAddFriendsActivity();
                return true;
            case R.id.action_leave:
                showLeaveGroupDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startAddFriendsActivity() {
        int countUnselectedFriendsInChat = DatabaseManager.getFriendsFilteredByIds(this, FriendUtils.getFriendIds(groupDialog.getOccupantList())).getCount();
        if (countUnselectedFriendsInChat != Consts.ZERO_INT_VALUE) {
            AddFriendsToGroupActivity.start(this, groupDialog);
        } else {
            DialogUtils.showLong(this, getResources().getString(R.string.gdd_all_friends_is_in_group));
        }
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
        canPerformLogout.set(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            avatarBitmapCurrent = imageUtils.getBitmap(outputUri);
            avatarImageView.setImageBitmap(avatarBitmapCurrent);
            startAction();
        } else if (resultCode == Crop.RESULT_ERROR) {
            DialogUtils.showLong(this, Crop.getError(result).getMessage());
        }
    }

    private void startCropActivity(Uri originalUri) {
        outputUri = Uri.fromFile(new File(getCacheDir(), Crop.class.getName()));
        new Crop(originalUri).output(outputUri).asSquare().start(this);
    }

    private void startAction() {
        if (actionMode != null) {
            return;
        }
        actionMode = startActionMode(new ActionModeCallback());
    }

    private void updateCurrentUserData() {
        groupNameCurrent = groupNameEditText.getText().toString();
    }

    private void updateUserData() {
        updateCurrentUserData();
        if (isGroupDataChanged()) {
            saveChanges();
        }
    }

    private boolean isGroupDataChanged() {
        return !groupNameCurrent.equals(groupNameOld) || isNeedUpdateAvatar;
    }

    private void saveChanges() {
        if (!isUserDataCorrect()) {
            DialogUtils.showLong(this, getString(R.string.gdd_name_not_entered));
            return;
        }

        dialogCurrent.setName(groupNameCurrent);

        if (isNeedUpdateAvatar) {
            new ReceiveFileFromBitmapTask(this).execute(imageUtils, avatarBitmapCurrent, true);
        } else {
            startUpdatingGroupDialog(null);
        }

        showProgress();
    }

    private boolean isUserDataCorrect() {
        return !TextUtils.isEmpty(groupNameCurrent);
    }

    private void updateOldGroupData() {
        groupNameOld = groupDialog.getName();
        photoUrlOld = groupDialog.getPhotoUrl();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        Friend selectedFriend = groupDialogOccupantsAdapter.getItem(position);
        if (selectedFriend != null) {
            startFriendProfile(selectedFriend);
        }
    }

    private void startFriendProfile(Friend selectedFriend) {
        QBUser currentUser = AppSession.getSession().getUser();
        if (currentUser.getId() == selectedFriend.getId()) {
            ProfileActivity.start(GroupDialogDetailsActivity.this);
        } else {
            FriendDetailsActivity.start(GroupDialogDetailsActivity.this, selectedFriend.getId());
        }
    }

    private void resetGroupData() {
        groupNameEditText.setText(groupNameOld);
        isNeedUpdateAvatar = false;
        loadAvatar(photoUrlOld);
    }

    private void startUpdatingGroupDialog(File imageFile) {
        QBUpdateGroupDialogCommand.start(this, dialogCurrent, imageFile);
    }

    @Override
    public void onCachedImageFileReceived(File imageFile) {
        startUpdatingGroupDialog(imageFile);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {

    }

    private class GroupNameTextWatcherListener extends SimpleTextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!groupNameOld.equals(s.toString())) {
                startAction();
            }
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

    private class LoadGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            groupDialog = (GroupDialog) bundle.getSerializable(QBServiceConsts.EXTRA_GROUP_DIALOG);
            updateOldGroupData();
            initUIWithData();
            initTextChangedListeners();
            initListView();
            hideProgress();
        }
    }

    private class UpdateGroupFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            DialogUtils.showLong(GroupDialogDetailsActivity.this, exception.getMessage());
            resetGroupData();
            hideProgress();
        }
    }

    private class LeaveGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            setResult(RESULT_LEAVE_GROUP, getIntent());
            finish();
        }
    }

    private class UpdateGroupNameSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            updateOldGroupData();
            hideProgress();
        }
    }

    private class UpdateGroupPhotoSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBDialog dialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            groupDialog = new GroupDialog(DatabaseManager.getDialogByDialogId(GroupDialogDetailsActivity.this,
                    dialog.getDialogId()));
            updateOldGroupData();
            hideProgress();
        }
    }
}