package com.quickblox.qmunicate.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.friend.FriendDetailsActivity;

import java.util.ArrayList;
import java.util.List;

public class FriendListFragment extends BaseFragment {

    private ListView friendList;

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

    private void initListView() {
        List<Friend> friends = new ArrayList<Friend>();

        QBUser user = new QBUser();
        user.setId(1);
        user.setEmail("dsads@@dsad.com");
        user.setFullName("Leonor Cantley");
        user.setFileId(1);
        Friend friend = new Friend(user);
        friend.setOnline(true);
        friend.setStatus("Online");
        friends.add(friend);

        user = new QBUser();
        user.setId(2);
        user.setEmail("asdasd@@dsasdasad.com");
        user.setFullName("James Cobs");
        user.setFileId(2);
        friend = new Friend(user);
        friend.setOnline(false);
        friend.setStatus("Last seen yesterday at 21:20");
        friends.add(friend);

        FriendListAdapter adapter = new FriendListAdapter(getActivity(), 0, 0, friends);

        friendList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FriendDetailsActivity.startActivity(getActivity());
            }
        });

        friendList.setAdapter(adapter);
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
}
