package com.quickblox.q_municate.ui.activities.chats;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.activities.profile.MyProfileActivity;
import com.quickblox.q_municate.ui.activities.profile.UserProfileActivity;
import com.quickblox.q_municate.ui.adapters.chats.GroupDialogOccupantsAdapter;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.helpers.MediaPickHelper;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.MediaUtils;
import com.quickblox.q_municate.utils.listeners.OnMediaPickedListener;
import com.quickblox.q_municate.utils.listeners.UserOperationListener;
import com.quickblox.q_municate.utils.listeners.simple.SimpleActionModeCallback;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateGroupDialogCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBAddFriendCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.Bind;
import butterknife.OnTextChanged;

public class GroupDialogDetailsActivity extends BaseLoggableActivity implements AdapterView.OnItemClickListener, OnMediaPickedListener {

    public static final int UPDATE_DIALOG_REQUEST_CODE = 100;
    public static final int RESULT_DELETE_GROUP = 2;

    @Bind(R.id.name_textview)
    EditText groupNameEditText;

    @Bind(R.id.occupants_textview)
    TextView occupantsTextView;

    @Bind(R.id.occupants_listview)
    ListView occupantsListView;

    @Bind(R.id.online_occupants_textview)
    TextView onlineOccupantsTextView;

    @Bind(R.id.avatar_imageview)
    RoundedImageView photoImageView;

    private Object actionMode;
    private boolean isNeedUpdateImage;
    private Uri imageUri;
    private QBChatDialog qbDialog;
    private String groupNameCurrent;
    private String photoUrlOld;
    private String groupNameOld;
    private MediaPickHelper mediaPickHelper;
    private GroupDialogOccupantsAdapter groupDialogOccupantsAdapter;
    private List<DialogNotification.Type> currentNotificationTypeList;
    private ArrayList<Integer> newFriendIdsList;
    private UserOperationAction friendOperationAction;
    private BroadcastReceiver updatingDialogDetailsBroadcastReceiver;
    private List<QMUser> occupantsList;
    private int countOnlineFriends;
    private DataManager dataManager;
    private String dialogId;

    public static void start(Activity context, String dialogId) {
        Intent intent = new Intent(context, GroupDialogDetailsActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        context.startActivityForResult(intent, UPDATE_DIALOG_REQUEST_CODE);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_group_dialog_details;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();

        initListView();

        addActions();
        registerBroadcastManagers();
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
                if (isChatInitializedAndUserLoggedIn() && checkNetworkAvailableWithError()) {
                    boolean joined = chatHelper != null && chatHelper.isDialogJoined(qbDialog);
                    if (joined) {
                        showLeaveGroupDialog();
                    } else {
                        ToastUtils.longToast(R.string.dialog_details_service_is_initializing);
                    }
                } else {
                    ToastUtils.longToast(R.string.dialog_details_service_is_initializing);
                }
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
            canPerformLogout.set(true);
        } else if (requestCode == AddFriendsToGroupActivity.RESULT_ADDED_FRIENDS) {
            if (data != null) {
                handleAddedFriends(data);
            }
            canPerformLogout.set(true);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);

        if (friendListHelper != null && groupDialogOccupantsAdapter != null) {
            groupDialogOccupantsAdapter.setFriendListHelper(friendListHelper);
            updateCountOnlineFriends();
        }
    }

    @Override
    public void onChangedUserStatus(int userId, boolean online) {
        super.onChangedUserStatus(userId, online);
        groupDialogOccupantsAdapter.notifyDataSetChanged();
        updateCountOnlineFriends();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        QMUser selectedFriend = groupDialogOccupantsAdapter.getItem(position);
        if (selectedFriend != null) {
            startFriendProfile(selectedFriend);
        }
    }

    @Override
    public void onMediaPicked(int requestCode, Attachment.Type attachmentType, Object attachment) {
        if (Attachment.Type.IMAGE.equals(attachmentType)) {
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
        title = getString(R.string.dialog_details_title);
        dataManager = DataManager.getInstance();
        dialogId = (String) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG_ID);
        mediaPickHelper = new MediaPickHelper();
        friendOperationAction = new UserOperationAction();
        currentNotificationTypeList = new ArrayList<>();
        updatingDialogDetailsBroadcastReceiver = new UpdatingDialogDetailsBroadcastReceiver();
        occupantsList = new ArrayList<>();
    }

    private void fillUIWithData() {
        updateDialog();

        groupNameEditText.setText(qbDialog.getName());

        updateCountOnlineFriends();

        occupantsTextView.setText(
                getString(R.string.dialog_details_participants, qbDialog.getOccupants().size()));

        if (!isNeedUpdateImage) {
            loadAvatar(qbDialog.getPhoto());
        }

        updateOldGroupData();
    }

