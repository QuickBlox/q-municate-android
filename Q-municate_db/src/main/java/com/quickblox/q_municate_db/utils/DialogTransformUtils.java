package com.quickblox.q_municate_db.utils;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class DialogTransformUtils {

    public static QBChatDialog createQBDialogFromLocalDialog(DataManager dataManager, Dialog dialog) {
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        QBChatDialog qbDialog = createQBDialogFromLocalDialog(dialog, dialogOccupantsList);
        return qbDialog;
    }

    private static QBChatDialog createQBDialogFromLocalDialog(Dialog dialog, List<DialogOccupant> dialogOccupantsList) {
        QBChatDialog qbDialog = new QBChatDialog();
        qbDialog.setDialogId(dialog.getDialogId());
        qbDialog.setRoomJid(dialog.getRoomJid());
        qbDialog.setPhoto(dialog.getPhoto());
        qbDialog.setName(dialog.getTitle());
        qbDialog.setOccupantsIds(createOccupantsIdsFromDialogOccupantsList(dialogOccupantsList));
        qbDialog.setType(
                Dialog.Type.PRIVATE.equals(dialog.getType()) ? QBDialogType.PRIVATE : QBDialogType.GROUP);

        qbDialog.setUpdatedAt(new Date(dialog.getModifiedDateLocal()));
        return qbDialog;
    }

    public static ArrayList<Integer> createOccupantsIdsFromDialogOccupantsList(
            List<DialogOccupant> dialogOccupantsList) {
        ArrayList<Integer> occupantsIdsList = new ArrayList<>(dialogOccupantsList.size());
        for (DialogOccupant dialogOccupant : dialogOccupantsList) {
            occupantsIdsList.add(dialogOccupant.getUser().getId());
        }
        return occupantsIdsList;
    }

    public static Dialog createLocalDialog(QBChatDialog qbDialog) {
        Dialog dialog = new Dialog();
        dialog.setDialogId(qbDialog.getDialogId());
        dialog.setRoomJid(qbDialog.getRoomJid());
        dialog.setTitle(qbDialog.getName());
        dialog.setPhoto(qbDialog.getPhoto());
        if (qbDialog.getUpdatedAt() != null) {
            dialog.setUpdatedAt(qbDialog.getUpdatedAt().getTime()/1000);
        }
        dialog.setModifiedDateLocal(qbDialog.getLastMessageDateSent());

        if (QBDialogType.PRIVATE.equals(qbDialog.getType())) {
            dialog.setType(Dialog.Type.PRIVATE);
        } else if (QBDialogType.GROUP.equals(qbDialog.getType())) {
            dialog.setType(Dialog.Type.GROUP);
        }

        return dialog;
    }

    public static List<Dialog> getListLocalDialogsFromQBDialogs(Collection<QBChatDialog> chatDialogs){
        List<Dialog> dialogsList = new ArrayList<>(chatDialogs.size());
        for (QBChatDialog chatDialog : chatDialogs){
            dialogsList.add(DialogTransformUtils.createLocalDialog(chatDialog));
        }

        return dialogsList;
    }

    public static List<QBChatDialog> getListQBDialogsFromLocalDialogs(Collection<Dialog> dialogsList) {
        List<QBChatDialog> chatDialogList = new ArrayList<>();

        if (!CollectionsUtil.isEmpty(dialogsList)) {
            for (Dialog dialog : dialogsList) {
                chatDialogList.add(DialogTransformUtils.createQBDialogFromLocalDialog(DataManager.getInstance(), dialog));
            }
        }

        return chatDialogList;
    }
}
