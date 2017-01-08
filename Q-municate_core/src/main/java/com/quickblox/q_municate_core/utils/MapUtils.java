package com.quickblox.q_municate_core.utils;

import android.net.Uri;

public class MapUtils {
    private static final String URI_SCHEME_MAP = "https://maps.googleapis.com/maps/api/staticmap?";
    private static final String ZOOM = "15";
    private static final String SIZE = "600x300";
    private static final String MAPTYPE = "roadmap";
    private static final String COLOR = "blue";

    //api static map key should be generated in your developers google console
    private static final String KEY = "AIzaSyC68nhqEWQaXQJqr422kpimzR7XuJuRndg";

    public static String generateMapStaticURI(double latitude, double longitude) {
        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter("zoom", ZOOM)
                .appendQueryParameter("size", SIZE)
                .appendQueryParameter("maptype", MAPTYPE)
                .appendQueryParameter("markers", "color:" + COLOR + "%7Clabel:S%7C" + latitude + "," + longitude)
                .appendQueryParameter("key", KEY);

        return URI_SCHEME_MAP + builder.build().getQuery();
    }
}