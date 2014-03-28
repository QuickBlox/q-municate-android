package com.quickblox.qmunicate.ui.views.smiles;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;

import com.quickblox.qmunicate.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmileysConvertor {

    private static final Map<Integer, String> smileysMap = new LinkedHashMap<Integer, String>();
    private static final Map<Integer, Pattern> patterns = new LinkedHashMap<Integer, Pattern>();

    static {
        smileysMap.put(R.drawable.laughing, "\ue415");
        smileysMap.put(R.drawable.sad, "\ue40e");
        smileysMap.put(R.drawable.love, "\ue106");
        smileysMap.put(R.drawable.not_talking, "\uD83D\uDE36");
        smileysMap.put(R.drawable.silly, "\ue105");
        smileysMap.put(R.drawable.smug, "\uD83D\uDE0E");
        smileysMap.put(R.drawable.winking, "\ue405");
        smileysMap.put(R.drawable.big_grin, "\ud83d\ude00");
        smileysMap.put(R.drawable.crying, "\uD83D\uDE22");
        smileysMap.put(R.drawable.straight_face, "\uD83D\uDE10");
        smileysMap.put(R.drawable.smile, "\ue056");
        smileysMap.put(R.drawable.tongue, "\uD83D\uDE1B");
        smileysMap.put(R.drawable.girl, "\uD83D\uDC67");
        smileysMap.put(R.drawable.angry, "\uD83D\uDE21");
        smileysMap.put(R.drawable.music, "\uD83C\uDFB5");
        smileysMap.put(R.drawable.sacred, "\uD83D\uDE07");
        smileysMap.put(R.drawable.sleepy, "\uD83D\uDE2A");
        smileysMap.put(R.drawable.sleeping, "\uD83D\uDE34");
        smileysMap.put(R.drawable.sick, "\uD83D\uDE37");
        smileysMap.put(R.drawable.party, "\uD83C\uDF86");
        smileysMap.put(R.drawable.scared, "\uD83D\uDE2E");
        smileysMap.put(R.drawable.smoking, "\uD83D\uDEAC");
        smileysMap.put(R.drawable.sweating, "\uD83D\uDE13");
        smileysMap.put(R.drawable.rainning, "\u2614");
        smileysMap.put(R.drawable.cold, "\uD83D\uDE30");
        smileysMap.put(R.drawable.dog, "\uD83D\uDC36");
        smileysMap.put(R.drawable.cat, "\uD83D\uDC31");
        smileysMap.put(R.drawable.heart, "\u2764");
        smileysMap.put(R.drawable.broken_heart, "\uD83D\uDC94");
        smileysMap.put(R.drawable.kiss, "\uD83D\uDC8B");
        smileysMap.put(R.drawable.win, "\uD83C\uDFC6");
        smileysMap.put(R.drawable.bomb, "\uD83D\uDCA3");
        smileysMap.put(R.drawable.cake, "\uD83C\uDF82");
        smileysMap.put(R.drawable.sun, "\u2600");
        smileysMap.put(R.drawable.night, "\uD83C\uDF19");
        smileysMap.put(R.drawable.stars, "\u2B50");
        smileysMap.put(R.drawable.gift, "\uD83C\uDF81");
        smileysMap.put(R.drawable.on_the_phone, "\uD83D\uDCF1");
        smileysMap.put(R.drawable.flower, "\uD83C\uDF39");
        smileysMap.put(R.drawable.poo, "\uD83D\uDCA9");
        smileysMap.put(R.drawable.drinks, "\uD83C\uDF79");
        smileysMap.put(R.drawable.balloon, "\uD83C\uDF88");
        smileysMap.put(R.drawable.clock, "\u23F0");
        smileysMap.put(R.drawable.mail, "\u2709");
        smileysMap.put(R.drawable.rainbow, "\uD83C\uDF08");
        smileysMap.put(R.drawable.ghost, "\uD83D\uDC7B");
        smileysMap.put(R.drawable.pig, "\uD83D\uDC37");

        for (Map.Entry<Integer, String> entry : smileysMap.entrySet()) {
            patterns.put(entry.getKey(), Pattern.compile(entry.getValue(), Pattern.CASE_INSENSITIVE));
        }
    }

    public static void addSmileySpans(Context context, Editable editable) {
        String recievedMessage = editable.toString();
        for (Map.Entry<Integer, String> entry : smileysMap.entrySet()) {
            Matcher matcher = patterns.get(entry.getKey()).matcher(recievedMessage);
            while (matcher.find()) {
                SmileSpan imageSpan = new SmileSpan(context, entry.getKey());
                editable.setSpan((imageSpan), matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    public static String getSymbolByResourceId(int id) {
        return smileysMap.get(id);
    }

    public static List<Integer> getMapAsList() {
        List<Integer> ids = new ArrayList<Integer>();
        for (Map.Entry<Integer, String> entry : smileysMap.entrySet()) {
            ids.add(entry.getKey());
        }
        return ids;
    }
}