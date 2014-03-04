package com.quickblox.qmunicate.ui.base;

import android.content.Loader;
import android.os.Bundle;

import com.quickblox.qmunicate.core.ui.BaseLoader;
import com.quickblox.qmunicate.core.ui.LoaderHelper;
import com.quickblox.qmunicate.core.ui.LoaderManager;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.core.ui.OnLoadFinishedListener;

public abstract class LoaderActivity<T> extends BaseActivity implements OnLoadFinishedListener<T>, LoaderManager<T> {

    private LoaderHelper<T> loaderHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loaderHelper = new QMLoaderHelper<T>(this, this, this);
    }

    @Override
    public void onLoaderException(int id, Exception e) {
        loaderHelper.onLoaderException(id, e);
    }

    @Override
    public <L extends Loader<?>> L getLoader(int id) {
        return loaderHelper.getLoader(id);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<T>> loader) {
        loaderHelper.onLoaderReset(loader);
    }

    @Override
    public BaseLoader<T> runLoader(int id) {
        return loaderHelper.runLoader(id);
    }

    @Override
    public BaseLoader<T> runLoader(int id, BaseLoader.Args args) {
        return loaderHelper.runLoader(id, args);
    }
}