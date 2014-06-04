package com.quickblox.qmunicate.caching.tables;


import android.net.Uri;

import com.quickblox.qmunicate.caching.ContentDescriptor;

public class DialogMessageTable {

    public static final String TABLE_NAME = "dialog_message";
    public static final String PATH = "dialog_message";
    public static final int PATH_TOKEN = 20;
    public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();

    public static class Cols {

        public static final String ID = "_id";
        public static final String ROOM_JID_ID = "room_jid_id";
        public static final String SENDER_ID = "sender_id";
        public static final String BODY = "body";
        public static final String TIME = "time";
        public static final String ATTACH_FILE_ID = "attach_file_id";
        public static final String IS_READ = "is_read";
    }
}