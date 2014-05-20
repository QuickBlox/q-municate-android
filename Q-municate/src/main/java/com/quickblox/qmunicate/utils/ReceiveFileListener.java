package com.quickblox.qmunicate.utils;

import java.io.File;

public interface ReceiveFileListener {

    public void onCachedImageFileReceived(File imageFile);

    public void onAbsolutePathExtFileReceived(String absolutePath);
}