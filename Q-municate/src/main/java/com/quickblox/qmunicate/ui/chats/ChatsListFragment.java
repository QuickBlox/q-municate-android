package com.quickblox.qmunicate.ui.chats;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.Chat;
import com.quickblox.qmunicate.model.GroupChat;
import com.quickblox.qmunicate.model.PrivateChat;
import com.quickblox.qmunicate.qb.commands.QBLoadChatsDialogsCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseFragment;

import java.util.List;

public class ChatsListFragment extends BaseFragment {

    private ListView chatsListView;

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
        chatsListAdapter = new ChatsListAdapter(getActivity(), getAllChatConversations());
        chatsListView.setAdapter(chatsListAdapter);

        initUI();
        initListeners();

        return chatsListView;
    }

    private Cursor getAllChatConversations() {
        return DatabaseManager.getAllChatConversations(baseActivity);
    }

    private void initUI() {
        setHasOptionsMenu(true);
    }

    private void initListeners() {
        chatsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                Cursor cursor = (Cursor) chatsListAdapter.getItem(position);
                Chat chat = DatabaseManager.getChatFromCursor(cursor, baseActivity);
                //                Log.i("ChatName", "Size: " + ((ArrayList)((GroupChat)chat).getOpponentsList()).size());
                if (chat instanceof PrivateChat) {
                    PrivateChatActivity.start(baseActivity, ((PrivateChat) chat).getFriend());
                } else if (chat instanceof GroupChat) {
                    //TODO: implement opening of multichat dialog.
                    //                    Log.i("ChatName", chat.getName());
                    //                    for(Friend friend : ((GroupChat)chat).getOpponentsList()){
                    //                        Log.i("ChatName", friend.getFullname());
                    //                    }
                    //                    ArrayList<Friend> opponentsList = (ArrayList)((GroupChat)chat).getOpponentsList();
                    //                    Collections.sort(opponentsList, new NewChatActivity.SimpleComparator());
                    //                    GroupChatActivity.start(baseActivity, opponentsList);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        addActions();
        loadChatsDialogs();
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION,
                new LoadChatsDialogsSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chats_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                NewChatActivity.start(baseActivity);
                break;
        }
        return true;
    }

    private void loadChatsDialogs() {
        QBLoadChatsDialogsCommand.start(baseActivity);
    }

    private class LoadChatsDialogsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            List<QBDialog> dialogsList = (List<QBDialog>) bundle.getSerializable(QBServiceConsts.EXTRA_CHATS_DIALOGS);
        }
    }
}