package com.quickblox.q_municate_core.core.communication;

import android.os.Parcel;
import android.os.Parcelable;

import org.webrtc.SessionDescription;

public class SessionDescriptionWrapper implements Parcelable {

    private SessionDescription sessionDescription;

    public SessionDescriptionWrapper(SessionDescription sessionDescription) {
        this.sessionDescription = sessionDescription;
    }


    public SessionDescription getSessionDescription() {
        return sessionDescription;
    }

    public SessionDescriptionWrapper(Parcel in) {
        SessionDescription.Type type = SessionDescription.Type.fromCanonicalForm(in.readString());
        String description = in.readString();
        sessionDescription = new SessionDescription(type, description);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(sessionDescription.type.canonicalForm());
        parcel.writeString(sessionDescription.description);
    }

    public static final Parcelable.Creator<SessionDescriptionWrapper> CREATOR = new Parcelable.Creator<SessionDescriptionWrapper>() {
        public SessionDescriptionWrapper createFromParcel(Parcel in) {
            return new SessionDescriptionWrapper(in);
        }

        public SessionDescriptionWrapper[] newArray(int size) {
            return new SessionDescriptionWrapper[size];
        }
    };
}
