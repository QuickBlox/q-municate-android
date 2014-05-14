package com.quickblox.qmunicate.ui.chats;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.GroupChat;

import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends BaseChatActivity {
    private List<Friend> friends;
    private String nameOfChat = "";
    private int allowedNameLength = 20;

    public GroupChatActivity() {
        super(R.layout.activity_group_chat);
    }

    public static void start(Context context, ArrayList<Friend> friends) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra(GroupChatDetailsActivity.EXTRA_GROUP, friends);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().hasExtra(GroupChatDetailsActivity.EXTRA_GROUP)) {
            friends = (List<Friend>)getIntent().getExtras().getSerializable(GroupChatDetailsActivity.EXTRA_GROUP);
        }
        for(Friend friend : friends){
            if(nameOfChat.length() < allowedNameLength){
                nameOfChat = nameOfChat + friend.getLogin() + ",";
            } else {
                nameOfChat = nameOfChat + "...";
                break;
            }
        }

        initUI();

        registerForContextMenu(messagesListView);
    }

    private void initUI() {
        actionBarSetup();
    }

    private void actionBarSetup() {
        ActionBar ab = getActionBar();
        ab.setTitle(nameOfChat);
        ab.setSubtitle("some information");
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
//                GroupChatDetailsActivity.start(this);
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