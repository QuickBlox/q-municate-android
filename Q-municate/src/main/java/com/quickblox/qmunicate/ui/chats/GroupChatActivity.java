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

public class GroupChatActivity extends BaseChatActivity {

    public GroupChatActivity() {
        super(R.layout.activity_group_chat);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initUI();

        registerForContextMenu(messagesListView);
    }

    private void initUI() {
        actionBarSetup();
    }

    private void actionBarSetup() {
        ActionBar ab = getActionBar();
        ab.setTitle("Name of Chat");
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