package com.quickblox.q_municate.ui.fragments.contacts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.listeners.UserSearchListener;
import com.quickblox.q_municate.ui.adapters.contacts.ContactsAdapter;
import com.quickblox.q_municate.utils.ContactsUtils;
import com.quickblox.q_municate_core.service.QBService;

public class YourContactsFragment extends BaseContactsFragment implements UserSearchListener {

    public static YourContactsFragment newInstance() {
        return new YourContactsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_contacts_list, container, false);

        activateButterKnife(view);

        initFields();
        initContactsList(usersList);
        initCustomListeners();

        return view;
    }

    @Override
    protected void initFields() {
        super.initFields();
        usersList = ContactsUtils.createContactsList(dataManager);
        swipyRefreshLayout.setEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        checkVisibilityEmptyLabel();
        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void prepareSearch() {
        contactsAdapter.setUserType(ContactsAdapter.UserType.LOCAl);
        contactsAdapter.flushFilter();
    }

    @Override
    public void search(String searchQuery) {
        contactsAdapter.setFilter(searchQuery);
    }

    @Override
    public void cancelSearch() {
        searchQuery = null;
        contactsAdapter.flushFilter();
    }

    @Override
    protected void updateContactsList() {
        usersList = ContactsUtils.createContactsList(dataManager);
        contactsAdapter.setList(usersList);

        if (searchQuery != null) {
            search(searchQuery);
        }
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        contactsAdapter.setFriendListHelper(friendListHelper);
    }

    @Override
    public void onChangedUserStatus(int userId, boolean online) {
        super.onChangedUserStatus(userId, online);
        contactsAdapter.notifyDataSetChanged();
    }
}