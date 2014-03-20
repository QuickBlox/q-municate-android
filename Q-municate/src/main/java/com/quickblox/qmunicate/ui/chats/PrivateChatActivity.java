package com.quickblox.qmunicate.ui.chats;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.ChatMessage;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.utils.DialogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrivateChatActivity extends BaseActivity {
    private ListView messagesListView;

    private List<ChatMessage> messagesArrayList;
    private ChatMessagesAdapter messagesAdapter;
    private static String nameOpponent;

    public static void start(Context context, String name) {
        Intent intent = new Intent(context, PrivateChatActivity.class);
        context.startActivity(intent);
        nameOpponent = name;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        initUI();

        messagesArrayList = new ArrayList<ChatMessage>();
        messagesAdapter = new ChatMessagesAdapter(this, R.layout.list_item_chat_message, messagesArrayList);
        messagesListView.setAdapter(messagesAdapter);

        initListeners();
        initListView();
    }

    private void initUI() {
        messagesListView = _findViewById(R.id.messagesListView);
        actionBarSetup();
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void actionBarSetup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar ab = getActionBar();
            ab.setTitle(nameOpponent);
            ab.setSubtitle("some information");
        }
    }

    private void updateFriendListAdapter() {
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.private_chat_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            case R.id.action_audio_call:
                // TODO add audio call
                DialogUtils.show(this, getString(R.string.comming_soon));
                return true;
            case R.id.action_video_call:
                // TODO add video call
                DialogUtils.show(this, getString(R.string.comming_soon));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}