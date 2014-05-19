package com.quickblox.qmunicate.ui.main;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBAddFriendCommand;
import com.quickblox.qmunicate.qb.commands.QBFriendsLoadCommand;
import com.quickblox.qmunicate.qb.commands.QBUserSearchCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class FriendsListFragment extends BaseFragment implements AdapterView.OnItemClickListener, OnRefreshListener, SearchView.OnQueryTextListener, FilterQueryProvider {

    private List<Friend> usersList;
    private UserListAdapter usersListAdapter;
    private LinearLayout globalSearchLayout;
    private State state;
    private String constraint;
    private boolean isImportInitialized;
    private ListView friendsListView;
    private TextView friendsTitle;
    private View friendsListViewTitle;
    private BaseAdapter friendsListAdapter;
    private PullToRefreshLayout pullToRefreshLayout;
    private int positionCounter;
    private boolean isHideSearchView;
    private Cursor friendsCursor;

    public static FriendsListFragment newInstance() {
        return new FriendsListFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addActionsAddFriend();
        setHasOptionsMenu(true);
    }

    private void addActionsAddFriend() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, failAction);
        baseActivity.addAction(QBServiceConsts.FRIENDS_LOAD_SUCCESS_ACTION, new FriendsLoadSuccessAction());
        baseActivity.addAction(QBServiceConsts.FRIENDS_LOAD_FAIL_ACTION, failAction);
        baseActivity.addAction(QBServiceConsts.USER_SEARCH_SUCCESS_ACTION, new UserSearchSuccessAction());
        baseActivity.addAction(QBServiceConsts.USER_SEARCH_FAIL_ACTION, new UserSearchFailAction());
        baseActivity.updateBroadcastActionList();
    }

    @Override
    public void onResume() {
        super.onResume();
        startFriendsListLoader();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friend_list_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(new SearchOnActionExpandListener());
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                friendsListView.addFooterView(globalSearchLayout);
                friendsTitle.setText(R.string.frl_friends);
                friendsListView.addHeaderView(friendsListViewTitle);
                positionCounter++;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startFriendsListLoader() {
        QBFriendsLoadCommand.start(baseActivity);
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        if (TextUtils.isEmpty(constraint)) {
            return null;
        }
        return DatabaseManager.fetchFriendsByFullname(baseActivity, constraint.toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getString(R.string.nvd_title_friends);
        state = State.FRIENDS_LIST;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = layoutInflater.inflate(R.layout.fragment_friend_list, container, false);

        isImportInitialized = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_IMPORT_INITIALIZED,
                false);

        initUI(rootView, layoutInflater);
        initGlobalSearchButton(layoutInflater);
        initPullToRefresh(rootView);
        initFriendsList();

        return rootView;
    }

    private void initUI(View view, LayoutInflater layoutInflater) {
        friendsListView = (ListView) view.findViewById(R.id.friendList);
        friendsListViewTitle = layoutInflater.inflate(R.layout.view_section_title_friends_list, null);
        friendsTitle = (TextView) friendsListViewTitle.findViewById(R.id.listTitle);
    }

    private void initGlobalSearchButton(LayoutInflater inflater) {
        globalSearchLayout = (LinearLayout) inflater.inflate(R.layout.view_global_search_button, null);
        globalSearchLayout.findViewById(R.id.globalSearchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startGlobalSearch();
                    }
                }
        );
    }

    private void initPullToRefresh(View view) {
        pullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.pullToRefreshLayout);
        ActionBarPullToRefresh.from(baseActivity).allChildrenArePullable().listener(this).setup(
                pullToRefreshLayout);
    }

    private void initFriendsList() {
        if (friendsCursor != null && !friendsCursor.isClosed()) {
            friendsCursor.close();
        }
        friendsCursor = getAllFriends();
        friendsListAdapter = new FriendsListCursorAdapter(baseActivity, friendsCursor);
        friendsListView.setAdapter(friendsListAdapter);
        friendsListView.setSelector(R.drawable.list_item_background_selector);
        friendsListView.setOnItemClickListener(this);
    }

    private void startGlobalSearch() {
        state = State.GLOBAL_LIST;
        friendsListView.removeFooterView(globalSearchLayout);
        initUserList();
    }

    private Cursor getAllFriends() {
        return DatabaseManager.getAllFriends(baseActivity);
    }

    private void initUserList() {
        usersList = new ArrayList<Friend>();
        usersListAdapter = new UserListAdapter(baseActivity, usersList,
                new UserListAdapter.UserListListener() {
                    @Override
                    public void onUserSelected(int position) {
                        addToFriendList(usersList.get(position));
                    }
                }
        );

        friendsTitle.setText(R.string.frl_all_users);

        friendsListView.setSelector(android.R.color.transparent);
        friendsListView.setAdapter(usersListAdapter);
        friendsListView.setOnItemClickListener(null);

        startUsersListLoader(constraint);
    }

    private void addToFriendList(final Friend friend) {
        baseActivity.showProgress();
        QBAddFriendCommand.start(baseActivity, friend);
    }

    private void startUsersListLoader(String newText) {
        QBUserSearchCommand.start(baseActivity, newText);
        usersListAdapter.setSearchCharacters(newText);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isImportInitialized) {
            addActionsAddFriends();
        }
    }

    private void addActionsAddFriends() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIENDS_SUCCESS_ACTION, new AddFriendsSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIENDS_FAIL_ACTION, new AddFriendsFailAction());
        baseActivity.updateBroadcastActionList();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        constraint = newText;
        if (state == State.FRIENDS_LIST) {
            FriendsListCursorAdapter friendListCursorAdapter = (FriendsListCursorAdapter) friendsListAdapter;
            friendListCursorAdapter.setFilterQueryProvider(this);
            friendListCursorAdapter.getFilter().filter(newText);
            friendListCursorAdapter.setSearchCharacters(newText);
        } else {
            startUsersListLoader(newText);
        }
        return true;
    }

    @Override
    public void onRefreshStarted(View view) {
        startFriendsListLoader();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        Cursor selectedItem = (Cursor) friendsListAdapter.getItem(position - positionCounter);
        FriendDetailsActivity.start(baseActivity, DatabaseManager.getFriendFromCursor(selectedItem));
    }

    private void updateUsersList(List<Friend> friendsList) {
        usersList.clear();
        usersList.addAll(friendsList);
        usersListAdapter.notifyDataSetChanged();
    }

    private void saveFriendToCache(Friend friend) {
        DatabaseManager.saveFriend(baseActivity, friend);
    }

    private void importFriendsFinished() {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
        startFriendsListLoader();
        baseActivity.hideProgress();
    }

    private void updateFriends(List<Friend> friendsList) {
        clearCachedFriends();
        saveFriendsToCache(friendsList);
    }

    private void clearCachedFriends() {
        DatabaseManager.deleteAllFriends(baseActivity);
    }

    private void saveFriendsToCache(List<Friend> friendsList) {
        DatabaseManager.saveFriends(baseActivity, friendsList);
    }

    private enum State {FRIENDS_LIST, GLOBAL_LIST}

    private class SearchOnActionExpandListener implements MenuItem.OnActionExpandListener {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            isHideSearchView = true;
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            state = State.FRIENDS_LIST;

            baseActivity.getActionBar().setDisplayShowHomeEnabled(true);

            if (isHideSearchView) {
                isHideSearchView = false;
                friendsListView.removeFooterView(globalSearchLayout);
                friendsListView.removeHeaderView(friendsListViewTitle);
                positionCounter--;
                initFriendsList();
            }

            return true;
        }
    }

    private class AddFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Friend friend = (Friend) bundle.getSerializable(QBServiceConsts.EXTRA_FRIEND);
            saveFriendToCache(friend);
            usersListAdapter.notifyDataSetChanged();
            baseActivity.hideProgress();
        }
    }

    private class FriendsLoadSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            List<Friend> friendsList = (List<Friend>) bundle.getSerializable(QBServiceConsts.EXTRA_FRIENDS);
            updateFriends(friendsList);
            pullToRefreshLayout.setRefreshComplete();
        }
    }

    private class UserSearchSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            List<Friend> friendsList = (List<Friend>) bundle.getSerializable(QBServiceConsts.EXTRA_FRIENDS);
            updateUsersList(friendsList);
        }
    }

    private class UserSearchFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception e = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            ErrorUtils.showError(baseActivity, e);
        }
    }

    private class AddFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            importFriendsFinished();
        }
    }

    private class AddFriendsFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            importFriendsFinished();
            DialogUtils.show(baseActivity, getResources().getString(R.string.dlg_no_friends_for_import));
        }
    }
}