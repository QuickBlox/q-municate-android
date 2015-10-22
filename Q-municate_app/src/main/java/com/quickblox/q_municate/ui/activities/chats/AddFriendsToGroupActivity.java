package com.quickblox.q_municate.ui.activities.chats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.others.BaseFriendsListActivity;
import com.quickblox.q_municate.ui.adapters.friends.FriendsAdapter;
import com.quickblox.q_municate.ui.adapters.friends.SelectableFriendsAdapter;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.chat.QBAddFriendsToGroupCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AddFriendsToGroupActivity extends BaseFriendsListActivity {

    public static final int RESULT_ADDED_FRIENDS = 9123;

    private QBDialog qbDialog;
    private List<Integer> friendIdsList;

    public static void start(Activity activity, QBDialog qbDialog) {
        Intent intent = new Intent(activity, AddFriendsToGroupActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, qbDialog);
        activity.startActivityForResult(intent, RESULT_ADDED_FRIENDS);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_friends_list;
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
    protected List<User> getFriendsList() {
        qbDialog = (QBDialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        List<Friend> friendsList = DataManager.getInstance().getFriendDataManager().getAllWithoutTheseIds(qbDialog.getOccupants());
        return UserFriendUtils.getUsersFromFriends(friendsList);
    }

    @Override
    protected FriendsAdapter getFriendsAdapter() {
        return new SelectableFriendsAdapter(this, getFriendsList(), true);
    }

    @Override
    protected void performDone() {
        List<User> selectedFriendsList = ((SelectableFriendsAdapter) friendsAdapter).getSelectedFriendsList();
        if (!selectedFriendsList.isEmpty()) {
            showProgress();
            friendIdsList = UserFriendUtils.getFriendIds(selectedFriendsList);
            QBAddFriendsToGroupCommand.start(this, qbDialog.getDialogId(), (ArrayList<Integer>) friendIdsList);
        } else {
            ToastUtils.longToast(R.string.add_friends_to_group_no_friends_for_adding);
        }
    }

    private void addActions() {
        addAction(QBServiceConsts.ADD_FRIENDS_TO_GROUP_SUCCESS_ACTION, new AddFriendsToGroupSuccessCommand());
        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.ADD_FRIENDS_TO_GROUP_SUCCESS_ACTION);
        updateBroadcastActionList();
    }

    private class AddFriendsToGroupSuccessCommand implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            Intent intent = new Intent();
            intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, (Serializable) friendIdsList);
            setResult(RESULT_ADDED_FRIENDS, intent);
            finish();
        }
    }
}