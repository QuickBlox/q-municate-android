package com.quickblox.q_municate_core.db.tables;

import android.net.Uri;

import com.quickblox.q_municate_core.db.ContentDescriptor;

public class FriendsRelationTable {

    public static final String TABLE_NAME = "friends_relation";
    public static final String PATH = "friends_relation_table";
    public static final int PATH_TOKEN = 30;
    public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();

    public static class Cols {

        public static final String RELATION_STATUS_ID = "relation_status_id";
        public static final String RELATION_STATUS = "relation_status";
    }
}