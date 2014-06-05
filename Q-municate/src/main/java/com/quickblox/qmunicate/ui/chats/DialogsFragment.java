package com.quickblox.qmunicate.ui.chats;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.TipsManager;

public class DialogsFragment extends BaseFragment {

    private ListView dialogsListView;
    private DialogsAdapter dialogsAdapter;

    public static DialogsFragment newInstance() {
        return new DialogsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getString(R.string.nvd_title_chats);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialogs_list, container, false);

        initUI(view);
        initListeners();
        initChatsDialogs();

        TipsManager.showTipIfNotShownYet(this, baseActivity.getString(R.string.tip_chats_list));

        return view;
    }

    private void initUI(View view) {
        setHasOptionsMenu(true);
        dialogsListView = (ListView) view.findViewById(R.id.chats_listview);
    }

    private void initListeners() {
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                Cursor selectedChatCursor = (Cursor) dialogsAdapter.getItem(position);
                QBDialog dialog = DatabaseManager.getDialogFromCursor(selectedChatCursor);
                if (dialog.getType() == QBDialogType.PRIVATE) {
                    startPrivateChatActivity(dialog);
                } else {
                    startGroupChatActivity(dialog);
                }
            }
        });
    }

    private void initChatsDialogs() {
        dialogsAdapter = new DialogsAdapter(baseActivity, getAllChats());
        dialogsListView.setAdapter(dialogsAdapter);
    }

    private void startPrivateChatActivity(QBDialog dialog) {
        int occupantId = ChatUtils.getOccupantIdFromList(dialog.getOccupants());
        Friend occupant = dialogsAdapter.getOccupantById(occupantId);
        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            PrivateDialogActivity.start(baseActivity, occupant, dialog);
        } else {
            PrivateDialogActivity.start(baseActivity, occupant, null);
        }
    }

    private void startGroupChatActivity(QBDialog dialog) {
        GroupDialogActivity.start(baseActivity, dialog.getRoomJid());
    }

    private Cursor getAllChats() {
        return DatabaseManager.getAllDialogs(baseActivity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dialogs_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                NewDialogActivity.start(baseActivity);
                break;
        }
        return true;
    }
}