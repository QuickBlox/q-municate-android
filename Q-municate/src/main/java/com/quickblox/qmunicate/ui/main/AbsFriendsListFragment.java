package com.quickblox.qmunicate.ui.main;


import android.app.Activity;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.LoaderFragment;
import com.quickblox.qmunicate.ui.webroom.RoomOccupantsLoader;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class AbsFriendsListFragment extends LoaderFragment<List<Friend>> implements AdapterView.OnItemClickListener, OnRefreshListener {

    protected ListView friendsListView;
    protected TextView friendsTitle;
    protected View friendsListViewTitle;
    protected List<Friend> friendsList;
    protected BaseAdapter friendsListAdapter;
    protected PullToRefreshLayout pullToRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_friend_list, container, false);

        friendsListView = (ListView) rootView.findViewById(R.id.friendList);

        friendsListViewTitle = inflater.inflate(R.layout.view_section_title_friends_list, null);
        friendsTitle = (TextView) friendsListViewTitle.findViewById(R.id.listTitle);
        friendsTitle.setVisibility(View.GONE);
        friendsListView.addHeaderView(friendsListViewTitle);

        pullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.pullToRefreshLayout);

        ActionBarPullToRefresh.from(baseActivity)
                .allChildrenArePullable()
                .listener(this)
                .setup(pullToRefreshLayout);

        initFriendList();

        return rootView;
    }

    protected void initFriendList() {
        friendsListAdapter = getFriendsAdapter();
        friendsListView.setAdapter(friendsListAdapter);
        friendsListView.setSelector(R.drawable.list_item_background_selector);
        friendsListView.setOnItemClickListener(this);
    }

    protected abstract BaseAdapter getFriendsAdapter();

    @Override
    public Loader<LoaderResult<List<Friend>>> onLoaderCreate(int id, Bundle args) {
        switch (id) {
            case FriendsListLoader.ID:
            case RoomOccupantsLoader.ID:
                return onFriendsLoaderCreate(baseActivity, args);
            default:
                return null;
        }
    }

    protected abstract AbsFriendsListLoader onFriendsLoaderCreate(Activity activity, Bundle args);

    protected void updateFriendsList(final int idLoader) {
        runLoader(idLoader, AbsFriendsListLoader.newArguments(Consts.FL_FRIENDS_PAGE_NUM,
                                        Consts.FL_FRIENDS_PER_PAGE));
    }
}