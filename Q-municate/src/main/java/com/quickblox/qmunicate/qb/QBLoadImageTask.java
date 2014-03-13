package com.quickblox.qmunicate.qb;

import android.app.Activity;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.qmunicate.core.concurrency.BaseErrorAsyncTask;

public class QBLoadImageTask extends BaseErrorAsyncTask<Object, Void, QBFile> {

    private ImageView imageView;

    public QBLoadImageTask(Activity activity) {
        super(activity);
    }

    @Override
    public QBFile performInBackground(Object... params) throws Exception {
        Integer fileId = (Integer) params[0];
        imageView = (ImageView) params[1];

        QBFile qbFile = new QBFile(fileId);
        qbFile = QBContent.getFile(qbFile);

        return qbFile;
    }

    @Override
    public void onResult(QBFile file) {
        ImageLoader.getInstance().displayImage(file.getPublicUrl(), imageView);
    }
}
