package com.quickblox.q_municate.ui.mediacall;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.ActivityHelper;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.media.MediaPlayerManager;
import com.quickblox.q_municate.ui.videocall.VideoCallFragment;
import com.quickblox.q_municate.ui.voicecall.VoiceCallFragment;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.push.QBSendPushCommand;
import com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper;
import com.quickblox.q_municate_core.qb.helpers.call.VideoChatHelperListener;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;
import com.quickblox.videochat.webrtc.view.VideoCallBacks;

import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class CallActivity extends BaseLogeableActivity implements IncomingCallFragmentInterface, OutgoingCallFragmentInterface, VideoChatHelperListener {

    private static final String TAG = CallActivity.class.getSimpleName();
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

    private static final Object INCOMING_VIDEO_FRAGMENT = "incoming_video_fragment";
    private static final Object OUTGOING_AUDIO_FRAGMENT = "outgoing_audio_fragment";
    private static final Object OUTGOING_VIDEO_FRAGMENT = "outgoing_video_fragment";

    private User opponent;
    private ConstsCore.CALL_DIRECTION_TYPE call_direction_type;
    private QBRTCTypes.QBConferenceType call_type;
    private MediaPlayerManager mediaPlayer;
    private String sessionId;
    private QBVideoChatHelper videoChatHelper;
    private Map<String, String> userInfo = new HashMap<String, String>();
    private boolean bounded;
    private ActivityHelper.ServiceConnectionListener serviceConnectionListener;
    private Handler callTasksHandler;
    private List<Runnable> callTasksQueue;
    private HashMap<String, Runnable> callTasksMap;
    private VideoRenderer.Callbacks LOCAL_RENDERER;
    private VideoRenderer.Callbacks REMOTE_RENDERER;
    private boolean isCleintReadyAccept;
    private static boolean callInProcess;
    private QBGLVideoView videoView;
    private Handler showIncomingCallWindowTaskHandler;
    private Runnable showIncomingCallWindowTask;


    public void startCall() {
        Runnable callTask = callTasksMap.get(START_CALL_TASK);
        executeCallTask(callTask);
    }


    /* ---------------------------   Implements methods   ===========================*/

    // Income call methods

    @Override
    public void acceptCallClick() {
        cancelPlayer();
        if (isCleintReadyAccept) {
            Log.d("CALL_INTEGRATION", "CallActivity. acceptCall() executed");
            Runnable acceptTask = callTasksMap.get(ACCEPT_CALL_TASK);
            executeCallTask(acceptTask);
        } else {
            showOutgoingFragment();
        }
    }

    @Override
    public void rejectCallClick() {
        cancelPlayer();
        Log.d("CALL_INTEGRATION", "CallActivity. rejectCall() executed");
        Runnable rejectTask = callTasksMap.get(REJECT_CALL_TASK);
        executeCallTask(rejectTask);
    }

    // Outgoing call methods

    @Override
    public void onMic() {
        Log.d("CALL_INTEGRATION", "CallActivity. onMic() executed");
        Runnable onMicTask = callTasksMap.get(ON_MIC_TASK);
        executeCallTask(onMicTask);
    }

    @Override
    public void offMic() {
        Log.d("CALL_INTEGRATION", "CallActivity. offMic() executed");
        Runnable offMicTask = callTasksMap.get(OFF_MIC_TASK);
        executeCallTask(offMicTask);
    }

    @Override
    public void onCam() {
        Log.d("CALL_INTEGRATION", "CallActivity. onCam() executed");
        Runnable onCamTask = callTasksMap.get(ON_CAM_TASK);
        executeCallTask(onCamTask);
    }

    @Override
    public void offCam() {
        Log.d("CALL_INTEGRATION", "CallActivity. offCam() executed");
        Runnable offCamTask = callTasksMap.get(OFF_CAM_TASK);
        executeCallTask(offCamTask);
    }

    @Override
    public void switchCam() {
        Log.d("CALL_INTEGRATION", "CallActivity. switchCam() executed");
        Runnable switchCamTask = callTasksMap.get(SWITCH_CAM_TASK);
        executeCallTask(switchCamTask);
    }

    @Override
    public void switchSpeaker() {
        Log.d("CALL_INTEGRATION", "CallActivity. switchSpeaker() executed");
        Runnable switchSpeakerTask = callTasksMap.get(SWITCH_SPEACKER_TASK);
        executeCallTask(switchSpeakerTask);
    }

    @Override
    public void hungUpClick() {
        Log.d("CALL_INTEGRATION", "CallActivity. hungUp() executed");
        Runnable hungUpTask = callTasksMap.get(HANG_UP_CALL_TASK);
        executeCallTask(hungUpTask);
    }

    public static void start(Context context, User friend, QBRTCTypes.QBConferenceType callType) {
        Log.d("CALL_INTEGRATION", "CallActivity. START STATIC CALL ACTIVITY");
        Log.i(TAG, "Friend.isOnline() = " + friend.isOnline());
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(ConstsCore.EXTRA_FRIEND, friend);
        intent.putExtra(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, ConstsCore.CALL_DIRECTION_TYPE.OUTGOING);
        intent.putExtra(ConstsCore.CALL_TYPE_EXTRA, callType);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canPerformLogout.set(false);
        setContentView(R.layout.activity_main_call);
        actionBar.hide();
        mediaPlayer = App.getInstance().getMediaPlayer();

        callTasksHandler = new Handler();
        callTasksQueue = new LinkedList<>();

        parseIntentExtras(getIntent().getExtras());

        initCallTasksMap();

        addAction(QBServiceConsts.SEND_PUSH_MESSAGES_FAIL_ACTION, failAction);
    }

    private void initCallTasksMap() {
        Log.d("CALL_INTEGRATION", "CallActivity. Set up tasks map");

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

        Log.d("CALL_INTEGRATION", "CallActivity. Set up tasks map finished");
    }

    private void executeCallTask(Runnable runnable) {
        if (videoChatHelper != null) {
//            callTasksHandler.post(callTask);
            runOnUiThread(runnable);
        } else {
            callTasksQueue.add(runnable);
        }
    }

    @Override
    protected void onResume() {

        Log.d("CALL_INTEGRATION", "Resume call activity");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("CALL_INTEGRATION", "Pause call activity");
        super.onPause();
    }


    @Override
    protected void onStop() {
        Log.d("CALL_INTEGRATION", "Stop call activity");
        super.onStop();
//        if (videoChatHelper != null) {
//            videoChatHelper.removeVideoChatHelperListener(this);
//        }
    }

    @Override
    protected void onDestroy() {
        Log.d("CALL_INTEGRATION", "Destroy call activity");
        cancelPlayer();
        super.onDestroy();

        if (videoChatHelper != null) {
            videoChatHelper.removeVideoChatHelperListener(this);
        }

        if(QBRTCClient.isInitiated()){
           QBRTCClient.getInstance().close();
        }

        videoChatHelper.disposeAllResources();

    }

    public void initIncommingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                if (currentFragment instanceof IncomingCallFragment) {
                    getVideoChatHelper().rejectCall(new HashMap<String, String>());
                } else {
                    getVideoChatHelper().hangUpCall(new HashMap<String, String>());
                }
            }
        };
    }

    public void startIncomeCallTimer() {
        Log.d("CALL_INTEGRATION", "Start stop Income call timer");
        if (showIncomingCallWindowTaskHandler == null){
            initIncommingCallTask();
        }
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + TimeUnit.SECONDS.toMillis(QBRTCConfig.getAnswerTimeInterval()));
    }

    public void stopIncomeCallTimer() {
        Log.d("CALL_INTEGRATION", "Start stop Income call timer");
        if(showIncomingCallWindowTaskHandler != null) {
            showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
            showIncomingCallWindowTaskHandler = null;
        }
    }

    protected void cancelPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stopPlaying();
        }
    }

    private void parseIntentExtras(Bundle extras) {
        call_direction_type = (ConstsCore.CALL_DIRECTION_TYPE) extras.getSerializable(
                ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
        call_type = (QBRTCTypes.QBConferenceType) extras.getSerializable(ConstsCore.CALL_TYPE_EXTRA);
        sessionId = extras.getString("sessionId", "");      //надо добавить константу
        opponent = (User) extras.getSerializable(ConstsCore.EXTRA_FRIEND);

        Log.i(TAG, "opponentId=" + opponent);
    }

    private void notifyFriendOnCall(User friend) {
        if (!friend.isOnline()) {
            String callMsg = getResources().getString(R.string.dlg_offline_call,
                    AppSession.getSession().getUser().getFullName());
            QBSendPushCommand.start(this, callMsg, friend.getUserId());
        }
    }

    private void showOutgoingFragment() {
        Log.d("CALL_INTEGRATION", "CallActivity. showOutgoingFragment");

        if (call_direction_type.equals(ConstsCore.CALL_DIRECTION_TYPE.OUTGOING)) {
            playOutgoingRingtone();
        }

//        playOutgoingRingtone();
        OutgoingCallFragment outgoingCallFragment = (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(
                call_type)) ? new VideoCallFragment() : new VoiceCallFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, call_direction_type);
        bundle.putSerializable(ConstsCore.EXTRA_FRIEND, opponent);
        bundle.putSerializable(ConstsCore.CALL_TYPE_EXTRA, call_type);
        outgoingCallFragment.setArguments(bundle);

        setCurrentFragment(outgoingCallFragment);


//        if (outgoingCallFragment instanceof VideoCallFragment) {
//            setCurrentFragment(outgoingCallFragment);
//        } else {
//            setCurrentFragment(outgoingCallFragment);
//        }
    }


    // TODO REVIEW REQUIREMENT OF THIS METHOD
    private void showOutgoingFragment(User opponentId,
                                      QBRTCTypes.QBConferenceType callType, String sessionId) {

        Log.d("CALL_INTEGRATION", "sCallActivity. showOutgoingFragment");
// TODO WHY JUST FOR VIDEO FRAGMENT BUNDLE ARE USING
        Bundle bundle = VideoCallFragment.generateArguments(opponentId,
                call_direction_type, callType, sessionId);

        OutgoingCallFragment outgoingCallFragment = (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(
                call_type)) ? new VideoCallFragment() : new VoiceCallFragment();
        outgoingCallFragment.setArguments(bundle);

        if (outgoingCallFragment instanceof VideoCallFragment) {
            setCurrentFragment(outgoingCallFragment);
        } else {
            setCurrentFragment(outgoingCallFragment);
        }
    }

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


    private void showIncomingFragment() {
        Log.d("CALL_INTEGRATION", "CallActivity. showIncomingFragment");
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


    @Override
    public void onReceiveNewSession(QBRTCSession qbrtcSession) {
        Log.d("CALL_INTEGRATION", "CallActivity. onReceiveNewSession");
    }

    @Override
    public void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer) {
        Log.d("CALL_INTEGRATION", "CallActivity. onUserNotAnswer");
        showToastMessage(getString(R.string.user_not_answer));
    }

    @Override
    public void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> stringStringMap) {
        Log.d("CALL_INTEGRATION", "CallActivity. onCallRejectByUser");
        showToastMessage(getString(R.string.user_reject_call));
    }

    @Override
    public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer) {
        Log.d("CALL_INTEGRATION", "CallActivity. onReceiveHangUpFromUser");
        showToastMessage(getString(R.string.user_hang_up_call));
    }

    @Override
    public void onSessionClosed(QBRTCSession qbrtcSession) {
        Log.d("CALL_INTEGRATION", "CallActivity. onSessionClosed");
        showToastMessage(getString(R.string.session_closed));
        finish();
    }

    @Override
    public void onSessionStartClose(QBRTCSession qbrtcSession) {
        Log.d("CALL_INTEGRATION", "CallActivity. onSessionStartClose");
        showToastMessage(getString(R.string.session_start_close));
    }

    @Override
    public void onLocalVideoTrackReceive(QBRTCSession session, final QBRTCVideoTrack videoTrack) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. onLocalVideoTrackReceive");
                showToastMessage(getString(R.string.local_video_track_received));
