package com.quickblox.qmunicate.core.gcm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.messages.model.QBNotificationType;
import com.quickblox.module.messages.model.QBPushType;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.model.PushMessage;

/**
 * Created by vadim on 14.03.14.
 */
public class NotificationHelper {

    public static final String ACTION_VIDEO_CALL = "com.quickblox.qmunicate.VIDEO_CALL";
    public static final String ACTION_AUDIO_CALL = "com.quickblox.qmunicate.AUDIO_CALL";
    public static final String MESSAGE = "message";

    public static final String CALL_TYPE = "call";

    public static QBEvent createPushEvent(QBUser qbUser, String message, String messageType){
        PushMessage pushMessage = new PushMessage();
        pushMessage.setMessage(message);
        pushMessage.setUserId(qbUser.getId());
        pushMessage.setType(messageType);
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String pushJsonMessage = gson.toJson(pushMessage);
        StringifyArrayList<Integer> userIds = new StringifyArrayList<Integer>();
        userIds.add(qbUser.getId());
        QBEvent event = new QBEvent();
        event.setUserIds(userIds);
        event.setEnvironment(QBEnvironment.DEVELOPMENT);
        event.setNotificationType(QBNotificationType.PUSH);
        event.setPushType(QBPushType.GCM);
        event.setMessage(pushJsonMessage);
        return event;
    }
}
