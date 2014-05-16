package com.quickblox.qmunicate.ui.chats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.main.FriendsListCursorAdapter;
import com.quickblox.qmunicate.ui.uihelper.SimpleActionModeCallback;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NewChatActivity extends BaseActivity implements AdapterView.OnItemClickListener, NewChatCounterFriendsListener {
    private ListView friendsListView;
    private TextView countSelectedFriendsTextView;
    private TextView createGroupChatTextView;

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
        friendsAdapter = new ChatSelectableFriendsAdapter(this, getAllFriends());
        friendsAdapter.setCounterChangedListener(this);

        initListeners();
        initListView();
    }

    private void initListView() {
        // TODO temp friendsList list.
//        QBUser serg = new QBUser("serik", "11111111", "Sergey Fedunets");
//        serg.setId(1);
//        QBUser igor = new QBUser("igor", "11111111", "Igor Shaforenko");
//        igor.setId(2);
//        QBUser anton = new QBUser("anton", "11111111", "Anton Dyachenko");
//        anton.setId(3);
//        QBUser vadim = new QBUser("vadim", "11111111", "Vadim Fite");
//        vadim.setId(4);
//        QBUser gena = new QBUser("gena", "11111111", "Gena Friend");
//        gena.setId(5);
//        friendsArrayList.add(new Friend(serg));
//        friendsArrayList.add(new Friend(igor));
//        friendsArrayList.add(new Friend(anton));
//        friendsArrayList.add(new Friend(vadim));
//        friendsArrayList.add(new Friend(gena));
//        updateFriendListAdapter();
        friendsListView.setAdapter(friendsAdapter);
        friendsListView.setSelector(R.drawable.list_item_background_selector);
        friendsListView.setOnItemClickListener(this);
    }

    private Cursor getAllFriends() {
        return DatabaseManager.getAllFriends(this);
    }

    private void updateFriendListAdapter() {
        Collections.sort(friendsArrayList, new SimpleComparator());
        friendsAdapter.notifyDataSetChanged();
    }

    private void initListeners() {
        friendsListView.setOnItemClickListener(this);
    }

    private void initUI() {
        friendsListView = _findViewById(R.id.chat_friends_listview);
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
        countSelectedFriendsTextView = (TextView) view.findViewById(R.id.count_selected_friends_textview);
        createGroupChatTextView = (TextView) view.findViewById(R.id.create_group_chat_textview);
//        createGroupChatTextView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                 GroupChatActivity.start(NewChatActivity.this, friendsAdapter.getSelectedFriends());
//            }
//        });
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
                GroupChatActivity.start(activity, friendsAdapter.getSelectedFriends());
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