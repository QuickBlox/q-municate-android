package com.quickblox.q_municate.utils.listeners;

import com.quickblox.q_municate_core.legacy.service.QBService;

public interface ServiceConnectionListener {

    void onConnectedToService(QBService service);
}