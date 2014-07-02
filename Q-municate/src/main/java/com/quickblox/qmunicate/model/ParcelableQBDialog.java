package com.quickblox.qmunicate.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.Utils;

//TODO VF maybe move this logic to Dialog class
public class ParcelableQBDialog implements Parcelable{

    private QBDialog dialog;

    public ParcelableQBDialog(QBDialog dialog){
        this.dialog = dialog;
    }

    public QBDialog getDialog() {
        return dialog;
    }

    @Override
    public int describeContents() {
        return Consts.NOT_INITIALIZED_VALUE;
    }

    public ParcelableQBDialog(Parcel inputParcel) {
        dialog = new QBDialog(inputParcel.readString());
        dialog.setName(inputParcel.readString());
        dialog.setType(QBDialogType.parseByCode(inputParcel.readInt()));
        dialog.setRoomJid(inputParcel.readString());
        dialog.setLastMessage(inputParcel.readString());
        dialog.setLastMessageDateSent(inputParcel.readLong());
        int[] occupantArray = new int[inputParcel.readInt()];
        inputParcel.readIntArray(occupantArray);
        dialog.setOccupantsIds(Utils.toArrayList(occupantArray));
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

    public static final Parcelable.Creator<ParcelableQBDialog> CREATOR = new Parcelable.Creator<ParcelableQBDialog>() {
        public ParcelableQBDialog createFromParcel(Parcel in) {
            return new ParcelableQBDialog(in);
        }

        public ParcelableQBDialog[] newArray(int size) {
            return new ParcelableQBDialog[size];
        }
    };
}
