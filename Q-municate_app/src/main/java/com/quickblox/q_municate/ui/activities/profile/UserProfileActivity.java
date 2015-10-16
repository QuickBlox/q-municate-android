package com.quickblox.q_municate.ui.activities.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.activities.chats.PrivateDialogActivity;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.qb.commands.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.commands.QBRemoveFriendCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.UserDataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;

import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.OnClick;

public class UserProfileActivity extends BaseLogeableActivity {

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
    private User user;
    private Observer userObserver;
    private boolean removeContactAndChatHistory;

    public static void start(Context context, int friendId) {
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND_ID, friendId);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);

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
        setTimestamp();
        setPhone();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addObservers();
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
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getUserId());
        if (dialogOccupant != null && dialogOccupant.getDialog() != null) {
            PrivateDialogActivity.start(UserProfileActivity.this, user, dialogOccupant.getDialog());
        } else {
            showProgress();
            QBCreatePrivateChatCommand.start(this, user);
        }
    }

    @OnClick(R.id.audio_call_button)
    void audioCall(View view) {
        ErrorUtils.showError(this, getString(R.string.coming_soon));
    }

    @OnClick(R.id.video_call_button)
    void videoCall(View view) {
        ErrorUtils.showError(this, getString(R.string.coming_soon));
    }

    @OnClick(R.id.delete_chat_history_button)
    void deleteChatHistory(View view) {
        removeContactAndChatHistory = false;
        showRemoveChatHistoryDialog();
    }

    @OnClick(R.id.remove_contact_and_chat_history_button)
    void removeContactAndChatHistory(View view) {
        removeContactAndChatHistory = true;
        showRemoveContactAndChatHistoryDialog();
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

    private void setTimestamp() {
        if (user.getLastLogin() != 0) {
            String timestamp = getString(R.string.last_seen,
                    DateUtils.toTodayYesterdayShortDateWithoutYear2(user.getLastLogin()),
                    DateUtils.formatDateSimpleTime(user.getLastLogin()));
            timestampTextView.setText(timestamp);
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
                        QBRemoveFriendCommand.start(UserProfileActivity.this, user.getUserId());
                    }
                });
    }

    private void showRemoveChatHistoryDialog() {
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
    }

    private void deleteChat() {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getUserId());
        String dialogId = dialogOccupant.getDialog().getDialogId();
        QBDeleteChatCommand.start(this, dialogId, Dialog.Type.PRIVATE);
    }

    private void startPrivateChat(QBDialog qbDialog) {
        PrivateDialogActivity.start(UserProfileActivity.this, user, ChatUtils.createLocalDialog(qbDialog));
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
            QBDialog qbDialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            startPrivateChat(qbDialog);
        }
    }
}