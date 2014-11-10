package com.quickblox.q_municate_core.core.concurrency;

import android.os.AsyncTask;

public abstract class BaseAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private Exception exception;

    @Override
    protected Result doInBackground(Params... params) {
        try {
            return performInBackground(params);
        } catch (Exception e) {
            exception = e;
            return null;
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (exception == null) {
            onResult(result);
        } else {
            onException(exception);
        }
    }

    public abstract void onResult(Result result);

    public abstract void onException(Exception e);

    public abstract Result performInBackground(Params... params) throws Exception;
}
