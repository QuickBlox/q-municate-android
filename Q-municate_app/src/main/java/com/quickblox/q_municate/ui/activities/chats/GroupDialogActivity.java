package com.quickblox.q_municate.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.chats.GroupDialogMessagesAdapter;
import com.quickblox.q_municate.utils.ReceiveFileFromBitmapTask;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.User;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class GroupDialogActivity extends BaseDialogActivity implements ReceiveFileFromBitmapTask.ReceiveFileListener {

    private Dialog currentDialog;

    public static void start(Context context, ArrayList<User> friends) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friends);
        context.startActivity(intent);
    }

    public static void start(Context context, Dialog dialog) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = (Dialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        if (dialog == null) {
            finish();
        }

        deleteTempMessages();
        startLoadDialogMessages();

        initListView();

        //        registerForContextMenu(messagesListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData();

        if (messagesAdapter != null && !messagesAdapter.isEmpty()) {
            scrollListView();
        }
    }

    protected void updateActionBar() {
        setActionBarTitle(dialog.getTitle());

        if (!TextUtils.isEmpty(dialog.getPhoto())) {
            loadLogoActionBar(dialog.getPhoto());
        } else {
            setActionBarIcon(R.drawable.placeholder_group);
        }
    }

    @Override
    protected void onConnectServiceLocally(QBService service) {
        onConnectServiceLocally(QBService.GROUP_CHAT_HELPER);
    }

    @Override
    protected void onUpdateChatDialog() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GroupDialogDetailsActivity.UPDATE_DIALOG_REQUEST_CODE == requestCode && GroupDialogDetailsActivity.RESULT_LEAVE_GROUP == resultCode) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
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
    protected Bundle generateBundleToInitDialog() {
        return null;
    }

    @Override
    protected void initListView() {
        List<CombinationMessage> combinationMessagesList = createCombinationMessagesList();
        messagesAdapter = new GroupDialogMessagesAdapter(this, combinationMessagesList, this, dialog);
        messagesListView.setAdapter((StickyListHeadersAdapter) messagesAdapter);

        scrollListView();
    }

    @Override
    protected void updateMessagesList() {
        List<CombinationMessage> combinationMessagesList = createCombinationMessagesList();
        messagesAdapter.setNewData(combinationMessagesList);
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

    //    @Override
    //    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
    //        super.onCreateContextMenu(menu, view, menuInfo);
    //        MenuInflater m = getMenuInflater();
    //        m.inflate(R.menu.group_dialog_ctx_menu, menu);
    //    }

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
}