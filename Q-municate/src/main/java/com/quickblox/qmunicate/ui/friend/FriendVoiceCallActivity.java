package com.quickblox.qmunicate.ui.friend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;

public class FriendVoiceCallActivity extends BaseActivity {
    private ImageButton imageButtonEndVoiceCall;
    private ImageButton imageButtonDynamicVoiceCall;
    private ImageButton imageButtonMuteVoiceCall;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, FriendVoiceCallActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int thisView = R.layout.activity_friend_voice_call;
        setContentView(thisView);
        findViewById(this);

        actionBar.hide();
        imageButtonEndVoiceCall.setOnClickListener(imageButtonEndVoiceCallOnClickListener);
    }

    private void findViewById(Activity activity) {
        imageButtonEndVoiceCall = (ImageButton) activity.findViewById(R.id.imageButtonEndVoiceCall);
        imageButtonDynamicVoiceCall = (ImageButton) activity.findViewById(R.id.imageButtonDynamicVoiceCall);
        imageButtonMuteVoiceCall = (ImageButton) activity.findViewById(R.id.imageButtonMuteVoiceCall);
    }

    View.OnClickListener imageButtonEndVoiceCallOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //
        }
    };
}
