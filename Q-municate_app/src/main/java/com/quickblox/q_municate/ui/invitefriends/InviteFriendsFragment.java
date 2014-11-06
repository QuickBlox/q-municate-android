package com.quickblox.q_municate.ui.invitefriends;

import android.content.Intent;
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

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate.utils.EmailUtils;
import com.quickblox.q_municate.utils.FacebookHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InviteFriendsFragment extends BaseFragment implements CounterChangedListener {

    private View view;
    private View headerList;
    private LinearLayout fromFacebookButton;
    private LinearLayout fromContactsButton;
    private ListView friendsListView;
    private TextView counterContactsTextView;
    private CheckBox checkAllContactsFriendsCheckBox;
    private FacebookHelper facebookHelper;
    private FacebookSessionStatusCallback facebookSessionStatusCallback;
    private List<InviteFriend> friendsContactsList;
    private InviteFriendsAdapter friendsAdapter;
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

        initFriendsLists();
        initHeaderUI(headerList);

        initListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.onActivityStart();
    }

    private void initFriendsLists() {
        friendsContactsList = new ArrayList<InviteFriend>();

        friendsAdapter = new InviteFriendsAdapter(baseActivity, friendsContactsList);
        friendsAdapter.setCounterChangedListener(this);

        friendsListView.addHeaderView(headerList);
        friendsListView.setAdapter(friendsAdapter);
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

        checkAllContactsFriendsCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        initCheckAllFriends(isChecked);
                    }
                });
    }

    private void initCheckAllFriends(boolean isCheck) {
        friendsAdapter.setCounterContacts(getCheckedFriends(friendsContactsList, isCheck));
        friendsAdapter.notifyDataSetChanged();
    }

    private int getCheckedFriends(List<InviteFriend> friends, boolean isCheck) {
        int newCounter;
        for (InviteFriend friend : friends) {
            friend.setSelected(isCheck);
        }
        newCounter = isCheck ? friends.size() : ConstsCore.ZERO_INT_VALUE;

        onCounterContactsChanged(newCounter);

        return newCounter;
    }

    private void setCheckedCheckBox(int countSelected, CheckBox checkBox) {
        if (countSelected == ConstsCore.ZERO_INT_VALUE) {
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
        friendsContactsList.addAll(EmailUtils.getContactsWithEmail(baseActivity));
        updateFriendsList();
        setVisibilityCountPart();
    }

    private void facebookFriendsOnClick() {
        facebookHelper.loginWithFacebook();
    }

    private void initHeaderUI(View view) {
        fromFacebookButton = (LinearLayout) view.findViewById(R.id.from_facebook_linearlayout);
        fromContactsButton = (LinearLayout) view.findViewById(R.id.from_contacts_linearlayout);
        counterContactsTextView = (TextView) view.findViewById(R.id.counter_contacts_textview);
        checkAllContactsFriendsCheckBox = (CheckBox) view.findViewById(R.id.check_contacts_checkbox);
    }

    private void initUI() {
        setHasOptionsMenu(true);
        friendsListView = (ListView) view.findViewById(R.id.friends_listview);
        headerList = getActivity().getLayoutInflater().inflate(R.layout.view_section_title_invite_friends,
                null);
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
        selectedContactsFriendsArray = getSelectedFriendsForInvite();

        if (selectedContactsFriendsArray.length > ConstsCore.ZERO_INT_VALUE) {
            sendInviteToContacts();
        } else {
            DialogUtils.showLong(baseActivity, getResources().getString(R.string.dlg_no_friends_selected));
        }

        clearCheckedFriends();
    }

    private String[] getSelectedFriendsForInvite() {
        List<String> arrayList = new ArrayList<String>();
        for (InviteFriend friend : friendsContactsList) {
            if (friend.isSelected()) {
                arrayList.add(friend.getId());
            }
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

    private void openRequestDialog() {
        WebDialog requestsDialog = facebookHelper.getWebDialogRequest();
        requestsDialog.show();
    }

    private void clearCheckedFriends() {
        for (InviteFriend friend : friendsContactsList) {
            friend.setSelected(false);
        }
        onCounterContactsChanged(ConstsCore.ZERO_INT_VALUE);
        friendsAdapter.setCounterContacts(ConstsCore.ZERO_INT_VALUE);
        friendsAdapter.notifyDataSetChanged();
    }

    private void setVisibilityCountPart() {
        if (!friendsContactsList.isEmpty()) {
            fromContactsButton.setClickable(false);
            counterContactsTextView.setVisibility(View.VISIBLE);
            checkAllContactsFriendsCheckBox.setVisibility(View.VISIBLE);
        } else {
            fromContactsButton.setClickable(true);
            counterContactsTextView.setVisibility(View.GONE);
            checkAllContactsFriendsCheckBox.setVisibility(View.GONE);
            DialogUtils.showLong(getActivity(), getResources().getString(R.string.dlg_no_friends));
        }
    }

    private void updateFriendsList() {
        Collections.sort(friendsContactsList, new SimpleComparator());
        friendsAdapter.notifyDataSetChanged();
    }

    private void sendInviteToContacts() {
        EmailUtils.sendInviteEmail(baseActivity, selectedContactsFriendsArray);
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                openRequestDialog();
            }
        }
    }

    private class SimpleComparator implements Comparator<InviteFriend> {

        public int compare(InviteFriend inviteFriend1, InviteFriend inviteFriend2) {
            return (inviteFriend1.getName()).compareTo(inviteFriend2.getName());
        }
    }
}