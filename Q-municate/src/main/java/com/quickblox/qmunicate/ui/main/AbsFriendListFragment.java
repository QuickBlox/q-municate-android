package com.quickblox.qmunicate.ui.main;


import android.app.Activity;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.LoaderFragment;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AbsFriendListFragment extends LoaderFragment<List<Friend>> {

    private static final String TAG = AbsFriendListFragment.class.getSimpleName();

    protected ListView listView;
    protected TextView listTitle;
    protected View listTitleView;
    protected List<Friend> friends;
    protected FriendListAdapter friendListAdapter;
    protected boolean isStopFriendListLoader;
    protected Timer friendListUpdateTimer;

    protected abstract FriendListAdapter getFriendsAdapter();

    protected abstract AbsFriendListLoader onFriendsLoaderCreate(Activity activity, Bundle args);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        listView = (ListView) inflater.inflate(R.layout.fragment_friend_list, container, false);

        listTitleView = inflater.inflate(R.layout.view_section_title, null);
        listTitle = (TextView) listTitleView.findViewById(R.id.listTitle);
        listTitle.setVisibility(View.GONE);
        listView.addHeaderView(listTitleView);

        initFriendList();

        return listView;
    }

    @Override
    public Loader<LoaderResult<List<Friend>>> onLoaderCreate(int id, Bundle args) {
        switch (id) {
            case FriendListLoader.ID:
            case RoomOccupantsLoader.ID:
                return onFriendsLoaderCreate(baseActivity, args);
            default:
                return null;
        }
    }

    @Override
    public void onLoaderResult(int id, List<Friend> data) {
        switch (id) {
            case FriendListLoader.ID:
            case RoomOccupantsLoader.ID:
                Log.i(TAG, "onLoaderResult " + "update list");
                friends.clear();
                friends.addAll(data);
                friendListAdapter.setNewData(friends);
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isStopFriendListLoader) {
            stopFriendListLoader();
        }
    }

    protected void startFriendListLoaderWithTimer(final int idLoader) {
        isStopFriendListLoader = true;
        friendListUpdateTimer = new Timer();
        friendListUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runLoader(idLoader, AbsFriendListLoader.newArguments(Consts.FL_FRIENDS_PAGE_NUM,
                        Consts.FL_FRIENDS_PER_PAGE));
            }
        }, Consts.FL_START_LOAD_DELAY, Consts.FL_UPDATE_DATA_PERIOD);
    }


    protected void stopFriendListLoader() {
        isStopFriendListLoader = false;
        friendListUpdateTimer.cancel();
    }

    protected void initFriendList() {
        friendListAdapter = getFriendsAdapter();
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


}