    private void updateDialog() {
        qbDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialogId);
        qbDialog.initForChat(QBChatService.getInstance());
        occupantsList = getUsersForGroupChat(qbDialog.getDialogId(), qbDialog.getOccupants());
        qbDialog.setOccupantsIds(ChatUtils.createOccupantsIdsFromUsersList(occupantsList));
        groupDialogOccupantsAdapter.setNewData(occupantsList);
    }

    private void registerBroadcastManagers() {
        localBroadcastManager.registerReceiver(updatingDialogDetailsBroadcastReceiver,
                new IntentFilter(QBServiceConsts.UPDATE_DIALOG_DETAILS));
    }

    private void unregisterBroadcastManagers() {
        localBroadcastManager.unregisterReceiver(updatingDialogDetailsBroadcastReceiver);
    }

    private void addActions() {
        addAction(QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION, new DeleteGroupDialogSuccessAction());
        addAction(QBServiceConsts.DELETE_DIALOG_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.UPDATE_GROUP_DIALOG_SUCCESS_ACTION, new UpdateGroupDialogSuccessAction());
        addAction(QBServiceConsts.UPDATE_GROUP_DIALOG_FAIL_ACTION, new UpdateGroupFailAction());

        addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, new AddFriendFailAction());

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION);
        removeAction(QBServiceConsts.DELETE_DIALOG_FAIL_ACTION);

        removeAction(QBServiceConsts.UPDATE_GROUP_DIALOG_SUCCESS_ACTION);
        removeAction(QBServiceConsts.UPDATE_GROUP_DIALOG_FAIL_ACTION);

        removeAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION);
        removeAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION);

        updateBroadcastActionList();
    }

    public void changeAvatarOnClick(View view) {
        mediaPickHelper.pickAnMedia(this, MediaUtils.IMAGE_REQUEST_CODE);
    }

    private void updateCountOnlineFriends() {
        if (friendListHelper != null) {
            countOnlineFriends = ChatUtils.getOnlineDialogOccupantsCount(friendListHelper, qbDialog.getOccupants());
        }
        onlineOccupantsTextView.setText(
                getString(R.string.dialog_details_online_participants, countOnlineFriends,
                        qbDialog.getOccupants().size()));
    }

    private void loadAvatar(String photoUrl) {
        ImageLoader.getInstance().displayImage(photoUrl, photoImageView,
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
                        deleteDialog(qbDialog);
                    }
                });
    }

    private void deleteDialog(QBChatDialog chatDialog) {
        if (chatDialog == null || chatDialog.getDialogId() == null) {
            return;
        }

        this.showProgress();
        QBDeleteChatCommand.start(this, chatDialog.getDialogId(), chatDialog.getType().getCode());
    }

    private void handleAddedFriends(Intent data) {
        newFriendIdsList = (ArrayList<Integer>) data.getSerializableExtra(QBServiceConsts.EXTRA_FRIENDS);
        if (newFriendIdsList != null) {

            updateCurrentData();
            updateOccupantsList();

            try {
                chatHelper.sendSystemMessageAboutCreatingGroupChat(qbDialog, newFriendIdsList);
            } catch (Exception e) {
                ErrorUtils.logError(e);
            }
            currentNotificationTypeList.add(DialogNotification.Type.ADDED_DIALOG);
            sendNotificationToGroup(false);
        }
    }

    private void startAddFriendsActivity() {
        AddFriendsToGroupActivity.start(this, qbDialog);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateImage = true;
            if (imageUri != null) {
                photoImageView.setImageURI(imageUri);
            }
            startAction();
        } else if (resultCode == Crop.RESULT_ERROR) {
            ToastUtils.longToast(Crop.getError(result).getMessage());
        }
    }

    private void startCropActivity(Uri originalUri) {
        String extensionOriginalUri = originalUri.getPath().substring(originalUri.getPath().lastIndexOf(".")).toLowerCase();
        imageUri = MediaUtils.getValidUri(new File(getCacheDir(), extensionOriginalUri), this);
        Crop.of(originalUri, imageUri).asSquare().start(this);
    }

    private void startAction() {
        if (actionMode != null) {
            return;
        }
        actionMode = startSupportActionMode(new ActionModeCallback());
    }

    private void updateCurrentData() {
        QBChatDialog qbChatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(qbDialog.getDialogId());
        occupantsList = QMUserService.getInstance().getUserCache().getUsersByIDs(qbChatDialog.getOccupants());
        groupNameCurrent = groupNameEditText.getText().toString();
    }

    private void checkForSaving() {
        updateCurrentData();
        if (isGroupDataChanged()) {
            saveChanges();
        }
    }

    private boolean isGroupDataChanged() {
        return !groupNameCurrent.equals(groupNameOld) || isNeedUpdateImage;
    }

    private void saveChanges() {
        if (!isUserDataCorrect()) {
            ToastUtils.longToast(R.string.dialog_details_name_not_entered);
            return;
        }

        if (!qbDialog.getName().equals(groupNameCurrent)) {
            qbDialog.setName(groupNameCurrent);

            currentNotificationTypeList.add(DialogNotification.Type.NAME_DIALOG);
        }

        if (isNeedUpdateImage) {
            currentNotificationTypeList.add(DialogNotification.Type.PHOTO_DIALOG);
            updateGroupDialog(MediaUtils.getCreatedFileFromUri(imageUri));
        } else {
            updateGroupDialog(null);
        }

        showProgress();
    }

    private void sendNotificationToGroup(boolean leavedFromDialog) {
        for (DialogNotification.Type messagesNotificationType : currentNotificationTypeList) {
            try {
                QBChatDialog localDialog = qbDialog;
                if (qbDialog != null) {
                    localDialog = dataManager.getQBChatDialogDataManager().getByDialogId(qbDialog.getDialogId());
                }
                chatHelper.sendGroupMessageToFriends(localDialog, messagesNotificationType,
                        newFriendIdsList, leavedFromDialog);
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

    private void startFriendProfile(QMUser selectedFriend) {
        QBUser currentUser = AppSession.getSession().getUser();
        if (currentUser.getId().intValue() == selectedFriend.getId().intValue()) {
            MyProfileActivity.start(GroupDialogDetailsActivity.this);
        } else {
            UserProfileActivity.start(GroupDialogDetailsActivity.this, selectedFriend.getId());
        }
    }

    private void resetGroupData() {
        groupNameEditText.setText(groupNameOld);
        isNeedUpdateImage = false;
        loadAvatar(photoUrlOld);
    }

    private void updateGroupDialog(File imageFile) {
        QBUpdateGroupDialogCommand.start(this, qbDialog, imageFile);
    }

    private void addToFriendList(final int userId) {
        if (isChatInitializedAndUserLoggedIn()) {
            showProgress();
            QBAddFriendCommand.start(this, userId);
        } else {
            ToastUtils.longToast(R.string.chat_service_is_initializing);
        }
    }

    public void updateUserStatus(int userId, boolean status) {
        QMUser user = findUserById(userId);
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

    public QMUser findUserById(int userId) {
        for (QMUser user : occupantsList) {
            if (userId == user.getId()) {
                return user;
            }
        }
        return null;
    }


    public List<QMUser> getUsersForGroupChat(String dialogId, List<Integer> idsList) {
        List<QMUser> usersList = new ArrayList<>();

        List<QMUser> qmUsers = QMUserService.getInstance().getUserCache().getUsersByIDs(idsList);

        List<DialogOccupant> dialogOccupants = dataManager.getDialogOccupantDataManager().getActualDialogOccupantsByDialog(dialogId);
        Set<Integer> dialogOccupantIdsSet = new HashSet<>();
        for (DialogOccupant dialogOccupant : dialogOccupants) {
            dialogOccupantIdsSet.add(dialogOccupant.getUser().getId());
        }

        for (QMUser qmUser : qmUsers) {
            if (dialogOccupantIdsSet.contains(qmUser.getId())) {
                usersList.add(qmUser);
            }
        }
        return usersList;
    }

    @Override
    protected void performLoginChatSuccessAction(Bundle bundle) {
        super.performLoginChatSuccessAction(bundle);
        if (chatHelper != null) {
            chatHelper.tryJoinRoomChat(qbDialog, null);
        }
    }

    private class UserOperationAction implements UserOperationListener {

        @Override
        public void onAddUserClicked(int userId) {
            if (checkNetworkAvailableWithError()) {
                addToFriendList(userId);
            }
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
                    if (checkNetworkAvailableWithError()) {
                        checkForSaving();
                    } else {
                        onDestroyActionMode(actionMode);
                    }
                    actionMode.finish();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }

    private class DeleteGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            setResult(RESULT_DELETE_GROUP, getIntent());
            finish();
        }
    }

    private class UpdateGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            qbDialog = (QBChatDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);

            updateCurrentData();
            updateOldGroupData();
            fillUIWithData();

            sendNotificationToGroup(false);
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