package com.quickblox.q_municate.ui.activities.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.authorization.LandingActivity;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.activities.changepassword.ChangePasswordActivity;
import com.quickblox.q_municate.ui.activities.profile.MyProfileActivity;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate.utils.helpers.FacebookHelper;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.rest.QBLogoutCompositeCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.Utils;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class SettingsActivity extends BaseLogeableActivity {

    @Bind(R.id.push_notification_switch)
    SwitchCompat pushNotificationSwitch;

    @Bind(R.id.change_password_view)
    LinearLayout changePasswordView;

    @Bind(R.id.version_textview)
    TextView versionView;

    public static void start(Context context) {
        Intent intent = new Intent(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_settings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpActionBarWithUpButton();

        fillUI();

        addActions();
    }

    private void fillUI() {
        pushNotificationSwitch.setChecked(appSharedHelper.isEnablePushNotifications());
        changePasswordView.setVisibility(
                LoginType.FACEBOOK.equals(AppSession.getSession().getLoginType()) ? View.GONE : View.VISIBLE);
        versionView.setText(getString(R.string.stn_version, Utils.getAppVersionName(this)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION, new LogoutSuccessAction());
        addAction(QBServiceConsts.LOGOUT_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.LOGOUT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOGOUT_FAIL_ACTION);

        updateBroadcastActionList();
    }

    @OnClick(R.id.profile_button)
    void showProfile() {
        MyProfileActivity.start(this);
    }

    @OnCheckedChanged(R.id.push_notification_switch)
    void enablePushNotification(boolean enable) {
        appSharedHelper.saveEnablePushNotifications(enable);
    }

    @OnClick(R.id.change_password_button)
    void changePassword() {
        ChangePasswordActivity.start(this);
    }

    @OnClick(R.id.logout_button)
    void logout() {
        TwoButtonsDialogFragment.show(
                getSupportFragmentManager(),
                R.string.dlg_logout,
                R.string.dlg_confirm,
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        showProgress();
                        FacebookHelper.logout();
                        QBLogoutCompositeCommand.start(SettingsActivity.this);
                    }
                });
    }

    private void startLandingScreen() {
        Intent intent = new Intent(this, LandingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        LandingActivity.start(this, intent);
    }

    private class LogoutSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            startLandingScreen();
        }
    }
}