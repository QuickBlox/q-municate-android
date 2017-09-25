package com.quickblox.q_municate.ui.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.activities.call.CallActivity;
import com.quickblox.q_municate.ui.activities.chats.PrivateDialogActivity;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.chat.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBRemoveFriendCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.utils.DialogTransformUtils;
import com.quickblox.q_municate_user_cache.QMUserCacheImpl;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.OnClick;

public class UserProfileActivity extends BaseLoggableActivity {

    @Bind(R.id.avatar_imageview)
    RoundedImageView avatarImageView;

    @Bind(R.id.name_textview)
    TextView nameTextView;

    @Bind(R.id.timestamp_textview)
    TextView timestampTextView;

    @Bind(R.id.phone_view)
    View phoneView;

    @Bind(R.id.phone_textview)
    TextView phoneTextView;

    private DataManager dataManager;
    private int userId;
    private QMUser user;
    private Observer userObserver;
    private boolean removeContactAndChatHistory;

    public static void start(Context context, int friendId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND_ID, friendId);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_user_profile;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();

        initUIWithUsersData();
        addActions();
    }

    private void initFields() {
        title = getString(R.string.user_profile_title);
        dataManager = DataManager.getInstance();
        canPerformLogout.set(true);
        userId = getIntent().getExtras().getInt(QBServiceConsts.EXTRA_FRIEND_ID);
        user = QMUserService.getInstance().getUserCache().get((long)userId);
        userObserver = new UserObserver();
    }

    private void initUIWithUsersData() {
        loadAvatar();
        setName();
        setPhone();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addObservers();
        setOnlineStatus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        deleteObservers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @OnClick(R.id.send_message_button)
    void sendMessage(View view) {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getId());
        if (dialogOccupant != null && dialogOccupant.getDialog() != null) {
            QBChatDialog chatDialog = DialogTransformUtils.createQBDialogFromLocalDialog(dataManager, dialogOccupant.getDialog());
            PrivateDialogActivity.startWithClearTop(UserProfileActivity.this, user, chatDialog);
        } else {
            showProgress();
            QBCreatePrivateChatCommand.start(this, user);
        }
    }

    @OnClick(R.id.audio_call_button)
    void audioCall(View view) {
        callToUser(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO);
    }

    @OnClick(R.id.video_call_button)
    void videoCall(View view) {
        callToUser(QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
    }

    @OnClick(R.id.delete_chat_history_button)
    void deleteChatHistory(View view) {
        if (checkNetworkAvailableWithError()) {
            removeContactAndChatHistory = false;
            showRemoveChatHistoryDialog();
        }
    }

    @OnClick(R.id.remove_contact_and_chat_history_button)
    void removeContactAndChatHistory(View view) {
        if (checkNetworkAvailableWithError()) {
            removeContactAndChatHistory = true;
            showRemoveContactAndChatHistoryDialog();
        }
    }

    @Override
    public void notifyChangedUserStatus(int userId, boolean online) {
        super.notifyChangedUserStatus(userId, online);
        if (user.getId() == userId) {
            if (online) {
                user.setLastRequestAt(new Date(System.currentTimeMillis()));
            }
            setOnlineStatus(online);
        }
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        setOnlineStatus();
    }

    private void addObservers() {
        ((Observable)QMUserService.getInstance().getUserCache()).addObserver(userObserver);
    }

    private void deleteObservers() {
        ((Observable)QMUserService.getInstance().getUserCache()).deleteObserver(userObserver);
    }

    private void addActions() {
        addAction(QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION, new RemoveFriendSuccessAction());
        addAction(QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION, new RemoveChatSuccessAction());
        addAction(QBServiceConsts.DELETE_DIALOG_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION, new CreatePrivateChatSuccessAction());
        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION);
        removeAction(QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION);

        removeAction(QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION);
        removeAction(QBServiceConsts.DELETE_DIALOG_FAIL_ACTION);

        removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void setOnlineStatus() {
        if (friendListHelper != null) {
            setOnlineStatus(friendListHelper.isUserOnline(user.getId()));
        }
    }

    private void setOnlineStatus(boolean online) {
        String offlineStatus = getString(R.string.last_seen,
                DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastRequestAt().getTime()),
                DateUtils.formatDateSimpleTime(user.getLastRequestAt().getTime()));
        timestampTextView.setText(OnlineStatusUtils.getOnlineStatus(this, online, offlineStatus));
    }

    private void setName() {
        nameTextView.setText(user.getFullName());
    }

    private void setPhone() {
        if (user.getPhone() != null) {
            phoneView.setVisibility(View.VISIBLE);
        } else {
            phoneView.setVisibility(View.GONE);
        }
        phoneTextView.setText(user.getPhone());
    }

    private void loadAvatar() {
        String url = user.getAvatar();
        ImageLoader.getInstance().displayImage(url, avatarImageView,
                ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    private void showRemoveContactAndChatHistoryDialog() {
        TwoButtonsDialogFragment.show(getSupportFragmentManager(),
                getString(R.string.user_profile_remove_contact_and_chat_history, user.getFullName()),
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        showProgress();
                        if (isUserFriendOrUserRequest()) {
                            QBRemoveFriendCommand.start(UserProfileActivity.this, user.getId());
                        } else {
                            deleteChat();
                        }
                    }
                });
    }

    private void showRemoveChatHistoryDialog() {
        if (isChatExists()) {
            TwoButtonsDialogFragment.show(
                    getSupportFragmentManager(),
                    getString(R.string.user_profile_delete_chat_history, user.getFullName()),
                    new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            showProgress();
                            deleteChat();
                        }
                    });
        } else {
            ToastUtils.longToast(R.string.user_profile_chat_does_not_exists);
        }
    }

    private void deleteChat() {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getId());
        if (dialogOccupant == null){
            finish();
        } else {
            QBChatDialog chatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialogOccupant.getDialog().getDialogId());
            if (chatDialog != null) {
                QBDeleteChatCommand.start(this, chatDialog.getDialogId(), chatDialog.getType().getCode());
            }
        }
    }

    private void startPrivateChat(QBChatDialog qbDialog) {
        PrivateDialogActivity.start(UserProfileActivity.this, user, qbDialog);
    }

    private boolean isUserFriendOrUserRequest() {
        boolean isFriend = dataManager.getFriendDataManager().existsByUserId(user.getId());
        boolean isUserRequest = dataManager.getUserRequestDataManager().existsByUserId(user.getId());
        return isFriend || isUserRequest;
    }

    private boolean isChatExists() {
        return dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getId()) != null;
    }

    private void callToUser(QBRTCTypes.QBConferenceType qbConferenceType) {
        if (!isChatInitializedAndUserLoggedIn()) {
            ToastUtils.longToast(R.string.call_chat_service_is_initializing);
            return;
        }

        boolean isFriend = DataManager.getInstance().getFriendDataManager().existsByUserId(user.getId());
        if (!isFriend) {
            ToastUtils.longToast(R.string.dialog_user_is_not_friend);
            return;
        }

        List<QBUser> qbUserList = new ArrayList<>(1);
        qbUserList.add(UserFriendUtils.createQbUser(user));
        CallActivity.start(this, qbUserList, qbConferenceType, null);
    }

    private class UserObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(QMUserCacheImpl.OBSERVE_KEY)) {
                user =  QMUserService.getInstance().getUserCache().get((long)userId);
                initUIWithUsersData();
            }
        }
    }

    private class RemoveFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            deleteChat();
        }
    }

    private class RemoveChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();

            if (removeContactAndChatHistory) {
                ToastUtils.longToast(getString(R.string.user_profile_success_contacts_deleting, user.getFullName()));
                finish();
            } else {
                ToastUtils.longToast(getString(R.string.user_profile_success_chat_history_deleting, user.getFullName()));
            }
        }
    }

    private class CreatePrivateChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            hideProgress();
            QBChatDialog qbDialog = (QBChatDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            startPrivateChat(qbDialog);
        }
    }
}