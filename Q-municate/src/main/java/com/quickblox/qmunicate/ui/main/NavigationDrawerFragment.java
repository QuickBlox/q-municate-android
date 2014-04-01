package com.quickblox.qmunicate.ui.main;

import android.app.ActionBar;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.qb.QBLogoutCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.dialogs.ConfirmDialog;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.utils.FacebookHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;

public class NavigationDrawerFragment extends BaseFragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private View fragmentContainerView;
    private TextView fullName;
    private ImageButton logoutButton;

    private NavigationDrawerCallbacks callbacks;
    private ActionBarDrawerToggle drawerToggle;
    private int currentSelectedPosition = 0;
    private boolean fromSavedInstanceState;
    private boolean userLearnedDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userLearnedDrawer = App.getInstance().getPrefsHelper()
                .getPref(PrefsHelper.PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
        }

        selectItem(currentSelectedPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        drawerListView = (ListView) rootView.findViewById(R.id.navigationList);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        drawerListView.setAdapter(new ArrayAdapter<String>(baseActivity.getActionBar().getThemedContext(),
                R.layout.list_item_navigation_drawler, R.id.textView,
                new String[]{getString(R.string.nvd_title_friends), getString(
                        R.string.nvd_title_chats), getString(R.string.nvd_title_settings), getString(
                        R.string.nvd_title_invite_friends),}
        ));
        drawerListView.setItemChecked(currentSelectedPosition, true);
        logoutButton = (ImageButton) rootView.findViewById(R.id.logoutImageButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
        fullName = (TextView) rootView.findViewById(R.id.fullnameTextView);
        QBUser user = App.getInstance().getUser();
        if (user != null) {
            fullName.setText(user.getFullName());
        }

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (NavigationDrawerCallbacks) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        baseActivity.addAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION, new LogoutSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOGOUT_FAIL_ACTION, new BaseActivity.FailAction(baseActivity));
        baseActivity.updateBroadcastActionList();
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
        callbacks = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
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

    private void selectItem(int position) {
        currentSelectedPosition = position;
        if (drawerListView != null) {
            drawerListView.setItemChecked(position, true);
        }
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(fragmentContainerView);
        }
        if (callbacks != null) {
            callbacks.onNavigationDrawerItemSelected(position);
        }
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

            baseActivity.invalidateOptionsMenu();

            if (!isAdded()) {
                return;
            }

            if (!userLearnedDrawer) {
                userLearnedDrawer = true;
                saveUserLearnedDrawer();
            }
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);

            baseActivity.invalidateOptionsMenu();

            if (!isAdded()) {
                return;
            }
        }
    }

    private class LogoutSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            LoginActivity.start(baseActivity);
            baseActivity.finish();
        }
    }
}