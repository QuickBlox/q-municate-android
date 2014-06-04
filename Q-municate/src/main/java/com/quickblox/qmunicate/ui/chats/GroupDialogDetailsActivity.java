package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.GroupDialog;
import com.quickblox.qmunicate.qb.commands.QBLeaveGroupDialogCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadGroupDialogCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.dialogs.ConfirmDialog;

import java.util.Collections;
import java.util.Comparator;

public class GroupDialogDetailsActivity extends BaseActivity {

    private TextView nameTextView;
    private TextView participantsTextView;
    private ListView friendsListView;
    private TextView onlineParticipantsTextView;

    private String jid;
    private GroupDialog dialog;

    private GroupDialogOccupantsAdapter friendsAdapter;

    public static void start(Context context, String jid) {
        Intent intent = new Intent(context, GroupDialogDetailsActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID_ID, jid);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_dialog_details);
        jid = (String) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_ROOM_JID_ID);
        dialog = new GroupDialog(DatabaseManager.getDialogByRoomJidId(this, jid));

        initUI();
        initUIWithData();
        addActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgress();
        QBLoadGroupDialogCommand.start(this, jid);
    }

    private void initUI() {
        nameTextView = _findViewById(R.id.name_textview);
        participantsTextView = _findViewById(R.id.participants_textview);
        friendsListView = _findViewById(R.id.chat_friends_listview);
        onlineParticipantsTextView = _findViewById(R.id.online_participants_textview);
    }

    private void initUIWithData() {
        nameTextView.setText(dialog.getName());
        participantsTextView.setText(getString(R.string.gdd_participants, dialog.getOccupantsCount()));
        onlineParticipantsTextView.setText(getString(R.string.gdd_online_participants,
                dialog.getOnlineOccupantsCount(), dialog.getOccupantsCount()));
    }

    private void initListView() {
        friendsAdapter = getFriendsAdapter();
        friendsListView.setAdapter(friendsAdapter);
    }

    private void addActions() {
        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_SUCCESS_ACTION, new LoadGroupDialogSuccessAction());
        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    private void updateFriendListAdapter() {
        Collections.sort(dialog.getOccupantList(), new SimpleComparator());
        friendsAdapter.notifyDataSetChanged();
    }

    protected GroupDialogOccupantsAdapter getFriendsAdapter() {
        return new GroupDialogOccupantsAdapter(this, dialog.getOccupantList());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_dialog_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            case R.id.action_add:
                AddFriendsToGroupActivity.start(this, dialog);
                return true;
            case R.id.action_leave:
                leaveGroup();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void leaveGroup() {
        ConfirmDialog dialog = ConfirmDialog.newInstance(R.string.dlg_leave_group, R.string.dlg_confirm);
        dialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBLeaveGroupDialogCommand.start(GroupDialogDetailsActivity.this, jid);
            }
        });
        dialog.show(getFragmentManager(), null);
    }

    private class SimpleComparator implements Comparator<Friend> {

        public int compare(Friend friend1, Friend friend2) {
            return (friend1.getEmail()).compareTo(friend2.getEmail());
        }
    }

    private class LoadGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            dialog = (GroupDialog) bundle.getSerializable(QBServiceConsts.EXTRA_GROUP_DIALOG);

            initUIWithData();
            initListView();
            hideProgress();
        }
    }

    private class LeaveGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            // TODO navigate to chat list
        }
    }
}