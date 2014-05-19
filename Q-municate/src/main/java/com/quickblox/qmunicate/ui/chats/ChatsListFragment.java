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

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
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
        return new ChatsListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getString(R.string.nvd_title_chats);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatsListView = (ListView) inflater.inflate(R.layout.fragment_chats_list, container, false);
        chatsArrayList = new ArrayList<Chat>();
        chatsListAdapter = new ChatsListAdapter(getActivity(), DatabaseManager.getAllChatConversations(getActivity()));
        chatsListView.setAdapter(chatsListAdapter);

        initUI();
        initListeners();
        initListView();

        return chatsListView;
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

    private void initUI() {
        setHasOptionsMenu(true);
    }

    private void initListeners() {
        chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                GroupChatDetailsActivity.start(getActivity(), (GroupChat)chatsArrayList.get(position));
            }
        });
    }

    private void initListView() {
        chatsArrayList.add(new GroupChat("Aaa", 1));
        chatsArrayList.add(new GroupChat("Bbb", 2));
        chatsArrayList.add(new GroupChat("Ccc", 3));
        chatsListAdapter.notifyDataSetChanged();
    }
}