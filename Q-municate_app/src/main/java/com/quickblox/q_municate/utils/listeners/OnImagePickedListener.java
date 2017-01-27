package com.quickblox.q_municate.utils.listeners;

import java.io.File;

public interface OnImagePickedListener {

    void onImagePicked(int requestCode, File file, String location);

    void onImagePickError(int requestCode, Exception e);

    void onImagePickClosed(int requestCode);
}