package com.quickblox.q_municate.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.adapters.friends.FriendsAdapter;
import com.quickblox.q_municate.utils.simple.SimpleOnRecycleItemClickListener;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.QBCreatePrivateChatCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

import butterknife.Bind;

public class NewMessageActivity extends BaseLogeableActivity {

    @Bind(R.id.users_recyclerview)
    RecyclerView usersRecyclerView;

    private DataManager dataManager;
    private FriendsAdapter friendsAdapter;
    private User selectedUser;

    public static void start(Context context) {
        Intent intent = new Intent(context, NewMessageActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        activateButterKnife();

        initActionBar();
        initFields();
        initRecyclerView();
        initCustomListeners();

        addActions();
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initFields() {
        dataManager = DataManager.getInstance();
    }

    private void initRecyclerView() {
        List<Friend> friendsList = dataManager.getFriendDataManager().getAllSorted();
        friendsAdapter = new FriendsAdapter(this, UserFriendUtils.getUsersFromFriends(friendsList));
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(friendsAdapter);
    }

    private void initCustomListeners() {
        friendsAdapter.setOnRecycleItemClickListener(new SimpleOnRecycleItemClickListener<User>() {

            @Override
            public void onItemClicked(View view, User user, int position) {
                super.onItemClicked(view, user, position);
                selectedUser = user;
                checkForOpenChat(user);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_message_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                break;
            case R.id.action_create_group:
                NewGroupDialogActivity.start(this);
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void addActions() {
        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION, new CreatePrivateChatSuccessAction());
        addAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CREATE_PRIVATE_CHAT_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void checkForOpenChat(User user) {
        showProgress();
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupantForPrivateChat(user.getUserId());
        if (dialogOccupant != null && dialogOccupant.getDialog() != null) {
            startPrivateChat(dialogOccupant.getDialog());
        } else {
            showProgress();
            QBCreatePrivateChatCommand.start(this, user);
        }
    }

    private void startPrivateChat(Dialog dialog) {
        PrivateDialogActivity.start(this, selectedUser, dialog);
        finish();
    }

    private class CreatePrivateChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            QBDialog qbDialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            startPrivateChat(ChatUtils.createLocalDialog(qbDialog));
        }
    }
}