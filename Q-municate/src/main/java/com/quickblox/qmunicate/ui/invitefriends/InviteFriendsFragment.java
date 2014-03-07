package com.quickblox.qmunicate.ui.invitefriends;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.PickerFragment;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.base.BaseFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InviteFriendsFragment extends BaseFragment {
    private View view;
    private static final int RESULT_OK = -1;
    private static final int PICK_FRIENDS_ACTIVITY = 1;
    private UiLifecycleHelper lifecycleHelper;
    boolean pickFriendsWhenSessionOpened;
    FriendPickerFragment friendPickerFragment;

    /**
     * The key for a String parameter in the fragment's Intent bundle to indicate what user's
     * friends should be shown. The default is to display the currently authenticated user's friends.
     */
    public static final String USER_ID_BUNDLE_KEY = "com.facebook.widget.FriendPickerFragment.UserId";
    /**
     * The key for a boolean parameter in the fragment's Intent bundle to indicate whether the
     * picker should allow more than one friend to be selected or not.
     */
    public static final String MULTI_SELECT_BUNDLE_KEY = "com.facebook.widget.FriendPickerFragment.MultiSelect";

    private static final String ID = "id";
    private static final String NAME = "name";

    private String userId;

    private boolean multiSelect = true;

    private List<String> preSelectedFriendIds = new ArrayList<String>();

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

        AppEventsLogger.activateApp(getActivity());
        //displaySelectedFriends(RESULT_OK);
        onClickPickFriends();

        return view;
    }

    private static List<GraphUser> selectedUsers;

    public static List<GraphUser> getSelectedUsers() {
        return selectedUsers;
    }

    public static void setSelectedUsers(List<GraphUser> selectedUsers_) {
        selectedUsers = selectedUsers_;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_FRIENDS_ACTIVITY:
                displaySelectedFriends(resultCode);
                break;
            default:
                Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
                break;
        }
    }

    private boolean ensureOpenSession() {
        if (Session.getActiveSession() == null ||
                !Session.getActiveSession().isOpened()) {
            Session.openActiveSession(getActivity(), true, new Session.StatusCallback() {
                @Override
                public void call(Session session, SessionState state, Exception exception) {
                    onSessionStateChanged(session, state, exception);
                }
            });
            return false;
        }
        return true;
    }

    private void onSessionStateChanged(Session session, SessionState state, Exception exception) {
        if (pickFriendsWhenSessionOpened && state.isOpened()) {
            pickFriendsWhenSessionOpened = false;

            startPickFriendsActivity();
        }
    }

    private void onClickPickFriends() {
        startPickFriendsActivity();
    }

    private void startPickFriendsActivity() {
        if (ensureOpenSession()) {

            //FragmentManager fm = getSupportFragmentManager();

            Intent intent = new Intent(getActivity(), InviteFriendsFragment.class);
            // Note: The following line is optional, as multi-select behavior is the default for
            // FriendPickerFragment. It is here to demonstrate how parameters could be passed to the
            // friend picker if single-select functionality was desired, or if a different user ID was
            // desired (for instance, to see friends of a friend).
            populateParameters(intent, null, true, true);

            // First time through, we create our fragment programmatically.
            final Bundle args = getArguments();
            friendPickerFragment = new FriendPickerFragment(args);

            //fm.beginTransaction().add(R.id.friend_picker_fragment, friendPickerFragment).commit();

            friendPickerFragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
                @Override
                public void onError(PickerFragment<?> fragment, FacebookException error) {
                    InviteFriendsFragment.this.onError(error);
                }
            });

            friendPickerFragment.setOnDoneButtonClickedListener(new PickerFragment.OnDoneButtonClickedListener() {
                @Override
                public void onDoneButtonClicked(PickerFragment<?> fragment) {
                    // We just store our selection in the Application for other activities to look at.
                    setSelectedUsers(friendPickerFragment.getSelection());

                    //setResult(RESULT_OK, null);
                    //finish();
                }
            });

            try {
                List<GraphUser> selectedUsers = FriendPickerSampleActivity.getSelectedUsers();
                if (selectedUsers != null && !selectedUsers.isEmpty()) {
                    friendPickerFragment.setSelection(selectedUsers);
                }
                // Load data, unless a query has already taken place.
                friendPickerFragment.loadData(false);
            } catch (Exception ex) {
                onError(ex);
            }

        } else {
            pickFriendsWhenSessionOpened = true;
        }
    }

    private void onError(Exception error) {
        String text = getString(R.string.facebook_exception, error.getMessage());
        Toast toast = Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    // A helper to simplify life for callers who want to populate a Bundle with the necessary
    // parameters. A more sophisticated Activity might define its own set of parameters; our needs
    // are simple, so we just populate what we want to pass to the FriendPickerFragment.
    private static void populateParameters(Intent intent, String userId, boolean multiSelect, boolean showTitleBar) {
        intent.putExtra(FriendPickerFragment.USER_ID_BUNDLE_KEY, userId);
        intent.putExtra(FriendPickerFragment.MULTI_SELECT_BUNDLE_KEY, multiSelect);
        intent.putExtra(FriendPickerFragment.SHOW_TITLE_BAR_BUNDLE_KEY, showTitleBar);
    }

    private void displaySelectedFriends(int resultCode) {
        String results = "";
        Collection<GraphUser> selection = getSelectedUsers();
        if (selection != null && selection.size() > 0) {
            ArrayList<String> names = new ArrayList<String>();
            for (GraphUser user : selection) {
                names.add(user.getName());
            }
            results = TextUtils.join(", ", names);
        } else {
            results = "<No friends selected>";
        }
        Toast.makeText(getActivity(), results, Toast.LENGTH_LONG);
    }
}