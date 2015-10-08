package com.quickblox.q_municate.ui.fragments.call;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.fragments.call.OutgoingCallFragment;

// TODO need to refactor
@Deprecated
public class VideoCallFragment extends OutgoingCallFragment {

    @Override
    protected int getContentView() {
        return R.layout.activity_video_call;
    }
}
