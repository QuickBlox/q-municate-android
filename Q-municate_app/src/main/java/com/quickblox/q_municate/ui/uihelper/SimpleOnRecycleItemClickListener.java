package com.quickblox.q_municate.ui.uihelper;

import android.view.View;

import com.quickblox.q_municate.core.listeners.OnRecycleItemClickListener;

public class SimpleOnRecycleItemClickListener<T> implements OnRecycleItemClickListener<T> {

    @Override
    public void onItemClicked(View view, T entity, int position) {
    }

    @Override
    public void onItemLongClicked(View view, T entity, int position) {
    }
}