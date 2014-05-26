package com.quickblox.qmunicate.utils;

import android.view.View;
import com.quickblox.qmunicate.ui.main.FriendsListFragment;

/**
 * Created by stas on 26.05.14.
 */
public class FriendsListTipButtonClicker implements View.OnClickListener{
    FriendsListFragment fragment;

    public FriendsListTipButtonClicker(FriendsListFragment fragment){
        this.fragment = fragment;
    }

    @Override
            public void onClick(View v) {
                fragment.onOptionsItemSelected(fragment.getSearchItem());
                fragment.getSearchItem().expandActionView();
            }
}
