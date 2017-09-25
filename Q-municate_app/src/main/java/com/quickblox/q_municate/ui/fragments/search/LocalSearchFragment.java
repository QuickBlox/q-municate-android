package com.quickblox.q_municate.ui.fragments.search;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.chats.PrivateDialogActivity;
import com.quickblox.q_municate.utils.DialogsUtils;
import com.quickblox.q_municate.utils.listeners.SearchListener;
import com.quickblox.q_municate.ui.activities.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.adapters.search.LocalSearchAdapter;
import com.quickblox.q_municate.ui.fragments.base.BaseLoaderFragment;
import com.quickblox.q_municate.utils.listeners.simple.SimpleOnRecycleItemClickListener;
import com.quickblox.q_municate.ui.views.recyclerview.SimpleDividerItemDecoration;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate_core.core.loader.BaseLoader;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.DialogSearchWrapper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.utils.DialogTransformUtils;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import butterknife.Bind;
import butterknife.OnTouch;

public class LocalSearchFragment extends BaseLoaderFragment<List<DialogSearchWrapper>> implements SearchListener {

    private final static int LOADER_ID = LocalSearchFragment.class.hashCode();
    private static final String TAG = LocalSearchFragment.class.getSimpleName();
    private static final String RESULT_ACTION_NAME = "load_dialogs_for_local_search_screen";

    @Bind(R.id.dialogs_recyclerview)
    RecyclerView dialogsRecyclerView;

    private DataManager dataManager;
    private Observer commonObserver;
    private LocalSearchAdapter localSearchAdapter;
    private String searchQuery;
    private List<DialogSearchWrapper> dialogsList;
    private DialogsListLoader dialogsListLoader;

    private Queue<LoaderConsumer> loaderConsumerQueue = new ConcurrentLinkedQueue<>();
    protected Handler handler = new Handler();
    private LoadDialogsBroadcastReceiver loadDialogsBroadcastReceiver;

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
        registerLoadDialogsReceiver();
        initDataLoader(LOADER_ID);

        addObservers();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        localSearchAdapter.notifyDataSetChanged();
        updateDialogsListFromQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteObservers();
        unregisterLoadDialogsReceiver();
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
    protected Loader<List<DialogSearchWrapper>> createDataLoader() {
        dialogsListLoader = new DialogsListLoader(getActivity(), dataManager);
        return dialogsListLoader;

    }

    @Override
    public void onLoadFinished(Loader<List<DialogSearchWrapper>> loader, List<DialogSearchWrapper> dialogsList) {
        updateDialogsListFromQueue();

        updateDialogsAdapter(dialogsList);
    }

    private void updateDialogsListFromQueue() {
        if(!loaderConsumerQueue.isEmpty()) {
            LoaderConsumer consumer = loaderConsumerQueue.poll();
            handler.post(consumer);
        }
    }

    private void updateDialogsAdapter(List<DialogSearchWrapper> dialogsList) {
        if (dialogsListLoader.isLoadAll()) {
            this.dialogsList = dialogsList;
        } else {
            this.dialogsList.addAll(dialogsList);
        }

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
        dialogsList = new ArrayList<>();
    }

