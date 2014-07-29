package com.quickblox.q_municate.caching.tables;

import android.net.Uri;

import com.quickblox.q_municate.caching.ContentDescriptor;

public class FriendTable {

    public static final String TABLE_NAME = "friend";
    public static final String PATH = "friend";
    public static final int PATH_TOKEN = 10;
    public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();

    public static class Cols {

        public static final String ID = "_id";
        public static final String FULLNAME = "fullname";
        public static final String EMAIL = "email";
        public static final String PHONE = "phone";
        public static final String FILE_ID = "fileId";
        public static final String AVATAR_UID = "avatarUid";
        public static final String STATUS = "status";
        public static final String ONLINE = "online";
        public static final String TYPE = "type";
    }
}