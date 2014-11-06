package com.quickblox.q_municate_core.core.ui;

import android.app.Activity;
import android.content.Loader;
import android.os.Bundle;

public abstract class LoaderHelper<T> implements LoaderManager<T> {

    protected final GenericCallback<T> callback;
    protected final Activity activity;

    public LoaderHelper(Activity activity, OnLoadFinishedListener<T> loadFinishedListener, LoaderManager<T> loaderManager) {
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
            loader = (BaseLoader<T>) activity.getLoaderManager().restartLoader(id, null, callback);
        } else {
            loader = (BaseLoader<T>) activity.getLoaderManager().initLoader(id, null, callback);
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
        return (L) activity.getLoaderManager().getLoader(id);
    }
}
