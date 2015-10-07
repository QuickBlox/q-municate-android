package com.quickblox.q_municate.ui.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.activities.chats.PrivateDialogActivity;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.ui.activities.call.CallActivity;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.qb.commands.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.commands.QBRemoveFriendCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.OnlineStatusUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.UserDataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;

import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;

public class UserProfileActivity extends BaseLogeableActivity {

    @Bind(R.id.avatar_imageview)
    RoundedImageView avatarImageView;

    @Bind(R.id.name_textview)
    TextView nameTextView;

    @Bind(R.id.status_textview)
    TextView statusTextView;

    @Bind(R.id.online_imageview)
    ImageView onlineImageView;

    @Bind(R.id.online_status_textview)
    TextView onlineStatusTextView;

    @Bind(R.id.phone_textview)
    TextView phoneTextView;

    @Bind(R.id.phone_relativelayout)
    View phoneView;

    private DataManager dataManager;
    private int userId;
    private User user;
    private Observer userObserver;

    public static void start(Context context, int friendId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND_ID, friendId);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friend_details);

        activateButterKnife();

        initActionBar();
        initFields();
        initUIWithUsersData();
        addActions();
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initFields() {
        dataManager = DataManager.getInstance();
        canPerformLogout.set(true);
        userId = getIntent().getExtras().getInt(QBServiceConsts.EXTRA_FRIEND_ID);
        user = dataManager.getUserDataManager().get(userId);
        userObserver = new UserObserver();
    }

    private void initUIWithUsersData() {
        loadAvatar();
        setName();
        setOnlineStatus(user);
        setStatus();
        setPhone();
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);

        if (friendListHelper != null) {
            setOnlineStatus(user);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        addObservers();

        setOnlineStatus(user);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friend_details_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showRemoveUserDialog();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void addObservers() {
        dataManager.getUserDataManager().addObserver(userObserver);
    }

    private void deleteObservers() {
        dataManager.getUserDataManager().deleteObserver(userObserver);
    }

    private void addActions() {
        addAction(QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION, new RemoveFriendSuccessAction());
        addAction(QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION, new CreatePrivateChatSuccessAction());
        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION);
        removeAction(QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION);

        removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void setStatus() {
        if (!TextUtils.isEmpty(user.getStatus())) {
            statusTextView.setText(user.getStatus());
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
        if (user != null && friendListHelper != null) {
            boolean online = friendListHelper.isUserOnline(user.getUserId());

            if (online) {
                onlineImageView.setVisibility(View.VISIBLE);
            } else {
                onlineImageView.setVisibility(View.GONE);
            }

            onlineStatusTextView.setText(OnlineStatusUtils.getOnlineStatus(online));
        }
    }

    private void loadAvatar() {
        String url = user.getAvatar();
        ImageLoader.getInstance().displayImage(url, avatarImageView,
                ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    private void showRemoveUserDialog() {
        TwoButtonsDialogFragment.show(
                getSupportFragmentManager(),
                getString(R.string.frd_dlg_remove_friend, user.getFullName()),
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        showProgress();
                        QBRemoveFriendCommand.start(UserProfileActivity.this, user.getUserId());
                    }
                });
    }

    public void videoCallClickListener(View view) {
        ErrorUtils.showError(this, getString(R.string.coming_soon));
//        callToUser(user, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.VIDEO);
    }

    private void callToUser(User friend, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM callType) {
        if (friend.getUserId() != AppSession.getSession().getUser().getId()) {
            if (checkFriendStatus(friend.getUserId())) {
                CallActivity.start(UserProfileActivity.this, friend, callType);
            }
        }
    }

    public void voiceCallClickListener(View view) {
        ErrorUtils.showError(this, getString(R.string.coming_soon));
//        callToUser(user, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.AUDIO);
    }

    private boolean checkFriendStatus(int userId) {
        boolean isFriend = DataManager.getInstance().getFriendDataManager().getByUserId(userId) != null;
        if (isFriend) {
            return true;
        } else {
            ToastUtils.longToast(R.string.dlg_user_is_not_friend);
            return false;
        }
    }

    public void chatClickListener(View view) {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getUserId());
        if (dialogOccupant != null && dialogOccupant.getDialog() != null) {
            PrivateDialogActivity.start(UserProfileActivity.this, user, dialogOccupant.getDialog());
        } else {
            showProgress();
            QBCreatePrivateChatCommand.start(this, user);
        }
    }

    private void deleteDialog() {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getUserId());
        String dialogId = dialogOccupant.getDialog().getDialogId();
        QBDeleteChatCommand.start(this, dialogId, Dialog.Type.PRIVATE);
    }

    @Override
    public void notifyChangedUserStatus(int userId, boolean online) {
        super.notifyChangedUserStatus(userId, online);
        setOnlineStatus(user);
    }

    private class UserObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(UserDataManager.OBSERVE_KEY)) {
                user = DataManager.getInstance().getUserDataManager().get(userId);
                initUIWithUsersData();
            }
        }
    }

    private class RemoveFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            deleteDialog();
            ToastUtils.longToast(getString(R.string.dlg_friend_removed, user.getFullName()));
            finish();
        }
    }

    private class CreatePrivateChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            hideProgress();
            QBDialog qbDialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            PrivateDialogActivity.start(UserProfileActivity.this, user, ChatUtils.createLocalDialog(qbDialog));
        }
    }
}