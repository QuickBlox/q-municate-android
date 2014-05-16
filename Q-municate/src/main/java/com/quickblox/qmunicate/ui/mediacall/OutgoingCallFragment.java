package com.quickblox.qmunicate.ui.mediacall;

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

import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat_webrtc.QBSignalingChannel;
import com.quickblox.module.videochat_webrtc.QBVideoChat;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.module.videochat_webrtc.model.CallConfig;
import com.quickblox.module.videochat_webrtc.model.ConnectionConfig;
import com.quickblox.module.videochat_webrtc.render.VideoStreamsView;
import com.quickblox.module.videochat_webrtc.utils.SignalingListenerImpl;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.ErrorUtils;

import org.webrtc.SessionDescription;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public abstract class OutgoingCallFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = OutgoingCallFragment.class.getSimpleName();
    protected QBVideoChat qbVideoChat;
    protected QBUser opponent;
    private Consts.CALL_DIRECTION_TYPE call_direction_type;
    private SessionDescription remoteSessionDescription;
    private boolean bounded;
    private QBService service;
    private WebRTC.MEDIA_STREAM call_type;
    private Timer callTimer;
    private ServiceConnection serviceConnection = new ChetServiceConnection();
    private OutgoingCallListener outgoingCallListener;
    private String sessionId;
    private QBSignalingChannel.SignalingListener signalingMessageHandler;
    private QBSignalingChannel.PLATFORM remotePlatform;
    private QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION deviceOrientation;

    public interface OutgoingCallListener {

        public void onConnectionAccepted();

        public void onConnectionRejected();

        public void onConnectionClosed();
    }

    private enum STOP_TYPE {
        REJECTED, CLOSED
    }

    protected abstract int getContentView();

    public static Bundle generateArguments(SessionDescriptionWrapper sessionDescriptionWrapper, QBUser user,
            Consts.CALL_DIRECTION_TYPE type, WebRTC.MEDIA_STREAM callType, String sessionId,
            QBSignalingChannel.PLATFORM platform,
            QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION deviceOrientation) {
        Bundle args = new Bundle();
        args.putSerializable(Consts.USER, user);
        args.putSerializable(Consts.CALL_DIRECTION_TYPE_EXTRA, type);
        args.putSerializable(Consts.CALL_TYPE_EXTRA, callType);
        args.putSerializable(WebRTC.ORIENTATION_EXTENSION, deviceOrientation);
        args.putSerializable(WebRTC.PLATFORM_EXTENSION, platform);
        args.putParcelable(Consts.REMOTE_DESCRIPTION, sessionDescriptionWrapper);
        args.putString(WebRTC.SESSION_ID_EXTENSION, sessionId);
        return args;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            outgoingCallListener = (OutgoingCallListener) activity;
        } catch (ClassCastException exception) {
            exception.printStackTrace();
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
        if (qbVideoChat != null) {
            qbVideoChat.onActivityPause();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getContentView(), container, false);
        rootView.findViewById(R.id.stopСallButton).setOnClickListener(this);
        rootView.findViewById(R.id.muteMicrophoneButton).setOnClickListener(this);
        initChatData();
        reInitChatIfExist(rootView);
        postInit(rootView);
        return rootView;
    }

    private void reInitChatIfExist(View rootView) {
        VideoStreamsView videoView = (VideoStreamsView) rootView.findViewById(R.id.ownVideoScreenImageView);
        if (qbVideoChat != null && videoView != null) {
            qbVideoChat.setVideoView(videoView);
        }
    }

    private void initChatData() {
        if (call_direction_type != null) {
            return;
        }
        SessionDescriptionWrapper sessionDescriptionWrapper = getArguments().getParcelable(
                Consts.REMOTE_DESCRIPTION);
        if (sessionDescriptionWrapper != null) {
            remoteSessionDescription = sessionDescriptionWrapper.getSessionDescription();
        }
        call_direction_type = (Consts.CALL_DIRECTION_TYPE) getArguments().getSerializable(
                Consts.CALL_DIRECTION_TYPE_EXTRA);
        opponent = (QBUser) getArguments().getSerializable(Consts.USER);
        call_type = (WebRTC.MEDIA_STREAM) getArguments().getSerializable(Consts.CALL_TYPE_EXTRA);
        remotePlatform = (QBSignalingChannel.PLATFORM) getArguments().getSerializable(
                WebRTC.PLATFORM_EXTENSION);
        deviceOrientation = (QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION) getArguments().getSerializable(
                WebRTC.ORIENTATION_EXTENSION);
        sessionId = getArguments().getString(WebRTC.SESSION_ID_EXTENSION, "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.stopСallButton:
                stopCall(true, STOP_TYPE.CLOSED);
                break;
            case R.id.muteMicrophoneButton:
                muteMicrophone();
                break;
            default:
                break;
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        if (qbVideoChat != null) {
            qbVideoChat.onActivityResume();
        }
    }

    public void initChat(QBSignalingChannel signalingChannel) {
        if (qbVideoChat != null) {
            return;
        }
        VideoStreamsView videoView = (VideoStreamsView) getView().findViewById(R.id.ownVideoScreenImageView);
        qbVideoChat = new QBVideoChat(getActivity(), signalingChannel, videoView);
        qbVideoChat.setMediaCaptureCallback(new MediaCapturerHandler());
        signalingMessageHandler = new VideoChatMessageHandler();
        signalingChannel.addSignalingListener(signalingMessageHandler);
        if (remoteSessionDescription != null) {
            qbVideoChat.setRemoteSessionDescription(remoteSessionDescription);
        }
        if (Consts.CALL_DIRECTION_TYPE.OUTGOING.equals(call_direction_type) && opponent != null) {
            startCall();
        } else {
            CallConfig callConfig = new CallConfig(opponent, sessionId, deviceOrientation);
            callConfig.setCallStreamType(call_type);
            callConfig.setSessionDescription(remoteSessionDescription);
            callConfig.setDevicePlatform(remotePlatform);
            qbVideoChat.accept(callConfig);
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
        if (qbVideoChat != null) {
            qbVideoChat.muteMicrophone(!qbVideoChat.isMicrophoneMute());
        }
    }

    private void startCall() {
        qbVideoChat.call(opponent, call_type);
        callTimer = new Timer();
        callTimer.schedule(new CancelCallTimerTask(), 30 * Consts.SECOND);
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
        if (qbVideoChat != null) {
            if (sendStop) {
                qbVideoChat.stopCall();
            } else {
                qbVideoChat.disposeConnection();
            }
            qbVideoChat.clean();
        }
        if (STOP_TYPE.CLOSED.equals(stopType)) {
            onConnectionClosed();
        } else {
            onConnectionRejected();
        }
    }

    private void onConnectedToService() {
        QBSignalingChannel signalingChannel = service.getQbVideoChatHelper().getSignalingChannel();
        if (signalingChannel != null && isExistActivity()) {
            initChat(signalingChannel);
        } else if (isExistActivity()) {
            ErrorUtils.showError(getActivity(), "Cannot establish connection. Check internet settings");
        }
    }

    private class MediaCapturerHandler implements QBVideoChat.MediaCaptureCallback {

        @Override
        public void onCaptureFail(WebRTC.MEDIA_STREAM media_stream, String s) {
            if (isExistActivity()) {
                ErrorUtils.showError(getActivity(), s);
            }
        }

        @Override
        public void onCaptureSuccess(WebRTC.MEDIA_STREAM media_stream) {

        }
    }

    private class VideoChatMessageHandler extends SignalingListenerImpl {

        @Override
        public void onAccepted(ConnectionConfig connectionConfig) {
            cancelCallTimer();
            if (isExistActivity()) {
                getBaseActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUtils.show(getActivity(), "accepted");
                    }
                });
            }
            onConnectionEstablished();
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
        public void onClosed(String error) {
            if (isExistActivity()) {
                ErrorUtils.showError(getActivity(), error);
            }
        }

        @Override
        public void onError(List<String> errors) {
            if (isExistActivity()) {
                ErrorUtils.showError(getActivity(), errors.toString());
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
            stopCall(true, STOP_TYPE.CLOSED);
        }
    }
}