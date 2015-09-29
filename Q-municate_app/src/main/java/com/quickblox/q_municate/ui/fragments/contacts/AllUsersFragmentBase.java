package com.quickblox.q_municate.ui.fragments.contacts;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayoutDirection;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.listeners.UserOperationListener;
import com.quickblox.q_municate.core.listeners.UserSearchListener;
import com.quickblox.q_municate.ui.adapters.contacts.ContactsAdapter;
import com.quickblox.q_municate.ui.fragments.dialogs.base.OneButtonDialogFragment;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBFindUsersCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.models.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AllUsersFragmentBase extends BaseContactsFragmentBase implements UserSearchListener, SwipyRefreshLayout.OnRefreshListener {

    private static final int SEARCH_DELAY = 1000;
    private static final int MIN_VALUE_FOR_SEARCH = 3;

    private Timer searchTimer;
    private int page = 1;
    private int totalEntries;
    private UserOperationAction userOperationAction;

    public static AllUsersFragmentBase newInstance() {
        return new AllUsersFragmentBase();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_contacts_list, container, false);

        activateButterKnife(view);

        initFields();
        addActions();
        initContactsList(usersList);
        initCustomListeners();

        return view;
    }

    @Override
    protected void initFields() {
        super.initFields();
        searchTimer = new Timer();
        usersList = new ArrayList<>();
        userOperationAction = new UserOperationAction();
        swipyRefreshLayout.setEnabled(false);
    }

    @Override
    protected void initContactsList(List<User> usersList) {
        super.initContactsList(usersList);
        contactsAdapter.setUserOperationListener(userOperationAction);
    }

    @Override
    protected void initCustomListeners() {
        super.initCustomListeners();

        swipyRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        checkVisibilityEmptyLabel();
        contactsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @Override
    protected void updateContactsList() {
        contactsAdapter.setList(usersList);
    }

    private void updateContactsList(List<User> usersList) {
        this.usersList = usersList;
        contactsAdapter.setList(usersList);
        contactsAdapter.setFilter(searchQuery);
    }

    private void removeActions() {
        baseActivity.removeAction(QBServiceConsts.FIND_USERS_SUCCESS_ACTION);
        baseActivity.removeAction(QBServiceConsts.FIND_USERS_FAIL_ACTION);

        baseActivity.removeAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION);
        baseActivity.removeAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION);

        baseActivity.updateBroadcastActionList();
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.FIND_USERS_SUCCESS_ACTION, new FindUserSuccessAction());
        baseActivity.addAction(QBServiceConsts.FIND_USERS_FAIL_ACTION, new FindUserFailAction());

        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, failAction);

        baseActivity.updateBroadcastActionList();
    }

    @Override
    public void prepareSearch() {
        clearOldData();

        if (contactsAdapter != null) {
            contactsAdapter.setUserType(ContactsAdapter.UserType.GLOBAL);
            updateContactsList();
        }
    }

    @Override
    public void search(String searchQuery) {
        this.searchQuery = searchQuery;
        clearOldData();
        startSearch();
    }

    @Override
    public void cancelSearch() {
        searchQuery = null;
        searchTimer.cancel();
        clearOldData();

        if (contactsAdapter != null) {
            updateContactsList();
        }
    }

    private void clearOldData() {
        usersList.clear();
        page = 1;
    }

    private void startSearch() {
        searchTimer.cancel();
        searchTimer = new Timer();
        searchTimer.schedule(new SearchTimerTask(), SEARCH_DELAY);
    }

    private void searchUsers() {
        if (!TextUtils.isEmpty(searchQuery) && checkSearchDataWithError(searchQuery)) {
            QBFindUsersCommand.start(baseActivity, AppSession.getSession().getUser(), searchQuery, page);
        }
    }

    private boolean checkSearchDataWithError(String searchQuery) {
        boolean correct = searchQuery != null && searchQuery.length() >= MIN_VALUE_FOR_SEARCH;
        if (correct) {
            return true;
        } else {
            OneButtonDialogFragment.show(getChildFragmentManager(), R.string.search_at_last_items, true);
            return false;
        }
    }

    private void addToFriendList(final int userId) {
        baseActivity.showProgress();
        QBAddFriendCommand.start(baseActivity, userId);
        KeyboardUtils.hideKeyboard(baseActivity);
    }

    @Override
    public void onRefresh(SwipyRefreshLayoutDirection swipyRefreshLayoutDirection) {
        if (!usersList.isEmpty() && usersList.size() < totalEntries) {
            page++;
            searchUsers();
        }
    }

    private void checkForEnablingRefreshLayout() {
        swipyRefreshLayout.setEnabled(usersList.size() != totalEntries);
    }

    private void parseResult(Bundle bundle) {
        String searchQuery = bundle.getString(QBServiceConsts.EXTRA_CONSTRAINT);
        totalEntries = bundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES);

        if (AllUsersFragmentBase.this.searchQuery.equals(searchQuery)) {
            Collection<User> newUsersCollection = (Collection<User>) bundle.getSerializable(QBServiceConsts.EXTRA_USERS);
            if (newUsersCollection != null && !newUsersCollection.isEmpty()) {
                usersList.addAll(newUsersCollection);

                updateContactsList(usersList);
            }
        } else {
            search(AllUsersFragmentBase.this.searchQuery);
        }
    }

    private class SearchTimerTask extends TimerTask {

        @Override
        public void run() {
            searchUsers();
        }
    }

    private class FindUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            parseResult(bundle);

            swipyRefreshLayout.setRefreshing(false);
            checkForEnablingRefreshLayout();
        }
    }

    private class FindUserFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            OneButtonDialogFragment.show(getChildFragmentManager(), R.string.frl_not_found_users, true);
            usersList.clear();

            swipyRefreshLayout.setRefreshing(false);
            checkForEnablingRefreshLayout();
        }
    }

    private class UserOperationAction implements UserOperationListener {

        @Override
        public void onAddUserClicked(int userId) {
            addToFriendList(userId);
        }
    }

    private class AddFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            int userId = bundle.getInt(QBServiceConsts.EXTRA_FRIEND_ID);

            User addedUser = dataManager.getUserDataManager().get(userId);
            contactsAdapter.notifyDataSetChanged();

            baseActivity.hideProgress();
        }
    }
}