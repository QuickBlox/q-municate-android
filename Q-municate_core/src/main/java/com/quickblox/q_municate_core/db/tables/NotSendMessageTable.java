package com.quickblox.q_municate_core.db.tables;

import android.net.Uri;

import com.quickblox.q_municate_core.db.ContentDescriptor;

/**
 * Created on 6/23/15.
 *
 * Table for storing messages by dialogs ID's that was not sent.
 * Table should contain one message for one dialog.
 *
 * When user logout via NavigationDrawerFragment.logout() method table should be clear.
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public class NotSendMessageTable {
    public static final String TABLE_NAME = "not_send_message";
    public static final String PATH = "not_send_message_table";
    public static final int PATH_TOKEN = 60;
    public static final Uri CONTENT_URI = ContentDescriptor.BASE_URI.buildUpon().appendPath(PATH).build();

    public static class Cols {

        public static final String ID = "_id";
        public static final String DIALOG_ID = "dialog_id";
        public static final String BODY = "body";
        public static final String ATTACH_FILE_ID = "attach_file_id";
    }
}
