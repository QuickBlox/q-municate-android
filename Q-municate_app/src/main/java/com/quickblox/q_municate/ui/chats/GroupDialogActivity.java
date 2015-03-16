package com.quickblox.q_municate.ui.chats;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.MessagesNotificationType;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate.utils.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate_db.managers.DatabaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class GroupDialogActivity extends BaseDialogActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener {

    public GroupDialogActivity() {
        super(R.layout.activity_dialog, QBService.GROUP_CHAT_HELPER);
    }

    public static void start(Context context, ArrayList<User> friends) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friends);
        context.startActivity(intent);
    }

    public static void start(Context context, Dialog dialog) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID, dialog.getDialogId());
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(QBServiceConsts.EXTRA_ROOM_JID)) {
            dialogId = getIntent().getStringExtra(QBServiceConsts.EXTRA_ROOM_JID);
        }

        dialog = (Dialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);

        startLoadDialogMessages();
        setCurrentDialog(ChatUtils.createQBDialogFromLocalDialog(dialog));

//        registerForContextMenu(messagesListView);
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
    protected void onFileSelected(Bitmap bitmap) {
        new ReceiveFileFromBitmapTask(GroupDialogActivity.this).execute(imageUtils, bitmap, true);
    }

    @Override
    protected void onFileLoaded(QBFile file) {
        try {
            ((QBGroupChatHelper) baseChatHelper).sendGroupMessageWithAttachImage(dialog.getRoomJid(), file);
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

    @Override
    protected void initListView() {
        List<Message> messagesList = DatabaseManager.getInstance().getMessageManager().getAll();
        messagesAdapter = new GroupDialogMessagesAdapter(this, messagesList, this,
                dialog);
        messagesListView.setAdapter((StickyListHeadersAdapter) messagesAdapter);
        isNeedToScrollMessages = true;
        scrollListView();
    }

//    protected QBDialog getQBDialog() {
//        Cursor cursor = (Cursor) messagesAdapter.getItem(messagesAdapter.getCount() - 1);
//
//        MessageCache messageCache = ChatDatabaseManager.getMessageCacheFromCursor(cursor);
//        MessagesNotificationType messagesNotificationType = messageCache.getMessagesNotificationType();
//
//        if (messagesNotificationType == null) {
//            dialog.setLastMessage(messageCache.getMessage());
//        } else if (ChatNotificationUtils.isUpdateChatNotificationMessage(messagesNotificationType.getCode())) {
//            dialog.setLastMessage(resources.getString(R.string.cht_notification_message));
//        }
//
//        dialog.setLastMessageDateSent(messageCache.getTime());
//        dialog.setUnreadMessageCount(ConstsCore.ZERO_INT_VALUE);
//        return dialog;
//    }

    protected void updateActionBar() {
        actionBar.setTitle(dialog.getTitle());
//        actionBar.setSubtitle(getString(R.string.gdd_participants, dialog.getOccupants().size()));
        actionBar.setLogo(R.drawable.placeholder_group);
        if (!TextUtils.isEmpty(dialog.getPhoto())) {
            loadLogoActionBar(dialog.getPhoto());
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
        sendMessage(false);
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

//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, view, menuInfo);
//        MenuInflater m = getMenuInflater();
//        m.inflate(R.menu.group_dialog_ctx_menu, menu);
//    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDialogData();

        if (messagesAdapter != null && !messagesAdapter.isEmpty()) {
            scrollListView();
        }
    }
}