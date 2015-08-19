package com.quickblox.q_municate.utils;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ColorUtils {

    private static final int COLOR_MAX_VALUE = 255;
    private static final float COLOR_ALPHA = 0.8f;
    private static Map<Integer, Integer> colorsMap = new HashMap<>();

    private Random random;

    public ColorUtils() {
        random = new Random();
    }

    public int getRandomTextColorById(Integer senderId) {
        if (colorsMap.get(senderId) != null) {
            return colorsMap.get(senderId);
        } else {
            int colorValue = getRandomColor();
            colorsMap.put(senderId, colorValue);
            return colorsMap.get(senderId);
        }
    }

    public int getRandomColor() {
        float[] hsv = new float[3];
        int color = Color.argb(COLOR_MAX_VALUE, random.nextInt(COLOR_MAX_VALUE), random.nextInt(
                COLOR_MAX_VALUE), random.nextInt(COLOR_MAX_VALUE));
        Color.colorToHSV(color, hsv);
        hsv[2] *= COLOR_ALPHA;
        color = Color.HSVToColor(hsv);
        return color;
    }
}