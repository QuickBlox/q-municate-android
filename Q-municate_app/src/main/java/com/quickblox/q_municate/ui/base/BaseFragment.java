package com.quickblox.q_municate.ui.base;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.core.listeners.UserStatusChangingListener;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment implements UserStatusChangingListener {

    protected static final String ARG_TITLE = "title";

    protected App app;
    protected BaseActivity baseActivity;
    protected BaseActivity.FailAction failAction;
    protected String title;

    protected QBFriendListHelper friendListHelper;
    protected QBPrivateChatHelper privateChatHelper;
    protected QBGroupChatHelper groupChatHelper;

    protected QBService service;

    private ServiceConnection serviceConnection;
    private boolean bounded;

    public String getTitle() {
        return title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        baseActivity = (BaseActivity) getActivity();
        baseActivity.setFragmentUserStatusChangingListener(this);

        failAction = baseActivity.getFailAction();
        app = App.getInstance();

        serviceConnection = new QBChatServiceConnection();
        connectToService();

        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().getActionBar().setTitle(title);
    }

    @Override
    public void onStop() {
        super.onStop();

        unbindService();
    }

    private void unbindService() {
        if (bounded && service != null && service.isRestricted()) {
            baseActivity.unbindService(serviceConnection);
        }
    }

    public BaseActivity getBaseActivity() {
        return (BaseActivity) getActivity();
    }

    protected boolean isExistActivity() {
        return ((!isDetached()) && (getBaseActivity() != null));
    }

    protected void activateButterKnife(View view) {
        ButterKnife.bind(this, view);
    }

    private void connectToService() {
        Intent intent = new Intent(baseActivity, QBService.class);
        baseActivity.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void onConnectedToService(QBService service) {
        if (friendListHelper == null) {
            friendListHelper = (QBFriendListHelper) service.getHelper(QBService.FRIEND_LIST_HELPER);
        }

        if (privateChatHelper == null) {
            privateChatHelper = (QBPrivateChatHelper) service.getHelper(QBService.PRIVATE_CHAT_HELPER);
        }

        if (groupChatHelper == null) {
            groupChatHelper = (QBGroupChatHelper) service.getHelper(QBService.GROUP_CHAT_HELPER);
        }
    }

    @Override
    public void onChangedUserStatus(int userId, boolean online) {
    }

    private class QBChatServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            bounded = true;
            service = ((QBService.QBServiceBinder) binder).getService();
            onConnectedToService(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}