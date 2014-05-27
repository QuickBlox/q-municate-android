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

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBCreateGroupChatCommand;
import com.quickblox.qmunicate.qb.commands.QBJoinGroupChatCommand;
import com.quickblox.qmunicate.qb.commands.QBSendGroupChatMessageCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;

public class GroupChatActivity extends BaseChatActivity {

    private BaseAdapter messagesAdapter;

    private QBDialog groupDialog;
    private ArrayList<Friend> friendList;
    private String groupName;
    private String groupJid;

    private ListView messagesListView;
    private EditText messageEditText;
    //    private ImageButton attachButton;
    private ImageButton sendButton;

    public GroupChatActivity() {
        super(R.layout.activity_group_chat);
    }

    public static void start(Context context, ArrayList<Friend> friends) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra(GroupChatDetailsActivity.EXTRA_FRIENDS, friends);
        context.startActivity(intent);
    }

    public static void start(Context context, QBDialog dialog) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra(GroupChatDetailsActivity.EXTRA_ROOM_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initActions();
        initChat();
        initUI();
        initListView();
        initListeners();
        registerForContextMenu(messagesListView);
    }

    private void initActions() {
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION, new CreateChatSuccessAction());
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION, failAction);
    }

    private void initUI() {
        messagesListView = _findViewById(R.id.messages_listview);
        messageEditText = _findViewById(R.id.message_edittext);
        //        attachButton = _findViewById(R.id.attach_button);
        sendButton = _findViewById(R.id.send_button);
        actionBarSetup();
    }

    private void initListView() {
        messagesAdapter = getMessagesAdapter();
        messagesListView.setAdapter(messagesAdapter);
    }

    private void initListeners() {
        //        messageEditText.addTextChangedListener(new SimpleTextWatcher() {
        //            @Override
        //            public void onTextChanged(CharSequence s, int start, int before, int count) {
        //                super.onTextChanged(s, start, before, count);
        //                if (TextUtils.isEmpty(s)) {
        //                    sendButton.setVisibility(View.GONE);
        //                    attachButton.setVisibility(View.VISIBLE);
        //                } else {
        //                    sendButton.setVisibility(View.VISIBLE);
        //                    attachButton.setVisibility(View.GONE);
        //                }
        //            }
        //        });
    }

    private void initChat() {
        Bundle extras = getIntent().getExtras();
        if (getIntent().hasExtra(GroupChatDetailsActivity.EXTRA_ROOM_DIALOG)) {
            groupDialog = (QBDialog) extras.getSerializable(GroupChatDetailsActivity.EXTRA_ROOM_DIALOG);
            groupName = groupDialog.getName();
            groupJid = groupDialog.getRoomJid();
            QBJoinGroupChatCommand.start(this, groupJid);
        } else {
            friendList = (ArrayList<Friend>) extras.getSerializable(GroupChatDetailsActivity.EXTRA_FRIENDS);
            groupName = createChatName();
            groupJid = Consts.EMPTY_STRING;
            QBCreateGroupChatCommand.start(this, groupName, friendList);
        }
    }

    private String createChatName() {
        String userFullname = App.getInstance().getUser().getFullName();
        String friendsFullnames = TextUtils.join(",", friendList);
        return userFullname + "," + friendsFullnames;
    }

    private Cursor getAllGroupChatMessages() {
        return DatabaseManager.getAllGroupChatMessagesByGroupId(this, groupJid);
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
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(groupName);
        actionBar.setSubtitle("some information");
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
                // GroupChatDetailsActivity.start(this);
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

    private class CreateChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            groupDialog = (QBDialog) bundle.getSerializable(QBServiceConsts.EXTRA_CHAT_DIALOG);
            groupName = groupDialog.getName();
            groupJid = groupDialog.getRoomJid();
            initListView();
        }
    }
}