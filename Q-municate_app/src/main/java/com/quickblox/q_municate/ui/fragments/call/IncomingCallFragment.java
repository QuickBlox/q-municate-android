package com.quickblox.q_municate.ui.fragments.call;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.call.CallActivity;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.call.RingtonePlayer;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IncomingCallFragment extends Fragment implements Serializable, View.OnClickListener {

    public static final String TAG = IncomingCallFragment.class.getSimpleName();

    private static final long CLICK_DELAY = TimeUnit.SECONDS.toMillis(2);
    private TextView incVideoCall;
    private TextView incAudioCall;
    private TextView callerName;
    private TextView otherIncUsers;
    private ImageButton rejectBtn;
    private ImageButton takeBtn;

    private ArrayList<Integer> opponents;
    private List<QBUser> opponentsFromCall = new ArrayList<>();
    private QBRTCSessionDescription sessionDescription;
    private Vibrator vibrator;
    private QBRTCTypes.QBConferenceType qbConferenceType;
    private View view;
    private long lastClickTime = 0l;
    private RingtonePlayer ringtonePlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);

        Log.d(TAG, "onCreate() from IncomeCallFragment");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getArguments() != null) {
            opponents = getArguments().getIntegerArrayList(QBServiceConsts.EXTRA_OPPONENTS);
            sessionDescription = (QBRTCSessionDescription) getArguments().getSerializable(QBServiceConsts.EXTRA_SESSION_DESCRIPTION);
            qbConferenceType = (QBRTCTypes.QBConferenceType) getArguments().getSerializable(QBServiceConsts.EXTRA_CONFERENCE_TYPE);

            Log.d(TAG, qbConferenceType.toString() + "From onCreateView()");
        }

        if (savedInstanceState == null) {

            view = inflater.inflate(R.layout.fragment_income_call, container, false);

            ((CallActivity) getActivity()).initActionBar();

            initUI(view);
            setDisplayedTypeCall(qbConferenceType);
            initButtonsListener();

        }

        ringtonePlayer = new RingtonePlayer(getActivity());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        startCallNotification();
    }

    public void onStop() {
        stopCallNotification();
        super.onDestroy();
        Log.d(TAG, "onDestroy() from IncomeCallFragment");
    }

    private void initButtonsListener() {
        rejectBtn.setOnClickListener(this);
        takeBtn.setOnClickListener(this);
    }

    private void initUI(View view) {

        incAudioCall = (TextView) view.findViewById(R.id.incAudioCall);
        incVideoCall = (TextView) view.findViewById(R.id.incVideoCall);

        callerName = (TextView) view.findViewById(R.id.callerName);
        callerName.setText(getCallerName(((CallActivity) getActivity()).getCurrentSession()));

        otherIncUsers = (TextView) view.findViewById(R.id.otherIncUsers);
        otherIncUsers.setText(getOtherIncUsersNames(opponents));

        rejectBtn = (ImageButton) view.findViewById(R.id.rejectBtn);
        takeBtn = (ImageButton) view.findViewById(R.id.takeBtn);
    }

    public void startCallNotification() {
        ringtonePlayer.play(false);

        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        long[] vibrationCycle = {0, 1000, 1000};
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(vibrationCycle, 1);
        }
    }

    private void stopCallNotification() {
        if (ringtonePlayer != null) {
            ringtonePlayer.stop();
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    private String getOtherIncUsersNames(ArrayList<Integer> opponents) {
        StringBuffer s = new StringBuffer("");
        opponents.remove(QBChatService.getInstance().getUser().getId());

        for (Integer i : opponents) {
            for (QBUser usr : opponentsFromCall) {
                if (usr.getId().equals(i)) {
                    if (opponents.indexOf(i) == (opponents.size() - 1)) {
                        s.append(usr.getFullName() + " ");
                        break;
                    } else {
                        s.append(usr.getFullName() + ", ");
                    }
                }
            }
        }
        return s.toString();
    }

    private String getCallerName(QBRTCSession session) {
        String s = new String();
        int i = session.getCallerID();

        opponentsFromCall.addAll(((CallActivity) getActivity()).getOpponentsList());

        for (QBUser usr : opponentsFromCall) {
            if (usr.getId().equals(i)) {
                s = usr.getFullName();
            }
        }
        return s;
    }

    private void setDisplayedTypeCall(QBRTCTypes.QBConferenceType conferenceType) {
        if (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(conferenceType)) {
            incVideoCall.setVisibility(View.VISIBLE);
            incAudioCall.setVisibility(View.INVISIBLE);
        } else if (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO.equals(conferenceType)) {
            incVideoCall.setVisibility(View.INVISIBLE);
            incAudioCall.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {

        if ((SystemClock.uptimeMillis() - lastClickTime) < CLICK_DELAY) {
            return;
        }
        lastClickTime = SystemClock.uptimeMillis();
        switch (v.getId()) {
            case R.id.rejectBtn:
                reject();
                break;
            case R.id.takeBtn:
                accept();
                break;
            default:
                break;
        }
    }

    private void accept() {
        takeBtn.setClickable(false);
        stopCallNotification();

        ((CallActivity) getActivity())
                .addConversationFragmentReceiveCall();
        ((CallActivity) getActivity())
                .startTimer();

        Log.d(TAG, "Call is started");
    }

    private void reject() {
        rejectBtn.setClickable(false);
        Log.d(TAG, "Call is rejected");

        stopCallNotification();

        ((CallActivity) getActivity()).rejectCurrentSession();
    }
}