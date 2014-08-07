package com.quickblox.q_municate.ui.chats;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.caching.tables.MessageTable;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.qb.commands.QBUpdateDialogCommand;
import com.quickblox.q_municate.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.mediacall.CallActivity;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.ReceiveFileListener;
import com.quickblox.q_municate.utils.ReceiveImageFileTask;

import java.io.File;
import java.io.FileNotFoundException;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class PrivateDialogActivity extends BaseDialogActivity implements ReceiveFileListener {

    private Friend opponentFriend;

    public PrivateDialogActivity() {
        super(R.layout.activity_dialog, QBService.PRIVATE_CHAT_HELPER);
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
        dialogId = dialog.getDialogId();

        initListView();
        initActionBar();
        startLoadDialogMessages();
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
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor(), null, bitmapOptions);
            new ReceiveImageFileTask(PrivateDialogActivity.this).execute(imageHelper, bitmap, true);
        } catch (FileNotFoundException e) {
            ErrorUtils.showError(this, e.getMessage());
        }
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        try {
            ((QBPrivateChatHelper) chatHelper).sendPrivateMessageWithAttachImage(file,
                    opponentFriend.getId());
        } catch (QBResponseException exc) {
            ErrorUtils.showError(this, exc);
        }
        //TODO call in command if it should be async
        //QBSendPrivateChatMessageCommand.start(PrivateDialogActivity.this, null, opponentFriend.getId(), file);
        scrollListView();
    }

    private void startUpdateChatDialog() {
        if (dialog != null) {
            QBUpdateDialogCommand.start(this, getDialog());
        }
    }

    private QBDialog getDialog() {
        Cursor cursor = (Cursor) messagesAdapter.getItem(messagesAdapter.getCount() - 1);
        String lastMessage = cursor.getString(cursor.getColumnIndex(MessageTable.Cols.BODY));
        Integer senderId = cursor.getInt(cursor.getColumnIndex(MessageTable.Cols.SENDER_ID));
        dialog.setLastMessage(lastMessage);
        dialog.setLastMessageDateSent(DateUtils.getCurrentTime());
        dialog.setUnreadMessageCount(Consts.ZERO_INT_VALUE);
        dialog.setLastMessageUserId(senderId);
        dialog.setType(QBDialogType.PRIVATE);
        return dialog;
    }

    private void initListView() {
        messagesAdapter = new PrivateDialogMessagesAdapter(this, getAllDialogMessagesByDialogId(), this, dialog);
        messagesListView.setAdapter(messagesAdapter);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(opponentFriend.getFullname());
        actionBar.setSubtitle(opponentFriend.getOnlineStatus());
        initColorsActionBar();
    }

    @Override
    public void onCachedImageFileReceived(File file) {
        startLoadAttachFile(file);
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
    }

    public void sendMessageOnClick(View view) {
        try {
            ((QBPrivateChatHelper) chatHelper).sendPrivateMessage(messageEditText.getText().toString(),
                    opponentFriend.getId());
        } catch (QBResponseException exc) {
            ErrorUtils.showError(this, exc);
        }
        //TODO call in command if it should be async
        /*QBSendPrivateChatMessageCommand.start(this, messageEditText.getText().toString(),
                opponentFriend.getId(), null);*/
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
    protected Bundle generateBundleToInitDialog() {
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, opponentFriend.getId());
        return bundle;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            case R.id.action_attach:
                attachButtonOnClick();
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
        if (friend.getId() != AppSession.getSession().getUser().getId()) {
            CallActivity.start(PrivateDialogActivity.this, friend, callType);
        }
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