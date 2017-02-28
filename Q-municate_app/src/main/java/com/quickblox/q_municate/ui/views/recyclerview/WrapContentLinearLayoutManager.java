package com.quickblox.q_municate.ui.views.recyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by pelipets on 2/28/17.
 */

public class WrapContentLinearLayoutManager extends LinearLayoutManager {

    public final static String TAG ="WrapContentLLM";

    public WrapContentLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}