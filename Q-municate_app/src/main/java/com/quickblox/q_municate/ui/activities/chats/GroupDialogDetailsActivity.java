package com.quickblox.q_municate.ui.activities.chats;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.adapters.chats.GroupDialogOccupantsAdapter;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.ui.activities.profile.UserProfileActivity;
import com.quickblox.q_municate.utils.listeners.UserOperationListener;
import com.quickblox.q_municate.ui.activities.profile.MyProfileActivity;
import com.quickblox.q_municate.utils.simple.SimpleActionModeCallback;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.tasks.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate.tasks.ReceiveUriScaledBitmapTask;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBLeaveGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBUpdateGroupDialogCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnTextChanged;

public class GroupDialogDetailsActivity extends BaseLogeableActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener, AdapterView.OnItemClickListener, ReceiveUriScaledBitmapTask.ReceiveUriScaledBitmapListener {

    public static final int UPDATE_DIALOG_REQUEST_CODE = 100;
    public static final int RESULT_LEAVE_GROUP = 2;

    @Bind(R.id.name_textview)
    EditText groupNameEditText;

    @Bind(R.id.occupants_textview)
    TextView occupantsTextView;

    @Bind(R.id.occupants_listview)
    ListView occupantsListView;

    @Bind(R.id.online_occupants_textview)
    TextView onlineOccupantsTextView;

    @Bind(R.id.avatar_imageview)
    RoundedImageView avatarImageView;

    private Object actionMode;
    private boolean isNeedUpdateAvatar;
    private Uri outputUri;
    private Bitmap avatarBitmapCurrent;
    private QBDialog qbDialog;
    private String groupNameCurrent;
    private String photoUrlOld;
    private String groupNameOld;
    private ImageUtils imageUtils;
    private GroupDialogOccupantsAdapter groupDialogOccupantsAdapter;
    private List<DialogNotification.Type> currentNotificationTypeList;
    private ArrayList<Integer> addedFriendIdsList;
    private UserOperationAction friendOperationAction;
    private BroadcastReceiver updatingDialogDetailsBroadcastReceiver;
    private List<User> occupantsList;
    private int countOnlineFriends;
    private DataManager dataManager;

    public static void start(Activity context, String dialogId) {
        Intent intent = new Intent(context, GroupDialogDetailsActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        context.startActivityForResult(intent, UPDATE_DIALOG_REQUEST_CODE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_group_dialog_details);

        activateButterKnife();

        initActionBar();
        initFields();
        initListView();

        addActions();
        registerBroadcastManagers();
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initFields() {
        dataManager = DataManager.getInstance();
        String dialogId = (String) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG_ID);
        qbDialog = ChatUtils.createQBDialogFromLocalDialog(dataManager,
                dataManager.getDialogDataManager().getByDialogId(dialogId));
        imageUtils = new ImageUtils(this);
        friendOperationAction = new UserOperationAction();
        currentNotificationTypeList = new ArrayList<>();
        updatingDialogDetailsBroadcastReceiver = new UpdatingDialogDetailsBroadcastReceiver();
        occupantsList = dataManager.getUserDataManager().getAllByIds(qbDialog.getOccupants());
    }

    private void fillUIWithData() {
        groupNameEditText.setText(qbDialog.getName());

        updateCountOnlineFriends();

        occupantsTextView.setText(getString(R.string.gdd_participants, qbDialog.getOccupants().size()));

        if (!isNeedUpdateAvatar) {
            loadAvatar(qbDialog.getPhoto());
        }

        updateOldGroupData();
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);

        if (friendListHelper != null && groupDialogOccupantsAdapter != null) {
            groupDialogOccupantsAdapter.setFriendListHelper(friendListHelper);
            updateCountOnlineFriends();
        }
    }

    @OnTextChanged(R.id.name_textview)
    void onGroupNameTextChanged(CharSequence s) {
        if (groupNameOld != null) {
            if (!s.toString().equals(groupNameOld)) {
                startAction();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillUIWithData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBroadcastManagers();
        removeActions();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            groupNameEditText.setText(qbDialog.getName());
            ((ActionMode) actionMode).finish();
            return true;
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
            case R.id.action_add:
                startAddFriendsActivity();
                break;
            case R.id.action_leave:
                showLeaveGroupDialog();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
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

    private void registerBroadcastManagers() {
        localBroadcastManager.registerReceiver(updatingDialogDetailsBroadcastReceiver,
                new IntentFilter(QBServiceConsts.UPDATE_DIALOG_DETAILS));
    }

    private void unregisterBroadcastManagers() {
        localBroadcastManager.unregisterReceiver(updatingDialogDetailsBroadcastReceiver);
    }

    private void addActions() {
        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_SUCCESS_ACTION, new LeaveGroupDialogSuccessAction());
        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.UPDATE_GROUP_DIALOG_SUCCESS_ACTION, new UpdateGroupDialogSuccessAction());
        addAction(QBServiceConsts.UPDATE_GROUP_DIALOG_FAIL_ACTION, new UpdateGroupFailAction());

        addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, new AddFriendFailAction());

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.LEAVE_GROUP_DIALOG_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LEAVE_GROUP_DIALOG_FAIL_ACTION);

