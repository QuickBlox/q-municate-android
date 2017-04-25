package com.quickblox.q_municate.loaders;

import android.content.Context;
import android.util.Log;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.utils.DialogsUtils;
import com.quickblox.q_municate_core.core.loader.BaseLoader;
import com.quickblox.q_municate_core.models.DialogWrapper;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoadDialogsCommand;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by roman on 4/25/17.
 */

public class DialogsListLoader extends BaseLoader<List<DialogWrapper>> {
    private static final String TAG = DialogsListLoader.class.getSimpleName();

    private boolean loadAll;
    private boolean needUpdate;

    private boolean loadCacheFinished;
    private boolean loadFromCache;

    private int startRow = 0;
    private int perPage = 0;

    public DialogsListLoader(Context context, DataManager dataManager) {
        super(context, dataManager);
    }

    public boolean isNeedUpdate() {
        return needUpdate;
    }

    public boolean isLoadCacheFinished() {
        return loadCacheFinished;
    }

    public void setLoadAll(boolean loadAll) {
        this.loadAll = loadAll;
        if(loadAll) {
            needUpdate = true;
        }
    }

    public void setPagination(int startRow, int perPage, boolean update) {
        this.needUpdate = update;
        this.startRow = startRow;
        this.perPage = perPage;
    }

    @Override
    protected List<DialogWrapper> getItems() {
        Log.d(TAG, "getItems() chatDialogs startRow= " + startRow + ", perPage= " + perPage + ", loadAll= " + loadAll);

        List<QBChatDialog> chatDialogs = loadAll ? dataManager.getQBChatDialogDataManager().getAllSorted() :
                dataManager.getQBChatDialogDataManager().getSkippedSorted(startRow, perPage);

        Log.d(TAG, "getItems() chatDialogs size= " + chatDialogs.size());

        List<DialogWrapper> dialogWrappers = new ArrayList<>(chatDialogs.size());
        for (QBChatDialog chatDialog : chatDialogs) {
            dialogWrappers.add(new DialogWrapper(getContext(), dataManager, chatDialog));
        }

        checkLoadFinishedFromCache(chatDialogs.size());

        return dialogWrappers;
    }

    private void checkLoadFinishedFromCache(int size) {
        if(size < ConstsCore.CHATS_DIALOGS_PER_PAGE && loadFromCache) {
            loadFromCache = false;
            loadCacheFinished = true;
        } else {
            loadCacheFinished = false;
        }
    }

    private void retrieveAllDialogsFromCacheByPages() {
        loadCacheFinished = false;
        long dialogsCount = DataManager.getInstance().getQBChatDialogDataManager().getAllCount();
        boolean isCacheEmpty = dialogsCount <= 0;

        if(isCacheEmpty) {
            QBLoadDialogsCommand.start(getContext(), false);
            return;
        }
        loadFromCache = true;
        DialogsUtils.loadAllDialogsFromCacheByPagesTask(getContext(), dialogsCount);
    }

    @Override
    public void loadData(){
        retrieveAllDialogsFromCacheByPages();
    }
}