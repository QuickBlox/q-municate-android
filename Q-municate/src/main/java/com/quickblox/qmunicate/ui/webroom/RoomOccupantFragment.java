package com.quickblox.qmunicate.ui.webroom;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBJoinRoomCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;
import com.quickblox.qmunicate.ui.main.AbsFriendsListFragment;
import com.quickblox.qmunicate.ui.main.AbsFriendsListLoader;

import java.util.ArrayList;
import java.util.List;


public class RoomOccupantFragment extends AbsFriendsListFragment {

    private static final String TAG = RoomOccupantFragment.class.getSimpleName();

    private boolean bounded;
    private QBService service;
    private ChetServiceConnection serviceConnection = new ChetServiceConnection();

    public static RoomOccupantFragment newInstance() {
        return new RoomOccupantFragment();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        baseActivity.addAction(QBServiceConsts.JOIN_ROOM_SUCCESS_ACTION, new JoinRoomSuccessAction());
        baseActivity.addAction(QBServiceConsts.JOIN_ROOM_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
//        QBJoinRoomCommand.start(baseActivity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        title = getString(R.string.nvd_title_web_room);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        connectToService();
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService();
    }

    @Override
    protected BaseAdapter getFriendsAdapter() {
        friendsList = new ArrayList<Friend>();
        return new RoomOccupantAdapter(baseActivity, friendsList);
    }

    @Override
    protected AbsFriendsListLoader onFriendsLoaderCreate(Activity activity, Bundle args) {
        return new RoomOccupantsLoader(activity, service.getQbChatHelper());
    }

    @Override
    public void onRefreshStarted(View view) {
        updateFriendsList(RoomOccupantsLoader.ID);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (position == 0) {
            return;
        }
        Friend friend = ((RoomOccupantAdapter) friendsListAdapter).getItem(position - 1);
        FriendDetailsActivity.start(baseActivity, friend);
    }

    @Override
    public void onLoaderResult(int id, List<Friend> data) {
        switch (id) {
            case RoomOccupantsLoader.ID:
                friendsList.clear();
                friendsList.addAll(data);
                ((RoomOccupantAdapter) friendsListAdapter).setNewData(friendsList);
        }
    }

    private void connectToService() {
        Intent intent = new Intent(getActivity(), QBService.class);
        if (isExistActivity()) {
            getBaseActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void unbindService() {
        if (isExistActivity() && bounded) {
            getBaseActivity().unbindService(serviceConnection);
        }
    }

    private class ChetServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.i(TAG, "on service connected");
            bounded = true;
            service = ((QBService.QBServiceBinder) binder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private class JoinRoomSuccessAction implements Command {
        @Override
        public void execute(Bundle bundle) {
            updateFriendsList(RoomOccupantsLoader.ID);
        }
    }
}