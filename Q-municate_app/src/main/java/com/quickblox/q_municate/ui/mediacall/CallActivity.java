package com.quickblox.q_municate.ui.mediacall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.media.MediaPlayerManager;
import com.quickblox.q_municate.ui.videocall.VideoCallFragment;
import com.quickblox.q_municate.ui.voicecall.VoiceCallFragment;
import com.quickblox.q_municate_core.core.exceptions.QBRTCSessionIsAbsentException;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.push.QBSendPushCommand;
import com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper;
import com.quickblox.q_municate_core.qb.helpers.call.VideoChatHelperSessionListener;
import com.quickblox.q_municate_core.qb.helpers.call.VideoChatVideoTracksListener;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.VideoCallBacks;

import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Receives callbacks from {@link QBVideoChatHelper}
 * <p/>
 * Methods not related with UI call on {@link QBVideoChatHelper} instance.
 * Callbacks received in {@link QBVideoChatHelper} are redirects to CallActivity through {@link VideoChatHelperSessionListener}
 * Interaction with call commands was put in fragments: {@link VideoCallFragment} and {@link VoiceCallFragment}
 * <p/>
 * Instance of this activity could be created via QBService from {@link QBVideoChatHelper} if INCOMING call was received or
 * from {@link com.quickblox.q_municate.ui.chats.PrivateDialogActivity} and {@link com.quickblox.q_municate.ui.friends.FriendDetailsActivity}
 * in case of OUTGOING call was initiated by us.
 * <p/>
 * Activity implements interfaces {@link IncomingCallActionsListener} for processing user accepting or rejecting of call and
 * {@link LocalVideoViewCreationListener} for processing interaction with call of UI commands (onMic(), offMic(), onCam(), offCam(),
 * switchCam(), switchSpeaker(), hangUpClick(), onLocalVideoViewCreated(), onRemoteVideoViewCreated())
 */

