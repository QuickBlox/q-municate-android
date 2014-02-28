package com.quickblox.qmunicate.ui.friend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;

public class FriendVideoCallActivity extends BaseActivity {
    private ImageButton imageButtonEndVideoCall;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, FriendVideoCallActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int thisView = R.layout.activity_friend_video_call;
        setContentView(thisView);
        findViewById(this);

        actionBar.hide();
        imageButtonEndVideoCall.setOnClickListener(viewEndCallOnClickListener);
    }

    private void findViewById(Activity activity) {
        imageButtonEndVideoCall = (ImageButton) activity.findViewById(R.id.imageButtonEndVideoCall);
    }

    View.OnClickListener viewEndCallOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //
        }
    };
}