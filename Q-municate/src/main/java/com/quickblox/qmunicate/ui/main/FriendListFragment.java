package com.quickblox.qmunicate.ui.main;

import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.QBAddFriendCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.LoaderFragment;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;
import com.quickblox.qmunicate.ui.utils.Consts;
import com.quickblox.qmunicate.ui.utils.DialogUtils;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FriendListFragment extends LoaderFragment<List<Friend>> implements SearchView.OnQueryTextListener {

    private static final String TAG = FriendListFragment.class.getSimpleName();
    private static final int START_DELAY = 0;
    private static final int UPDATE_DATA_PERIOD = 300000;

    private ListView listView;
    private TextView listTitle;
    private View listTitleView;
    private List<Friend> friends;
    private List<Friend> users;

    private FriendListAdapter friendListAdapter;
    private UserListAdapter userListAdapter;
    private LinearLayout globalLayout;

    private State state;

    private Timer friendListUpdateTimer;
    private String constraint;
    private boolean isImportInitialized;
    private boolean isStopFriendListLoader;

    public static FriendListFragment newInstance() {
        return new FriendListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        listView = (ListView) inflater.inflate(R.layout.fragment_friend_list, container, false);

        listTitleView = inflater.inflate(R.layout.view_section_title, null);
        listTitle = (TextView) listTitleView.findViewById(R.id.listTitle);
        listTitle.setVisibility(View.GONE);
        listView.addHeaderView(listTitleView);

        isImportInitialized = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);

        initGlobalSearchButton(inflater);
        initFriendList();

        return listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, new BaseActivity.FailAction(
                baseActivity));
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isStopFriendListLoader) {
            stopFriendListLoader();
        }
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        title = getString(R.string.nvd_title_friends);
        state = State.FRIEND_LIST;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!isImportInitialized) {
            baseActivity.addAction(QBServiceConsts.ADD_FRIENDS_SUCCESS_ACTION, new AddFriendsSuccessAction());
            baseActivity.addAction(QBServiceConsts.ADD_FRIENDS_FAIL_ACTION, new AddFriendsFailAction());
        } else {
            if (state == State.FRIEND_LIST) {
                startFriendListLoaderWithTimer();
            }
        }
    }

    @Override
    public Loader<LoaderResult<List<Friend>>> onLoaderCreate(int id, Bundle args) {
        switch (id) {
            case FriendListLoader.ID:
                return new FriendListLoader(baseActivity);
            case UserListLoader.ID:
                return new UserListLoader(baseActivity);
            default:
                return null;
        }
    }

    @Override
    public void onLoaderResult(int id, List<Friend> data) {
        switch (id) {
            case FriendListLoader.ID:
                friends.clear();
                friends.addAll(data);
                friendListAdapter.notifyDataSetChanged();
                break;
            case UserListLoader.ID:
                users.clear();
                users.addAll(data);
                userListAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        constraint = newText;
        if (state == State.FRIEND_LIST) {
            friendListAdapter.getFilter().filter(newText);
        } else {
            startUserListLoader(newText);
        }
        return true;
    }

    private void startUserListLoader(String newText) {
        runLoader(UserListLoader.ID, UserListLoader.newArguments(newText, Consts.LOAD_PAGE_NUM,
                Consts.LOAD_PER_PAGE));
    }

    private void startFriendListLoaderWithTimer() {
        isStopFriendListLoader = true;
        friendListUpdateTimer = new Timer();
        friendListUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runLoader(FriendListLoader.ID, FriendListLoader.newArguments(Consts.LOAD_PAGE_NUM,
                        Consts.LOAD_PER_PAGE));
            }
        }, START_DELAY, UPDATE_DATA_PERIOD);
    }

    private void stopFriendListLoader() {
        isStopFriendListLoader = false;
        friendListUpdateTimer.cancel();
    }

    private void initFriendList() {
        friends = App.getInstance().getFriends();
        friendListAdapter = new FriendListAdapter(baseActivity, friends);
        listView.setAdapter(friendListAdapter);
        listView.setSelector(R.drawable.list_item_background_selector);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    return;
                }
                Friend friend = friendListAdapter.getItem(position - 1);
                FriendDetailsActivity.start(baseActivity, friend);
            }
        });
    }

    private void initUserList() {
        users = new ArrayList<Friend>();
        userListAdapter = new UserListAdapter(baseActivity, friends, users,
                new UserListAdapter.UserListListener() {
                    @Override
                    public void onUserSelected(int position) {
                        addToFriendList(users.get(position));
                    }
                }
        );
        listView.setSelector(android.R.color.transparent);
        listView.setAdapter(userListAdapter);
        listView.setOnItemClickListener(null);

        startUserListLoader(constraint);
    }

    private void addToFriendList(final Friend friend) {
        baseActivity.showProgress();
        QBAddFriendCommand.start(baseActivity, friend);
    }

    private void initGlobalSearchButton(LayoutInflater inflater) {
        globalLayout = (LinearLayout) inflater.inflate(R.layout.view_global_search_button, null);
        globalLayout.findViewById(R.id.globalSearchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGlobalSearch();
            }
        });
    }

    private void showGlobalSearchButton() {
        listView.addFooterView(globalLayout);
    }

    private void hideGlobalSearchButton() {
        listView.removeFooterView(globalLayout);
    }

    private void startGlobalSearch() {
        state = State.GLOBAL_LIST;
        listTitle.setText(R.string.frl_all_users);
        hideGlobalSearchButton();
        initUserList();
    }

    private void importFriendsFinished() {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
        startFriendListLoaderWithTimer();
        baseActivity.hideProgress();
    }

    private enum State {FRIEND_LIST, GLOBAL_LIST}

    private class SearchOnActionExpandListener implements MenuItem.OnActionExpandListener {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            showGlobalSearchButton();
            baseActivity.getActionBar().setIcon(android.R.color.transparent);
            listTitle.setVisibility(View.VISIBLE);
            listTitle.setText(R.string.frl_friends);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            hideGlobalSearchButton();
            state = State.FRIEND_LIST;
            listTitle.setVisibility(View.GONE);
            initFriendList();
            baseActivity.getActionBar().setDisplayShowHomeEnabled(true);
            return true;
        }
    }

    private class AddFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Friend friend = (Friend) bundle.getSerializable(QBServiceConsts.EXTRA_FRIEND);
            friends.add(friend);
            userListAdapter.notifyDataSetChanged();
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