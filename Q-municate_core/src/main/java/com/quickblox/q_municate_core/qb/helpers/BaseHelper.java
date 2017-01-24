package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseHelper {

    protected Context context;

    public BaseHelper(Context context) {
        this.context = context;
    }
}