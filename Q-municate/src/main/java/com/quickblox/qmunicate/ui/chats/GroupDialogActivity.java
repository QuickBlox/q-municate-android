package com.quickblox.qmunicate.ui.chats;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.caching.tables.DialogMessageTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBUpdateDialogCommand;
import com.quickblox.qmunicate.qb.helpers.QBMultiChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.ReceiveFileListener;
import com.quickblox.qmunicate.utils.ReceiveImageFileTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class GroupDialogActivity extends BaseDialogActivity implements ReceiveFileListener {

    private static final String EXTRA_FRIENDS = "extra_friends";
    private static final String EXTRA_ROOM_JID = "extra_room_jid";

    private String groupName;

    public GroupDialogActivity() {
        super(R.layout.activity_dialog, QBService.MULTI_CHAT_HELPER);
    }

    public static void start(Context context, ArrayList<Friend> friends) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(EXTRA_FRIENDS, friends);
        context.startActivity(intent);
    }

    public static void start(Context context, QBDialog qbDialog) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(EXTRA_ROOM_JID, qbDialog.getDialogId());
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, qbDialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(EXTRA_ROOM_JID)) {
            dialogId = getIntent().getStringExtra(EXTRA_ROOM_JID);
        }

        dialog = (QBDialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        initListView();
        startLoadDialogMessages();

        registerForContextMenu(messagesListView);
    }

    protected void addActions() {
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION, new LoadAttachFileSuccessAction());
        addAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    @Override
    protected void onUpdateChatDialog() {
        if (messagesAdapter != null && !messagesAdapter.isEmpty()) {
            startUpdateChatDialog();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    protected void removeActions() {
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOAD_ATTACH_FILE_FAIL_ACTION);
    }

    @Override
    protected void onFileSelected(Uri originalUri) {
        try {
            ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(originalUri, "r");
            Bitmap bitmap = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor(), null, bitmapOptions);
            new ReceiveImageFileTask(GroupDialogActivity.this).execute(imageHelper, bitmap, true);
        } catch (FileNotFoundException e) {
            ErrorUtils.showError(this, e.getMessage());
        } catch (OutOfMemoryError e) {
            ErrorUtils.showError(this, e.getMessage());
        }
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        try {
            ((QBMultiChatHelper) chatHelper).sendGroupMessageWithAttachImage(dialog.getRoomJid(), file);
        } catch (QBResponseException e) {
            ErrorUtils.showError(this, e);
        }
        //TODO make in command if will be low performance
        //QBSendGroupDialogMessageCommand.start(GroupDialogActivity.this, dialogId, null, file);
    }

    @Override
    protected Bundle generateBundleToInitDialog() {
        return null;
    }

    private void startUpdateChatDialog() {
        QBDialog dialog = getQBDialog();
        if (dialog != null) {
            QBUpdateDialogCommand.start(this, dialog);
        }
    }

    private QBDialog getQBDialog() {
        Cursor cursor = (Cursor) messagesAdapter.getItem(messagesAdapter.getCount() - 1);
        String lastMessage = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.BODY));
        long dateSent = cursor.getLong(cursor.getColumnIndex(DialogMessageTable.Cols.TIME));
        dialog.setLastMessage(lastMessage);
        dialog.setLastMessageDateSent(dateSent);
        dialog.setUnreadMessageCount(Consts.ZERO_INT_VALUE);
        return dialog;
    }

    private void updateChatData() {
        dialog = DatabaseManager.getDialogByDialogId(this, dialogId);
        groupName = dialog.getName();

        updateActionBar();
    }

    private void initListView() {
        messagesAdapter = new GroupDialogMessagesAdapter(this, getAllDialogMessagesByDialogId(), this, dialog);
        messagesListView.setAdapter(messagesAdapter);
    }

    private void updateActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(groupName);
        actionBar.setSubtitle(getString(R.string.gdd_participants, dialog.getOccupants().size()));
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
            ((QBMultiChatHelper) chatHelper).sendGroupMessage(dialog.getRoomJid(),
                    messageEditText.getText().toString());
        } catch (QBResponseException e) {
            ErrorUtils.showError(this, e);
        }
        //QBSendGroupDialogMessageCommand.start(this, dialog.getRoomJid(), messageEditText.getText().toString(),
        //      null); TODO make async if will be low perfomance
        messageEditText.setText(Consts.EMPTY_STRING);
        scrollListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_dialog_menu, menu);
        return true;
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
            case R.id.action_group_details:
                GroupDialogDetailsActivity.start(this, dialog.getDialogId());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater m = getMenuInflater();
        m.inflate(R.menu.group_dialog_ctx_menu, menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addActions();
        updateChatData();
        scrollListView();
    }
}