//                videoTrack.addRenderer(new VideoRenderer(LOCAL_RENDERER));
//                Log.d("CALL_INTEGRATION", "Video view is " + videoView);
                videoTrack.addRenderer(new VideoRenderer(new VideoCallBacks(getVideoView(), QBGLVideoView.Endpoint.LOCAL)));
                getVideoView().setVideoTrack(videoTrack, QBGLVideoView.Endpoint.LOCAL);

            }
        });
    }

    @Override
    public void onRemoteVideoTrackReceive(QBRTCSession session, final QBRTCVideoTrack videoTrack, Integer userID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. onRemoteVideoTrackReceive");
                showToastMessage(getString(R.string.remote_video_track_received));
//                videoTrack.addRenderer(new VideoRenderer(REMOTE_RENDERER));
//                Log.d("CALL_INTEGRATION", "Video view is " + videoView);
                videoTrack.addRenderer(new VideoRenderer(new VideoCallBacks(getVideoView(), QBGLVideoView.Endpoint.REMOTE)));
                getVideoView().setVideoTrack(videoTrack, QBGLVideoView.Endpoint.REMOTE);
            }
        });
    }

    @Override
    public void onClientReady() {
        Log.d("CALL_INTEGRATION", "CallActivity. onClientReady");
        isCleintReadyAccept = true;
        acceptCallClick();
    }

    @Override
    public void onConnectedToService(QBService service) {
        super.onConnectedToService(service);

        if (videoChatHelper == null) {

            Log.d("CALL_INTEGRATION", "CallActivity. onConnectedToService");
            videoChatHelper = (QBVideoChatHelper) service.getHelper(QBService.VIDEO_CHAT_HELPER);
            videoChatHelper.addVideoChatHelperListener(CallActivity.this);


            if (call_direction_type != null) {
                if (ConstsCore.CALL_DIRECTION_TYPE.INCOMING.equals(call_direction_type)) {
                    showIncomingFragment();
                } else {
                    notifyFriendOnCall(opponent);
                    showOutgoingFragment();
                }
            }

            executeScheduledTasks();

            Log.d("CALL_INTEGRATION", "CallActivity. QBRTCClient start listening calls");
            QBRTCClient.getInstance().prepareToProcessCalls(this);
//            if(call_direction_type == ConstsCore.CALL_DIRECTION_TYPE.INCOMING){
//                QBRTCClient.
//            }
        }
    }

    private void executeScheduledTasks() {
        Log.d("CALL_INTEGRATION", "CallActivity. executeScheduledTasks");
        for (Runnable task : callTasksQueue) {
//            callTasksHandler.post(task);
            runOnUiThread(task);
        }
    }


    // --------------------------------  TASK LIST  --------------------------------- //

    private Runnable initStartCallTask() {
        final List<Integer> opponents = new ArrayList<>();
        opponents.add(opponent.getUserId());
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initStartCallTask lunched");
                videoChatHelper.startCall(userInfo, opponents, call_type);
            }
        };
        return task;
    }

    private Runnable initAcceptCallTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initAcceptCallTask lunched");
                videoChatHelper.acceptCall(userInfo);
