package com.quickblox.q_municate_core.models;


import com.quickblox.chat.model.QBAttachment;

import java.io.Serializable;

public class QBAttachmentExtended extends QBAttachment implements Serializable {

    public QBAttachmentExtended(String type) {
        super(type);
    }
}