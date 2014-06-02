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
import com.quickblox.qmunicate.qb.commands.QBLoadFriendListCommand;
import com.quickblox.qmunicate.qb.commands.QBLoadUsersCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.FriendsListTipButtonClicker;
import com.quickblox.qmunicate.utils.PrefsHelper;
import com.quickblox.qmunicate.utils.TipsManager;

import java.util.ArrayList;
import java.util.List;

public class FriendsListFragment extends BaseFragment implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener, FilterQueryProvider {

    private List<Friend> usersList;
    private UserListAdapter usersListAdapter;
    private LinearLayout globalSearchLayout;
    private State state;
    private String constraint;
    private boolean isImportInitialized;
    private ListView friendsListView;
    private TextView friendsTitle;
    private TextView emptyListTextView;
    private View friendsListViewTitle;
    private FriendsListCursorAdapter friendsListAdapter;
    private int positionCounter;
    private boolean isHideSearchView;
    private Cursor friendsCursor;
    private SearchOnActionExpandListener searchListener;
    private MenuItem searchItem;

    public static FriendsListFragment newInstance() {
        return new FriendsListFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addActionsAddFriend();
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO SF temp
        // emptyListTextView.setVisibility(friendsListAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friend_list_menu, menu);
        searchListener = new SearchOnActionExpandListener();
        searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(searchListener);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                friendsTitle.setText(R.string.frl_friends);
                positionCounter++;
                friendsListView.setAdapter(null);
                friendsListView.addHeaderView(friendsListViewTitle);
                friendsListView.addFooterView(globalSearchLayout);
                friendsListView.setAdapter(friendsListAdapter);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addActionsAddFriend() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, failAction);
        baseActivity.addAction(QBServiceConsts.LOAD_USERS_SUCCESS_ACTION, new UserSearchSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_USERS_FAIL_ACTION, new UserSearchFailAction());
        baseActivity.updateBroadcastActionList();
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        if (TextUtils.isEmpty(constraint)) {
            return null;
        }
        return DatabaseManager.getFriends(baseActivity, constraint.toString());
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
        initFriendsList();
        TipsManager.showTipWithButtonsIfNotShownYet(this, getActivity().getString(R.string.tip_friend_list),
                new FriendsListTipButtonClicker(this));
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isImportInitialized) {
            addActionsAddFriends();
        }
        QBLoadFriendListCommand.start(baseActivity);
    }

    private void addActionsAddFriends() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIENDS_SUCCESS_ACTION, new AddFriendsSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIENDS_FAIL_ACTION, new AddFriendsFailAction());
        baseActivity.updateBroadcastActionList();
    }

    private void initUI(View view, LayoutInflater layoutInflater) {
        friendsListView = (ListView) view.findViewById(R.id.friends_listview);
        friendsListViewTitle = layoutInflater.inflate(R.layout.view_section_title_friends_list, null);
        friendsTitle = (TextView) friendsListViewTitle.findViewById(R.id.listTitle);
        emptyListTextView = (TextView) view.findViewById(R.id.empty_list_textview);
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
        QBLoadUsersCommand.start(baseActivity, newText);
        usersListAdapter.setSearchCharacters(newText);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        constraint = newText;
        friendsListAdapter.setFilterQueryProvider(this);
        if (state == State.FRIENDS_LIST && isHideSearchView) {
            friendsListAdapter.getFilter().filter(newText);
            friendsListAdapter.setSearchCharacters(newText);
        } else if (state == State.GLOBAL_LIST) {
            startUsersListLoader(newText);
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
        Cursor selectedItem = (Cursor) friendsListAdapter.getItem(position - positionCounter);
        if(selectedItem.getCount() != 0){
            FriendDetailsActivity.start(baseActivity, DatabaseManager.getFriendFromCursor(selectedItem));
        }
    }

    public MenuItem getSearchItem() {
        return searchItem;
    }

    private void updateUsersList(List<Friend> friendsList) {
        usersList.clear();
        usersList.addAll(friendsList);
        usersListAdapter.notifyDataSetChanged();
    }

    private void importFriendsFinished() {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
        baseActivity.hideProgress();
    }

    private enum State {FRIENDS_LIST, GLOBAL_LIST}

    private class SearchOnActionExpandListener implements MenuItem.OnActionExpandListener {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            isHideSearchView = true;
            // TODO SF temp
            //            emptyListTextView.setVisibility(View.GONE);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            state = State.FRIENDS_LIST;

            baseActivity.getActionBar().setDisplayShowHomeEnabled(true);

            if (isHideSearchView) {
                isHideSearchView = false;
                // TODO SF temp
                //                emptyListTextView.setVisibility(friendsListAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                friendsListAdapter.setSearchCharacters(null);
                friendsListAdapter.setFilterQueryProvider(null);
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
            usersListAdapter.notifyDataSetChanged();
            baseActivity.hideProgress();
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