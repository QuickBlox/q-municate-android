package com.quickblox.q_municate.ui.activities.chats;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.chats.PrivateDialogMessagesAdapter;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.listeners.FriendOperationListener;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.friend.QBAcceptFriendCommand;
import com.quickblox.q_municate_core.qb.commands.friend.QBRejectFriendCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.FriendDataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.User;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.Observable;
import java.util.Observer;

public class PrivateDialogActivity extends BaseDialogActivity {

    private FriendOperationAction friendOperationAction;
    private FriendObserver friendObserver;
    private int operationItemPosition;

    public static void start(Context context, User opponent, Dialog dialog) {
        Intent intent = new Intent(context, PrivateDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENT, opponent);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initFields();

        if (dialog == null) {
            finish();
        }

        if (isNetworkAvailable()) {
            deleteTempMessages();
        }

        addObservers();

        fillActionBar();
        initMessagesRecyclerView();
    }

    private void initFields() {
        chatHelperIdentifier = QBService.PRIVATE_CHAT_HELPER;
        friendOperationAction = new FriendOperationAction();
        friendObserver = new FriendObserver();
        opponentUser = (User) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_OPPONENT);
        dialog = (Dialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        combinationMessagesList = createCombinationMessagesList();
    }

    private void addObservers() {
        dataManager.getFriendDataManager().addObserver(friendObserver);
    }

    private void deleteObservers() {
        dataManager.getFriendDataManager().deleteObserver(friendObserver);
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
    protected void onResume() {
        super.onResume();

        if (isNetworkAvailable()) {
            startLoadDialogMessages();
        }

        checkMessageSendingPossibility();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        deleteObservers();
    }

    @Override
    protected void updateActionBar() {
    }

    @Override
    protected void onConnectServiceLocally(QBService service) {
        onConnectServiceLocally();
        setOnlineStatus(opponentUser);
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        try {
            privateChatHelper.sendPrivateMessageWithAttachImage(file, opponentUser.getUserId());
        } catch (QBResponseException exc) {
            ErrorUtils.showError(this, exc);
        }
    }

    @Override
    protected Bundle generateBundleToInitDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, opponentUser.getUserId());
        return bundle;
    }

    @Override
    protected void initMessagesRecyclerView() {
        super.initMessagesRecyclerView();
        messagesAdapter = new PrivateDialogMessagesAdapter(this, friendOperationAction, combinationMessagesList, this, dialog);
        messagesRecyclerView.addItemDecoration(
                new StickyRecyclerHeadersDecoration((StickyRecyclerHeadersAdapter) messagesAdapter));
        findLastFriendsRequest();

        messagesRecyclerView.setAdapter(messagesAdapter);
        scrollMessagesToBottom();
    }

    @Override
    protected void updateMessagesList() {
        int oldMessagesCount = messagesAdapter.getAllItems().size();

        this.combinationMessagesList = createCombinationMessagesList();
        messagesAdapter.setList(combinationMessagesList);
        findLastFriendsRequest();

        checkForScrolling(oldMessagesCount);
    }

    private void findLastFriendsRequest() {
        ((PrivateDialogMessagesAdapter) messagesAdapter).findLastFriendsRequestMessagesPosition();
        messagesAdapter.notifyDataSetChanged();
    }

    private void setOnlineStatus(User friend) {
        if (friend != null) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null && friendListHelper != null) {
                actionBar.setSubtitle(
                        OnlineStatusUtils.getOnlineStatus(friendListHelper.isUserOnline(friend.getUserId())));
            }
        }
    }

    private void fillActionBar() {
        setActionBarTitle(opponentUser.getFullName());

        setOnlineStatus(opponentUser);

        if (!TextUtils.isEmpty(opponentUser.getAvatar())) {
            loadActionBarLogo(opponentUser.getAvatar());
        } else {
            setDefaultActionBarLogo(R.drawable.placeholder_user);
        }
    }

    @Override
    public void notifyChangedUserStatus(int userId, boolean online) {
        super.notifyChangedUserStatus(userId, online);

        if (opponentUser != null && opponentUser.getUserId() == userId) {
            setOnlineStatus(opponentUser);
        }
    }

    public void sendMessage(View view) {
        sendMessage(true);
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
                opponentUser.getUserId()) != null;
        if (!isFriend && item.getItemId() != android.R.id.home) {
            ToastUtils.longToast(R.string.dialog_user_is_not_friend);
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_audio_call:
                callToUser(opponentUser, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.AUDIO);
                break;
            case R.id.action_video_call:
                callToUser(opponentUser, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.VIDEO);
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void callToUser(User friend, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM callType) {
        ErrorUtils.showError(this, getString(R.string.coming_soon));
//        if (friend.getUserId() != AppSession.getSession().getUser().getId()) {
//            CallActivity.start(PrivateDialogActivity.this, friend, callType);
//        }
    }

    private void acceptUser(final int userId) {
        showProgress();
        QBAcceptFriendCommand.start(this, userId);
    }

    private void rejectUser(final int userId) {
        showRejectUserDialog(userId);
    }

    private void checkMessageSendingPossibility() {
        boolean enable = dataManager.getFriendDataManager().existsByUserId(opponentUser.getUserId()) && isNetworkAvailable();
        super.checkMessageSendingPossibility(enable);
    }

    private void showRejectUserDialog(final int userId) {
        User user = DataManager.getInstance().getUserDataManager().get(userId);
        if (user == null) {
            return;
        }

        TwoButtonsDialogFragment.show(getSupportFragmentManager(),
                getString(R.string.frl_dlg_reject_friend, user.getFullName()),
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        showProgress();
                        QBRejectFriendCommand.start(PrivateDialogActivity.this, userId);
                    }
        });
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
            ((PrivateDialogMessagesAdapter) messagesAdapter).clearLastRequestMessagePosition();
            messagesAdapter.notifyItemChanged(operationItemPosition);
            startLoadDialogMessages();
            hideProgress();
        }
    }

    private class RejectFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            ((PrivateDialogMessagesAdapter) messagesAdapter).clearLastRequestMessagePosition();
            messagesAdapter.notifyItemChanged(operationItemPosition);
            startLoadDialogMessages();
            hideProgress();
        }
    }

    private class FriendObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(FriendDataManager.OBSERVE_KEY)) {
                checkMessageSendingPossibility();
            }
        }
    }
}