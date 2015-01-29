package com.quickblox.q_municate.ui.friends;

import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.db.tables.FriendTable;
import com.quickblox.q_municate_core.db.tables.UserTable;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.FriendGroup;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBFindUsersCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FriendsListFragment extends BaseFragment implements SearchView.OnQueryTextListener, AbsListView.OnScrollListener {

    private static final int SEARCH_DELAY = 1000;

    private State state;
    private String constraint;
    private ExpandableListView friendsListView;
    private TextView emptyListTextView;
    private FriendsListAdapter friendsListAdapter;
    private SearchView searchView;
    private Toast errorToast;
    private ContentObserver userTableContentObserver;
    private ContentObserver friendTableContentObserver;
    private FriendOperationAction friendOperationAction;
    private Resources resources;
    private Timer searchTimer;

    private int firstVisiblePositionList;
    private View listLoadingView;
    private boolean loadingMore;
    private int page = -1; // first loading
    private int totalEntries;
    private int loadedItems;
    private int lastItemInScreen;
    private int totalItemCountInList;

    private List<FriendGroup> friendGroupList;
    private FriendGroup friendGroupAllFriends;
    private FriendGroup friendGroupAllUsers;

    public static FriendsListFragment newInstance() {
        return new FriendsListFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        checkVisibilityEmptyLabel();

        if (page == -1) {
            friendsListView.removeFooterView(listLoadingView);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterContentObservers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friend_list_menu, menu);
        SearchOnActionExpandListener searchOnActionExpandListener = new SearchOnActionExpandListener();
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(searchOnActionExpandListener);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    private void registerContentObservers() {
        userTableContentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                updateAllFriendsData();
            }
        };

        friendTableContentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                updateAllFriendsData();
            }
        };

        baseActivity.getContentResolver().registerContentObserver(UserTable.CONTENT_URI, true,
                userTableContentObserver);
        baseActivity.getContentResolver().registerContentObserver(FriendTable.CONTENT_URI, true,
                friendTableContentObserver);
    }

    private void updateAllFriendsData() {
        firstVisiblePositionList = friendsListView.getFirstVisiblePosition();
        updateAllFriends();
        initFriendAdapter();

        if (!TextUtils.isEmpty(constraint)) {
            performQueryTextChange();
        }

        checkVisibilityEmptyLabel();

        friendsListView.setSelection(firstVisiblePositionList);
    }

    private void unregisterContentObservers() {
        baseActivity.getContentResolver().unregisterContentObserver(userTableContentObserver);
        baseActivity.getContentResolver().unregisterContentObserver(friendTableContentObserver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getString(R.string.nvd_title_contacts);
        state = State.FRIENDS_LIST;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = layoutInflater.inflate(R.layout.fragment_friend_list, container, false);

        resources = getResources();
        friendOperationAction = new FriendOperationAction();
        searchTimer = new Timer();
        friendGroupList = new ArrayList<FriendGroup>();

        initUI(rootView);
        initListeners();
        registerContentObservers();
        addActions();

        initFriendList();

        return rootView;
    }

    private void initUI(View view) {
        friendsListView = (ExpandableListView) view.findViewById(R.id.friends_expandablelistview);
        listLoadingView = baseActivity.getLayoutInflater().inflate(R.layout.view_load_more, null);
        friendsListView.addFooterView(listLoadingView);
        emptyListTextView = (TextView) view.findViewById(R.id.empty_list_textview);
    }

    private void initListeners() {
        friendsListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.hideKeyboard(baseActivity);
                return false;
            }
        });

        friendsListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // nothing do
                return true;
            }
        });

        friendsListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                    int childPosition, long id) {
                User selectedUser = (User) friendsListAdapter.getChild(groupPosition, childPosition);
                boolean isFriend = UsersDatabaseManager.isFriendInBaseWithPending(baseActivity,
                        selectedUser.getUserId());
                if (isFriend) {
                    startFriendDetailsActivity(selectedUser.getUserId());
                }
                return false;
            }
        });
    }

    private void initFriendList() {
        friendGroupList.clear();

        initAllFriends();
        initAllUsers();
        initFriendAdapter();
    }

    private void initAllFriends() {
        int countFriends = UsersDatabaseManager.getAllFriendsCountWithPending(baseActivity);
        friendGroupAllFriends = new FriendGroup(FriendGroup.GROUP_POSITION_MY_CONTACTS, resources.getString(
                R.string.frl_column_header_name_my_contacts));
        friendGroupAllFriends.setUserList(new ArrayList<User>(countFriends));

        if (countFriends > ConstsCore.ZERO_INT_VALUE) {
            List<User> friendList = UsersDatabaseManager.getAllFriendsList(baseActivity);
            friendGroupAllFriends.addUserList(friendList);
        }

        friendGroupList.add(friendGroupAllFriends);
    }

    private void initAllUsers() {
        friendGroupAllUsers = new FriendGroup(FriendGroup.GROUP_POSITION_ALL_USERS, resources.getString(
                R.string.frl_column_header_name_all_users));
        friendGroupAllUsers.setUserList(new ArrayList<User>());

        friendGroupList.add(friendGroupAllUsers);
    }

    private void updateAllFriends() {
        int countFriends = UsersDatabaseManager.getAllFriendsCountWithPending(baseActivity);
        friendGroupAllFriends.getUserList().clear();

        if (countFriends > ConstsCore.ZERO_INT_VALUE) {
            List<User> friendList = UsersDatabaseManager.getAllFriendsList(baseActivity);
            friendGroupAllFriends.addUserList(friendList);
        }
    }

    private void initFriendAdapter() {
        sortLists();

        friendsListAdapter = new FriendsListAdapter(baseActivity, friendOperationAction, friendGroupList);
        friendsListAdapter.setSearchCharacters(constraint);
        friendsListView.setAdapter(friendsListAdapter);
        friendsListView.setGroupIndicator(null);
        friendsListView.setOnScrollListener(this);

        expandAll();
    }

    private void sortLists() {
        UserComparator userComparator = new UserComparator();
        Collections.sort(friendGroupList.get(FriendGroup.GROUP_POSITION_ALL_USERS).getUserList(),
                userComparator);
        Collections.sort(friendGroupList.get(FriendGroup.GROUP_POSITION_MY_CONTACTS).getUserList(),
                userComparator);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        constraint = query;
        KeyboardUtils.hideKeyboard(baseActivity);
        friendsListAdapter.filterData(query);
        expandAll();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        if (state.equals(State.GLOBAL_LIST)) {
            constraint = query;
            page = 1; // first page for loading items

            initFriendList();

            if (!TextUtils.isEmpty(constraint)) {
                performQueryTextChange();
            } else {
                baseActivity.hideActionBarProgress();
            }

            checkUsersListLoader();
        }

        return true;
    }

    private void performQueryTextChange() {
        friendsListAdapter.filterData(constraint);
        expandAll();
    }

    private void removeActions() {
        baseActivity.removeAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION);
        baseActivity.removeAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION);

        baseActivity.removeAction(QBServiceConsts.FIND_USERS_SUCCESS_ACTION);
        baseActivity.removeAction(QBServiceConsts.FIND_USERS_FAIL_ACTION);
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, failAction);

        baseActivity.addAction(QBServiceConsts.FIND_USERS_SUCCESS_ACTION, new FindUserSuccessAction());
        baseActivity.addAction(QBServiceConsts.FIND_USERS_FAIL_ACTION, new FindUserFailAction());

        baseActivity.updateBroadcastActionList();
    }

    private void startFriendDetailsActivity(int userId) {
        FriendDetailsActivity.start(baseActivity, userId);
    }

    private void expandAll() {
        int count = friendsListAdapter.getGroupCount();
        for (int i = 0; i < count; i++) {
            friendsListView.expandGroup(i);
        }
    }

    private void addToFriendList(final int userId) {
        baseActivity.showProgress();
        QBAddFriendCommand.start(baseActivity, userId);
        KeyboardUtils.hideKeyboard(baseActivity);
        searchView.clearFocus();
    }

    private void checkUsersListLoader() {
        searchTimer.cancel();
        searchTimer = new Timer();
        searchTimer.schedule(new SearchTimerTask(), SEARCH_DELAY);
        baseActivity.showActionBarProgress();
    }

    private void findUsers() {
        if (TextUtils.isEmpty(constraint)) {
            return;
        }

        loadingMore = true;

        QBFindUsersCommand.start(baseActivity, AppSession.getSession().getUser(), constraint, page);
    }

    private void showErrorToast(String error) {
        if (errorToast != null) {
            errorToast.cancel();
        }
        errorToast = ErrorUtils.getErrorToast(baseActivity, error);
        errorToast.show();
    }

    private void checkVisibilityEmptyLabel() {
        if (state == State.GLOBAL_LIST) {
            emptyListTextView.setVisibility(View.GONE);
        } else {
            int countFriends = UsersDatabaseManager.getAllFriendsCountWithPending(baseActivity);

            if ((countFriends + friendGroupAllUsers.getUserList().size()) > ConstsCore.ZERO_INT_VALUE) {
                emptyListTextView.setVisibility(View.GONE);
            } else {
                emptyListTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            if ((lastItemInScreen == totalItemCountInList) && !loadingMore && state == State.GLOBAL_LIST) {
                if (TextUtils.isEmpty(constraint)) {
                    return;
                }

                firstVisiblePositionList = totalItemCountInList - 1;
                int currentPage = (page - 1);
                loadedItems = currentPage * ConstsCore.FL_FRIENDS_PER_PAGE;

                if (ConstsCore.FL_FRIENDS_PER_PAGE < totalEntries) {
                    loadMoreItems();
                }
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        lastItemInScreen = firstVisibleItem + visibleItemCount;
        totalItemCountInList = totalItemCount;
    }

    private void loadMoreItems() {
        if (!friendGroupList.isEmpty()) {
            if (loadedItems < totalEntries) {
                friendsListView.addFooterView(listLoadingView);
                findUsers();
                page++;
            }
        } else {
            friendsListView.addFooterView(listLoadingView);
            findUsers();
            page++;
        }
    }

    private void updateFriendList(Collection<User> newUserCollection) {
        friendGroupAllUsers.removeFriendsFromList(new ArrayList<User>(newUserCollection));
        friendGroupAllUsers.addUserList(new ArrayList<User>(newUserCollection));
        friendGroupAllUsers.removeFriendsFromList(friendGroupAllFriends.getUserList());

        initFriendAdapter();

        performQueryTextChange();
    }

    private void cancelSearch() {
        state = State.FRIENDS_LIST;

        constraint = null;
        initFriendList();
        checkVisibilityEmptyLabel();

        baseActivity.hideActionBarProgress();
    }

    private enum State {FRIENDS_LIST, GLOBAL_LIST}

    private class SearchTimerTask extends TimerTask {

        @Override
        public void run() {
            findUsers();
        }
    }

    private class SearchOnActionExpandListener implements MenuItem.OnActionExpandListener {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            state = State.GLOBAL_LIST;
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            if (state == State.GLOBAL_LIST) {
                cancelSearch();
            }
            return true;
        }
    }

    private class FriendOperationAction implements FriendOperationListener {

        @Override
        public void onAddUserClicked(int userId) {
            addToFriendList(userId);
        }
    }

    private class AddFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            int userId = bundle.getInt(QBServiceConsts.EXTRA_FRIEND_ID);

            User addedUser = UsersDatabaseManager.getUserById(baseActivity, userId);
            friendGroupAllFriends.getUserList().add(addedUser);
            friendGroupAllUsers.getUserList().remove(addedUser);
            initFriendAdapter();

            baseActivity.hideProgress();
        }
    }

    private class FindUserSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            totalEntries = bundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES);
            loadingMore = false;

            if (FriendsListFragment.this.constraint.equals(constraint)) {
                Collection<User> newUsersCollection = (Collection<User>) bundle.getSerializable(
                        QBServiceConsts.EXTRA_USERS);
                if (!newUsersCollection.isEmpty()) {
                    updateFriendList(newUsersCollection);
                }
            } else {
                onQueryTextChange(FriendsListFragment.this.constraint);
            }

            checkVisibilityEmptyLabel();

            friendsListView.removeFooterView(listLoadingView);
            friendsListView.setSelection(firstVisiblePositionList);
            baseActivity.hideActionBarProgress();
        }
    }

    private class FindUserFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            String notFoundError = getResources().getString(R.string.frl_not_found_users);
            showErrorToast(notFoundError);

            checkVisibilityEmptyLabel();

            friendsListView.removeFooterView(listLoadingView);
            baseActivity.hideActionBarProgress();
        }
    }

    private class UserComparator implements Comparator<User> {

        @Override
        public int compare(User firstUser, User secondUser) {
            if (firstUser.getFullName() == null || secondUser.getFullName() == null) {
                return 0;
            }

            return String.CASE_INSENSITIVE_ORDER.compare(firstUser.getFullName(), secondUser.getFullName());
        }
    }
}