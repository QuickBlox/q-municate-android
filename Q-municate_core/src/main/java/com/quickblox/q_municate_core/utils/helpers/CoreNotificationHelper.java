package com.quickblox.q_municate_core.utils.helpers;

import android.text.TextUtils;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBPushType;

import java.util.HashMap;
import java.util.List;

public class CoreNotificationHelper {

    public static QBEvent createPushEvent(List<Integer> userIdsList, String message, String messageType) {
        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        userIds.addAll(userIdsList);
        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.PRODUCTION);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setPushType(QBPushType.GCM);
        setMessage(event, message, messageType);
        return event;
    }

    private static void setMessage(QBEvent event, String message, String messageType) {
        if (!setMessageWithTypeIfNeed(event, message, messageType)) {
            event.setMessage(message);
        }
    }

    private static boolean setMessageWithTypeIfNeed(QBEvent event, String message, String messageType) {
        if (!TextUtils.isEmpty(messageType)) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("data.message", message);
            data.put("data.type", messageType);
            event.setMessage(data);
            return true;
        }
        return false;
    }
}