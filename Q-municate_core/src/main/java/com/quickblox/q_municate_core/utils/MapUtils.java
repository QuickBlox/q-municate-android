package com.quickblox.q_municate_core.utils;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MapUtils {
    private static final String URI_SCHEME_MAP = "https://maps.googleapis.com/maps/api/staticmap?";
    private static final String ZOOM = "15";
    private static final String SIZE = "600x300";
    private static final String MAPTYPE = "roadmap";
    private static final String COLOR = "blue";
    //api static map key should be generated in your developers google console
    private static final String KEY = "AIzaSyC68nhqEWQaXQJqr422kpimzR7XuJuRndg";

    public static final String EXTRA_LOCATION_LATITUDE = "location_latitude";
    public static final String EXTRA_LOCATION_LONGITUDE = "location_longitude";

    private static String generateURI(double latitude, double longitude) {
        Uri.Builder builder = new Uri.Builder();
        builder.appendQueryParameter("zoom", ZOOM)
                .appendQueryParameter("size", SIZE)
                .appendQueryParameter("maptype", MAPTYPE)
                .appendQueryParameter("markers", "color:" + COLOR + "%7Clabel:S%7C" + latitude + "," + longitude)
                .appendQueryParameter("key", KEY);

        return URI_SCHEME_MAP + builder.build().getQuery();
    }

    public static String generateLocationData(double latitude, double longitude) {
//         "data":"{\"lat\":\"50.014141\",\"lng\":\"36.229058\"}"
        JsonObject innerObject = new JsonObject();
        innerObject.addProperty("lat", String.valueOf(latitude));
        innerObject.addProperty("lng", String.valueOf(longitude));

        return innerObject.toString();
    }

    public static String getRemoteUri(String location) {
//        String locations = "{\"lat\":\"50.014141\",\"lng\":\"36.229058\"}";
        JsonParser jsonParser = new JsonParser();
        JsonObject jo = (JsonObject)jsonParser.parse(location);
        double lat = jo.get("lat").getAsDouble();
        double lng = jo.get("lng").getAsDouble();

        return generateURI(lat, lng).replaceAll("&amp;(?!&)", "&");
    }
}