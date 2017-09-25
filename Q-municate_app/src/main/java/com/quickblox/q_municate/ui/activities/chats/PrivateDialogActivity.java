package com.quickblox.q_municate.ui.activities.chats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.call.CallActivity;
import com.quickblox.q_municate.ui.activities.profile.UserProfileActivity;
import com.quickblox.q_municate.ui.adapters.chats.PrivateChatMessageAdapter;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.listeners.FriendOperationListener;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.friend.QBAcceptFriendCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBRejectFriendCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.FriendDataManager;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.OnClick;

public class PrivateDialogActivity extends BaseDialogActivity {

    private FriendOperationAction friendOperationAction;
    private QMUser opponentUser;
    private FriendObserver friendObserver;
    private BroadcastReceiver typingMessageBroadcastReceiver;
    private int operationItemPosition;
    private final String TAG = PrivateDialogActivity.class.getSimpleName();

    public static void start(Context context, QMUser opponent, QBChatDialog chatDialog) {
        Intent intent = getIntentWithExtra(context, opponent, chatDialog);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        context.startActivity(intent);
    }

    public static void startWithClearTop(Context context, QMUser opponent, QBChatDialog chatDialog) {
        Intent intent = getIntentWithExtra(context, opponent, chatDialog);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void startForResult(Fragment fragment, QMUser opponent, QBChatDialog chatDialog,
                                      int requestCode) {
        Intent intent = getIntentWithExtra(fragment.getContext(), opponent, chatDialog);
        fragment.startActivityForResult(intent, requestCode);
    }

    private static Intent getIntentWithExtra(Context context, QMUser opponent, QBChatDialog chatDialog) {
        Intent intent = new Intent(context, PrivateDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENT, opponent);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, chatDialog);
        return intent;
    }

    @Override
    protected void addActions() {
        super.addActions();

        addAction(QBServiceConsts.ACCEPT_FRIEND_SUCCESS_ACTION, new AcceptFriendSuccessAction());
        addAction(QBServiceConsts.ACCEPT_FRIEND_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.REJECT_FRIEND_SUCCESS_ACTION, new RejectFriendSuccessAction());
        addAction(QBServiceConsts.REJECT_FRIEND_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    @Override
    protected void updateActionBar() {
        setOnlineStatus(opponentUser);

        checkActionBarLogo(opponentUser.getAvatar(), R.drawable.placeholder_user);
    }

    @Override
    protected void onConnectServiceLocally(QBService service) {
        onConnectServiceLocally();
        setOnlineStatus(opponentUser);
    }

    @Override
    protected Bundle generateBundleToInitDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, opponentUser.getId());
        return bundle;
    }

    @Override
    protected void initChatAdapter() {
        messagesAdapter = new PrivateChatMessageAdapter(this, combinationMessagesList, friendOperationAction, currentChatDialog);
    }

    @Override
    protected void initMessagesRecyclerView() {
        super.initMessagesRecyclerView();
        messagesRecyclerView.addItemDecoration(
                new StickyRecyclerHeadersDecoration(messagesAdapter));
        findLastFriendsRequest(true);

        messagesRecyclerView.setAdapter(messagesAdapter);
        scrollMessagesToBottom(0);
    }

    @Override
    protected void updateMessagesList() {
        findLastFriendsRequest(false);
    }

    @Override
    public void notifyChangedUserStatus(int userId, boolean online) {
        super.notifyChangedUserStatus(userId, online);

        if (opponentUser != null && opponentUser.getId() == userId) {
            if (online) {
                //gets opponentUser from DB with updated field 'last_request_at'
                actualizeOpponentUserFromDb();
            }

            setOnlineStatus(opponentUser);
        }
    }

