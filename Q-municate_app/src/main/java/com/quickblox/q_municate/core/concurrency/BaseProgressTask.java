package com.quickblox.q_municate.core.concurrency;

import android.app.Activity;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.dialogs.ProgressDialog;
import com.quickblox.q_municate_core.core.concurrency.BaseErrorAsyncTask;

public abstract class BaseProgressTask<Params, Progress, Result> extends BaseErrorAsyncTask<Params, Progress, Result> {

    protected ProgressDialog progress;

    protected BaseProgressTask(Activity activity) {
        this(activity, R.string.dlg_wait_please, true);
    }

    protected BaseProgressTask(Activity activity, int messageId, boolean showDialog) {
        super(activity);
        if (showDialog) {
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
