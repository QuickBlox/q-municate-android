package com.quickblox.q_municate.ui.chats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.model.GroupDialog;
import com.quickblox.q_municate.qb.commands.QBAddFriendsToGroupCommand;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.FriendUtils;

import java.util.ArrayList;

public class AddFriendsToGroupActivity extends BaseSelectableFriendListActivity implements NewDialogCounterFriendsListener {

    private static final String EXTRA_GROUP_DIALOG = "extra_group_dialog";

    private GroupDialog dialog;

    public static void start(Context context, GroupDialog dialog) {
        Intent intent = new Intent(context, AddFriendsToGroupActivity.class);
        intent.putExtra(EXTRA_GROUP_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addActions();
    }

    @Override
    protected Cursor getFriends() {
        dialog = (GroupDialog) getIntent().getExtras().getSerializable(EXTRA_GROUP_DIALOG);
        return DatabaseManager.getFriendsFilteredByIds(this, FriendUtils.getFriendIds(dialog.getOccupantList()));
    }

    @Override
    protected void onFriendsSelected(ArrayList<User> selectedFriends) {
        showProgress();
        QBAddFriendsToGroupCommand.start(this, dialog.getId(), FriendUtils.getFriendIds(selectedFriends));
    }

    private void addActions() {
        addAction(QBServiceConsts.ADD_FRIENDS_TO_GROUP_SUCCESS_ACTION, new AddFriendsToGroupSuccessCommand());
        updateBroadcastActionList();
    }

    private class AddFriendsToGroupSuccessCommand implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            finish();
        }
    }
}