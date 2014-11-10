package com.quickblox.q_municate.utils;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.utils.ConstsCore;

public class TextViewHelper {

    public static void changeTextColorView(Context context, TextView textView, String target) {
        String stringTextView = (String) textView.getText();
        String stringTextViewLowerCase = stringTextView.toLowerCase();

        int startSpan;
        int endSpan = ConstsCore.ZERO_INT_VALUE;

        Spannable spanRange = new SpannableString(stringTextView);

        while (true) {
            startSpan = stringTextViewLowerCase.indexOf(target.toLowerCase(), endSpan);
            ForegroundColorSpan foreColour = new ForegroundColorSpan(context.getResources().getColor(R.color.red));
            if (startSpan < 0) {
                break;
            }
            endSpan = startSpan + target.length();
            spanRange.setSpan(foreColour, startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(spanRange);
    }
}