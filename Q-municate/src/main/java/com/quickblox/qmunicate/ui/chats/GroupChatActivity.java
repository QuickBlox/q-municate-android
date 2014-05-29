package com.quickblox.qmunicate.ui.chats;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.caching.tables.ChatMessagesTable;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBCreateGroupChatCommand;
import com.quickblox.qmunicate.qb.commands.QBJoinGroupChatCommand;
import com.quickblox.qmunicate.qb.commands.QBSendGroupChatMessageCommand;
import com.quickblox.qmunicate.qb.commands.QBUpdateChatDialogCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ReceiveFileListener;
import com.quickblox.qmunicate.utils.ReceiveImageFileTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class GroupChatActivity extends BaseChatActivity implements ReceiveFileListener {

    private BaseAdapter messagesAdapter;

    private QBDialog groupDialog;
    private ArrayList<Friend> friendList;
    private String groupName;
    private String groupJid;

    public GroupChatActivity() {
        super(R.layout.activity_chat);
    }

    public static void start(Context context, ArrayList<Friend> friends) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_GROUP_CHAT, friends);
        context.startActivity(intent);
    }

    public static void start(Context context, QBDialog dialog) {
        Intent intent = new Intent(context, GroupChatActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initChat();
        initListView();
        initActionBar();

        registerForContextMenu(messagesListView);
    }

    private void initChat() {
        Bundle extras = getIntent().getExtras();
        if (getIntent().hasExtra(QBServiceConsts.EXTRA_CHAT_DIALOG)) {
            groupDialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_CHAT_DIALOG);
            groupName = groupDialog.getName();
            groupJid = groupDialog.getRoomJid();
            QBJoinGroupChatCommand.start(this, groupJid);
        } else {
            friendList = (ArrayList<Friend>) extras.getSerializable(QBServiceConsts.EXTRA_GROUP_CHAT);
            groupName = createChatName();
            groupJid = Consts.EMPTY_STRING;
            QBCreateGroupChatCommand.start(this, groupName, friendList);
        }
    }

    private void initListView() {
        messagesAdapter = getMessagesAdapter();
        messagesListView.setAdapter(messagesAdapter);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(groupName);
        actionBar.setSubtitle("some information");
    }

    private String createChatName() {
        String userFullname = App.getInstance().getUser().getFullName();
        String friendsFullnames = TextUtils.join(",", friendList);
        return userFullname + "," + friendsFullnames;
    }

    protected BaseAdapter getMessagesAdapter() {
        return new GroupChatMessagesAdapter(this, getAllGroupChatMessages());
    }

    private Cursor getAllGroupChatMessages() {
        return DatabaseManager.getAllGroupChatMessagesByGroupId(this, groupJid);
    }

    protected void addActions() {
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION, new CreateChatSuccessAction());
        addAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION, new LoadAttachFileSuccessAction());
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_SUCCESS_ACTION, new LoadDialogMessagesSuccessAction());
        addAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    @Override
    protected void onUpdateChatDialog() {
        if(!messagesAdapter.isEmpty()) {
            startUpdateChatDialog();
        }
    }

    private void startUpdateChatDialog() {
        Cursor cursor = (Cursor) messagesAdapter.getItem(messagesAdapter.getCount() - 1);
        String lastMessage = cursor.getString(cursor.getColumnIndex(ChatMessagesTable.Cols.BODY));
        QBUpdateChatDialogCommand.start(this, groupJid, lastMessage, Consts.ZERO_VALUE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    protected void removeActions() {
        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CREATE_GROUP_CHAT_FAIL_ACTION);
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);
        removeAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_DIALOG_MESSAGES_FAIL_ACTION);
    }

    @Override
    protected void onFileSelected(Uri originalUri) {
        try {
            ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(originalUri, "r");
            new ReceiveImageFileTask(GroupChatActivity.this).execute(imageHelper,
                    BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor()), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        QBSendGroupChatMessageCommand.start(GroupChatActivity.this, null, file);
    }

    @Override
    public void onCachedImageFileReceived(File file) {
        startLoadAttachFile(file);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {

    }

    public void sendMessageOnClick(View view) {
        QBSendGroupChatMessageCommand.start(this, messageEditText.getText().toString(), null);
        messageEditText.setText(Consts.EMPTY_STRING);
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

    @Override
    protected void onResume() {
        super.onResume();
        addActions();
        startLoadDialogMessages(groupDialog, groupJid);
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