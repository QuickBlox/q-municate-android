package com.quickblox.qmunicate.core.ui;

import android.os.Bundle;
import android.support.v4.content.Loader;

public interface LoaderManager<T> {

    void onLoaderReset(Loader<LoaderResult<T>> loader);

    void onLoaderException(int id, Exception e);

    Loader<LoaderResult<T>> onLoaderCreate(int id, Bundle args);

    BaseLoader<T> runLoader(int id);

    BaseLoader<T> runLoader(int id, BaseLoader.Args args);

    @SuppressWarnings("unchecked")
    <L extends Loader<?>> L getLoader(int id);
}
