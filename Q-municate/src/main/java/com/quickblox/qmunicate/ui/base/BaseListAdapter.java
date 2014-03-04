package com.quickblox.qmunicate.ui.base;

import android.app.Activity;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.quickblox.qmunicate.qb.QBLoadImageTask;

import java.util.List;

public abstract class BaseListAdapter<T> extends BaseAdapter {

    protected List<T> objects;
    protected Activity activity;

    public BaseListAdapter(Activity activity, List<T> objects) {
        this.activity = activity;
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public T getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected void displayImage(Integer fileId, ImageView imageView) {
        new QBLoadImageTask(activity).execute(fileId, imageView);
    }
}
