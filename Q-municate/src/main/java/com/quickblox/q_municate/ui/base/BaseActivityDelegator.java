package com.quickblox.q_municate.ui.base;

import android.content.Context;

//It class uses for delegate functionality from Activity
public abstract class BaseActivityDelegator {

    private Context context;

    public BaseActivityDelegator(Context context){
        this.context = context;
    }


    public Context getContext() {
        return context;
    }
}
