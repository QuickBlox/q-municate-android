package com.quickblox.q_municate.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {

    private static final String DEFAULT_LABEL = "q-municate_simple_text_clipboard";

    public static void copySimpleTextToClipboard(Context context, String text){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(DEFAULT_LABEL, text);
        clipboard.setPrimaryClip(clip);
    }
}
