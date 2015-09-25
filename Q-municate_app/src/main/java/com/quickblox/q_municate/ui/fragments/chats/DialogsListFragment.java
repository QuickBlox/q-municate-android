package com.quickblox.q_municate.ui.fragments.chats;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.chats.DialogsListAdapter;
import com.quickblox.q_municate.ui.fragments.base.BaseFragment;
import com.quickblox.q_municate.ui.activities.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.activities.chats.PrivateDialogActivity;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.commands.QBDeleteChatCommand;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.DialogDataManager;
import com.quickblox.q_municate_db.managers.DialogOccupantDataManager;
import com.quickblox.q_municate_db.managers.MessageDataManager;
import com.quickblox.q_municate_db.managers.UserDataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.OnItemClick;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public class  DialogsListFragment extends BaseFragment {

    private static final String TAG = DialogsListFragment.class.getSimpleName();

    @Bind(R.id.chats_listview)
    ListView dialogsListView;

    @Bind(R.id.empty_list_textview)
    TextView emptyListTextView;

    private DialogsListAdapter dialogsListAdapter;
    private DataManager dataManager;

    private QBUser qbUser;

    private Observer dialogObserver;
    private Observer messageObserver;
    private Observer userObserver;
    private Observer dialogOccupantsObserver;

    public static DialogsListFragment newInstance() {
        return new DialogsListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dialogs_list, container, false);

        activateButterKnife(view);

        initFields();
        initChatsDialogs();

        registerForContextMenu(dialogsListView);

        Crouton.cancelAllCroutons();

        return view;
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        // HACK
        actionBarBridge.setActionBarTitle(" " + qbUser.getFullName());

        checkVisibilityUserIcon();
    }

    private void initFields() {
        dataManager = DataManager.getInstance();
        dialogObserver = new DialogObserver();
        messageObserver = new MessageObserver();
        userObserver = new UsersObserver();
        dialogOccupantsObserver = new DialogOccupantsObserver();
        qbUser = AppSession.getSession().getUser();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater menuInflater = baseActivity.getMenuInflater();
        menuInflater.inflate(R.menu.dialogs_list_ctx_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item
                .getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete:
                Dialog dialog = dialogsListAdapter.getItem(adapterContextMenuInfo.position);
                deleteDialog(dialog);
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        addObservers();

        Crouton.cancelAllCroutons();

        if (dialogsListAdapter != null) {
            checkVisibilityEmptyLabel();
        }

        if (dialogsListAdapter != null) {
            dialogsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteObservers();
    }

    @OnItemClick(R.id.chats_listview)
    public void startChat(int position) {
        Dialog dialog = dialogsListAdapter.getItem(position);
        if (dialog.getType() == Dialog.Type.PRIVATE) {
            startPrivateChatActivity(dialog);
        } else {
            startGroupChatActivity(dialog);
        }
    }

    private void checkVisibilityEmptyLabel() {
        emptyListTextView.setVisibility(dialogsListAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void checkVisibilityUserIcon() {
        UserCustomData userCustomData = Utils.customDataToObject(qbUser.getCustomData());
        if (!TextUtils.isEmpty(userCustomData.getAvatar_url())) {
            loadLogoActionBar(userCustomData.getAvatar_url());
        }
    }

    private void loadLogoActionBar(String logoUrl) {
        ImageLoader.getInstance().loadImage(logoUrl,
                ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        actionBarBridge.setActionBarIcon(
                                ImageUtils.getRoundIconDrawable(getActivity(), loadedBitmap));
                    }
                });
    }

    private void addObservers() {
        dataManager.getDialogDataManager().addObserver(dialogObserver);
        dataManager.getMessageDataManager().addObserver(messageObserver);
        dataManager.getUserDataManager().addObserver(userObserver);
        dataManager.getDialogOccupantDataManager().addObserver(dialogOccupantsObserver);
    }

    private void deleteObservers() {
        dataManager.getDialogDataManager().deleteObserver(dialogObserver);
        dataManager.getMessageDataManager().deleteObserver(messageObserver);
        dataManager.getUserDataManager().deleteObserver(userObserver);
        dataManager.getDialogOccupantDataManager().deleteObserver(dialogOccupantsObserver);
    }

    private void initChatsDialogs() {
        List<Dialog> dialogsList = dataManager.getDialogDataManager().getAll();
        dialogsListAdapter = new DialogsListAdapter(baseActivity, dialogsList);
        dialogsListView.setAdapter(dialogsListAdapter);
    }

    private void startPrivateChatActivity(Dialog dialog) {
        List<DialogOccupant> occupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        User occupant = ChatUtils.getOpponentFromPrivateDialog(UserFriendUtils.createLocalUser(AppSession.getSession().getUser()), occupantsList);
        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            PrivateDialogActivity.start(baseActivity, occupant, dialog);
        }
    }

    private void startGroupChatActivity(Dialog dialog) {
        GroupDialogActivity.start(baseActivity, dialog);
    }

    private void updateDialogsList() {
        List<Dialog> dialogsList = dataManager.getDialogDataManager().getAll();
        dialogsListAdapter.setNewData(dialogsList);
        dialogsListAdapter.notifyDataSetChanged();
        checkEmptyList(dialogsList.size());
    }

    private void deleteDialog(Dialog dialog) {
        QBDeleteChatCommand.start(baseActivity, dialog.getDialogId(), dialog.getType());
    }

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
            if (data != null && data.equals(DialogDataManager.OBSERVE_KEY)) {
                updateDialogsList();
            }
        }
    }

    private class MessageObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(MessageDataManager.OBSERVE_KEY)) {
                updateDialogsList();
            }
        }
    }

    private class UsersObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(UserDataManager.OBSERVE_KEY)) {
                updateDialogsList();
            }
        }
    }

    private class DialogOccupantsObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(DialogOccupantDataManager.OBSERVE_KEY)) {
                updateDialogsList();
            }
        }
    }
}