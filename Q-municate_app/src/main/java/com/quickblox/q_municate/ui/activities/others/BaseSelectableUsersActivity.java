package com.quickblox.q_municate.ui.activities.others;

import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.views.recyclerview.SimpleDividerItemDecoration;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.adapters.friends.SelectableFriendsAdapter;
import com.quickblox.q_municate.utils.listeners.SelectUsersListener;
import com.quickblox.q_municate.utils.simple.SimpleActionModeCallback;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;

public abstract class BaseSelectableUsersActivity extends BaseLogeableActivity implements SelectUsersListener {

    @Bind(R.id.users_recyclerview)
    RecyclerView usersRecyclerView;

    protected SelectableFriendsAdapter selectableFriendsAdapter;
    protected DataManager dataManager;

    private ActionMode actionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        activateButterKnife();

        initFields();
        initActionBar();
        initRecyclerView();
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
        selectableFriendsAdapter = new SelectableFriendsAdapter(this, getUsers());
        selectableFriendsAdapter.setCounterChangedListener(this);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));;
        usersRecyclerView.setAdapter(selectableFriendsAdapter);
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);

        if (friendListHelper != null) {
            selectableFriendsAdapter.setFriendListHelper(friendListHelper);
        }
    }

    @Override
    public void onCounterUsersChanged(int valueCounter) {
        if (actionMode != null) {
            if (valueCounter == ConstsCore.ZERO_INT_VALUE) {
                actionMode.finish();
                return;
            }
        } else {
            startAction();
        }
        actionMode.setTitle(getString(R.string.ndl_ac_mode_title) + ConstsCore.SPACE + valueCounter);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            actionMode.finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    protected abstract List<User> getUsers();

    private void startAction() {
        actionMode = startSupportActionMode(new ActionModeCallback());
    }

    protected abstract void onUsersSelected(ArrayList<User> selectedFriends);

    public static class SimpleComparator implements Comparator<User> {

        public int compare(User friend1, User friend2) {
            return (new Integer(friend1.getUserId())).compareTo(friend2.getUserId());
        }
    }

    @Override
    public void notifyChangedUserStatus(int userId, boolean online) {
        super.notifyChangedUserStatus(userId, online);

        selectableFriendsAdapter.notifyDataSetChanged();
    }

    private class ActionModeCallback extends SimpleActionModeCallback {

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.done_menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_done:
                    ArrayList<User> selectedFriends = new ArrayList<User>(
                            selectableFriendsAdapter.getSelectedFriends());
                    Collections.sort(selectedFriends, new SimpleComparator());
                    onUsersSelected(selectedFriends);

                    actionMode.finish();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            selectableFriendsAdapter.clearSelectedFriends();
            actionMode = null;
        }
    }
}