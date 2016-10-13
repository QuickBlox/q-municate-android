package com.quickblox.q_municate_base_service;

import com.quickblox.q_municate_base_cache.QMBaseCache;

public abstract class QMBaseService {

    private QMServiceManagerListener serviceManagerListener;
    private QMBaseCache cache;


    public void init(QMBaseCache cache){
        this.cache = cache;
        serviceWillStart();
    }

    protected abstract void serviceWillStart();

}
