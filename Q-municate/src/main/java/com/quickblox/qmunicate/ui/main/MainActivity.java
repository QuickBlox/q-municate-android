package com.quickblox.qmunicate.ui.main;

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
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.core.gcm.GSMHelper;
import com.quickblox.qmunicate.qb.commands.QBJoinGroupDialogCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadDialogsCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadFriendListCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseLogeableActivity;
import com.quickblox.qmunicate.ui.chats.DialogsFragment;
import com.quickblox.qmunicate.ui.importfriends.ImportFriends;
import com.quickblox.qmunicate.ui.invitefriends.InviteFriendsFragment;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.FacebookHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseLogeableActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final int ID_FRIEND_LIST_FRAGMENT = 0;
    public static final int ID_CHATS_LIST_FRAGMENT = 1;
    public static final int ID_INVITE_FRIENDS_FRAGMENT = 2;
    public static final int ID_SETTINGS_FRAGMENT = 3;
    private static final String TAG = MainActivity.class.getSimpleName();
    private NavigationDrawerFragment navigationDrawerFragment;
    private FacebookHelper facebookHelper;
    private ImportFriends importFriends;
    private boolean isImportInitialized;
    private boolean isSignUpInitialized;
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
            case ID_FRIEND_LIST_FRAGMENT:
                fragment = FriendsListFragment.newInstance();
                break;
            case ID_CHATS_LIST_FRAGMENT:
                fragment = DialogsFragment.newInstance();
                break;
            case ID_INVITE_FRIENDS_FRAGMENT:
                fragment = InviteFriendsFragment.newInstance();
                break;
            case ID_SETTINGS_FRAGMENT:
                fragment = SettingsFragment.newInstance();
                break;
        }
        setCurrentFragment(fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        useDoubleBackPressed = true;

        initPrefValues();

        gsmHelper = new GSMHelper(this);

        initNavigationDrawer();

        if (!isImportInitialized && isSignUpInitialized) {
            showProgress();
            facebookHelper = new FacebookHelper(this, savedInstanceState,
                    new FacebookSessionStatusCallback());
            importFriends = new ImportFriends(MainActivity.this, facebookHelper);
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_SIGN_UP_INITIALIZED, false);
        }

        checkGCMRegistration();
        loadFriendsList();
        loadChatsDialogs();
    }

    private void initPrefValues() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        isImportInitialized = prefsHelper.getPref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);
        isSignUpInitialized = prefsHelper.getPref(PrefsHelper.PREF_SIGN_UP_INITIALIZED, false);
    }

    private void initNavigationDrawer() {
        navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(
                R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(
                R.id.drawer_layout));
    }

    private void checkGCMRegistration() {
        if (gsmHelper.checkPlayServices()) {
            if (!gsmHelper.isDeviceRegisteredWithUser(App.getInstance().getUser())) {
                gsmHelper.registerInBackground();
                return;
            }
            int subscriptionId = gsmHelper.getSubscriptionId();
            if (Consts.NOT_INITIALIZED_VALUE == subscriptionId) {
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
        addActions();
    }

    private void addActions() {
        addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION, new LoadChatsDialogsSuccessAction());
        addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    private void joinGroupDialogs(List<QBDialog> dialogsList) {
        List<String> roomJidList = getRoomJidListFromDialogs(dialogsList);
        QBJoinGroupDialogCommand.start(this, roomJidList);
    }

    private List<String> getRoomJidListFromDialogs(List<QBDialog> dialogsList) {
        List<String> roomJidList = new ArrayList<String>();
        for (QBDialog dialog : dialogsList) {
            if (dialog.getType() != QBDialogType.PRIVATE) {
                roomJidList.add(dialog.getRoomJid());
            }
        }
        return roomJidList;
    }

    private class LoadChatsDialogsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            List<QBDialog> dialogsList = (List<QBDialog>) bundle.getSerializable(
                    QBServiceConsts.EXTRA_CHATS_DIALOGS);
            joinGroupDialogs(dialogsList);
        }
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                importFriends.startGetFriendsListTask(true);
            } else if (!(!session.isOpened() && !session.isClosed())) {
                importFriends.startGetFriendsListTask(false);
                hideProgress();
            }
        }
    }
}