package com.quickblox.q_municate.ui.activities.changepassword;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.quickblox.q_municate_core.qb.commands.QBLoginChatCompositeCommand;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.commands.QBChangePasswordCommand;
import com.quickblox.q_municate_core.qb.commands.QBLogoutAndDestroyChatCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.activities.base.BaseLogeableActivity;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate.utils.ValidationUtils;

import butterknife.Bind;

public class ChangePasswordActivity extends BaseLogeableActivity {

    @Bind(R.id.old_password_edittext)
    EditText oldPasswordEditText;

    @Bind(R.id.new_password_edittext)
    EditText newPasswordEditText;

    private ValidationUtils validationUtils;
    private Resources resources;
    private QBUser user;
    private String oldPasswordText;
    private String newPasswordText;

    public static void start(Context context) {
        Intent intent = new Intent(context, ChangePasswordActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);

        activateButterKnife();

        initActionBar();
        initFields();

        addActions();
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initFields() {
        resources = getResources();
        canPerformLogout.set(false);
        user = AppSession.getSession().getUser();
        validationUtils = new ValidationUtils(this, new EditText[]{oldPasswordEditText, newPasswordEditText},
                new String[]{resources.getString(R.string.cpw_not_old_password_field_entered), resources
                        .getString(R.string.cpw_not_new_password_field_entered)}
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addActions() {
        addAction(QBServiceConsts.CHANGE_PASSWORD_SUCCESS_ACTION, new ChangePasswordSuccessAction());
        addAction(QBServiceConsts.CHANGE_PASSWORD_FAIL_ACTION, new ChangePasswordFailAction());

        addAction(QBServiceConsts.LOGOUT_CHAT_SUCCESS_ACTION, new LogoutChatSuccessAction());
        addAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION, new LoginChatSuccessAction());

        addAction(QBServiceConsts.LOGOUT_CHAT_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.CHANGE_PASSWORD_SUCCESS_ACTION);
        removeAction(QBServiceConsts.CHANGE_PASSWORD_FAIL_ACTION);

        removeAction(QBServiceConsts.LOGOUT_CHAT_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION);

        removeAction(QBServiceConsts.LOGOUT_CHAT_FAIL_ACTION);
        removeAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_FAIL_ACTION);

        updateBroadcastActionList();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    public void changePasswordOnClickListener(View view) {
        oldPasswordText = oldPasswordEditText.getText().toString();
        newPasswordText = newPasswordEditText.getText().toString();
        if (validationUtils.isValidChangePasswordData(oldPasswordText, newPasswordText)) {
            updatePasswords(oldPasswordText, newPasswordText);
            showProgress();
            QBChangePasswordCommand.start(this, user);
        }
    }

    private void updatePasswords(String oldPasswordText, String newPasswordText) {
        user.setOldPassword(oldPasswordText);
        user.setPassword(newPasswordText);
    }

    private void clearFields() {
        oldPasswordEditText.setText(ConstsCore.EMPTY_STRING);
        newPasswordEditText.setText(ConstsCore.EMPTY_STRING);
    }

    private void clearFieldNewPassword() {
        newPasswordEditText.setText(ConstsCore.EMPTY_STRING);
    }

    private void saveUserCredentials(QBUser user) {
        user.setPassword(newPasswordEditText.getText().toString());
        AppSession.getSession().updateUser(user);
    }

    private void logoutChat(){
        showProgress();
        QBLogoutAndDestroyChatCommand.start(this, true);
    }

    @Override
    protected void onFailAction(String action) {
        super.onFailAction(action);
        if (QBServiceConsts.LOGOUT_CHAT_FAIL_ACTION.equals(action) || QBServiceConsts.LOGIN_FAIL_ACTION.equals(action)) {
            hideProgress();
            finish();
        }
    }

    private class LogoutChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBLoginChatCompositeCommand.start(ChangePasswordActivity.this);
        }
    }

    private class LoginChatSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            finish();
        }
    }

    private class ChangePasswordSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            saveUserCredentials(user);
            DialogUtils.showLong(ChangePasswordActivity.this, getString(R.string.dlg_password_changed));
            logoutChat();
        }
    }

    private class ChangePasswordFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            hideProgress();
            DialogUtils.showLong(ChangePasswordActivity.this, exception.getMessage());
            updatePasswords(oldPasswordText, oldPasswordText);
            clearFieldNewPassword();
        }
    }
}