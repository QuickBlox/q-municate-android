package com.quickblox.q_municate.utils.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

import com.quickblox.q_municate.App;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public static final String ACTION_LOCAL_CONNECTIVITY = "action_local_connectivity";
    public static final String EXTRA_IS_ACTIVE_CONNECTION = "extra_is_active_connection";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        boolean activeConnection = isActiveConnection(context);
        notifyAboutChangeConnection(activeConnection);
    }

    private boolean isActiveConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

        if (activeNetworkInfo != null) {
            return activeNetworkInfo.isConnected();
        }

        return false;
    }

    private void notifyAboutChangeConnection(boolean activeConnection) {
        Intent intent = new Intent(ACTION_LOCAL_CONNECTIVITY);
        intent.putExtra(EXTRA_IS_ACTIVE_CONNECTION, activeConnection);
        LocalBroadcastManager.getInstance(App.getInstance()).sendBroadcast(intent);
    }
}