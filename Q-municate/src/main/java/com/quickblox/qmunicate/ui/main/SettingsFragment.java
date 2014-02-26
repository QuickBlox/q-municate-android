package com.quickblox.qmunicate.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBLogoutTask;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.profile.ProfileActivity;
import com.quickblox.qmunicate.ui.utils.Consts;

import org.jraf.android.backport.switchwidget.Switch;

public class SettingsFragment extends BaseFragment {

    private Button profile;
    private Switch pushNotification;
    private Button changePassword;
    private Button logout;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, App.getInstance().getString(R.string.nvd_title_settings));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        profile = (Button) v.findViewById(R.id.profile);
        pushNotification = (Switch) v.findViewById(R.id.pushNotification);
        changePassword = (Button) v.findViewById(R.id.changePassword);
        logout = (Button) v.findViewById(R.id.logout);

        pushNotification.setChecked(getPushNotifications());

        initListeners();
        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void initListeners() {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.startActivity(getActivity());
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

    }

    private void logout() {
        new QBLogoutTask(getActivity()).execute();
    }

    private void savePushNotification(boolean value) {
        SharedPreferences prefs = App.getInstance().getSharedPreferences();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Consts.PREF_PUSH_NOTIFICATIONS, value);
        editor.commit();
    }

    private boolean getPushNotifications() {
        SharedPreferences prefs = App.getInstance().getSharedPreferences();
        return prefs.getBoolean(Consts.PREF_PUSH_NOTIFICATIONS, false);
    }
}
