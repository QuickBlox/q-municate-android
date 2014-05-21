package com.quickblox.qmunicate.ui.chats;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBSendGroupChatMessageCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.ui.uihelper.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.List;

public class GroupChatActivity extends BaseChatActivity {
    private List<Friend> friendList;
    private String chatName = "";
    private int allowedNameLength = 20;
    private QBChatHelper qbChatHelper;
    private BaseAdapter messagesAdapter;

    private ListView messagesListView;
    private EditText messageEditText;
    private ImageButton attachButton;
    private ImageButton sendButton;

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
            friendList = (List<Friend>)getIntent().getExtras().getSerializable(GroupChatDetailsActivity.EXTRA_GROUP);
        }
        //TODO: Sometimes causes crash, logging will be improved later.
//        Log.i("ChatName", "Size in GroupChat: " + friendList.size());
        for(Friend friend : friendList){
            if(friend != null){
                chatName = chatName + friend.getFullname() + ",";
            }
            Log.i("ChatName","chatName: " + chatName);
        }

        initUI();
        initListView();
        initChat();
        initListeners();
        registerForContextMenu(messagesListView);
    }

    private void initUI() {
        messagesListView = _findViewById(R.id.messages_listview);
        messageEditText = _findViewById(R.id.message_edittext);
        attachButton = _findViewById(R.id.attach_button);
        sendButton = _findViewById(R.id.send_button);
        actionBarSetup();
    }

    private void initListView() {
        messagesAdapter = getMessagesAdapter();
        messagesListView.setAdapter(messagesAdapter);
    }

    private void initListeners() {
        messageEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
                if (TextUtils.isEmpty(s)) {
                    sendButton.setVisibility(View.GONE);
                    attachButton.setVisibility(View.VISIBLE);
                } else {
                    sendButton.setVisibility(View.VISIBLE);
                    attachButton.setVisibility(View.GONE);
                }
            }
        });
    }

    private void initChat(){
        qbChatHelper = QBChatHelper.getInstance();
        qbChatHelper.initRoomChat(this, chatName, friendList);
    }

    private Cursor getAllGroupChatMessages() {
        return DatabaseManager.getAllGroupChatMessagesByGroupId(this, chatName);
    }

    protected BaseAdapter getMessagesAdapter() {
        return new GroupChatMessagesAdapter(this, getAllGroupChatMessages(), friendList);
    }

    public void sendMessageOnClick(View view) {
        Log.i("GroupMessage: ", "From send, Chat message: " + messageEditText.getText().toString());
        QBSendGroupChatMessageCommand.start(this, messageEditText.getText().toString());
        messageEditText.setText("");
    }

    private void actionBarSetup() {
        ActionBar ab = getActionBar();
        ab.setTitle(chatName);
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