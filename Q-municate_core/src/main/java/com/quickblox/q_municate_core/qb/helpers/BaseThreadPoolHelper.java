package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseThreadPoolHelper extends BaseHelper {

    //ThreadPoolExecutor
    private static final int THREAD_POOL_SIZE = 3;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    protected ThreadPoolExecutor threadPoolExecutor;

    public BaseThreadPoolHelper(Context context) {
        super(context);
        initThreads();
    }

    private void initThreads() {
        BlockingQueue<Runnable> threadQueue = new LinkedBlockingQueue<>();
        threadPoolExecutor = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, threadQueue);
        threadPoolExecutor.allowCoreThreadTimeOut(true);
    }
}
