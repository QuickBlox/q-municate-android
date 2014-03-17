package com.quickblox.qmunicate.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.utils.ErrorUtils;

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {

    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Bundle result = intent.getExtras();
        if (null != result && result.containsKey(QBServiceConsts.EXTRA_ERROR)) {
            onException((Exception) result.getSerializable(QBServiceConsts.EXTRA_ERROR));
        } else {
            onResult(result);
        }
    }

    public abstract void onResult(Bundle bundle);

    public void onException(Exception e) {
        ErrorUtils.showError(context, e);
    }
}