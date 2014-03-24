package com.quickblox.qmunicate.ui.invitefriends;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.InviteFriend;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.utils.Consts;
import com.quickblox.qmunicate.ui.utils.DialogUtils;
import com.quickblox.qmunicate.ui.utils.FacebookHelper;
import com.quickblox.qmunicate.ui.utils.FriendsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InviteFriendsFragment extends BaseFragment implements CounterChangedListener {
    private View view;
    private LinearLayout fromFacebookButton;
    private LinearLayout fromContactsButton;
    private ListView friendsListView;
    private TextView counterFacebookTextView;
    private TextView counterContactsTextView;
    private CheckBox checkAllFacebookFriendsCheckBox;
    private CheckBox checkAllContactsFriendsCheckBox;

    private FacebookHelper facebookHelper;
    private FacebookSessionStatusCallback facebookSessionStatusCallback;
    private List<InviteFriend> friendsList;
    private List<InviteFriend> friendsFacebookList;
    private List<InviteFriend> friendsContactsList;
    private InviteFriendsAdapter friendsAdapter;
    private FriendsUtils friendsUtils;
    private boolean isUpdateFacebookFriendsList = true;
    private String[] selectedFacebookFriends;
    private String[] selectedContactsFriends;

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
        friendsUtils = new FriendsUtils(getActivity());

        friendsList = new ArrayList<InviteFriend>();
        friendsFacebookList = new ArrayList<InviteFriend>();
        friendsContactsList = new ArrayList<InviteFriend>();

        friendsAdapter = new InviteFriendsAdapter(getActivity(), R.layout.list_item_invite_friend, (ArrayList<InviteFriend>) friendsList);
        friendsAdapter.setCounterChangedListener(this);

        friendsListView.setAdapter(friendsAdapter);

        initListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.onActivityStart();
    }

    private void initUI() {
        setHasOptionsMenu(true);
        fromFacebookButton = (LinearLayout) view.findViewById(R.id.fromFacebookButton);
        fromContactsButton = (LinearLayout) view.findViewById(R.id.fromContactsButton);
        counterFacebookTextView = (TextView) view.findViewById(R.id.counterFacebookTextView);
        counterContactsTextView = (TextView) view.findViewById(R.id.counterContactsTextView);
        checkAllFacebookFriendsCheckBox = (CheckBox) view.findViewById(R.id.checkAllFacebookFriendsCheckBox);
        checkAllContactsFriendsCheckBox = (CheckBox) view.findViewById(R.id.checkAllContactsFriendsCheckBox);
        friendsListView = (ListView) view.findViewById(R.id.friendsListView);
    }

    private void initListeners() {
        fromFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebookFriendsOnClick();
            }
        });

        fromContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contactsFriendsOnClick();
            }
        });

        checkAllFacebookFriendsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                initCheckAllFriends(true, isChecked);
            }
        });

        checkAllContactsFriendsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                initCheckAllFriends(false, isChecked);
            }
        });
    }

    private void facebookFriendsOnClick() {
        facebookHelper.loginWithFacebook();
    }

    private void contactsFriendsOnClick() {
        getContactsFriendsList();
    }

    private void getContactsFriendsList() {
        new GetContactsFriendsListTask().execute();
    }

    private void initCheckAllFriends(boolean isFacebookFriends, boolean isCheck) {
        if (isFacebookFriends) {
            friendsAdapter.setCounterFacebook(getCheckedFriends(true, friendsFacebookList, isCheck));
        } else {
            friendsAdapter.setCounterContacts(getCheckedFriends(false, friendsContactsList, isCheck));
        }
        friendsAdapter.notifyDataSetChanged();
    }

    private int getCheckedFriends(boolean isFacebook, List<InviteFriend> friends, boolean isCheck) {
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

        if (isFacebook) {
            onCounterFacebookChanged(newCounter);
        } else {
            onCounterContactsChanged(newCounter);
        }

        return newCounter;
    }

    @Override
    public void onCounterFacebookChanged(int valueCounterFacebook) {
        counterFacebookTextView.setText(valueCounterFacebook + "");
    }

    @Override
    public void onCounterContactsChanged(int valueCounterContacts) {
        counterContactsTextView.setText(valueCounterContacts + "");
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
    public void onStop() {
        super.onStop();
        facebookHelper.onActivityStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.invite_friends_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                performActionNext();
                break;
        }
        return true;
    }

    private void performActionNext() {
        getBaseActivity().showProgress();
        if (friendsAdapter.isEmpty()) {
            DialogUtils.show(getActivity(), getResources().getString(R.string.dlg_no_friends_selected));
            getBaseActivity().hideProgress();
        } else {
            selectedFacebookFriends = getSelectedFriendsForInvite(InviteFriend.VIA_FACEBOOK_TYPE);
            selectedContactsFriends = getSelectedFriendsForInvite(InviteFriend.VIA_CONTACTS_TYPE);

            if (selectedFacebookFriends.length == 0 && selectedContactsFriends.length == 0) {
                DialogUtils.show(getActivity(), getResources().getString(R.string.dlg_no_friends_selected));
                getBaseActivity().hideProgress();
                return;
            }

            if (selectedFacebookFriends.length > 0) {
                sendInviteToFacebook();
            }

            new ActionSendInviteToContactsTask().execute();
        }
        clearCheckedFriends();
    }

    private void clearCheckedFriends() {
        for (InviteFriend friend : friendsList) {
            friend.setSelected(false);
        }
        friendsAdapter.notifyDataSetChanged();
        onCounterFacebookChanged(Consts.ZERO_VALUE);
        onCounterContactsChanged(Consts.ZERO_VALUE);
    }

    private void sendInviteToFacebook() {
        facebookHelper.postInviteToWall(new FacebookSendInviteCallback(), getSelectedFriendsForInvite(InviteFriend.VIA_FACEBOOK_TYPE));
    }

    private String[] getSelectedFriendsForInvite(int type) {
        List<String> arrayList = new ArrayList<String>();
        for (InviteFriend friend: friendsList) {
            if (friend.isSelected() && friend.getViaLabelType() == type) {
                arrayList.add(friend.getId());
            }
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

    private void getFacebookFriendsList() {
        getBaseActivity().showProgress();
        Request.executeMyFriendsRequestAsync(Session.getActiveSession(), new Request.GraphUserListCallback() {

            @Override
            public void onCompleted(List<com.facebook.model.GraphUser> users, Response response) {
                for (com.facebook.model.GraphUser user : users) {
                    friendsFacebookList.add(new InviteFriend(user.getId(), user.getName(), user.getLink(), InviteFriend.VIA_FACEBOOK_TYPE, null, false));
                }
                friendsList.addAll(friendsFacebookList);
                updateFriendsList();
                setVisibilityCountPart(friendsFacebookList, fromFacebookButton, counterFacebookTextView, checkAllFacebookFriendsCheckBox);
                getBaseActivity().hideProgress();
            }
        });
    }

    private void updateFriendsList() {
        Collections.sort(friendsList, new SimpleComparator());
        friendsAdapter.notifyDataSetChanged();
    }

    private void setVisibilityCountPart(List friends, LinearLayout fromButton, TextView counterTextView, CheckBox checkBox) {
        if (!friends.isEmpty()) {
            fromButton.setClickable(false);
            counterTextView.setVisibility(View.VISIBLE);
            checkBox.setVisibility(View.VISIBLE);
        } else {
            fromButton.setClickable(true);
            counterTextView.setVisibility(View.GONE);
            checkBox.setVisibility(View.GONE);
            DialogUtils.show(getActivity(), getResources().getString(R.string.dlg_no_friends));
        }
    }

    private void sendInviteToContacts() {
        friendsUtils.sendEmail(selectedContactsFriends);
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                new GetFacebookFriendsListTask().execute();
            }
        }
    }

    private class FacebookSendInviteCallback implements Request.Callback {
        public void onCompleted(Response response) {
            FacebookRequestError error = response.getError();
            if (error != null) {
                DialogUtils.show(getActivity(), getResources().getString(R.string.facebook_exception) + error);
            } else {
                DialogUtils.show(getActivity(), getResources().getString(R.string.dlg_success_posted_to_facebook));
            }
            getBaseActivity().hideProgress();
        }
    }

    private class SimpleComparator implements Comparator<InviteFriend> {
        public int compare(InviteFriend inviteFriend1, InviteFriend inviteFriend2) {
            return (inviteFriend1.getName()).compareTo(inviteFriend2.getName());
        }
    }

    private class GetContactsFriendsListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            getBaseActivity().showProgress();
            friendsContactsList = friendsUtils.getContactsWithEmail();
            friendsList.addAll(friendsContactsList);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            updateFriendsList();
            setVisibilityCountPart(friendsContactsList, fromContactsButton, counterContactsTextView, checkAllContactsFriendsCheckBox);
            getBaseActivity().hideProgress();
        }
    }

    private class GetFacebookFriendsListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            facebookHelper.checkPermissions();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (isUpdateFacebookFriendsList) {
                isUpdateFacebookFriendsList = false;
                getFacebookFriendsList();
            }
        }
    }

    private class ActionSendInviteToContactsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            if (selectedContactsFriends.length > 0) {
                sendInviteToContacts();
                getBaseActivity().hideProgress();
            }
            return null;
        }
    }
}