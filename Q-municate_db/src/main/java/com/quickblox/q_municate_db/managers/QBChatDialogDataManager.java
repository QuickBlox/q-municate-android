package com.quickblox.q_municate_db.managers;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.utils.DialogTransformUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observer;

public class QBChatDialogDataManager {
    private final DialogDataManager dialogDataManager;

    public QBChatDialogDataManager(DialogDataManager dialogDataManager) {
        this.dialogDataManager = dialogDataManager;
    }

    public QBChatDialog getByDialogId(String dialogId) {
        QBChatDialog chatDialog = null;
        Dialog dialog = dialogDataManager.getByDialogId(dialogId);

        if (dialog != null) {
            chatDialog = DialogTransformUtils.createQBDialogFromLocalDialog(DataManager.getInstance(), dialog);
        }

        return chatDialog;
    }

    public QBChatDialog getByRoomJid(String roomJid) {
        QBChatDialog chatDialog = null;
        Dialog dialog = dialogDataManager.getByRoomJid(roomJid);

        if (dialog != null) {
            chatDialog = DialogTransformUtils.createQBDialogFromLocalDialog(DataManager.getInstance(), dialog);
        }

        return chatDialog;
    }

    public void createOrUpdate(QBChatDialog chatDialog){
        Dialog dialog = DialogTransformUtils.createLocalDialog(chatDialog);
        dialogDataManager.createOrUpdate(dialog);
    }

    public void update(QBChatDialog chatDialog){
        Dialog dialog = DialogTransformUtils.createLocalDialog(chatDialog);
        dialogDataManager.update(dialog);
    }

    public void update(QBChatDialog chatDialog, boolean notify) {
        Dialog dialog = DialogTransformUtils.createLocalDialog(chatDialog);
        dialogDataManager.update(dialog, notify);
    }

    public void createOrUpdateAll(List<QBChatDialog> qbChatDialogsList) {
        List<Dialog> dialogsList = new ArrayList<>(qbChatDialogsList.size());
        for (QBChatDialog chatDialog : qbChatDialogsList){
            dialogsList.add(DialogTransformUtils.createLocalDialog(chatDialog));
        }

        dialogDataManager.createOrUpdateAll(dialogsList);
    }

    public void deleteById(String dialogId) {
        dialogDataManager.deleteById(dialogId);
    }

    public List<QBChatDialog> getAllSorted() {
        List<Dialog> dialogsList = dialogDataManager.getAllSorted();
        List<QBChatDialog> chatDialogList = new ArrayList<>();

        if (!CollectionsUtil.isEmpty(dialogsList)) {
            for (Dialog dialog : dialogsList) {
                chatDialogList.add(DialogTransformUtils.createQBDialogFromLocalDialog(DataManager.getInstance(), dialog));
            }
        }

        return chatDialogList;
    }

    public List<QBChatDialog> getAll() {
        List<Dialog> dialogsList = dialogDataManager.getAll();
        List<QBChatDialog> chatDialogList = new ArrayList<>();

        if (!CollectionsUtil.isEmpty(dialogsList)) {
            for (Dialog dialog : dialogsList) {
                chatDialogList.add(DialogTransformUtils.createQBDialogFromLocalDialog(DataManager.getInstance(), dialog));
            }
        }

        return chatDialogList;
    }

    public void addObserver(Observer observer) {
        dialogDataManager.addObserver(observer);
    }

    public void deleteObserver(Observer observer) {
        dialogDataManager.deleteObserver(observer);
    }

    public String getObserverKey() {
        return dialogDataManager.getObserverKey();
    }



}
