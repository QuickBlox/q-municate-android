package com.quickblox.q_municate.ui.activities.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.facebook.Session;
import com.facebook.SessionState;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.gcm.GSMHelper;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.fragments.chats.DialogsListFragment;
import com.quickblox.q_municate.utils.helpers.FacebookHelper;
import com.quickblox.q_municate.utils.helpers.ImportFriendsHelper;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.Utils;

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
        initFields(savedInstanceState);
        setUpActionBarWithUpButton();

        checkGCMRegistration();

        if (!isChatInitializedAndUserLoggedIn()) {
            loginChat();
        } else {
            checkImportFriends();
        }

        launchDialogsListFragment();
    }

    private void initFields(Bundle savedInstanceState) {
        title = " " + AppSession.getSession().getUser().getFullName();
        gsmHelper = new GSMHelper(this);
        importFriendsSuccessAction = new ImportFriendsSuccessAction();
        importFriendsFailAction = new ImportFriendsFailAction();
        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());
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
        actualizeCurrentTitle();
        super.onResume();
        addActions();
        checkGCMRegistration();
    }

    private void actualizeCurrentTitle() {
        if (AppSession.getSession().getUser().getFullName() != null) {
            title = " " + AppSession.getSession().getUser().getFullName();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeActions();
    }

    @Override
    protected void checkShowingConnectionError() {
        if (!isNetworkAvailable()) {
            setActionBarTitle(getString(R.string.dlg_internet_connection_is_missing));
            setActionBarIcon(null);
        } else {
            setActionBarTitle(title);
            checkVisibilityUserIcon();
        }
    }

    @Override
    protected void performLoginChatSuccessAction(Bundle bundle) {
        super.performLoginChatSuccessAction(bundle);
        actualizeCurrentTitle();
        checkImportFriends();
    }

    private void addActions() {
        addAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION, importFriendsSuccessAction);
        addAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION, importFriendsFailAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION);
        removeAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private void checkVisibilityUserIcon() {
        UserCustomData userCustomData = Utils.customDataToObject(AppSession.getSession().getUser().getCustomData());
        if (!TextUtils.isEmpty(userCustomData.getAvatar_url())) {
            loadLogoActionBar(userCustomData.getAvatar_url());
        } else {
            setActionBarIcon(ImageUtils.getRoundIconDrawable(this,
                            BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_user)));
        }
    }

    private void loadLogoActionBar(String logoUrl) {
        ImageLoader.getInstance().loadImage(logoUrl, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        setActionBarIcon(ImageUtils.getRoundIconDrawable(MainActivity.this, loadedBitmap));
                    }
                });
    }

    private void performImportFriendsSuccessAction() {
        appSharedHelper.saveUsersImportInitialized(true);
        hideProgress();
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

    private void launchDialogsListFragment() {
        setCurrentFragment(DialogsListFragment.newInstance());
    }

    private void checkImportFriends() {
        Log.d("IMPORT_FRIENDS", "checkImportFriends()");
        if (!appSharedHelper.isUsersImportInitialized()) {
            showProgress();
            importFriendsHelper = new ImportFriendsHelper(MainActivity.this, facebookHelper);
        }
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {

            if (session.isOpened()) {
                importFriendsHelper.startGetFriendsListTask(true);
            } else if (!session.isClosed() && !appSharedHelper.isUsersImportInitialized()) {
                importFriendsHelper.startGetFriendsListTask(false);
                appSharedHelper.saveUsersImportInitialized(true);
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