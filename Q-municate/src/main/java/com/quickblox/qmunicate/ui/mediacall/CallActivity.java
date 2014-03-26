package com.quickblox.qmunicate.ui.mediacall;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat.model.objects.CallType;
import com.quickblox.module.videochat_webrtc.SignalingChannel;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.media.MediaPlayerManager;
import com.quickblox.qmunicate.ui.utils.Consts;
import com.quickblox.qmunicate.ui.videocall.VideoCallFragment;
import com.quickblox.qmunicate.ui.voicecall.VoiceCallFragment;

public class CallActivity extends BaseActivity implements IncomingCallFragment.IncomingCallClickListener, OutgoingCallFragment.OutgoingCallListener {

    private static final String TAG = CallActivity.class.getSimpleName();
    private QBUser opponent;
    private Consts.CALL_DIRECTION_TYPE call_direction_type;
    private Fragment currentFragment;
    private SessionDescriptionWrapper sessionDescriptionWrapper;
    private CallType call_type;
    private SignalingChannel signalingChannel;
    private MediaPlayerManager mediaPlayer;

    public static void start(Context context, QBUser friend, CallType callType) {
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(Consts.USER, friend);
        intent.putExtra(Consts.CALL_DIRECTION_TYPE_EXTRA, Consts.CALL_DIRECTION_TYPE.OUTGOING);
        intent.putExtra(Consts.CALL_TYPE_EXTRA, callType);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onAcceptClick() {
        accept();
    }

    @Override
    public void onDenyClick() {
        reject();
    }

    @Override
    public void onConnectionAccepted() {
        cancelPlayer();
    }

    @Override
    public void onConnectionRejected() {
        cancelPlayer();
        finish();
    }

    @Override
    public void onConnectionClosed() {
        cancelPlayer();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_call);
        actionBar.hide();
        mediaPlayer = App.getInstance().getMediaPlayer();
        parseIntentExtras(getIntent().getExtras());
    }

    @Override
    protected void onDestroy() {
        cancelPlayer();
        super.onDestroy();
    }

    @Override
    protected void onConnectedToService() {
        signalingChannel = service.getQbChatHelper().getSignalingChannel();
    }

    private void setCurrentFragment(Fragment fragment) {
        Log.i(TAG, "setCurrentFragment" + fragment.toString());
        currentFragment = fragment;
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = buildTransaction();
        transaction.replace(R.id.container, fragment, null);
        transaction.commit();
    }

    private void cancelPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stopPlaying();
        }
    }

    private void reject() {
        if (signalingChannel != null && opponent != null) {
            signalingChannel.sendReject(opponent, System.currentTimeMillis());
        }
        cancelPlayer();
        finish();
    }

    private void accept() {
        cancelPlayer();
        showOutgoingFragment(sessionDescriptionWrapper, opponent, call_type);
    }

    private FragmentTransaction buildTransaction() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(android.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        return transaction;
    }

    private void parseIntentExtras(Bundle extras) {
        call_direction_type = (Consts.CALL_DIRECTION_TYPE) extras.getSerializable(Consts.CALL_DIRECTION_TYPE_EXTRA);
        call_type = (CallType) extras.getSerializable(Consts.CALL_TYPE_EXTRA);
        Log.i(TAG, "call_direction_type=" + call_direction_type);
        opponent = (QBUser) extras.getSerializable(Consts.USER);
        if (call_direction_type != null) {
            if (Consts.CALL_DIRECTION_TYPE.INCOMING.equals(call_direction_type)) {
                sessionDescriptionWrapper =
                        extras.getParcelable(Consts.REMOTE_DESCRIPTION);
                showIncomingFragment();
            } else {
                showOutgoingFragment();
            }
        }
        Log.i(TAG, "opponentId=" + opponent);
    }

    private void showOutgoingFragment() {
        playOutgoingRingtone();
        OutgoingCallFragment outgoingCallFragment = CallType.VIDEO_AUDIO.equals(call_type) ? new VideoCallFragment() :
                new VoiceCallFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Consts.CALL_DIRECTION_TYPE_EXTRA, call_direction_type);
        bundle.putSerializable(Consts.USER, opponent);
        bundle.putSerializable(Consts.CALL_TYPE_EXTRA, call_type);
        outgoingCallFragment.setArguments(bundle);
        setCurrentFragment(outgoingCallFragment);
    }

    private void playOutgoingRingtone() {
        if (mediaPlayer != null) {
            mediaPlayer.playSound("ringback.wav", true);
        }
    }

    private void playIncomingRingtone() {
        if (mediaPlayer != null) {
            mediaPlayer.playDefaultRingTone();
        }
    }

    private void showOutgoingFragment(SessionDescriptionWrapper sessionDescriptionWrapper, QBUser opponentId, CallType callType) {
        Bundle bundle = VideoCallFragment.generateArguments(sessionDescriptionWrapper, opponentId, call_direction_type, callType);
        OutgoingCallFragment outgoingCallFragment = CallType.VIDEO_AUDIO.equals(call_type) ? new VideoCallFragment() :
                new VoiceCallFragment();
        outgoingCallFragment.setArguments(bundle);
        setCurrentFragment(outgoingCallFragment);
    }

    private void showIncomingFragment() {
        playIncomingRingtone();
        IncomingCallFragment incomingCallFragment = IncomingCallFragment.newInstance(call_type, opponent.getFullName());
        setCurrentFragment(incomingCallFragment);
    }

}
