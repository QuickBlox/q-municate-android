package com.quickblox.q_municate.ui.mediacall;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;

/**
 * Fragments contains UI of incoming call.
 * Throws callbacks to activity about user actions (accept or reject call)
 */


public class IncomingCallFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = IncomingCallFragment.class.getSimpleName();
    private IncomingCallActionsListener incomingCallClickListener;
    private User friend;
    private boolean isVideoCall;
    private QBRTCTypes.QBConferenceType callType;
    private QBRTCSessionDescription sessionDescription;
    private ImageButton acceptCallButton;
    private ImageButton denyCallButton;


    public static IncomingCallFragment newInstance(QBRTCTypes.QBConferenceType callType, User friend) {
        IncomingCallFragment fragment = new IncomingCallFragment();
        Bundle args = new Bundle();
        args.putSerializable(ConstsCore.CALL_TYPE_EXTRA, callType);
        args.putSerializable(ConstsCore.EXTRA_FRIEND, friend);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            incomingCallClickListener = (IncomingCallActionsListener) activity;
        } catch (ClassCastException e) {
            ErrorUtils.logError(TAG, e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_popup_call, container, false);
        parseIncomeParameters();
        initUI(rootView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((CallActivity) getActivity()).startIncomeCallTimer();
    }

    private void parseIncomeParameters() {

        if (getArguments() != null) {
            sessionDescription = (QBRTCSessionDescription) getArguments().getSerializable("sessionDescription");
            friend = (User) getArguments().getSerializable(ConstsCore.EXTRA_FRIEND);
            callType = (QBRTCTypes.QBConferenceType) getArguments().getSerializable(ConstsCore.CALL_TYPE_EXTRA);
        }
    }

    private void initUI(View rootView) {
        User friendFromDB = UsersDatabaseManager.getUserById(getActivity().getBaseContext(), friend.getUserId());

        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(callType);
        ((ImageButton) rootView.findViewById(R.id.acceptCallButton)).setImageResource(
                isVideoCall ? R.drawable.ic_video : R.drawable.ic_call);
        ((TextView) rootView.findViewById(R.id.callTextView)).setText(
                isVideoCall ? R.string.cll_incoming_call_video : R.string.cll_incoming_call_audio);
        if (friendFromDB != null) {
            ((TextView) rootView.findViewById(R.id.name_textview)).setText(/*friend*/friendFromDB.getFullName());
            RoundedImageView avatarView = (RoundedImageView) rootView.findViewById(R.id.avatar_imageview);
            avatarView.setOval(true);
            if (!TextUtils.isEmpty(/*friend*/friendFromDB.getAvatarUrl())) {
                ImageLoader.getInstance().displayImage(/*friend*/friendFromDB.getAvatarUrl(),
                        avatarView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
            }
        } else {
            ((TextView) rootView.findViewById(R.id.name_textview)).setText(getString(R.string.user_was_not_found_in_db));
        }

        acceptCallButton = (ImageButton) rootView.findViewById(R.id.acceptCallButton);
        acceptCallButton.setOnClickListener(this);
        denyCallButton = (ImageButton) rootView.findViewById(R.id.denyCallButton);
        denyCallButton.setOnClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        incomingCallClickListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.acceptCallButton:
                Log.d(TAG, "Accept call was clicked");
                setEnabledAllButtons(false);
                accept();
                break;
            case R.id.denyCallButton:
                Log.d(TAG, "Deny call was clicked");
                ((CallActivity) getActivity()).stopIncomeCallTimer();
                setEnabledAllButtons(false);
                reject();
                break;
            default:
                break;
        }
    }

    private void setEnabledAllButtons(boolean enabled) {
        acceptCallButton.setEnabled(enabled);
        denyCallButton.setEnabled(enabled);
    }

    // -----------------   Throws callbacks methods (IncomingCallFragmentInterface) --------- //
    private void reject() {
        if (incomingCallClickListener != null) {
            incomingCallClickListener.rejectCallClick();
        }
    }


    private void accept() {
        if (incomingCallClickListener != null) {
            incomingCallClickListener.acceptCallClick();
        }
    }
}