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
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.InviteFriend;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.utils.*;

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
    private String[] selectedFacebookFriendsArray;
    private String[] selectedContactsFriendsArray;

    public static InviteFriendsFragment newInstance() {
        return new InviteFriendsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = getString(R.string.nvd_title_invite_friends);
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

        friendsAdapter = new InviteFriendsAdapter(baseActivity, friendsList);
        friendsAdapter.setCounterChangedListener(this);

        View header = getActivity().getLayoutInflater().inflate(R.layout.view_section_title_invite_friends, null);
        friendsListView.addHeaderView(header);
        friendsListView.setAdapter(friendsAdapter);

        initHeaderUI(header);

        initListeners();
        TipsManager.showTipIfNotShownYet(this, getActivity().getString(R.string.tip_invite_friends));



        return view;
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
        setCheckedCheckBox(valueCounterFacebook, checkAllFacebookFriendsCheckBox);
        counterFacebookTextView.setText(valueCounterFacebook + "");
    }

    private void setCheckedCheckBox(int countSelected, CheckBox checkBox) {
        if(countSelected == 0) {
            checkBox.setChecked(false);
        }
    }

    @Override
    public void onCounterContactsChanged(int valueCounterContacts) {
        setCheckedCheckBox(valueCounterContacts, checkAllContactsFriendsCheckBox);
        counterContactsTextView.setText(valueCounterContacts + "");
    }

    private void contactsFriendsOnClick() {
        getContactsFriendsList();
    }

    private void getContactsFriendsList() {
        new GetContactsFriendsListTask().execute();
    }

    private void facebookFriendsOnClick() {
        facebookHelper.loginWithFacebook();
    }

    private void initHeaderUI(View view) {
        fromFacebookButton = (LinearLayout) view.findViewById(R.id.fromFacebookButton);
        fromContactsButton = (LinearLayout) view.findViewById(R.id.fromContactsButton);
        counterFacebookTextView = (TextView) view.findViewById(R.id.counterFacebookTextView);
        counterContactsTextView = (TextView) view.findViewById(R.id.counterContactsTextView);
        checkAllFacebookFriendsCheckBox = (CheckBox) view.findViewById(R.id.checkAllFacebookFriendsCheckBox);
        checkAllContactsFriendsCheckBox = (CheckBox) view.findViewById(R.id.checkAllContactsFriendsCheckBox);
    }

    private void initUI() {
        setHasOptionsMenu(true);
        friendsListView = (ListView) view.findViewById(R.id.friendsListView);
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.onActivityStart();
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
        baseActivity.showProgress();
        if (friendsAdapter.isEmpty()) {
            DialogUtils.show(getActivity(), getResources().getString(R.string.dlg_no_friends_selected));
            baseActivity.hideProgress();
        } else {
            selectedFacebookFriendsArray = getSelectedFriendsForInvite(InviteFriend.VIA_FACEBOOK_TYPE);
            selectedContactsFriendsArray = getSelectedFriendsForInvite(InviteFriend.VIA_CONTACTS_TYPE);

            if (selectedFacebookFriendsArray.length == 0 && selectedContactsFriendsArray.length == 0) {
                DialogUtils.show(getActivity(), getResources().getString(R.string.dlg_no_friends_selected));
                baseActivity.hideProgress();
                return;
            }

            if (selectedFacebookFriendsArray.length > 0) {
                sendInviteToFacebook();
            }

            new ActionSendInviteToContactsTask().execute();
        }
        clearCheckedFriends();
    }

    private String[] getSelectedFriendsForInvite(int type) {
        List<String> arrayList = new ArrayList<String>();
        for (InviteFriend friend : friendsList) {
            if (friend.isSelected() && friend.getViaLabelType() == type) {
                arrayList.add(friend.getId());
            }
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

    private void sendInviteToFacebook() {
        facebookHelper.postInviteToWall(new FacebookSendInviteCallback(), getSelectedFriendsForInvite(InviteFriend.VIA_FACEBOOK_TYPE));
    }

    private void clearCheckedFriends() {
        for (InviteFriend friend : friendsList) {
            friend.setSelected(false);
        }
        onCounterFacebookChanged(Consts.ZERO_INT_VALUE);
        onCounterContactsChanged(Consts.ZERO_INT_VALUE);
        friendsAdapter.setCounterFacebook(Consts.ZERO_INT_VALUE);
        friendsAdapter.setCounterContacts(Consts.ZERO_INT_VALUE);
        friendsAdapter.notifyDataSetChanged();
    }

    private void getFacebookFriendsList() {
        baseActivity.showProgress();
        Request.executeMyFriendsRequestAsync(Session.getActiveSession(), new Request.GraphUserListCallback() {

            @Override
            public void onCompleted(List<com.facebook.model.GraphUser> users, Response response) {
                for (com.facebook.model.GraphUser user : users) {
                    friendsFacebookList.add(new InviteFriend(user.getId(), user.getName(), user.getLink(), InviteFriend.VIA_FACEBOOK_TYPE, null, false));
                }
                friendsList.addAll(friendsFacebookList);
                updateFriendsList();
                setVisibilityCountPart(friendsFacebookList, fromFacebookButton, counterFacebookTextView, checkAllFacebookFriendsCheckBox);
                baseActivity.hideProgress();
            }
        });
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

    private void updateFriendsList() {
        Collections.sort(friendsList, new SimpleComparator());
        friendsAdapter.notifyDataSetChanged();
    }

    private void sendInviteToContacts() {
        friendsUtils.sendEmail(selectedContactsFriendsArray);
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
            baseActivity.hideProgress();
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
            baseActivity.showProgress();
            friendsContactsList = friendsUtils.getContactsWithEmail();
            friendsList.addAll(friendsContactsList);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            updateFriendsList();
            setVisibilityCountPart(friendsContactsList, fromContactsButton, counterContactsTextView, checkAllContactsFriendsCheckBox);
            baseActivity.hideProgress();
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
            if (selectedContactsFriendsArray.length > 0) {
                sendInviteToContacts();
                baseActivity.hideProgress();
            }
            return null;
        }
    }
}