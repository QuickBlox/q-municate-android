package com.quickblox.qmunicate.ui.base;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.utils.DialogUtils;

public abstract class BaseActivity extends Activity {
    public static final int DOUBLE_BACK_DELAY = 2000;

    private static final String TAG = BaseActivity.class.getSimpleName();
    protected App app;
    protected ActionBar actionBar;

    protected boolean useDoubleBackPressed;
    protected boolean doubleBackToExitPressedOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = App.getInstance();
        actionBar = this.getActionBar();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce || !useDoubleBackPressed) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        DialogUtils.show(this, getString(R.string.dlg_click_back_again));
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, DOUBLE_BACK_DELAY);
    }

    @SuppressWarnings("unchecked")
    protected <T> T _findViewById(int viewId) {
        return (T) findViewById(viewId);
    }
}