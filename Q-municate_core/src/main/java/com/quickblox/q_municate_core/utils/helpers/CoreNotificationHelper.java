package com.quickblox.q_municate_core.utils.helpers;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;

import java.util.List;

public class CoreNotificationHelper {

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