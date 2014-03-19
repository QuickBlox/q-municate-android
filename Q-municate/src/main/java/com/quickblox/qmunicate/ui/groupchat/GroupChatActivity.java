package com.quickblox.qmunicate.ui.groupchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.ChatMessage;
import com.quickblox.qmunicate.ui.base.BaseActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupChatActivity extends BaseActivity {
    private ListView messagesListView;

    private List<ChatMessage> messagesArrayList;
    private ChatMessagesAdapter messagesAdapter;

    public static void start(Context context) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        initUI();

        messagesArrayList = new ArrayList<ChatMessage>();
        messagesAdapter = new ChatMessagesAdapter(this, R.layout.list_item_chat_message, messagesArrayList);
        messagesListView.setAdapter(messagesAdapter);

        initListeners();
        initListView();
    }

    private void initUI() {
        messagesListView = _findViewById(R.id.messagesListView);
    }

    private void initListeners() {
        registerForContextMenu(messagesListView);
    }

    private void initListView() {
        // TODO temp list.
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        messagesArrayList.add(new ChatMessage("", new Date(), true));
        updateFriendListAdapter();
    }

    private void updateFriendListAdapter() {
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            case R.id.action_group_details:
                GroupChatDetailsActivity.start(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater m = getMenuInflater();
        m.inflate(R.menu.group_chat_ctx_menu, menu);
    }
}