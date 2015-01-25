package com.quickblox.q_municate.ui.friends;

import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MatrixCursor;
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
import android.widget.ExpandableListAdapter;
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
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate_core.qb.commands.QBFindUsersCommand;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.FriendUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

public class FriendsListFragment extends BaseFragment implements SearchView.OnQueryTextListener, AbsListView.OnScrollListener {

    private static final int SEARCH_DELAY = 1000;

    private State state;
    private String constraint;
    private ExpandableListView friendsListView;
    private TextView emptyListTextView;
    private FriendsListCursorAdapter friendsListAdapter;
    private SearchOnActionExpandListener searchOnActionExpandListener;
    private MenuItem searchItem;
    private SearchView searchView;
    private Toast errorToast;
    private ContentObserver userTableContentObserver;
    private ContentObserver friendTableContentObserver;
    private MatrixCursor headersCursor;
    private Collection<User> userCollection;
    private MatrixCursor searchResultCursor;
    private FriendOperationAction friendOperationAction;
    private Resources resources;
    private Timer searchTimer;
    private int firstVisiblePositionList;

    private View listLoadingView;
    private boolean loadingMore;
    private int page = -1; // first loading
    private int totalEntries;
    private int loadedItems;

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
        if (State.GLOBAL_LIST.equals(state)) {
            initFriendsListForSearch();
        } else {
            initFriendsList();
        }
        checkVisibilityEmptyLabel();

