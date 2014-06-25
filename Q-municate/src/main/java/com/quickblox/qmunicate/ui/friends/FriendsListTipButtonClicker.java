package com.quickblox.qmunicate.ui.friends;

import android.view.View;

public class FriendsListTipButtonClicker implements View.OnClickListener {

    private FriendsListFragment fragment;

    public FriendsListTipButtonClicker(FriendsListFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onClick(View v) {
        fragment.onOptionsItemSelected(fragment.getSearchItem());
        fragment.getSearchItem().expandActionView();
    }
}