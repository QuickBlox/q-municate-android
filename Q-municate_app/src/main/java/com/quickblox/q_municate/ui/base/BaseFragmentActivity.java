package com.quickblox.q_municate.ui.base;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.Window;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.dialogs.ProgressDialog;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class BaseFragmentActivity extends FragmentActivity implements QBLogeable, ActivityHelper.ServiceConnectionListener {

    public static final int DOUBLE_BACK_DELAY = 2000;

    protected final ProgressDialog progress;
    protected App app;
    protected ActionBar actionBar;
    protected boolean useDoubleBackPressed;
    protected Fragment currentFragment;
    protected FailAction failAction;
    protected SuccessAction successAction;
    protected AtomicBoolean canPerformLogout = new AtomicBoolean(true);
    protected ActivityHelper activityHelper;
    private QBDialog currentDialog;
    private boolean doubleBackToExitPressedOnce;

    public BaseFragmentActivity() {
        progress = ProgressDialog.newInstance(R.string.dlg_wait_please);
    }

    public FailAction getFailAction() {
        return failAction;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce || !useDoubleBackPressed) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        DialogUtils.show(this, getString(R.string.dlg_click_back_again));
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, DOUBLE_BACK_DELAY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        app = App.getInstance();
        if (savedInstanceState != null && savedInstanceState.containsKey(CAN_PERFORM_LOGOUT)) {
            canPerformLogout = new AtomicBoolean(savedInstanceState.getBoolean(CAN_PERFORM_LOGOUT));
        }
        actionBar = getActionBar();
        failAction = new FailAction();
        successAction = new SuccessAction();
        activityHelper = new ActivityHelper(this, new GlobalListener(), this);
        activityHelper.onCreate();
    }

    @Override
    protected void onPause() {
        activityHelper.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityHelper.onResume();
        addAction(QBServiceConsts.LOGIN_REST_SUCCESS_ACTION, successAction);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CAN_PERFORM_LOGOUT, canPerformLogout.get());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        activityHelper.onStart();
        super.onStart();
    }

    @Override
    protected void onStop() {
        activityHelper.onStop();
        super.onStop();
    }

    public QBService getService() {
        return activityHelper.service;
    }

    protected void setCurrentDialog(QBDialog dialog) {
        currentDialog = dialog;
    }

    public void updateBroadcastActionList() {
        activityHelper.updateBroadcastActionList();
    }

    public void showProgress() {
        progress.show(getFragmentManager(), null);
    }

    public void hideProgress() {
        if (progress != null && progress.getActivity() != null) {
            progress.dismissAllowingStateLoss();
        }
    }

    public void addAction(String action, Command command) {
        activityHelper.addAction(action, command);
    }

    public void removeAction(String action) {
        activityHelper.removeAction(action);
    }

    @Override
    public void onConnectedToService(QBService service) {

    }

    protected void navigateToParent() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        if (intent == null) {
            finish();
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            NavUtils.navigateUpTo(this, intent);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T _findViewById(int viewId) {
        return (T) findViewById(viewId);
    }

    protected void setCurrentFragment(Fragment fragment) {
        currentFragment = fragment;
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = buildTransaction();
        transaction.replace(R.id.container, fragment, null);
        transaction.commit();
    }

    private FragmentTransaction buildTransaction() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        return transaction;
    }

    protected void onFailAction(String action) {

    }

    protected void onSuccessAction(String action) {

    }

    public void hideActionBarProgress() {
        activityHelper.hideActionBarProgress();
    }

    public void showActionBarProgress() {
        activityHelper.showActionBarProgress();
    }

    @Override
    public boolean isCanPerformLogoutInOnStop() {
        return canPerformLogout.get();
    }

    public class SuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            onSuccessAction(bundle.getString(QBServiceConsts.COMMAND_ACTION));
        }
    }

    public class FailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception e = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            ErrorUtils.showError(BaseFragmentActivity.this, e);
            hideProgress();
            onFailAction(bundle.getString(QBServiceConsts.COMMAND_ACTION));
        }
    }

    private class GlobalListener implements ActivityHelper.GlobalActionsListener {

        @Override
        public void onReceiveChatMessageAction(Bundle extras) {
            if (currentDialog == null) {
                return;
            }
            if (!isFromCurrentDialog(extras)) {
                activityHelper.onReceivedChatMessageNotification(extras);
            }
        }

        @Override
        public void onReceiveForceReloginAction(Bundle extras) {
            activityHelper.forceRelogin();
        }

        @Override
        public void onReceiveRefreshSessionAction(Bundle extras) {
            DialogUtils.show(BaseFragmentActivity.this, getString(R.string.dlg_refresh_session));
//            showProgress();
            activityHelper.refreshSession();
        }

        @Override
        public void onReceiveContactRequestAction(Bundle extras) {
            if (currentDialog == null) {
                return;
            }
            if (!isFromCurrentDialog(extras)) {
                activityHelper.onReceivedContactRequestNotification(extras);
            }
        }
    }

    private boolean isFromCurrentDialog(Bundle extras) {
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        boolean isFromCurrentChat = dialogId != null && dialogId.equals(currentDialog.getDialogId());
        return isFromCurrentChat;
    }
}