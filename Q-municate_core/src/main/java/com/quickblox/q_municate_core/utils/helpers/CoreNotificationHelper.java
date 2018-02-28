package com.quickblox.q_municate_core.utils.helpers;

import android.text.TextUtils;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBEvent;
import com.quickblox.messages.model.QBNotificationType;
import com.quickblox.messages.model.QBPushType;
import com.quickblox.q_municate_core.utils.ConstsCore;

import org.json.JSONObject;

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
            String eventMsg = customMessage(message, messageType);
            event.setMessage(eventMsg);
            return true;
        }
        return false;
    }

    private static boolean isCallType(String messageType) {
        return TextUtils.equals(messageType, ConstsCore.PUSH_MESSAGE_TYPE_CALL);
    }

    private static String customMessage(String message, String messageType) {
        JSONObject json = new JSONObject();
        try {
            json.put("message", message);
            // custom parameters
            json.put("type", messageType);
            if (isCallType(messageType)) {
                json.put("ios_voip", "1");
                json.put("VOIPCall", "1");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return json.toString();
    }
}