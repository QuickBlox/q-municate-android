package com.quickblox.q_municate_core.service;

/**
 * Created on 7/22/15.
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public interface ConnectivityListener {

    /**
     * Called if one of connectivity {@link android.net.ConnectivityManager#TYPE_MOBILE} or
     * {@link android.net.ConnectivityManager#TYPE_WIFI}  was enabled/disabled
     * @param isConnected
     */
    void onConnectionChange(boolean isConnected);
}
