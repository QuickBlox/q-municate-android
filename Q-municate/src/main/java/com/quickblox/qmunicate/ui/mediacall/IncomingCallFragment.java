package com.quickblox.qmunicate.ui.mediacall;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.utils.Consts;


public class IncomingCallFragment extends BaseFragment implements View.OnClickListener {

    private WebRTC.MEDIA_STREAM callType;

    private IncomingCallClickListener incomingCallClickListener;
    private String userName;

    public interface IncomingCallClickListener {

        public void onAcceptClick();

        public void onDenyClick();
    }

    public static IncomingCallFragment newInstance(WebRTC.MEDIA_STREAM callType, String userName) {
        IncomingCallFragment fragment = new IncomingCallFragment();
        Bundle args = new Bundle();
        args.putSerializable(Consts.CALL_TYPE_EXTRA, callType);
        args.putSerializable(Consts.USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_popup_call, container, false);
        callType = (WebRTC.MEDIA_STREAM) getArguments().getSerializable(Consts.CALL_TYPE_EXTRA);
        userName = getArguments().getString(Consts.USER_NAME, "");
        boolean isVideoCall = WebRTC.MEDIA_STREAM.VIDEO.equals(callType);
        ((ImageButton) rootView.findViewById(R.id.acceptCallButton)).setImageResource(
                isVideoCall ? R.drawable.ic_video : R.drawable.ic_call);
        ((TextView) rootView.findViewById(R.id.callTextView)).setText(
                isVideoCall ? R.string.cll_incoming_call_video : R.string.cll_incoming_call_audio);
        ((TextView) rootView.findViewById(R.id.name_textview)).setText(userName);
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
            e.printStackTrace();
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