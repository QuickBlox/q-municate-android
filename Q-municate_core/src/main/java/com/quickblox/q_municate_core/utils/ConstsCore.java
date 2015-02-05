package com.quickblox.q_municate_core.utils;

public class ConstsCore {

    // Friend list consts
    public static final int FL_FRIENDS_PAGE_NUM = 1;
    public static final int FL_FRIENDS_PER_PAGE = 20;

    public static final int CHATS_DIALOGS_PER_PAGE = 100;
    public static final int DIALOG_MESSAGES_PER_PAGE = 100;

    public static final int ZERO_INT_VALUE = 0;
    public static final long ZERO_LONG_VALUE = 0L;
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";

    public static final int SECOND = 1000;

    public static final int DEFAULT_PACKET_REPLY_TIMEOUT = 15 * 1000;
    public static final int DEFAULT_CALL_PACKET_REPLY_TIMEOUT = 5 * SECOND;
    public static final int DEFAULT_DIALING_TIME = 35 * SECOND;
    public static final long DEFAULT_CLEAR_SESSION_TIMEOUT_SECONDS = 10;

    public static final int OPPONENT_DIALOG_MESSAGE_TYPE = 0;
    public static final int OWN_DIALOG_MESSAGE_TYPE = 1;
    public static final int MESSAGES_TYPE_COUNT = 2;

    public static final String ENCODING_UTF8 = "UTF-8";

    public static final int DELAY_LONG_CLICK_ANIMATION_LONG = 500;
    public static final int DELAY_LONG_CLICK_ANIMATION_SHORT = 300;

    public static final int CIRCL_BORDER_WIDTH = 4;
    public static final float CIRCL_SHADOW_RADIUS = 4.0f;
    public static final float CIRCL_SHADOW_DX = 0.0f;
    public static final float CIRCL_SHADOW_DY = 2.0f;

    public static final String EXTRA_FRIEND = "Friend";
    public static final String FRIEND_FIELD_USER_ID = "user_id";
    public static final String FRIEND_FIELD_FRIEND_ID = "FriendID";
    public static final String PUSH_MESSAGE = "message";
    public static final String PROPERTY_REG_USER_ID = "registered_push_user";
    public static final String USER = "user";
    public static final String USER_NAME = "user_name";
    public static final String CALL_DIRECTION_TYPE_EXTRA = "call_type_direction";
    public static final String CALL_TYPE_EXTRA = "call_type";
    public static final String REMOTE_DESCRIPTION = "session_description";

    public static final int CHAT_ATTACH_WIDTH = 200;
    public static final int CHAT_ATTACH_HEIGHT = 200;

    // TODO VF rename and move these consts to PrefsHelper
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String ACCEPTED_CALL = "accepted_call";
    public static final int NOT_INITIALIZED_VALUE = -1;
    public static final int LOGIN_TIMEOUT = 40000;
    public static final String DEFAULT_CALL_MESSAGE = "";
    public static final String TOKEN_REQUIRED_ERROR = "Token is required";
    public static final String SESSION_DOES_NOT_EXIST = "Required session does not exist";
    public static final int FULL_QUALITY = 100;
    public static final int TOKEN_VALID_TIME_IN_MINUTES = 1;

    public static enum CALL_DIRECTION_TYPE {
        INCOMING, OUTGOING
    }

    //Sound
    public static final String ASSETS_SOUND_PATH = "sound/";

    // Facebook Wall
    public static final String FB_WALL_PARAM_NAME = "name";
    public static final String FB_WALL_PARAM_DESCRIPTION = "description";
    public static final String FB_WALL_PARAM_LINK = "link";
    public static final String FB_WALL_PARAM_PICTURE = "picture";
    public static final String FB_WALL_PARAM_PLACE = "place";
    public static final String FB_WALL_PARAM_TAGS = "tags";
    public static final String FB_WALL_PARAM_FEED = "me/feed";

    // Facebook Request
    public static final String FB_REQUEST_PARAM_MESSAGE = "message";
    public static final String FB_REQUEST_PARAM_TITLE = "title";

    public static final String TYPE_OF_EMAIL = "message/rfc822";
}
