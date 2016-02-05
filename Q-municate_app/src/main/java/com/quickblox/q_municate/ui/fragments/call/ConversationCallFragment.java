package com.quickblox.q_municate.ui.fragments.call;

import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.call.CallActivity;
import com.quickblox.q_municate.ui.adapters.call.OpponentsFromCallAdapter;
import com.quickblox.q_municate.ui.views.RTCGLVideoView;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.StartConversationReason;
import com.quickblox.q_municate_core.qb.commands.push.QBSendPushCommand;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.call.CameraUtils;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBMediaStreamManager;
import com.quickblox.videochat.webrtc.exception.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.VideoRenderer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * QuickBlox team
 */
public class ConversationCallFragment extends Fragment implements Serializable, QBRTCClientVideoTracksCallbacks,
        QBRTCSessionConnectionCallbacks, CallActivity.QBRTCSessionUserCallback/*, OpponentsFromCallAdapter.OnAdapterEventListener*/ {

    private static final int DEFAULT_ROWS_COUNT = 2;
    private static final int DEFAULT_COLS_COUNT = 3;
    private static final long TOGGLE_CAMERA_DELAY = 1000;
    private static final long LOCAL_TRACk_INITIALIZE_DELAY = 500;

    private String TAG = ConversationCallFragment.class.getSimpleName();
    private ArrayList<QBUser> opponents;
    private QBRTCTypes.QBConferenceType qbConferenceType;
    private StartConversationReason startConversationReason;
    private String sessionID;

    private ToggleButton cameraToggle;
//    private ToggleButton switchCameraToggle;
//    private ToggleButton dynamicToggleVideoCall;
    private ToggleButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
//    private View myCameraOff;
    private View view;
    private Map<String, String> userInfo;
    private boolean isVideoCall = false;
    private boolean isAudioEnabled = true;
    private List<QBUser> allUsers = new ArrayList<>();
    private LinearLayout actionVideoButtonsLayout;
    private String callerName;
    private boolean isMessageProcessed;
    private RTCGLVideoView localVideoView;
    private RTCGLVideoView remoteVideoView;
    private IntentFilter intentFilter;
    private AudioStreamReceiver audioStreamReceiver;
    private CameraState cameraState = CameraState.NONE;
//    private RecyclerView recyclerView;
//    private SparseArray<OpponentsFromCallAdapter.ViewHolder> opponentViewHolders;
    private boolean isPeerToPeerCall;
    private QBRTCVideoTrack localVideoTrack;
    private QBRTCVideoTrack remoteVideoTrack;
    private Handler mainHandler;

    public static ConversationCallFragment newInstance(List<QBUser> opponents, String callerName,
            QBRTCTypes.QBConferenceType qbConferenceType,
            StartConversationReason reason,
            String sessionId) {

        ConversationCallFragment fragment = new ConversationCallFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(QBServiceConsts.EXTRA_CONFERENCE_TYPE, qbConferenceType);
        bundle.putString(QBServiceConsts.EXTRA_CALLER_NAME, callerName);
        bundle.putSerializable(QBServiceConsts.EXTRA_OPPONENTS, (Serializable) opponents);

        bundle.putSerializable(QBServiceConsts.EXTRA_START_CONVERSATION_REASON_TYPE, reason);
        if (sessionId != null) {
            bundle.putString(QBServiceConsts.EXTRA_SESSION_ID, sessionId);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_conversation, container, false);
        Log.d(TAG, "Fragment. Thread id: " + Thread.currentThread().getId());

        if (getArguments() != null) {
            opponents = (ArrayList<QBUser>) getArguments().getSerializable(QBServiceConsts.EXTRA_OPPONENTS);
            qbConferenceType = (QBRTCTypes.QBConferenceType) getArguments().getSerializable(QBServiceConsts.EXTRA_CONFERENCE_TYPE);
            startConversationReason = (StartConversationReason) getArguments().getSerializable(QBServiceConsts.EXTRA_START_CONVERSATION_REASON_TYPE);
            sessionID = getArguments().getString(QBServiceConsts.EXTRA_SESSION_ID);
            callerName = getArguments().getString(QBServiceConsts.EXTRA_CALLER_NAME);

            isPeerToPeerCall = opponents.size() == 1;
            isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(qbConferenceType);
            Log.d(TAG, "CALLER_NAME: " + callerName);
            Log.d(TAG, "opponents: " + opponents.toString());
        }

        initViews(view);
        ((CallActivity)getActivity()).initActionBar();
        ((CallActivity)getActivity()).setCallActionBarTitle(StartConversationReason.INCOME_CALL_FOR_ACCEPTION
                .equals(startConversationReason) ? callerName : opponents.get(0).getFullName());
        initButtonsListener();
        initSessionListener();
        setUpUiByCallType();

        mainHandler = new FragmentLifeCycleHandler();
        return view;

    }

    private void initSessionListener() {
        ((CallActivity) getActivity()).addVideoTrackCallbacksListener(this);
    }

    private void setUpUiByCallType() {
        if (!isVideoCall) {
            cameraToggle.setVisibility(View.GONE);
        }
    }

    public void actionButtonsEnabled(boolean enability) {

        cameraToggle.setEnabled(enability);
        micToggleVideoCall.setEnabled(enability);

        // inactivate toggle buttons
        cameraToggle.setActivated(enability);
        micToggleVideoCall.setActivated(enability);
    }


    @Override
    public void onStart() {

        getActivity().registerReceiver(audioStreamReceiver, intentFilter);

        super.onStart();
        QBRTCSession session = ((CallActivity) getActivity()).getCurrentSession();
        if (!isMessageProcessed) {
            if (startConversationReason == StartConversationReason.INCOME_CALL_FOR_ACCEPTION) {
                session.acceptCall(session.getUserInfo());
            } else {
                sendPushAboutCall();
                session.startCall(session.getUserInfo());
            }
            isMessageProcessed = true;
        }
        ((CallActivity) getActivity()).addTCClientConnectionCallback(this);
        ((CallActivity) getActivity()).addRTCSessionUserCallback(this);
    }

    private void sendPushAboutCall() {
        if (isPeerToPeerCall) {
            QBFriendListHelper qbFriendListHelper = new QBFriendListHelper(getActivity());
            QBUser qbUser = opponents.get(0);

            if (!qbFriendListHelper.isUserOnline(qbUser.getId())) {
                String callMsg = getString(R.string.dlg_offline_call,
                        AppSession.getSession().getUser().getFullName());
                QBSendPushCommand.start(getActivity(), callMsg, qbUser.getId());
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() from " + TAG);
        super.onCreate(savedInstanceState);

        intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);

        audioStreamReceiver = new AudioStreamReceiver();
    }

    private void initViews(View view) {

//        localVideoView = (RTCGLVideoView) ((ViewStub) view.findViewById(R.id.localViewStub)).inflate();
//        if (localVideoTrack != null) {
//            fillVideoView(localVideoView, localVideoTrack, !isPeerToPeerCall);
//        }

        remoteVideoView = (RTCGLVideoView) ((ViewStub) view.findViewById(R.id.remoteViewStub)).inflate();
//        if (remoteVideoTrack != null) {
//            fillVideoView(remoteVideoView, remoteVideoTrack, isPeerToPeerCall);
//        }

        cameraToggle = (ToggleButton) view.findViewById(R.id.cameraToggle);
        if (!isPeerToPeerCall) {
            initLocalViewUI(view);
        }
//        dynamicToggleVideoCall = (ToggleButton) view.findViewById(R.id.switch_speaker_toggle);
        micToggleVideoCall = (ToggleButton) view.findViewById(R.id.micToggleVideoCall);

        actionVideoButtonsLayout = (LinearLayout) view.findViewById(R.id.element_set_video_buttons);

        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);
//        incUserName = (TextView) view.findViewById(R.id.incUserName);
//        incUserName.setText(callerName);

        actionButtonsEnabled(false);
    }

//    private void setGrid(int columnsCount, int rowsCount) {
//        int gridWidth = recyclerView.getMeasuredWidth();
//        float itemMargin = getResources().getDimension(R.dimen.grid_item_divider);
//        int cellSize = defineMinSize(gridWidth, recyclerView.getMeasuredHeight(),
//                columnsCount, rowsCount, itemMargin);
//        Log.i(TAG, "onGlobalLayout : cellSize=" + cellSize);
//
//        OpponentsFromCallAdapter opponentsAdapter = new OpponentsFromCallAdapter(getActivity(), opponents, cellSize,
//                cellSize, gridWidth, columnsCount, (int) itemMargin,
//                isVideoEnabled);
//        opponentsAdapter.setAdapterListener(ConversationCallFragment.this);
//        recyclerView.setAdapter(opponentsAdapter);
//    }

    private int defineMinSize(int measuredWidth, int measuredHeight, int columnsCount, int rowsCount, float padding) {
        int cellWidth = measuredWidth / columnsCount - (int) (padding * 2);
        int cellHeight = measuredHeight / rowsCount - (int) (padding * 2);
        return Math.min(cellWidth, cellHeight);
    }

    private int defineRowCount() {
        int result = DEFAULT_ROWS_COUNT;
        int opponentsCount = opponents.size();
        if (opponentsCount < 3) {
            result = opponentsCount;
        }
        return result;

    }

    private int defineColumnsCount() {
        int result = DEFAULT_COLS_COUNT;
        int opponentsCount = opponents.size();
        if (opponentsCount == 1 || opponentsCount == 2) {
            result = 1;
        } else if (opponentsCount == 4) {
            result = 2;
        }
        return result;
    }

    @Override
    public void onResume() {
        super.onResume();

        // If user changed camera state few times and last state was CameraState.ENABLED_FROM_USER // Жень, глянь здесь, смысл в том, что мы здесь включаем камеру, если юзер ее не выключал
        // than we turn on cam, else we nothing change
        if (cameraState != CameraState.DISABLED_FROM_USER
                && isVideoCall) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        // If camera state is CameraState.ENABLED_FROM_USER or CameraState.NONE
        // than we turn off cam
        if (cameraState != CameraState.DISABLED_FROM_USER && isVideoCall) {
            toggleCamera(false);
        }

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(audioStreamReceiver);
        ((CallActivity) getActivity()).removeRTCClientConnectionCallback(this);
        ((CallActivity) getActivity()).removeRTCSessionUserCallback();
    }

    private void initSwitchCameraButton(View view) {
//        switchCameraToggle = (ToggleButton) view.findViewById(R.id.switch_camera_toggle);
//        switchCameraToggle.setOnClickListener(getCameraSwitchListener());
//        switchCameraToggle.setActivated(false);
//        switchCameraToggle.setVisibility(isVideoEnabled ?
//                View.VISIBLE : View.INVISIBLE);
    }

    private void initButtonsListener() {
        cameraToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                cameraState = isChecked ? CameraState.ENABLED_FROM_USER : CameraState.DISABLED_FROM_USER;
                toggleCamera(isChecked);
            }
        });

