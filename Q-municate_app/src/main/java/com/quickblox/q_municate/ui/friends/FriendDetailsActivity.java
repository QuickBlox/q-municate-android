package com.quickblox.q_municate.ui.friends;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.qb.commands.QBDeleteDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBRemoveFriendCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.chats.PrivateDialogActivity;
import com.quickblox.q_municate.ui.dialogs.AlertDialog;
import com.quickblox.q_municate.ui.mediacall.CallActivity;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;

public class FriendDetailsActivity extends BaseLogeableActivity {

    private RoundedImageView avatarImageView;
    private TextView nameTextView;
    private TextView statusTextView;
    private ImageView onlineImageView;
    private TextView onlineStatusTextView;
    private TextView phoneTextView;
    private View phoneView;

    private User friend;
    private Cursor friendCursor;
    private ContentObserver statusContentObserver;

    public static void start(Context context, int friendId) {
        Intent intent = new Intent(context, FriendDetailsActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND_ID, friendId);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);
        canPerformLogout.set(true);
        int friendId = getIntent().getExtras().getInt(QBServiceConsts.EXTRA_FRIEND_ID);
        friendCursor = UsersDatabaseManager.getFriendCursorById(this, friendId);
        friend = UsersDatabaseManager.getUserById(this, friendId);
        initUI();
        registerStatusChangingObserver();
        initUIWithFriendsData();
        initBroadcastActionList();
    }

    private void initUI() {
        avatarImageView = _findViewById(R.id.avatar_imageview);
        nameTextView = _findViewById(R.id.name_textview);
        statusTextView = _findViewById(R.id.status_textview);
        onlineImageView = _findViewById(R.id.online_imageview);
        onlineStatusTextView = _findViewById(R.id.online_status_textview);
        phoneTextView = _findViewById(R.id.phone_textview);
        phoneView = _findViewById(R.id.phone_relativelayout);
    }

    private void registerStatusChangingObserver() {
        statusContentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                if (FriendDetailsActivity.this.friend !=null) {
                    friend = UsersDatabaseManager.getUserById(FriendDetailsActivity.this,
                            FriendDetailsActivity.this.friend.getUserId());
                    setOnlineStatus(friend);
                }
            }

            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }
        };
        friendCursor.registerContentObserver(statusContentObserver);
    }

    private void unregisterStatusChangingObserver() {
        friendCursor.unregisterContentObserver(statusContentObserver);
    }

    private void initBroadcastActionList() {
        addAction(QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION, new RemoveFriendSuccessAction());
        addAction(QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.GET_FILE_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.GET_FILE_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION, new CreateChatSuccessAction());
        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION, failAction);
    }

    private void initUIWithFriendsData() {
        loadAvatar();
        setName();
        setOnlineStatus(friend);
        setPhone();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterStatusChangingObserver();
    }

    private void setName() {
        nameTextView.setText(friend.getFullName());
    }

    private void setPhone() {
        if (friend.getPhone() != null) {
            phoneView.setVisibility(View.VISIBLE);
        } else {
            phoneView.setVisibility(View.GONE);
        }
        phoneTextView.setText(friend.getPhone());
    }

    private void setOnlineStatus(User user) {
        if (user != null) {
            if (user.isOnline()) {
                onlineImageView.setVisibility(View.VISIBLE);
            } else {
                onlineImageView.setVisibility(View.GONE);
            }
            statusTextView.setText(user.getStatus());
            onlineStatusTextView.setText(user.getOnlineStatus(this));
        }
    }

    private void loadAvatar() {
        String url = friend.getAvatarUrl();
        ImageLoader.getInstance().displayImage(url, avatarImageView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friend_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_delete:
                showRemoveUserDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showRemoveUserDialog() {
        AlertDialog alertDialog = AlertDialog.newInstance(getResources().getString(
                R.string.frd_dlg_remove_friend, friend.getFullName()));
        alertDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBRemoveFriendCommand.start(FriendDetailsActivity.this, friend.getUserId());
            }
        });
        alertDialog.setNegativeButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show(getFragmentManager(), null);
    }

    public void videoCallClickListener(View view) {
        callToUser(friend, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.VIDEO);
    }

    private void callToUser(User friend, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM callType) {
        if (friend.getUserId() != AppSession.getSession().getUser().getId()) {
            if (checkFriendStatus(friend.getUserId())) {
                CallActivity.start(FriendDetailsActivity.this, friend, callType);
            }
        }
    }

    public void voiceCallClickListener(View view) {
        callToUser(friend, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.AUDIO);
    }

    private boolean checkFriendStatus(int userId) {
        boolean isFriend = UsersDatabaseManager.isFriendInBase(this, userId);
        if (isFriend) {
            return true;
        } else {
            DialogUtils.showLong(this, getResources().getString(R.string.dlg_user_is_not_friend));
            return false;
        }
    }

    public void chatClickListener(View view) {
        if (checkFriendStatus(friend.getUserId())) {
            QBDialog existingPrivateDialog = ChatUtils.getExistPrivateDialog(this, friend.getUserId());
            if (existingPrivateDialog != null) {
                PrivateDialogActivity.start(FriendDetailsActivity.this, friend, existingPrivateDialog);
            } else {
                showProgress();
                QBCreatePrivateChatCommand.start(this, friend);
            }
        }
    }

    private void deleteDialog() {
        String dialogId = ChatDatabaseManager.getPrivateDialogIdByOpponentId(this, friend.getUserId());
        QBDeleteDialogCommand.start(this, dialogId, QBDialogType.PRIVATE);
    }

    private class CreateChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            hideProgress();
            QBDialog dialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            if (dialog != null) {
                PrivateDialogActivity.start(FriendDetailsActivity.this, friend, dialog);
                finish();
            } else {
                ErrorUtils.showError(FriendDetailsActivity.this, getString(R.string.dlg_fail_create_chat));
            }
        }
    }

    private class RemoveFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            deleteDialog();
            DialogUtils.showLong(FriendDetailsActivity.this, getString(R.string.dlg_friend_removed, friend.getFullName()));
            finish();
        }
    }
}