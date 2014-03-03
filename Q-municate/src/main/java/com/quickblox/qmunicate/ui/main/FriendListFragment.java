package com.quickblox.qmunicate.ui.main;

import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.LoaderFragment;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FriendListFragment extends LoaderFragment<FriendListLoader.Result> {

    private static final int START_DELAY = 0;
    private static final int UPDATE_DATA_PERIOD = 20000; // 300000 5 minutes

    private ListView friendList;
    private List<Friend> friends;
    private FriendListAdapter adapter;

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
        initListView();
        return friendList;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        runLoaderWithTimer();
    }

    private void runLoaderWithTimer() {
        Timer loaderTimer = new Timer();
        loaderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runLoader(FriendListLoader.ID, FriendListLoader.newArguments(1, 50));
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.friend_list_menu, menu);
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
}
