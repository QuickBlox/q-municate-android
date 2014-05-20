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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.filetransfer.qb.commands.QBLoadAttachFileCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBSendPrivateChatMessageCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.mediacall.CallActivity;
import com.quickblox.qmunicate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.ReceiveFileListener;
import com.quickblox.qmunicate.utils.ReceiveImageFileTask;
import com.quickblox.qmunicate.utils.ImageHelper;

import java.io.File;
import java.io.FileNotFoundException;

public class PrivateChatActivity extends BaseChatActivity implements ReceiveFileListener {

    public static final String EXTRA_OPPONENT = "opponentFriend";

    private ListView messagesListView;
    private EditText messageEditText;
    private ImageButton attachButton;
    private ImageButton sendButton;

    private BaseAdapter messagesAdapter;
    private Friend opponentFriend;
    private ImageHelper imageHelper;

    private int chatId;

    private PrivateChatActivity instance;
    private QBChatHelper chatHelper;

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
        imageHelper = new ImageHelper(this);

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
        chatHelper = QBChatHelper.getInstance();
        chatHelper.initPrivateChat(opponentFriend.getId());
    }

    protected BaseAdapter getMessagesAdapter() {
        return new PrivateChatMessagesAdapter(this, getAllPrivateChatMessages(), opponentFriend);
    }

    private Cursor getAllPrivateChatMessages() {
        return DatabaseManager.getAllPrivateChatMessagesByChatId(this, chatId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);
    }

    @Override
    public void onCachedImageFileReceived(File file) {
        startLoadAttachFile(file);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    private void startLoadAttachFile(File file) {
        showProgress();
        QBLoadAttachFileCommand.start(this, file);
    }

    public void attachButtonOnClick(View view) {
        imageHelper.getImage();
    }

    public void sendMessageOnClick(View view) {
        QBSendPrivateChatMessageCommand.start(this, messageEditText.getText().toString(), null);
        messageEditText.setText("");
        scrollListView();
    }

    private void scrollListView() {
        messagesListView.setSelection(messagesAdapter.getCount() - 1);
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
                callToUser(opponentFriend, WebRTC.MEDIA_STREAM.AUDIO);
                return true;
            case R.id.action_video_call:
                callToUser(opponentFriend, WebRTC.MEDIA_STREAM.VIDEO);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri originalUri = data.getData();
            try {
                ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(originalUri, "r");
                new ReceiveImageFileTask(PrivateChatActivity.this).execute(imageHelper,
                        BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor()), true);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scrollListView();
        addActions();
    }

    private void callToUser(Friend friend, WebRTC.MEDIA_STREAM callType) {
        if (friend.isOnline() && friend.getId() != App.getInstance().getUser().getId()) {
            QBUser qbUser = new QBUser(friend.getId());
            qbUser.setFullName(friend.getFullname());
            CallActivity.start(PrivateChatActivity.this, qbUser, callType);
        } else if (!friend.isOnline()) {
            ErrorUtils.showError(this, getString(R.string.frd_offline_user));
        }
    }

    private void addActions() {
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION, new LoadAttachFileSuccessAction());
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION, failAction);
    }

    private class LoadAttachFileSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBFile qbFile = (QBFile) bundle.getSerializable(QBServiceConsts.EXTRA_ATTACH_FILE);
            QBSendPrivateChatMessageCommand.start(PrivateChatActivity.this, null, qbFile);
            hideProgress();
            scrollListView();
        }
    }
}