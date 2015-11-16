package com.quickblox.q_municate.ui.activities.invitefriends;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.WebDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.listeners.CounterChangedListener;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.adapters.invitefriends.InviteFriendsAdapter;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.helpers.EmailHelper;
import com.quickblox.q_municate.utils.helpers.FacebookHelper;
import com.quickblox.q_municate_core.models.InviteFriend;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;

import static butterknife.ButterKnife.findById;

// TODO need to refactor
@Deprecated
public class InviteFriendsActivity extends BaseLoggableActivity implements CounterChangedListener {

    @Bind(R.id.friends_listview)
    ListView friendsListView;

    private View headerView;
    private LinearLayout fromFacebookButton;
    private LinearLayout fromContactsButton;
    private TextView counterContactsTextView;
    private CheckBox checkAllContactsCheckBox;

    private FacebookHelper facebookHelper;
    private List<InviteFriend> friendsContactsList;
    private InviteFriendsAdapter friendsAdapter;
    private String[] selectedContactsFriendsArray;

    public static void start(Context context) {
        Intent intent = new Intent(context, InviteFriendsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_invite_friends;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpActionBarWithUpButton();

        initFields(savedInstanceState);
        initCustomUI();
        initCustomListeners();
        initFriendsList();
    }

    private void initCustomListeners() {
        fromFacebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFromFacebook();
            }
        });

        fromContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectFromContacts();
            }
        });

        checkAllContactsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkAllContacts(isChecked);
            }
        });
    }

    private void initFields(Bundle savedInstanceState) {
        FacebookSessionStatusCallback facebookSessionStatusCallback = new FacebookSessionStatusCallback();
        facebookHelper = new FacebookHelper(this, savedInstanceState, facebookSessionStatusCallback);
    }

    private void initCustomUI() {
        headerView = getLayoutInflater().inflate(R.layout.view_section_title_invite_friends, null);
        fromFacebookButton = findById(headerView, R.id.from_facebook_linearlayout);
        fromContactsButton = findById(headerView, R.id.from_contacts_linearlayout);
        counterContactsTextView = findById(headerView, R.id.counter_contacts_textview);
        checkAllContactsCheckBox = findById(headerView, R.id.check_all_contacts_checkbox);
    }

    private void initFriendsList() {
        friendsContactsList = new ArrayList<InviteFriend>();

        friendsAdapter = new InviteFriendsAdapter(this, friendsContactsList);
        friendsAdapter.setCounterChangedListener(this);

        friendsListView.addHeaderView(headerView);
        friendsListView.setAdapter(friendsAdapter);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.invite_friends_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_next:
                if (checkNetworkAvailableWithError()) {
                    performActionNext();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onCounterContactsChanged(int valueCounterContacts) {
        setCheckedCheckBox(valueCounterContacts, checkAllContactsCheckBox);
        counterContactsTextView.setText(String.valueOf(valueCounterContacts));
    }

    private void selectFromFacebook() {
        facebookHelper.loginWithFacebook();
    }

    private void selectFromContacts() {
        friendsContactsList.addAll(EmailHelper.getContactsWithEmail(this));
        updateFriendsList();
        setVisibilityCountPart();
    }

    private void checkAllContacts(boolean isCheck) {
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

    private void performActionNext() {
        selectedContactsFriendsArray = getSelectedFriendsForInvite();

        if (selectedContactsFriendsArray.length > ConstsCore.ZERO_INT_VALUE) {
            sendInviteToContacts();
        } else {
            ToastUtils.longToast(R.string.dlg_no_friends_selected);
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
            checkAllContactsCheckBox.setVisibility(View.VISIBLE);
        } else {
            fromContactsButton.setClickable(true);
            counterContactsTextView.setVisibility(View.GONE);
            checkAllContactsCheckBox.setVisibility(View.GONE);
            ToastUtils.longToast(R.string.dlg_no_friends);
        }
    }

    private void updateFriendsList() {
        Collections.sort(friendsContactsList, new SimpleComparator());
        friendsAdapter.notifyDataSetChanged();
    }

    private void sendInviteToContacts() {
        EmailHelper.sendInviteEmail(this, selectedContactsFriendsArray);
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