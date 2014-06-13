package com.quickblox.qmunicate.ui.chats;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.caching.tables.DialogMessageTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBCreatePrivateChatCommand;
import com.quickblox.qmunicate.qb.commands.QBSendPrivateChatMessageCommand;
import com.quickblox.qmunicate.qb.commands.QBUpdateDialogCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.mediacall.CallActivity;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ReceiveFileListener;
import com.quickblox.qmunicate.utils.ReceiveImageFileTask;

import java.io.File;
import java.io.FileNotFoundException;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class PrivateDialogActivity extends BaseDialogActivity implements ReceiveFileListener {

    private Friend opponentFriend;
    private QBDialog dialog;

    public PrivateDialogActivity() {
        super(R.layout.activity_dialog);
    }

    public static void start(Context context, Friend opponent, QBDialog dialog) {
        Intent intent = new Intent(context, PrivateDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENT, opponent);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        opponentFriend = (Friend) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_OPPONENT);
        dialog = (QBDialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        chatJidId = opponentFriend.getId() + Consts.EMPTY_STRING;

        if (dialog == null) {
            dialog = getDialogByRoomJidId();
        }

        initListView();
        initActionBar();
        initChat();
        initStartLoadDialogMessages();
    }

    private QBDialog getDialogByRoomJidId() {
        return DatabaseManager.getDialogByRoomJidId(this, chatJidId);
    }

    private void createTempDialog() {
        DatabaseManager.createTempPrivateDialogByRoomJidId(this, chatJidId);
    }

    private void initStartLoadDialogMessages() {
        if (dialog != null && messagesAdapter.isEmpty()) {
            startLoadDialogMessages(dialog, chatJidId, Consts.ZERO_LONG_VALUE);
        } else if (dialog != null && !messagesAdapter.isEmpty()) {
            startLoadDialogMessages(dialog, chatJidId, dialog.getLastMessageDateSent());
        } else {
            createTempDialog();
        }
    }

    @Override
    protected void onUpdateChatDialog() {
        if (!messagesAdapter.isEmpty()) {
            startUpdateChatDialog();
        }
    }

    @Override
    protected void onFileSelected(Uri originalUri) {
        try {
            ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(originalUri, "r");
            new ReceiveImageFileTask(PrivateDialogActivity.this).execute(imageHelper,
                    BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor()), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        QBSendPrivateChatMessageCommand.start(PrivateDialogActivity.this, null, file);
        scrollListView();
    }

    private void startUpdateChatDialog() {
        if (dialog != null) {
            QBUpdateDialogCommand.start(this, getDialog(), chatJidId);
        }
    }

    private QBDialog getDialog() {
        Cursor cursor = (Cursor) messagesAdapter.getItem(messagesAdapter.getCount() - 1);
        String lastMessage = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.BODY));
        dialog.setLastMessage(lastMessage);
        dialog.setUnreadMessageCount(Consts.ZERO_INT_VALUE);
        return dialog;
    }

    private void initListView() {
        messagesAdapter = new PrivateDialogMessagesAdapter(this, getAllDialogMessagesByRoomJidId(), opponentFriend, dialog, this);
        messagesListView.setAdapter(messagesAdapter);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(opponentFriend.getFullname());
        actionBar.setSubtitle(opponentFriend.getOnlineStatus());
    }

    private void initChat() {
        QBCreatePrivateChatCommand.start(this, opponentFriend);
    }

    private Cursor getAllDialogMessagesByRoomJidId() {
        return DatabaseManager.getAllDialogMessagesByRoomJidId(this, chatJidId);
    }

    @Override
    public void onCachedImageFileReceived(File file) {
        startLoadAttachFile(file);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    public void sendMessageOnClick(View view) {
        QBSendPrivateChatMessageCommand.start(this, messageEditText.getText().toString(), null);
        messageEditText.setText(Consts.EMPTY_STRING);
        scrollListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.private_dialog_menu, menu);
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

    private void callToUser(Friend friend, WebRTC.MEDIA_STREAM callType) {
        if (friend.getId() != App.getInstance().getUser().getId()) {
            CallActivity.start(PrivateDialogActivity.this, friend, callType);
        }
    }

    public String getCurrentOpponent() {
        return currentOpponent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        scrollListView();
        currentOpponent = opponentFriend.getFullname();
    }

    @Override
    protected void onPause() {
        super.onPause();
        currentOpponent = null;
        Crouton.cancelAllCroutons();
    }
}