package com.quickblox.qmunicate.core.concurrency;

import android.app.Activity;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.dialogs.ProgressDialog;
import com.quickblox.qmunicate.ui.utils.Consts;

public abstract class BaseProgressTask<Params, Progress, Result> extends BaseErrorAsyncTask<Params, Progress, Result> {

    protected ProgressDialog progress;

    protected BaseProgressTask(Activity activity) {
        this(activity, R.string.dlg_wait_please);
    }


    protected BaseProgressTask(Activity activity, int messageId) {
        super(activity);
        if (messageId != Consts.NOT_INITIALIZED_VALUE) {
            progress = ProgressDialog.newInstance(messageId);
        }
    }

    @Override
    protected void onPreExecute() {
        if (progress != null) {
            showDialog(progress);
        }
    }

    @Override
    public void onResult(Result result) {
        if (progress != null) {
            hideDialog(progress);
        }
    }

    @Override
    public void onException(Exception e) {
        if (progress != null) {
            hideDialog(progress);
        }
        super.onException(e);
    }
}
