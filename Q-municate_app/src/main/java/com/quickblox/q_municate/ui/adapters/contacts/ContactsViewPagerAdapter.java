package com.quickblox.q_municate.ui.adapters.contacts;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.quickblox.q_municate.core.listeners.UserSearchListener;
import com.quickblox.q_municate.ui.fragments.contacts.AllUsersFragment;
import com.quickblox.q_municate.ui.fragments.contacts.YourContactsFragment;

public class ContactsViewPagerAdapter extends FragmentStatePagerAdapter {

    public static final int COUNT_CONTACTS_TYPES = 2;
    public static final int POSITION_YOUR_CONTACTS = 0;
    public static final int POSITION_ALL_USERS = 1;

    private YourContactsFragment yourContactsFragment;
    private AllUsersFragment allUsersFragment;

    public ContactsViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        yourContactsFragment = YourContactsFragment.newInstance();
        allUsersFragment = AllUsersFragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position) {
            case POSITION_YOUR_CONTACTS:
                fragment = yourContactsFragment;
                break;
            case POSITION_ALL_USERS:
                fragment = allUsersFragment;
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return COUNT_CONTACTS_TYPES;
    }

    public void search(int position, String searchQuery) {
        UserSearchListener userSearchListener = getUserSearchListener(position);
        if (userSearchListener != null) {
            userSearchListener.search(searchQuery);
        }
    }

    public void cancelSearch(int position) {
        UserSearchListener userSearchListener = getUserSearchListener(position);
        if (userSearchListener != null) {
            userSearchListener.cancelSearch();
        }
    }

    private UserSearchListener getUserSearchListener(int position) {
        switch (position) {
            case POSITION_YOUR_CONTACTS:
                return yourContactsFragment;
            case POSITION_ALL_USERS:
                return allUsersFragment;
        }
        return null;
    }
}