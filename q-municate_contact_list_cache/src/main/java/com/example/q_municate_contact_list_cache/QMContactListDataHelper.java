package com.example.q_municate_contact_list_cache;

import android.content.Context;

import com.quickblox.chat.model.QBContactEntry;
import com.quickblox.q_municate_base_cache.QMBaseDataHelper;

public class QMContactListDataHelper extends QMBaseDataHelper {

    private static final Class<?>[] TABLES = {
            QBContactEntry.class
    };

    public QMContactListDataHelper(Context context) {
        super(context, context.getString(R.string.db_name), context.getResources().getInteger(R.integer.db_version), R.raw.orm);
    }

    @Override
    protected Class<?>[] getTables() {
        return TABLES;
    }
}