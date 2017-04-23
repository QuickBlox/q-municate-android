package com.quickblox.q_municate.ui.fragments.chats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.about.AboutActivity;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.activities.chats.NewMessageActivity;
import com.quickblox.q_municate.ui.activities.chats.PrivateDialogActivity;
import com.quickblox.q_municate.ui.activities.feedback.FeedbackActivity;
import com.quickblox.q_municate.ui.activities.invitefriends.InviteFriendsActivity;
import com.quickblox.q_municate.ui.activities.settings.SettingsActivity;
import com.quickblox.q_municate.ui.adapters.chats.DialogsListAdapter;
import com.quickblox.q_municate.ui.fragments.base.BaseLoaderFragment;
import com.quickblox.q_municate.ui.fragments.search.SearchFragment;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.core.loader.BaseLoader;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.DialogWrapper;
import com.quickblox.q_municate_core.qb.commands.chat.QBDeleteChatCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoadDialogsCommand;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCompositeCommand;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_user_cache.QMUserCacheImpl;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import butterknife.Bind;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class DialogsListFragment extends BaseLoaderFragment<List<DialogWrapper>> implements BaseActivity.LoadChatsSuccessActionCallback{

    public static final int PICK_DIALOG = 100;

    private static final String TAG = DialogsListFragment.class.getSimpleName();
    private static final int LOADER_ID = DialogsListFragment.class.hashCode();

    @Bind(R.id.chats_listview)
    ListView dialogsListView;

    @Bind(R.id.empty_list_textview)
    TextView emptyListTextView;

    private DialogsListAdapter dialogsListAdapter;
    private DataManager dataManager;
    private QBUser qbUser;
    private Observer commonObserver;
    private DialogsListLoader dialogsListLoader;
    private Queue<LoaderConsumer> loaderConsumerQueue = new ConcurrentLinkedQueue<>();

    private boolean isAfterResumed;

    protected DialogListFragmentListener dialogListFragmentListener;
    protected Handler handler = new Handler();

    public static DialogsListFragment newInstance() {
        return new DialogsListFragment();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            dialogListFragmentListener = (DialogListFragmentListener) activity;
            Log.d(TAG, "dialogListFragmentListener= " + dialogListFragmentListener);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogListFragmentListener");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_dialogs_list, container, false);

        activateButterKnife(view);

        initFields();
        initChatsDialogs();

        registerForContextMenu(dialogsListView);

        return view;
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        actionBarBridge.setActionBarUpButtonEnabled(false);

        loadingBridge.hideActionBarProgress();
    }

    private void initFields() {
        dataManager = DataManager.getInstance();
        commonObserver = new CommonObserver();
        qbUser = AppSession.getSession().getUser();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        baseActivity.showSnackbar(R.string.dialog_loading_dialogs, Snackbar.LENGTH_INDEFINITE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.dialogs_list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                launchContactsFragment();
                break;
            case R.id.action_start_invite_friends:
                InviteFriendsActivity.start(getActivity());
                break;
            case R.id.action_start_feedback:
                FeedbackActivity.start(getActivity());
                break;
            case R.id.action_start_settings:
                SettingsActivity.start(getActivity());
                break;
            case R.id.action_start_about:
                AboutActivity.start(getActivity());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        MenuInflater menuInflater = baseActivity.getMenuInflater();
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        QBChatDialog chatDialog = dialogsListAdapter.getItem(adapterContextMenuInfo.position).getChatDialog();
        if(chatDialog.getType().equals(QBDialogType.GROUP)){
            menuInflater.inflate(R.menu.dialogs_list_group_ctx_menu, menu);
        } else{
            menuInflater.inflate(R.menu.dialogs_list_private_ctx_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.action_delete:
                if (baseActivity.checkNetworkAvailableWithError()) {
                    QBChatDialog chatDialog = dialogsListAdapter.getItem(adapterContextMenuInfo.position).getChatDialog();
                    deleteDialog(chatDialog);
                }
                break;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        addActions();
        initDataLoader(LOADER_ID);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        dialogListFragmentListener.setLoadChatsSuccessActionCallback(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        addObservers();
        isAfterResumed = true;
        if (dialogsListAdapter != null) {
            checkVisibilityEmptyLabel();
        }
        if (dialogsListAdapter != null) {
            dialogsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        dialogListFragmentListener.removeLoadChatsSuccessActionCallback(this);
        deleteObservers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy() deleteObservers");
        removeActions();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult "+requestCode);
        if (PICK_DIALOG == requestCode && data != null){
            updateDialog(data.getStringExtra(QBServiceConsts.EXTRA_DIALOG_ID));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateDialog(String dialogId) {
        QBChatDialog qbChatDialog = dataManager.getQBChatDialogDataManager().getByDialogId(dialogId);
        DialogWrapper dialogWrapper = new DialogWrapper(getContext(), dataManager, qbChatDialog);
        Log.i(TAG, "updateDialog dialogWrapper="+dialogWrapper.getTotalCount());
        dialogsListAdapter.updateItem(dialogWrapper);

        int start = dialogsListView.getFirstVisiblePosition();
        for(int i=start, j=dialogsListView.getLastVisiblePosition();i<=j;i++) {
            DialogWrapper result = (DialogWrapper) dialogsListView.getItemAtPosition(i);
            if (result.getChatDialog().getDialogId().equals(dialogId)) {
                View view = dialogsListView.getChildAt(i - start);
                dialogsListView.getAdapter().getView(i, view, dialogsListView);
                break;
            }
        }
    }

    @OnItemClick(R.id.chats_listview)
    void startChat(int position) {
        QBChatDialog chatDialog = dialogsListAdapter.getItem(position).getChatDialog();

        if (!baseActivity.checkNetworkAvailableWithError() && isFirstOpeningDialog(chatDialog.getDialogId())) {
            return;
        }

        if (QBDialogType.PRIVATE.equals(chatDialog.getType())) {
            startPrivateChatActivity(chatDialog);
        } else {
            startGroupChatActivity(chatDialog);
        }
    }

    @OnClick(R.id.fab_dialogs_new_chat)
    public void onAddChatClick(View view) {
        addChat();
    }

    private boolean isFirstOpeningDialog(String dialogId){
        return !dataManager.getMessageDataManager().getTempMessagesByDialogId(dialogId).isEmpty();
    }

    @Override
    public void onConnectedToService(QBService service) {
        if (chatHelper == null) {
            if (service != null) {
                chatHelper = (QBChatHelper) service.getHelper(QBService.CHAT_HELPER);
            }
        }
    }

    @Override
    protected Loader<List<DialogWrapper>> createDataLoader() {
        dialogsListLoader = new DialogsListLoader(getActivity(), dataManager);
        return dialogsListLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<DialogWrapper>> loader, List<DialogWrapper> dialogsList) {
        updateDialogsListFromQueue();
//TODO CHECK THIS isAfterResumed RP
        if(isAfterResumed){
            Log.d(TAG, "onLoadFinished isAfterResumed");
            isAfterResumed = false;
            dialogsListLoader.loadCacheFinished = false;

        }
            if(dialogsListLoader.isNeedUpdate()) {
            dialogsListLoader.needUpdate = false;
            dialogsListAdapter.setNewData(dialogsList);
        }
        else {
            dialogsListAdapter.addNewData((ArrayList<DialogWrapper>) dialogsList);
        }
        Log.d(TAG, "onLoadFinished dialogsListAdapter.getCount() " + dialogsListAdapter.getCount());
        checkEmptyList(dialogsListAdapter.getCount());
        if(!baseActivity.isDialogLoading()) {
            Log.d(TAG, "onLoadFinished baseActivity.hideSnackBar()");
            baseActivity.hideSnackBar();
        }
        if(dialogsListLoader.isLoadCacheFinished()) {
            dialogsListLoader.setLoadCacheFinished(false);
            loadDialogsFromREST(getContext(), true);
        }
    }

    private void addChat(){
        boolean hasFriends = !dataManager.getFriendDataManager().getAll().isEmpty();
        if (isFriendsLoading()){
            ToastUtils.longToast(R.string.chat_service_is_initializing);
        } else if (!hasFriends){
            ToastUtils.longToast(R.string.new_message_no_friends_for_new_message);
        } else {
            NewMessageActivity.start(getActivity());
        }
    }

    private boolean isFriendsLoading(){
        return QBLoginChatCompositeCommand.isRunning();
    }

    private void checkVisibilityEmptyLabel() {
        emptyListTextView.setVisibility(dialogsListAdapter.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void addObservers() {
        dataManager.getQBChatDialogDataManager().addObserver(commonObserver);
        dataManager.getMessageDataManager().addObserver(commonObserver);
        ((Observable)QMUserService.getInstance().getUserCache()).addObserver(commonObserver);
        dataManager.getDialogOccupantDataManager().addObserver(commonObserver);
    }

    private void deleteObservers() {
        if (dataManager != null) {
            dataManager.getQBChatDialogDataManager().deleteObserver(commonObserver);
            dataManager.getMessageDataManager().deleteObserver(commonObserver);
            ((Observable)QMUserService.getInstance().getUserCache()).deleteObserver(commonObserver);
            dataManager.getDialogOccupantDataManager().deleteObserver(commonObserver);
        }
    }

    private void removeActions() {
        baseActivity.removeAction(QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION);
        baseActivity.removeAction(QBServiceConsts.DELETE_DIALOG_FAIL_ACTION);


        baseActivity.updateBroadcastActionList();
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.DELETE_DIALOG_SUCCESS_ACTION, new DeleteDialogSuccessAction());
        baseActivity.addAction(QBServiceConsts.DELETE_DIALOG_FAIL_ACTION, new DeleteDialogFailAction());
        baseActivity.addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION, baseActivity.new LoadChatsSuccessAction());

        baseActivity.updateBroadcastActionList();
    }

    private void initChatsDialogs() {
        List<DialogWrapper> dialogsList = new ArrayList<>();
        dialogsListAdapter = new DialogsListAdapter(baseActivity, dialogsList);
        dialogsListView.setAdapter(dialogsListAdapter);
    }

    private void startPrivateChatActivity(QBChatDialog chatDialog) {
        List<DialogOccupant> occupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(chatDialog.getDialogId());
        QMUser opponent = ChatUtils.getOpponentFromPrivateDialog(UserFriendUtils.createLocalUser(qbUser), occupantsList);

        if (!TextUtils.isEmpty(chatDialog.getDialogId())) {
            PrivateDialogActivity.startForResult(this, opponent, chatDialog, PICK_DIALOG);
        }
    }

    private void startGroupChatActivity(QBChatDialog chatDialog) {
        GroupDialogActivity.startForResult(this, chatDialog, PICK_DIALOG);
    }

    private void updateDialogsList(int startRow, int perPage, boolean update) {
        if(!loaderConsumerQueue.isEmpty()){
            Log.d(TAG, "updateDialogsList loaderConsumerQueue.add");
            loaderConsumerQueue.offer(new LoaderConsumer(startRow, perPage, update));
            return;
        }

        if(dialogsListLoader.isLoading) {
            Log.d(TAG, "updateDialogsList dialogsListLoader.isLoading");
            loaderConsumerQueue.offer(new LoaderConsumer(startRow, perPage, update));
        } else {
            Log.d(TAG, "updateDialogsList onChangedData");
            dialogsListLoader.isLoading = true;
            dialogsListLoader.setPagination(startRow, perPage, update);
            onChangedData();
        }
    }

    private void updateDialogsList(){
        if(!loaderConsumerQueue.isEmpty()){
            Log.d(TAG, "updateDialogsList loaderConsumerQueue.add");
            loaderConsumerQueue.offer(new LoaderConsumer(true));
            return;
        }

        if(dialogsListLoader.isLoading) {
            Log.d(TAG, "updateDialogsList dialogsListLoader.isLoading");
            loaderConsumerQueue.offer(new LoaderConsumer(true));
        } else {
            dialogsListLoader.setLoadAll(true);
            onChangedData();
        }
    }

    private void updateDialogsListFromQueue() {
        if(!loaderConsumerQueue.isEmpty()) {
            LoaderConsumer consumer = loaderConsumerQueue.poll();
            handler.post(consumer);
        }
    }

    private class LoaderConsumer implements Runnable {
        boolean loadAll;
        boolean update;
        int startRow;
        int perPage;

        LoaderConsumer(boolean loadAll) {
            this.loadAll = loadAll;
        }

        LoaderConsumer(int startRow, int perPage, boolean update) {
            this.startRow = startRow;
            this.perPage = perPage;
            this.update = update;
        }

        @Override
        public void run() {
            Log.d(TAG, "LoaderConsumer onChangedData");
            dialogsListLoader.setLoadAll(loadAll);
            dialogsListLoader.setPagination(startRow, perPage, update);
            onChangedData();
        }
    }

    private void deleteDialog(QBChatDialog chatDialog) {
        if(chatDialog == null || chatDialog.getDialogId() == null){
            return;
        }

        baseActivity.showProgress();
        QBDeleteChatCommand.start(baseActivity, chatDialog.getDialogId(), chatDialog.getType().getCode());
    }

    private void checkEmptyList(int listSize) {
        if (listSize > 0) {
            emptyListTextView.setVisibility(View.GONE);
        } else {
            emptyListTextView.setVisibility(View.VISIBLE);
        }
    }

    private void launchContactsFragment() {
        baseActivity.setCurrentFragment(SearchFragment.newInstance(), true);
    }

    @Override
    public void performLoadChatsSuccessActionPerPage(int startRow, int perPage, boolean update) {
        updateDialogsList(startRow, perPage, update);
    }

    @Override
    public void performLoadChatsSuccessActionUpdateAll() {
        updateDialogsList();
    }

    private class DeleteDialogSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            baseActivity.hideProgress();
        }
    }

    private class DeleteDialogFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            baseActivity.hideProgress();
        }
    }

    private static class DialogsListLoader extends BaseLoader<List<DialogWrapper>> {
        boolean loadAll;
        boolean needUpdate;

        boolean loadCacheFinished;
        boolean loadFromCache;

        int startRow = 0;
        int perPage = 0;

        DialogsListLoader(Context context, DataManager dataManager) {
            super(context, dataManager);
        }

        boolean isNeedUpdate() {
            return needUpdate;
        }

        boolean isLoadCacheFinished() {
            return loadCacheFinished;
        }

        void setLoadAll(boolean loadAll) {
            this.loadAll = loadAll;
            if(loadAll) {
                needUpdate = true;
            }
        }

        void setLoadCacheFinished(boolean loadCacheFinished) {
            this.loadCacheFinished = loadCacheFinished;
        }

        void setPagination(int startRow, int perPage, boolean update) {
            this.needUpdate = update;
            this.startRow = startRow;
            this.perPage = perPage;
        }

        @Override
        protected List<DialogWrapper> getItems() {
            long timeBegin = System.currentTimeMillis();
            Log.d(TAG, "LOADTIME START get dialogs from base and wrap!");

            Log.d(TAG, "get dialogs startRow= " + startRow + ", perPage= " + perPage + ", loadAll= " + loadAll);

            List<QBChatDialog> chatDialogs = loadAll ? dataManager.getQBChatDialogDataManager().getAllSorted() :
                    dataManager.getQBChatDialogDataManager().getSkippedSorted(startRow, perPage);

            Log.d(TAG, "List<DialogWrapper> getItems() getSkippedSorted!!! chatDialogs size= " + chatDialogs.size());

            List<DialogWrapper> dialogWrappers = new ArrayList<>(chatDialogs.size());

            for (QBChatDialog chatDialog : chatDialogs) {
                dialogWrappers.add(new DialogWrapper(getContext(), dataManager, chatDialog));
            }


            long timeEnd = System.currentTimeMillis();
            long dif = timeEnd - timeBegin;
            int seconds = (int) (dif / 1000) % 60;
            Log.d(TAG, "LOADTIME END wrap dialogs! seconds= " + seconds);

            checkLoadFinishedFromCache(chatDialogs.size());

            return dialogWrappers;
        }

        private void checkLoadFinishedFromCache(int size) {
            if(size < ConstsCore.CHATS_DIALOGS_PER_PAGE && loadFromCache) {
                loadFromCache = false;
                setLoadCacheFinished(true);
            }
        }

        private void loadAllDialogsFromCacheByPages() {
            boolean needToLoadMore;
            boolean update = true;

            int startRow = 0;
            int perPage = ConstsCore.CHATS_DIALOGS_PER_PAGE;

            long dialogSize = DataManager.getInstance().getQBChatDialogDataManager().getAllSize();
            boolean isCacheEmpty = dialogSize <= 0;
            Log.d(TAG, "loadAllDialogsFromCacheByPages dialogSize = " + dialogSize);
            if(isCacheEmpty) {
                loadDialogsFromREST(getContext(), false);
                return;
            }
            loadFromCache = true;

            do {
                needToLoadMore = dialogSize > ConstsCore.CHATS_DIALOGS_PER_PAGE;

                if(!needToLoadMore){
                    perPage = (int) dialogSize;
                }

                Bundle bundle = new Bundle();
                bundle.putInt(ConstsCore.DIALOGS_START_ROW, startRow);
                bundle.putInt(ConstsCore.DIALOGS_PER_PAGE, perPage);
                bundle.putBoolean(ConstsCore.DIALOGS_NEED_UPDATE, update);
                update = false;
                Log.d(TAG, "loadAllDialogsFromCacheByPages sendLoadPageSuccess startRow= " + startRow + " perPage= " + perPage);

                sendLoadPageSuccess(bundle);
                dialogSize -= perPage;

                startRow += perPage;
            } while (needToLoadMore);
        }

        private void sendLoadPageSuccess(Bundle result) {
            Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION);
            if (null != result) {
                intent.putExtras(result);
            }
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
        }

        @Override
        public void loadData(){
            loadAllDialogsFromCacheByPages();
        }
    }

    private static void loadDialogsFromREST(Context context, boolean updateAll) {
        Log.d(TAG, "QBLoadDialogsCommand.start");
        QBLoadDialogsCommand.start(context, updateAll);
    }


    private class CommonObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            Log.d(TAG, "CommonObserver update " + observable + " data= " + data.toString());
            if (data != null) {
                if (data instanceof Bundle) {
                    String observeKey = ((Bundle) data).getString(BaseManager.EXTRA_OBSERVE_KEY);
                    Log.i(TAG, "CommonObserver update, key="+observeKey);
                    if (observeKey.equals(dataManager.getMessageDataManager().getObserverKey())
                            && (((Bundle) data).getSerializable(BaseManager.EXTRA_OBJECT) instanceof Message)){
                        Message message = getObjFromBundle((Bundle) data);
                        if (message.getDialogOccupant() != null && message.getDialogOccupant().getDialog() != null) {
                            updateDialog(message.getDialogOccupant().getDialog().getDialogId());
                        }
                    }
                    else if (observeKey.equals(dataManager.getQBChatDialogDataManager().getObserverKey())) {
                        Dialog dialog = getObjFromBundle((Bundle) data);
                        if (dialog != null) {
                            updateDialog(dialog.getDialogId());
                        }
                    } else if (observeKey.equals(dataManager.getDialogOccupantDataManager().getObserverKey())) {
                        DialogOccupant dialogOccupant = getObjFromBundle((Bundle) data);
                        if (dialogOccupant != null && dialogOccupant.getDialog() != null) {
                            updateDialog(dialogOccupant.getDialog().getDialogId());
                        }
                    }
                } else if (data.equals(QMUserCacheImpl.OBSERVE_KEY)) {
                    Log.d(TAG, "else if (data.equals(QMUserCacheImpl.OBSERVE_KEY))");
//                    updateDialogsList();
                }
            }
        }
    }

    private <T> T getObjFromBundle(Bundle data){
        return (T)(data).getSerializable(BaseManager.EXTRA_OBJECT);
    }
}