package com.quickblox.q_municate.ui.activities.chats;

import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.listeners.NewDialogCounterFriendsListener;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.adapters.chats.DialogsSelectableFriendsAdapter;
import com.quickblox.q_municate.ui.uihelpers.SimpleActionModeCallback;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BaseSelectableFriendListActivity extends BaseLogeableActivity implements NewDialogCounterFriendsListener {

    protected DialogsSelectableFriendsAdapter friendsAdapter;
    protected ListView friendsListView;

    private ActionMode actionMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_base_group_friendlist);

        activateButterKnife();

        initActionBar();
        initBase();
        initUI();
        initListView();
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initUI() {
        friendsListView = _findViewById(R.id.chat_friends_listview);
    }

    protected abstract List<User> getFriends();

    private void initListView() {
        friendsAdapter = new DialogsSelectableFriendsAdapter(this, getFriends());
        friendsAdapter.setCounterChangedListener(this);
        friendsAdapter.setFriendListHelper(friendListHelper);
        friendsListView.setAdapter(friendsAdapter);
        friendsListView.setSelector(R.drawable.selector_list_item);
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);

        if (friendListHelper != null) {
            friendsAdapter.setFriendListHelper(friendListHelper);
        }
    }

    @Override
    public void onCounterFriendsChanged(int valueCounter) {
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

    private void startAction() {
        actionMode = startSupportActionMode(new ActionModeCallback());
    }

    private void initBase() {
        canPerformLogout.set(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            actionMode.finish();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected abstract void onFriendsSelected(ArrayList<User> selectedFriends);

    public static class SimpleComparator implements Comparator<User> {

        public int compare(User friend1, User friend2) {
            return (new Integer(friend1.getUserId())).compareTo(friend2.getUserId());
        }
    }

    @Override
    public void notifyChangedUserStatus(int userId, boolean online) {
        super.notifyChangedUserStatus(userId, online);

        friendsAdapter.notifyDataSetChanged();
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
                            friendsAdapter.getSelectedFriends());
                    Collections.sort(selectedFriends, new SimpleComparator());
                    onFriendsSelected(selectedFriends);

                    actionMode.finish();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            friendsAdapter.clearSelectedFriends();
            actionMode = null;
        }
    }
}