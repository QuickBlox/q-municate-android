package com.quickblox.q_municate_base_service;

import com.quickblox.q_municate_base_cache.QMBaseCache;

public abstract class QMBaseService {

    private QMServiceManagerListener serviceManagerListener;
    private QMBaseCache cache;

    public void init(QMServiceManagerListener serviceManagerListener){
        this.serviceManagerListener = serviceManagerListener;
        serviceWillStart();
    }

    public void init(QMServiceManagerListener serviceManagerListener, QMBaseCache cache){
        this.serviceManagerListener = serviceManagerListener;
        this.cache = cache;
        serviceWillStart();
    }

    protected abstract void serviceWillStart();

}
