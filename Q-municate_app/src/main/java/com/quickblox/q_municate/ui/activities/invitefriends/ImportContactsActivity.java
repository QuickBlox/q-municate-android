package com.quickblox.q_municate.ui.activities.invitefriends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.core.helper.CollectionUtils;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.invitefriends.ImportContactAdapter;
import com.quickblox.q_municate.utils.ContactsUtils;
import com.quickblox.q_municate.utils.DialogsUtils;
import com.quickblox.q_municate.utils.helpers.FriendListServiceManager;
import com.quickblox.q_municate.utils.helpers.ImportContactsHelper;
import com.quickblox.q_municate.utils.listeners.CounterChangedListener;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.utils.listeners.UserOperationListener;
import com.quickblox.q_municate.utils.listeners.simple.SimpleActionModeCallback;
import com.quickblox.q_municate_core.models.InviteContact;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate.utils.helpers.SystemPermissionHelper;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ImportContactsActivity extends BaseLoggableActivity implements CounterChangedListener {

    private static final String TAG = ImportContactsActivity.class.getSimpleName();
    @Bind(R.id.contacts)
    RecyclerView contactsRecyclerView;

    private List<InviteContact> contactsList;
    private ImportContactAdapter importContactAdapter;
    private ActionMode actionMode;
    private SystemPermissionHelper systemPermissionHelper;
    private ImportContactsHelper importContactsHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, ImportContactsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_import_contacts;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();
    }

    private void initFields() {
        title = getString(R.string.import_contacts_title);
        systemPermissionHelper = new SystemPermissionHelper(this);
        importContactsHelper = new ImportContactsHelper(this);
        contactsList = new ArrayList<>();
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
        contactsList.addAll(importContactsHelper.getContactsFromAddressBook());
        FriendListServiceManager.getInstance().findContactsOnQb(contactsList)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<QMUser>>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                    }

                    @Override
                    public void onNext(List<QMUser> qmUsers) {
                        hideProgress();
                        initFriendsList(prepareFriendsListFromQbUsers(qmUsers));
                    }
                });
    }

    @Override
    public void onCounterContactsChanged(int valueCounterContacts) {
        if (actionMode != null){
            actionMode.invalidate();
        }

        if (valueCounterContacts != ConstsCore.ZERO_INT_VALUE) {
            startActionModeAndUpdateUi();
        } else {
            stopActionModeAndUpdateUi();
        }
    }

    private void startActionModeAndUpdateUi() {
        if (actionMode != null) {
            return;
        }
        actionMode = startSupportActionMode(new ActionModeCallback());
        importContactAdapter.notifyDataSetChanged();
    }

    private void stopActionModeAndUpdateUi() {
        if (actionMode != null) {
            actionMode.finish();
            importContactAdapter.notifyDataSetChanged();
        }
    }

    private void checkAllContacts() {
        importContactAdapter.selectAllContacts();
    }

    private void addContactsToFriends(List<Integer> usersIds) {
        showProgress();
        FriendListServiceManager.getInstance().addFriends(usersIds)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<Integer>>() {
                    @Override
                    public void onCompleted() {
                        Log.v(TAG, "onCompleted()");
                        hideProgress();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(List<Integer> integers) {
                        if (!CollectionUtils.isEmpty(integers)) {
                            updateContactsListWithoutInvitedContacts(integers);
                        }
                        hideProgress();
                        Log.v(TAG, "onNext(List<Integer> integers)");
                    }
                });
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


    private void initFriendsList(List<InviteContact> realQbFriendsList) {
        contactsList = realQbFriendsList;
        importContactAdapter = new ImportContactAdapter(this, contactsList, new UserOperationListener() {
            @Override
            public void onAddUserClicked(int userId) {
                showProgress();
                addContactsToFriends(Collections.singletonList(userId));
            }
        });
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        importContactAdapter.setCounterChangedListener(this);
        contactsRecyclerView.setAdapter(importContactAdapter);
    }

    //TODO VT need move tu utils-class
    private List<InviteContact> prepareFriendsListFromQbUsers(List<QMUser> realQbFriendsList) {
        List<InviteContact> realFriendsList = new ArrayList<>(realQbFriendsList.size());

        for (QMUser qbUser : realQbFriendsList){
            InviteContact inviteContact = null;

            if (qbUser.getPhone() != null){
                inviteContact = ContactsUtils.getInviteContactById(qbUser.getPhone(), contactsList);
            } else if (qbUser.getEmail() != null){
                inviteContact = ContactsUtils.getInviteContactById(qbUser.getEmail(), contactsList);
            }

            if (inviteContact != null) {
                inviteContact.setQbId(qbUser.getId());
                inviteContact.setQbName(qbUser.getFullName());
                inviteContact.setQbAvatarUrl(Utils.customDataToObject(qbUser.getCustomData()).getAvatarUrl());
                realFriendsList.add(inviteContact);
            }
        }

        return realFriendsList;
    }

    private void updateContactsListWithoutInvitedContacts(List<Integer> invitedUsers){
        contactsList = ContactsUtils.removeInviteContactsByQbIds(contactsList, invitedUsers);
        importContactAdapter.setNewData(contactsList);
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
                        addContactsToFriends(importContactAdapter.getSelectedFriends());
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
}