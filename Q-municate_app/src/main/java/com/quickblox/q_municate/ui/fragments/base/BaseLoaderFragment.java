package com.quickblox.q_municate.ui.fragments.base;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

public abstract class BaseLoaderFragment<T> extends BaseFragment implements LoaderManager.LoaderCallbacks<T> {

    protected Loader<T> loader;

    protected void initDataLoader(int id) {
        getLoaderManager().initLoader(id, null, this).forceLoad();
    }

    protected abstract Loader<T> createDataLoader();

    @Override
    public Loader<T> onCreateLoader(int id, Bundle args) {
        loader = createDataLoader();
        return loader;
    }

    @Override
    public void onLoaderReset(Loader<T> loader) {
        // nothing by default.
    }

    protected void onChangedData() {
        loader.onContentChanged();
    }
}