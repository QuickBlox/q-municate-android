package com.quickblox.q_municate_user_service.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.quickblox.q_municate_base_cache.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUserCustomData;

/**
 * Created by pelipets on 1/10/17.
 */

public class Utils {

    public static QMUserCustomData customDataToObject(String userCustomDataString) {
        if (TextUtils.isEmpty(userCustomDataString)) {
            return new QMUserCustomData();
        }

        QMUserCustomData userCustomData = null;
        GsonBuilder gsonBuilder = new GsonBuilder();

        Gson gson = gsonBuilder.create();

        try {
            userCustomData = gson.fromJson(userCustomDataString, QMUserCustomData.class);
        } catch (JsonSyntaxException e) {
            ErrorUtils.logError(e);
        }

        return userCustomData;
    }
}
