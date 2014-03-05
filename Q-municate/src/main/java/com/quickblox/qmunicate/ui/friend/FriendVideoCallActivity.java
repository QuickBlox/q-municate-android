package com.quickblox.qmunicate.ui.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;

public class FriendVideoCallActivity extends BaseActivity {
    private ImageButton imageButtonEndVideoCall;

    public static void start(Context context) {
        Intent intent = new Intent(context, FriendVideoCallActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_video_call);
        initUI();
    }

    private void initUI() {
        imageButtonEndVideoCall = _findViewById(R.id.imageButtonEndVideoCall);
        actionBar.hide();
    }
}