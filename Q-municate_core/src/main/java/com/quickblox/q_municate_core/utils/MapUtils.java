package com.quickblox.q_municate_core.utils;

import android.support.v4.util.Pair;

import com.quickblox.ui.kit.chatmessage.adapter.utils.JsonParserBase;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MapUtils {

    public static String generateLocationJson(Pair<String, Double> latitude, Pair<String, Double> longitude) {
        HashMap<String, String> latLng = new LinkedHashMap<>();
        latLng.put(latitude.first, String.valueOf(latitude.second));
        latLng.put(longitude.first, String.valueOf(longitude.second));
        return JsonParserBase.serialize(latLng);
    }
}