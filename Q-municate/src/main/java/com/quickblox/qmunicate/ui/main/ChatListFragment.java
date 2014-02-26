package com.quickblox.qmunicate.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Chat;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.GroupChat;
import com.quickblox.qmunicate.model.PrivateChat;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.groupchat.GroupChatActivity;
import com.quickblox.qmunicate.ui.newchat.NewChatActivity;
import com.quickblox.qmunicate.ui.privatechat.PrivateChatActivity;

import java.util.ArrayList;
import java.util.List;

public class ChatListFragment extends BaseFragment {

    private ListView chatList;
    private List<Chat> chats;

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
        chats = new ArrayList<Chat>();

        final QBUser user = new QBUser();
        user.setId(1);
        user.setEmail("dsads@@dsad.com");
        user.setFullName("Leonor Cantley");
        user.setFileId(1);

        final Friend friend = new Friend(user);
        final Chat chat = new PrivateChat(friend);
        chats.add(chat);

        GroupChat groupChat = new GroupChat();
        groupChat.setName("My group");
        chats.add(groupChat);

        ChatListAdapter adapter = new ChatListAdapter(getActivity(), 0, 0, chats);
        chatList.setAdapter(adapter);

        chatList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        PrivateChatActivity.startActivity(getActivity());
                        break;
                    case 1:
                        GroupChatActivity.startActivity(getActivity());
                        break;
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chat_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                NewChatActivity.startActivity(getActivity());
                break;
        }
        return true;
    }
}
