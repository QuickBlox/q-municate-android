package com.quickblox.q_municate.ui.mediacall;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
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
    private IntentFilter intentFilter;
    private AudioStreamReceiver audioStreamReceiver;
    protected boolean callIsStarted;


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
        ((CallActivity)getActivity()).cancelPlayer();                   // надо пересмотреть

    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                startTimer(timerTextView);
            }
        });
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

        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

        audioStreamReceiver = new AudioStreamReceiver();
        getActivity().registerReceiver(audioStreamReceiver, intentFilter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        outgoingCallFragmentInterface = null;

        getActivity().unregisterReceiver(audioStreamReceiver);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
//        if (!callIsStarted) {
//            Log.d("CALL_INTEGRATION", "OutgoingCallFragment. onStart");
//            QBRTCClient.getInstance().addConnectionCallbacksListener(this);
//        }


        if (getArguments() != null){
            ConstsCore.CALL_DIRECTION_TYPE directionType = (ConstsCore.CALL_DIRECTION_TYPE) getArguments().getSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
            if (directionType == ConstsCore.CALL_DIRECTION_TYPE.OUTGOING && !callIsStarted){
                Log.d("CALL_INTEGRATION", "OutgoingCallFragment. Start call");
                ((CallActivity)getActivity()).startCall();
                callIsStarted = true;
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        QBRTCClient.getInstance().removeConnectionCallbacksListener(OutgoingCallFragment.this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Log.d("CALL_INTEGRATION", "OutgoingCallFragment. onStart");
        QBRTCClient.getInstance().addConnectionCallbacksListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        Log.d("CALL_INTEGRATION", "OutgoingCallFragment. onCreateView ");
        View rootView = inflater.inflate(getContentView(), container, false);
        rootView.findViewById(R.id.stopСallButton).setOnClickListener(this);

        initChatData();
        initUI(rootView);

        Log.d("CALL_INTEGRATION","init video calls client");
        if (!QBRTCClient.isInitiated()) {
            QBRTCClient.init();
        }

        Log.d("CALL_INTEGRATION", " call prepare to prosess smack calls on video chat client");
        QBRTCClient.getInstance().prepareToProcessCalls(getActivity());

        return rootView;
    }

    private void initChatData() {

        Log.d("CALL_INTEGRATION", "OutgoingCallFragment. initChatData()");

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
        if (handler == null) {
            handler = new Handler();
            updater = new TimeUpdater(textView, handler);
            handler.postDelayed(updater, ConstsCore.SECOND);
        }
    }

    private void stopTimer() {
        if (handler != null && updater != null) {
            handler.removeCallbacks(updater);
        }
    }

    private void setDynamicButtonState (){
        AudioManager audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        if (audioManager.isBluetoothA2dpOn()) {
            // через Bluetooth
            muteDynamicButton.setChecked(true);
        } else if (audioManager.isSpeakerphoneOn()) {
            // через динамик телефона
            muteDynamicButton.setChecked(false);
        } else if (audioManager.isWiredHeadsetOn()) {
            // через проводные наушники
            muteDynamicButton.setChecked(true);
        } else {
            muteDynamicButton.setChecked(false);
        }
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)){
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)){
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

            if (intent.getIntExtra("state", -1) == 0 /*|| intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -1) == 0*/){
                muteDynamicButton.setChecked(false);
            } else if (intent.getIntExtra("state", -1) == 1) {
                muteDynamicButton.setChecked(true);
            } else {
//                Toast.makeText(context, "Output audio stream is incorrect", Toast.LENGTH_LONG).show();
            }
            muteDynamicButton.invalidate();


//            Toast.makeText(context, "Audio stream changed", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

//        unregisterBroadcastReceiver();
    }

//    public void registerBroadcastReceiver() {
//        getActivity().registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
//    }
//
//    public void unregisterBroadcastReceiver() {
//        getActivity().unregisterReceiver(myNoisyAudioStreamReceiver);
//    }

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

    @Override
    public void onDestroy() {
        super.onDestroy();
        QBRTCClient.getInstance().removeConnectionCallbacksListener(OutgoingCallFragment.this);

    }
}