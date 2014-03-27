package com.quickblox.qmunicate.ui.base;

import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;

public abstract class BaseListAdapter<T> extends BaseAdapter {

    protected List<T> objects;
    protected BaseActivity activity;

    public BaseListAdapter(BaseActivity activity, List<T> objects) {
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

    protected void displayImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, Consts.UIL_AVATAR_DISPLAY_OPTIONS);
    }
}