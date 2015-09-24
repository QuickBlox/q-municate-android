package com.quickblox.q_municate.ui.activities.chats;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.view.ActionMode;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.adapters.chats.GroupDialogOccupantsAdapter;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.ui.activities.profile.UserProfileActivity;
import com.quickblox.q_municate.core.listeners.UserOperationListener;
import com.quickblox.q_municate.ui.activities.profile.MyProfileActivity;
import com.quickblox.q_municate.ui.uihelpers.SimpleActionModeCallback;
import com.quickblox.q_municate.ui.uihelpers.SimpleTextWatcher;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.utils.image.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate.utils.image.ReceiveUriScaledBitmapTask;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.GroupDialog;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBLeaveGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoadGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBUpdateGroupDialogCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
    private Dialog currentDialog;
    private String groupNameCurrent;
    private String photoUrlOld;
    private String groupNameOld;
    private ImageUtils imageUtils;
    private GroupDialogOccupantsAdapter groupDialogOccupantsAdapter;
    private List<DialogNotification.Type> currentNotificationTypeList;
    private ArrayList<Integer> addedFriendIdsList;
    private UserOperationAction friendOperationAction;
    private boolean loadedDialogInfo;
    private BroadcastReceiver updatingDialogDetailsBroadcastReceiver;
    private List<User> occupantsList;
    private int countOnlineFriends;
    private DataManager databaseManager;

    public static void start(Activity context, String dialogId) {
        Intent intent = new Intent(context, GroupDialogDetailsActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        context.startActivityForResult(intent, UPDATE_DIALOG_REQUEST_CODE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_dialog_details);

        initActionBar();

        dialogId = (String) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG_ID);
        databaseManager = DataManager.getInstance();
        imageUtils = new ImageUtils(this);
        friendOperationAction = new UserOperationAction();
        loadedDialogInfo = false;
        currentNotificationTypeList = new ArrayList<>();

        initUI();

        initDialogs();
        initUIWithData();

        addActions();

        initLocalBroadcastManagers();
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initUIWithData() {
        groupNameEditText.setText(groupDialog.getName());

        if (friendListHelper != null) {
            countOnlineFriends = groupDialog.getOnlineOccupantsCount(friendListHelper);
        }

        updateCountOnlineFriends();

        participantsTextView.setText(getString(R.string.gdd_participants, groupDialog.getOccupantsCount()));

        if (!isNeedUpdateAvatar) {
            loadAvatar(groupDialog.getPhotoUrl());
        }

        updateOldGroupData();
    }

    private void initDialogs() {
        currentDialog = databaseManager.getDialogDataManager().getByDialogId(dialogId);
        groupDialog = new GroupDialog(ChatUtils.createQBDialogFromLocalDialog(currentDialog));
    }

    private void initLocalBroadcastManagers() {
        updatingDialogDetailsBroadcastReceiver = new UpdatingDialogDetailsBroadcastReceiver();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(updatingDialogDetailsBroadcastReceiver, new IntentFilter(
                        QBServiceConsts.UPDATE_DIALOG_DETAILS));
    }

    private void unregisterLocalBroadcastManagers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updatingDialogDetailsBroadcastReceiver);
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);

        if (friendListHelper != null && groupDialogOccupantsAdapter != null) {
            groupDialogOccupantsAdapter.setFriendListHelper(friendListHelper);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroupDialog();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterLocalBroadcastManagers();
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
                if (!loadedDialogInfo) {
                    DialogUtils.showLong(GroupDialogDetailsActivity.this, getString(R.string.gdd_group_info_is_loading));
                } else {
                    startAddFriendsActivity();
                }
                return true;
            case R.id.action_leave:
                showLeaveGroupDialog();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, data);
        } else if (requestCode == AddFriendsToGroupActivity.RESULT_ADDED_FRIENDS) {
            if (data != null) {
                handleAddedFriends(data);
            }
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

    @Override
    public void onUriScaledBitmapReceived(Uri originalUri) {
        hideProgress();
        canPerformLogout.set(false);
        startCropActivity(originalUri);
    }

    private void loadGroupDialog() {
        currentDialog = databaseManager.getDialogDataManager().getByDialogId(dialogId);
        QBLoadGroupDialogCommand.start(this, ChatUtils.createQBDialogFromLocalDialog(currentDialog));
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

    private void updateCountOnlineFriends() {
        onlineParticipantsTextView.setText(getString(R.string.gdd_online_participants, countOnlineFriends,
                        groupDialog.getOccupantsCount()));
    }

    private void loadAvatar(String photoUrl) {
        ImageLoader.getInstance().displayImage(photoUrl, avatarImageView,
                ImageLoaderUtils.UIL_GROUP_AVATAR_DISPLAY_OPTIONS);
    }

    private void initListView() {
        groupDialogOccupantsAdapter = getFriendsAdapter();
        groupDialogOccupantsAdapter.setFriendListHelper(friendListHelper);
        friendsListView.setAdapter(groupDialogOccupantsAdapter);
        friendsListView.setOnItemClickListener(this);
    }

    private void addActions() {
        UpdateGroupFailAction updateGroupFailAction = new UpdateGroupFailAction();

        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_SUCCESS_ACTION, new LoadGroupDialogSuccessAction());
        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_SUCCESS_ACTION, new LeaveGroupDialogSuccessAction());
        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.UPDATE_GROUP_DIALOG_SUCCESS_ACTION, new UpdateGroupDialogSuccessAction());
        addAction(QBServiceConsts.UPDATE_GROUP_DIALOG_FAIL_ACTION, updateGroupFailAction);

        addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, new AddFriendFailAction());

        updateBroadcastActionList();
    }

    protected GroupDialogOccupantsAdapter getFriendsAdapter() {
        occupantsList = groupDialog.getOccupantList();
        return new GroupDialogOccupantsAdapter(this, friendOperationAction, occupantsList);
    }

    private void showLeaveGroupDialog() {
        TwoButtonsDialogFragment.show(getSupportFragmentManager(),
                R.string.dlg_leave_group,
                R.string.dlg_confirm,
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        leaveGroup();
                    }
                });
    }

    private void leaveGroup() {
        showProgress();
        currentNotificationTypeList.add(DialogNotification.Type.LEAVE_DIALOG);
        sendNotificationToGroup();
        Dialog dialog = databaseManager.getDialogDataManager().getByRoomJid(groupDialog.getRoomJid());
        QBLeaveGroupDialogCommand.start(GroupDialogDetailsActivity.this, dialog);
    }

    private void initTextChangedListeners() {
        groupNameEditText.addTextChangedListener(new GroupNameTextWatcherListener());
    }

    private void handleAddedFriends(Intent data) {
        addedFriendIdsList = (ArrayList<Integer>) data.getSerializableExtra(QBServiceConsts.EXTRA_FRIENDS);
        if (addedFriendIdsList != null) {
            try {
                groupChatHelper.sendNotificationToPrivateChatAboutCreatingGroupChat(
                        ChatUtils.createQBDialogFromLocalDialog(currentDialog), addedFriendIdsList);
            } catch (Exception e) {
                ErrorUtils.logError(e);
            }
            currentNotificationTypeList.add(DialogNotification.Type.ADDED_DIALOG);
            sendNotificationToGroup();
        }
    }

    private void startAddFriendsActivity() {
        List<Integer> friendsIdsList = UserFriendUtils.getFriendIds(groupDialog.getOccupantList());
        int countUnselectedFriendsInChat = DataManager.getInstance().getFriendDataManager().getFriendsByIds(friendsIdsList).size();
        if (countUnselectedFriendsInChat != ConstsCore.ZERO_INT_VALUE) {
            AddFriendsToGroupActivity.start(this, groupDialog);
        } else {
            DialogUtils.showLong(this, getString(R.string.gdd_all_friends_is_in_group));
        }
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
        actionMode = startSupportActionMode(new ActionModeCallback());
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

        if (!currentDialog.getTitle().equals(groupNameCurrent)) {
            currentDialog.setTitle(groupNameCurrent);

            currentNotificationTypeList.add(DialogNotification.Type.NAME_DIALOG);
        }

        if (isNeedUpdateAvatar) {
            new ReceiveFileFromBitmapTask(this).execute(imageUtils, avatarBitmapCurrent, true);

            currentNotificationTypeList.add(DialogNotification.Type.PHOTO_DIALOG);
        } else {
            updateGroupDialog(null);
        }

        showProgress();
    }

    private void sendNotificationToGroup() {
        for (DialogNotification.Type messagesNotificationType : currentNotificationTypeList) {
            try {
                groupChatHelper.sendNotificationToFriends(ChatUtils.createQBDialogFromLocalDialog(
                        currentDialog), messagesNotificationType, addedFriendIdsList);
            } catch (QBResponseException e) {
                ErrorUtils.logError(e);
                hideProgress();
            }
        }
        currentNotificationTypeList.clear();
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
        User selectedFriend = groupDialogOccupantsAdapter.getItem(position);
        if (selectedFriend != null) {
            startFriendProfile(selectedFriend);
        }
    }

    private void startFriendProfile(User selectedFriend) {
        QBUser currentUser = AppSession.getSession().getUser();
        if (currentUser.getId() == selectedFriend.getUserId()) {
            MyProfileActivity.start(GroupDialogDetailsActivity.this);
        } else {
            UserProfileActivity.start(GroupDialogDetailsActivity.this, selectedFriend.getUserId());
        }
    }

    private void resetGroupData() {
        groupNameEditText.setText(groupNameOld);
        isNeedUpdateAvatar = false;
        loadAvatar(photoUrlOld);
    }

    private void updateGroupDialog(File imageFile) {
        QBUpdateGroupDialogCommand.start(this, ChatUtils.createQBDialogFromLocalDialog(currentDialog),
                imageFile);
    }

    @Override
    public void onCachedImageFileReceived(File imageFile) {
        updateGroupDialog(imageFile);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {

    }

    private void addToFriendList(final int userId) {
        showProgress();
        QBAddFriendCommand.start(this, userId);
    }

    public void updateUserStatus(int userId, boolean status) {
        User user = findUserById(userId);
        if (user != null) {
            groupDialogOccupantsAdapter.notifyDataSetChanged();

            if (status) {
                ++countOnlineFriends;
            } else {
                --countOnlineFriends;
            }

            updateCountOnlineFriends();
        }
    }

    public User findUserById(int userId) {
        for (User user : occupantsList) {
            if (userId == user.getUserId()) {
                return user;
            }
        }
        return null;
    }

    private class UserOperationAction implements UserOperationListener {

        @Override
        public void onAddUserClicked(int userId) {
            addToFriendList(userId);
        }
    }

    private class AddFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            groupDialogOccupantsAdapter.notifyDataSetChanged();
            hideProgress();
        }
    }

    private class AddFriendFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            DialogUtils.showLong(GroupDialogDetailsActivity.this, exception.getMessage());
            hideProgress();
        }
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
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.done_menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_done:
                    updateUserData();
                    actionMode.finish();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            resetGroupData();
            actionMode = null;
        }
    }

    private class LoadGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            groupDialog = (GroupDialog) bundle.getSerializable(QBServiceConsts.EXTRA_GROUP_DIALOG);

            initUIWithData();
            initTextChangedListeners();
            initListView();
            loadedDialogInfo = true;
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

    private class UpdateGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            currentDialog = (Dialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            groupDialog = new GroupDialog(ChatUtils.createQBDialogFromLocalDialog(currentDialog));
            groupDialog.setOccupantList((ArrayList<User>) occupantsList);

            updateOldGroupData();

            sendNotificationToGroup();
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

    private class UpdatingDialogDetailsBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(QBServiceConsts.UPDATE_DIALOG_DETAILS)) {
                int userId = intent.getIntExtra(QBServiceConsts.EXTRA_USER_ID, ConstsCore.ZERO_INT_VALUE);
                if (occupantsList != null && userId != ConstsCore.ZERO_INT_VALUE) {
                    boolean online = intent.getBooleanExtra(QBServiceConsts.EXTRA_STATUS, false);
                    updateUserStatus(userId, online);
                }
            }
        }
    }
}