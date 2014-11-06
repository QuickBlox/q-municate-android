package com.quickblox.q_municate.ui.base;

import android.app.Activity;
import android.util.Log;

import com.quickblox.q_municate_core.core.ui.LoaderHelper;
import com.quickblox.q_municate_core.core.ui.LoaderManager;
import com.quickblox.q_municate_core.core.ui.OnLoadFinishedListener;
import com.quickblox.q_municate_core.utils.ErrorUtils;

public class QMLoaderHelper<T> extends LoaderHelper<T> {

    private static final String TAG = QMLoaderHelper.class.getSimpleName();

    public QMLoaderHelper(Activity activity, OnLoadFinishedListener<T> loadFinishedListener,
            LoaderManager<T> loaderManager) {
        super(activity, loadFinishedListener, loaderManager);
    }

    @Override
    public void onLoaderException(int i, Exception e) {
        Log.e(TAG, "Unknown error during loading", e);
        ErrorUtils.showError(activity, e);
    }
}