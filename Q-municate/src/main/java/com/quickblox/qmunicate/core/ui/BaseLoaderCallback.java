package com.quickblox.qmunicate.core.ui;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import java.lang.ref.WeakReference;

public abstract class BaseLoaderCallback<T> implements LoaderManager.LoaderCallbacks<LoaderResult<T>> {

    private WeakReference<OnLoadFinishedListener<T>> listener;

    public BaseLoaderCallback(final OnLoadFinishedListener<T> listener) {
        this.listener = new WeakReference<OnLoadFinishedListener<T>>(listener);
    }

    @Override
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void onLoadFinished(final Loader<LoaderResult<T>> loader, final LoaderResult<T> result) {
        final OnLoadFinishedListener<T> onLoadFinish = listener.get();

        if (onLoadFinish != null && result != null) {
            if (result.getException() != null) {
                onLoadFinish.onLoaderException(loader.getId(), result.getException());
            } else {
                onLoadFinish.onLoaderResult(loader.getId(), result.getResult());
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader<LoaderResult<T>> loaderResultLoader) {
        if (listener != null) {
            listener.clear();
            listener = null;
        }
    }
}
