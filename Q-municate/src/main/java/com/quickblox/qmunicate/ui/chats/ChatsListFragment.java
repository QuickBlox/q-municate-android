package com.quickblox.qmunicate.ui.chats;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Chat;
import com.quickblox.qmunicate.model.GroupChat;
import com.quickblox.qmunicate.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.List;

public class ChatsListFragment extends BaseFragment {
    private ListView chatsListView;

    private List<Chat> chatsArrayList;
    private ChatsListAdapter chatsListAdapter;

    public static ChatsListFragment newInstance() {
        ChatsListFragment fragment = new ChatsListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, App.getInstance().getString(R.string.nvd_title_chats));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatsListView = (ListView) inflater.inflate(R.layout.fragment_chats_list, container, false);
        chatsArrayList = new ArrayList<Chat>();
        chatsListAdapter = new ChatsListAdapter(getActivity(), R.layout.list_item_chat, chatsArrayList);
        chatsListView.setAdapter(chatsListAdapter);

        initUI();
        initListeners();
        initListView();

        return chatsListView;
    }

    private void initListView() {
        chatsArrayList.add(new GroupChat("Aaa", 1));
        chatsArrayList.add(new GroupChat("Bbb", 2));
        chatsArrayList.add(new GroupChat("Ccc", 3));
        chatsListAdapter.notifyDataSetChanged();
    }

    private void initListeners() {
        chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
            }
        });
    }

    private void initUI() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chats_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                NewChatActivity.start(getActivity());
                break;
        }
        return true;
    }
}