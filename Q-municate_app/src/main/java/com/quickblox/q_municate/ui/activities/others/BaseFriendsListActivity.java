package com.quickblox.q_municate.ui.activities.others;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.adapters.friends.FriendsAdapter;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

import butterknife.Bind;

public abstract class BaseFriendsListActivity extends BaseLogeableActivity {

    @Bind(R.id.friends_recyclerview)
    protected RecyclerView friendsRecyclerView;

    protected FriendsAdapter friendsAdapter;
    protected DataManager dataManager;

    @Override
    protected void onStart() {
        super.onStart();
        initFields();
        initActionBar();
        initRecyclerView();
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initFields() {
        dataManager = DataManager.getInstance();
    }

    protected void initRecyclerView() {
        friendsAdapter = getFriendsAdapter();
        friendsRecyclerView.setAdapter(friendsAdapter);
        friendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                performDone();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    protected List<User> getFriendsList() {
        List<Friend> friendsList = dataManager.getFriendDataManager().getAllSorted();
        return UserFriendUtils.getUsersFromFriends(friendsList);
    }

    protected abstract FriendsAdapter getFriendsAdapter();

    protected abstract void performDone();
}