        removeAction(QBServiceConsts.UPDATE_GROUP_DIALOG_SUCCESS_ACTION);
        removeAction(QBServiceConsts.UPDATE_GROUP_DIALOG_FAIL_ACTION);

        removeAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION);
        removeAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION);

        updateBroadcastActionList();
    }

    public void changeAvatarOnClick(View view) {
        canPerformLogout.set(false);
        imageUtils.getImage();
    }

    private void updateCountOnlineFriends() {
        if (friendListHelper != null) {
            countOnlineFriends = ChatUtils.getOnlineDialogOccupantsCount(friendListHelper, qbDialog.getOccupants());
        }
        onlineOccupantsTextView.setText(getString(R.string.gdd_online_participants, countOnlineFriends,
                qbDialog.getOccupants().size()));
    }

    private void loadAvatar(String photoUrl) {
        ImageLoader.getInstance().displayImage(photoUrl, avatarImageView,
                ImageLoaderUtils.UIL_GROUP_AVATAR_DISPLAY_OPTIONS);
    }

    private void initListView() {
        groupDialogOccupantsAdapter = new GroupDialogOccupantsAdapter(this, friendOperationAction, occupantsList);
        groupDialogOccupantsAdapter.setFriendListHelper(friendListHelper);
        occupantsListView.setAdapter(groupDialogOccupantsAdapter);
        occupantsListView.setOnItemClickListener(this);
    }

    private void updateOccupantsList() {
        groupDialogOccupantsAdapter.setNewData(occupantsList);
    }

    private void showLeaveGroupDialog() {
        TwoButtonsDialogFragment.show(getSupportFragmentManager(), R.string.dlg_leave_group,
                R.string.dlg_confirm, new MaterialDialog.ButtonCallback() {
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
        QBLeaveGroupDialogCommand.start(GroupDialogDetailsActivity.this,
                ChatUtils.createLocalDialog(qbDialog));
    }

    private void handleAddedFriends(Intent data) {
        addedFriendIdsList = (ArrayList<Integer>) data.getSerializableExtra(QBServiceConsts.EXTRA_FRIENDS);
        if (addedFriendIdsList != null) {

            updateCurrentData();
            updateOccupantsList();

            try {
                groupChatHelper.sendNotificationToPrivateChatAboutCreatingGroupChat(qbDialog, addedFriendIdsList);
            } catch (Exception e) {
                ErrorUtils.logError(e);
            }
            currentNotificationTypeList.add(DialogNotification.Type.ADDED_DIALOG);
            sendNotificationToGroup();
        }
    }

    private void startAddFriendsActivity() {
        int countUnselectedFriendsInChat = dataManager.getFriendDataManager().getAllByIds(
                qbDialog.getOccupants()).size();
        if (countUnselectedFriendsInChat != ConstsCore.ZERO_INT_VALUE) {
            AddFriendsToGroupActivity.start(this, qbDialog);
        } else {
            ToastUtils.longToast(R.string.gdd_all_friends_is_in_group);
        }
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            avatarBitmapCurrent = imageUtils.getBitmap(outputUri);
            avatarImageView.setImageBitmap(avatarBitmapCurrent);
            startAction();
        } else if (resultCode == Crop.RESULT_ERROR) {
            ToastUtils.longToast(Crop.getError(result).getMessage());
        }
    }

    private void startCropActivity(Uri originalUri) {
        outputUri = Uri.fromFile(new File(getCacheDir(), Crop.class.getName()));
        Crop.of(originalUri, outputUri).asSquare().start(this);
    }

    private void startAction() {
        if (actionMode != null) {
            return;
        }
        actionMode = startSupportActionMode(new ActionModeCallback());
    }

    private void updateCurrentData() {
        qbDialog = ChatUtils.createQBDialogFromLocalDialog(dataManager,
                dataManager.getDialogDataManager().getByDialogId(qbDialog.getDialogId()));
        occupantsList = dataManager.getUserDataManager().getAllByIds(qbDialog.getOccupants());
        groupNameCurrent = groupNameEditText.getText().toString();
    }

    private void checkForSaving() {
        updateCurrentData();
        if (isGroupDataChanged()) {
            saveChanges();
        }
    }

    private boolean isGroupDataChanged() {
        return !groupNameCurrent.equals(groupNameOld) || isNeedUpdateAvatar;
    }

    private void saveChanges() {
        if (!isUserDataCorrect()) {
            ToastUtils.longToast(R.string.gdd_name_not_entered);
            return;
        }

        if (!qbDialog.getName().equals(groupNameCurrent)) {
            qbDialog.setName(groupNameCurrent);

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
                groupChatHelper.sendNotificationToFriends(qbDialog, messagesNotificationType, addedFriendIdsList);
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
        groupNameOld = qbDialog.getName();
        photoUrlOld = qbDialog.getPhoto();
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
        QBUpdateGroupDialogCommand.start(this, qbDialog, imageFile);
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
            if (exception != null) {
                ToastUtils.longToast(exception.getMessage());
            }

            hideProgress();
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
                    checkForSaving();
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
            qbDialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);

            updateCurrentData();
            updateOldGroupData();
            fillUIWithData();

            sendNotificationToGroup();
            hideProgress();
        }
    }

    private class UpdateGroupFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            if (exception != null) {
                ToastUtils.longToast(exception.getMessage());
            }

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