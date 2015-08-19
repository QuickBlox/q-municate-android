package com.quickblox.q_municate.ui.chats.privatedialog;

import android.app.ActionBar;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.chats.base.BaseDialogActivity;
import com.quickblox.q_municate.ui.dialogs.AlertDialog;
import com.quickblox.q_municate.ui.mediacall.CallActivity;
import com.quickblox.q_municate.utils.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.QBAcceptFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBRejectFriendCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.OnlineStatusHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.User;

import java.io.File;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class PrivateDialogActivity extends BaseDialogActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener {

    private FriendOperationAction friendOperationAction;

    public static void start(Context context, User opponent, Dialog dialog) {
        Intent intent = new Intent(context, PrivateDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENT, opponent);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendOperationAction = new FriendOperationAction();
        opponentUser = (User) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_OPPONENT);
        dialog = (Dialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);

        if (dialog == null) {
            finish();
        }

        initActionBar();
        setCurrentDialog(dialog);
        initListView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Crouton.cancelAllCroutons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (messagesAdapter != null && !messagesAdapter.isEmpty()) {
            Log.d("Scroll-test", "onResume()");
            scrollListView();
        }

        startLoadDialogMessages();

        checkMessageSendingPossibility();
    }

    @Override
    protected void updateActionBar() {
    }

    @Override
    protected void onConnectServiceLocally(QBService service) {
        onConnectServiceLocally(QBService.PRIVATE_CHAT_HELPER);
        setOnlineStatus(opponentUser);
    }

    @Override
    protected void onUpdateChatDialog() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        opponentUser = null;
    }

    @Override
    protected void onFileSelected(Uri originalUri) {
        Bitmap bitmap = imageUtils.getBitmap(originalUri);
        new ReceiveFileFromBitmapTask(PrivateDialogActivity.this).execute(imageUtils, bitmap, true);
    }

    @Override
    protected void onFileSelected(Bitmap bitmap) {
        new ReceiveFileFromBitmapTask(PrivateDialogActivity.this).execute(imageUtils, bitmap, true);
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        try {
            privateChatHelper.sendPrivateMessageWithAttachImage(file, opponentUser.getUserId());
        } catch (QBResponseException exc) {
            ErrorUtils.showError(this, exc);
        }

        Log.d("Scroll-test", "onFileLoaded()");
        scrollListView();
    }

    @Override
    protected Bundle generateBundleToInitDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, opponentUser.getUserId());
        return bundle;
    }

    @Override
    protected void initListView() {
        List<CombinationMessage> combinationMessagesList = createCombinationMessagesList();
        messagesAdapter = new PrivateDialogMessagesAdapter(this, friendOperationAction,
                combinationMessagesList, this, dialog);
        findLastFriendsRequest();

        Log.d("Scroll-test", "initListView()");
        scrollListView();
        messagesListView.setAdapter((StickyListHeadersAdapter) messagesAdapter);
    }

    @Override
    protected void updateMessagesList() {
        List<CombinationMessage> combinationMessagesList = createCombinationMessagesList();
        messagesAdapter.setNewData(combinationMessagesList);
        findLastFriendsRequest();
    }

    private void findLastFriendsRequest() {
        ((PrivateDialogMessagesAdapter) messagesAdapter).findLastFriendsRequestMessagesPosition();
    }

    private void setOnlineStatus(User friend) {
        if (friend != null) {
            ActionBar actionBar = getActionBar();
            if (actionBar != null && friendListHelper != null) {
                actionBar.setSubtitle(OnlineStatusHelper.getOnlineStatus(
                        friendListHelper.isUserOnline(friend.getUserId())));
            }
        }
    }

    private void initActionBar() {
        actionBar.setTitle(opponentUser.getFullName());
        actionBar.setLogo(R.drawable.placeholder_user);

        setOnlineStatus(opponentUser);

        if (!TextUtils.isEmpty(opponentUser.getAvatar())) {
            loadLogoActionBar(opponentUser.getAvatar());
        }
    }

    @Override
    public void onChangedUserStatus(int userId, boolean online) {
        super.onChangedUserStatus(userId, online);

        if (opponentUser != null && opponentUser.getUserId() == userId) {
            setOnlineStatus(opponentUser);
        }
    }

    @Override
    public void onCachedImageFileReceived(File file) {
        startLoadAttachFile(file);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    public void sendMessageOnClick(View view) {
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
            DialogUtils.showLong(PrivateDialogActivity.this, getResources().getString(
                    R.string.dlg_user_is_not_friend));
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            case R.id.action_attach:
                attachButtonOnClick();
                return true;
            case R.id.action_audio_call:
                callToUser(opponentUser, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.AUDIO);
                return true;
            case R.id.action_video_call:
                callToUser(opponentUser, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.VIDEO);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void callToUser(User friend, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM callType) {
        if (friend.getUserId() != AppSession.getSession().getUser().getId()) {
            CallActivity.start(PrivateDialogActivity.this, friend, callType);
        }
    }

    private void checkMessageSendingPossibility() {
        boolean isFriend = DataManager.getInstance().getFriendDataManager().getByUserId(
                opponentUser.getUserId()) != null;
        messageEditText.setEnabled(isFriend);
        smilePanelImageButton.setEnabled(isFriend);
    }

    private void acceptUser(final int userId) {
        showProgress();
        QBAcceptFriendCommand.start(this, userId);
    }

    private void rejectUser(final int userId) {
        showRejectUserDialog(userId);
    }

    private void showRejectUserDialog(final int userId) {
        User user = DataManager.getInstance().getUserDataManager().get(userId);
        AlertDialog alertDialog = AlertDialog.newInstance(getResources().getString(
                R.string.frl_dlg_reject_friend, user.getFullName()));
        alertDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBRejectFriendCommand.start(PrivateDialogActivity.this, userId);
            }
        });
        alertDialog.setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show(getFragmentManager(), null);
    }

    public interface FriendOperationListener {

        void onAcceptUserClicked(int userId);

        void onRejectUserClicked(int userId);
    }

    private class FriendOperationAction implements FriendOperationListener {

        @Override
        public void onAcceptUserClicked(int userId) {
            acceptUser(userId);
        }

        @Override
        public void onRejectUserClicked(int userId) {
            rejectUser(userId);
        }
    }
}