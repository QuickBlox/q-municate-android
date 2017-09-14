package com.quickblox.q_municate_core.utils;

public class ConstsCore {

    // Friend list consts
    public static final int FL_FRIENDS_PAGE_NUM = 1;
    public static final int FL_FRIENDS_PER_PAGE = 20;

    public static final int USERS_PAGE_NUM = 1;
    public static final int USERS_PER_PAGE = 100;

    public static final String CHAT_MUC = "@muc.";
    public static final int CHATS_DIALOGS_PER_PAGE = 50;
    public static final int DIALOG_MESSAGES_PER_PAGE = 50;
    public static final String DIALOGS_START_ROW = "dialogs_start_row";
    public static final String DIALOGS_PER_PAGE = "dialogs_per_page";
    public static final String DIALOGS_UPDATE_ALL = "dialogs_update_all";
    public static final String DIALOGS_UPDATE_BY_IDS = "dialogs_update_by_ids";

    public static final int ZERO_INT_VALUE = 0;
    public static final long ZERO_LONG_VALUE = 0L;
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";

    public static final int SECOND = 1000;

    public static final int DEFAULT_PACKET_REPLY_TIMEOUT = 15 * 1000;

    public static final String PUSH_MESSAGE = "message";

    public static final int NOT_INITIALIZED_VALUE = -1;
    public static final int LOGIN_TIMEOUT = 40000;
    public static final int HTTP_TIMEOUT_IN_SECONDS = 40 * 1000;
    public static final String TOKEN_REQUIRED_ERROR = "Token is required";
    public static final String SESSION_DOES_NOT_EXIST = "Required session does not exist";
    public static final int TOKEN_VALID_TIME_IN_MINUTES = 1;

    //Sound
    public static final String ASSETS_SOUND_PATH = "sound/";

    // Facebook Request
    public static final String FB_REQUEST_PARAM_MESSAGE = "message";
    public static final String FB_REQUEST_PARAM_TITLE = "title";

    public static final String TYPE_OF_EMAIL = "message/rfc822";

    public static final String NOTIFICATION_TYPE = "notificationType";
    public static final String STATE = "state";
    public static final String USER = "user";

    //    Location
    public static final String EXTRA_LOCATION_LATITUDE = "location_latitude";
    public static final String EXTRA_LOCATION_LONGITUDE = "location_longitude";
    public static final String LATITUDE_PARAM = "lat";
    public static final String LONGITUDE_PARAM = "lng";

    public static final int LAST_MESSAGE_LENGTH = 100;

    public static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; //10Mb
    public static final long MAX_AUDIO_VIDEO_SIZE = 100 * 1024 * 1024; //100mb
    public static final long MAX_FILENAME_LENGTH = 100;

    // AudioVideoRecorder
    public static final int MAX_RECORD_DURATION_IN_SEC = 30;
    public static final int MIN_RECORD_DURATION_IN_SEC = 1;
    public static final int VIDEO_QUALITY_LOW = 0;
    public static final int VIDEO_QUALITY_HIGH = 1;
    public static final int CHRONOMETER_ALARM_SECOND = 27;

}