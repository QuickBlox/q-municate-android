package com.quickblox.qmunicate.ui.views.smiles;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.text.style.ClickableSpan;
import android.view.View;

import java.util.List;

public class UrlSpan extends ClickableSpan {

    String url;
    private Context context;

    public UrlSpan(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    @Override
    public void onClick(View widget) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (isHasActivityForAction(intent)) {
            context.startActivity(intent);
        }
    }

    protected boolean isHasActivityForAction(Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }
}
