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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.LoaderFragment;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FriendListFragment extends LoaderFragment<FriendListLoader.Result> implements SearchView.OnQueryTextListener {

    public static final int PAGE_NUM = 1;
    public static final int PER_PAGE = 100;
    private static final int START_DELAY = 0;
    private static final int UPDATE_DATA_PERIOD = 300000; // 20000 - 20 sec.;  300000 - 5 minutes
    private static final int GLOBAL_SEARCH_MARGIN = 15;

    private ListView friendList;
    private List<Friend> friends;
    private FriendListAdapter adapter;
    private SearchView searchView;
    private LinearLayout globalLayout;

    public static FriendListFragment newInstance() {
        FriendListFragment fragment = new FriendListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, App.getInstance().getString(R.string.nvd_title_friends));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        friendList = (ListView) inflater.inflate(R.layout.fragment_friend_list, container, false);

        initGlobalSearchButton();
        initListView();
        return friendList;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        runLoaderWithTimer();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friend_list_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                showGlobalSearchButton();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideGlobalSearchButton();
                return true;
            }
        });
        searchView = (SearchView) searchItem.getActionView();
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
    public Loader<LoaderResult<FriendListLoader.Result>> onLoaderCreate(int id, Bundle args) {
        return new FriendListLoader(getActivity());
    }

    @Override
    public void onLoaderResult(int id, com.quickblox.qmunicate.ui.main.FriendListLoader.Result data) {
        friends.clear();
        friends.addAll(data.friends);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return true;
    }

    private void runLoaderWithTimer() {
        Timer loaderTimer = new Timer();
        loaderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runLoader(FriendListLoader.ID, FriendListLoader.newArguments(PAGE_NUM, PER_PAGE));
            }
        }, START_DELAY, UPDATE_DATA_PERIOD);
    }

    private void initListView() {
        friends = App.getInstance().getFriends();
        adapter = new FriendListAdapter(getActivity(), friends);
        friendList.setAdapter(adapter);


        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int friendId = adapter.getItem(position).getId();
                FriendDetailsActivity.startActivity(getActivity(), friendId);
            }
        });
    }

    private void initGlobalSearchButton() {
        globalLayout = new LinearLayout(getActivity());
        globalLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(GLOBAL_SEARCH_MARGIN, GLOBAL_SEARCH_MARGIN, GLOBAL_SEARCH_MARGIN, GLOBAL_SEARCH_MARGIN);

        Button globalSearch = new Button(getActivity());
        globalSearch.setText(getString(R.string.frl_global_search));
        globalSearch.setTextColor(getResources().getColor(R.color.white));
        globalSearch.setBackgroundResource(R.drawable.global_search_button);

        globalSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGlobalSearch();
            }
        });
        globalLayout.addView(globalSearch, layoutParams);
    }

    private void showGlobalSearchButton() {
        friendList.addFooterView(globalLayout);
    }

    private void hideGlobalSearchButton() {
        friendList.removeFooterView(globalLayout);
    }

    private void startGlobalSearch() {

    }
}
