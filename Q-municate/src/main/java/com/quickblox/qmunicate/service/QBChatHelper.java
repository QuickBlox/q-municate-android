package com.quickblox.qmunicate.service;

import android.content.Context;
import android.content.Intent;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.xmpp.QBPrivateChat;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat_webrtc.ISignalingChannel;
import com.quickblox.module.videochat_webrtc.SignalingChannel;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.ui.mediacall.CallActivity;
import com.quickblox.qmunicate.ui.utils.Consts;

import org.webrtc.SessionDescription;

import java.util.List;

public class QBChatHelper {

    private Lo lo = new Lo(this);
    private QBPrivateChat privateChat;
    private SignalingChannel signalingChannel;
    private Context context;
    private ISignalingChannel.MessageObserver messageObserver = new SignalingMessageObserver();

    public SignalingChannel getSignalingChannel() {
        return signalingChannel;
    }

    public void init(Context context) {
        this.context = context;
        privateChat = QBChatService.getInstance().getPrivateChatInstance();
        signalingChannel = new SignalingChannel(privateChat);
        signalingChannel.addMessageObserver(messageObserver);
    }

    private class SignalingMessageObserver implements ISignalingChannel.MessageObserver {

        @Override
        public void onCall(QBUser user, CallType callType, SessionDescription sessionDescription,
                           long l) {
            SessionDescriptionWrapper sessionDescriptionWrapper = new SessionDescriptionWrapper(sessionDescription);
            lo.g("onCall");
            Intent intent = new Intent(context, CallActivity.class);
            intent.putExtra(Consts.CALL_DIRECTION_TYPE_EXTRA, Consts.CALL_DIRECTION_TYPE.INCOMING);
            intent.putExtra(Consts.CALL_TYPE_EXTRA, callType);
            intent.putExtra(Consts.USER, user);
            intent.putExtra(Consts.REMOTE_DESCRIPTION, sessionDescriptionWrapper);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent);
        }

        @Override
        public void onAccepted(QBUser user, SessionDescription sessionDescription, long session) {

        }

        @Override
        public void onStop(QBUser user, String s, long session) {

        }

        @Override
        public void onRejected(QBUser user, long session) {

        }

        @Override
        public void onError(List<String> errors) {
            lo.g("error while establishing connection" + errors.toString());
        }
    }

}

