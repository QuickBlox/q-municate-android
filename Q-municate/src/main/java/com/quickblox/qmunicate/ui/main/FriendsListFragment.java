package com.quickblox.qmunicate.ui.main;

import android.app.Activity;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.SearchView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBAddFriendCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class FriendsListFragment extends AbsFriendsListFragment implements SearchView.OnQueryTextListener, FilterQueryProvider {

    private static final String TAG = FriendsListFragment.class.getSimpleName();

    private List<Friend> usersList;
    private UserListAdapter usersListAdapter;
    private LinearLayout globalSearchLayout;
    private State state;
    private String constraint;
    private boolean isImportInitialized;

    public static FriendsListFragment newInstance() {
        return new FriendsListFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addActionsAddFriend();
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addActionsAddFriend() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, new BaseActivity.FailAction(baseActivity));
        baseActivity.updateBroadcastActionList();
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        return DatabaseManager.fetchFriendsByFullname(baseActivity, constraint.toString());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        title = getString(R.string.nvd_title_friends);
        state = State.FRIENDS_LIST;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isImportInitialized) {
            addActionsAddFriends();
        }
        initFriendList();
    }

    private void addActionsAddFriends() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIENDS_SUCCESS_ACTION, new AddFriendsSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIENDS_FAIL_ACTION, new AddFriendsFailAction());
        baseActivity.updateBroadcastActionList();
    }

    @Override
    public void onLoaderResult(int id, List<Friend> data) {
        switch (id) {
            case FriendsListLoader.ID:
                clearCachedFriends();
                saveFriendsToCache(data);
                pullToRefreshLayout.setRefreshComplete();
                break;
            case UserListLoader.ID:
                usersList.clear();
                usersList.addAll(data);
                usersListAdapter.notifyDataSetChanged();
                break;
        }
    }

    private void clearCachedFriends() {
        DatabaseManager.deleteAllFriends(baseActivity);
    }

    private void saveFriendsToCache(List<Friend> friendsList) {
        DatabaseManager.saveFriends(baseActivity, friendsList);
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
        } else {
            startUserListLoader(newText);
        }
        return true;
    }

    @Override
    public void onRefreshStarted(View view) {
        updateFriendsList(FriendsListLoader.ID);
    }

    private void startUserListLoader(String newText) {
        runLoader(UserListLoader.ID, UserListLoader.newArguments(newText, Consts.FL_FRIENDS_PAGE_NUM,
                Consts.FL_FRIENDS_PER_PAGE));
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        Cursor selectedItem = (Cursor) friendsListAdapter.getItem(--position);
        FriendDetailsActivity.start(baseActivity, DatabaseManager.getFriendFromCursor(selectedItem));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        isImportInitialized = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);

        initGlobalSearchButton(inflater);

        return view;
    }

    @Override
    protected BaseAdapter getFriendsAdapter() {
        return new FriendsListCursorAdapter(baseActivity, getAllFriends());
    }

    @Override
    public Loader<LoaderResult<List<Friend>>> onLoaderCreate(int id, Bundle args) {
        Loader<LoaderResult<List<Friend>>> resultLoader = super.onLoaderCreate(id, args);
        if (resultLoader != null) {
            return resultLoader;
        }
        switch (id) {
            case UserListLoader.ID:
                return new UserListLoader(baseActivity);
            default:
                return null;
        }
    }

    @Override
    protected AbsFriendsListLoader onFriendsLoaderCreate(Activity activity, Bundle args) {
        return new FriendsListLoader(activity);
    }

    private Cursor getAllFriends() {
        return DatabaseManager.getAllFriends(baseActivity);
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

    private void startGlobalSearch() {
        state = State.GLOBAL_LIST;
        friendsTitle.setText(R.string.frl_all_users);
        hideGlobalSearchButton();
        initUserList();
    }

    private void hideGlobalSearchButton() {
        if (globalSearchLayout != null) {
            friendsListView.removeFooterView(globalSearchLayout);
        }
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
        friendsListView.setSelector(android.R.color.transparent);
        friendsListView.setAdapter(usersListAdapter);
        friendsListView.setOnItemClickListener(null);

        startUserListLoader(constraint);
    }

    private void addToFriendList(final Friend friend) {
        baseActivity.showProgress();
        QBAddFriendCommand.start(baseActivity, friend);
    }

    private void showGlobalSearchButton() {
        friendsListView.addFooterView(globalSearchLayout);
    }

    private void saveFriendToCache(Friend friend) {
        DatabaseManager.saveFriend(baseActivity, friend);
    }

    private void importFriendsFinished() {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
        updateFriendsList(FriendsListLoader.ID);
        baseActivity.hideProgress();
    }

    private enum State {FRIENDS_LIST, GLOBAL_LIST}

    private class SearchOnActionExpandListener implements MenuItem.OnActionExpandListener {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            showGlobalSearchButton();
            friendsTitle.setVisibility(View.VISIBLE);
            friendsTitle.setText(R.string.frl_friends);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            hideGlobalSearchButton();
            state = State.FRIENDS_LIST;
            if (friendsTitle != null) {
                friendsTitle.setVisibility(View.GONE);
            }
            initFriendList();
            baseActivity.getActionBar().setDisplayShowHomeEnabled(true);
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