    private void actualizeOpponentUserFromDb() {
        QMUser opponentUserFromDb = QMUserService.getInstance().getUserCache().get((long) opponentUser.getId());

        if (opponentUserFromDb != null) {
            opponentUser = opponentUserFromDb;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.private_dialog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isFriend = DataManager.getInstance().getFriendDataManager().getByUserId(
                opponentUser.getId()) != null;
        if (!isFriend && item.getItemId() != android.R.id.home) {
            ToastUtils.longToast(R.string.dialog_user_is_not_friend);
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_audio_call:
                callToUser(opponentUser, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO);
                break;
            case R.id.switch_camera_toggle:
                callToUser(opponentUser, QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO);
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void checkMessageSendingPossibility() {
        boolean enable = dataManager.getFriendDataManager().existsByUserId(opponentUser.getId()) && isNetworkAvailable();
        checkMessageSendingPossibility(enable);
    }

    @OnClick(R.id.toolbar)
    void openProfile(View view) {
        UserProfileActivity.start(this, opponentUser.getId());
    }

    @Override
    protected void initFields() {
        super.initFields();
        friendOperationAction = new FriendOperationAction();
        friendObserver = new FriendObserver();
        typingMessageBroadcastReceiver = new TypingStatusBroadcastReceiver();
        opponentUser = (QMUser) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_OPPONENT);
        title = opponentUser.getFullName();
    }

    @Override
    protected void registerBroadcastReceivers() {
        super.registerBroadcastReceivers();
        localBroadcastManager.registerReceiver(typingMessageBroadcastReceiver,
                new IntentFilter(QBServiceConsts.TYPING_MESSAGE));
    }

    @Override
    protected void unregisterBroadcastReceivers() {
        super.unregisterBroadcastReceivers();
        localBroadcastManager.unregisterReceiver(typingMessageBroadcastReceiver);
    }

    @Override
    protected void addObservers() {
        super.addObservers();
        dataManager.getFriendDataManager().addObserver(friendObserver);
    }

    @Override
    protected void deleteObservers() {
        super.deleteObservers();
        dataManager.getFriendDataManager().deleteObserver(friendObserver);
    }

    private void findLastFriendsRequest(boolean needNotifyAdapter) {
        ((PrivateChatMessageAdapter) messagesAdapter).findLastFriendsRequestMessagesPosition();
        if (needNotifyAdapter) {
            messagesAdapter.notifyDataSetChanged();
        }
    }

    private void setOnlineStatus(QMUser user) {
        if (user != null) {
            if (friendListHelper != null) {
                String offlineStatus = getString(R.string.last_seen, DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastRequestAt().getTime()),
                        DateUtils.formatDateSimpleTime(user.getLastRequestAt().getTime()));
                setActionBarSubtitle(
                        OnlineStatusUtils.getOnlineStatus(this, friendListHelper.isUserOnline(user.getId()), offlineStatus));
            }
        }
    }

    public void sendMessage(View view) {
        sendMessage();
    }

    private void callToUser(QMUser user, QBRTCTypes.QBConferenceType qbConferenceType) {
        if (!isChatInitializedAndUserLoggedIn()) {
            ToastUtils.longToast(R.string.call_chat_service_is_initializing);
            return;
        }
        List<QBUser> qbUserList = new ArrayList<>(1);
        qbUserList.add(UserFriendUtils.createQbUser(user));
        CallActivity.start(PrivateDialogActivity.this, qbUserList, qbConferenceType, null);
    }

    private void acceptUser(final int userId) {
        if (isNetworkAvailable()) {
            if (!isChatInitializedAndUserLoggedIn()) {
                ToastUtils.longToast(R.string.call_chat_service_is_initializing);
                return;
            }

            showProgress();
            QBAcceptFriendCommand.start(this, userId);
        } else {
            ToastUtils.longToast(R.string.dlg_fail_connection);
            return;
        }
    }

    private void rejectUser(final int userId) {
        if (isNetworkAvailable()) {
            if (!isChatInitializedAndUserLoggedIn()) {
                ToastUtils.longToast(R.string.call_chat_service_is_initializing);
                return;
            }

            showRejectUserDialog(userId);
        } else {
            ToastUtils.longToast(R.string.dlg_fail_connection);
            return;
        }
    }

    private void showRejectUserDialog(final int userId) {
        QMUser user = QMUserService.getInstance().getUserCache().get((long) userId);
        if (user == null) {
            return;
        }

        TwoButtonsDialogFragment.show(getSupportFragmentManager(),
                getString(R.string.dialog_message_reject_friend, user.getFullName()),
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        showProgress();
                        QBRejectFriendCommand.start(PrivateDialogActivity.this, userId);
                    }
                });
    }

    private void updateCurrentChatFromDB() {
        QBChatDialog updatedDialog = null;
        if (currentChatDialog != null) {
            updatedDialog = dataManager.getQBChatDialogDataManager().getByDialogId(currentChatDialog.getDialogId());
        } else {
            finish();
        }

        if (updatedDialog == null) {
            finish();
        } else {
            currentChatDialog = updatedDialog;
            initCurrentDialog();
        }
    }

    private void showTypingStatus() {
        setActionBarSubtitle(R.string.dialog_now_typing);
    }

    private void hideTypingStatus() {
        setOnlineStatus(opponentUser);
    }

    private class FriendOperationAction implements FriendOperationListener {

        @Override
        public void onAcceptUserClicked(int position, int userId) {
            operationItemPosition = position;
            acceptUser(userId);
        }

        @Override
        public void onRejectUserClicked(int position, int userId) {
            operationItemPosition = position;
            rejectUser(userId);
        }
    }

    private class AcceptFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            ((PrivateChatMessageAdapter) messagesAdapter).clearLastRequestMessagePosition();
            messagesAdapter.notifyItemChanged(operationItemPosition);
            startLoadDialogMessages(false);
            hideProgress();
        }
    }

    private class RejectFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            ((PrivateChatMessageAdapter) messagesAdapter).clearLastRequestMessagePosition();
            messagesAdapter.notifyItemChanged(operationItemPosition);
            startLoadDialogMessages(false);
            hideProgress();
        }
    }

    private class FriendObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null) {
                String observerKey = ((Bundle) data).getString(FriendDataManager.EXTRA_OBSERVE_KEY);
                if (observerKey.equals(dataManager.getFriendDataManager().getObserverKey())) {
                    updateCurrentChatFromDB();
                    checkMessageSendingPossibility();
                }
            }
        }
    }

    private class TypingStatusBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            int userId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);
            // TODO: now it is possible only for Private chats
            if (currentChatDialog != null && opponentUser != null && userId == opponentUser.getId()) {
                if (QBDialogType.PRIVATE.equals(currentChatDialog.getType())) {
                    boolean isTyping = extras.getBoolean(QBServiceConsts.EXTRA_IS_TYPING);
                    if (isTyping) {
                        showTypingStatus();
                    } else {
                        hideTypingStatus();
                    }
                }
            }
        }
    }
}