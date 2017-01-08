package com.quickblox.q_municate.utils.listeners;

import android.os.Bundle;

import java.io.File;

public interface OnImagePickedListener {

    void onImagePicked(int requestCode, File file, String url);

    void onImagePickError(int requestCode, Exception e);

    void onImagePickClosed(int requestCode);
}