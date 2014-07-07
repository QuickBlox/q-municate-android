package com.quickblox.qmunicate.ui.chats.smiles;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.views.indicator.IconPagerAdapter;
import com.quickblox.qmunicate.ui.views.smiles.SmileysConvertor;

import java.util.ArrayList;
import java.util.List;

public class SmilesTabFragmentAdapter extends FragmentStatePagerAdapter implements IconPagerAdapter {

    public static final String RESOURCE_KEY = "RESOURCE_KEY";
    private static final int SMILES_COUNT_PER_PAGE = 24;

    private List<Integer> smilesResources;

    public SmilesTabFragmentAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        smilesResources = SmileysConvertor.getMapAsList();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new SmileTabFragment();
        Bundle bundle = new Bundle();
        int startingPosition = position * SMILES_COUNT_PER_PAGE;
        int endingPosition;
        if (smilesResources.size() / (SMILES_COUNT_PER_PAGE * (position + 1)) > 0) {
            endingPosition = SMILES_COUNT_PER_PAGE * (position + 1);
        } else {
            endingPosition = startingPosition + smilesResources.size() % SMILES_COUNT_PER_PAGE;
        }
        List<Integer> listToSend = new ArrayList<Integer>();
        listToSend.addAll(smilesResources.subList(startingPosition, endingPosition));
        bundle.putIntegerArrayList(RESOURCE_KEY, (ArrayList<Integer>) listToSend);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getIconResId(int index) {
        return R.drawable.tour_page_indicator_state;
    }

    @Override
    public int getCount() {
        int division = smilesResources.size() / SMILES_COUNT_PER_PAGE;
        int mod = smilesResources.size() % SMILES_COUNT_PER_PAGE;
        return division + (mod == 0 ? 0 : 1);
    }
}