package com.quickblox.q_municate_base_service;

import com.quickblox.q_municate_base_cache.QMBaseCache;
import com.quickblox.q_municate_base_cache.model.BaseModel;

public abstract class QMBaseService implements QMMemoryCacheListener {

    private QMServiceManagerListener serviceManagerListener;

    public void init(){
        serviceWillStart();
    }

    public void init(QMServiceManagerListener serviceManagerListener){
        this.serviceManagerListener = serviceManagerListener;
        serviceWillStart();
    }

    public abstract void init(QMBaseCache<BaseModel> cache);


    protected abstract void serviceWillStart();


    //Implements QMMemoryCacheListener

    @Override
    public void free() {

    }

}
