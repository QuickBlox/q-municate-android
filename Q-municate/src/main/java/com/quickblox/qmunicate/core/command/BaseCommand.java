package com.quickblox.qmunicate.core.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.quickblox.qmunicate.service.QBServiceConsts;

public abstract class BaseCommand {

    protected final Context context;
    protected final String resultAction;

    public BaseCommand(Context context, String resultAction) {
        this.context = context;
        this.resultAction = resultAction;
    }

    public void execute(Bundle extras) {
        Bundle result;
        try {
            result = perform(extras);
        } catch (Exception e) {
            result = new Bundle();
            result.putSerializable(QBServiceConsts.EXTRA_ERROR, e);
        }

        Intent intent = new Intent(resultAction);
        if (null != result) {
            intent.putExtras(result);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected abstract Bundle perform(Bundle extras) throws Exception;
}
