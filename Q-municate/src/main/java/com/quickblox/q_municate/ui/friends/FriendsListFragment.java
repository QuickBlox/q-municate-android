package com.quickblox.q_municate.ui.friends;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.caching.DatabaseManager;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate.qb.commands.QBLoadUsersCommand;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DialogUtils;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class FriendsListFragment extends BaseFragment implements AdapterView.OnItemClickListener, SearchView.OnQueryTextListener, FilterQueryProvider {

    private static final String TAG = FriendsListFragment.class.getSimpleName();
    private List<Friend> usersList;
    private UserListAdapter usersListAdapter;
    private LinearLayout globalSearchLayout;
    private State state;
    private String constraint;
    private ListView friendsListView;
    private TextView friendsTitle;
    private TextView emptyListTextView;
    private View friendsListViewTitle;
    private FriendsListCursorAdapter friendsListAdapter;
    private int positionCounter;
    private boolean isNeedToHideSearchView;
    private Cursor friendsCursor;
    private SearchOnActionExpandListener searchListener;
    private MenuItem searchItem;
    private SearchView searchView;
    private static boolean isFriendsListLoaded = false;
    private Toast errorToast;

    public static FriendsListFragment newInstance() {
        return new FriendsListFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friend_list_menu, menu);
        searchListener = new SearchOnActionExpandListener();
        searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(searchListener);
        searchView = (SearchView) searchItem.getActionView();
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

    @Override
    public Cursor runQuery(CharSequence constraint) {
        if (TextUtils.isEmpty(constraint)) {
            return null;
        }
        return DatabaseManager.getFriendsByFullname(baseActivity, constraint.toString());
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

        initUI(rootView, layoutInflater);
        initListeners();
        initGlobalSearchButton(layoutInflater);
        initFriendsList();

        //        TipsManager.showTipWithButtonsIfNotShownYet(this, getActivity().getString(R.string.tip_friend_list),
        //                new FriendsListTipButtonClicker(this));

        return rootView;
    }

    private void initListeners() {
        friendsListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                KeyboardUtils.hideKeyboard(baseActivity);
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        addActions();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        KeyboardUtils.hideKeyboard(baseActivity);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        constraint = newText;
        friendsListAdapter.setFilterQueryProvider(this);
        if (state == State.FRIENDS_LIST && isNeedToHideSearchView) {
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
        if (selectedItem.getCount() != Consts.ZERO_INT_VALUE && !selectedItem.isBeforeFirst()) {
            FriendDetailsActivity.start(baseActivity, DatabaseManager.getFriendFromCursor(selectedItem).getId());
        }
    }

    public MenuItem getSearchItem() {
        return searchItem;
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, failAction);
        baseActivity.addAction(QBServiceConsts.LOAD_USERS_SUCCESS_ACTION, new UserSearchSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_USERS_FAIL_ACTION, new UserSearchFailAction());
        baseActivity.addAction(QBServiceConsts.LOAD_FRIENDS_SUCCESS_ACTION, new LoadFriendsSuccessAction());
        baseActivity.addAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION, new ImportFriendsSuccessAction());
        baseActivity.addAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION, new ImportFriendsFailAction());
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
        friendsListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if(!isNeedToHideSearchView) {
                    checkVisibilityEmptyLabel();
                }
            }
        });
        friendsListView.setAdapter(friendsListAdapter);
        friendsListView.setSelector(R.drawable.list_item_background_selector);
        friendsListView.setOnItemClickListener(this);
        if(isFriendsListLoaded) {
            checkVisibilityEmptyLabel();
        }
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
                });

        friendsTitle.setText(R.string.frl_all_users);

        friendsListView.setSelector(android.R.color.transparent);
        friendsListView.setAdapter(usersListAdapter);
        friendsListView.setOnItemClickListener(null);

        startUsersListLoader(constraint);
    }

    private void addToFriendList(final Friend friend) {
        baseActivity.showProgress();
        QBAddFriendCommand.start(baseActivity, friend);
        KeyboardUtils.hideKeyboard(baseActivity);
        searchView.clearFocus();
    }

    private void startUsersListLoader(String newText) {
        QBLoadUsersCommand.start(baseActivity, newText);
        usersListAdapter.setSearchCharacters(newText);
    }

    private void updateUsersList(List<Friend> friendsList) {
        usersList.clear();
        usersList.addAll(friendsList);
        usersListAdapter.notifyDataSetChanged();
    }

    private void importFriendsFinished() {
        isFriendsListLoaded = true;
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
        baseActivity.hideProgress();
    }

    private void showErrorToast(String error) {
        if (errorToast != null) {
            errorToast.cancel();
        }
        errorToast = ErrorUtils.getErrorToast(baseActivity, error);
        errorToast.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!isNeedToHideSearchView) {
            checkVisibilityEmptyLabel();
        }
    }

    private void checkVisibilityEmptyLabel() {
        emptyListTextView.setVisibility(friendsListAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private enum State {FRIENDS_LIST, GLOBAL_LIST}

    private class SearchOnActionExpandListener implements MenuItem.OnActionExpandListener {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            isNeedToHideSearchView = true;
            emptyListTextView.setVisibility(View.GONE);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            state = State.FRIENDS_LIST;
            baseActivity.getActionBar().setDisplayShowHomeEnabled(true);
            if (isNeedToHideSearchView) {
                isNeedToHideSearchView = false;
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
            String notFoundError = getResources().getString(R.string.frl_not_found_users);
            showErrorToast(notFoundError);
        }
    }

    private class ImportFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            importFriendsFinished();
        }
    }

    private class ImportFriendsFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            importFriendsFinished();
            DialogUtils.showLong(baseActivity, getResources().getString(R.string.dlg_no_friends_for_import));
        }
    }

    private class LoadFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle){
            List<Friend> friendsList = (List<Friend>) bundle.getSerializable(QBServiceConsts.EXTRA_FRIENDS);
            isFriendsListLoaded = true;
            if (friendsList.isEmpty()) {
                emptyListTextView.setVisibility(View.VISIBLE);
            }
        }
    }
}