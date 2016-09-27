package com.quickblox.q_municate_base_service;

import com.quickblox.q_municate_base_cache.QMBaseCache;
import com.quickblox.q_municate_base_cache.model.BaseModel;

public interface QMBaseService {

    void init();

    void init(QMBaseCache<BaseModel> cache);

}
