package com.quickblox.qmunicate.ui.utils;

import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;

public class UriCreator {

    private static final String SERVER_ADDRESS = App.getInstance().getResources().getString(
            R.string.api_quickblox_domain);
    private static final String TEMPLATE = "http://%s/blobs/%s.xml?token=%s";
    private static final String HTTP = "http://";

    public static String getUri(String uid) throws BaseServiceException {
        if (uid == null) {
            return null;
        }
        return String.format(TEMPLATE, SERVER_ADDRESS, uid, QBAuth.getBaseService().getToken());
    }

    public static String cutUid(String website) {
        if (website == null) {
            return null;
        }
        return website.replace(HTTP, "");
    }
}
