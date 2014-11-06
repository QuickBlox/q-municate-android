package com.quickblox.q_municate_core.utils;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;

import java.util.ArrayList;
import java.util.List;

public class ChatDialogUtils {

    public static ArrayList<ParcelableQBDialog> dialogsToParcelableDialogs(List<QBDialog> dialogList){
        ArrayList<ParcelableQBDialog> parcelableDialogList = new ArrayList<ParcelableQBDialog>(dialogList.size());
        for (QBDialog dialog : dialogList) {
            ParcelableQBDialog parcelableQBDialog = new ParcelableQBDialog(dialog);
            parcelableDialogList.add(parcelableQBDialog);
        }
        return parcelableDialogList;
    }

    public static ArrayList<QBDialog> parcelableDialogsToDialogs(List<ParcelableQBDialog> parcelableDialogList){
        ArrayList<QBDialog> dialogList = new ArrayList<QBDialog>(parcelableDialogList.size());
        for (ParcelableQBDialog parcelableDialog : parcelableDialogList) {
            dialogList.add(parcelableDialog.getDialog());
        }
        return dialogList;
    }
}