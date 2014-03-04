package com.quickblox.qmunicate.ui.friend;

import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.ui.base.LoaderActivity;

public class FriendDetailsActivity extends LoaderActivity<FriendDetailsLoader.Result> {

    public static final String PARAM_FRIEND_ID = "Friend ID";

    public static void startActivity(Context context, int friendId) {
        Intent intent = new Intent(context, FriendDetailsActivity.class);
        intent.putExtra(PARAM_FRIEND_ID, friendId);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);
        actionBar.setDisplayHomeAsUpEnabled(true);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<LoaderResult<FriendDetailsLoader.Result>> onLoaderCreate(int id, Bundle args) {
        return new FriendDetailsLoader(this);
    }

    @Override
    public void onLoaderResult(int id, FriendDetailsLoader.Result data) {

    }
}
