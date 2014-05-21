package com.quickblox.qmunicate.caching.tables;


import android.net.Uri;
import com.quickblox.qmunicate.caching.ContentDescriptor;

public class ChatTable {

    public static final String TABLE_NAME = "chats";
    public static final String PATH = "chats";
    public static final int PATH_TOKEN = 30;

    public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();

    public static class Cols {

        public static final String ID = "_id";
        public static final String CHAT_ID = "chat_id";
        public static final String CHAT_NAME = "chat_name";
        public static final String LAST_MESSAGE = "last_message";
        public static final String FILE_ID = "file_id";
        public static final String IS_GROUP = "is_group";
        public static final String MEMBERS_IDS = "members_ids";
        public static final String AVATAR_ID = "avatar_id";
    }
}