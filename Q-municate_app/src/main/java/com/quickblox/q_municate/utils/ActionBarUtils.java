package com.quickblox.q_municate.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.widget.TextView;

import com.quickblox.q_municate_core.utils.ConstsCore;

public class ActionBarUtils {

    public static void initColorsActionBar(Activity activity) {
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        int actionBarSubTitleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
        if (actionBarTitleId > ConstsCore.ZERO_INT_VALUE) {
            TextView title = (TextView) activity.findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.WHITE);
            }
        }
        if (actionBarSubTitleId > ConstsCore.ZERO_INT_VALUE) {
            TextView subTitle = (TextView) activity.findViewById(actionBarSubTitleId);
            if (subTitle != null) {
                float alpha = 0.5f;
                subTitle.setTextColor(Color.WHITE);
                subTitle.setAlpha(alpha);
            }
        }
    }
}