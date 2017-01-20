package com.quickblox.q_municate.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.chats.GroupChatMessagesAdapter;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;

public class GroupDialogActivity extends BaseDialogActivity {

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

        initFields();

        if (dialog == null) {
            finish();
        }

        setUpActionBarWithUpButton();

        if (isNetworkAvailable()) {
            deleteTempMessages();
        }

        initMessagesRecyclerView();
    }

    @Override
    protected void initMessagesRecyclerView() {
        super.initMessagesRecyclerView();
        messagesAdapter = new GroupChatMessagesAdapter(this, combinationMessagesList, dialog);
        messagesRecyclerView.addItemDecoration(
                new StickyRecyclerHeadersDecoration((StickyRecyclerHeadersAdapter) messagesAdapter));
        messagesRecyclerView.setAdapter(messagesAdapter);

        scrollMessagesToBottom();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateData();

        if (isNetworkAvailable()) {
            startLoadDialogMessages();
        }

        checkMessageSendingPossibility();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_dialog_menu, menu);
        return true;
    }

    @Override
    protected void onConnectServiceLocally(QBService service) {
        onConnectServiceLocally();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (GroupDialogDetailsActivity.UPDATE_DIALOG_REQUEST_CODE == requestCode && GroupDialogDetailsActivity.RESULT_LEAVE_GROUP == resultCode) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onFileLoaded(QBFile file, String dialogId) {
        sendGroupMessageWithAttach(dialogId, file, null);
    }

    @Override
    protected void onLocationLoaded(String location, String dialogId) {
        Log.d("GroupDialogActivity", "location= " + location);
        sendGroupMessageWithAttach(dialogId, null, location);
    }

    private void sendGroupMessageWithAttach(String dialogId, QBFile file, String location) {
        if (!dialogId.equals(dialog.getDialogId())) {
            return;
        }
        try {
            if (file != null) {
                ((QBGroupChatHelper) baseChatHelper).sendGroupMessageWithAttachImage(dialog.getRoomJid(), file);
            } else if (!TextUtils.isEmpty(location)) {
                ((QBGroupChatHelper) baseChatHelper).sendGroupMessageWithAttachLocation(dialog.getRoomJid(), location);
            }
        } catch (QBResponseException exc) {
            ErrorUtils.showError(this, exc);
        }
    }
    @Override
    protected Bundle generateBundleToInitDialog() {
        return null;
    }

    @Override
    protected void updateMessagesList() {
        int oldMessagesCount = messagesAdapter.getItemCount();

        this.combinationMessagesList = createCombinationMessagesList();
        processCombinationMessages();
        messagesAdapter.addList(combinationMessagesList);

        checkForScrolling(oldMessagesCount);
    }

    @Override
    protected void checkMessageSendingPossibility() {
        checkMessageSendingPossibility(isNetworkAvailable());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_group_details:
                GroupDialogDetailsActivity.start(this, dialog.getDialogId());
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void updateActionBar() {
        if (isNetworkAvailable() && dialog != null) {
            setActionBarTitle(dialog.getTitle());
            checkActionBarLogo(dialog.getPhoto(), R.drawable.placeholder_group);
        }
    }

    private void initFields() {
        chatHelperIdentifier = QBService.GROUP_CHAT_HELPER;
        dialog = (Dialog) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG);
        combinationMessagesList = createCombinationMessagesList();
        if (dialog != null)
        title = dialog.getTitle();
    }

    private void processCombinationMessages(){
        QBUser currentUser = AppSession.getSession().getUser();
        for (Object cmb :combinationMessagesList){
            CombinationMessage cm = (CombinationMessage)cmb;
            boolean ownMessage = !cm.isIncoming(currentUser.getId());
            if (!State.READ.equals(cm.getState()) && !ownMessage && isNetworkAvailable()) {
                cm.setState(State.READ);
                QBUpdateStatusMessageCommand.start(this, ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog), cm, false);
            } else if (ownMessage) {
                cm.setState(State.READ);
                dataManager.getMessageDataManager().update(cm.toMessage(), false);
            }
        }
    }

    public void sendMessage(View view) {
        sendMessage(false);
    }
}