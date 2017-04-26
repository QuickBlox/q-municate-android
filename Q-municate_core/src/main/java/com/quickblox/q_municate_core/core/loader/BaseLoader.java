package com.quickblox.q_municate_core.core.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.quickblox.q_municate_db.managers.DataManager;

public abstract class BaseLoader<T> extends AsyncTaskLoader<T> {

    private static final String TAG = BaseLoader.class.getSimpleName();

    private T objectsList;
    protected DataManager dataManager;
    public volatile boolean isLoading;

    public BaseLoader(Context context, DataManager dataManager) {
        // Loaders may be used across multiple Activitys (assuming they aren't
        // bound to the LoaderManager), so NEVER hold a reference to the context
        // directly. Doing so will cause you to leak an entire Activity's context.
        // The superclass constructor will store a reference to the Application
        // Context instead, and can be retrieved with a call to getContext().
        super(context);
        this.dataManager = dataManager;
    }

    @Override
    public void deliverResult(T objectsList) {
        isLoading = false;
        this.objectsList = objectsList;

        if (isStarted()) {
            Log.i(TAG, "+++ Delivering results to the LoaderManager! +++");
            // If the Loader is in a started state, have the superclass deliver the
            // results to the client.
            super.deliverResult(objectsList);
        }
    }

    @Override
    protected void onStartLoading() {
        Log.i(TAG, "+++ onStartLoading() called! +++");

        if (objectsList != null) {
            // Deliver any previously loaded data immediately.
            Log.i(TAG, "+++ Delivering previously loaded data to the client...");
            deliverResult(objectsList);
        }

        if (takeContentChanged()) {
            // When the observer detects a new installed application, it will call
            // onContentChanged() on the Loader, which will cause the next call to
            // takeContentChanged() to return true. If this is ever the case (or if
            // the current data is null), we force a new load.
            Log.i(TAG, "+++ A content change has been detected... so force load! +++");
            forceLoad();
        } else if (objectsList == null) {
            // If the current data is null... then we should make it non-null! :)
            Log.i(TAG, "+++ The current data is data is null... so force load! +++");
            loadData();
        }
    }

    public void loadData(){
        forceLoad();
    }

    @Override
    public void forceLoad() {
        isLoading = true;
        Log.i(TAG, "+++ forceLoad() called! +++");
        super.forceLoad();
    }

    @Override
    protected void onStopLoading() {
        Log.i(TAG, "+++ onStopLoading() called! +++");
        isLoading = false;
        cancelLoad();
    }

    @Override
    protected void onReset() {
        // Ensure the loader is stopped.
        onStopLoading();

        if (objectsList != null) {
            objectsList = null;
        }
    }

    @Override
    public void onCanceled(T objectsList) {
        Log.i(TAG, "+++ onCanceled() called! +++");
        this.objectsList = objectsList;
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(objectsList);
    }

    @Override
    public T loadInBackground() {
        isLoading = true;
        return getItems();
    }

    protected abstract T getItems();
}