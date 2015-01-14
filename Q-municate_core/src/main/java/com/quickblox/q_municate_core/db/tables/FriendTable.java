package com.quickblox.q_municate_core.db.tables;

import android.net.Uri;

import com.quickblox.q_municate_core.db.ContentDescriptor;

public class FriendTable {

    public static final String TABLE_NAME = "friend";
    public static final String PATH = "friend_table";
    public static final int PATH_TOKEN = 20;
    public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();

    public static class Cols {

        public static final String ID = "_id";
        public static final String USER_ID = "user_id";
        public static final String RELATION_STATUS_ID = "relation_status_id";
        public static final String IS_PENDING_STATUS = "is_pending_status";
        public static final String IS_NEW_FRIEND_STATUS = "is_new_friend_status";
    }
}