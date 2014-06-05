package com.quickblox.qmunicate.ui.chats;

import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.uihelper.SimpleActionModeCallback;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public abstract class BaseSelectableFriendListActivity extends BaseActivity implements NewDialogCounterFriendsListener {

    protected DialogsSelectableFriendsAdapter friendsAdapter;
    protected ListView friendsListView;
    protected TextView countSelectedFriendsTextView;
    private ActionMode actionMode;
    private boolean isNeedToCloseWithoutRedirect;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_group_friendlist);

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
            if (valueCounter == Consts.ZERO_INT_VALUE) {
                isNeedToCloseWithoutRedirect = true;
                actionMode.finish();
                return;
            }
        } else {
            startAction();
        }
        countSelectedFriendsTextView.setText(valueCounter + Consts.EMPTY_STRING);
    }

    private void startAction() {
        actionMode = startActionMode(new ActionModeCallback());
        View view = getActionModeView();
        countSelectedFriendsTextView = (TextView) view.findViewById(R.id.count_selected_friends_textview);
        actionMode.setCustomView(view);
    }

    protected abstract View getActionModeView();

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            isNeedToCloseWithoutRedirect = true;
            actionMode.finish();
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

    protected abstract void onFriendsSelected(ArrayList<Friend> selectedFriends);

    public static class SimpleComparator implements Comparator<Friend> {

        public int compare(Friend friend1, Friend friend2) {
            return (new Integer(friend1.getId())).compareTo(friend2.getId());
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
                ArrayList<Friend> selectedFriends = new ArrayList<Friend>(
                        friendsAdapter.getSelectedFriends());
                Collections.sort(selectedFriends, new SimpleComparator());
                onFriendsSelected(selectedFriends);
            }
            actionMode = null;
        }
    }
}