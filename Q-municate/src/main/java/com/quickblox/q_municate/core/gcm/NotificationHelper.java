package com.quickblox.q_municate.core.gcm;

import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationType;

import java.util.List;

public class NotificationHelper {

    public static final String ACTION_VIDEO_CALL = "com.quickblox.qmunicate.VIDEO_CALL";
    public static final String ACTION_AUDIO_CALL = "com.quickblox.qmunicate.AUDIO_CALL";
    public static final String MESSAGE = "message";

    public static final String CALL_TYPE = "call";

    public static QBEvent createPushEvent(List<Integer> userIdsList, String message, String messageType) {
        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        userIds.addAll(userIdsList);
        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.PRODUCTION);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setMessage(message);
        return event;
    }
}