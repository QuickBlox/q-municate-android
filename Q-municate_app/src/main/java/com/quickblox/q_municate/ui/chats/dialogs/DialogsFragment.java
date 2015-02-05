package com.quickblox.q_municate.ui.chats.dialogs;

import android.app.LoaderManager;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
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

import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate.ui.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.chats.PrivateDialogActivity;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.db.tables.UserTable;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DialogUtils;

import java.util.ArrayList;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class DialogsFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DIALOGS_LOADER_ID = 0;

    private ListView dialogsListView;
    private DialogsAdapter dialogsAdapter;
    private TextView emptyListTextView;
    private ContentObserver userTableContentObserver;
    private Cursor dialogsCursor;
    private int selectedPositionList;

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
        Crouton.cancelAllCroutons();

        addActions();
        initCursorLoaders();

        registerForContextMenu(dialogsListView);
        registerContentObservers();

        return view;
    }

    private void registerContentObservers() {
        userTableContentObserver = new ContentObserver(new Handler()) {

            @Override
            public void onChange(boolean selfChange) {
                selectedPositionList = dialogsListView.getFirstVisiblePosition();
                initCursorLoaders();
            }
        };

        baseActivity.getContentResolver().registerContentObserver(UserTable.CONTENT_URI, true,
                userTableContentObserver);
    }

    private void unregisterContentObservers() {
        baseActivity.getContentResolver().unregisterContentObserver(userTableContentObserver);
    }

    private void initCursorLoaders() {
        getLoaderManager().initLoader(DIALOGS_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ChatDatabaseManager.getAllDialogsCursorLoader(baseActivity);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dialogsCursor) {
        this.dialogsCursor = dialogsCursor;
        initChatsDialogs();
        checkVisibilityEmptyLabel();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
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
                Cursor selectedChatCursor = (Cursor) dialogsAdapter.getItem(position);
                QBDialog dialog = ChatDatabaseManager.getDialogFromCursor(selectedChatCursor);
                if (dialog.getType() == QBDialogType.PRIVATE) {
                    startPrivateChatActivity(dialog);
                } else {
                    startGroupChatActivity(dialog);
                }
            }
        });
    }

    private void checkVisibilityEmptyLabel() {
        emptyListTextView.setVisibility(dialogsAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        Crouton.cancelAllCroutons();
        if (dialogsAdapter != null) {
            checkVisibilityEmptyLabel();
        }
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        unregisterContentObservers();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dialogs_list_menu, menu);
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

    private void initChatsDialogs() {
        dialogsAdapter = new DialogsAdapter(baseActivity, dialogsCursor);
        dialogsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkVisibilityEmptyLabel();
            }
        });
        dialogsListView.setAdapter(dialogsAdapter);

        if (selectedPositionList != ConstsCore.ZERO_INT_VALUE) {
            dialogsListView.setSelection(selectedPositionList);
        }
    }

    private void startPrivateChatActivity(QBDialog dialog) {
        int occupantId = ChatUtils.getOccupantIdFromList(dialog.getOccupants());
        User occupant = dialogsAdapter.getOccupantById(occupantId);
        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            PrivateDialogActivity.start(baseActivity, occupant, dialog);
        }
    }

    private void startGroupChatActivity(QBDialog dialog) {
        GroupDialogActivity.start(baseActivity, dialog);
    }

    private void startNewDialogPage() {
        boolean isFriends = UsersDatabaseManager.getAllFriends(baseActivity)
                .getCount() > ConstsCore.ZERO_INT_VALUE;
        if (isFriends) {
            NewDialogActivity.start(baseActivity);
        } else {
            DialogUtils.showLong(baseActivity, getResources().getString(
                    R.string.ndl_no_friends_for_new_chat));
        }
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION,
                new LoadChatsDialogsSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
    }

    //    @Override
    //    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
    //        super.onCreateContextMenu(menu, view, menuInfo);
    //        MenuInflater menuInflater = baseActivity.getMenuInflater();
    //        menuInflater.inflate(R.menu.dialogs_list_ctx_menu, menu);
    //    }

    private class LoadChatsDialogsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            ArrayList<ParcelableQBDialog> parcelableDialogsList = bundle.getParcelableArrayList(
                    QBServiceConsts.EXTRA_CHATS_DIALOGS);
            if (parcelableDialogsList == null) {
                emptyListTextView.setVisibility(View.VISIBLE);
            }
        }
    }
}