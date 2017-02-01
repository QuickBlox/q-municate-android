package com.quickblox.q_municate.ui.activities.about;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.agreements.UserAgreementActivity;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.utils.StringObfuscator;

import butterknife.Bind;
import butterknife.OnClick;

public class AboutActivity extends BaseLoggableActivity {

    @Bind(R.id.app_version_textview)
    TextView appVersionTextView;

    public static void start(Context context) {
        Intent intent = new Intent(context, AboutActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_about;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();
        fillUI();
    }

    private void initFields() {
        title = getString(R.string.about_title);
    }

    private void fillUI() {
        appVersionTextView.setText(StringObfuscator.getAppVersionName());
    }

    @OnClick(R.id.license_button)
    void openUserAgreement(View view) {
        UserAgreementActivity.start(this);
    }
}