    private void initDialogsList() {
        localSearchAdapter = new LocalSearchAdapter(baseActivity, dialogsList);
        localSearchAdapter.setFriendListHelper(friendListHelper);
        dialogsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        dialogsRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));;
        dialogsRecyclerView.setAdapter(localSearchAdapter);
    }

    private void initCustomListeners() {
        localSearchAdapter.setOnRecycleItemClickListener(new SimpleOnRecycleItemClickListener<DialogSearchWrapper>() {

            @Override
            public void onItemClicked(View view, DialogSearchWrapper dialogSearchWrapper, int position) {
                if (QBDialogType.PRIVATE.equals(dialogSearchWrapper.getChatDialog().getType())) {
                    startPrivateChatActivity(dialogSearchWrapper.getChatDialog());
                } else {
                    startGroupChatActivity(dialogSearchWrapper.getChatDialog());
                }
            }
        });
    }

    private void startPrivateChatActivity(QBChatDialog chatDialog) {
        List<DialogOccupant> occupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(chatDialog.getDialogId());
        QMUser occupant = ChatUtils.getOpponentFromPrivateDialog(
                UserFriendUtils.createLocalUser(AppSession.getSession().getUser()), occupantsList);
        if (!TextUtils.isEmpty(chatDialog.getDialogId())) {
            PrivateDialogActivity.start(baseActivity, occupant, chatDialog);
        }
    }

    private void addObservers() {
        dataManager.getUserRequestDataManager().addObserver(commonObserver);
        dataManager.getFriendDataManager().addObserver(commonObserver);
        dataManager.getQBChatDialogDataManager().addObserver(commonObserver);
    }

    private void deleteObservers() {
        if (dataManager != null) {
            dataManager.getUserRequestDataManager().deleteObserver(commonObserver);
            dataManager.getFriendDataManager().deleteObserver(commonObserver);
            dataManager.getQBChatDialogDataManager().deleteObserver(commonObserver);
        }
    }

    private void registerLoadDialogsReceiver() {
        if (loadDialogsBroadcastReceiver == null){
            loadDialogsBroadcastReceiver = new  LoadDialogsBroadcastReceiver();
        }

        LocalBroadcastManager.getInstance(baseActivity).registerReceiver(loadDialogsBroadcastReceiver, new IntentFilter(RESULT_ACTION_NAME));
    }

    private void unregisterLoadDialogsReceiver(){
        if (loadDialogsBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(baseActivity).unregisterReceiver(loadDialogsBroadcastReceiver);
        }
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

    private void startGroupChatActivity(QBChatDialog chatDialog) {
        GroupDialogActivity.start(baseActivity, chatDialog);
    }

    private void updateDialogsList(int startRow, int perPage) {
//        logic for correct behavior of pagination loading dialogs
//        we can't fire onChangedData until we have incomplete loader task in queue
        if (!loaderConsumerQueue.isEmpty()) {
            Log.d(TAG, "updateDialogsList loaderConsumerQueue.add");
            loaderConsumerQueue.offer(new LoaderConsumer(startRow, perPage));
            return;
        }

//        if Loader is in loading process, we don't fire onChangedData, cause we do not want interrupt current load task
        if (dialogsListLoader.isLoading) {
            Log.d(TAG, "updateDialogsList dialogsListLoader.isLoading");
            loaderConsumerQueue.offer(new LoaderConsumer(startRow, perPage));
        } else {
//        we don't have tasks in queue, so load dialogs by pages
            if(!isResumed()) {
                loaderConsumerQueue.offer(new LoaderConsumer(startRow, perPage));
            } else {
                Log.d(TAG, "updateDialogsList onChangedData");
                dialogsListLoader.setPagination(startRow, perPage);
                onChangedData();
            }
        }
    }

    private static class DialogsListLoader extends BaseLoader<List<DialogSearchWrapper>> {

        private static final String TAG = DialogsListLoader.class.getSimpleName();
        private boolean loadAll;
        private int startRow = 0;
        private int perPage = 0;

        public DialogsListLoader(Context context, DataManager dataManager) {
            super(context, dataManager);
        }

        public boolean isLoadAll() {
            return loadAll;
        }

        public void setLoadAll(boolean loadAll) {
            this.loadAll = loadAll;
        }

        public void setPagination(int startRow, int perPage) {
            this.startRow = startRow;
            this.perPage = perPage;
        }

        @Override
        protected List<DialogSearchWrapper> getItems() {
            Log.d(TAG, "getItems() chatDialogs startRow= " + startRow + ", perPage= " + perPage + ", loadAll= " + loadAll);

            List<QBChatDialog> dialogsList = loadAll ? dataManager.getQBChatDialogDataManager().getAllSorted() :
                    dataManager.getQBChatDialogDataManager().getSkippedSorted(startRow, perPage);

            List<DialogSearchWrapper> wrappedList = new ArrayList<>(dialogsList.size());
            for (QBChatDialog dialog : dialogsList){
                wrappedList.add(new DialogSearchWrapper(getContext(), dataManager, dialog));
            }

            return wrappedList;
        }

        private void retrieveAllDialogsFromCacheByPages() {
            long dialogsCount = DataManager.getInstance().getQBChatDialogDataManager().getAllCount();
            boolean isCacheEmpty = dialogsCount <= 0;

            if(isCacheEmpty) {
                return;
            }

            DialogsUtils.loadAllDialogsFromCacheByPagesTask(getContext(), dialogsCount, RESULT_ACTION_NAME);
        }

        @Override
        public void loadData(){
            retrieveAllDialogsFromCacheByPages();
        }
    }

    private class LoaderConsumer implements Runnable {
        boolean loadAll;
        int startRow;
        int perPage;

        LoaderConsumer(int startRow, int perPage) {
            this.startRow = startRow;
            this.perPage = perPage;
        }

        @Override
        public void run() {
            Log.d(TAG, "LoaderConsumer onChangedData");
            dialogsListLoader.setLoadAll(loadAll);
            dialogsListLoader.setPagination(startRow, perPage);
            onChangedData();
        }
    }

    private class CommonObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null) {
                String observerKey = ((Bundle) data).getString(BaseManager.EXTRA_OBSERVE_KEY);
                int action = ((Bundle) data).getInt(BaseManager.EXTRA_ACTION);
                Log.v(TAG, "CommonObserver.update() observerKey = " + observerKey);
                Log.v(TAG, "action = " + action);

                if (observerKey.equals(dataManager.getQBChatDialogDataManager().getObserverKey())) {
                    if (((Bundle) data).getSerializable(BaseManager.EXTRA_OBJECT) != null) {
                        Dialog dialog = (Dialog) ((Bundle) data).getSerializable(BaseManager.EXTRA_OBJECT);
                        QBChatDialog chatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialog.getDialogId());
                        DialogSearchWrapper wrappedChatDialog = new DialogSearchWrapper(baseActivity, dataManager, chatDialog);

                        if (action == BaseManager.DELETE_ACTION) {
                            localSearchAdapter.removeItem(wrappedChatDialog);
                        }
                    } else if (action == BaseManager.DELETE_BY_ID_ACTION) {
                        String removedDialogId = ((Bundle) data).getString(BaseManager.EXTRA_OBJECT_ID);
                        localSearchAdapter.removeItemByDialogId(removedDialogId);
                    }
                }
            }
        }
    }

    private class LoadDialogsBroadcastReceiver extends BroadcastReceiver{
        private final String TAG = LoadDialogsBroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            Log.d(TAG, "bundle= " + bundle);
            if(bundle != null) {
                if (isLoadPerPage(bundle)) {
                    updateDialogsList(bundle.getInt(ConstsCore.DIALOGS_START_ROW), bundle.getInt(ConstsCore.DIALOGS_PER_PAGE));
                }
            }
        }

        private boolean isLoadPerPage(Bundle bundle) {
            return bundle.get(ConstsCore.DIALOGS_START_ROW) != null && bundle.get(ConstsCore.DIALOGS_PER_PAGE) != null;
        }
    }
}