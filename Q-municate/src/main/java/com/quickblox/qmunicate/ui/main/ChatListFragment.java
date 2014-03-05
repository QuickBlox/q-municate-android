package com.quickblox.qmunicate.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.newchat.NewChatActivity;

public class ChatListFragment extends BaseFragment {

    private ListView chatList;

    public static ChatListFragment newInstance() {
        ChatListFragment fragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, App.getInstance().getString(R.string.nvd_title_chats));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        chatList = (ListView) inflater.inflate(R.layout.fragment_chat_list, container, false);

        initListView();
        return chatList;
    }

    private void initListView() {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chat_list_menu, menu);
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
