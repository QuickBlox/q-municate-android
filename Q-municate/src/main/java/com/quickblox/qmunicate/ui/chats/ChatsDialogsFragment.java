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

import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.ChatCache;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBLoadChatsDialogsCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;

public class ChatsDialogsFragment extends BaseFragment {

    private ListView chatsDialogsListView;
    private ChatsDialogsAdapter chatsDialogsAdapter;

    public static ChatsDialogsFragment newInstance() {
        return new ChatsDialogsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getString(R.string.nvd_title_chats);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats_list, container, false);

        initUI(view);
        initListeners();
        initChatsDialogs();
        
        return view;
    }

    private void initUI(View view) {
        setHasOptionsMenu(true);
        chatsDialogsListView = (ListView) view.findViewById(R.id.chats_listview);
    }

    private void initListeners() {
        chatsDialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                Cursor selectedChatCursor = (Cursor) chatsDialogsAdapter.getItem(position);
                ChatCache chatCache = DatabaseManager.getChatCacheFromCursor(selectedChatCursor);
                if (chatCache.getType() == QBDialogType.PRIVATE.ordinal()) {
                    startPrivateChatActivity(chatCache);
                } else {
                    startGroupChatActivity(chatCache);
                }
            }
        });
    }

    private void startPrivateChatActivity(ChatCache chatCache) {
        int occupantId = ChatUtils.getOccupantIdFromArray(chatCache.getOccupantsIds());
        Friend occupant = chatsDialogsAdapter.getOccupantById(occupantId);
        if (chatCache.getDialogId().equals(occupantId + Consts.EMPTY_STRING)) {
            PrivateChatActivity.start(baseActivity, occupant, null);
        } else {
            PrivateChatActivity.start(baseActivity, occupant, chatCache);
        }
    }

    private void startGroupChatActivity(ChatCache chatCache) {
        GroupChatActivity.start(baseActivity, chatCache);
    }

    @Override
    public void onResume() {
        super.onResume();
        addActions();
        loadChatsDialogs();
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

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION,
                new LoadChatsDialogsSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
    }

    private void loadChatsDialogs() {
        QBLoadChatsDialogsCommand.start(baseActivity);
    }

    private void initChatsDialogs() {
        chatsDialogsAdapter = new ChatsDialogsAdapter(baseActivity, getAllChats());
        chatsDialogsListView.setAdapter(chatsDialogsAdapter);
    }

    private Cursor getAllChats() {
        return DatabaseManager.getAllChats(baseActivity);
    }

    private class LoadChatsDialogsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
//            List<QBDialog> dialogsList = (List<QBDialog>) bundle.getSerializable(
//                    QBServiceConsts.EXTRA_CHATS_DIALOGS);
//
//            initChatsDialogs();
        }
    }
}