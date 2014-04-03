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
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FriendListFragment extends AbsFriendListFragment implements SearchView.OnQueryTextListener {

    private List<Friend> users;

    private UserListAdapter userListAdapter;
    private LinearLayout globalLayout;

    private State state;

    private String constraint;
    private boolean isImportInitialized;

    public static FriendListFragment newInstance() {
        return new FriendListFragment();
    }

    @Override
    protected FriendListAdapter getFriendsAdapter() {
        friends = App.getInstance().getFriends();
        return new FriendListAdapter(baseActivity, friends);
    }

    @Override
    protected AbsFriendListLoader onFriendsLoaderCreate(Activity activity, Bundle args) {
        return new FriendListLoader(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        isImportInitialized = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);
        initGlobalSearchButton(inflater);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_SUCCESS_ACTION, new AddFriendSuccessAction());
        baseActivity.addAction(QBServiceConsts.ADD_FRIEND_FAIL_ACTION, new BaseActivity.FailAction(
                baseActivity));
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
                startFriendListLoaderWithTimer(FriendListLoader.ID);
            }
        }
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
    public void onLoaderResult(int id, List<Friend> data) {
        super.onLoaderResult(id, data);
        switch (id) {
            case UserListLoader.ID:
                users.clear();
                users.addAll(data);
                userListAdapter.setNewData(users);
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
        runLoader(UserListLoader.ID, UserListLoader.newArguments(newText, Consts.FL_FRIENDS_PAGE_NUM,
                Consts.FL_FRIENDS_PER_PAGE));
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
        startFriendListLoaderWithTimer(FriendListLoader.ID);
        baseActivity.hideProgress();
    }

    private enum State {FRIEND_LIST, GLOBAL_LIST}

    private class SearchOnActionExpandListener implements MenuItem.OnActionExpandListener {

        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            showGlobalSearchButton();
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