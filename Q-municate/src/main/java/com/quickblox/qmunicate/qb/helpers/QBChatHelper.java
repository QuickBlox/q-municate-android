package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.chat.QBChatRoom;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.listeners.RoomListener;
import com.quickblox.module.chat.xmpp.QBPrivateChat;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat_webrtc.ExtensionSignalingChannel;
import com.quickblox.module.videochat_webrtc.ISignalingChannel;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.ui.mediacall.CallActivity;
import com.quickblox.qmunicate.utils.Consts;

import org.webrtc.SessionDescription;

import java.util.List;
import java.util.Map;

public class QBChatHelper implements RoomListener {

    private Lo lo = new Lo(this);
    private QBPrivateChat privateChat;
    private ISignalingChannel signalingChannel;
    private Context context;
    private ISignalingChannel.MessageObserver messageObserver = new SignalingMessageObserver();

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
        lo.g("on Error when join" + s.toString());
        roomListener.onError(s);
    }

    public void init(Context context) {
        this.context = context;
        privateChat = QBChatService.getInstance().getPrivateChatInstance();
        signalingChannel = new ExtensionSignalingChannel(privateChat);
        signalingChannel.addMessageObserver(messageObserver);
    }

    public QBChatRoom getJoinedRoom() {
        return joinedRoom;
    }

    public void joinRoom(String name, RoomListener roomListener) {
        this.roomListener = roomListener;
        QBChatService.getInstance().joinRoom(name, this);
    }

    private class SignalingMessageObserver implements ISignalingChannel.MessageObserver {

        @Override
        public void onCall(QBUser user, int callType, SessionDescription sessionDescription, String sessionId,
                ISignalingChannel.PLATFORM platform,
                ISignalingChannel.PLATFORM_DEVICE_ORIENTATION deviceOrientation, Map<String, String> params) {
            SessionDescriptionWrapper sessionDescriptionWrapper = new SessionDescriptionWrapper(
                    sessionDescription);
            lo.g("onCall" + callType);
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra(Consts.CALL_DIRECTION_TYPE_EXTRA, Consts.CALL_DIRECTION_TYPE.INCOMING);
            intent.putExtra(Consts.CALL_TYPE_EXTRA, callType);
            intent.putExtra(WebRTC.SESSION_ID_EXTENSION, sessionId);
            intent.putExtra(Consts.USER, user);
            intent.putExtra(Consts.REMOTE_DESCRIPTION, sessionDescriptionWrapper);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent);
        }

        @Override
        public void onAccepted(QBUser user, SessionDescription sessionDescription, String sessionId,
                ISignalingChannel.PLATFORM platform,
                ISignalingChannel.PLATFORM_DEVICE_ORIENTATION deviceOrientation, Map<String, String> params) {

        }

        @Override
        public void onParametersChanged(QBUser qbUser, String s,
                ISignalingChannel.PLATFORM_DEVICE_ORIENTATION deviceOrientation,
                Map<String, String> stringStringMap) {

        }

        @Override
        public void onStop(QBUser user, ISignalingChannel.STOP_REASON reason, String session) {

        }

        @Override
        public void onRejected(QBUser user, String session) {

        }

        @Override
        public void onError(List<String> errors) {
            lo.g("error while establishing connection" + errors.toString());
        }
    }
}

