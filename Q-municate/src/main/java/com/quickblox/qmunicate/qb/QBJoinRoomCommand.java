package com.quickblox.qmunicate.qb;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.QBChatRoom;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.service.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

public class QBJoinRoomCommand extends ServiceCommand {

    private static final String TAG = QBJoinRoomCommand.class.getSimpleName();

    private QBChatRoom qbChatRoom;
    private volatile boolean existAnswer;

    private String error;

    QBChatHelper qbChatHelper;

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.JOIN_ROOM_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    public QBJoinRoomCommand(Context context, String successAction, String failAction, QBChatHelper qbChatHelper) {
        super(context, successAction, failAction);
        this.qbChatHelper = qbChatHelper;
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        qbChatHelper.joinRoom(Consts.DEFAULT_WEB_ROOM, new JoinRoomListener());
        while (!existAnswer) {
            Thread.yield();
        }
        if (error != null) {
            throw new QBResponseException(error);
        }
        return null;
    }

    private class JoinRoomListener implements RoomListener {

        @Override
        public void onCreatedRoom(QBChatRoom qbChatRoom) {
            existAnswer = true;
        }

        @Override
        public void onJoinedRoom(QBChatRoom qbChatRoom) {
            existAnswer = true;
            QBJoinRoomCommand.this.qbChatRoom = qbChatRoom;
        }

        @Override
        public void onError(String stringError) {
            existAnswer = true;
            QBJoinRoomCommand.this.error = stringError;
        }
    }
}