//                showOutgoingFragment(opponent, call_type, sessionId);
            }
        };
        return task;
    }


    private Runnable initRejectCallTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initRejectCallTask lunched");
//                cancelPlayer();
                videoChatHelper.rejectCall(userInfo);
                finish();
            }
        };
        return task;
    }

    private Runnable initMicOffTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initMicOffTask lunched");
                videoChatHelper.setMicState(false);
            }
        };
        return task;
    }

    private Runnable initMicOnTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initMicOnTask lunched");
                videoChatHelper.setMicState(true);
            }
        };
        return task;
    }

    private Runnable initCamOnTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initCamOnTask lunched");
                videoChatHelper.setCamState(true);
            }
        };
        return task;
    }

    private Runnable initCamOffTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initCamOffTask lunched");
                videoChatHelper.setCamState(false);
            }
        };
        return task;
    }

    private Runnable initSwitchSpeakerTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initSwitchSpeakerTask lunched");
                videoChatHelper.switchMic();
            }
        };
        return task;
    }


    private Runnable initSwitchCamTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initSwitchCamTask lunched");
                videoChatHelper.switchCam();
            }
        };
        return task;
    }

    private Runnable initHangUpCallTask() {
        Runnable task = new Runnable() {
            @Override
            public void run() {
                Log.d("CALL_INTEGRATION", "CallActivity. initHangUpCallTask lunched");
                videoChatHelper.hangUpCall(userInfo);
                finish();
            }
        };
        return task;
    }

    @Override
    public boolean isCanPerformLogoutInOnStop() {
        return false;
    }

    public QBGLVideoView getVideoView() {
        return  (QBGLVideoView) findViewById(R.id.videoScreenImageView);
    }

    public void setVideoView(QBGLVideoView videoView) {
        this.videoView = videoView;
    }

    public QBVideoChatHelper getVideoChatHelper() {
        return videoChatHelper;
    }
}