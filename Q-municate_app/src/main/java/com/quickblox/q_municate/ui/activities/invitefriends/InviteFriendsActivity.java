package com.quickblox.q_municate.ui.activities.invitefriends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.DialogsUtils;
import com.quickblox.q_municate.utils.listeners.CounterChangedListener;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.adapters.invitefriends.InviteFriendsAdapter;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.helpers.EmailHelper;
import com.quickblox.q_municate.utils.listeners.simple.SimpleActionModeCallback;
import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate.utils.helpers.SystemPermissionHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

public class InviteFriendsActivity extends BaseLoggableActivity implements CounterChangedListener {

    @Bind(R.id.friends_listview)
    ListView friendsListView;

    private List<InviteFriend> friendsContactsList;
    private InviteFriendsAdapter friendsAdapter;
    private String[] selectedContactsFriendsArray;
    private ActionMode actionMode;
    private boolean allContactsChecked;
    private SystemPermissionHelper systemPermissionHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, InviteFriendsActivity.class);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissionsAndInitFriendsListIfPossible();
    }

    private void checkPermissionsAndInitFriendsListIfPossible() {
        if (systemPermissionHelper.isAllPermissionsGrantedForImportFriends()){
            initFriendsList();
        } else {
            systemPermissionHelper.requestPermissionsForImportFriends();
        }
    }

    @Override
    public void onCounterContactsChanged(int valueCounterContacts) {
        if (valueCounterContacts != ConstsCore.ZERO_INT_VALUE) {
            startActionMode();
        } else {
            stopActionMode();
        }
    }

    private void initFields() {
        title = getString(R.string.invite_friends_title);
        systemPermissionHelper = new SystemPermissionHelper(this);
    }

    private void initFriendsList() {
        friendsContactsList = EmailHelper.getContactsWithEmail(this);
        friendsAdapter = new InviteFriendsAdapter(this, friendsContactsList);
        friendsAdapter.setCounterChangedListener(this);
        friendsListView.setAdapter(friendsAdapter);
    }

    private void checkAllContacts() {
        allContactsChecked = !allContactsChecked;
        friendsAdapter.setCounterContacts(getCheckedFriends(friendsContactsList, allContactsChecked));
        friendsAdapter.notifyDataSetChanged();
    }

    private int getCheckedFriends(List<InviteFriend> friends, boolean isCheck) {
        int newCounter;
        for (InviteFriend friend : friends) {
            friend.setSelected(isCheck);
        }
        newCounter = isCheck ? friends.size() : ConstsCore.ZERO_INT_VALUE;

        onCounterContactsChanged(newCounter);

        return newCounter;
    }

    private void startActionMode() {
        if (actionMode != null) {
            return;
        }
        actionMode = startSupportActionMode(new ActionModeCallback());
    }

    private void stopActionMode() {
        if (actionMode != null) {
            actionMode.finish();
        }
    }

    private void performActionNext() {
        selectedContactsFriendsArray = getSelectedFriendsForInvite();

        if (selectedContactsFriendsArray.length > ConstsCore.ZERO_INT_VALUE) {
            sendInviteToContacts();
        } else {
            ToastUtils.longToast(R.string.dlg_no_friends_selected);
        }

        clearCheckedFriends();
    }

    private String[] getSelectedFriendsForInvite() {
        List<String> arrayList = new ArrayList<String>();
        for (InviteFriend friend : friendsContactsList) {
            if (friend.isSelected()) {
                arrayList.add(friend.getId());
            }
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

    private void clearCheckedFriends() {
        for (InviteFriend friend : friendsContactsList) {
            friend.setSelected(false);
        }
        onCounterContactsChanged(ConstsCore.ZERO_INT_VALUE);
        friendsAdapter.setCounterContacts(ConstsCore.ZERO_INT_VALUE);
        friendsAdapter.notifyDataSetChanged();
    }

    private void sendInviteToContacts() {
        EmailHelper.sendInviteEmail(this, selectedContactsFriendsArray);
    }

    private class ActionModeCallback extends SimpleActionModeCallback {

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
                        performActionNext();
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
            clearCheckedFriends();
            actionMode = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SystemPermissionHelper.PERMISSIONS_FOR_IMPORT_FRIENDS_REQUEST: {
                if (grantResults.length > 0) {
                    if (systemPermissionHelper.isAllPermissionsGrantedForImportFriends()){
                        initFriendsList();
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
                        SystemPermissionHelper.openSystemSettings(InviteFriendsActivity.this);
                    }
                });
    }
}