package com.quickblox.q_municate_user_cache;

import android.content.Context;

import com.quickblox.q_municate_base_cache.QMBaseDataHelper;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

public class QMUserDataHelper extends QMBaseDataHelper {

    private static final Class<?>[] TABLES = {
            QMUser.class
    };

    public QMUserDataHelper(Context context) {
        super(context, context.getString(R.string.db_name), context.getResources().getInteger(R.integer.db_version), R.raw.orm);
    }

    @Override
    protected Class<?>[] getTables() {
        return TABLES;
    }
}
