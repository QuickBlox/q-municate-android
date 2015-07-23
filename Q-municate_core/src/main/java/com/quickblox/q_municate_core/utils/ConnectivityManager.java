package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.qb.commands.QBReloginCommand;
import com.quickblox.q_municate_core.service.ConnectivityListener;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 7/23/15.
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public class ConnectivityManager implements ConnectivityListener {

    private static ConnectivityManager INSTANCE;
    private final Context context;
    private Set<ConnectivityListener> connectivityListeners;
    private static boolean isConnectivityExists = true;

    public static boolean isConnectionExists() {
        return isConnectivityExists;
    }

    public ConnectivityManager(Context context) {
        this.context = context;
        connectivityListeners = new HashSet<>();
    }

    /**
     * Init instance of the manager
     */
    public static  ConnectivityManager getInstance (Context context){
        if(INSTANCE == null){
            synchronized (ConnectivityManager.class){
                if(INSTANCE == null){
                    INSTANCE = new ConnectivityManager(context);
                }
            }
        }
        return INSTANCE;
    }


    /**
     * Override this method in each child which need to listen connectivity state
     *
     * @param isConnected
     */
    @Override
    public void onConnectionChange(final boolean isConnected) {
        isConnectivityExists = isConnected;
        notifyAllConnectivityListeners();
        Log.d("Fixes CONNECTIVITY", "onConnectionChange in BaseActivity");
        Log.d("Fixes CONNECTIVITY", "Connection is " + isConnected);
        if (isConnected) {
            QBReloginCommand.start(context);
        } else {
//                        android.app.AlertDialog.Builder diologBuilder = new android.app.AlertDialog.Builder(getApplicationContext());
//                        diologBuilder.setTitle(getString(R.string.connection_lost_title))
//                                .setMessage(getString(R.string.connection_lost))
//                                .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                });
//                        diologBuilder.show();
            Toast.makeText(context, context.getString(R.string.connection_lost), Toast.LENGTH_LONG).show();
        }
    }

    public void addConnectivityListener(ConnectivityListener connectivityListener){
        if(connectivityListener != null) {
            connectivityListeners.add(connectivityListener);
        }
    }

    public void removeConnectivityListener(ConnectivityListener connectivityListener){
        connectivityListeners.remove(connectivityListener);
    }

    private void notifyAllConnectivityListeners() {
        for (ConnectivityListener listener : connectivityListeners){
            listener.onConnectionChange(isConnectivityExists);
        }
    }
}
