package com.quickblox.q_municate.utils;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.quickblox.users.model.QBUser;

public class AnalyticsUtils {

    private static String GA_PROPERTY = "UA-26728270-9";

    public static void pushAnalyticsData(Context context, QBUser user, String action) {
        GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
        Tracker tracker = analytics.newTracker(GA_PROPERTY);

        // You only need to set User ID on a tracker once. By setting it on the tracker, the ID will be
        // sent with all subsequent hits.
        tracker.set("&uid", String.valueOf(user.getId()));
        // This hit will be sent with the User ID value and be visible in User-ID-enabled views (profiles).
        tracker.send(new HitBuilders.EventBuilder().setCategory("UX").setAction(action).build());
    }
}