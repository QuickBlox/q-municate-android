package com.quickblox.q_municate.ui.activities.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class MainActivity extends BaseLoggableActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private FacebookHelper facebookHelper;
    private ImportFriendsHelper importFriendsHelper;
    private GSMHelper gsmHelper;

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
        importFriendsSuccessAction = new ImportFriendsSuccessAction();
        importFriendsFailAction = new ImportFriendsFailAction();

        if (!appSharedHelper.isUsersImportInitialized()) {
            showProgress();
            facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());
            importFriendsHelper = new ImportFriendsHelper(MainActivity.this, facebookHelper);
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
        addAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION, importFriendsSuccessAction);
        addAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION, importFriendsFailAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void performImportFriendsSuccessAction() {
        appSharedHelper.saveUsersImportInitialized(true);
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
}