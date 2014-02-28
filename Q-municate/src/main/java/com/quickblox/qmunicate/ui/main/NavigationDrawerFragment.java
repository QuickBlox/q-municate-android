package com.quickblox.qmunicate.ui.main;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBLogoutTask;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

public class NavigationDrawerFragment extends BaseFragment {

    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private NavigationDrawerCallbacks callbacks;

    private ActionBarDrawerToggle drawerToggle;

    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private View fragmentContainerView;
    private TextView email;
    private ImageButton logoutButton;

    private int currentSelectedPosition = 0;
    private boolean fromSavedInstanceState;
    private boolean userLearnedDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userLearnedDrawer = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            currentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            fromSavedInstanceState = true;
        }

        selectItem(currentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        drawerListView = (ListView) rootView.findViewById(R.id.navigationList);
        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });
        drawerListView.setAdapter(new ArrayAdapter<String>(
                getActionBar().getThemedContext(),
                R.layout.list_item_navigation_drawler,
                R.id.textView,
                new String[]{
                        getString(R.string.nvd_title_friends),
                        getString(R.string.nvd_title_chats),
                        getString(R.string.nvd_title_settings),
                }));
        drawerListView.setItemChecked(currentSelectedPosition, true);
        email = (TextView) rootView.findViewById(R.id.email);
        email.setText(App.getInstance().getUser().getEmail());
        logoutButton = (ImageButton) rootView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new QBLogoutTask(getActivity()).execute();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (NavigationDrawerCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public boolean isDrawerOpen() {
        return drawerLayout != null && drawerLayout.isDrawerOpen(fragmentContainerView);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        fragmentContainerView = getActivity().findViewById(fragmentId);
        this.drawerLayout = drawerLayout;

        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        drawerToggle = new QMActionBarDrawlerToggle(getActivity(),
                NavigationDrawerFragment.this.drawerLayout,
                R.drawable.ic_drawer,
                R.string.nvd_open,
                R.string.nvd_close
        );

        if (!userLearnedDrawer && !fromSavedInstanceState) {
            this.drawerLayout.openDrawer(fragmentContainerView);
        }

        this.drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                drawerToggle.syncState();
            }
        });

        this.drawerLayout.setDrawerListener(drawerToggle);
    }

    private void saveUserLearnedDrawler() {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_USER_LEARNED_DRAWER, true);
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

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerItemSelected(int position);
    }

    private class QMActionBarDrawlerToggle extends ActionBarDrawerToggle {

        public QMActionBarDrawlerToggle(FragmentActivity activity, DrawerLayout drawerLayout, int drawlerImageRes,
                                        int openDrawlerContentDescRes, int closeDrawlerContentDescRes) {
            super(activity, drawerLayout, drawlerImageRes, openDrawlerContentDescRes, closeDrawlerContentDescRes);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            if (!isAdded()) {
                return;
            }

            getActivity().supportInvalidateOptionsMenu();
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            if (!isAdded()) {
                return;
            }

            if (!userLearnedDrawer) {
                userLearnedDrawer = true;
                saveUserLearnedDrawler();
            }

            getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
        }
    }
}
