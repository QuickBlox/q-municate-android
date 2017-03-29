package com.quickblox.q_municate.ui.activities.chats;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.ChatDialogUtils;
import com.quickblox.q_municate_core.core.concurrency.BaseAsyncTask;
import com.quickblox.q_municate.ui.adapters.chats.GroupChatMessagesAdapter;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;

public class GroupDialogActivity extends BaseDialogActivity {

    private static final String TAG = GroupDialogActivity.class.getSimpleName();

    public static void start(Context context, ArrayList<QMUser> friends) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friends);
        context.startActivity(intent);
    }

    public static void start(Context context, QBChatDialog chatDialog) {
        Intent intent = new Intent(context, GroupDialogActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, chatDialog);
        context.startActivity(intent);
    }

    @Override
    protected void initMessagesRecyclerView() {
        super.initMessagesRecyclerView();
        messagesAdapter = new GroupChatMessagesAdapter(this, combinationMessagesList);
        messagesRecyclerView.addItemDecoration(
                new StickyRecyclerHeadersDecoration(messagesAdapter));
        messagesRecyclerView.setAdapter(messagesAdapter);

        scrollMessagesToBottom();
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
        if (GroupDialogDetailsActivity.UPDATE_DIALOG_REQUEST_CODE == requestCode && GroupDialogDetailsActivity.RESULT_DELETE_GROUP == resultCode) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected Bundle generateBundleToInitDialog() {
        return null;
    }

    @Override
    protected void updateMessagesList() {
        processCombinationMessages();
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
                GroupDialogDetailsActivity.start(this, currentChatDialog.getDialogId());
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void updateActionBar() {
        if (isNetworkAvailable() && currentChatDialog != null) {
            setActionBarTitle(ChatDialogUtils.getTitleForChatDialog(currentChatDialog, dataManager));
            checkActionBarLogo(currentChatDialog.getPhoto(), R.drawable.placeholder_group);
        }
    }

    @Override
    protected void initFields() {
        super.initFields();
        if (currentChatDialog != null) {
            title = ChatDialogUtils.getTitleForChatDialog(currentChatDialog, dataManager);
        }
    }

    private void processCombinationMessages(){
        if(combinationMessagesList == null){
            return;
        }
        (new BaseAsyncTask<Void, Void, Void>() {
            @Override
            public Void performInBackground(Void... params) throws Exception {
                QBUser currentUser = AppSession.getSession().getUser();
                for (CombinationMessage cm : combinationMessagesList) {
                    boolean ownMessage = !cm.isIncoming(currentUser.getId());
                    if (!State.READ.equals(cm.getState()) && !ownMessage && isNetworkAvailable()) {
                        cm.setState(State.READ);
                        QBUpdateStatusMessageCommand.start(GroupDialogActivity.this, currentChatDialog, cm, false);
                    } else if (ownMessage) {
                        cm.setState(State.READ);
                        dataManager.getMessageDataManager().update(cm.toMessage(), false);
                    }
                }
                return null;
            }

            @Override
            public void onResult(Void aVoid) {

            }

            @Override
            public void onException(Exception e) {

            }
        }).execute();
    }

    public void sendMessage(View view) {
        sendMessage();
    }
}