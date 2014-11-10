package com.quickblox.q_municate_core.core.ui;

import android.content.AsyncTaskLoader;
import android.content.Context;

public abstract class BaseLoader<T> extends AsyncTaskLoader<LoaderResult<T>> {

    protected Args args;

    public BaseLoader(Context context) {
        super(context);
    }

    @Override
    public LoaderResult<T> loadInBackground() {
        try {
            return new LoaderResult<T>(performInBackground());
        } catch (Exception e) {
            return new LoaderResult<T>(e);
        }
    }

    public void setArgs(Args args) {
        this.args = args;
    }

    public abstract T performInBackground() throws Exception;

    public static class Args {
    }

    public static class Arguments extends BaseLoader.Args {
        public int page;
        public int perPage;
    }
}
