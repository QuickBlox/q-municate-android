package com.quickblox.qmunicate.core.ui;

import android.os.Bundle;
import android.support.v4.content.Loader;

public class GenericCallback<T> extends BaseLoaderCallback<T> {

    private final LoaderManager<T> manager;

    public GenericCallback(OnLoadFinishedListener<T> listener, LoaderManager<T> manager) {
        super(listener);
        this.manager = manager;
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<T>> loaderResultLoader) {
        super.onLoaderReset(loaderResultLoader);
        manager.onLoaderReset(loaderResultLoader);
    }

    @Override
    public Loader<LoaderResult<T>> onCreateLoader(int id, Bundle bundle) {
        return manager.onLoaderCreate(id, bundle);
    }
}
