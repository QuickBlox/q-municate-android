package com.quickblox.q_municate.ui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.QBLogoutCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.authorization.landing.LandingActivity;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate.ui.dialogs.ConfirmDialog;
import com.quickblox.q_municate.utils.FacebookHelper;
import com.quickblox.q_municate_core.utils.PrefsHelper;

import java.util.Arrays;
import java.util.List;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class NavigationDrawerFragment extends BaseFragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static DrawerLayout drawerLayout;
    private static View fragmentContainerView;
    private Resources resources;
    private ListView drawerListView;
    private TextView fullNameTextView;
    private ImageButton logoutButton;

    private NavigationDrawerCallbacks navigationDrawerCallbacks;
    private NavigationDrawerCounterListener navigationDrawerCounterListener;
    private ActionBarDrawerToggle drawerToggle;
    private int currentSelectedPosition = 0;
    private boolean fromSavedInstanceState;
    private boolean userLearnedDrawer;
    private NavigationDrawerAdapter navigationDrawerAdapter;
    private BroadcastReceiver countUnreadDialogsBroadcastReceiver;

    public static boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resources = getResources();

        initPrefValues();

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
        }

        selectItem(currentSelectedPosition);

        initLocalBroadcastManagers();
    }

    private void initLocalBroadcastManagers() {
        countUnreadDialogsBroadcastReceiver = new CountUnreadDialogsBroadcastReceiver();

        LocalBroadcastManager.getInstance(baseActivity).registerReceiver(countUnreadDialogsBroadcastReceiver,
                new IntentFilter(QBServiceConsts.GOT_CHAT_MESSAGE));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        initUI(rootView);
        initListeners();
        initNavigationAdapter();

        drawerListView.setItemChecked(currentSelectedPosition, true);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        navigationDrawerCallbacks = (NavigationDrawerCallbacks) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        baseActivity.getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        QBUser user = AppSession.getSession().getUser();
        if (user != null) {
            fullNameTextView.setText(user.getFullName());
        }
        addActions();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navigationDrawerCallbacks = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void setUp(int fragmentId, final DrawerLayout drawerLayout) {
        fragmentContainerView = baseActivity.findViewById(fragmentId);
        this.drawerLayout = drawerLayout;

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = baseActivity.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        drawerToggle = new QMActionBarDrawerToggle(baseActivity, drawerLayout, R.drawable.ic_drawer,
                R.string.nvd_open, R.string.nvd_close);

        if (!userLearnedDrawer && !fromSavedInstanceState) {
            drawerLayout.openDrawer(fragmentContainerView);
        }

        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        drawerLayout.setDrawerListener(drawerToggle);
    }

    private void initPrefValues() {
        PrefsHelper prefsHelper = PrefsHelper.getPrefsHelper();
        userLearnedDrawer = prefsHelper.getPref(PrefsHelper.PREF_USER_LEARNED_DRAWER, false);
    }

    private void selectItem(int position) {
        currentSelectedPosition = position;
        if (drawerListView != null) {
            drawerListView.setItemChecked(position, true);
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
        if (navigationDrawerCallbacks != null) {
            navigationDrawerCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    private void initNavigationAdapter() {
        navigationDrawerAdapter = new NavigationDrawerAdapter(baseActivity, getNavigationDrawerItems());
        drawerListView.setAdapter(navigationDrawerAdapter);
        navigationDrawerCounterListener = navigationDrawerAdapter;
    }

    private void initUI(View rootView) {
        drawerListView = (ListView) rootView.findViewById(R.id.navigation_listview);
        logoutButton = (ImageButton) rootView.findViewById(R.id.logout_imagebutton);
        fullNameTextView = (TextView) rootView.findViewById(R.id.fullname_textview);
    }

    private void initListeners() {
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                selectItem(position);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }


    private List<String> getNavigationDrawerItems() {
        String[] itemsArray = resources.getStringArray(R.array.nvd_items_array);
        return Arrays.asList(itemsArray);
    }

    private void logout() {
        ConfirmDialog dialog = ConfirmDialog.newInstance(R.string.dlg_logout, R.string.dlg_confirm);
        dialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                baseActivity.showProgress();
                FacebookHelper.logout();
                QBLogoutCommand.start(baseActivity);
            }
        });
        dialog.show(getFragmentManager(), null);
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION, new LogoutSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOGOUT_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
    }

    private void saveUserLearnedDrawer() {
        PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_USER_LEARNED_DRAWER, true);
    }

    private int getCountUnreadDialogs() {
        return ChatDatabaseManager.getCountUnreadDialogs(baseActivity);
    }

    public interface NavigationDrawerCallbacks {

        void onNavigationDrawerItemSelected(int position);
    }

    public interface NavigationDrawerCounterListener {

        public void onUpdateCountUnreadDialogs(int count);
    }

    private class CountUnreadDialogsBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                navigationDrawerCounterListener.onUpdateCountUnreadDialogs(getCountUnreadDialogs());
            }
        }
    }

    private class QMActionBarDrawerToggle extends ActionBarDrawerToggle {

        public QMActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int drawerImageRes,
                int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, drawerImageRes, openDrawerContentDescRes,
                    closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);

            Crouton.cancelAllCroutons();

            baseActivity.invalidateOptionsMenu();

            if (!userLearnedDrawer) {
                userLearnedDrawer = true;
                saveUserLearnedDrawer();
            }

            navigationDrawerCounterListener.onUpdateCountUnreadDialogs(getCountUnreadDialogs());
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            baseActivity.invalidateOptionsMenu();
        }
    }

    private class LogoutSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            LandingActivity.start(baseActivity);
            baseActivity.finish();
        }
    }
}