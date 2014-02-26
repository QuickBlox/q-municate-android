package com.quickblox.qmunicate.core.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.Loader;

/**
 * TODO create common generic error handler and remove concrete implementations from MB and MW
 */
public abstract class LoaderHelper<T> implements LoaderManager<T> {

    private static final String TAG = LoaderHelper.class.getName();

    protected final GenericCallback<T> callback;
    protected final FragmentActivity activity;

    public LoaderHelper(FragmentActivity activity, OnLoadFinishedListener<T> loadFinishedListener, LoaderManager<T> loaderManager) {
        this.activity = activity;
        callback = new GenericCallback<T>(loadFinishedListener, loaderManager);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<T>> loader) {
        // nothing by default
    }

    @Override
    public Loader<LoaderResult<T>> onLoaderCreate(int id, Bundle args) {
        throw new UnsupportedOperationException("This functionality cannot be delegated!");
    }

    @Override
    public BaseLoader<T> runLoader(int id) {
        return runLoader(id, null);
    }

    @Override
    public BaseLoader<T> runLoader(int id, BaseLoader.Args args) {
        BaseLoader<T> loader = getLoader(id);
        if (loader != null) {
            loader.reset();
        } else {
            loader = (BaseLoader<T>) activity.getSupportLoaderManager().initLoader(id, null, callback);
        }
        if (args != null) {
            loader.setArgs(args);
        }
        loader.forceLoad();
        return loader;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <L extends Loader<?>> L getLoader(int id) {
        return (L) activity.getSupportLoaderManager().getLoader(id);
    }
}
