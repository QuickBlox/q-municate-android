package com.quickblox.q_municate.utils;

import java.io.File;

public class FileUtils {

    public static void removeFile(String filePath) {
        File file = new File(filePath);
        file.deleteOnExit();
    }
}