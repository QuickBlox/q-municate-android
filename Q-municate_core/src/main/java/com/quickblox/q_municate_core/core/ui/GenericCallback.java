package com.quickblox.q_municate_core.core.ui;

import android.content.Loader;
import android.os.Bundle;

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
