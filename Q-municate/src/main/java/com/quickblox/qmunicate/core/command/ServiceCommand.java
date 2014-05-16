package com.quickblox.qmunicate.core.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;
import com.quickblox.qmunicate.service.QBServiceConsts;

public abstract class ServiceCommand implements Command {

    protected final Context context;
    protected final String successAction;
    protected final String failAction;

    public ServiceCommand(Context context, String successAction, String failAction) {
        this.context = context;
        this.successAction = successAction;
        this.failAction = failAction;
    }

    public void execute(Bundle bundle) {
        Bundle result;
        try {
            result = perform(bundle);
            sendResult(result, successAction);
        } catch (Exception e) {
            e.printStackTrace();
            result = new Bundle();
            result.putSerializable(QBServiceConsts.EXTRA_ERROR, e);
            sendResult(result, failAction);
        }
    }

    protected void sendResult(Bundle result, String action) {
        Intent intent = new Intent(action);
        if (null != result) {
            intent.putExtras(result);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected abstract Bundle perform(Bundle extras) throws Exception;
}
