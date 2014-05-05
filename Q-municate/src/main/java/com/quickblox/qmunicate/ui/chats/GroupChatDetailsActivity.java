package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupChatDetailsActivity extends BaseActivity {
    private ListView friendsListView;

    private List<Friend> friendsArrayList;
    private ChatFriendsAdapter friendsAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, GroupChatDetailsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_details);

        initUI();

        friendsArrayList = new ArrayList<Friend>();
        friendsAdapter = new ChatFriendsAdapter(this, R.layout.list_item_chat_friend, friendsArrayList);
        friendsListView.setAdapter(friendsAdapter);

        initListeners();
        initListView();
    }

    private void initUI() {
        friendsListView = _findViewById(R.id.chat_friends_listview);
    }

    private void initListeners() {
    }

    private void initListView() {
        // TODO temp friendsList list.
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_chat_details_menu, menu);
        return true;
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

    private class SimpleComparator implements Comparator<Friend> {
        public int compare(Friend friend1, Friend friend2) {
            // TODO getEmail() is wrong
            return (friend1.getEmail()).compareTo(friend2.getEmail());
        }
    }
}