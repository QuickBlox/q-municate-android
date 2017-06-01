package com.quickblox.q_municate_core.network;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.Task;
import com.google.android.gms.gcm.TaskParams;
import com.quickblox.q_municate_core.qb.commands.chat.QBLoginChatCompositeCommand;


public class NetworkGCMTaskService extends GcmTaskService {

    private final static String TASK_NETWORK_TAG = "network";
    private final static String EXTRA_KEY = "what";

    @Override
    public int onRunTask(TaskParams taskParams) {
        QBLoginChatCompositeCommand.start(this);
        return GcmNetworkManager.RESULT_SUCCESS;
    }

    public static void scheduleOneOff(Context context, String what) {
        Bundle extras = new Bundle();
        extras.putString(EXTRA_KEY, what);
        OneoffTask task = new OneoffTask.Builder()
                .setService(NetworkGCMTaskService.class)
                .setTag(TASK_NETWORK_TAG)
                .setExtras(extras)
//                Execution window: The time period in which the task will execute.
//                First param is the lower bound and the second is the upper bound (both are in seconds).
                .setExecutionWindow(0L, 30L)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setUpdateCurrent(true)
                .build();

        GcmNetworkManager.getInstance(context).schedule(task);
    }
}
