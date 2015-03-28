package com.quickblox.q_municate.ui.chats.dialogs;

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
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate.ui.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.chats.PrivateDialogActivity;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DatabaseManager;
import com.quickblox.q_municate_db.managers.DialogManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class DialogsFragment extends BaseFragment {

    private ListView dialogsListView;
    private DialogsAdapter dialogsAdapter;
    private TextView emptyListTextView;
    private DatabaseManager databaseManager;

    private Observer dialogObserver;

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

        initFields();
        initUI(view);
        initListeners();

        Crouton.cancelAllCroutons();

        initChatsDialogs();

        //        registerForContextMenu(dialogsListView);

        return view;
    }

    private void initFields() {
        databaseManager = DatabaseManager.getInstance();
        dialogObserver = new DialogObserver();
    }

    private void initUI(View view) {
        setHasOptionsMenu(true);
        dialogsListView = (ListView) view.findViewById(R.id.chats_listview);
        emptyListTextView = (TextView) view.findViewById(R.id.empty_list_textview);
    }

    private void initListeners() {
        dialogsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                startChat(position);
            }
        });
    }

    private void startChat(int position) {
        Dialog dialog = dialogsAdapter.getItem(position);
        if (dialog.getType() == Dialog.Type.PRIVATE) {
            startPrivateChatActivity(dialog);
        } else {
            startGroupChatActivity(dialog);
        }
    }

    private void checkVisibilityEmptyLabel() {
        emptyListTextView.setVisibility(dialogsAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        addObservers();
        Crouton.cancelAllCroutons();
        if (dialogsAdapter != null) {
            checkVisibilityEmptyLabel();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        deleteObservers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.dialogs_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                startNewDialogPage();
                break;
        }
        return true;
    }

    private void addObservers() {
        databaseManager.getDialogManager().addObserver(dialogObserver);
    }

    private void deleteObservers() {
        databaseManager.getDialogManager().deleteObserver(dialogObserver);
    }

    private void initChatsDialogs() {
        List<Dialog> dialogsList = databaseManager.getDialogManager().getAll();
        dialogsAdapter = new DialogsAdapter(baseActivity, dialogsList);
        dialogsListView.setAdapter(dialogsAdapter);
    }

    private void startPrivateChatActivity(Dialog dialog) {
        List<DialogOccupant> occupantsList = databaseManager.getDialogOccupantManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        User occupant = ChatUtils.getOpponentFromPrivateDialog(UserFriendUtils.createLocalUser(AppSession.getSession().getUser()), occupantsList);
        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            PrivateDialogActivity.start(baseActivity, occupant, dialog);
        }
    }

    private void startGroupChatActivity(Dialog dialog) {
        GroupDialogActivity.start(baseActivity, dialog);
    }

    private void startNewDialogPage() {
        boolean isFriends = !databaseManager.getFriendManager().getAll().isEmpty();
        if (isFriends) {
            NewDialogActivity.start(baseActivity);
        } else {
            DialogUtils.showLong(baseActivity, getString(R.string.ndl_no_friends_for_new_chat));
        }
    }

    private void updateDialogsList() {
        List<Dialog> dialogsList = databaseManager.getDialogManager().getAll();
        dialogsAdapter.setNewData(dialogsList);
        dialogsAdapter.notifyDataSetChanged();
        checkEmptyList(dialogsList.size());
    }

    //    @Override
    //    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
    //        super.onCreateContextMenu(menu, view, menuInfo);
    //        MenuInflater menuInflater = baseActivity.getMenuInflater();
    //        menuInflater.inflate(R.menu.dialogs_list_ctx_menu, menu);
    //    }

    //    @Override
    //    public boolean onContextItemSelected(MenuItem item) {
    //        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    //        switch (item.getItemId()) {
    //            case R.id.action_delete:
    //                Cursor selectedChatCursor = (Cursor) dialogsAdapter.getItem(adapterContextMenuInfo.position);
    //                QBDialog dialog = ChatDatabaseManager.getDialogFromCursor(selectedChatCursor);
    //                deleteDialog(dialog);
    //                break;
    //        }
    //        return true;
    //    }

    //    private void deleteDialog(QBDialog dialog) {
    //        QBDeleteDialogCommand.start(baseActivity, dialog.getDialogId(), dialog.getType());
    //    }

    private void checkEmptyList(int listSize) {
        if (listSize > 0) {
            emptyListTextView.setVisibility(View.GONE);
        } else {
            emptyListTextView.setVisibility(View.VISIBLE);
        }
    }

    private class DialogObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(DialogManager.OBSERVE_DIALOG)) {
                updateDialogsList();
            }
        }
    }
}