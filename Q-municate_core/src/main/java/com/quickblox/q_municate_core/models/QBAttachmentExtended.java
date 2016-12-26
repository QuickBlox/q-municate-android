package com.quickblox.q_municate_core.models;


import com.quickblox.chat.model.QBAttachment;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class QBAttachmentExtended extends QBAttachment implements Serializable {
    public QBAttachmentExtended(String type) {
        super(type);
    }

    private void writeObject(ObjectOutputStream oos)
            throws IOException {
        // default serialization
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois)
            throws ClassNotFoundException, IOException {
        // default deserialization
        ois.defaultReadObject();
    }
}