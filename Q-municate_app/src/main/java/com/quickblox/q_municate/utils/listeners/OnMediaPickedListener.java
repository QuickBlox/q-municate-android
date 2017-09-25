package com.quickblox.q_municate.utils.listeners;

import com.quickblox.q_municate_db.models.Attachment;

public interface OnMediaPickedListener {

    void onMediaPicked(int requestCode, Attachment.Type attachmentType, Object attachment);

    void onMediaPickError(int requestCode, Exception e);

    void onMediaPickClosed(int requestCode);
}