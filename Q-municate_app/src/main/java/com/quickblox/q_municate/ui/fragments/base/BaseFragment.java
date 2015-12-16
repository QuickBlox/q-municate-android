package com.quickblox.q_municate.ui.fragments.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.utils.bridges.ActionBarBridge;
import com.quickblox.q_municate.utils.bridges.ConnectionBridge;
import com.quickblox.q_municate.utils.bridges.LoadingBridge;
import com.quickblox.q_municate.utils.bridges.SnackbarBridge;
import com.quickblox.q_municate.utils.listeners.ServiceConnectionListener;
import com.quickblox.q_municate.utils.listeners.UserStatusChangingListener;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;

import butterknife.ButterKnife;

public abstract class BaseFragment extends Fragment implements UserStatusChangingListener, ServiceConnectionListener {

    protected App app;
    protected BaseActivity baseActivity;
    protected BaseActivity.FailAction failAction;
    protected ConnectionBridge connectionBridge;
    protected ActionBarBridge actionBarBridge;
    protected LoadingBridge loadingBridge;
    protected SnackbarBridge snackbarBridge;

    protected QBFriendListHelper friendListHelper;
    protected QBPrivateChatHelper privateChatHelper;
    protected QBGroupChatHelper groupChatHelper;

    protected QBService service;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof BaseActivity) {
            baseActivity = (BaseActivity) activity;
            service = baseActivity.getService();
            friendListHelper = baseActivity.getFriendListHelper();
            privateChatHelper = baseActivity.getPrivateChatHelper();
            groupChatHelper = baseActivity.getGroupChatHelper();
        }

        if (activity instanceof ConnectionBridge) {
            connectionBridge = (ConnectionBridge) activity;
        }

        if (activity instanceof ActionBarBridge) {
            actionBarBridge = (ActionBarBridge) activity;
        }

        if (activity instanceof LoadingBridge) {
            loadingBridge = (LoadingBridge) activity;
        }

        if (activity instanceof SnackbarBridge) {
            snackbarBridge = (SnackbarBridge) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addListeners();

        app = App.getInstance();
        failAction = baseActivity.getFailAction();
    }

    public void initActionBar() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        initActionBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    private void addListeners() {
        baseActivity.addFragmentUserStatusChangingListener(this);
        baseActivity.addFragmentServiceConnectionListener(this);
    }

    private void removeListeners() {
        baseActivity.removeFragmentUserStatusChangingListener(this);
        baseActivity.removeFragmentServiceConnectionListener(this);
    }

    protected boolean isExistActivity() {
        return ((!isDetached()) && (baseActivity != null));
    }

    protected void activateButterKnife(View view) {
        ButterKnife.bind(this, view);
    }

    @Override
    public void onChangedUserStatus(int userId, boolean online) {
        // nothing by default
    }

    @Override
    public void onConnectedToService(QBService service) {
        // nothing by default
    }
}