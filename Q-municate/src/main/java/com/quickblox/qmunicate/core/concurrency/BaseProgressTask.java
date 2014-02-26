package com.quickblox.qmunicate.core.concurrency;

import android.support.v4.app.FragmentActivity;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.dialogs.ProgressDialog;

public abstract class BaseProgressTask<Params, Progress, Result> extends BaseErrorAsyncTask<Params, Progress, Result> {

    protected final ProgressDialog progress;

    protected BaseProgressTask(FragmentActivity activity) {
        this(activity, R.string.dlg_wait_please);
    }

    protected BaseProgressTask(FragmentActivity activity, int messageId) {
        super(activity);
        progress = ProgressDialog.newInstance(messageId);
    }

    @Override
    protected void onPreExecute() {
        showDialog(progress);
    }

    @Override
    public void onResult(Result result) {
        hideDialog(progress);
    }

    @Override
    public void onException(Exception e) {
        hideDialog(progress);
        super.onException(e);
    }
}
