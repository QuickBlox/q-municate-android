package com.quickblox.q_municate.ui.chats;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.caching.DatabaseManager;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.model.GroupDialog;
import com.quickblox.q_municate.qb.commands.QBLeaveGroupDialogCommand;
import com.quickblox.q_municate.qb.commands.QBLoadGroupDialogCommand;
import com.quickblox.q_municate.qb.commands.QBUpdateGroupNameCommand;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.dialogs.ConfirmDialog;
import com.quickblox.q_municate.ui.friends.FriendDetailsActivity;
import com.quickblox.q_municate.ui.main.MainActivity;
import com.quickblox.q_municate.ui.profile.ProfileActivity;
import com.quickblox.q_municate.ui.uihelper.SimpleActionModeCallback;
import com.quickblox.q_municate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DialogUtils;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.ImageHelper;

import java.io.FileNotFoundException;

public class GroupDialogDetailsActivity extends BaseLogeableActivity implements AdapterView.OnItemClickListener {

    private EditText groupNameEditText;
    private TextView participantsTextView;
    private ListView friendsListView;
    private TextView onlineParticipantsTextView;
    private RoundedImageView avatarImageView;

    private String groupNameCurrent;
    private String groupNameOld;

    private String dialogId;
    private GroupDialog groupDialog;

    private Object actionMode;
    private boolean closeActionMode;
    private boolean isNeedUpdateAvatar;
    private Bitmap avatarBitmapCurrent;
    private ImageHelper imageHelper;
    private GroupDialogOccupantsAdapter groupDialogOccupantsAdapter;

    public static void start(Context context, String dialogId) {
        Intent intent = new Intent(context, GroupDialogDetailsActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_dialog_details);
        dialogId = (String) getIntent().getExtras().getSerializable(QBServiceConsts.EXTRA_DIALOG_ID);
        groupDialog = new GroupDialog(DatabaseManager.getDialogByDialogId(this, dialogId));
        imageHelper = new ImageHelper(this);
        initUI();
        initUIWithData();
        addActions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showProgress();
        QBLoadGroupDialogCommand.start(this, dialogId, groupDialog.getRoomJid());
    }

    public void changeAvatarOnClick(View view) {
        canPerformLogout.set(false);
        imageHelper.getImage();
    }

    private void initUI() {
        avatarImageView = _findViewById(R.id.avatar_imageview);
        avatarImageView.setOval(true);
        groupNameEditText = _findViewById(R.id.name_textview);
        participantsTextView = _findViewById(R.id.participants_textview);
        friendsListView = _findViewById(R.id.chat_friends_listview);
        onlineParticipantsTextView = _findViewById(R.id.online_participants_textview);
    }

    private void initUIWithData() {
        groupNameEditText.setText(groupDialog.getName());
        participantsTextView.setText(getString(R.string.gdd_participants, groupDialog.getOccupantsCount()));
        onlineParticipantsTextView.setText(getString(R.string.gdd_online_participants,
                groupDialog.getOnlineOccupantsCount(), groupDialog.getOccupantsCount()));
        updateOldUserData();
    }

    private void initListView() {
        groupDialogOccupantsAdapter = getFriendsAdapter();
        friendsListView.setAdapter(groupDialogOccupantsAdapter);
        friendsListView.setOnItemClickListener(this);
    }

