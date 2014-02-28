package com.quickblox.qmunicate.ui.friend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;

public class FriendDetailsActivity extends BaseActivity {
    private ImageView imageViewFriendVideoCall;
    private ImageView imageViewFriendVoiceCall;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, FriendDetailsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int thisView = R.layout.activity_friend_details;
        setContentView(thisView);
        findViewById(this);

        actionBar.setDisplayHomeAsUpEnabled(true);
        imageViewFriendVideoCall.setOnClickListener(imageViewFriendVideoCallOnClickListener);
        imageViewFriendVoiceCall.setOnClickListener(imageViewFriendVoiceCallOnClickListener);
    }

    private void findViewById(Activity activity) {
        imageViewFriendVideoCall = (ImageView) activity.findViewById(R.id.imageViewFriendVideoCall);
        imageViewFriendVoiceCall = (ImageView) activity.findViewById(R.id.imageViewFriendVoiceCall);
    }

    View.OnClickListener imageViewFriendVideoCallOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FriendVideoCallActivity.startActivity(FriendDetailsActivity.this);
        }
    };

    View.OnClickListener imageViewFriendVoiceCallOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FriendVoiceCallActivity.startActivity(FriendDetailsActivity.this);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.friend_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_delete:
                // TODO delete user from friendlist
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