        if (page == -1) {
            friendsListView.removeFooterView(listLoadingView);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterContentObservers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friend_list_menu, menu);
        searchOnActionExpandListener = new SearchOnActionExpandListener();
        searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(searchOnActionExpandListener);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    private void registerContentObservers() {
        userTableContentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                firstVisiblePositionList = friendsListView.getFirstVisiblePosition();
                checkInitFriendsList();
            }
        };

        friendTableContentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                checkInitFriendsList();
            }
        };

        baseActivity.getContentResolver().registerContentObserver(UserTable.CONTENT_URI, true,
                userTableContentObserver);
        baseActivity.getContentResolver().registerContentObserver(FriendTable.CONTENT_URI, true,
                friendTableContentObserver);
    }

    private void checkInitFriendsList() {
        if (State.GLOBAL_LIST.equals(state)) {
            initFriendsListForSearch();
        } else {
            initFriendsList();
        }
        checkVisibilityEmptyLabel();
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
        userCollection = new TreeSet<User>();
        searchTimer = new Timer();

        initUI(rootView);
        initListeners();
        registerContentObservers();

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        addActions();
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
                Cursor selectedItem = friendsListAdapter.getChild(groupPosition, childPosition);
                if (selectedItem.getCount() != ConstsCore.ZERO_INT_VALUE && !selectedItem.isBeforeFirst()) {
                    User selectedUser = UsersDatabaseManager.getUserFromCursor(selectedItem);
                    startFriendDetailsActivity(selectedUser.getUserId());
                }
                return false;
            }
        });
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        KeyboardUtils.hideKeyboard(baseActivity);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        constraint = newText;

        if (TextUtils.isEmpty(newText)) {
            return true;
        }

        page = 1; // first page for loading items

        clearUserCollection();

        if (State.GLOBAL_LIST.equals(state)) {
            friendsListView.removeFooterView(listLoadingView);
            checkUsersListLoader();
        }

        return true;
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, failAction);

        baseActivity.addAction(QBServiceConsts.LOAD_USERS_SUCCESS_ACTION, new UserSearchSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_USERS_FAIL_ACTION, new UserSearchFailAction());

        baseActivity.addAction(QBServiceConsts.LOAD_FRIENDS_SUCCESS_ACTION, new LoadFriendsSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_FRIENDS_FAIL_ACTION, failAction);

        baseActivity.updateBroadcastActionList();
    }

    private void initUI(View view) {
        friendsListView = (ExpandableListView) view.findViewById(R.id.friends_expandablelistview);
        listLoadingView = baseActivity.getLayoutInflater().inflate(R.layout.view_search_footer_loading, null);
        friendsListView.addFooterView(listLoadingView);
        emptyListTextView = (TextView) view.findViewById(R.id.empty_list_textview);
    }

    private void initFriendsList() {
        baseActivity.showActionBarProgress();

        int countFriends = UsersDatabaseManager.getAllFriendsCountWithPending(baseActivity);
        createHeadersCursor(countFriends);

        friendsListAdapter = new FriendsListCursorAdapter(baseActivity, headersCursor, null,
                friendOperationAction, false);

        updateFriendsList();
    }

    private void initFriendsListForSearch() {
        int countFriends = UsersDatabaseManager.getAllFriendsCountByFullNameWithPending(baseActivity, constraint);
        createHeadersCursor(countFriends);

        friendsListAdapter = new FriendsListCursorAdapter(baseActivity, headersCursor, searchResultCursor,
                friendOperationAction, true);
        friendsListAdapter.setSearchCharacters(constraint);

        updateFriendsList();
    }

    private void updateFriendsList() {
        friendsListView.setAdapter(friendsListAdapter);
        friendsListView.setGroupIndicator(null);
        friendsListView.setOnScrollListener(this);

        expandAllGroups(headersCursor);

        if (firstVisiblePositionList != ConstsCore.ZERO_INT_VALUE) {
            friendsListView.setSelection(firstVisiblePositionList);
        }

        baseActivity.hideActionBarProgress();
    }

    private void startFriendDetailsActivity(int userId) {
        FriendDetailsActivity.start(baseActivity, userId);
    }

    private void expandAllGroups(Cursor headersCursor) {
        for (int i = 0; i < headersCursor.getCount(); i++) {
            friendsListView.expandGroup(i);
        }
    }

    private void createHeadersCursor(int countFriends) {
        headersCursor = new MatrixCursor(
                new String[]{FriendsListCursorAdapter.HEADER_COLUMN_ID, FriendsListCursorAdapter.HEADER_COLUMN_STATUS_NAME, FriendsListCursorAdapter.HEADER_COLUMN_HEADER_NAME});

        if (countFriends > ConstsCore.ZERO_INT_VALUE) {
            headersCursor.addRow(new String[]{UsersDatabaseManager.getRelationStatusIdByName(baseActivity,
                    QBFriendListHelper.RELATION_STATUS_BOTH) + ConstsCore.EMPTY_STRING, QBFriendListHelper.RELATION_STATUS_BOTH, resources
                    .getString(R.string.frl_column_header_name_contacts)});
        }

        if (state == State.GLOBAL_LIST) {
            headersCursor.addRow(
                    new String[]{QBFriendListHelper.VALUE_RELATION_STATUS_ALL_USERS + ConstsCore.EMPTY_STRING, QBFriendListHelper.RELATION_STATUS_ALL_USERS, resources
                            .getString(R.string.frl_column_header_name_all_users)});
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

    private void startUsersListLoader() {
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

            if ((countFriends + userCollection.size()) > ConstsCore.ZERO_INT_VALUE) {
                emptyListTextView.setVisibility(View.GONE);
            } else {
                emptyListTextView.setVisibility(View.VISIBLE);
                friendsListView.setAdapter((ExpandableListAdapter) null);
            }
        }
    }

    private void initAfterSuccess() {
        searchResultCursor = FriendUtils.createSearchResultCursor(baseActivity, userCollection);
        initFriendsListForSearch();
        checkVisibilityEmptyLabel();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        int lastInScreen = firstVisibleItem + visibleItemCount;
        if ((lastInScreen == totalItemCount) && !loadingMore && state == State.GLOBAL_LIST) {
            if (TextUtils.isEmpty(constraint)) {
                return;
            }

            firstVisiblePositionList = totalItemCount - 1;
            int currentPage = (page - 1);
            loadedItems = currentPage * ConstsCore.FL_FRIENDS_PER_PAGE;

            loadMoreItems();
        }
    }

    private void loadMoreItems() {
        if (userCollection != null && !userCollection.isEmpty()) {
            if (loadedItems < totalEntries) {
                friendsListView.addFooterView(listLoadingView);
                loadingMore = true;
                startUsersListLoader();
                page++;
            }
        } else {
            friendsListView.addFooterView(listLoadingView);
            loadingMore = true;
            startUsersListLoader();
            page++;
        }
    }

    private void prepareUserList(Collection<User> newUserCollection) {
        if (!userCollection.isEmpty()) {
            userCollection.addAll(newUserCollection);
        } else {
            userCollection = newUserCollection;
        }
    }

    private void clearUserCollection() {
        if (userCollection != null) {
            userCollection.clear();
        }
    }

    private enum State {FRIENDS_LIST, GLOBAL_LIST}

    private class SearchTimerTask extends TimerTask {

        @Override
        public void run() {
            startUsersListLoader();
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
            state = State.FRIENDS_LIST;

            initFriendsList();
            friendsListAdapter.setSearchCharacters(null);
            clearUserCollection();
            checkVisibilityEmptyLabel();

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
            userCollection.remove(addedUser);
            initAfterSuccess();
            baseActivity.hideProgress();
        }
    }

    private class UserSearchSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            String constraint = bundle.getString(QBServiceConsts.EXTRA_CONSTRAINT);
            totalEntries = bundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES);

            loadingMore = false;

            if (FriendsListFragment.this.constraint.equals(constraint)) {
                Collection<User> newUsersList = (Collection<User>) bundle.getSerializable(QBServiceConsts.EXTRA_USERS);
                prepareUserList(newUsersList);
                initAfterSuccess();
            } else {
                onQueryTextChange(FriendsListFragment.this.constraint);
            }

            baseActivity.hideActionBarProgress();

            friendsListView.removeFooterView(listLoadingView);
            friendsListView.setSelection(firstVisiblePositionList);
        }
    }

    private class UserSearchFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            String notFoundError = getResources().getString(R.string.frl_not_found_users);
            showErrorToast(notFoundError);
            baseActivity.hideActionBarProgress();
            friendsListView.removeFooterView(listLoadingView);
            clearUserCollection();
        }
    }

    private class LoadFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            initFriendsList();
            checkVisibilityEmptyLabel();
            friendsListView.removeFooterView(listLoadingView);
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