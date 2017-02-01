package com.quickblox.q_municate_core.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.Utils;

//TODO VF maybe move this logic to Dialog class
public class ParcelableQBDialog implements Parcelable {

    public static final Parcelable.Creator<ParcelableQBDialog> CREATOR = new Parcelable.Creator<ParcelableQBDialog>() {
        public ParcelableQBDialog createFromParcel(Parcel in) {
            return new ParcelableQBDialog(in);
        }

        public ParcelableQBDialog[] newArray(int size) {
            return new ParcelableQBDialog[size];
        }
    };
    private QBChatDialog dialog;

    public ParcelableQBDialog(QBChatDialog dialog) {
        this.dialog = dialog;
    }

    public ParcelableQBDialog(Parcel inputParcel) {
        dialog = new QBChatDialog(inputParcel.readString());
        dialog.setName(inputParcel.readString());
        dialog.setType(QBDialogType.parseByCode(inputParcel.readInt()));
        dialog.setRoomJid(inputParcel.readString());
        dialog.setLastMessage(inputParcel.readString());
        dialog.setLastMessageDateSent(inputParcel.readLong());
        int[] occupantArray = new int[inputParcel.readInt()];
        inputParcel.readIntArray(occupantArray);
        dialog.setOccupantsIds(Utils.toArrayList(occupantArray));
    }

    public QBChatDialog getDialog() {
        return dialog;
    }

    @Override
    public int describeContents() {
        return ConstsCore.NOT_INITIALIZED_VALUE;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(dialog.getDialogId());
        parcel.writeString(dialog.getName());
        parcel.writeInt(dialog.getType().getCode());
        parcel.writeString(dialog.getRoomJid());
        parcel.writeString(dialog.getLastMessage());
        parcel.writeLong(dialog.getLastMessageDateSent());
        int[] occupantArray = Utils.toIntArray(dialog.getOccupants());
        parcel.writeInt(occupantArray.length);
        parcel.writeIntArray(occupantArray);
    }
}