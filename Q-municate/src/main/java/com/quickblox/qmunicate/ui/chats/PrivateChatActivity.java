package com.quickblox.qmunicate.ui.chats;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBPrivateChatManager;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.ChatMessage;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.qmunicate.utils.DialogUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import java.io.IOException;
import java.util.Date;

public class PrivateChatActivity extends BaseActivity implements QBMessageListener<QBPrivateChat>, QBPrivateChatManagerListener {

    public static final String EXTRA_OPPONENT = "opponentFriend";

    private ListView messagesListView;
    private EditText messageEditText;
    private ImageButton attachButton;
    private ImageButton sendButton;

    private BaseAdapter messagesAdapter;
    private Friend opponentFriend;
    private QBPrivateChat qbPrivateChat;

    public static void start(Context context, Friend opponent) {
        Intent intent = new Intent(context, PrivateChatActivity.class);
        intent.putExtra(EXTRA_OPPONENT, opponent);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        opponentFriend = (Friend) getIntent().getExtras().getSerializable(EXTRA_OPPONENT);

        initUI();
        initListView();
        initListeners();
        initActionBar();
        initChat();
    }

    private void initUI() {
        messagesListView = _findViewById(R.id.messagesListView);
        messageEditText = _findViewById(R.id.messageEditText);
        attachButton = _findViewById(R.id.attachButton);
        sendButton = _findViewById(R.id.sendButton);
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

    private void initActionBar() {
        ActionBar ab = getActionBar();
        ab.setTitle(opponentFriend.getEmail());
        ab.setSubtitle(opponentFriend.getOnlineStatus());
    }

    private void initChat() {
        // Step 1: Initialize Chat Module
        QBChatService qbChatService;
        QBPrivateChatManager qbPrivateChatManager;

        // Step 2: Login
        qbChatService = QBChatService.getInstance();
        try {
            qbChatService.login(App.getInstance().getUser());
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        }

        // Step 3: Create Chat
        qbPrivateChatManager = qbChatService.getPrivateChatManager();
        qbPrivateChatManager.addPrivateChatManagerListener(this);
        qbPrivateChat = qbPrivateChatManager.createChat(opponentFriend.getId(), this);
    }

    protected BaseAdapter getMessagesAdapter() {
        Cursor cursor = getAllPrivateChatMessagesBySenderId();
        if(cursor != null) {
            return new PrivateChatMessagesAdapter(this, getAllPrivateChatMessagesBySenderId());
        } else {
            return null;
        }
    }

    private Cursor getAllPrivateChatMessagesBySenderId() {
        return DatabaseManager.getAllPrivateChatMessagesBySenderId(this, opponentFriend.getId());
    }

    public void sendMessageOnClick(View view) {
        Message message = getMessage();
        try {
            qbPrivateChat.sendMessage(message);
            saveMessageToCache(message);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private Message getMessage() {
        Message message = new Message();
        message.setBody(messageEditText.getText().toString());
        message.setSubject(App.getInstance().getUser().getEmail());
        return message;
    }

    private void saveMessageToCache(Message message) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSubject(message.getSubject());
        chatMessage.setBody(message.getBody());
        chatMessage.setSenderName(opponentFriend.getEmail());
        chatMessage.setSenderId(opponentFriend.getId());
        chatMessage.setTime(new Date(System.currentTimeMillis()));
        DatabaseManager.savePrivateChatMessage(this, chatMessage);
    }

    @Override
    public void processMessage(QBPrivateChat qbPrivateChat, Message message) {
        saveMessageToCache(message);
    }

    @Override
    public void chatCreated(QBPrivateChat qbPrivateChat, boolean createdLocally) {
        if (createdLocally) {
            // createdLocally = true
            // мы сами создали этот чат
        } else {
            // createdLocally = false
            // чат создал кто-то удаленно и нам пришло сообщение
            // нужно добавить слушателя и первое сообщение получим в него же
            this.qbPrivateChat.addMessageListener(PrivateChatActivity.this);
        }
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