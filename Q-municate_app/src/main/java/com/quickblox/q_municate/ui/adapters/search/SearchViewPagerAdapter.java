package com.quickblox.q_municate.ui.adapters.search;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.quickblox.q_municate.utils.listeners.SearchListener;
import com.quickblox.q_municate.ui.fragments.search.GlobalSearchFragment;
import com.quickblox.q_municate.ui.fragments.search.LocalSearchFragment;

public class SearchViewPagerAdapter extends FragmentStatePagerAdapter {

    public static final int COUNT_TYPES = 2;
    public static final int LOCAl_SEARCH = 0;
    public static final int GLOBAL_SEARCH = 1;

    private LocalSearchFragment localSearchFragment;
    private GlobalSearchFragment globalSearchFragment;

    public SearchViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        localSearchFragment = LocalSearchFragment.newInstance();
        globalSearchFragment = GlobalSearchFragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position) {
            case LOCAl_SEARCH:
                fragment = localSearchFragment;
                break;
            case GLOBAL_SEARCH:
                fragment = globalSearchFragment;
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return COUNT_TYPES;
    }

    public void prepareSearch(int position) {
        SearchListener searchListener = getSearchListener(position);
        if (searchListener != null) {
            searchListener.prepareSearch();
        }
    }

    public void search(int position, String searchQuery) {
        SearchListener searchListener = getSearchListener(position);
        if (searchListener != null) {
            searchListener.search(searchQuery);
        }
    }

    public void cancelSearch(int position) {
        SearchListener searchListener = getSearchListener(position);
        if (searchListener != null) {
            searchListener.cancelSearch();
        }
    }

    private SearchListener getSearchListener(int position) {
        switch (position) {
            case LOCAl_SEARCH:
                return localSearchFragment;
            case GLOBAL_SEARCH:
                return globalSearchFragment;
        }
        return null;
    }
}