public class CallActivity extends BaseLogeableActivity implements IncomingCallActionsListener,
        // Video chat helper listeners
        VideoChatHelperSessionListener, VideoChatVideoTracksListener,
        // Outgoing fragments listeners
        CallStoppedListener, CallAudioActionsListener,CallVideoActionsListener {


    private static final String TAG = CallActivity.class.getSimpleName();
    private static final String CALL_INTEGRATION = "CALL_INTEGRATION";

    // Call tasks identification
    private static final String START_CALL_TASK = "start_call_task";
    private static final String REJECT_CALL_TASK = "reject_call_task";
    private static final String ACCEPT_CALL_TASK = "accept_call_task";
    private static final String HANG_UP_CALL_TASK = "hang_up_call_task";
    private static final String ON_MIC_TASK = "on_mic_task";
    private static final String OFF_MIC_TASK = "off_mic_task";
    private static final String ON_CAM_TASK = "on_cam_task";
    private static final String OFF_CAM_TASK = "off_cam_task";
    private static final String SWITCH_CAM_TASK = "switch_cam_task";
    private static final String SWITCH_SPEACKER_TASK = "switch_speaker_task";

    /**
     * Current call opponent
     */
    private User opponent;

    /**
     * Call direction type. Could be INCOMMING or OUTGOING
     */
    private ConstsCore.CALL_DIRECTION_TYPE call_direction_type;

    /**
     * Call type. Could be AUDIO or VIDEO
     */
    private QBRTCTypes.QBConferenceType call_type;

    /**
     * Executes all non UI call logic, throws callbacks related with call state, video tracks and session existence.
     */
    private QBVideoChatHelper videoChatHelper;

    /**
     * Map of all video call tasks
     */
    private HashMap<String, Runnable> callTasksMap;

    /**
     * Queue of called tasks
     */
    private Queue<Runnable> callTasksQueue;


    // Thre fields for closing activity if user didn't response in a set period.
    private Runnable closeIncomeCallTimerTask;
    private ScheduledThreadPoolExecutor singleTheadScheduledExecutor;
    private ScheduledFuture<?> closeIncomeCallFutureTask;

    /**
     * Caller video view
     */
    private QBGLVideoView localVideoView;

    /**
     * Callee video view
     */
    private QBGLVideoView remoteVideoView;

    /**
     * Map of opponents video views
     */
    private Map<VideoTracks, Set<Runnable>> videoTracksSetEnumMap;

    private MediaPlayerManager mediaPlayer;
    private Map<String, String> userInfo = new HashMap<>();


    /**
     * Starts activity with params in bundle
     *
     * @param context  - application context
     * @param friend   - call opponent
     * @param callType - call type (AUDIO|VIDEO)
     */
    public static void start(Context context, User friend, QBRTCTypes.QBConferenceType callType) {
        Log.d(CALL_INTEGRATION, "CallActivity. START STATIC CALL ACTIVITY");
        Log.i(TAG, "Friend.isOnline() = " + friend.isOnline());
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(ConstsCore.EXTRA_FRIEND, friend);
        intent.putExtra(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, ConstsCore.CALL_DIRECTION_TYPE.OUTGOING);
        intent.putExtra(ConstsCore.CALL_TYPE_EXTRA, callType);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
    }


    /**
     * Start call task
     */
    public void startCall() {
        Log.d(CALL_INTEGRATION, "CallActivity. startCall() executed");
        Runnable callTask = callTasksMap.get(START_CALL_TASK);
        executeCallTask(callTask);
    }


    /**
     * Start call accepting task
     */
    @Override
    public void acceptCallClick() {
        Log.d(CALL_INTEGRATION, "CallActivity. acceptCall() executed");
        cancelPlayer();
        showOutgoingFragment();
        Runnable acceptTask = callTasksMap.get(ACCEPT_CALL_TASK);
        executeCallTask(acceptTask);
    }

    /**
     * Start call rejecting task
     */
    @Override
    public void rejectCallClick() {
        Log.d(CALL_INTEGRATION, "CallActivity. rejectCall() executed");
        cancelPlayer();
        Runnable rejectTask = callTasksMap.get(REJECT_CALL_TASK);
        executeCallTask(rejectTask);
    }

    /**
     * Start turn on/off mic task
     */
    @Override
    public void onMic(boolean turnOn) {
        if (turnOn) {
            Log.d(CALL_INTEGRATION, "CallActivity. onMic() executed");
            Runnable onMicTask = callTasksMap.get(ON_MIC_TASK);
            executeCallTask(onMicTask);
        } else {
            Log.d(CALL_INTEGRATION, "CallActivity. offMic() executed");
            Runnable offMicTask = callTasksMap.get(OFF_MIC_TASK);
            executeCallTask(offMicTask);
        }
    }

    /**
     * Start turn on/off cam task
     */
    @Override
    public void onCam(boolean turnOn) {
        if (turnOn) {
            Log.d(CALL_INTEGRATION, "CallActivity. onCam() executed");
            Runnable onCamTask = callTasksMap.get(ON_CAM_TASK);
            executeCallTask(onCamTask);
        } else {
            Log.d(CALL_INTEGRATION, "CallActivity. offCam() executed");
            Runnable offCamTask = callTasksMap.get(OFF_CAM_TASK);
            executeCallTask(offCamTask);
        }
    }

    /**
     * Start cam switching task
     */
    @Override
    public void switchCam() {
        Log.d(CALL_INTEGRATION, "CallActivity. switchCam() executed");
        Runnable switchCamTask = callTasksMap.get(SWITCH_CAM_TASK);
        executeCallTask(switchCamTask);
    }

    /**
     * Start cam switching task
     */
    @Override
    public void switchSpeaker() {
        Log.d(CALL_INTEGRATION, "CallActivity. switchSpeaker() executed");
        Runnable switchSpeakerTask = callTasksMap.get(SWITCH_SPEACKER_TASK);
        executeCallTask(switchSpeakerTask);
    }

    /**
     * Start hang up task
     */
    @Override
    public void hangUpClick() {
        Log.d(CALL_INTEGRATION, "CallActivity. hungUp() executed");
        Runnable hungUpTask = callTasksMap.get(HANG_UP_CALL_TASK);
        executeCallTask(hungUpTask);
    }

    /**
     * Start local straming task
     */
    public void onLocalVideoViewCreated() {
        for (Runnable runnable : videoTracksSetEnumMap.get(VideoTracks.LOCAL_VIDEO_TRACK)) {
            executeCallTask(runnable);
        }
    }

    /**
     * Start remote streaming task
     */
    public void onRemoteVideoViewCreated() {
        for (Runnable runnable : videoTracksSetEnumMap.get(VideoTracks.REMOTE_VIDEO_TRACK)) {
            executeCallTask(runnable);
        }
    }


    // ------------------------- Call activity lifecycle methods ------------------ //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(CALL_INTEGRATION, "onCreate call activity" + this);

        if (getIntent().getExtras() != null) {
            parseIntentExtras(getIntent().getExtras());
        }

        canPerformLogout.set(false);
        setContentView(R.layout.activity_main_call);
        actionBar.hide();
        mediaPlayer = App.getInstance().getMediaPlayer();

        // Queue for storing tasks which was called while activity doesn't have link on VideoChatHelper
        callTasksQueue = new LinkedList<>();

        // Prepare video tracks sets for storing in map in relation with video track type
        videoTracksSetEnumMap = new EnumMap<>(VideoTracks.class);
        for (VideoTracks videoTracks : VideoTracks.values()) {
            videoTracksSetEnumMap.put(videoTracks, new HashSet<Runnable>());
        }

        // Init map of allowed call's tasks
        // TODO Could be moved to separate enumeration or inner class
        initCallTasksMap();


        if (call_direction_type == ConstsCore.CALL_DIRECTION_TYPE.OUTGOING) {
            startCall();
        }

        addAction(QBServiceConsts.SEND_PUSH_MESSAGES_FAIL_ACTION, failAction);
    }

    private void parseIntentExtras(Bundle extras) {
        call_direction_type = (ConstsCore.CALL_DIRECTION_TYPE) extras.getSerializable(
                ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
        call_type = (QBRTCTypes.QBConferenceType) extras.getSerializable(ConstsCore.CALL_TYPE_EXTRA);
        opponent = (User) extras.getSerializable(ConstsCore.EXTRA_FRIEND);

        Log.i(TAG, "opponentId = " + opponent);
    }


    // TODO Could be moved to separate enumeration or inner class
    private void initCallTasksMap() {
        Log.d(CALL_INTEGRATION, "CallActivity. Set up tasks map");

        callTasksMap = new HashMap<>();

        // Main tasks
        callTasksMap.put(START_CALL_TASK, initStartCallTask());
        callTasksMap.put(ACCEPT_CALL_TASK, initAcceptCallTask());
        callTasksMap.put(REJECT_CALL_TASK, initRejectCallTask());
        callTasksMap.put(HANG_UP_CALL_TASK, initHangUpCallTask());

        // Mic tasks
        callTasksMap.put(ON_MIC_TASK, initMicOnTask());
        callTasksMap.put(OFF_MIC_TASK, initMicOffTask());

        // Cam tasks
        callTasksMap.put(ON_CAM_TASK, initCamOnTask());
        callTasksMap.put(OFF_CAM_TASK, initCamOffTask());

        // Switch cam input and  speaker output
        callTasksMap.put(SWITCH_CAM_TASK, initSwitchCamTask());
        callTasksMap.put(SWITCH_SPEACKER_TASK, initSwitchSpeakerTask());

        Log.d(CALL_INTEGRATION, "CallActivity. Set up tasks map finished");
    }

    private void executeCallTask(Runnable runnable) {
        if (videoChatHelper != null) {
            runOnUiThread(runnable);
        } else {
            callTasksQueue.add(runnable);
        }
    }

    @Override
    protected void onResume() {

        Log.d(CALL_INTEGRATION, "Resume call activity " + this);
        super.onResume();

        // Call activity's lifecycle methods on GLSurfaceViews to allow system mange GL rendering
        if (getLocalVideoView() != null) {
            getLocalVideoView().onResume();
        }

        if (getRemoteVideoView() != null) {
            getRemoteVideoView().onResume();
        }
    }

    @Override
    protected void onPause() {
        Log.d(CALL_INTEGRATION, "Pause call activity " + this);
        super.onPause();

        // Call activity's lifecycle methods on GLSurfaceViews to allow system mange GL rendering
        if (getLocalVideoView() != null) {
            getLocalVideoView().onPause();
        }

        if (getRemoteVideoView() != null) {
            getRemoteVideoView().onPause();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopCall();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(CALL_INTEGRATION, "Destroy call activity " + this);
        stopCall();
    }

    private void stopCall() {
        stopIncomeCallTimer();
        cancelPlayer();

        if (QBRTCClient.isInitiated()) {
            QBRTCClient.getInstance().close(true);
        }

        if (videoChatHelper != null) {
            videoChatHelper.setClientClosed();
            videoChatHelper.removeVideoChatHelperSessionListener(this);
            videoChatHelper.removeVideoChatHelperVideoTracksListener(this);
        }
    }

    // --- Init scheduler on closing activity if user did not responseon coll or didn't receive once --- //
    public void initIncomingCallTask() {

        singleTheadScheduledExecutor = new ScheduledThreadPoolExecutor(1);
        closeIncomeCallTimerTask = new Runnable() {
            @Override
            public void run() {

                Log.d(CALL_INTEGRATION, "Execute Income call timer close");
                if (currentFragment instanceof IncomingCallFragment) {
                    Log.d(CALL_INTEGRATION, "IncomingCallFragment close with rejectCall");
                    executeCallTask(callTasksMap.get(REJECT_CALL_TASK));
                } else {
                    Log.d(CALL_INTEGRATION, "OutgoingCallFragment close with hangUpCall");
                    executeCallTask(callTasksMap.get(HANG_UP_CALL_TASK));
                }
            }
        };
    }

    public void startIncomeCallTimer() {
        Log.d(CALL_INTEGRATION, "Start Income call timer");

        if (singleTheadScheduledExecutor == null) {
            initIncomingCallTask();
        }

        closeIncomeCallFutureTask = singleTheadScheduledExecutor
                .schedule(closeIncomeCallTimerTask, QBRTCConfig.getAnswerTimeInterval(), TimeUnit.SECONDS);
    }

    public void stopIncomeCallTimer() {

        Log.d(CALL_INTEGRATION, "Stop Income call timer");
        if (singleTheadScheduledExecutor != null) {
            closeIncomeCallFutureTask.cancel(true);
            singleTheadScheduledExecutor.shutdown();
            singleTheadScheduledExecutor = null;
        }
    }


    /**
     * Throw push notification about call if user ofline
     *
     * @param friend
     */
    private void notifyFriendOnCall(User friend) {
        if (!friend.isOnline()) {
            String callMsg = getResources().getString(R.string.dlg_offline_call,
                    AppSession.getSession().getUser().getFullName());
            QBSendPushCommand.start(this, callMsg, friend.getUserId());
        }
    }

    private void showOutgoingFragment() {
        Log.d(CALL_INTEGRATION, "CallActivity. showOutgoingFragment");

        if (call_direction_type.equals(ConstsCore.CALL_DIRECTION_TYPE.OUTGOING)) {
            playOutgoingRingtone();
        }

        OutgoingCallFragment outgoingCallFragment = (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(
                call_type)) ? new VideoCallFragment() : new VoiceCallFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, call_direction_type);
        bundle.putSerializable(ConstsCore.EXTRA_FRIEND, opponent);
        bundle.putSerializable(ConstsCore.CALL_TYPE_EXTRA, call_type);
        outgoingCallFragment.setArguments(bundle);

        setCurrentFragment(outgoingCallFragment);

    }

    private void showIncomingFragment() {
        Log.d(CALL_INTEGRATION, "CallActivity. showIncomingFragment");
        playIncomingRingtone();
        IncomingCallFragment incomingCallFragment = IncomingCallFragment.newInstance(call_type,
                opponent);
        setCurrentFragment(incomingCallFragment);
    }

    private void showToastMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CallActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --------------- Manage call ringtones ------------ //
    private void playOutgoingRingtone() {
        if (mediaPlayer != null) {
            mediaPlayer.playSound("calling.mp3", true);
        }
    }

    private void playIncomingRingtone() {
        if (mediaPlayer != null) {
            mediaPlayer.playDefaultRingTone();
        }
    }

    protected void cancelPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stopPlaying();
        }
    }


    // -------------- Implements QBVideoChatHelper methods ---------------- //

    @Override
    public void onReceiveNewSession() {
        Log.d(CALL_INTEGRATION, "CallActivity. onReceiveNewSession");
    }

    @Override
    public void onUserNotAnswer(Integer integer) {
        Log.d(CALL_INTEGRATION, "CallActivity. onUserNotAnswer");
        showToastMessage(getString(R.string.user_not_answer));
    }

    @Override
    public void onCallRejectByUser(Integer integer, Map<String, String> stringStringMap) {
        Log.d(CALL_INTEGRATION, "CallActivity. onCallRejectByUser");
        showToastMessage(getString(R.string.user_reject_call));
    }

    @Override
    public void onReceiveHangUpFromUser(Integer integer) {
        Log.d(CALL_INTEGRATION, "CallActivity. onReceiveHangUpFromUser");

        if (currentFragment instanceof IncomingCallFragment
                || currentFragment instanceof OutgoingCallFragment) {
            showToastMessage(getString(R.string.user_hang_up_call));
        }
    }

    @Override
    public void onSessionClosed() {
        Log.d(CALL_INTEGRATION, "CallActivity. onSessionClosed");
        cancelPlayer();
        stopIncomeCallTimer();
        finish();
    }

    @Override
    public void onSessionStartClose() {
        Log.d(CALL_INTEGRATION, "CallActivity. onSessionStartClose");
    }

    @Override
    public void onLocalVideoTrackReceive(final QBRTCVideoTrack videoTrack) {
        if (getLocalVideoView() == null) {
            videoTracksSetEnumMap.get(VideoTracks.LOCAL_VIDEO_TRACK).add(initLocalVideoTrackTask(videoTrack));
        } else {
            initLocalVideoTrack(videoTrack);
        }
    }

    @Override
    public void onRemoteVideoTrackReceive(final QBRTCVideoTrack videoTrack, Integer userID) {
        if (getRemoteVideoView() == null) {
            videoTracksSetEnumMap.get(VideoTracks.REMOTE_VIDEO_TRACK).add(initRemoteVideoTrackTask(videoTrack));
        } else {
            initRemoteVideoTrack(videoTrack);
        }
    }

    @Override
    public void onError(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }


    /**
     * Callback on service connected uses for QBVideoChatHelper instance receive
     *
     * @param service
     */
    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);

        if (videoChatHelper == null) {

            Log.d(CALL_INTEGRATION, "CallActivity. onConnectedToService");
            videoChatHelper = (QBVideoChatHelper) service.getHelper(QBService.VIDEO_CHAT_HELPER);

            videoChatHelper.addVideoChatHelperSessionListener(this);
            videoChatHelper.addVideoChatVideoTracksListener(this);

            if (call_direction_type != null) {
                if (ConstsCore.CALL_DIRECTION_TYPE.INCOMING.equals(call_direction_type)) {
                    showIncomingFragment();
                } else {
                    notifyFriendOnCall(opponent);
                    showOutgoingFragment();
                }
            }
            executeScheduledTasks();
        }
    }

    /**
     * Executes all scheduled tasks as soon as we receive QBVideoChatHelper instance
     */
    private void executeScheduledTasks() {
        Log.d(CALL_INTEGRATION, "CallActivity. executeScheduledTasks");
        if (videoChatHelper != null) {
            for (Runnable task : callTasksQueue) {
                runOnUiThread(task);
            }
        }
    }


    // --------------------------------  CALL TASKS LIST  --------------------------------- //
    // TODO All methods from list below could be moved to separate enumeration or inner class

    private Runnable initStartCallTask() {
        return new Runnable() {
            @Override
            public void run() {
                final List<Integer> opponents = new ArrayList<>();
                opponents.add(opponent.getUserId());
                Log.d(CALL_INTEGRATION, "CallActivity. initStartCallTask lunched");
                videoChatHelper.startCall(userInfo, opponents, call_type);
            }
        };
    }

    private Runnable initAcceptCallTask() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "CallActivity. initAcceptCallTask lunched");
                try {
                    videoChatHelper.acceptCall(userInfo);
                } catch (QBRTCSessionIsAbsentException e) {
                    Log.d(CALL_INTEGRATION, e.getMessage() + " acceptCall");
                }
            }
        };
    }

    private Runnable initRejectCallTask() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "CallActivity. initRejectCallTask lunched");
                cancelPlayer();
                try {
                    videoChatHelper.rejectCall(userInfo);
                } catch (QBRTCSessionIsAbsentException e) {
                    finishActivitiForAbsentSession();
                    Log.d(CALL_INTEGRATION, e.getMessage() + " rejectCall");
                }
            }
        };
    }

    private Runnable initMicOffTask() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "CallActivity. initMicOffTask lunched");
                try {
                    videoChatHelper.setMicState(false);
                } catch (QBRTCSessionIsAbsentException e) {
                    Log.d(CALL_INTEGRATION, e.getMessage() + " setMicState to false");
                }
            }
        };
    }

    private Runnable initMicOnTask() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "CallActivity. initMicOnTask lunched");
                try {
                    videoChatHelper.setMicState(true);
                } catch (QBRTCSessionIsAbsentException e) {
                    Log.d(CALL_INTEGRATION, e.getMessage() + " setMicState to true");
                }
            }
        };
    }

    private Runnable initCamOnTask() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "CallActivity. initCamOnTask lunched");
                try {
                    videoChatHelper.setCamState(true);
                } catch (QBRTCSessionIsAbsentException e) {
                    Log.d(CALL_INTEGRATION, e.getMessage() + " setCamState to true");
                }
            }
        };
    }

    private Runnable initCamOffTask() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "CallActivity. initCamOffTask lunched");
                try {
                    videoChatHelper.setCamState(false);
                } catch (QBRTCSessionIsAbsentException e) {
                    Log.d(CALL_INTEGRATION, e.getMessage() + " setCamState to false");
                }
            }
        };
    }

    private Runnable initSwitchSpeakerTask() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "CallActivity. initSwitchSpeakerTask lunched");
                try {
                    videoChatHelper.switchMic();
                } catch (QBRTCSessionIsAbsentException e) {
                    Log.d(CALL_INTEGRATION, e.getMessage() + " switchMic");
                }
            }
        };
    }


    private Runnable initSwitchCamTask() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "CallActivity. initSwitchCamTask lunched");
                try {
                    videoChatHelper.switchCam(null);
                } catch (QBRTCSessionIsAbsentException e) {
                    Log.d(CALL_INTEGRATION, e.getMessage() + " switchCam");
                }
            }
        };
    }

    private Runnable initHangUpCallTask() {
        return new Runnable() {
            @Override
            public void run() {
                Log.d(CALL_INTEGRATION, "CallActivity. initHangUpCallTask lunched");
                try {
                    videoChatHelper.hangUpCall(userInfo);
                } catch (QBRTCSessionIsAbsentException e) {
                    finishActivitiForAbsentSession();
                    Log.d(CALL_INTEGRATION, e.getMessage() + " hangUpCall");
                }
            }
        };
    }

    private void finishActivitiForAbsentSession() {
        videoChatHelper.setVideoChatHelperState(QBVideoChatHelper.VideoHelperStates.WAIT_FOR_CALL);
        CallActivity.this.finish();
    }

    private Runnable initRemoteVideoTrackTask(final QBRTCVideoTrack videoTrack) {
        return new Runnable() {
            @Override
            public void run() {
                initRemoteVideoTrack(videoTrack);
            }
        };
    }

    private Runnable initLocalVideoTrackTask(final QBRTCVideoTrack videoTrack) {
        return new Runnable() {
            @Override
            public void run() {
                initLocalVideoTrack(videoTrack);
            }
        };
    }

    @Override
    public boolean isCanPerformLogoutInOnStop() {
        return false;
    }

    public QBGLVideoView getLocalVideoView() {
        return (QBGLVideoView) findViewById(R.id.localVideoView);
    }

    public void setLocalVideoView(QBGLVideoView videoView) {
        this.localVideoView = videoView;
        onLocalVideoViewCreated();
    }

    public QBGLVideoView getRemoteVideoView() {
        return (QBGLVideoView) findViewById(R.id.remoteVideoView);
    }

    public void setRemoteVideoView(QBGLVideoView videoView) {
        this.remoteVideoView = videoView;
        onRemoteVideoViewCreated();
    }

    public QBVideoChatHelper getVideoChatHelper() {
        return videoChatHelper;
    }

    private void initLocalVideoTrack(QBRTCVideoTrack videoTrack) {
        Log.d(CALL_INTEGRATION, "CallActivity. onLocalVideoTrackReceive");

        Log.d(CALL_INTEGRATION, "Video view is " + localVideoView);
        videoTrack.addRenderer(new VideoRenderer(new VideoCallBacks(getLocalVideoView(), QBGLVideoView.Endpoint.LOCAL)));
        getLocalVideoView().setVideoTrack(videoTrack, QBGLVideoView.Endpoint.LOCAL);
    }

    private void initRemoteVideoTrack(QBRTCVideoTrack videoTrack) {
        Log.d(CALL_INTEGRATION, "CallActivity. onRemoteVideoTrackReceive");

        Log.d(CALL_INTEGRATION, "Video view is " + remoteVideoView);
        videoTrack.addRenderer(new VideoRenderer(new VideoCallBacks(getRemoteVideoView(), QBGLVideoView.Endpoint.REMOTE)));
        getRemoteVideoView().setVideoTrack(videoTrack, QBGLVideoView.Endpoint.REMOTE);
    }

    enum VideoTracks {
        LOCAL_VIDEO_TRACK,
        REMOTE_VIDEO_TRACK
    }

}