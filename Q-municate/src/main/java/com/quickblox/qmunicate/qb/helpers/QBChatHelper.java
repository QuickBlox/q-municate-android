package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.chat.QBChatRoom;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.xmpp.QBPrivateChat;
import com.quickblox.module.videochat_webrtc.ExtensionSignalingChannel;
import com.quickblox.module.videochat_webrtc.ISignalingChannel;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.module.videochat_webrtc.model.CallConfig;
import com.quickblox.module.videochat_webrtc.model.ConnectionConfig;
import com.quickblox.module.videochat_webrtc.utils.MessageHandlerImpl;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.ui.mediacall.CallActivity;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;

public class QBChatHelper implements RoomListener {

    private Lo lo = new Lo(this);
    private QBPrivateChat privateChat;
    private ISignalingChannel signalingChannel;
    private Context context;
    private ISignalingChannel.MessageHandler messageObserver = new SignalingMessageHandler();

    private QBChatRoom joinedRoom;
    private RoomListener roomListener;

    public ISignalingChannel getSignalingChannel() {
        return signalingChannel;
    }

    @Override
    public void onCreatedRoom(QBChatRoom qbChatRoom) {
        lo.g("on create Room ");
        roomListener.onCreatedRoom(qbChatRoom);
    }

    @Override
    public void onJoinedRoom(QBChatRoom qbChatRoom) {
        lo.g("on join Room");
        joinedRoom = qbChatRoom;
        roomListener.onJoinedRoom(qbChatRoom);
    }

    @Override
    public void onError(String s) {
        lo.g("on Error when join" + s);
        roomListener.onError(s);
    }

    public void init(Context context) {
        this.context = context;
        privateChat = QBChatService.getInstance().getPrivateChatInstance();
        signalingChannel = new ExtensionSignalingChannel(privateChat);
        signalingChannel.addMessageHandler(messageObserver);
    }

    public QBChatRoom getJoinedRoom() {
        return joinedRoom;
    }

    public void joinRoom(String name, RoomListener roomListener) {
        this.roomListener = roomListener;
        QBChatService.getInstance().joinRoom(name, this);
    }

    private class SignalingMessageHandler extends MessageHandlerImpl {

        @Override
        public void onCall(ConnectionConfig connectionConfig) {
            CallConfig callConfig = (CallConfig) connectionConfig;
            SessionDescriptionWrapper sessionDescriptionWrapper = new SessionDescriptionWrapper(
                    callConfig.getSessionDescription());
            lo.g("onCall" + callConfig.getCallStreamType().toString());
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra(Consts.CALL_DIRECTION_TYPE_EXTRA, Consts.CALL_DIRECTION_TYPE.INCOMING);
            intent.putExtra(WebRTC.PLATFORM_EXTENSION, callConfig.getDevicePlatform());
            intent.putExtra(WebRTC.ORIENTATION_EXTENSION, callConfig.getDeviceOrientation());
            intent.putExtra(Consts.CALL_TYPE_EXTRA, callConfig.getCallStreamType());
            intent.putExtra(WebRTC.SESSION_ID_EXTENSION, callConfig.getConnectionSession());
            intent.putExtra(Consts.USER, callConfig.getParticipant());
            intent.putExtra(Consts.REMOTE_DESCRIPTION, sessionDescriptionWrapper);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent);
        }

        @Override
        public void onError(List<String> errors) {
            lo.g("error while establishing connection" + errors.toString());
        }
    }
}

