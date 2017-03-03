package com.quickblox.q_municate.ui.activities.chats;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.others.BaseFriendsListActivity;
import com.quickblox.q_municate.ui.adapters.friends.FriendsAdapter;
import com.quickblox.q_municate.ui.adapters.friends.SelectableFriendsAdapter;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.listeners.simple.SimpleOnRecycleItemClickListener;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.chat.QBAddFriendsToGroupCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AddFriendsToGroupActivity extends BaseFriendsListActivity {

    public static final int RESULT_ADDED_FRIENDS = 9123;

    private QBChatDialog qbDialog;
    private List<Integer> friendIdsList;

    public static void start(Activity activity, QBChatDialog qbDialog) {
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
    protected void onStart() {
        super.onStart();
        initFields();
        setUpActionBarWithUpButton();
        initCustomListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @Override
    protected List<QMUser> getFriendsList() {
        qbDialog = (QBChatDialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        qbDialog.initForChat(QBChatService.getInstance());
        List<Friend> friendsList = dataManager.getFriendDataManager().getAllForGroupDetails(qbDialog.getOccupants());
        if (!friendsList.isEmpty()) {
            List<Integer> actualFriendIdsList = UserFriendUtils.getFriendIdsListFromList(friendsList);
            List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                    .getActualDialogOccupantsByIds(qbDialog.getDialogId(), actualFriendIdsList);
            if (!dialogOccupantsList.isEmpty()) {
                friendsList.removeAll(UserFriendUtils.getFriendsListFromDialogOccupantsList(dialogOccupantsList));
            }
        }
        if (!friendsList.isEmpty()) {
            Collections.sort(friendsList, new FriendsComparator());
        }
        return UserFriendUtils.getUsersFromFriends(friendsList);
    }

    @Override
    protected FriendsAdapter getFriendsAdapter() {
        return new SelectableFriendsAdapter(this, getFriendsList(), true);
    }

    @Override
    protected void performDone() {
        List<QMUser> selectedFriendsList = ((SelectableFriendsAdapter) friendsAdapter).getSelectedFriendsList();
        if (!selectedFriendsList.isEmpty()) {
            if (isChatInitializedAndUserLoggedIn() && checkNetworkAvailableWithError()) {
                boolean joined = chatHelper != null && qbDialog != null && chatHelper.isDialogJoined(qbDialog);
                if(joined) {
                    showProgress();
                    friendIdsList = UserFriendUtils.getFriendIds(selectedFriendsList);
                    QBAddFriendsToGroupCommand.start(this, qbDialog.getDialogId(),
                            (ArrayList<Integer>) friendIdsList);
                } else{
                    ToastUtils.longToast(R.string.chat_service_is_initializing);
                }
            } else {
                ToastUtils.longToast(R.string.chat_service_is_initializing);
            }
        } else {
            ToastUtils.longToast(R.string.add_friends_to_group_no_friends_for_adding);
        }
    }

    private void initFields() {
        title = getString(R.string.add_friends_to_group_title);
    }

    private void initCustomListeners() {
        friendsAdapter.setOnRecycleItemClickListener(new SimpleOnRecycleItemClickListener<QMUser>() {

            @Override
            public void onItemClicked(View view, QMUser entity, int position) {
                ((SelectableFriendsAdapter) friendsAdapter).selectFriend(position);
            }
        });
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

    private class FriendsComparator implements Comparator<Friend> {

        @Override
        public int compare(Friend friend1, Friend friend2) {
            String firstName = friend1.getUser().getFullName();
            String secondName = friend2.getUser().getFullName();
            return firstName.compareToIgnoreCase(secondName);
        }
    }
}