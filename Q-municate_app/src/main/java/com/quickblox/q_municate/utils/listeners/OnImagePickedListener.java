package com.quickblox.q_municate.utils.listeners;

import com.quickblox.q_municate_db.models.Attachment;

public interface OnImagePickedListener {

    void onImagePicked(int requestCode, Attachment.Type attachmentType, Object attachment);

    void onImagePickError(int requestCode, Exception e);

    void onImagePickClosed(int requestCode);
}