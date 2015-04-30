package com.quickblox.q_municate.ui.mediacall;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientConnectionCallbacks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public abstract class OutgoingCallFragment extends BaseFragment implements View.OnClickListener, QBRTCClientConnectionCallbacks {

    public static final String TAG = "LCYCLE" + OutgoingCallFragment.class.getSimpleName();
    private static String SESSION_ID_EXTENSION = "sessionId";
    protected User opponent;
    private ConstsCore.CALL_DIRECTION_TYPE call_direction_type;
    private boolean bounded;
    private QBService service;
    private Timer callTimer;
    private String sessionId;

    protected OutgoingCallFragmentInterface outgoingCallFragmentInterface;


    private ArrayList<Integer> opponents;
    private int startReason;
    private QBRTCSessionDescription sessionDescription;

    private Map<String, String> userInfo;
//    private boolean isVideoEnabled = true;
    private boolean isAudioEnabled = true;
    private List<QBUser> allUsers = new ArrayList<>();
    private boolean isMessageProcessed;
    private QBRTCTypes.QBConferenceType call_type;
    private ToggleButton muteDynamicButton;
    private ImageButton stopСallButton;
    private ToggleButton muteMicrophoneButton;
    private TextView timerTextView;
    private Handler handler;
    private TimeUpdater updater;
    protected QBVideoChatHelper videoChatHelper;


    protected void initUI(View rootView) {
        Log.d("CALL_INTEGRATION","OutgoingCallFragment initUI ");
        timerTextView = (TextView) rootView.findViewById(R.id.timerTextView);
        if (updater != null) {
            updater.setTextView(timerTextView);
        }


        muteDynamicButton = (ToggleButton) rootView.findViewById(R.id.muteDynamicButton);
        muteDynamicButton.setOnClickListener(this);

        stopСallButton = (ImageButton) rootView.findViewById(R.id.stopСallButton);
        stopСallButton.setOnClickListener(this);

        muteMicrophoneButton = (ToggleButton) rootView.findViewById(R.id.muteMicrophoneButton);
        muteMicrophoneButton.setOnClickListener(this);

    }

    // ----------------------------- ConnectionState callbacks -------------------------- //

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {

    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startTimer(timerTextView);
            }
        });
        ((CallActivity)getActivity()).cancelPlayer();                   // надо пересмотреть
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().finish();
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().finish();
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().finish();
    }


    /* ==========================   Q-municate original code   ==========================*/


    protected abstract int getContentView();

    public static Bundle generateArguments(User friend,
            ConstsCore.CALL_DIRECTION_TYPE type, QBRTCTypes.QBConferenceType callType, String sessionId) {

        Bundle args = new Bundle();
        args.putSerializable(ConstsCore.EXTRA_FRIEND, friend);
        args.putSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, type);
        args.putSerializable(ConstsCore.CALL_TYPE_EXTRA, callType);
        args.putString(SESSION_ID_EXTENSION, sessionId);
        return args;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            Log.d("CALL_INTEGRATION", "OutgoingCallFragment. onAttach");
            outgoingCallFragmentInterface = (OutgoingCallFragmentInterface)activity;
            videoChatHelper = outgoingCallFragmentInterface.needVideoChatHelper();
        } catch (ClassCastException e) {
            ErrorUtils.logError(TAG, e);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        outgoingCallFragmentInterface = null;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

        Log.d("CALL_INTEGRATION", "OutgoingCallFragment. onStart");
        QBRTCClient.getInstance().addConnectionCallbacksListener(this);


        if (getArguments() != null){
            ConstsCore.CALL_DIRECTION_TYPE directionType = (ConstsCore.CALL_DIRECTION_TYPE) getArguments().getSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
            if (directionType == ConstsCore.CALL_DIRECTION_TYPE.OUTGOING){
                Log.d("CALL_INTEGRATION", "OutgoingCallFragment. Start call");
                ((CallActivity)getActivity()).startCall();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        QBRTCClient.getInstance().removeConnectionCallbacksListener(OutgoingCallFragment.this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        Log.d("CALL_INTEGRATION","OutgoingCallFragment. onCreateView ");
        View rootView = inflater.inflate(getContentView(), container, false);
        rootView.findViewById(R.id.stopСallButton).setOnClickListener(this);

        initChatData();
        initUI(rootView);

        Log.d("CALL_INTEGRATION","init video calls client");
        if (!QBRTCClient.isInitiated()) {
            QBRTCClient.init();
        }

        Log.d("CALL_INTEGRATION"," call prepare to prosess smack calls on video chat client");
        QBRTCClient.getInstance().prepareToProcessCalls(getActivity());

        return rootView;
    }

    private void initChatData() {

        Log.d("CALL_INTEGRATION","OutgoingCallFragment. initChatData()");

        if (call_direction_type != null) {
            return;
        }
        call_direction_type = (ConstsCore.CALL_DIRECTION_TYPE) getArguments().getSerializable(
                ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
        opponent = (User) getArguments().getSerializable(ConstsCore.EXTRA_FRIEND);
        call_type = (QBRTCTypes.QBConferenceType) getArguments().getSerializable(
                ConstsCore.CALL_TYPE_EXTRA);
        sessionId = getArguments().getString(SESSION_ID_EXTENSION, "");

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.muteDynamicButton:
                switchAudioOutput();
                break;
            case R.id.stopСallButton:
                stopCall();
                Log.d("Track", "Call is stopped");
                break;
            case R.id.muteMicrophoneButton:
                toggleMicrophone();
                break;
            default:
                break;
        }
    }

    private void switchAudioOutput(){
        if (outgoingCallFragmentInterface != null) {
            outgoingCallFragmentInterface.switchSpeaker();
            Log.d(TAG, "Speaker switched!");
        }
    }

    private void toggleMicrophone(){
        if (outgoingCallFragmentInterface != null) {
            if (isAudioEnabled) {
                outgoingCallFragmentInterface.offMic();
                isAudioEnabled = false;
                Log.d(TAG, "Mic is off!");
            } else {
                outgoingCallFragmentInterface.onMic();
                isAudioEnabled = true;
                Log.d(TAG, "Mic is on!");
            }
        }
    }

    public void stopCall(){
        if (outgoingCallFragmentInterface != null) {
            outgoingCallFragmentInterface.hungUpClick();
        }
        stopTimer();
    }

    private void startTimer(TextView textView) {

        handler = new Handler();
        updater = new TimeUpdater(textView, handler);
        handler.postDelayed(updater, ConstsCore.SECOND);
    }

    private void stopTimer() {
        if (handler != null && updater != null) {
            handler.removeCallbacks(updater);
        }
    }

//    private void cancelCallTimer() {
//        if (callTimer != null) {
//            callTimer.cancel();
//            callTimer = null;
//        }
//    }

//    class CancelCallTimerTask extends TimerTask {
//
//        @Override
//        public void run() {
//            if (isExistActivity()) {
//                getBaseActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        stopCall(true, STOP_TYPE.CLOSED);
//                    }
//                });
//            }
//        }
//    }
}