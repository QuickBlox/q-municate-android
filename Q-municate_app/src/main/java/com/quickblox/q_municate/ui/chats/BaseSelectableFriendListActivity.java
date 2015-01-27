package com.quickblox.q_municate.ui.chats;

import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.chats.dialogs.NewDialogCounterFriendsListener;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.uihelper.SimpleActionModeCallback;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public abstract class BaseSelectableFriendListActivity extends BaseLogeableActivity implements NewDialogCounterFriendsListener {

    protected DialogsSelectableFriendsAdapter friendsAdapter;
    protected ListView friendsListView;
    private ActionMode actionMode;
    private boolean isNeedToCloseWithoutRedirect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_group_friendlist);
        initBase();
        initUI();
        initListView();
    }

    @Override
    public void onBackPressed() {
        isNeedToCloseWithoutRedirect = true;
        super.onBackPressed();
    }

    private void initUI() {
        friendsListView = _findViewById(R.id.chat_friends_listview);
    }

    protected abstract Cursor getFriends();

    private void initListView() {
        friendsAdapter = new DialogsSelectableFriendsAdapter(this, getFriends());
        friendsAdapter.setCounterChangedListener(this);
        friendsListView.setAdapter(friendsAdapter);
        friendsListView.setSelector(R.drawable.list_item_background_selector);
    }

    @Override
    public void onCounterFriendsChanged(int valueCounter) {
        if (actionMode != null) {
            if (valueCounter == ConstsCore.ZERO_INT_VALUE) {
                closeActionModeWithRedirect(true);
                return;
            }
        } else {
            startAction();
        }
        actionMode.setTitle(getResources().getString(R.string.ndl_ac_mode_title) + ConstsCore.SPACE + valueCounter);
    }

    private void startAction() {
        actionMode = startActionMode(new ActionModeCallback());
    }

    private void closeActionModeWithRedirect(boolean isNeedToCloseWithoutRedirect) {
        this.isNeedToCloseWithoutRedirect = isNeedToCloseWithoutRedirect;
        actionMode.finish();
    }

    private void initBase() {
        canPerformLogout.set(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            closeActionModeWithRedirect(true);
            return true;
        } else {
            isNeedToCloseWithoutRedirect = false;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                isNeedToCloseWithoutRedirect = true;
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

    private class ActionModeCallback extends SimpleActionModeCallback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (!isNeedToCloseWithoutRedirect) {
                ArrayList<User> selectedFriends = new ArrayList<User>(
                        friendsAdapter.getSelectedFriends());
                Collections.sort(selectedFriends, new SimpleComparator());
                onFriendsSelected(selectedFriends);
            }
            actionMode = null;
        }
    }
}