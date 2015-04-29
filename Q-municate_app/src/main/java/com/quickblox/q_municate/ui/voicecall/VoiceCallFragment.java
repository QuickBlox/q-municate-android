package com.quickblox.q_municate.ui.voicecall;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.mediacall.OutgoingCallFragment;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.Consts;

public class VoiceCallFragment extends OutgoingCallFragment {

    @Override
    protected void initUI(View rootView) {
        super.initUI(rootView);
        if (opponent != null) {
            ((TextView) rootView.findViewById(R.id.name_textview)).setText(opponent.getFullName());
            RoundedImageView avatarView = (RoundedImageView) rootView.findViewById(R.id.avatar_imageview);
            avatarView.setOval(true);
            if (!TextUtils.isEmpty(opponent.getAvatarUrl())) {
                ImageLoader.getInstance().displayImage(opponent.getAvatarUrl(),
                        avatarView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
            }
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_voice_call;
    }

}
