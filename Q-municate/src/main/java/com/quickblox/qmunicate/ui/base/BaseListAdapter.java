package com.quickblox.qmunicate.ui.base;

import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.quickblox.qmunicate.qb.QBLoadImageTask;

import java.util.List;

public abstract class BaseListAdapter<T> extends ArrayAdapter<T> {

    FragmentActivity activity;

    public BaseListAdapter(FragmentActivity activity, List<T> objects) {
        super(activity, 0, 0, objects);
        this.activity = activity;
    }

    protected void displayImage(Integer fileId, ImageView imageView) {
        new QBLoadImageTask(activity).execute(fileId, imageView);
    }

}
