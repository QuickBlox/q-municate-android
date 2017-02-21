package com.quickblox.q_municate.utils;

import android.content.Context;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_db.utils.ErrorUtils;

/**
 * Created by pelipets on 2/9/17.
 */

public class AuthUtils {


    public static void parseExceptionMessage(Context context, String errorMessage) {
        if (errorMessage != null) {
            if (errorMessage.equals(context.getString(R.string.error_bad_timestamp))) {
                errorMessage = context.getString(R.string.error_bad_timestamp_from_app);
            } else if (errorMessage.equals(context.getString(R.string.error_login_or_email_required))) {
                errorMessage = context.getString(R.string.error_login_or_email_required_from_app);
            } else if (errorMessage.equals(context.getString(R.string.error_email_already_taken))
                    && AppSession.getSession().getLoginType().equals(LoginType.FACEBOOK)) {
                errorMessage = context.getString(R.string.error_email_already_taken_from_app);
            } else if (errorMessage.equals(context.getString(R.string.error_unauthorized))) {
                errorMessage = context.getString(R.string.error_unauthorized_from_app);
            }

            ErrorUtils.showError(context, errorMessage);
        }
    }
}
