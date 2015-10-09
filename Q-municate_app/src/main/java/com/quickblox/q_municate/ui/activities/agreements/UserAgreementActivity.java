package com.quickblox.q_municate.ui.activities.agreements;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;

import butterknife.Bind;

public class UserAgreementActivity extends BaseActivity {

    @Bind(R.id.user_agreement_webview)
    WebView userAgreementWebView;

    public static void start(Context context) {
        Intent intent = new Intent(context, UserAgreementActivity.class);
        context.startActivity(intent);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activateButterKnife();
        setContentView(R.layout.activity_user_agreement);

        initActionBar();
        initUserAgreementWebView();
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initUserAgreementWebView() {
        userAgreementWebView.getSettings().setJavaScriptEnabled(true);
        String policyLink = getString(R.string.app_policy_link);
        userAgreementWebView.loadUrl(policyLink);
    }
}