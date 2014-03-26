package com.quickblox.qmunicate.ui.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.quickblox.qmunicate.core.concurrency.BaseAsyncTask;
import com.quickblox.qmunicate.ui.base.BaseActivity;

//public class GetListViewSizeTask {
//    public void getListViewSize(ListView listView) {
//        ListAdapter listAdapter = listView.getAdapter();
//        if (listAdapter == null)
//            return;
//
//        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
//        int totalHeight = 0;
//        View view = null;
//        for (int i = 0; i < listAdapter.getCount(); i++) {
//            view = listAdapter.getView(i, view, listView);
//            if (i == 0)
//                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, WindowManager.LayoutParams.WRAP_CONTENT));
//
//            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
//            totalHeight += view.getMeasuredHeight();
//        }
//        ViewGroup.LayoutParams params = listView.getLayoutParams();
//        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
//        listView.setLayoutParams(params);
//        listView.requestLayout();
//    }
//}

public class GetListViewSizeTask extends BaseAsyncTask {
    private BaseActivity activity;
    private ListView listView;
    private ListAdapter listAdapter;

    public GetListViewSizeTask(BaseActivity activity, ListView listView) {
        this.activity = activity;
        this.listView = listView;
    }

    @Override
    public void onResult(Object totalHeight) {
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = (Integer)totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
        activity.hideProgress();
    }

    @Override
    public void onException(Exception e) {
        activity.hideProgress();
    }

    @Override
    public Object performInBackground(Object[] params) {
        listAdapter = listView.getAdapter();

//        int totalHeight = 0;
//        for (int size = 0; size < listAdapter.getCount(); size++) {
//            View listItem = listAdapter.getView(size, null, listView);
//            listItem.measure(0, 0);
//            totalHeight += listItem.getMeasuredHeight();
//        }

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, WindowManager.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        return totalHeight;
    }
}