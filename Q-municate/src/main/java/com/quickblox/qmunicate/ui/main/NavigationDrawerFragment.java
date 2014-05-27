package com.quickblox.qmunicate.ui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
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

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.qb.commands.QBLogoutCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.dialogs.ConfirmDialog;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.utils.FacebookHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;
import de.keyboardsurfer.android.widget.crouton.Crouton;

import java.util.Arrays;
import java.util.List;

public class NavigationDrawerFragment extends BaseFragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private Resources resources;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private View fragmentContainerView;
    private TextView fullnameTextView;
    private ImageButton logoutButton;

    private NavigationDrawerCallbacks navigationDrawerCallbacks;
    private ActionBarDrawerToggle drawerToggle;
    private int currentSelectedPosition = 0;
    private boolean fromSavedInstanceState;
    private boolean userLearnedDrawer;
    private boolean isMissedMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        resources = getResources();

        initPrefValues();

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
        }

        if (isMissedMessage) {
            currentSelectedPosition = MainActivity.ID_CHATS_LIST_FRAGMENT;
            saveMissedMessageFlag(false);
        }

        selectItem(currentSelectedPosition);
    }

    private void initPrefValues() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        userLearnedDrawer = prefsHelper.getPref(PrefsHelper.PREF_USER_LEARNED_DRAWER, false);
        isMissedMessage = prefsHelper.getPref(PrefsHelper.PREF_MISSED_MESSAGE, false);
    }

    private void saveMissedMessageFlag(boolean isMissedMessage) {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_MISSED_MESSAGE, isMissedMessage);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        initUI(rootView);
        initListeners();

        NavigationDrawerAdapter navigationDrawerAdapter = new NavigationDrawerAdapter(baseActivity,
                getNavigationDrawerItems());
        drawerListView.setAdapter(navigationDrawerAdapter);

        drawerListView.setItemChecked(currentSelectedPosition, true);

        return rootView;
    }

    private void initUI(View rootView) {
        drawerListView = (ListView) rootView.findViewById(R.id.navigation_listview);
        logoutButton = (ImageButton) rootView.findViewById(R.id.logout_imagebutton);
        fullnameTextView = (TextView) rootView.findViewById(R.id.fullname_textview);
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        navigationDrawerCallbacks = (NavigationDrawerCallbacks) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        baseActivity.addAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION, new LogoutSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOGOUT_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
        baseActivity.getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        QBUser user = App.getInstance().getUser();
        if (user != null) {
            fullnameTextView.setText(user.getFullName());
        }
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

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
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

    private void clearCache() {
        DatabaseManager.deleteAllFriends(baseActivity);
        DatabaseManager.deleteAllChats(baseActivity);
        // TODO SF clear something else
    }

    private void saveUserLearnedDrawer() {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_USER_LEARNED_DRAWER, true);
    }

    public interface NavigationDrawerCallbacks {

        void onNavigationDrawerItemSelected(int position);
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
            clearCache();
            LoginActivity.start(baseActivity);
            baseActivity.finish();
        }
    }
}