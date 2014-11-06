package com.quickblox.q_municate.ui.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.QBLogoutCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.authorization.login.LoginActivity;
import com.quickblox.q_municate.ui.base.BaseFragment;
import com.quickblox.q_municate.ui.dialogs.ConfirmDialog;
import com.quickblox.q_municate.ui.profile.ProfileActivity;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_core.utils.Utils;

public class SettingsFragment extends BaseFragment {

    private Button profileButton;
    private Switch pushNotificationSwitch;
    private Button changePasswordButton;
    private Button logoutButton;
    private TextView versionView;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        initUI(rootView);

        pushNotificationSwitch.setChecked(getPushNotifications());
        QBUser user = AppSession.getSession().getUser();
        if (user == null || null == user.getFacebookId()) {
            rootView.findViewById(R.id.change_password_linearlyout).setVisibility(View.VISIBLE);
        } else {
            rootView.findViewById(R.id.change_password_linearlyout).setVisibility(View.GONE);
        }

        versionView.setText(getString(R.string.stn_version, Utils.getAppVersionName(baseActivity)));

        initListeners();

        //        TipsManager.showTipIfNotShownYet(this, getActivity().getString(R.string.tip_settings));

        return rootView;
    }

    private void initUI(View rootView) {
        profileButton = (Button) rootView.findViewById(R.id.profile_button);
        pushNotificationSwitch = (Switch) rootView.findViewById(R.id.push_notification_switch);
        changePasswordButton = (Button) rootView.findViewById(R.id.change_password_button);
        logoutButton = (Button) rootView.findViewById(R.id.logout_button);
        versionView = (TextView) rootView.findViewById(R.id.version_textview);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        title = getString(R.string.nvd_title_settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addActions();
    }

    private void addActions() {
        baseActivity.addAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION, new LogoutSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOGOUT_FAIL_ACTION, failAction);
        baseActivity.updateBroadcastActionList();
    }

    private void initListeners() {
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.start(baseActivity);
            }
        });

        pushNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePushNotification(isChecked);
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChangePasswordActivity();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void startChangePasswordActivity() {
        ChangePasswordActivity.start(baseActivity);
    }

    private void logout() {
        ConfirmDialog dialog = ConfirmDialog.newInstance(R.string.dlg_logout, R.string.dlg_confirm);
        dialog.setPositiveButton(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                baseActivity.showProgress();
                QBLogoutCommand.start(getActivity());
            }
        });
        dialog.show(getFragmentManager(), null);
    }

    private void savePushNotification(boolean value) {
        PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_PUSH_NOTIFICATIONS, !value);
    }

    private boolean getPushNotifications() {
        return !PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_PUSH_NOTIFICATIONS, false);
    }

    private class LogoutSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            LoginActivity.start(baseActivity);
            baseActivity.finish();
        }
    }
}