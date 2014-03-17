package com.quickblox.qmunicate.ui.friend;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.receiver.BaseBroadcastReceiver;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.QBLoadImageTask;
import com.quickblox.qmunicate.qb.command.QBRemoveFriendCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.LoaderActivity;
import com.quickblox.qmunicate.ui.dialogs.ConfirmDialog;
import com.quickblox.qmunicate.ui.videocall.VideoCallActivity;
import com.quickblox.qmunicate.ui.voicecall.VoiceCallActivity;

import java.util.Timer;
import java.util.TimerTask;

public class FriendDetailsActivity extends LoaderActivity<Friend> {

    public static final String EXTRA_FRIEND = "Friend";

    private static final int START_DELAY = 0;
    private static final int UPDATE_DATA_PERIOD = 300000;

    private ImageView avatarImageView;
    private TextView nameTextView;
    private ImageView onlineImageView;
    private TextView onlineStatusTextView;
    private TextView phoneTextView;
    private View phoneView;

    private Friend friend;
    private Timer friendUpdateTimer;

    public static void start(Context context, Friend friend) {
        Intent intent = new Intent(context, FriendDetailsActivity.class);
        intent.putExtra(EXTRA_FRIEND, friend);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);

        avatarImageView = _findViewById(R.id.avatarImageView);
        nameTextView = _findViewById(R.id.nameTextView);
        onlineImageView = _findViewById(R.id.onlineImageView);
        onlineStatusTextView = _findViewById(R.id.onlineStatusTextView);
        phoneTextView = _findViewById(R.id.phoneTextView);
        phoneView = _findViewById(R.id.phoneView);

        registerReceiver(new RemoveFriendBroadcastReceiver(), QBServiceConsts.REMOVE_FRIEND_RESULT);
        friend = (Friend) getIntent().getExtras().getSerializable(EXTRA_FRIEND);

        fillUI(friend);
    }

    @Override
    public void onStart() {
        super.onStart();
        startLoaderWithTimer();
    }

    @Override
    public void onStop() {
        friendUpdateTimer.cancel();
        super.onStop();
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
                navigateToParent();
                return true;
            case R.id.action_delete:
                showRemoveUserDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<LoaderResult<Friend>> onLoaderCreate(int id, Bundle args) {
        return new FriendDetailsLoader(this);
    }

    @Override
    public void onLoaderResult(int id, Friend data) {
        fillUI(data);
    }

    public void videoCallClickListener(View view) {
        VideoCallActivity.start(FriendDetailsActivity.this);
    }

    public void voiceCallClickListener(View view) {
        VoiceCallActivity.start(FriendDetailsActivity.this);
    }

    public void chatClickListener(View view) {
        // TODO IS start chat with user
    }

    private void fillUI(Friend friend) {
        new QBLoadImageTask(this).execute(friend.getFileId(), avatarImageView);
        nameTextView.setText(friend.getFullname());
        if (friend.isOnline()) {
            onlineImageView.setVisibility(View.VISIBLE);
        } else {
            onlineImageView.setVisibility(View.GONE);
        }
        if (friend.getPhone() != null) {
            phoneView.setVisibility(View.VISIBLE);
        } else {
            phoneView.setVisibility(View.GONE);
        }
        onlineStatusTextView.setText(friend.getOnlineStatus());
        phoneTextView.setText(friend.getPhone());
    }

    private void startLoaderWithTimer() {
        friendUpdateTimer = new Timer();
        friendUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runLoader(FriendDetailsLoader.ID, FriendDetailsLoader.newArguments(friend.getId()));
            }
        }, START_DELAY, UPDATE_DATA_PERIOD);
    }

    private void showRemoveUserDialog() {
        ConfirmDialog dialog = ConfirmDialog.newInstance(R.string.dlg_remove_user, R.string.dlg_confirm);
        dialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress();
                QBRemoveFriendCommand.start(FriendDetailsActivity.this, friend);
            }
        });
        dialog.show(getFragmentManager(), null);
    }

    private class RemoveFriendBroadcastReceiver extends BaseBroadcastReceiver {

        @Override
        public void onResult(Bundle bundle) {
            App.getInstance().getFriends().remove(friend);
            hideProgress();
            finish();
        }
    }
}
