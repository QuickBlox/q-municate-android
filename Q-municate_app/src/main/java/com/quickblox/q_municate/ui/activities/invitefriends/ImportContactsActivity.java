package com.quickblox.q_municate.ui.activities.invitefriends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.invitefriends.ImportFriendAdapter;
import com.quickblox.q_municate.utils.DialogsUtils;
import com.quickblox.q_municate.utils.helpers.ImportContactsHelper;
import com.quickblox.q_municate.utils.listeners.CounterChangedListener;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.adapters.invitefriends.InviteFriendsAdapter;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.helpers.EmailHelper;
import com.quickblox.q_municate.utils.listeners.UserOperationListener;
import com.quickblox.q_municate.utils.listeners.simple.SimpleActionModeCallback;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate_core.qb.commands.friend.QBAddFriendCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate.utils.helpers.SystemPermissionHelper;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class ImportContactsActivity extends BaseLoggableActivity implements CounterChangedListener {

    @Bind(R.id.friends_listview)
    RecyclerView friendsListView;

    private List<InviteFriend> friendsContactsList;
    private ImportFriendAdapter friendsAdapter;
    private ActionMode actionMode;
    private SystemPermissionHelper systemPermissionHelper;
    private ImportContactsHelper importContactsHelper;
    private ImportContactsSuccessAction importContactsSuccessAction;
    private ImportContactsFailAction importContactsFailAction;
    private AddContactsToFriendSuccessAction addContactsToFriendSuccessAction;
    private AddContactsToFriendFailAction addContactsToFriendFailAction;

    public static void start(Context context) {
        Intent intent = new Intent(context, ImportContactsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_invite_friends;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();
        addActions();
    }

    private void initFields() {
        title = getString(R.string.import_contacts_title);
        systemPermissionHelper = new SystemPermissionHelper(this);
        importContactsHelper = new ImportContactsHelper(this);
        importContactsSuccessAction = new ImportContactsSuccessAction();
        importContactsFailAction = new ImportContactsFailAction();
        addContactsToFriendSuccessAction = new AddContactsToFriendSuccessAction();
        addContactsToFriendFailAction = new AddContactsToFriendFailAction();
        friendsContactsList = new ArrayList<>();
    }

    private void addActions(){
        addAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION, importContactsSuccessAction);
        addAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION, importContactsFailAction);

        addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, addContactsToFriendSuccessAction);
        addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, addContactsToFriendFailAction);
    }

    protected void removeActions() {
        removeAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION);
        removeAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION);

        removeAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION);
        removeAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissionsAndInitFriendsListIfPossible();
    }

    private void checkPermissionsAndInitFriendsListIfPossible() {
        if (systemPermissionHelper.isAllPermissionsGrantedForImportFriends()){
            initImportContactsTask();
        } else {
            systemPermissionHelper.requestPermissionsForImportFriends();
        }
    }

    private void initImportContactsTask() {
        showProgress();
        friendsContactsList.addAll(EmailHelper.getContactsWithPhone(this));
        friendsContactsList.addAll(EmailHelper.getContactsWithEmail(this));
        importContactsHelper.startGetFriendsListTask(false);
    }

    @Override
    public void onCounterContactsChanged(int valueCounterContacts) {
        if (actionMode != null){
            actionMode.invalidate();
        }

        if (valueCounterContacts != ConstsCore.ZERO_INT_VALUE) {
            startActionMode();
        } else {
            stopActionMode();
        }
    }

    private void startActionMode() {
        if (actionMode != null) {
            return;
        }
        actionMode = startSupportActionMode(new ActionModeCallback());
        friendsAdapter.notifyDataSetChanged();
    }

    private void stopActionMode() {
        if (actionMode != null) {
            actionMode.finish();
            friendsAdapter.notifyDataSetChanged();
        }
    }

    private void checkAllContacts() {
        friendsAdapter.selectAllFriends();
    }

    private void addContactsToFriends(){
        showProgress();
        QBAddFriendCommand.start(this, friendsAdapter.getSelectedFriends());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SystemPermissionHelper.PERMISSIONS_FOR_IMPORT_FRIENDS_REQUEST: {
                if (grantResults.length > 0) {
                    if (systemPermissionHelper.isAllPermissionsGrantedForImportFriends()){
                        initImportContactsTask();
                    } else {
                        showPermissionSettingsDialog();
                    }
                }
            }
        }
    }

    private void showPermissionSettingsDialog() {
        DialogsUtils.showOpenAppSettingsDialog(
                getSupportFragmentManager(),
                getString(R.string.dlg_need_permission_read_contacts, getString(R.string.app_name)),
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        finish();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        SystemPermissionHelper.openSystemSettings(ImportContactsActivity.this);
                    }
                });
    }


    private void initFriendsList(List<QBUser> realQbFriendsList) {
        friendsAdapter = new ImportFriendAdapter(this, prepareFriendsListFromQbUsers(realQbFriendsList), new UserOperationListener() {
            @Override
            public void onAddUserClicked(int userId) {

            }
        });
        friendsListView.setLayoutManager(new LinearLayoutManager(this));
        friendsAdapter.setCounterChangedListener(this);
        friendsListView.setAdapter(friendsAdapter);
    }

    private List<InviteFriend> prepareFriendsListFromQbUsers(List<QBUser> realQbFriendsList) {
        List<InviteFriend> realFriendsList = new ArrayList<>(realQbFriendsList.size());

        for (QBUser qbUser : realQbFriendsList){
            InviteFriend inviteFriend = null;

            if (qbUser.getPhone() != null){
                inviteFriend = getContactById(qbUser.getPhone(), friendsContactsList);
            } else if (qbUser.getEmail() != null){
                inviteFriend = getContactById(qbUser.getEmail(), friendsContactsList);
            }

            if (inviteFriend != null) {
                inviteFriend.setQbId(qbUser.getId());
                inviteFriend.setQbName(qbUser.getFullName());
                inviteFriend.setQbAvatarUrl(Utils.customDataToObject(qbUser.getCustomData()).getAvatarUrl());
                realFriendsList.add(inviteFriend);
            }
        }

        return realFriendsList;
    }


    //TODO VT need move to utils-class
    private InviteFriend getContactById(String inviteFriendId, List<InviteFriend> sourceList) {
        for (InviteFriend inviteFriend : sourceList){
            if (inviteFriend.getId().equals(inviteFriendId)){
                return inviteFriend;
            }
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    private class ActionModeCallback extends SimpleActionModeCallback {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            //TODO VT need add updating action mode after getting designer's screens
            return super.onPrepareActionMode(mode, menu);
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.invite_friends_menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_send:
                    if (checkNetworkAvailableWithError()) {
                        addContactsToFriends();
                        actionMode.finish();
                    }
                    return true;
                case R.id.action_select_all:
                    checkAllContacts();
                    return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
        }
    }


    private class ImportContactsSuccessAction implements Command{

        @Override
        public void execute(Bundle bundle) throws Exception {
            hideProgress();
            List<QBUser> realQbFriendsList = (List<QBUser>) bundle.getSerializable(QBServiceConsts.EXTRA_FRIENDS);
            initFriendsList(realQbFriendsList);
        }
    }

    private class ImportContactsFailAction implements Command{

        @Override
        public void execute(Bundle bundle) throws Exception {
            hideProgress();
        }
    }

    private class AddContactsToFriendSuccessAction implements Command{

        @Override
        public void execute(Bundle bundle) throws Exception {

        }
    }

    private class AddContactsToFriendFailAction implements Command{

        @Override
        public void execute(Bundle bundle) throws Exception {

        }
    }
}