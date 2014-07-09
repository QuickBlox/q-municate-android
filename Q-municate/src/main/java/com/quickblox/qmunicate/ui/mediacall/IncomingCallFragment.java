package com.quickblox.qmunicate.ui.mediacall;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ErrorUtils;


public class IncomingCallFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = IncomingCallFragment.class.getSimpleName();
    private WebRTC.MEDIA_STREAM callType;

    private IncomingCallClickListener incomingCallClickListener;
    private Friend friend;

    public interface IncomingCallClickListener {

        public void onAcceptClick();

        public void onDenyClick();
    }

    public static IncomingCallFragment newInstance(WebRTC.MEDIA_STREAM callType, Friend friend) {
        IncomingCallFragment fragment = new IncomingCallFragment();
        Bundle args = new Bundle();
        args.putSerializable(Consts.CALL_TYPE_EXTRA, callType);
        args.putSerializable(Consts.EXTRA_FRIEND, friend);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_popup_call, container, false);
        callType = (WebRTC.MEDIA_STREAM) getArguments().getSerializable(Consts.CALL_TYPE_EXTRA);
        friend = (Friend) getArguments().getSerializable(Consts.EXTRA_FRIEND);
        boolean isVideoCall = WebRTC.MEDIA_STREAM.VIDEO.equals(callType);
        ((ImageButton) rootView.findViewById(R.id.acceptCallButton)).setImageResource(
                isVideoCall ? R.drawable.ic_video : R.drawable.ic_call);
        ((TextView) rootView.findViewById(R.id.callTextView)).setText(
                isVideoCall ? R.string.cll_incoming_call_video : R.string.cll_incoming_call_audio);
        ((TextView) rootView.findViewById(R.id.name_textview)).setText(friend.getFullname());
        if(!TextUtils.isEmpty(friend.getAvatarUrl())){
            ImageLoader.getInstance().displayImage(friend.getAvatarUrl(),
                    (android.widget.ImageView) rootView.findViewById(R.id.avatar_imageview), Consts.UIL_AVATAR_DISPLAY_OPTIONS);
        }
        rootView.findViewById(R.id.acceptCallButton).setOnClickListener(this);
        rootView.findViewById(R.id.denyCallButton).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            incomingCallClickListener = (IncomingCallClickListener) activity;
        } catch (ClassCastException e) {
            ErrorUtils.logError(TAG, e);
        }
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
                accept();
                break;
            case R.id.denyCallButton:
                reject();
                break;
            default:
                break;
        }
    }

    private void reject() {
        if (incomingCallClickListener != null) {
            incomingCallClickListener.onDenyClick();
        }
    }


    private void accept() {
        if (incomingCallClickListener != null) {
            incomingCallClickListener.onAcceptClick();
        }
    }
}