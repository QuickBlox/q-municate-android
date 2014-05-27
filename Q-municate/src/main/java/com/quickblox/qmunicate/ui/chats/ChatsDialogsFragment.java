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

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBLoadChatsDialogsCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;

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
                QBDialog dialog = chatsDialogsAdapter.getItem(position);
                if (dialog.getType() == QBDialogType.PRIVATE) {
                    startPrivateChatActivity(dialog);
                }
            }
        });
    }

    private void startPrivateChatActivity(QBDialog dialog) {
        int occupantId = ChatUtils.getOccupantsIdsFromDialog(dialog).get(Consts.ZERO_VALUE);
        Friend occupant = chatsDialogsAdapter.getOccupantById(occupantId);
        PrivateChatActivity.start(baseActivity, occupant);
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

    private void loadChatsDialogs() {
        QBLoadChatsDialogsCommand.start(baseActivity);
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

    private void initChatsDialogs(List<QBDialog> dialogsList) {
        chatsDialogsAdapter = new ChatsDialogsAdapter(baseActivity, dialogsList);
        chatsDialogsListView.setAdapter(chatsDialogsAdapter);
    }

    private class LoadChatsDialogsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            List<QBDialog> dialogsList = (List<QBDialog>) bundle.getSerializable(
                    QBServiceConsts.EXTRA_CHATS_DIALOGS);
            initChatsDialogs(dialogsList);
        }
    }
}