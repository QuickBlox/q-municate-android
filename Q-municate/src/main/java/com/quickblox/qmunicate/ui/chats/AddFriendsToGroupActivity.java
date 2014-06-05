package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.GroupDialog;
import com.quickblox.qmunicate.qb.commands.QBAddFriendsToGroupCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.util.ArrayList;
import java.util.List;

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
        return DatabaseManager.getFriendsFilteredByIds(this, getFriendIds(dialog.getOccupantList()));
    }

    @Override
    protected View getActionModeView() {
        return getLayoutInflater().inflate(R.layout.action_mode_add_friends, null);
    }

    @Override
    protected void onFriendsSelected(ArrayList<Friend> selectedFriends) {
        showProgress();
        QBAddFriendsToGroupCommand.start(this, dialog.getRoomJid(), getFriendIds(selectedFriends));
    }

    private void addActions() {
        addAction(QBServiceConsts.ADD_FRIENDS_TO_GROUP_SUCCESS_ACTION, new AddFriendsToGroupSuccessCommand());
        updateBroadcastActionList();
    }

    private ArrayList<Integer> getFriendIds(List<Friend> friendList) {
        ArrayList<Integer> friendIds = new ArrayList<Integer>();
        for (Friend friend : friendList) {
            friendIds.add(friend.getId());
        }
        return friendIds;
    }

    private class AddFriendsToGroupSuccessCommand implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            finish();
        }
    }
}