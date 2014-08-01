package com.quickblox.q_municate.caching;

import android.content.UriMatcher;
import android.net.Uri;

import com.quickblox.q_municate.caching.tables.DialogMessageTable;
import com.quickblox.q_municate.caching.tables.DialogTable;
import com.quickblox.q_municate.caching.tables.FriendTable;

public class ContentDescriptor {

    public static final String AUTHORITY = "com.qmun.quickblox";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    public static final UriMatcher URI_MATCHER = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(AUTHORITY, FriendTable.PATH, FriendTable.PATH_TOKEN);
        matcher.addURI(AUTHORITY, DialogMessageTable.PATH, DialogMessageTable.PATH_TOKEN);
        matcher.addURI(AUTHORITY, DialogTable.PATH, DialogTable.PATH_TOKEN);
        // TODO SF other tables can be added

        return matcher;
    }
}