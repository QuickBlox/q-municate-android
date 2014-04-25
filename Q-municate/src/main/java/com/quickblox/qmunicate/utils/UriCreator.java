package com.quickblox.qmunicate.utils;

import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;

public class UriCreator {

    private static final String TEMPLATE = "http://%s/blobs/%s.xml?token=%s";
    private static final String HTTP = "http://";

    public static String getUri(String uid) throws BaseServiceException {
        if (uid == null) {
            return null;
        }
        return String.format(TEMPLATE, Consts.QB_DOMAIN, uid, QBAuth.getBaseService().getToken());
    }

    public static String cutUid(String website) {
        if (website == null) {
            return null;
        }
        return website.replace(HTTP, "");
    }
}
