package com.quickblox.q_municate_core.utils;


public class MapUtils {
    private static final String mapStaticPath = "https://maps.googleapis.com/maps/api/staticmap?&zoom=15&size=600x300&maptype=roadmap&markers=color:blue%7Clabel:S%7C50.014394,%2036.229572&key=AIzaSyC68nhqEWQaXQJqr422kpimzR7XuJuRndg";

    public static String generateMapStaticURL(double latitude, double longitude){
        String url = mapStaticPath;


        return url;
    }
}
