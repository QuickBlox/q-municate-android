package com.quickblox.qmunicate.ui.friend;

import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.QBLoadImageTask;
import com.quickblox.qmunicate.qb.QBRemoveFriendTask;
import com.quickblox.qmunicate.ui.base.LoaderActivity;

public class FriendDetailsActivity extends LoaderActivity<FriendDetailsLoader.Result> {

    public static final String PARAM_FRIEND = "Friend";

    private ImageView avatarImageView;
    private TextView nameTextView;
    private ImageView onlineImageView;
    private TextView onlineStatusTextView;
    private TextView photeTextView;

    private Friend friend;

    public static void start(Context context, Friend friend) {
        Intent intent = new Intent(context, FriendDetailsActivity.class);
        intent.putExtra(PARAM_FRIEND, friend);
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
        photeTextView = _findViewById(R.id.photeTextView);

        friend = (Friend) getIntent().getExtras().getSerializable(PARAM_FRIEND);

        fillUI(friend);
        runLoader(FriendDetailsLoader.ID, FriendDetailsLoader.newArguments(friend.getId()));
    }

    private void fillUI(Friend friend) {
        new QBLoadImageTask(this).execute(friend.getFileId(), avatarImageView);
        nameTextView.setText(friend.getFullname());
        if (friend.isOnline()) {
            onlineImageView.setVisibility(View.VISIBLE);
        } else {
            onlineImageView.setVisibility(View.INVISIBLE);
        }
        onlineStatusTextView.setText(friend.getOnlineStatus());
        photeTextView.setText(friend.getPhone());
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
                removeFriend();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void removeFriend() {
        new QBRemoveFriendTask(this).execute(friend, new QBRemoveFriendTask.Callback() {
            @Override
            public void onSuccess() {
                App.getInstance().getFriends().remove(friend);
                finish();
            }
        });
    }

    @Override
    public Loader<LoaderResult<FriendDetailsLoader.Result>> onLoaderCreate(int id, Bundle args) {
        return new FriendDetailsLoader(this);
    }

    @Override
    public void onLoaderResult(int id, FriendDetailsLoader.Result data) {
        fillUI(data.friend);
    }
}
