package com.quickblox.q_municate_core.qb.commands.push;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.q_municate_core.core.command.CompositeServiceCommand;
import com.quickblox.q_municate_core.network.NetworkGCMTaskService;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

public class QBPushCallCompositeCommand extends CompositeServiceCommand {
    private static final String TAG = QBPushCallCompositeCommand.class.getSimpleName();

    private static boolean isRunning;

    public QBPushCallCompositeCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context) {
        Log.i(TAG, "start");
        setIsRunning(true);
        Intent intent = new Intent(QBServiceConsts.PUSH_CALL_COMPOSITE_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        try {
            super.perform(extras);
        } catch (XMPPException | IOException | SmackException e) {
            NetworkGCMTaskService.scheduleOneOff(context, "");
            throw e;
        }
        return extras;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static void setIsRunning(boolean isRunning) {
        QBPushCallCompositeCommand.isRunning = isRunning;
    }
}