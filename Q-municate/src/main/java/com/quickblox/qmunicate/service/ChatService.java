package com.quickblox.qmunicate.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.quickblox.module.chat.smack.SmackAndroid;

public class ChatService extends Service {

    private SmackAndroid smackAndroid;

    public ChatService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        smackAndroid = SmackAndroid.init(this);
    }

    @Override
    public void onDestroy() {
        smackAndroid.onDestroy();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
