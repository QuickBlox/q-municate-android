package com.quickblox.qmunicate.ui.friend;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.internal.core.exception.BaseServiceException;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBRemoveFriendCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.dialogs.ConfirmDialog;
import com.quickblox.qmunicate.ui.mediacall.CallActivity;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.UriCreator;

public class FriendDetailsActivity extends BaseActivity {

    public static final String EXTRA_FRIEND = "Friend";

    private ImageView avatarImageView;
    private TextView nameTextView;
    private ImageView onlineImageView;
    private TextView onlineStatusTextView;
    private TextView phoneTextView;
    private View phoneView;

    private Friend friend;

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

        addAction(QBServiceConsts.REMOVE_FRIEND_SUCCESS_ACTION, new RemoveFriendSuccessAction());
        addAction(QBServiceConsts.REMOVE_FRIEND_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.GET_FILE_FAIL_ACTION, failAction);
        updateBroadcastActionList();

        friend = (Friend) getIntent().getExtras().getSerializable(EXTRA_FRIEND);

        initFriendsFields(friend);
    }

    private void initFriendsFields(Friend friend) {
        try {
            String uri = UriCreator.getUri(friend.getAvatarUid());
            ImageLoader.getInstance().displayImage(uri, avatarImageView, Consts.UIL_AVATAR_DISPLAY_OPTIONS);
        } catch (BaseServiceException e) {
            ErrorUtils.showError(this, e);
        }

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

    public void videoCallClickListener(View view) {
        callToUser(friend, WebRTC.MEDIA_STREAM.VIDEO);
    }

    private void callToUser(Friend friend, WebRTC.MEDIA_STREAM callType) {
        if (friend.isOnline() && friend.getId() != App.getInstance().getUser().getId()) {
            QBUser qbUser = new QBUser(friend.getId());
            qbUser.setFullName(friend.getFullname());
            CallActivity.start(FriendDetailsActivity.this, qbUser, callType);
        } else {
            ErrorUtils.showError(this, getString(R.string.frd_offline_user));
        }
    }

    public void voiceCallClickListener(View view) {

        callToUser(friend, WebRTC.MEDIA_STREAM.AUDIO);
    }

    public void chatClickListener(View view) {
        //        PrivateChatActivity.start(FriendDetailsActivity.this, nameTextView.getText().toString());
    }

    private class RemoveFriendSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            App.getInstance().getFriends().remove(friend);
            DialogUtils.show(FriendDetailsActivity.this, getString(R.string.dlg_friend_removed));
            finish();
        }
    }
}