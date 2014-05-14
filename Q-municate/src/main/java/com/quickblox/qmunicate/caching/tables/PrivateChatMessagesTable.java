package com.quickblox.qmunicate.caching.tables;


import android.net.Uri;

import com.quickblox.qmunicate.caching.ContentDescriptor;

public class PrivateChatMessagesTable {

    public static final String TABLE_NAME = "private_chat_messages";
    public static final String PATH = "private_chat_messages";
    public static final int PATH_TOKEN = 20;

    public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();

    public static class Cols {

        public static final String ID = "_id";
        public static final String BODY = "body";
        public static final String SENDER_ID = "sender_id";
        public static final String TIME = "time";
        public static final String INCOMING = "incoming";
        public static final String CHAT_ID = "chat_id";
        public static final String ATTACH_FILE_URL = "attach_file_url";
    }
}