package com.quickblox.qmunicate.caching.tables;

import android.net.Uri;

import com.quickblox.qmunicate.caching.ContentDescriptor;

public class DialogTable {

    public static final String TABLE_NAME = "dialog";
    public static final String PATH = "dialog";
    public static final int PATH_TOKEN = 30;
    public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();

    public static class Cols {

        public static final String ID = "_id";
        public static final String DIALOG_ID = "dialog_id";
        public static final String ROOM_JID_ID = "room_jid_id";
        public static final String NAME = "name";
        public static final String COUNT_UNREAD_MESSAGES = "count_unread_messages";
        public static final String LAST_MESSAGE = "last_message";
        public static final String LAST_DATE_SENT = "last_date_sent";
        public static final String OCCUPANTS_IDS = "occupants_ids";
        public static final String TYPE = "type";
    }
}