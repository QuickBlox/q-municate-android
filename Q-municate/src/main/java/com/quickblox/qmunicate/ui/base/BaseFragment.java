package com.quickblox.qmunicate.ui.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.quickblox.qmunicate.App;

public abstract class BaseFragment extends Fragment {

    protected static final int FRIEND_LIST_LOADER_ID = 1;
    protected static final String ARG_TITLE = "title";
    protected App app;
    private String title;

    public String getTitle() {
        return title;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = App.getInstance();
        if (getArguments() != null) {
            title = getArguments().getString(ARG_TITLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActionBar().setTitle(title);
    }

    protected ActionBar getActionBar() {
        return ((ActionBarActivity) getActivity()).getSupportActionBar();
    }
}
