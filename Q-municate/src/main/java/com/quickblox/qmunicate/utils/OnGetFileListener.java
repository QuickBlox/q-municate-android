package com.quickblox.qmunicate.utils;

import java.io.File;

public interface OnGetFileListener {

    public void onGotCachedFile(File imageFile);

    public void onGotAbsolutePathCreatedFile(String absolutePath);
}