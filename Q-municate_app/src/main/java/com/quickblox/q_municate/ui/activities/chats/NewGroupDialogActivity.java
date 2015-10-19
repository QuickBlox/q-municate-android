package com.quickblox.q_municate.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.others.BaseSelectableUsersActivity;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.QBCreateGroupDialogCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;

import java.util.ArrayList;
import java.util.List;

public class NewGroupDialogActivity extends BaseSelectableUsersActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, NewGroupDialogActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addActions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @Override
    protected List<User> getUsers() {
        List<Friend> friendsList = dataManager.getFriendDataManager().getAllSorted();
        return UserFriendUtils.getUsersFromFriends(friendsList);
    }

    @Override
    protected void onUsersSelected(ArrayList<User> selectedFriends) {
        createGroupChat(selectedFriends);
    }

    protected void removeActions() {
        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION);

        updateBroadcastActionList();
    }

    protected void addActions() {
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION, new CreateGroupChatSuccessAction());
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void createGroupChat(ArrayList<User> friendList) {
        showProgress();
        String groupName = ChatUtils.createChatName(friendList);
        QBCreateGroupDialogCommand.start(this, groupName, friendList);
    }

    private class CreateGroupChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            QBDialog dialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);

            if (dialog != null && dialog.getRoomJid() != null) {
                GroupDialogActivity.start(NewGroupDialogActivity.this, ChatUtils.createLocalDialog(dialog));
                finish();
            } else {
                ErrorUtils.showError(NewGroupDialogActivity.this, getString(R.string.dlg_fail_create_groupchat));
            }
        }
    }
}