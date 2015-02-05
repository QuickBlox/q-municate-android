package com.quickblox.q_municate.ui.mediacall;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.quickblox.chat.exception.QBChatException;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.core.communication.SessionDescriptionWrapper;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.MediaUtils;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.videochat.webrtc.QBVideoChannel;
import com.quickblox.videochat.webrtc.QBVideoChat;
import com.quickblox.videochat.webrtc.exception.QBVideoException;
import com.quickblox.videochat.webrtc.listener.QBVideoChatWebRTCSignalingListenerImpl;
import com.quickblox.videochat.webrtc.model.CallConfig;
import com.quickblox.videochat.webrtc.model.ConnectionConfig;
import com.quickblox.videochat.webrtc.signaling.QBSignalingChannel;
import com.quickblox.videochat.webrtc.view.QBVideoStreamView;

import org.webrtc.SessionDescription;

import java.util.Timer;
import java.util.TimerTask;


public abstract class OutgoingCallFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = OutgoingCallFragment.class.getSimpleName();
    protected QBVideoChat videoChat;
    protected User opponent;
    private ConstsCore.CALL_DIRECTION_TYPE call_direction_type;
    private SessionDescription remoteSessionDescription;
    private boolean bounded;
    private QBService service;
    private com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM call_type;
    private Timer callTimer;
    private ServiceConnection serviceConnection = new ChetServiceConnection();
    private OutgoingCallListener outgoingCallListener;
    private String sessionId;
    private QBVideoChatWebRTCSignalingListenerImpl signalingMessageHandler;
    private QBSignalingChannel.PLATFORM remotePlatform;
    private QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION deviceOrientation;
    private QBVideoChannel signalingChannel;
    private QBVideoChatHelper videoChatHelper;
    private ConnectionConfig currentConnectionConfig;

    public interface OutgoingCallListener {

        public void onConnectionAccepted();

        public void onConnectionRejected();

        public void onConnectionClosed();
    }

    private enum STOP_TYPE {
        REJECTED, CLOSED
    }

    protected abstract int getContentView();

    public static Bundle generateArguments(SessionDescriptionWrapper sessionDescriptionWrapper, User friend,
            ConstsCore.CALL_DIRECTION_TYPE type, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM callType, String sessionId,
            QBSignalingChannel.PLATFORM platform,
            QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION deviceOrientation) {
        Bundle args = new Bundle();
        args.putSerializable(ConstsCore.EXTRA_FRIEND, friend);
        args.putSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, type);
        args.putSerializable(ConstsCore.CALL_TYPE_EXTRA, callType);
        args.putSerializable(com.quickblox.videochat.webrtc.Consts.ORIENTATION_EXTENSION, deviceOrientation);
        args.putSerializable(com.quickblox.videochat.webrtc.Consts.PLATFORM_EXTENSION, platform);
        args.putParcelable(ConstsCore.REMOTE_DESCRIPTION, sessionDescriptionWrapper);
        args.putString(com.quickblox.videochat.webrtc.Consts.SESSION_ID_EXTENSION, sessionId);
        return args;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            outgoingCallListener = (OutgoingCallListener) activity;
        } catch (ClassCastException e) {
            ErrorUtils.logError(TAG, e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        outgoingCallListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        connectToService();
    }

    @Override
    public void onStop() {
        super.onStop();
        unbindService();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoChat != null) {
            videoChat.onActivityPause();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getContentView(), container, false);
        rootView.findViewById(R.id.stopСallButton).setOnClickListener(this);
        ToggleButton muteMicrophoneToggleBtn = (ToggleButton) rootView.findViewById(R.id.muteMicrophoneButton);
        boolean microphoneMuted = MediaUtils.isMicrophoneMuted(getActivity());
        muteMicrophoneToggleBtn.setChecked(microphoneMuted);
        if (microphoneMuted){
            ErrorUtils.showError(getActivity(), getActivity().getString(R.string.dlg_microphone_muted));
        }
        muteMicrophoneToggleBtn.setOnCheckedChangeListener(new MuteMicrophoneCheckedChangeListener());
        initChatData();
        reInitChatIfExist(rootView);
        postInit(rootView);
        return rootView;
    }

    private void reInitChatIfExist(View rootView) {
        QBVideoStreamView videoView = (QBVideoStreamView) rootView.findViewById(R.id.ownVideoScreenImageView);
        if (videoChat != null && videoView != null) {
            videoChat.setVideoView(videoView);
        }
    }

    private void initChatData() {
        if (call_direction_type != null) {
            return;
        }
        SessionDescriptionWrapper sessionDescriptionWrapper = getArguments().getParcelable(
                ConstsCore.REMOTE_DESCRIPTION);
        if (sessionDescriptionWrapper != null) {
            remoteSessionDescription = sessionDescriptionWrapper.getSessionDescription();
        }
        call_direction_type = (ConstsCore.CALL_DIRECTION_TYPE) getArguments().getSerializable(
                ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
        opponent = (User) getArguments().getSerializable(ConstsCore.EXTRA_FRIEND);
        call_type = (com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM) getArguments().getSerializable(
                ConstsCore.CALL_TYPE_EXTRA);
        remotePlatform = (QBSignalingChannel.PLATFORM) getArguments().getSerializable(
                com.quickblox.videochat.webrtc.Consts.PLATFORM_EXTENSION);
        deviceOrientation = (QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION) getArguments().getSerializable(
                com.quickblox.videochat.webrtc.Consts.ORIENTATION_EXTENSION);
        sessionId = getArguments().getString(com.quickblox.videochat.webrtc.Consts.SESSION_ID_EXTENSION, "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stopСallButton:
                stopCall(true, STOP_TYPE.CLOSED);
                break;
            default:
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (videoChat != null) {
            videoChat.onActivityResume();
        }
    }

    public void initChat(QBSignalingChannel signalingChannel) throws QBVideoException {
        if (videoChat != null) {
            return;
        }
        QBVideoStreamView videoView = (QBVideoStreamView) getView().findViewById(R.id.ownVideoScreenImageView);
        videoChat = new QBVideoChat(getActivity(), signalingChannel, videoView);
        videoChat.setMediaCaptureCallback(new MediaCapturerHandler());
        signalingMessageHandler = new VideoChatMessageHandler();
        signalingChannel.addSignalingListener(signalingMessageHandler);
        if (remoteSessionDescription != null) {
            videoChat.setRemoteSessionDescription(remoteSessionDescription);
        }
        if (ConstsCore.CALL_DIRECTION_TYPE.OUTGOING.equals(call_direction_type) && opponent != null) {
            startCall();
        } else {
            QBUser userOpponent = Utils.friendToUser(opponent);
            CallConfig callConfig = new CallConfig(userOpponent, sessionId, deviceOrientation);
            callConfig.setCallStreamType(call_type);
            callConfig.setSessionDescription(remoteSessionDescription);
            callConfig.setDevicePlatform(remotePlatform);
            currentConnectionConfig = callConfig;
            videoChat.accept(callConfig);
            onConnectionEstablished();
        }
    }

    protected void onConnectionEstablished() {
        if (outgoingCallListener != null) {
            outgoingCallListener.onConnectionAccepted();
        }
    }

    protected void postInit(View rootView) {
    }

    protected void onConnectionClosed() {
        if (outgoingCallListener != null) {
            outgoingCallListener.onConnectionClosed();
        }
    }

    protected void onConnectionRejected() {
        if (outgoingCallListener != null) {
            outgoingCallListener.onConnectionRejected();
        }
    }

    private void muteMicrophone() {
        if (videoChat != null) {
            videoChat.muteMicrophone(!videoChat.isMicrophoneMute());
        }
    }

    private void startCall() {
        QBUser sender = AppSession.getSession().getUser();
        if (sender != null) {
            QBUser userOpponent = Utils.friendToUser(opponent);
            currentConnectionConfig = videoChat.call(userOpponent, call_type,
                    ConstsCore.DEFAULT_CALL_PACKET_REPLY_TIMEOUT);
            callTimer = new Timer();
            callTimer.schedule(new CancelCallTimerTask(), ConstsCore.DEFAULT_DIALING_TIME);
        }
    }

    private void connectToService() {
        Intent intent = new Intent(getActivity(), QBService.class);
        if (isExistActivity()) {
            getBaseActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void cancelCallTimer() {
        if (callTimer != null) {
            callTimer.cancel();
            callTimer = null;
        }
    }

    private void unbindService() {
        if (isExistActivity() && bounded) {
            getBaseActivity().unbindService(serviceConnection);
        }
    }

    private void stopCall(boolean sendStop, STOP_TYPE stopType) {
        cancelCallTimer();
        if (videoChat != null) {
            if (sendStop) {
                videoChat.stopCall();
            } else {
                videoChat.disposeConnection();
            }
        }
        if (signalingChannel != null) {
            signalingChannel.removeSignalingListener(signalingMessageHandler);
        }
        if (videoChatHelper != null) {
            videoChatHelper.closeSignalingChannel(currentConnectionConfig);
        }
        if (STOP_TYPE.CLOSED.equals(stopType)) {
            onConnectionClosed();
        } else {
            onConnectionRejected();
        }
    }

    private void onConnectedToService() {
        videoChatHelper = (QBVideoChatHelper) service.getHelper(QBService.VIDEO_CHAT_HELPER);
        if (ConstsCore.CALL_DIRECTION_TYPE.INCOMING.equals(call_direction_type)) {
            signalingChannel = videoChatHelper.getSignalingChannel(opponent.getUserId());
        } else {
            signalingChannel = videoChatHelper.makeSignalingChannel(opponent.getUserId());
        }
        if (signalingChannel != null && isExistActivity()) {
            tryInitChat();
        } else if (isExistActivity()) {
            ErrorUtils.showError(getActivity(), "Cannot establish connection. Check internet settings");
        }
    }

    private void tryInitChat() {
        try {
            initChat(signalingChannel);
        } catch (QBVideoException e) {
            ErrorUtils.showError(getActivity(), getString(R.string.videochat_init_fail));
            stopCall(true, STOP_TYPE.CLOSED);
        }
    }

    private class MuteMicrophoneCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            muteMicrophone();
        }
    }

    private class MediaCapturerHandler implements QBVideoChat.MediaCaptureCallback {

        @Override
        public void onCaptureFail(com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM media_stream, String s) {
            if (isExistActivity()) {
                ErrorUtils.showError(getActivity(), s);
            }
        }

        @Override
        public void onCaptureSuccess(com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM media_stream) {

        }
    }

    private class VideoChatMessageHandler extends QBVideoChatWebRTCSignalingListenerImpl {

        @Override
        public void onAccepted(ConnectionConfig connectionConfig) {
            cancelCallTimer();
            if (isExistActivity()) {
                getBaseActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.show(getActivity(), "accepted");
                        onConnectionEstablished();
                    }
                });
            }
        }

        @Override
        public void onStop(ConnectionConfig connectionConfig) {
            stopCall(false, STOP_TYPE.CLOSED);
        }

        @Override
        public void onRejected(ConnectionConfig connectionConfig) {
            cancelCallTimer();
            if (isExistActivity()) {
                getBaseActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.show(getActivity(), "Rejected");
                        stopCall(false, STOP_TYPE.REJECTED);
                    }
                });
            }
        }

        @Override
        public void onClosed(final String error) {
            if (isExistActivity()) {
                getBaseActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ErrorUtils.showError(getActivity(), error);
                    }
                });
            }
        }

        @Override
        public void onError(final QBSignalingChannel.PacketType state, final QBChatException e) {
            if (isExistActivity()) {
                getBaseActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ErrorUtils.showError(getActivity(), e.getLocalizedMessage());
                        switch (state) {
                            case qbvideochat_call:
                            case qbvideochat_acceptCall: {
                                stopCall(true, STOP_TYPE.CLOSED);
                                break;
                            }
                            default:
                                break;
                        }
                    }
                });
            }
        }
    }

    private class ChetServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.i(TAG, "onServiceConnected");
            bounded = true;
            service = ((QBService.QBServiceBinder) binder).getService();
            onConnectedToService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    class CancelCallTimerTask extends TimerTask {

        @Override
        public void run() {
            if (isExistActivity()) {
                getBaseActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        stopCall(true, STOP_TYPE.CLOSED);
                    }
                });
            }
        }
    }
}