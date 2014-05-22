package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.videochat_webrtc.ExtensionSignalingChannel;
import com.quickblox.module.videochat_webrtc.QBSignalingChannel;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.module.videochat_webrtc.model.CallConfig;
import com.quickblox.module.videochat_webrtc.model.ConnectionConfig;
import com.quickblox.module.videochat_webrtc.utils.SignalingListenerImpl;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.ui.mediacall.CallActivity;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;

public class QBVideoChatHelper extends BaseHelper {

    private final Lo lo = new Lo(this);
    private QBSignalingChannel signalingChannel;

    public QBVideoChatHelper(Context context) {
        super(context);
    }

    public QBSignalingChannel getSignalingChannel() {
        return signalingChannel;
    }

    public void init() {
        signalingChannel = new ExtensionSignalingChannel(QBChatService.getInstance().getSignalingManager());
        signalingChannel.addSignalingListener(new VideoSignalingListener());
    }

    private class VideoSignalingListener extends SignalingListenerImpl {

        @Override
        public void onError(List<String> errorList) {
            lo.g("error while establishing connection" + errorList.toString());
        }

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
    }
}
