package com.quickblox.q_municate.ui.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.authorization.LandingActivity;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate.ui.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.utils.FacebookHelper;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.QBLogoutCompositeCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class NavigationDrawerFragment extends BaseFragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    @Bind(R.id.navigation_drawer_listview)
    ListView drawerListView;

    @Bind(R.id.fullname_textview)
    TextView fullNameTextView;

    private NavigationDrawerCallbacks navigationDrawerCallbacks;
    private NavigationDrawerCounterListener drawerCounterListener;
    private android.support.v7.app.ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;

    private int currentDrawerPosition;
    private boolean fromSavedInstanceState;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            navigationDrawerCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            currentDrawerPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(currentDrawerPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        activateButterKnife(rootView);

        initNavigationAdapter();

        drawerListView.setItemChecked(currentDrawerPosition, true);

        return rootView;
    }

    private void initNavigationAdapter() {
        NavigationDrawerAdapter navigationDrawerAdapter = new NavigationDrawerAdapter(baseActivity,
                getDrawerItems());
        drawerListView.setAdapter(navigationDrawerAdapter);
        drawerCounterListener = (NavigationDrawerCounterListener) navigationDrawerAdapter;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    private List<String> getDrawerItems() {
        String[] itemsArray = getResources().getStringArray(R.array.nvd_items_array);
        return Arrays.asList(itemsArray);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, currentDrawerPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
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
    public void onDetach() {
        super.onDetach();
        navigationDrawerCallbacks = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.LEFT);
    }

    public void setUp(DrawerLayout drawerLayout) {
        this.drawerLayout = drawerLayout;

        actionBarDrawerToggle = new SimpleActionBarDrawerToggle(baseActivity, drawerLayout, R.string.nvd_open,
                R.string.nvd_close);

        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        this.drawerLayout.setDrawerListener(actionBarDrawerToggle);

        if (!PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_USER_LEARNED_DRAWER, false) && !fromSavedInstanceState) {
            this.drawerLayout.openDrawer(Gravity.LEFT);
        }

        // Defer code dependent on restoration of previous instance state.
        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                actionBarDrawerToggle.syncState();
            }
        });
    }

    @OnItemClick(R.id.navigation_drawer_listview)
    public void selectItem(int position) {
        currentDrawerPosition = position;

        if (drawerListView != null) {
            drawerListView.setItemChecked(position, true);
        }

        if (drawerLayout != null) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }

        if (navigationDrawerCallbacks != null) {
            navigationDrawerCallbacks.onDrawerItemSelected(position);
        }
    }

    @OnClick(R.id.logout_imagebutton)
    public void logout(View view) {
        TwoButtonsDialogFragment.show(getFragmentManager(), R.string.dlg_logout, R.string.dlg_confirm,
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        baseActivity.showProgress();
                        FacebookHelper.logout();
                        QBLogoutCompositeCommand.start(baseActivity);
                    }
                });
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION, new LogoutSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOGOUT_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
    }

    private int getCountUnreadDialogs() {
        // TODO temp
        //        return ChatDatabaseManager.getCountUnreadDialogs(baseActivity);
        return 0;
    }

    public interface NavigationDrawerCounterListener {

        public void onUpdateCountUnreadDialogs(int count);
    }

    public interface NavigationDrawerCallbacks {

        void onDrawerItemSelected(int position);
    }

    private class CountUnreadDialogsBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                drawerCounterListener.onUpdateCountUnreadDialogs(getCountUnreadDialogs());
            }
        }
    }

    private class SimpleActionBarDrawerToggle extends android.support.v7.app.ActionBarDrawerToggle {

        public SimpleActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout,
                int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);

            Crouton.cancelAllCroutons();

            drawerCounterListener.onUpdateCountUnreadDialogs(getCountUnreadDialogs());

            if (!isAdded()) {
                return;
            }

            if (drawerListView != null) {
                drawerListView.setItemChecked(currentDrawerPosition, true);
            }
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
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