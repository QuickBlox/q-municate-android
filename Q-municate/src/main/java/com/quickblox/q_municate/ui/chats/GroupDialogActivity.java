package com.quickblox.q_municate.ui.chats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.db.tables.MessageTable;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.qb.commands.QBUpdateDialogCommand;
import com.quickblox.q_municate.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.ReceiveFileFromBitmapTask;

import java.io.File;
import java.util.ArrayList;

public class GroupDialogActivity extends BaseDialogActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener {

    private String groupName;

    public GroupDialogActivity() {
        super(R.layout.activity_dialog, QBService.MULTI_CHAT_HELPER);
    }

    public static void start(Context context, ArrayList<User> friends) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friends);
        context.startActivity(intent);
    }

    public static void start(Context context, QBDialog qbDialog) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID, qbDialog.getDialogId());
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, qbDialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(QBServiceConsts.EXTRA_ROOM_JID)) {
            dialogId = getIntent().getStringExtra(QBServiceConsts.EXTRA_ROOM_JID);
        }

        dialog = (QBDialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        initListView();
        startLoadDialogMessages();

        registerForContextMenu(messagesListView);
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
        Bitmap bitmap = imageUtils.getBitmap(originalUri);
        new ReceiveFileFromBitmapTask(GroupDialogActivity.this).execute(imageUtils, bitmap, true);
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        try {
            ((QBMultiChatHelper) chatHelper).sendGroupMessageWithAttachImage(dialog.getRoomJid(), file);
        } catch (QBResponseException e) {
            ErrorUtils.showError(this, e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GroupDialogDetailsActivity.UPDATE_DIALOG_REQUEST_CODE == requestCode &&
                GroupDialogDetailsActivity.RESULT_LEAVE_GROUP == resultCode) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
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
        String lastMessage = cursor.getString(cursor.getColumnIndex(MessageTable.Cols.BODY));
        long dateSent = cursor.getLong(cursor.getColumnIndex(MessageTable.Cols.TIME));
        dialog.setLastMessage(lastMessage);
        dialog.setLastMessageDateSent(dateSent);
        dialog.setUnreadMessageCount(Consts.ZERO_INT_VALUE);
        return dialog;
    }

    private void updateChatData() {
        dialog = DatabaseManager.getDialogByDialogId(this, dialogId);
        if (dialog != null) {
            groupName = dialog.getName();
            updateActionBar();
        }
    }

    private void initListView() {
        messagesAdapter = new GroupDialogMessagesAdapter(this, getAllDialogMessagesByDialogId(), this,
                dialog);
        messagesListView.setAdapter(messagesAdapter);
    }

    private void updateActionBar() {
        actionBar.setTitle(groupName);
        actionBar.setSubtitle(getString(R.string.gdd_participants, dialog.getOccupants().size()));
        actionBar.setLogo(R.drawable.placeholder_group);
        if (!TextUtils.isEmpty(dialog.getPhotoUrl())) {
            loadLogoActionBar(dialog.getPhotoUrl());
        }
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
        updateChatData();
        scrollListView();
    }
}