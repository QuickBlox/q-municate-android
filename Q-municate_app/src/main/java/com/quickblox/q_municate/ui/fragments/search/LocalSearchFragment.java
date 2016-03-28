package com.quickblox.q_municate.ui.fragments.search;

import android.content.Context;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.chats.PrivateDialogActivity;
import com.quickblox.q_municate.utils.listeners.SearchListener;
import com.quickblox.q_municate.ui.activities.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.adapters.search.LocalSearchAdapter;
import com.quickblox.q_municate.ui.fragments.base.BaseLoaderFragment;
import com.quickblox.q_municate.utils.listeners.simple.SimpleOnRecycleItemClickListener;
import com.quickblox.q_municate.ui.views.recyclerview.SimpleDividerItemDecoration;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate_core.core.loader.BaseLoader;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.FriendDataManager;
import com.quickblox.q_municate_db.managers.UserRequestDataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;

import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.OnTouch;

public class LocalSearchFragment extends BaseLoaderFragment<List<Dialog>> implements SearchListener {

    private final static int LOADER_ID = LocalSearchFragment.class.hashCode();

    @Bind(R.id.dialogs_recyclerview)
    RecyclerView dialogsRecyclerView;

    private DataManager dataManager;
    private Observer commonObserver;
    private LocalSearchAdapter localSearchAdapter;
    private String searchQuery;
    private List<Dialog> dialogsList;

    public static LocalSearchFragment newInstance() {
        return new LocalSearchFragment();
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View view = layoutInflater.inflate(R.layout.fragment_local_search, container, false);

        activateButterKnife(view);

        initFields();
        initDialogsList();
        initCustomListeners();
        initDataLoader(LOADER_ID);

        addObservers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        localSearchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteObservers();
    }

    @OnTouch(R.id.dialogs_recyclerview)
    boolean touchList(View view, MotionEvent event) {
        KeyboardUtils.hideKeyboard(baseActivity);
        return false;
    }

    @Override
    public void prepareSearch() {
        if (localSearchAdapter != null) {
            localSearchAdapter.flushFilter();
        }
    }

    @Override
    public void search(String searchQuery) {
        if (localSearchAdapter != null) {
            localSearchAdapter.setFilter(searchQuery);
        }
    }

    @Override
    public void cancelSearch() {
        searchQuery = null;

        if (localSearchAdapter != null) {
            localSearchAdapter.flushFilter();
        }
    }

    @Override
    protected Loader<List<Dialog>> createDataLoader() {
        return new DialogsListLoader(getActivity(), dataManager);
    }

    @Override
    public void onLoadFinished(Loader<List<Dialog>> loader, List<Dialog> dialogsList) {
        this.dialogsList = dialogsList;
        updateLocal();
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);
        if (friendListHelper != null && localSearchAdapter != null) {
            localSearchAdapter.setFriendListHelper(friendListHelper);
        }
    }

    @Override
    public void onChangedUserStatus(int userId, boolean online) {
        super.onChangedUserStatus(userId, online);
        localSearchAdapter.notifyDataSetChanged();
    }

    private void initFields() {
        dataManager = DataManager.getInstance();
        commonObserver = new CommonObserver();
        dialogsList = Collections.emptyList();
    }

    private void initDialogsList() {
        localSearchAdapter = new LocalSearchAdapter(baseActivity, dialogsList);
        localSearchAdapter.setFriendListHelper(friendListHelper);
        dialogsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dialogsRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));;
        dialogsRecyclerView.setAdapter(localSearchAdapter);
    }

    private void initCustomListeners() {
        localSearchAdapter.setOnRecycleItemClickListener(new SimpleOnRecycleItemClickListener<Dialog>() {

            @Override
            public void onItemClicked(View view, Dialog dialog, int position) {
                if (dialog.getType() == Dialog.Type.PRIVATE) {
                    startPrivateChatActivity(dialog);
                } else {
                    startGroupChatActivity(dialog);
                }
            }
        });
    }

    private void startPrivateChatActivity(Dialog dialog) {
        List<DialogOccupant> occupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        User occupant = ChatUtils.getOpponentFromPrivateDialog(
                UserFriendUtils.createLocalUser(AppSession.getSession().getUser()), occupantsList);
        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            PrivateDialogActivity.start(baseActivity, occupant, dialog);
        }
    }

    private void addObservers() {
        dataManager.getUserRequestDataManager().addObserver(commonObserver);
        dataManager.getFriendDataManager().addObserver(commonObserver);
    }

    private void deleteObservers() {
        dataManager.getUserRequestDataManager().deleteObserver(commonObserver);
        dataManager.getFriendDataManager().deleteObserver(commonObserver);
    }

    private void updateList() {
        onChangedData();
    }

    private void updateLocal() {
        localSearchAdapter.setList(dialogsList);

        if (searchQuery != null) {
            search(searchQuery);
        }
    }

    private void startGroupChatActivity(Dialog dialog) {
        GroupDialogActivity.start(baseActivity, dialog);
    }

    private static class DialogsListLoader extends BaseLoader<List<Dialog>> {

        public DialogsListLoader(Context context, DataManager dataManager) {
            super(context, dataManager);
        }

        @Override
        protected List<Dialog> getItems() {
            return ChatUtils.fillTitleForPrivateDialogsList(getContext().getResources().getString(R.string.deleted_user),
                    dataManager, dataManager.getDialogDataManager().getAllSorted());
        }
    }

    private class CommonObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null) {
                if (data.equals(UserRequestDataManager.OBSERVE_KEY) || data.equals(FriendDataManager.OBSERVE_KEY)) {
                    updateList();
                }
            }
        }
    }
}