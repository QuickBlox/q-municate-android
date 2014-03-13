package com.quickblox.qmunicate.ui.invitefriends;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.quickblox.qmunicate.ui.dialogs.ProgressDialog;
import com.quickblox.qmunicate.ui.utils.DialogUtils;
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
        setHasOptionsMenu(true);
        fromFacebookButton = (LinearLayout) view.findViewById(R.id.fromFacebookButton);
        counterFacebookTextView = (TextView) view.findViewById(R.id.counterFacebookTextView);
        checkAllFacebookFriendsCheckBox = (CheckBox) view.findViewById(R.id.checkAllFacebookFriendsCheckBox);
        usersListView = (ListView) view.findViewById(R.id.usersListView);
    }

    private void initListeners() {
        fromFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.invite_friends_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                if (isAdapterEmpty()) {
                    DialogUtils.show(getActivity(), getResources().getString(R.string.dlg_no_friends_selected));
                } else {
                    postToFacebookWallOnClick();
                }
                break;
        }
        return true;
    }

    private boolean isAdapterEmpty() {
        return friendsAdapter.isEmpty();
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
                    Log.d("fb123", "id = " + user.getId() + ", name = " + user.getName());
                }
                friendsAdapter.notifyDataSetChanged();
                setVisibilityCountPart(friendsFacebookList, fromFacebookButton, counterFacebookTextView, checkAllFacebookFriendsCheckBox);
            }
        });
    }

    private void initCheckAllFriends(boolean isFacebookFriends, boolean isCheck) {
        if (isFacebookFriends) {
            friendsAdapter.setCounterFacebook(getCheckedFriends(friendsFacebookList, isCheck));
        }
        // TODO Contacts part
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

    private void setVisibilityCountPart(List friends, LinearLayout fromButton, TextView counterTextView, CheckBox checkBox) {
        if (friends.size() > 0) {
            fromButton.setClickable(false);
            counterTextView.setVisibility(View.VISIBLE);
            checkBox.setVisibility(View.VISIBLE);
        } else if (friends.size() <= 0) {
            fromButton.setClickable(true);
            counterTextView.setVisibility(View.GONE);
            checkBox.setVisibility(View.GONE);
            DialogUtils.show(getActivity(), getResources().getString(R.string.dlg_no_facebook_friends));
        }
    }

    private void postToFacebookWallOnClick() {
        new InviteViaFacebook(getActivity()).postToFacebookWall(getSelectedFriendsForInvite());
    }

    private String[] getSelectedFriendsForInvite() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (int i = 0; i < friendsAdapter.getCount(); i++) {
            InviteFriend friend = friendsAdapter.getItem(i);
            if (friend.isSelected()) {
                arrayList.add(friend.getId());
            }
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

    @Override
    public void onCounterFacebookChanged(int valueCounterFacebook) {
        counterFacebookTextView.setText(valueCounterFacebook + "");
    }

    @Override
    public void onCounterContactsChanged(int valueCounterContacts) {
        // TODO Contacts part
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