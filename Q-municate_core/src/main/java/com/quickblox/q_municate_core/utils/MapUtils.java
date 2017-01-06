package com.quickblox.q_municate_core.utils;


import android.net.Uri;

public class MapUtils {
    private static final String mapStaticPath = "https://maps.googleapis.com/maps/api/staticmap?zoom=15&size=600x300&maptype=roadmap&markers=color:blue%7Clabel:S%7C50.014394,%2036.229572&key=AIzaSyC68nhqEWQaXQJqr422kpimzR7XuJuRndg";

    private static String zoom = "15";
    private static String size = "600x300";
    private static String maptype = "roadmap";
    private static String color = "blue";

    //api static map key should be generated in your developers google console
    private static String key = "AIzaSyC68nhqEWQaXQJqr422kpimzR7XuJuRndg";

    public static String generateMapStaticURL(double latitude, double longitude) {
        String url = "";
//        StringBuilder builder = new StringBuilder();
//        builder.append("https://maps.googleapis.com/maps/api/staticmap?")
//                .append("zoom=" + zoom)
//                .append("size=" + size)
//                .append()

//        Uri.Builder builder = new Uri.Builder();
//        builder.scheme("https")
//                .authority("maps.googleapis.com")
//                .appendPath("maps")
//                .appendPath("api")
//                .appendPath("staticmap")
//                .appendQueryParameter("zoom", zoom)
//                .appendQueryParameter("size", size)
//                .appendQueryParameter("maptype", maptype).build();
//
//
//        Uri builtUri = Uri.parse(builder.toString())
//                .buildUpon().appendEncodedPath(new StringBuilder("markers=color:").append(color).append("%7Clabel:S%7C").append(latitude).append(",").append(longitude).toString())
//                .appendQueryParameter("key", key)
//                .build();

        return builtUri.toString();
    }
}
