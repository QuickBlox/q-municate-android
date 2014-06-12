package com.quickblox.qmunicate.ui.base;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.chats.PrivateDialogActivity;
import com.quickblox.qmunicate.ui.dialogs.ProgressDialog;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.ErrorUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class BaseFragmentActivity extends FragmentActivity implements QBLogeable {

    public static final int DOUBLE_BACK_DELAY = 2000;

    public static boolean isNeedToSaveSession = false;

    protected final ProgressDialog progress;
    protected BroadcastReceiver broadcastReceiver;
    protected BroadcastReceiver messageBroadcastReceiver;
    protected App app;
    protected ActionBar actionBar;
    protected QBService service;
    protected boolean useDoubleBackPressed;
    protected Fragment currentFragment;
    protected FailAction failAction;
    protected String currentOpponent;
    protected String roomJidId;
    private View newMessageView;
    private TextView newMessageTextView;
    private TextView senderMessageTextView;
    private boolean doubleBackToExitPressedOnce;
    private Map<String, Command> broadcastCommandMap = new HashMap<String, Command>();
    private boolean bounded;
    private ServiceConnection serviceConnection = new QBChatServiceConnection();

    public BaseFragmentActivity() {
        progress = ProgressDialog.newInstance(R.string.dlg_wait_please);
    }

    public void showNewMessageAlert(String sender, String message) {
        newMessageTextView.setText(message);
        senderMessageTextView.setText(sender);
        Crouton.cancelAllCroutons();
        Crouton.show(this, newMessageView);
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
        super.onCreate(savedInstanceState);
        app = App.getInstance();
        actionBar = getActionBar();
        broadcastReceiver = new BaseBroadcastReceiver();
        messageBroadcastReceiver = new MessageBroadcastReceiver();
        failAction = new FailAction();
        initUI();
    }

    private void initUI() {
        newMessageView = getLayoutInflater().inflate(R.layout.list_item_new_message, null);
        newMessageTextView = (TextView) newMessageView.findViewById(R.id.message_textview);
        senderMessageTextView = (TextView) newMessageView.findViewById(R.id.sender_textview);
    }

    @Override
    protected void onPause() {
        unregisterBroadcastReceiver();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBroadcastActionList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        connectToService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }

    private void unbindService() {
        if (bounded) {
            unbindService(serviceConnection);
        }
    }

    private void connectToService() {
        Intent intent = new Intent(this, QBService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void updateBroadcastActionList() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageBroadcastReceiver);
        IntentFilter intentFilter = new IntentFilter();
        for (String commandName : broadcastCommandMap.keySet()) {
            intentFilter.addAction(commandName);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageBroadcastReceiver, new IntentFilter(
                QBServiceConsts.GOT_CHAT_MESSAGE));
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    public void showProgress() {
        progress.show(getFragmentManager(), null);
    }

    public void hideProgress() {
        try {
            if (progress != null && !progress.isDetached()) {
                progress.dismissAllowingStateLoss();
            }
        } catch (NullPointerException npe) {
            ErrorUtils.logError(npe);
        }
    }

    public void addAction(String action, Command command) {
        broadcastCommandMap.put(action, command);
    }

    public void removeAction(String action) {
        broadcastCommandMap.remove(action);
    }

    protected void onConnectedToService() {
    }

    protected void navigateToParent() {
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        NavUtils.navigateUpTo(this, intent);
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

    @Override
    public boolean isCanPerformLogoutInOnStop() {
        return true;
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

    private class BaseBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (intent != null && (action) != null) {
                Command command = broadcastCommandMap.get(action);
                if (command != null) {
                    Log.d("STEPS", "executing " + action);
                    command.execute(intent.getExtras());
                }
            }
        }
    }

    private class MessageBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                return;
            }
            String sender = extras.getString(QBServiceConsts.EXTRA_SENDER_CHAT_MESSAGE);
            String jidId = extras.getString(QBServiceConsts.EXTRA_ROOM_JID);
            boolean isNotCurrentOpponent = sender != null &&
                    !sender.equals(currentOpponent);
            boolean isFromCurrentChat = jidId != null && jidId.equals(roomJidId);
            if (MainActivity.isNeedToShowCrouton && isNotCurrentOpponent && !isFromCurrentChat) {
                String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
                showNewMessageAlert(sender, message);
            }
        }
    }

    private class QBChatServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            bounded = true;
            service = ((QBService.QBServiceBinder) binder).getService();
            onConnectedToService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }
}