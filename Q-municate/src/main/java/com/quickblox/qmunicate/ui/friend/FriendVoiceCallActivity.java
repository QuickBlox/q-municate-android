package com.quickblox.qmunicate.ui.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;

public class FriendVoiceCallActivity extends BaseActivity {
    private ImageButton imageButtonEndVoiceCall;
    private ImageButton imageButtonDynamicVoiceCall;
    private ImageButton imageButtonMuteVoiceCall;

    public static void start(Context context) {
        Intent intent = new Intent(context, FriendVoiceCallActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int thisView = R.layout.activity_friend_voice_call;
        setContentView(thisView);
        initUI();
    }

    private void initUI() {
        imageButtonEndVoiceCall = (ImageButton) findViewById(R.id.imageButtonEndVoiceCall);
        imageButtonDynamicVoiceCall = (ImageButton) findViewById(R.id.imageButtonDynamicVoiceCall);
        imageButtonMuteVoiceCall = (ImageButton) findViewById(R.id.imageButtonMuteVoiceCall);
        actionBar.hide();
    }
}
