package com.quickblox.q_municate.ui.fragments.contacts;

import android.content.Context;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.listeners.UserSearchListener;
import com.quickblox.q_municate_core.core.loader.BaseLoader;
import com.quickblox.q_municate.ui.adapters.contacts.ContactsAdapter;
import com.quickblox.q_municate.utils.ContactsUtils;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.User;

import java.util.Collections;
import java.util.List;

public class YourContactsFragment extends BaseContactsFragment implements UserSearchListener {

    private final static int LOADER_ID = YourContactsFragment.class.hashCode();

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
        initDataLoader(LOADER_ID);

        return view;
    }

    @Override
    protected void initFields() {
        super.initFields();
        usersList = Collections.emptyList();
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
        if (contactsAdapter != null) {
            contactsAdapter.setUserType(ContactsAdapter.UserType.LOCAl);
            contactsAdapter.flushFilter();
        }
    }

    @Override
    public void search(String searchQuery) {
        if (contactsAdapter != null) {
            contactsAdapter.setFilter(searchQuery);
        }
    }

    @Override
    public void cancelSearch() {
        searchQuery = null;

        if (contactsAdapter != null) {
            contactsAdapter.flushFilter();
        }
    }

    @Override
    protected void updateContactsList() {
        onChangedData();
    }

    private void updateLocal() {
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

    @Override
    protected Loader<List<User>> createDataLoader() {
        return new UsersLoader(getActivity(), dataManager);
    }

    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> usersList) {
        this.usersList = usersList;
        updateLocal();
    }

    private static class UsersLoader extends BaseLoader<List<User>> {

        public UsersLoader(Context context, DataManager dataManager) {
            super(context, dataManager);
        }

        @Override
        protected List<User> getItems() {
            return ContactsUtils.createContactsList(dataManager);
        }
    }
}