package com.quickblox.qmunicate.ui.main;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBSubscription;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.qb.commands.QBLogoutCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseFragment;
import com.quickblox.qmunicate.ui.dialogs.ChangePasswordDialog;
import com.quickblox.qmunicate.ui.dialogs.ConfirmDialog;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.ui.profile.ProfileActivity;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.PrefsHelper;
import com.quickblox.qmunicate.utils.TipsManager;
import com.quickblox.qmunicate.utils.Utils;


public class SettingsFragment extends BaseFragment {

    private Button profile;
    private Switch pushNotification;
    private Button changePassword;
    private Button logout;
    private ChangePasswordDialog changePasswordDialog;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        profile = (Button) rootView.findViewById(R.id.profile);
        pushNotification = (Switch) rootView.findViewById(R.id.pushNotification);
        changePassword = (Button) rootView.findViewById(R.id.changePassword);
        logout = (Button) rootView.findViewById(R.id.logout);

        pushNotification.setChecked(getPushNotifications());
        App app = App.getInstance();
        QBUser user = app.getUser();
        if (user == null || null == user.getFacebookId()) {
            rootView.findViewById(R.id.changePasswordLayout).setVisibility(View.VISIBLE);
        } else {
            rootView.findViewById(R.id.changePasswordLayout).setVisibility(View.GONE);
        }

        TextView versionView = (TextView) rootView.findViewById(R.id.version);
        versionView.setText(getString(R.string.stn_version, Utils.getAppVersionName(baseActivity)));

        initListeners();

        TipsManager.showTipIfNotShownYet(this, getActivity().getString(R.string.tip_settings));

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        title = getString(R.string.nvd_title_settings);
        changePasswordDialog = ChangePasswordDialog.newInstance();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        baseActivity.addAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION, new LogoutSuccessAction());
        baseActivity.addAction(QBServiceConsts.CHANGE_PASSWORD_SUCCESS_ACTION, new ChangePasswordSuccessAction());
        baseActivity.addAction(QBServiceConsts.LOGOUT_FAIL_ACTION, failAction);
        baseActivity.addAction(QBServiceConsts.CHANGE_PASSWORD_FAIL_ACTION, failAction);
    }

    private void initListeners() {
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.start(baseActivity);
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
        changePasswordDialog.show(getFragmentManager(), null);
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
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_PUSH_NOTIFICATIONS, value);
    }

    private boolean getPushNotifications() {
        return App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_PUSH_NOTIFICATIONS, false);
    }

    private class LogoutSuccessAction implements Command {
        @Override
        public void execute(Bundle bundle) {
            LoginActivity.start(baseActivity);
            baseActivity.finish();
        }
    }

    private class ChangePasswordSuccessAction implements Command {
        @Override
        public void execute(Bundle bundle) {
            baseActivity.hideProgress();
            DialogUtils.show(baseActivity, getString(R.string.dlg_password_changed));
        }
    }
}
