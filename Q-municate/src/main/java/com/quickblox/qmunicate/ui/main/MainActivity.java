package com.quickblox.qmunicate.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment navigationDrawerFragment;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        useDoubleBackPressed = true;

        navigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        navigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = FriendListFragment.newInstance();
                break;
            case 1:
                fragment = ChatListFragment.newInstance();
                break;
            case 2:
                fragment = SettingsFragment.newInstance();
                break;
        }
        setCurrentFragment(fragment);
    }

    public void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction ft = buildTransaction();
        ft.replace(R.id.container, fragment, null);
        ft.commit();
    }

    public void addFragment(Fragment fragment) {
        FragmentTransaction ft = buildTransaction();
        ft.addToBackStack(((Object) fragment).getClass().getName());
        ft.replace(R.id.container, fragment, null);
        ft.commit();
    }

    protected FragmentTransaction buildTransaction() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        return ft;
    }
}
