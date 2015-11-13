package com.quickblox.q_municate.ui.activities.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.MenuItem;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.chat.QBChatService;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.gcm.GSMHelper;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.fragments.chats.DialogsListFragment;
import com.quickblox.q_municate.utils.helpers.FacebookHelper;
import com.quickblox.q_municate.utils.helpers.ImportFriendsHelper;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoadDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCompositeCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class MainActivity extends BaseLoggableActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FacebookHelper facebookHelper;
    private ImportFriendsHelper importFriendsHelper;
    private GSMHelper gsmHelper;

    private LoginChatCompositeSuccessAction loginChatCompositeSuccessAction;
    private ImportFriendsSuccessAction importFriendsSuccessAction;
    private ImportFriendsFailAction importFriendsFailAction;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpActionBarWithUpButton();

        initFields(savedInstanceState);

        checkGCMRegistration();

        if (!isLoggedInChat()) {
            loginChat();
        }

        launchDialogsListFragment();
    }

    private void initFields(Bundle savedInstanceState) {
        gsmHelper = new GSMHelper(this);
        loginChatCompositeSuccessAction = new LoginChatCompositeSuccessAction();
        importFriendsSuccessAction = new ImportFriendsSuccessAction();
        importFriendsFailAction = new ImportFriendsFailAction();

        if (!appSharedHelper.isUsersImportInitialized()) {
//            showProgress();
//            facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());
//            importFriendsHelper = new ImportFriendsHelper(MainActivity.this, facebookHelper);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (facebookHelper != null) {
            facebookHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        addActions();
        checkGCMRegistration();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeActions();
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION, loginChatCompositeSuccessAction);
        addAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION, importFriendsSuccessAction);
        addAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION, importFriendsFailAction);
        addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION, new LoadChatsSuccessAction());

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void loginChat() {
        QBLoginChatCompositeCommand.start(this);
    }

    private void performImportFriendsSuccessAction() {
        appSharedHelper.saveUsersImportInitialized(true);
    }

    private void performLoginChatSuccessAction(Bundle bundle) {
        checkLoadDialogs();
        hideProgress();
    }

    private void checkLoadDialogs() {
        if (appSharedHelper.isFirstAuth()) {
            showSnackbar(R.string.dlgs_loading_dialogs, Snackbar.LENGTH_INDEFINITE);
            QBLoadDialogsCommand.start(this);
        }
    }

    private void checkGCMRegistration() {
        if (gsmHelper.checkPlayServices()) {
            if (!gsmHelper.isDeviceRegisteredWithUser()) {
                gsmHelper.registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    private void performImportFriendsFailAction(Bundle bundle) {
        performImportFriendsSuccessAction();
    }

    private boolean isLoggedInChat() {
        return QBChatService.isInitialized() && QBChatService.getInstance().isLoggedIn();
    }

    private void performLoadChatsSuccessAction(Bundle bundle) {
        appSharedHelper.saveFirstAuth(false);
        hideSnackBar();
    }

    private void launchDialogsListFragment() {
        setCurrentFragment(DialogsListFragment.newInstance());
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                importFriendsHelper.startGetFriendsListTask(true);
            } else if (!session.isClosed() && !appSharedHelper.isUsersImportInitialized()) {
                importFriendsHelper.startGetFriendsListTask(false);
                hideProgress();
            }
        }
    }

    private class LoginChatCompositeSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            performLoginChatSuccessAction(bundle);
        }
    }

    private class ImportFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            performImportFriendsSuccessAction();
        }
    }

    private class ImportFriendsFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            performImportFriendsFailAction(bundle);
        }
    }

    private class LoadChatsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            performLoadChatsSuccessAction(bundle);
        }
    }
}