package com.quickblox.q_municate.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.chats.GroupDialogMessagesAdapter;
import com.quickblox.q_municate_core.core.concurrency.BaseAsyncTask;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;

public class GroupDialogActivity extends BaseDialogActivity {

    public static void start(Context context, ArrayList<QMUser> friends) {
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
        messagesAdapter = new GroupDialogMessagesAdapter(this, combinationMessagesList, this, dialog);
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
            startLoadDialogMessages(false);
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
        if(!dialogId.equals(dialog.getDialogId())){
            return;
        }

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
    protected void updateMessagesList() {
        final int oldMessagesCount = messagesAdapter.getAllItems().size();
        (new BaseAsyncTask<Void, Void, Boolean>() {
            @Override
            public Boolean performInBackground(Void... params) throws Exception {
                combinationMessagesList = createCombinationMessagesList();
                processCombinationMessages();
                return true;
            }

            @Override
            public void onResult(Boolean aBoolean) {
                messagesAdapter.setList(combinationMessagesList);
                checkForScrolling(oldMessagesCount);
            }

            @Override
            public void onException(Exception e) {
                ErrorUtils.showError(GroupDialogActivity.this, e);
            }

        }).execute();
    }

    @Override
    protected void additionalActionsAfterLoadMessages() {
        processCombinationMessages();
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
        for (CombinationMessage cm :combinationMessagesList){
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