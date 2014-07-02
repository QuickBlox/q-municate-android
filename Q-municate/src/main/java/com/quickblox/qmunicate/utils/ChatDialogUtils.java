package com.quickblox.qmunicate.utils;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.model.ParcelableQBDialog;

import java.util.ArrayList;
import java.util.List;

public class ChatDialogUtils {

    public static ArrayList<ParcelableQBDialog> dialogsToParcelableDialogs(List<QBDialog> dialogs){
        ArrayList<ParcelableQBDialog> parcelableQBDialogs = new ArrayList<ParcelableQBDialog>(dialogs.size());
        for (QBDialog dialog : dialogs) {
            ParcelableQBDialog parcelableQBDialog = new ParcelableQBDialog(dialog);
            parcelableQBDialogs.add(parcelableQBDialog);
        }
        return parcelableQBDialogs;
    }

    public static ArrayList<QBDialog> parcelableDialogsToDialogs(List<ParcelableQBDialog> parcelableDialogs){
        ArrayList<QBDialog> dialogs = new ArrayList<QBDialog>(parcelableDialogs.size());
        for (ParcelableQBDialog parcelableDialog : parcelableDialogs) {
            dialogs.add(parcelableDialog.getDialog());
        }
        return dialogs;
    }
}
