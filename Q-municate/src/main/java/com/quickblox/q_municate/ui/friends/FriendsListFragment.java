package com.quickblox.q_municate.ui.friends;

import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
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
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.command.Command;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.db.tables.UserTable;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.qb.commands.QBAcceptFriendCommand;
import com.quickblox.q_municate.qb.commands.QBAddFriendCommand;
import com.quickblox.q_municate.qb.commands.QBLoadUsersCommand;
import com.quickblox.q_municate.qb.commands.QBRejectFriendCommand;
import com.quickblox.q_municate.qb.helpers.QBFriendListHelper;
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
    private List<User> usersList;
    private UserListAdapter usersListAdapter;
    private LinearLayout globalSearchLayout;
    private State state;
    private String constraint;
    private ExpandableListView friendsListView;
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
    private Toast errorToast;
    private boolean isFriendsListLoaded = false;
    private ContentObserver statusContentObserver;

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
        if (!isNeedToHideSearchView) {
            checkVisibilityEmptyLabel();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterStatusChangingObserver();
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
                friendsListView.setAdapter((ExpandableListAdapter) null);
                friendsListView.addHeaderView(friendsListViewTitle);
                friendsListView.addFooterView(globalSearchLayout);
                friendsListView.setAdapter(friendsListAdapter);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void registerStatusChangingObserver() {
        statusContentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                initFriendsList();
            }
        };
        baseActivity.getContentResolver().registerContentObserver(UserTable.CONTENT_URI, true, statusContentObserver);
    }

    private void unregisterStatusChangingObserver() {
        baseActivity.getContentResolver().unregisterContentObserver(statusContentObserver);
    }

    @Override
    public Cursor runQuery(CharSequence constraint) {
        if (TextUtils.isEmpty(constraint)) {
            return null;
        }
        return DatabaseManager.getFriendsByFullName(baseActivity, constraint.toString());
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
        registerStatusChangingObserver();

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
//        Cursor selectedItem = (Cursor) friendsListAdapter.getItem(position - positionCounter);
//        if (selectedItem.getCount() != Consts.ZERO_INT_VALUE && !selectedItem.isBeforeFirst()) {
//            FriendDetailsActivity.start(baseActivity, DatabaseManager.getFriendFromCursor(selectedItem)
//                    .getUserId());
//        }
    }

    public MenuItem getSearchItem() {
        return searchItem;
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, failAction);
        baseActivity.addAction(QBServiceConsts.ACCEPT_FRIEND_FAIL_ACTION, failAction);
        baseActivity.addAction(QBServiceConsts.LOAD_USERS_SUCCESS_ACTION, new UserSearchSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_USERS_FAIL_ACTION, new UserSearchFailAction());
        baseActivity.addAction(QBServiceConsts.LOAD_FRIENDS_SUCCESS_ACTION, new LoadFriendsSuccessAction());
        baseActivity.addAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION,
                new ImportFriendsSuccessAction());
        baseActivity.addAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION, new ImportFriendsFailAction());
        baseActivity.updateBroadcastActionList();
    }

    private void initUI(View view, LayoutInflater layoutInflater) {
        friendsListView = (ExpandableListView) view.findViewById(R.id.friends_listview);
        friendsListViewTitle = layoutInflater.inflate(R.layout.view_section_title_friends_list, null);
        friendsTitle = (TextView) friendsListViewTitle.findViewById(R.id.list_title_textview);
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
                });
    }

    private void initFriendsList() {
        if (friendsCursor != null && !friendsCursor.isClosed()) {
            friendsCursor.close();
        }
        friendsCursor = getAllFriends();

        Cursor headersCursor = createHeadersCursor();

        friendsListAdapter = new FriendsListCursorAdapter(baseActivity, createHeadersCursor(),new  FriendSelectAction());
        friendsListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (!isNeedToHideSearchView) {
                    checkVisibilityEmptyLabel();
                }
            }
        });
        friendsListView.setAdapter(friendsListAdapter);
        friendsListView.setGroupIndicator(null);
        friendsListView.setOnItemClickListener(this);
        friendsListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return true;
            }
        });

        if (isFriendsListLoaded) {
            checkVisibilityEmptyLabel();
        }

        expandAllGroups(headersCursor);
    }

    private void expandAllGroups(Cursor headersCursor) {
        for (int i = 0; i < headersCursor.getCount(); i++) {
            friendsListView.expandGroup(i);
        }
    }

    private Cursor createHeadersCursor() {
        MatrixCursor headersCursor = new MatrixCursor(new String[] {
                FriendsListCursorAdapter.HEADER_COLUMN_ID,
                FriendsListCursorAdapter.HEADER_COLUMN_STATUS_NAME,
                FriendsListCursorAdapter.HEADER_COLUMN_HEADER_NAME});

        headersCursor.addRow(new String[] {DatabaseManager.getRelationStatusIdByName(baseActivity, QBFriendListHelper.RELATION_STATUS_NONE) + Consts.EMPTY_STRING, QBFriendListHelper.RELATION_STATUS_NONE, "none-none-none"});
//        headersCursor.addRow(new String[] {DatabaseManager.getRelationStatusIdByName(baseActivity, QBFriendListHelper.RELATION_STATUS_TO) + Consts.EMPTY_STRING, QBFriendListHelper.RELATION_STATUS_TO, "to-to-to"});
//        headersCursor.addRow(new String[] {DatabaseManager.getRelationStatusIdByName(baseActivity, QBFriendListHelper.RELATION_STATUS_FROM) + Consts.EMPTY_STRING, QBFriendListHelper.RELATION_STATUS_FROM, "from-from-from"});
        headersCursor.addRow(new String[] {DatabaseManager.getRelationStatusIdByName(baseActivity, QBFriendListHelper.RELATION_STATUS_BOTH) + Consts.EMPTY_STRING, QBFriendListHelper.RELATION_STATUS_BOTH, "both + from"});
//        headersCursor.addRow(new String[] {DatabaseManager.getRelationStatusIdByName(baseActivity, QBFriendListHelper.RELATION_STATUS_REMOVE) + Consts.EMPTY_STRING, QBFriendListHelper.RELATION_STATUS_REMOVE, "remove-remove-remove"});

        return headersCursor;
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
        usersList = new ArrayList<User>();
        usersListAdapter = new UserListAdapter(baseActivity, usersList,
                new FriendSelectListener() {
                    @Override
                    public void onAddUserClicked(int position) {
//                        addToFriendList(usersList.get(position));
                    }

                    @Override
                    public void onAcceptUserClicked(int position) {

                    }

                    @Override
                    public void onRejectUserClicked(int position) {

                    }
                });

        friendsTitle.setText(R.string.frl_all_users);

        friendsListView.setSelector(android.R.color.transparent);
        friendsListView.setAdapter(usersListAdapter);
        friendsListView.setOnItemClickListener(null);

        startUsersListLoader(constraint);
    }

    private void addToFriendList(final int userId) {
        baseActivity.showProgress();
        QBAddFriendCommand.start(baseActivity, userId);
        KeyboardUtils.hideKeyboard(baseActivity);
        searchView.clearFocus();
    }

    private void acceptUser(final int userId) {
        QBAcceptFriendCommand.start(baseActivity, userId);
    }

    private void rejectUser(final int userId) {
        QBRejectFriendCommand.start(baseActivity, userId);
    }

    private void startUsersListLoader(String newText) {
        QBLoadUsersCommand.start(baseActivity, newText);
        usersListAdapter.setSearchCharacters(newText);
    }

    private void updateUsersList(List<User> friendsList) {
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

    public interface FriendSelectListener {

        void onAddUserClicked(int userId);
        void onAcceptUserClicked(int userId);
        void onRejectUserClicked(int userId);
    }

    private class FriendSelectAction implements FriendSelectListener {

        @Override
        public void onAddUserClicked(int userId) {
            addToFriendList(userId);
        }

        @Override
        public void onAcceptUserClicked(int userId) {
            acceptUser(userId);
        }

        @Override
        public void onRejectUserClicked(int userId) {
            rejectUser(userId);
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
            List<User> friendsList = (List<User>) bundle.getSerializable(QBServiceConsts.EXTRA_FRIENDS);
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
        public void execute(Bundle bundle) {
            List<User> friendsList = (List<User>) bundle.getSerializable(QBServiceConsts.EXTRA_FRIENDS);
            isFriendsListLoaded = true;
            if (friendsList.isEmpty()) {
                emptyListTextView.setVisibility(View.VISIBLE);
            }
        }
    }
}