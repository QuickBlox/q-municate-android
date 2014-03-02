package com.quickblox.qmunicate.ui.base;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public abstract class BaseListAdapter<T> extends ArrayAdapter<T> {

    public BaseListAdapter(Context context, int resource, int textViewResourceId, List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    protected void displayImage(ImageView imageView, String url) {
        ImageLoader.getInstance().displayImage(url, imageView);
    }

}
