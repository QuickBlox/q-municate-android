package com.quickblox.qmunicate.utils;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.quickblox.qmunicate.R;

public class Consts {

    // QuickBlox
    public static final String QB_APP_ID = "7232";
    public static final String QB_AUTH_KEY = "MpOecRZy-5WsFva";
    public static final String QB_AUTH_SECRET = "dTSLaxDsFKqegD7";
    public static final String QB_DOMAIN = "api.stage.quickblox.com";

    // Friend list consts
    public static final int FL_START_LOAD_DELAY = 0;
    public static final int FL_UPDATE_DATA_PERIOD = 30000;
    public static final int FL_ONLINE_STATUS_TIMEOUT = 15;
    public static final int FL_FRIENDS_PAGE_NUM = 1;
    public static final int FL_FRIENDS_PER_PAGE = 100;

    public static final int CHATS_DIALOGS_PER_PAGE = 100;
    public static final int DIALOG_MESSAGES_PER_PAGE = 100;

    public static final int ZERO_VALUE = 0;
    public static final String EMPTY_STRING = "";

    public static final int LEFT_CHAT_MESSAGE_TYPE_1 = 0;
    public static final int RIGHT_CHAT_MESSAGE_TYPE_2 = 1;
    public static final int MESSAGES_TYPE_COUNT = 2;

    public static final String ENCODING_UTF8 = "UTF-8";

    public static final int DELAY_LONG_CLICK_ANIMATION_LONG = 500;
    public static final int DELAY_LONG_CLICK_ANIMATION_SHORT = 300;

    public static final int CIRCL_BORDER_WIDTH = 4;
    public static final float CIRCL_SHADOW_RADIUS = 4.0f;
    public static final float CIRCL_SHADOW_DX = 0.0f;
    public static final float CIRCL_SHADOW_DY = 2.0f;

    public static final String FRIEND_CLASS_NAME = "Friend";
    public static final String FRIEND_FIELD_USER_ID = "user_id";
    public static final String FRIEND_FIELD_FRIEND_ID = "FriendID";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    public static final String USER = "user";
    public static final String USER_NAME = "user_name";
    public static final String CALL_DIRECTION_TYPE_EXTRA = "call_type_direction";
    public static final String CALL_TYPE_EXTRA = "call_type";
    public static final String REMOTE_DESCRIPTION = "session_description";
    public static final String GCM_SENDER_ID = "265299067289";

    // TODO VF rename and move these consts to PrefsHelper
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String ACCEPTED_CALL = "accepted_call";
    public static final int NOT_INITIALIZED_VALUE = -1;
    public static final int AVATAR_BITMAP_SIZE = 70;
    public static final int SECOND = 1000;
    public static final String DEFAULT_WEB_ROOM = "test";

    public static enum CALL_DIRECTION_TYPE {
        INCOMING, OUTGOING
    }

    //Sound
    public static final String ASSETS_SOUND_PATH = "sound/";

    // Facebook
    public static final String FB_WALL_PARAM_NAME = "name";
    public static final String FB_WALL_PARAM_DESCRIPTION = "description";
    public static final String FB_WALL_PARAM_LINK = "link";
    public static final String FB_WALL_PARAM_PICTURE = "picture";
    public static final String FB_WALL_PARAM_PLACE = "place";
    public static final String FB_WALL_PARAM_TAGS = "tags";
    public static final String FB_WALL_PARAM_FEED = "me/feed";
    public static final String INVITE_TYPE_OF_EMAIL = "message/rfc822";

    // Universal Image Loader
    public static final DisplayImageOptions UIL_DEFAULT_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Bitmap.Config.RGB_565)
            .cacheOnDisc(true).cacheInMemory(true).build();
    public static DisplayImageOptions UIL_AVATAR_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.placeholder_user).showImageForEmptyUri(R.drawable.placeholder_user)
            .showImageOnFail(R.drawable.placeholder_user).cacheOnDisc(true).cacheInMemory(true).build();
}
