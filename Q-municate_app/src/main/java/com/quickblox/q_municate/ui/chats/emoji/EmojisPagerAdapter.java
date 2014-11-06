package com.quickblox.q_municate.ui.chats.emoji;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class EmojisPagerAdapter extends FragmentPagerAdapter {

    private List<EmojiGridFragment> fragmentsList;

    public EmojisPagerAdapter(FragmentManager fm, List<EmojiGridFragment> fragmentsList) {
        super(fm);
        this.fragmentsList = fragmentsList;
    }

    @Override
    public Fragment getItem(int i) {
        return fragmentsList.get(i);
    }

    @Override
    public int getCount() {
        return fragmentsList.size();
    }
}