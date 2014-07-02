package com.quickblox.qmunicate.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
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
        return 0;
    }

    public ParcelableQBDialog(Parcel in) {
        dialog = new QBDialog(in.readString());
        dialog.setName(in.readString());
        dialog.setType(QBDialogType.parseByCode(in.readInt()));
        dialog.setRoomJid(in.readString());
        dialog.setLastMessage(in.readString());
        dialog.setLastMessageDateSent(in.readLong());
        int[] occupants = new int[in.readInt()];
        in.readIntArray(occupants);
        dialog.setOccupantsIds(Utils.toArrayList(occupants));
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(dialog.getDialogId());
        parcel.writeString(dialog.getName());
        parcel.writeInt(dialog.getType().getCode());
        parcel.writeString(dialog.getRoomJid());
        parcel.writeString(dialog.getLastMessage());
        parcel.writeLong(dialog.getLastMessageDateSent());
        int[] occupnats = Utils.toIntArray(dialog.getOccupants());
        parcel.writeInt(occupnats.length);
        parcel.writeIntArray(occupnats);
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
