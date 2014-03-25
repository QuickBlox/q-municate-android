package com.quickblox.qmunicate.ui.chats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.uihelper.SimpleActionModeCallback;
import com.quickblox.qmunicate.ui.utils.Consts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NewChatActivity extends BaseActivity implements AdapterView.OnItemClickListener, NewChatCounterFriendsListener {
    private ListView friendsListView;
    private TextView countSelectedFriendsTextView;

    private Activity activity;
    private List<Friend> friendsArrayList;
    private ChatSelectableFriendsAdapter friendsAdapter;
    private ActionMode actionMode;
    private boolean closeWithoutRedirect;

    public static void start(Context context) {
        Intent intent = new Intent(context, NewChatActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        activity = this;
        initUI();

        friendsArrayList = new ArrayList<Friend>();
        friendsAdapter = new ChatSelectableFriendsAdapter(this, R.layout.list_item_chat_friend_selectable, friendsArrayList);
        friendsListView.setAdapter(friendsAdapter);
        friendsAdapter.setCounterChangedListener(this);

        initListeners();
        initListView();
    }

    private void initListView() {
        // TODO temp friends list.
        friendsArrayList.add(new Friend(new QBUser("serik", "11111111", "Sergey Fedunets")));
        friendsArrayList.add(new Friend(new QBUser("igor", "11111111", "Igor Shaforenko")));
        friendsArrayList.add(new Friend(new QBUser("anton", "11111111", "Anton Dyachenko")));
        friendsArrayList.add(new Friend(new QBUser("vadim", "11111111", "Vadim Fite")));
        friendsArrayList.add(new Friend(new QBUser("gena", "11111111", "Gena Friend")));
        updateFriendListAdapter();
    }

    private void updateFriendListAdapter() {
        Collections.sort(friendsArrayList, new SimpleComparator());
        friendsAdapter.notifyDataSetChanged();
    }

    private void initListeners() {
        friendsListView.setOnItemClickListener(this);
    }

    private void initUI() {
        friendsListView = _findViewById(R.id.chatFriendsListView);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onCounterFriendsChanged(int valueCounter) {
        if (actionMode != null) {
            if (valueCounter == Consts.ZERO_VALUE) {
                closeWithoutRedirect = true;
                actionMode.finish();
                return;
            }
        } else {
            startAction();
        }
        countSelectedFriendsTextView.setText(valueCounter + "");
    }

    private void startAction() {
        actionMode = startActionMode(new ActionModeCallback());
        View view = getLayoutInflater().inflate(R.layout.action_mode_new_chat, null);
        countSelectedFriendsTextView = (TextView) view.findViewById(R.id.countSelectedFriendsTextView);
        actionMode.setCustomView(view);
    }

    private class ActionModeCallback extends SimpleActionModeCallback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (!closeWithoutRedirect) {
                GroupChatActivity.start(activity);
                actionMode = null;
                closeWithoutRedirect = false;
            } else {
                actionMode = null;
                closeWithoutRedirect = false;
            }
        }
    }

    private class SimpleComparator implements Comparator<Friend> {
        public int compare(Friend friend1, Friend friend2) {
            // TODO getEmail() is wrong
            return (friend1.getEmail()).compareTo(friend2.getEmail());
        }
    }
}