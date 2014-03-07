package com.quickblox.qmunicate.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBLogoutTask;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.dialogs.ChangePasswordDialog;
import com.quickblox.qmunicate.ui.profile.ProfileActivity;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

public class SettingsFragment extends BaseFragment {

    private Button profile;
    private Switch pushNotification;
    private Button changePassword;
    private Button logout;
    private ChangePasswordDialog dialog;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, App.getInstance().getString(R.string.nvd_title_settings));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        profile = (Button) rootView.findViewById(R.id.profile);
        pushNotification = (Switch) rootView.findViewById(R.id.pushNotification);
        changePassword = (Button) rootView.findViewById(R.id.changePassword);
        logout = (Button) rootView.findViewById(R.id.logout);

        pushNotification.setChecked(getPushNotifications());

        initListeners();
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        dialog = ChangePasswordDialog.newInstance();
    }

    private void initListeners() {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.start(getActivity());
            }
        });

        pushNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePushNotification(isChecked);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void changePassword() {
        dialog.show(getFragmentManager(), null);
    }

    private void logout() {
        new QBLogoutTask(getActivity()).execute();
    }

    private void savePushNotification(boolean value) {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_PUSH_NOTIFICATIONS, value);
    }

    private boolean getPushNotifications() {
        return App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_PUSH_NOTIFICATIONS, false);
    }
}
