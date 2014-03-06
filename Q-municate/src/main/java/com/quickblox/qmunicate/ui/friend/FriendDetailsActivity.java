package com.quickblox.qmunicate.ui.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.videocall.VideoCallActivity;
import com.quickblox.qmunicate.ui.voicecall.VoiceCallActivity;

public class FriendDetailsActivity extends BaseActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, FriendDetailsActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);
        initUI();
    }

    private void initUI() {
    }

    public void startFriendVideoCallActivityOnClick(View view) {
        VideoCallActivity.start(FriendDetailsActivity.this);
    }

    public void startFriendVoiceCallActivityOnClick(View view) {
        VoiceCallActivity.start(FriendDetailsActivity.this);
    }

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
