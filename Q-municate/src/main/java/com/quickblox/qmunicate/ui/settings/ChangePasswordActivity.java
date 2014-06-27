package com.quickblox.qmunicate.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.qb.commands.QBChangePasswordCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseLogeableActivity;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.ValidationUtils;

public class ChangePasswordActivity extends BaseLogeableActivity {

    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private ValidationUtils validationUtils;
    private Resources resources;

    public static void start(Context context) {
        Intent intent = new Intent(context, ChangePasswordActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        resources = getResources();
        canPerformLogout.set(false);

        initUI();

        addActions();
    }

    public void changePasswordOnClickListener(View view) {
        String oldPasswordText = oldPasswordEditText.getText().toString();
        String newPasswordText = newPasswordEditText.getText().toString();
        if (validationUtils.isValidChangePasswordData(oldPasswordText, newPasswordText)) {
            QBUser user = AppSession.getSession().getUser();
            user.setOldPassword(oldPasswordText);
            user.setPassword(newPasswordText);
            showProgress();
            QBChangePasswordCommand.start(this, user);
        }
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

    private void initUI() {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        oldPasswordEditText = _findViewById(R.id.old_password_edittext);
        newPasswordEditText = _findViewById(R.id.new_password_edittext);
        validationUtils = new ValidationUtils(this, new EditText[]{oldPasswordEditText, newPasswordEditText},
                new String[]{resources.getString(R.string.cpw_not_old_password_field_entered), resources
                        .getString(R.string.cpw_not_new_password_field_entered)}
        );
    }

    private void addActions() {
        addAction(QBServiceConsts.CHANGE_PASSWORD_SUCCESS_ACTION, new ChangePasswordSuccessAction());
        addAction(QBServiceConsts.CHANGE_PASSWORD_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    private void clearFields() {
        oldPasswordEditText.setText(Consts.EMPTY_STRING);
        newPasswordEditText.setText(Consts.EMPTY_STRING);
    }

    private void saveUserCredentials(QBUser user) {
        user.setPassword(newPasswordEditText.getText().toString());
        AppSession.getSession().updateUser(user);
    }

    private class ChangePasswordSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            saveUserCredentials(user);
            hideProgress();
            clearFields();
            DialogUtils.showLong(ChangePasswordActivity.this, getString(R.string.dlg_password_changed));
        }
    }
}