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

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.uihelper.SimpleActionModeCallback;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NewDialogActivity extends BaseActivity implements AdapterView.OnItemClickListener, NewDialogCounterFriendsListener {

    private ListView friendsListView;
    private TextView countSelectedFriendsTextView;
    private TextView createGroupChatTextView;

    private Activity activity;
    private List<Friend> friendsArrayList;
    private DialogsSelectableFriendsAdapter friendsAdapter;
    private ActionMode actionMode;
    private boolean closeWithoutRedirect;

    public static void start(Context context) {
        Intent intent = new Intent(context, NewDialogActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_dialog);

        activity = this;
        initUI();

        friendsArrayList = new ArrayList<Friend>();
        friendsAdapter = new DialogsSelectableFriendsAdapter(this, getAllFriends());
        friendsAdapter.setCounterChangedListener(this);

        initListeners();
        initListView();
    }

    private void initListView() {
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
        View view = getLayoutInflater().inflate(R.layout.action_mode_new_dialog, null);
        countSelectedFriendsTextView = (TextView) view.findViewById(R.id.count_selected_friends_textview);
        createGroupChatTextView = (TextView) view.findViewById(R.id.create_group_chat_textview);
        actionMode.setCustomView(view);
    }

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
            if (!closeWithoutRedirect) {
                List<Friend> membersList = new ArrayList<Friend>(friendsAdapter.getSelectedFriends());
                Collections.sort(membersList, new SimpleComparator());
                GroupDialogActivity.start(activity, (ArrayList<Friend>) membersList);
                finish();
                actionMode = null;
                closeWithoutRedirect = false;
            } else {
                actionMode = null;
                closeWithoutRedirect = false;
            }
        }
    }
}