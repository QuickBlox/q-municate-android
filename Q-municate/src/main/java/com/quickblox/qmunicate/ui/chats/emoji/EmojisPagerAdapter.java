package com.quickblox.qmunicate.ui.chats.emoji;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class EmojisPagerAdapter extends FragmentPagerAdapter {

    private List<EmojiGridFragment> fragments;

    public EmojisPagerAdapter(FragmentManager fm, List<EmojiGridFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}