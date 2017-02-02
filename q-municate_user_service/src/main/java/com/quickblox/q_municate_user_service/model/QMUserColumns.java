package com.quickblox.q_municate_user_service.model;

import com.quickblox.q_municate_base_cache.model.QMBaseColumns;

public interface QMUserColumns extends QMBaseColumns{

    String TABLE_NAME = "qm_user";
    String ID = "id";
    String FULL_NAME = "full_name";
    String EMAIL = "email";
    String LOGIN = "login";
    String PHONE = "phone";
    String WEBSITE = "website";
    String LAST_REQUEST_AT = "last_request_at";
    String EXTERNAL_ID = "external_user_id";
    String FACEBOOK_ID = "facebook_id";
    String TWITTER_ID = "twitter_id";
    String TWITTER_DIGITS_ID = "twitter_digits_id";
    String BLOB_ID = "blob_id";
    String TAGS = "user_tags";
    String PASSWORD = "password";
    String OLD_PASSWORD = "old_password";
    String CUSTOM_DATE = "custom_date";
    String AVATAR = "avatar";
    String STATUS = "status";
}