    private void addActions() {
        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_SUCCESS_ACTION, new LoadGroupDialogSuccessAction());
        addAction(QBServiceConsts.LOAD_GROUP_DIALOG_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_SUCCESS_ACTION, new LeaveGroupDialogSuccessAction());
        addAction(QBServiceConsts.LEAVE_GROUP_DIALOG_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.UPDATE_GROUP_NAME_SUCCESS_ACTION, new UpdateGroupNameSuccessAction());
        addAction(QBServiceConsts.UPDATE_GROUP_NAME_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    protected GroupDialogOccupantsAdapter getFriendsAdapter() {
        return new GroupDialogOccupantsAdapter(this, groupDialog.getOccupantList());
    }

    private void showLeaveGroupDialog() {
        ConfirmDialog dialog = ConfirmDialog.newInstance(R.string.dlg_leave_group, R.string.dlg_confirm);
        dialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBLeaveGroupDialogCommand.start(GroupDialogDetailsActivity.this, groupDialog.getRoomJid());
            }
        });
        dialog.show(getFragmentManager(), null);
    }

    private void initTextChangedListeners() {
        groupNameEditText.addTextChangedListener(new GroupNameTextWatcherListener());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (actionMode != null && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            groupNameEditText.setText(groupDialog.getName());
            closeActionMode = true;
            ((ActionMode) actionMode).finish();
            return true;
        } else {
            closeActionMode = false;
        }
        return super.dispatchKeyEvent(event);
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
                AddFriendsToGroupActivity.start(this, groupDialog);
                return true;
            case R.id.action_leave:
                showLeaveGroupDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            isNeedUpdateAvatar = true;
            Uri originalUri = data.getData();
            try {
                ParcelFileDescriptor descriptor = getContentResolver().openFileDescriptor(originalUri, "r");
                avatarBitmapCurrent = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor());
            } catch (FileNotFoundException e) {
                ErrorUtils.logError(e);
            }
            ImageLoader.getInstance().displayImage(originalUri.toString(), avatarImageView,
                    Consts.UIL_AVATAR_DISPLAY_OPTIONS);
            startAction();
        }
        canPerformLogout.set(true);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startAction() {
        if (actionMode != null) {
            return;
        }
        actionMode = startActionMode(new ActionModeCallback());
    }

    private void updateCurrentUserData() {
        groupNameCurrent = groupNameEditText.getText().toString();
    }

    private void updateUserData() {
        updateCurrentUserData();
        if (isGroupDataChanged()) {
            saveChanges();
        }
    }

    private boolean isGroupDataChanged() {
        return !groupNameCurrent.equals(groupNameOld) || isNeedUpdateAvatar;
    }

    private void saveChanges() {
        if (!isUserDataCorrect()) {
            DialogUtils.showLong(this, getString(R.string.dlg_not_all_fields_entered));
            return;
        }
        showProgress();
        QBUpdateGroupNameCommand.start(this, dialogId, groupNameCurrent);
    }

    private boolean isUserDataCorrect() {
        return !TextUtils.isEmpty(groupNameCurrent);
    }

    private void updateOldUserData() {
        groupNameOld = groupNameEditText.getText().toString();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        Friend selectedFriend = groupDialogOccupantsAdapter.getItem(position);
        if(selectedFriend != null) {
            friendOnClick(selectedFriend);
        }
    }

    private void friendOnClick(Friend selectedFriend) {
        QBUser currentUser = AppSession.getSession().getUser();
        if(currentUser.getId() == selectedFriend.getId()) {
            ProfileActivity.start(GroupDialogDetailsActivity.this);
        } else {
            FriendDetailsActivity.start(GroupDialogDetailsActivity.this, selectedFriend.getId());
        }
    }

    private class GroupNameTextWatcherListener extends SimpleTextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!groupNameOld.equals(s.toString())) {
                startAction();
            }
        }
    }

    private class ActionModeCallback extends SimpleActionModeCallback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (!closeActionMode) {
                updateUserData();
            }
            actionMode = null;
        }
    }

    private class LoadGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            groupDialog = (GroupDialog) bundle.getSerializable(QBServiceConsts.EXTRA_GROUP_DIALOG);
            updateOldUserData();
            initUIWithData();
            initTextChangedListeners();
            initListView();
            hideProgress();
        }
    }

    private class LeaveGroupDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            MainActivity.start(GroupDialogDetailsActivity.this);
            finish();
        }
    }

    private class UpdateGroupNameSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            updateOldUserData();
            hideProgress();
        }
    }
}