//        dynamicToggleVideoCall.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (((CallActivity) getActivity()).getCurrentSession() != null) {
//                    Log.d(TAG, "Dynamic switched!");
//                    ((CallActivity) getActivity()).getCurrentSession().switchAudioOutput();
//                }
//            }
//        });

        micToggleVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CallActivity) getActivity()).getCurrentSession() != null) {
                    if (isAudioEnabled) {
                        Log.d(TAG, "Mic is off!");
                        ((CallActivity) getActivity()).getCurrentSession().setAudioEnabled(false);
                        isAudioEnabled = false;
                    } else {
                        Log.d(TAG, "Mic is on!");
                        ((CallActivity) getActivity()).getCurrentSession().setAudioEnabled(true);
                        isAudioEnabled = true;
                    }
                }
            }
        });

        handUpVideoCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionButtonsEnabled(false);
                handUpVideoCall.setEnabled(false);
                Log.d(TAG, "Call is stopped");

                ((CallActivity) getActivity()).hangUpCurrentSession();
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);

            }
        });
    }

    private View.OnClickListener getCameraSwitchListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                QBRTCSession currentSession = ((CallActivity) getActivity()).getCurrentSession();
                if (currentSession == null) {
                    return;
                }
                final QBMediaStreamManager mediaStreamManager = currentSession.getMediaStreamManager();
                if (mediaStreamManager == null) {
                    return;
                }
                boolean cameraSwitched = mediaStreamManager.switchCameraInput(new Runnable() {
                    @Override
                    public void run() {
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                toggleCamerainternal(mediaStreamManager);
                            }
                        });
                    }
                });
            }

        };
    }

    private void toggleCamerainternal(QBMediaStreamManager mediaStreamManager){
        toggleCameraOnUiThread(false);
        int currentCameraId = mediaStreamManager.getCurrentCameraId();
        Log.d(TAG, "Camera was switched!");
        RTCGLVideoView.RendererConfig config = new RTCGLVideoView.RendererConfig();
        config.mirror = CameraUtils.isCameraFront(currentCameraId);
        localVideoView.updateRenderer(isPeerToPeerCall ? RTCGLVideoView.RendererSurface.SECOND :
                RTCGLVideoView.RendererSurface.MAIN, config);
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toggleCameraOnUiThread(true);
            }
        }, TOGGLE_CAMERA_DELAY);
    }

    private void toggleCameraOnUiThread(final boolean toggle){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleCamera(toggle);
            }
        });
    }

    private void runOnUiThread(Runnable runnable){
        mainHandler.post(runnable);
    }

    private void toggleCamera(boolean isNeedEnableCam) {
        QBRTCSession currentSession = ((CallActivity) getActivity()).getCurrentSession();
        if (currentSession != null && currentSession.getMediaStreamManager() != null){
            currentSession.getMediaStreamManager().setVideoEnabled(isNeedEnableCam);
//            localVideoView.setVisibility(isNeedEnableCam ? View.VISIBLE: View.INVISIBLE);
//            if (myCameraOff != null) {
//                myCameraOff.setVisibility(isNeedEnableCam ? View.INVISIBLE : View.VISIBLE);
//            }
//            if (switchCameraToggle != null) {
//                switchCameraToggle.setVisibility(isNeedEnableCam ? View.VISIBLE : View.INVISIBLE);
//            }
        }
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession qbrtcSession, final QBRTCVideoTrack videoTrack) {
        Log.d(TAG, "onLocalVideoTrackReceive() run");

        if (localVideoView != null) {
            fillVideoView(localVideoView, videoTrack, !isPeerToPeerCall);
        } else {
            //localVideoView hasn't been inflated yet. Will set track while OnBindLastViewHolder
            localVideoTrack = videoTrack;
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
        Log.d(TAG, "onRemoteVideoTrackReceive for opponent= " + userID);
        if (remoteVideoView != null) {
            fillVideoView(remoteVideoView, videoTrack);
        } else {
            //remoteVideoView hasn't been inflated yet. Will set track while OnBindLastViewHolder
            remoteVideoTrack = videoTrack;
        }
    }

    //last opponent view is bind
//    @Override
//    public void OnBindLastViewHolder(OpponentsFromCallAdapter.ViewHolder holder, int position) {
//        Log.i(TAG, "OnBindLastViewHolder position=" + position);
//        if (!isVideoEnabled) {
//            return;
//        }
//        if (isPeerToPeerCall) {
//            localVideoView = holder.getOpponentView();
//            initLocalViewUI(holder.itemView);
//        } else {
//            //on group call we postpone initialization of localVideoView due to set it on Gui renderer.
//            // Refer to RTCGlVIew
//            mainHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    localVideoView = (RTCGLVideoView) ((ViewStub) getView().findViewById(R.id.localViewStub)).inflate();
//                    if (localVideoTrack != null) {
//                        fillVideoView(localVideoView, localVideoTrack, !isPeerToPeerCall);
//                    }
//                }
//            }, LOCAL_TRACk_INITIALIZE_DELAY);
//        }
//    }

    private void initLocalViewUI(View localView) {
        initSwitchCameraButton(localView);
    }

    private void fillVideoView(RTCGLVideoView videoView, QBRTCVideoTrack videoTrack, boolean remoteRenderer) {
        videoTrack.addRenderer(new VideoRenderer(remoteRenderer ?
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.MAIN) :
                videoView.obtainVideoRenderer(RTCGLVideoView.RendererSurface.SECOND)));
        Log.d(TAG, (remoteRenderer ? "remote" : "local") + " Track is rendering");
    }

    private void fillVideoView(RTCGLVideoView videoView, QBRTCVideoTrack videoTrack) {
        fillVideoView(videoView, videoTrack, true);
    }

    @Override
    public void onStartConnectToUser(QBRTCSession qbrtcSession, Integer userId) {
//        setStatusForOpponent(userId, getString(R.string.checking));
    }

    @Override
    public void onConnectedToUser(QBRTCSession qbrtcSession,final Integer userId) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                actionButtonsEnabled(true);
            }
        });
