package com.quickblox.qmunicate.ui.invitefriends;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.InviteFriend;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.utils.FacebookHelper;

import java.util.ArrayList;
import java.util.List;

public class InviteFriendsFragment extends BaseFragment implements CounterChangedListener {
    private View view;
    private LinearLayout fromFacebookButton;
    private ListView usersListView;
    private TextView counterFacebookTextView;
    private CheckBox checkAllFacebookFriendsCheckBox;

    private FacebookHelper facebookHelper;
    private FacebookSessionStatusCallback facebookSessionStatusCallback;
    private List<InviteFriend> friendsFacebookList;
    private InviteFriendsAdapter friendsAdapter;

    public static InviteFriendsFragment newInstance() {
        InviteFriendsFragment fragment = new InviteFriendsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, App.getInstance().getString(R.string.nvd_title_invite_friends));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_invite_friends, container, false);

        initUI();

        facebookSessionStatusCallback = new FacebookSessionStatusCallback();
        facebookHelper = new FacebookHelper(getActivity(), savedInstanceState, facebookSessionStatusCallback);

        friendsFacebookList = new ArrayList<InviteFriend>();
        friendsAdapter = new InviteFriendsAdapter(getActivity(), R.layout.list_item_invite_friend, (ArrayList<InviteFriend>) friendsFacebookList);
        friendsAdapter.setCounterChangedListener(this);

        usersListView.setAdapter(friendsAdapter);

        initListeners();

        return view;
    }

    private void initUI() {
        fromFacebookButton = (LinearLayout) view.findViewById(R.id.fromFacebookButton);
        counterFacebookTextView = (TextView) view.findViewById(R.id.counterFacebookTextView);
        checkAllFacebookFriendsCheckBox = (CheckBox) view.findViewById(R.id.checkAllFacebookFriendsCheckBox);
        usersListView = (ListView) view.findViewById(R.id.usersListView);
    }

    private void initListeners() {
        fromFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookFriendsOnClick();
            }
        });

        checkAllFacebookFriendsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                initCheckAllFriends(true, isChecked);
            }
        });
    }

    private void setEnabledCheckBox(List friends, CheckBox checkBox) {
        if (friends.size() > 0) {
            checkBox.setEnabled(true);
        } else if (friends.size() <= 0) {
            checkBox.setEnabled(false);
        }
    }

    private void initCheckAllFriends(boolean isFacebookFriends, boolean isCheck) {
        if (isFacebookFriends) {
            friendsAdapter.setCounterFacebook(getCheckedFriends(friendsFacebookList, isCheck));
        }
        friendsAdapter.notifyDataSetChanged();
    }

    private int getCheckedFriends(List<InviteFriend> friends, boolean isCheck) {
        int newCounter;
        if (isCheck) {
            for (InviteFriend friend : friends) {
                friend.setSelected(true);
            }
            newCounter = friends.size();
        } else {
            for (InviteFriend friend : friends) {
                friend.setSelected(false);
            }
            newCounter = 0;
        }
        onCounterFacebookChanged(newCounter);
        return newCounter;
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.onActivityStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        facebookHelper.onActivityStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        facebookHelper.onSaveInstanceState(outState);
    }

    private void facebookFriendsOnClick() {
        facebookHelper.loginWithFacebook();
    }

    private void getFacebookFriendsList() {
        Request.executeMyFriendsRequestAsync(Session.getActiveSession(), new Request.GraphUserListCallback() {

            @Override
            public void onCompleted(List<com.facebook.model.GraphUser> users, Response response) {
                for (com.facebook.model.GraphUser user : users) {
                    friendsFacebookList.add(new InviteFriend(user.getId(), user.getName(), user.getLink(), InviteFriend.VIA_FACEBOOK_TYPE, false));
                }
                friendsAdapter.notifyDataSetChanged();
                setEnabledCheckBox(friendsFacebookList, checkAllFacebookFriendsCheckBox);
            }
        });
    }

    @Override
    public void onCounterFacebookChanged(int valueCounterFacebook) {
        counterFacebookTextView.setText(valueCounterFacebook + "");
    }

    @Override
    public void onCounterContactsChanged(int valueCounterContacts) {
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                getFacebookFriendsList();
            }
        }
    }
}