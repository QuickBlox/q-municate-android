package com.quickblox.qmunicate.ui.base;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.quickblox.module.content.model.QBFile;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.qb.QBGetFileCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.squareup.picasso.Picasso;

import java.util.List;

public abstract class BaseListAdapter<T> extends BaseAdapter {

    protected List<T> objects;
    protected BaseActivity activity;

    private SparseArray<ImageView> imageViewArray = new SparseArray<ImageView>();

    public BaseListAdapter(BaseActivity activity, List<T> objects) {
        this.activity = activity;
        this.objects = objects;
        activity.addAction(QBServiceConsts.GET_FILE_SUCCESS_ACTION, new GetFileSuccessAction());
        activity.updateBroadcastActionList();
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
        imageViewArray.put(fileId, imageView);
        QBGetFileCommand.start(activity, fileId);
    }

    private class GetFileSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBFile file = (QBFile) bundle.getSerializable(QBServiceConsts.EXTRA_FILE);
            ImageView imageView = imageViewArray.get(file.getId());
            Picasso.with(activity)
                    .load(file.getPublicUrl())
                    .placeholder(R.drawable.placeholder_user)
                    .into(imageView);
        }
    }
}