//        setStatusForOpponent(userId, getString(R.string.connected));
    }


    @Override
    public void onConnectionClosedForUser(QBRTCSession qbrtcSession, Integer integer) {
//        setStatusForOpponent(integer, getString(R.string.closed));
    }

    @Override
    public void onDisconnectedFromUser(QBRTCSession qbrtcSession, Integer integer) {
//        setStatusForOpponent(integer, getString(R.string.disconnected));
    }

    @Override
    public void onDisconnectedTimeoutFromUser(QBRTCSession qbrtcSession, Integer integer) {
//        setStatusForOpponent(integer, getString(R.string.time_out));
    }

    @Override
    public void onConnectionFailedWithUser(QBRTCSession qbrtcSession, Integer integer) {
//        setStatusForOpponent(integer, getString(R.string.failed));
    }

    @Override
    public void onError(QBRTCSession qbrtcSession, QBRTCException e) {

    }

    @Override
    public void onUserNotAnswer(QBRTCSession session, Integer userId) {
//        setStatusForOpponent(userId, getString(R.string.call_no_answer));
    }

    @Override
    public void onCallRejectByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
//        setStatusForOpponent(userId, getString(R.string.call_rejected));
    }

    @Override
    public void onCallAcceptByUser(QBRTCSession session, Integer userId, Map<String, String> userInfo) {
//        setStatusForOpponent(userId, getString(R.string.call_accepted));
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession session, Integer userId) {
//        setStatusForOpponent(userId, getString(R.string.call_hung_up));
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)) {
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)) {
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

//            dynamicToggleVideoCall.setChecked(intent.getIntExtra("state", -1) == 1);

        }
    }

    private enum CameraState {
        NONE,
        DISABLED_FROM_USER,
        ENABLED_FROM_USER
    }

    class FragmentLifeCycleHandler extends Handler{

        @Override
        public void dispatchMessage(Message msg) {
            if (isAdded() && getActivity() != null) {
                super.dispatchMessage(msg);
            } else {
                Log.d(TAG, "Fragment under destroying");
            }
        }
    }

    class DividerItemDecoration extends RecyclerView.ItemDecoration {

        private int space;

        public DividerItemDecoration(@NonNull Context context, @DimenRes int dimensionDivider) {
            this.space = context.getResources().getDimensionPixelSize(dimensionDivider);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(space, space, space, space);
        }

    }
}