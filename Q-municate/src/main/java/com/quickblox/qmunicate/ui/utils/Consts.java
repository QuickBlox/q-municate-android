package com.quickblox.qmunicate.ui.utils;

public class Consts {
    public static final int ZERO_VALUE = 0;

    public static final int NOT_INITIALIZED_VALUE = -1;
    public static final int SECOND = 1000;
    public static final String FRIEND_CLASS_NAME = "Friend";
    public static final String FRIEND_FIELD_USER_ID = "user_id";
    public static final String FRIEND_FIELD_FRIEND_ID = "FriendID";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    public static final String USER = "user";
    public static final String USER_NAME = "user_name";
    public static final String CALL_DIRECTION_TYPE_EXTRA = "call_type_direction";
    public static final String CALL_TYPE_EXTRA = "call_type";
    public static final String REMOTE_DESCRIPTION = "session_description";
    public static final String GCM_SENDER_ID = "291727224931";
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String ACCEPTED_CALL = "accepted_call";

    // Facebook
    public static final String FB_WALL_PARAM_NAME = "name";
    public static final String FB_WALL_PARAM_DESCRIPTION = "description";
    public static final String FB_WALL_PARAM_LINK = "link";
    public static final String FB_WALL_PARAM_PICTURE = "picture";
    public static final String FB_WALL_PARAM_PLACE = "place";
    public static final String FB_WALL_PARAM_TAGS = "tags";
    public static final String FB_WALL_PARAM_FEED = "me/feed";
    public static final String INVITE_TYPE_OF_EMAIL = "message/rfc822";

    public static String GSM_SENDER = "761750217637";
    public static String SENDER_ID = "291727224931";

    // Universal Image Loader
    public static final int UIL_MAX_BITMAP_SIZE = 2000000;
    public static DisplayImageOptions defaultDisplayOptions = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .cacheOnDisc(true)
            .cacheInMemory(true)
            .build();
    public static DisplayImageOptions avatarDisplayOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.placeholder_user)
            .showImageForEmptyUri(R.drawable.placeholder_user)
            .showImageOnFail(R.drawable.placeholder_user)
            .build();
}    public static enum CALL_DIRECTION_TYPE {
        INCOMING, OUTGOING
    }
}
