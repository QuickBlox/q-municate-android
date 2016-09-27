package com.quickblox.q_municate_core.core.concurrency;

import android.app.Activity;
import android.app.DialogFragment;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.lang.ref.WeakReference;

public abstract class BaseErrorAsyncTask<Params, Progress, Result> extends BaseAsyncTask<Params, Progress, Result> {

    private static final String TAG = BaseErrorAsyncTask.class.getName();

    protected WeakReference<Activity> activityRef;

    protected BaseErrorAsyncTask(Activity activity) {
        this.activityRef = new WeakReference<Activity>(activity);
    }

    @Override
    public void onException(Exception e) {

        Activity parentActivity = activityRef.get();

        if (e instanceof QBResponseException) {
            ErrorUtils.showError(parentActivity, e);
        }
    }

    protected void showDialog(DialogFragment dialog) {
        showDialog(dialog, null);
    }

    protected void showDialog(DialogFragment dialog, String tag) {
        if (activityRef.get() != null) {
            dialog.show(activityRef.get().getFragmentManager(), tag);
        }
    }

    protected void hideDialog(DialogFragment dialog) {
        if (dialog.getActivity() != null) {
            dialog.dismissAllowingStateLoss();
        }
    }

    protected boolean isActivityAlive() {
        Activity activity = activityRef.get();
        return activity != null && !activity.isFinishing();
    }
}
