package com.quickblox.q_municate.utils.helpers;

import android.content.Context;

import com.flurry.android.FlurryAgent;
import com.quickblox.auth.session.QBSettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tereha on 03.03.16.
 */
public class FlurryAnalyticsHelper {
    public static void pushAnalyticsData(Context context) {
        // init Flurry
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.init(context, "P8NWM9PBFCK2CWC8KZ59");

        Map<String, String> params = new HashMap<>();

        //param keys and values have to be of String type
        params.put("app_id", QBSettings.getInstance().getApplicationId());
        params.put("chat_endpoint", QBSettings.getInstance().getChatEndpoint());

        //up to 10 params can be logged with each event
        FlurryAgent.logEvent("connect_to_chat", params);
    }
}