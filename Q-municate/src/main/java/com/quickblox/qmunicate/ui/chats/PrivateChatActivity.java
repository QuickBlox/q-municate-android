package com.quickblox.qmunicate.ui.chats;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBSendPrivateChatMessageCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.qmunicate.utils.DialogUtils;

public class PrivateChatActivity extends BaseChatActivity {

    public static final String EXTRA_OPPONENT = "opponentFriend";

    private ListView messagesListView;
    private EditText messageEditText;
    private ImageButton attachButton;
    private ImageButton sendButton;

    private BaseAdapter messagesAdapter;
    private Friend opponentFriend;

    private int chatId;

    private PrivateChatActivity instance;
    private QBChatHelper qbChatHelper;

    public PrivateChatActivity() {
        super(R.layout.activity_private_chat);
    }

    public static void start(Context context, Friend opponent) {
        Intent intent = new Intent(context, PrivateChatActivity.class);
        intent.putExtra(EXTRA_OPPONENT, opponent);
        context.startActivity(intent);
    }

    public PrivateChatActivity getInstance() {
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        instance = this;
        opponentFriend = (Friend) getIntent().getExtras().getSerializable(EXTRA_OPPONENT);
        chatId = opponentFriend.getId();

        initUI();
        initListView();
        initListeners();
        initActionBar();
        initChat();
    }

    private void initUI() {
        messagesListView = _findViewById(R.id.messages_listview);
        messageEditText = _findViewById(R.id.message_edittext);
        attachButton = _findViewById(R.id.attach_button);
        sendButton = _findViewById(R.id.send_button);
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
        ab.setTitle(opponentFriend.getFullname());
        ab.setSubtitle(opponentFriend.getOnlineStatus());
    }

    private void initChat() {
        qbChatHelper = QBChatHelper.getInstance();
        qbChatHelper.initPrivateChat(opponentFriend.getId());
    }

    protected BaseAdapter getMessagesAdapter() {
        return new PrivateChatMessagesAdapter(this, getAllPrivateChatMessages(), opponentFriend);
    }

    private Cursor getAllPrivateChatMessages() {
        return DatabaseManager.getAllPrivateChatMessagesBySenderId(this, chatId);
    }

    public void sendMessageOnClick(View view) {
        QBSendPrivateChatMessageCommand.start(this, messageEditText.getText().toString());
        messageEditText.setText("");
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

    @Override
    protected void onResume() {
        super.onResume();
        messagesListView.setSelection(messagesAdapter.getCount() - 1);
    }
}