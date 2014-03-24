package com.quickblox.qmunicate.ui.base;

import android.app.Fragment;
import android.os.Bundle;

import com.quickblox.qmunicate.App;

public abstract class BaseFragment extends Fragment {

    protected App app;
    protected BaseActivity baseActivity;
    protected String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseActivity = (BaseActivity) getActivity();
        app = App.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        baseActivity.getActionBar().setTitle(title);
    }
}
