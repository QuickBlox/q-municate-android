package com.quickblox.q_municate.ui.fragments.contacts;

import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.orangegangsters.github.swipyrefreshlayout.library.SwipyRefreshLayout;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.profile.UserProfileActivity;
import com.quickblox.q_municate.ui.adapters.contacts.ContactsAdapter;
import com.quickblox.q_municate.ui.fragments.base.BaseLoaderFragment;
import com.quickblox.q_municate.ui.uihelpers.SimpleOnRecycleItemClickListener;
import com.quickblox.q_municate.ui.views.recyclerview.SimpleDividerItemDecoration;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.managers.FriendDataManager;
import com.quickblox.q_municate_db.managers.UserRequestDataManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.models.UserRequest;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.OnTouch;

public abstract class BaseContactsFragment extends BaseLoaderFragment<List<User>> {

    @Bind(R.id.contacts_swipyrefreshlayout)
    SwipyRefreshLayout swipyRefreshLayout;

    @Bind(R.id.contacts_recyclerview)
    RecyclerView contactsRecyclerView;

    protected DataManager dataManager;
    protected ContactsAdapter contactsAdapter;
    protected List<User> usersList;
    protected String searchQuery;

    private Observer friendObserver;
    private Observer userRequestObserver;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addObservers();
    }

    protected void initFields() {
        dataManager = DataManager.getInstance();
        friendObserver = new FriendObserver();
        userRequestObserver = new UserRequestObserver();
    }

    protected void initContactsList(List<User> usersList) {
        contactsAdapter = new ContactsAdapter(baseActivity, usersList);
        contactsAdapter.setFriendListHelper(friendListHelper);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        contactsRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));;
        contactsRecyclerView.setAdapter(contactsAdapter);
    }

    protected void initCustomListeners() {
        contactsAdapter.setOnRecycleItemClickListener(new SimpleOnRecycleItemClickListener<User>() {

            @Override
            public void onItemClicked(View view, User user, int position) {
                boolean isFriend = dataManager.getFriendDataManager().existsByUserId(user.getUserId());
                boolean outgoingUser = dataManager.getUserRequestDataManager()
                        .existsByUserId(user.getUserId());
                if (isFriend || outgoingUser) {
                    startFriendDetailsActivity(user.getUserId());
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deleteObservers();
    }

    @OnTouch(R.id.contacts_recyclerview)
    public boolean touchContactsList(View view, MotionEvent event) {
        KeyboardUtils.hideKeyboard(baseActivity);
        return false;
    }

    private void addObservers() {
        dataManager.getUserRequestDataManager().addObserver(userRequestObserver);
        dataManager.getFriendDataManager().addObserver(friendObserver);
    }

    private void deleteObservers() {
        dataManager.getUserRequestDataManager().deleteObserver(userRequestObserver);
        dataManager.getFriendDataManager().deleteObserver(friendObserver);
    }

    protected void startFriendDetailsActivity(int userId) {
        UserProfileActivity.start(baseActivity, userId);
    }

    protected void checkVisibilityEmptyLabel() {
        List<Friend> friendList = dataManager.getFriendDataManager().getAll();
        int countFriends = friendList.size();
        List<UserRequest> userRequestList = dataManager.getUserRequestDataManager().getAll();
        int countUserRequests = userRequestList.size();

//        if ((countFriends + countUserRequests) > 0) {
//            emptyListTextView.setVisibility(View.GONE);
//        } else {
//            emptyListTextView.setVisibility(View.VISIBLE);
//        }
    }

    protected abstract void updateContactsList();

    @Override
    protected Loader<List<User>> createDataLoader() {
        // nothing by default
        return null;
    }

    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> dialogsList) {
        // nothing by default
    }

    private class FriendObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(FriendDataManager.OBSERVE_KEY)) {
                updateContactsList();
            }
        }
    }

    private class UserRequestObserver implements Observer {

        @Override
        public void update(Observable observable, Object data) {
            if (data != null && data.equals(UserRequestDataManager.OBSERVE_KEY)) {
                updateContactsList();
            }
        }
    }
}