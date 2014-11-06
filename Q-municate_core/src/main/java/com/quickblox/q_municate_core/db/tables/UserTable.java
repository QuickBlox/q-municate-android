package com.quickblox.q_municate_core.db.tables;

import android.net.Uri;

import com.quickblox.q_municate_core.db.ContentDescriptor;

public class UserTable {

    public static final String TABLE_NAME = "user";

    public static final String PATH = "user_table";
    public static final String USER_FRIEND_PATH = "user_friend_tables";

    public static final int PATH_TOKEN = 10;
    public static final int USER_FRIEND_PATH_TOKEN = 11;

    public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();
    public static final Uri USER_FRIEND_CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(USER_FRIEND_PATH).build();

    public static class Cols {

        public static final String ID = "_id";
        public static final String USER_ID = "user_id";
        public static final String FULL_NAME = "full_name";
        public static final String EMAIL = "email";
        public static final String LOGIN = "login";
        public static final String PHONE = "phone";
        public static final String WEB_SITE = "web_site";
        public static final String CUSTOM_DATA = "custom_data";
        public static final String LAST_REQUEST_AT = "last_request_at";
        public static final String EXTERNAL_ID = "external_id";
        public static final String FACEBOOK_ID = "facebook_id";
        public static final String TWITTER_ID = "twitter_id";
        public static final String BLOB_ID = "blob_id";
        public static final String AVATAR_URL = "avatar_url";
        public static final String STATUS = "status";
        public static final String IS_ONLINE = "online";
    }
}