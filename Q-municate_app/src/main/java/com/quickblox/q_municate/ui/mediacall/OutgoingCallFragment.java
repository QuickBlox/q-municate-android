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
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientConnectionCallbacks;

/**
 * Base fragment for all call fragments.
 * Each call fragment extends UI of this one.
 * <p/>
 * Fragment receives callbacks about current session peer connection states.
 * Also it throws callbacks to activity about interaction with UI elements related with call.
 * <p/>
 *
 * Each call fragment should receive in bundle next call info:
 * <ul>
 *      <li>EXTRA_FRIEND - call opponent</li>
 *      <li>CALL_DIRECTION_TYPE_EXTRA - INCOMING|OUTGOING</li>
 * </ul>
 */
public abstract class OutgoingCallFragment extends BaseFragment implements View.OnClickListener, QBRTCClientConnectionCallbacks {

    // Log tags
    public static final String TAG = OutgoingCallFragment.class.getSimpleName();
    private static final String CALL_INTEGRATION = "CALL_INTEGRATION";

    // Necessary fot call info
    protected User opponent;
    protected LocalVideoViewCreationListener localVideoViewCreationListener;
    protected RemoteVideoViewCreationListener remoteVideoViewCreationListener;
    protected CallStoppedListener callStoppedListener;
    protected CallAudioActionsListener callAudioActionsListener;
    protected CallVideoActionsListener callVideoActionsListener;

    protected QBVideoChatHelper videoChatHelper;
    private ConstsCore.CALL_DIRECTION_TYPE call_direction_type;
    // Internal fields
    private Handler handler;
    private TimeUpdater updater;
    private boolean callStateStopped;

    // Listenning of plagining/unplagining HEADSET/SCO devices
    private AudioStreamReceiver audioStreamReceiver;
    // UI elements
    private boolean isAudioEnabled = true;
    private IntentFilter intentFilter;
    private ToggleButton muteDynamicButton;
    private ImageButton stopСallButton;
    private ToggleButton muteMicrophoneButton;
    private TextView timerTextView;

    public static Bundle generateArguments(User friend,
                                           ConstsCore.CALL_DIRECTION_TYPE type, QBRTCTypes.QBConferenceType callType, String sessionId) {

        Bundle args = new Bundle();
        args.putSerializable(ConstsCore.EXTRA_FRIEND, friend);
        args.putSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, type);
        args.putSerializable(ConstsCore.CALL_TYPE_EXTRA, callType);
        args.putString(ConstsCore.SESSION_ID, sessionId);
        return args;
    }

    protected void initUI(View rootView) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment initUI ");
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

        setActionButtonsEnability(false);
    }


    // ----------------------------- ConnectionState callbacks -------------------------- //

    public void setActionButtonsEnability(boolean enability) {

        muteDynamicButton.setEnabled(enability);
        muteMicrophoneButton.setEnabled(enability);

        // inactivate toggle buttons
        muteDynamicButton.setActivated(enability);
        muteMicrophoneButton.setActivated(enability);
    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer integer) {
        ((CallActivity) getActivity()).cancelPlayer();                   // надо пересмотреть
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "OutgoingCallFragment onConnectedToUser ");
                ((CallActivity) getActivity()).stopIncomeCallTimer();
                startTimer(timerTextView);
                setActionButtonsEnability(true);
            }
        });
    }

    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment onConnectionClosedForUser ");
        ((CallActivity) getActivity()).stopIncomeCallTimer();
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "OutgoingCallFragment onDisconnectedFromUser ");
                Toast.makeText(getActivity(), "Disconnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment onDisconnectedTimeoutFromUser");
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment onConnectionFailedWithUser");
        setActionButtonsEnability(false);
    }

    // -----------------------------  Q-municate original code  ----------------------------- /

    @Override
    public void onError(QBRTCSession session, QBRTCException exeption) {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment onError");
        Toast.makeText(getActivity(), "ERROR:" + exeption.getMessage(), Toast.LENGTH_LONG).show();
        setActionButtonsEnability(false);
    }

    protected abstract int getContentView();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Check is activity implements fragment interface
        try {
            Log.d(CALL_INTEGRATION, "OutgoingCallFragment. onAttach");
            callStoppedListener = (CallStoppedListener) activity;
            callAudioActionsListener = (CallAudioActionsListener) activity;
            callVideoActionsListener = (CallVideoActionsListener) activity;

            videoChatHelper = ((CallActivity) getActivity()).getVideoChatHelper();
        } catch (ClassCastException e) {
            ErrorUtils.logError(TAG, e);
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Log.d(CALL_INTEGRATION, "OutgoingCallFragment. onCreate");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView()");
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment. onCreateView ");
        View rootView = inflater.inflate(getContentView(), container, false);

        initChatData();
        initUI(rootView);
        return rootView;
    }


    private void initChatData() {
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment. initChatData()");

        if (call_direction_type != null) {
            return;
        }

        call_direction_type = (ConstsCore.CALL_DIRECTION_TYPE) getArguments().getSerializable(
                ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
        opponent = (User) getArguments().getSerializable(ConstsCore.EXTRA_FRIEND);
    }


    @Override
    public void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
        Log.d(CALL_INTEGRATION, "OutgoingCallFragment. onStart");
        QBRTCClient.getInstance().addConnectionCallbacksListener(this);

        registerAudioReceiver();
    }


    private void registerAudioReceiver() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

        audioStreamReceiver = new AudioStreamReceiver();
        getActivity().registerReceiver(audioStreamReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        QBRTCClient.getInstance().removeConnectionCallbacksListener(this);
        getActivity().unregisterReceiver(audioStreamReceiver);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callStoppedListener = null;
        callAudioActionsListener = null;
        callVideoActionsListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.muteDynamicButton:
                switchAudioOutput();
                break;
            case R.id.stopСallButton:
                setCallState(true);
                setActionButtonsEnability(false);
                stopСallButton.setEnabled(false);
                stopСallButton.setActivated(false);
                stopCall();
                break;
            case R.id.muteMicrophoneButton:
                toggleMicrophone();
                break;
            default:
                break;
        }
    }


    // --------------------- Call actions redirection to activity ------------------------ //

    private void switchAudioOutput() {
        if (callAudioActionsListener != null) {
            callAudioActionsListener.switchSpeaker();
            Log.d(TAG, "Speaker switched!");
        }
    }

    private void toggleMicrophone() {
        if (callAudioActionsListener != null) {
            if (isAudioEnabled) {
                callAudioActionsListener.onMic(false);
                isAudioEnabled = false;
                Log.d(TAG, "Mic is off!");
            } else {
                callAudioActionsListener.onMic(true);
                isAudioEnabled = true;
                Log.d(TAG, "Mic is on!");
            }
        }
    }

    public void stopCall() {
        if (callStoppedListener != null) {
            callStoppedListener.hangUpClick();
        }
        stopTimer();

        Log.d(CALL_INTEGRATION, "Fragment in activity " + getActivity());
    }


    // ----------------------- Internal methods  ------------------- //

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

    public void setCallState(boolean callStateStopped) {
        this.callStateStopped = callStateStopped;
    }

    public boolean isCallStopped() {
        return callStateStopped;
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

            if (intent.getIntExtra("state", -1) == 0) {
                muteDynamicButton.setChecked(false);
            } else if (intent.getIntExtra("state", -1) == 1) {
                muteDynamicButton.setChecked(true);
            }
            muteDynamicButton.invalidate();
        }
    }
}