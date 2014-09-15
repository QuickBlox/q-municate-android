package com.quickblox.q_municate.ui.main;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.core.gcm.GSMHelper;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.model.ParcelableQBDialog;
import com.quickblox.q_municate.qb.commands.QBLoadDialogsCommand;
import com.quickblox.q_municate.qb.commands.QBLoadFriendListCommand;
import com.quickblox.q_municate.qb.commands.QBLoadUserCommand;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.chats.DialogsFragment;
import com.quickblox.q_municate.ui.chats.FindUnknownFriendsTask;
import com.quickblox.q_municate.ui.feedback.FeedbackFragment;
import com.quickblox.q_municate.ui.friends.FriendsListFragment;
import com.quickblox.q_municate.ui.importfriends.ImportFriends;
import com.quickblox.q_municate.ui.invitefriends.InviteFriendsFragment;
import com.quickblox.q_municate.ui.settings.SettingsFragment;
import com.quickblox.q_municate.utils.ChatDialogUtils;
import com.quickblox.q_municate.utils.FacebookHelper;
import com.quickblox.q_municate.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseLogeableActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final int ID_CHATS_LIST_FRAGMENT = 0;
    public static final int ID_FRIEND_LIST_FRAGMENT = 1;
    public static final int ID_INVITE_FRIENDS_FRAGMENT = 2;
    public static final int ID_SETTINGS_FRAGMENT = 3;
    public static final int ID_FEEDBACK_FRAGMENT = 4;

    private static final String TAG = MainActivity.class.getSimpleName();
    private NavigationDrawerFragment navigationDrawerFragment;
    private FacebookHelper facebookHelper;
    private ImportFriends importFriends;
    private GSMHelper gsmHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (navigationDrawerFragment != null) {
            prepareMenu(menu);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (currentFragment instanceof InviteFriendsFragment) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        } else if (facebookHelper != null) {
            facebookHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void prepareMenu(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(!navigationDrawerFragment.isDrawerOpen());
            menu.getItem(i).collapseActionView();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        switch (position) {
            case ID_CHATS_LIST_FRAGMENT:
                fragment = DialogsFragment.newInstance();
                break;
            case ID_FRIEND_LIST_FRAGMENT:
                fragment = FriendsListFragment.newInstance();
                break;
            case ID_INVITE_FRIENDS_FRAGMENT:
                fragment = InviteFriendsFragment.newInstance();
                break;
            case ID_SETTINGS_FRAGMENT:
                fragment = SettingsFragment.newInstance();
                break;
            case ID_FEEDBACK_FRAGMENT:
                fragment = FeedbackFragment.newInstance();
                break;
        }
        setCurrentFragment(fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        useDoubleBackPressed = true;

        gsmHelper = new GSMHelper(this);

        initNavigationDrawer();

        if (!isImportInitialized()) {
            showProgress();
            facebookHelper = new FacebookHelper(this, savedInstanceState,
                    new FacebookSessionStatusCallback());
            importFriends = new ImportFriends(MainActivity.this, facebookHelper);
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_SIGN_UP_INITIALIZED, false);
        }

        initBroadcastActionList();
        checkGCMRegistration();
        loadFriendsList();
    }

    private boolean isImportInitialized() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        return prefsHelper.getPref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);
    }

    private void initBroadcastActionList() {
        addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION, new LoadDialogsSuccessAction());
        addAction(QBServiceConsts.LOAD_FRIENDS_SUCCESS_ACTION, new LoadFriendsSuccessAction());
        addAction(QBServiceConsts.LOAD_FRIENDS_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION, failAction);
    }

    private void initNavigationDrawer() {
        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(
                R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(
                R.id.drawer_layout));
    }

    private void checkGCMRegistration() {
        if (gsmHelper.checkPlayServices()) {
            if (!gsmHelper.isDeviceRegisteredWithUser(AppSession.getSession().getUser())) {
                gsmHelper.registerInBackground();
                return;
            }
            boolean subscribed = gsmHelper.isSubscribed();
            if (!subscribed) {
                gsmHelper.subscribeToPushNotifications();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }

    private void loadFriendsList() {
        QBLoadFriendListCommand.start(this);
    }

    private void loadChatsDialogs() {
        QBLoadDialogsCommand.start(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gsmHelper.checkPlayServices();
    }

    @Override
    protected void onFailAction(String action) {
        if (QBServiceConsts.LOAD_FRIENDS_FAIL_ACTION.equals(action)) {
            loadChatsDialogs();
        }
    }

    private class LoadDialogsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            ArrayList<ParcelableQBDialog> parcelableDialogsList = bundle.getParcelableArrayList(
                    QBServiceConsts.EXTRA_CHATS_DIALOGS);
            if (!parcelableDialogsList.isEmpty()) {
                ArrayList<QBDialog> dialogsList = ChatDialogUtils.parcelableDialogsToDialogs(
                        parcelableDialogsList);
                new FindUnknownFriendsTask(MainActivity.this).execute(dialogsList, null);
            }
        }
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                importFriends.startGetFriendsListTask(true);
            } else if (!(!session.isOpened() && !session.isClosed()) && !isImportInitialized()) {
                importFriends.startGetFriendsListTask(false);
                hideProgress();
            }
        }
    }

    private class LoadFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            loadChatsDialogs();
        }
    }
}