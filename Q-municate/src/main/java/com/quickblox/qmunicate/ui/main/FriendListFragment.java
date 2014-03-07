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

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.QBAddFriendTask;
import com.quickblox.qmunicate.ui.base.LoaderFragment;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FriendListFragment extends LoaderFragment<List<Friend>> implements SearchView.OnQueryTextListener {

    public static final int PAGE_NUM = 1;
    public static final int PER_PAGE = 100;

    private static final int START_DELAY = 0;
    private static final int UPDATE_DATA_PERIOD = 300000;

    private ListView listView;
    private List<Friend> friends;
    private List<Friend> users;
    private FriendListAdapter friendListAdapter;
    private UserListAdapter userListAdapter;
    private LinearLayout globalLayout;

    private State state;

    private Timer friendListUpdateTimer;
    private String constraint;

    public static FriendListFragment newInstance() {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, App.getInstance().getString(R.string.nvd_title_friends));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        listView = (ListView) inflater.inflate(R.layout.fragment_friend_list, container, false);

        initFriendList();
        initGlobalSearchButton(inflater);
        return listView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        state = State.FRIEND_LIST;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (state == State.FRIEND_LIST) {
            startFriendListLoaderWithTimer();
        }
    }

    @Override
    public void onStop() {
        stopFriendListLoader();
        super.onStop();
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
    public Loader<LoaderResult<List<Friend>>> onLoaderCreate(int id, Bundle args) {
        switch (id) {
            case FriendListLoader.ID:
                return new FriendListLoader(getActivity());
            case UserListLoader.ID:
                return new UserListLoader(getActivity());
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
        runLoader(UserListLoader.ID, UserListLoader.newArguments(newText, PAGE_NUM, PER_PAGE));
    }

    private void startFriendListLoaderWithTimer() {
        friendListUpdateTimer = new Timer();
        friendListUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runLoader(FriendListLoader.ID, FriendListLoader.newArguments(PAGE_NUM, PER_PAGE));
            }
        }, START_DELAY, UPDATE_DATA_PERIOD);
    }

    private void stopFriendListLoader() {
        friendListUpdateTimer.cancel();
    }

    private void initFriendList() {
        friends = App.getInstance().getFriends();
        friendListAdapter = new FriendListAdapter(getActivity(), friends);
        listView.setAdapter(friendListAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Friend friend = friendListAdapter.getItem(position);
                FriendDetailsActivity.start(getActivity(), friend);
            }
        });
    }

    private void initUserList() {
        users = new ArrayList<Friend>();
        userListAdapter = new UserListAdapter(getActivity(), friends, users, new UserListAdapter.UserListListener() {
            @Override
            public void onUserSelected(int position) {
                addToFriendList(users.get(position));
            }
        });
        listView.setAdapter(userListAdapter);
        listView.setOnItemClickListener(null);
        startUserListLoader(constraint);
    }

    private void addToFriendList(final Friend friend) {
        new QBAddFriendTask(getActivity()).execute(friend, new QBAddFriendTask.Callback() {
            @Override
            public void onSuccess() {
                friends.add(friend);
                userListAdapter.notifyDataSetChanged();
            }
        });
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
        hideGlobalSearchButton();
        initUserList();
    }

    private enum State {FRIEND_LIST, GLOBAL_LIST}

    private class SearchOnActionExpandListener implements MenuItem.OnActionExpandListener {
        @Override
        public boolean onMenuItemActionExpand(MenuItem item) {
            showGlobalSearchButton();
            getActivity().getActionBar().setIcon(android.R.color.transparent);
            return true;
        }

        @Override
        public boolean onMenuItemActionCollapse(MenuItem item) {
            hideGlobalSearchButton();
            if (state == State.GLOBAL_LIST) {
                state = State.FRIEND_LIST;
                initFriendList();
            }
            getActivity().getActionBar().setDisplayShowHomeEnabled(true);
            return true;
        }
    }
}
