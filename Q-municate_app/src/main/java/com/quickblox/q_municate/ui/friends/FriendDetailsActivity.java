package com.quickblox.q_municate.ui.friends;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.chats.PrivateDialogActivity;
import com.quickblox.q_municate.ui.dialogs.AlertDialog;
import com.quickblox.q_municate.ui.mediacall.CallActivity;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBDeleteDialogCommand;
import com.quickblox.q_municate_core.qb.commands.QBRemoveFriendCommand;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
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

    private QBPrivateChatHelper privateChatHelper;
    private User user;
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
        user = UsersDatabaseManager.getUserById(this, friendId);
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
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override
            public void onChange(boolean selfChange) {
                if (FriendDetailsActivity.this.user != null) {
                    user = UsersDatabaseManager.getUserById(FriendDetailsActivity.this,
                            FriendDetailsActivity.this.user.getUserId());
                    setOnlineStatus(user);
                }
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
    }

    private void initUIWithFriendsData() {
        loadAvatar();
        setName();
        setOnlineStatus(user);
        setStatus();
        setPhone();
    }

    private void setStatus() {
        if (!TextUtils.isEmpty(user.getStatus())) {
            statusTextView.setText(user.getStatus());
        }
    }

    @Override
    public void onConnectedToService(QBService service) {
        if (privateChatHelper == null) {
            privateChatHelper = (QBPrivateChatHelper) service.getHelper(QBService.PRIVATE_CHAT_HELPER);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterStatusChangingObserver();
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

    private void setOnlineStatus(User user) {
        if (user != null) {
            if (user.isOnline()) {
                onlineImageView.setVisibility(View.VISIBLE);
            } else {
                onlineImageView.setVisibility(View.GONE);
            }
            onlineStatusTextView.setText(user.getOnlineStatus(this));
        }
    }

    private void loadAvatar() {
        String url = user.getAvatarUrl();
        ImageLoader.getInstance().displayImage(url, avatarImageView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    private void showRemoveUserDialog() {
        AlertDialog alertDialog = AlertDialog.newInstance(getResources().getString(
                R.string.frd_dlg_remove_friend, user.getFullName()));
        alertDialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBRemoveFriendCommand.start(FriendDetailsActivity.this, user.getUserId());
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
        callToUser(user, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.VIDEO);
    }

    private void callToUser(User friend, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM callType) {
        if (friend.getUserId() != AppSession.getSession().getUser().getId()) {
            if (checkFriendStatus(friend.getUserId())) {
                CallActivity.start(FriendDetailsActivity.this, friend, callType);
            }
        }
    }

    public void voiceCallClickListener(View view) {
        callToUser(user, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.AUDIO);
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
        if (checkFriendStatus(user.getUserId())) {
            try {
                QBDialog existingPrivateDialog = privateChatHelper.createPrivateDialogIfNotExist(
                        user.getUserId());
                PrivateDialogActivity.start(FriendDetailsActivity.this, user, existingPrivateDialog);
            } catch (QBResponseException e) {
                ErrorUtils.showError(this, e);
            }
        }
    }

    private void deleteDialog() {
        String dialogId = ChatDatabaseManager.getPrivateDialogIdByOpponentId(this, user.getUserId());
        QBDeleteDialogCommand.start(this, dialogId, QBDialogType.PRIVATE);
    }

    private class RemoveFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            deleteDialog();
            DialogUtils.showLong(FriendDetailsActivity.this, getString(R.string.dlg_friend_removed,
                    user.getFullName()));
            finish();
        }
    }
}