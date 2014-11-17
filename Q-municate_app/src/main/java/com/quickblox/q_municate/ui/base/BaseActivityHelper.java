package com.quickblox.q_municate.ui.base;

import android.content.Context;

//It class uses for delegate functionality from Activity
public abstract class BaseActivityHelper {

    private Context context;

    public BaseActivityHelper(Context context) {
        this.context = context;
    }


    public Context getContext() {
        return context;
    }
}