package com.quickblox.q_municate.ui.activities.agreements;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;

import butterknife.Bind;

public class UserAgreementActivity extends BaseLoggableActivity {

    @Bind(R.id.user_agreement_webview)
    WebView userAgreementWebView;

    public static void start(Context context) {
        Intent intent = new Intent(context, UserAgreementActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_user_agreement;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();
        initUserAgreementWebView();
    }

    private void initFields() {
        title = getString(R.string.user_agreement_title);
    }

    private void initUserAgreementWebView() {
        userAgreementWebView.getSettings().setJavaScriptEnabled(true);
        String policyLink = getString(R.string.app_policy_link);
        userAgreementWebView.loadUrl(policyLink);
    }
}