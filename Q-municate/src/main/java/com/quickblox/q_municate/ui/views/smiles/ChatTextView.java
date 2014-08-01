package com.quickblox.q_municate.ui.views.smiles;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.util.AttributeSet;

import com.quickblox.q_municate.ui.views.customFontComponents.clearableEditText.SimpleTextWatcher;
import com.quickblox.q_municate.ui.views.customFontComponents.textViews.LightTextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatTextView extends LightTextView {

    private static final String URL_PATTERN = "\\b(((ht|f)tp(s?)\\:\\/\\/|~\\/|\\/)|www.)" +
            "(\\w+:\\w+@)?(([-\\w]+\\.)+(com|org|net|gov" +
            "|mil|biz|info|mobi|name|aero|jobs|museum" +
            "|travel|[a-z]{2}))(:[\\d]{1,5})?" +
            "(((\\/([-\\w~!$+|.,=]|%[a-f\\d]{2})+)+|\\/)+|\\?|#)?" +
            "((\\?([-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)" +
            "(&(?:[-\\w~!$+|.,*:]|%[a-f\\d{2}])+=?" +
            "([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)*)*" +
            "(#([-\\w~!$+|.,*:=]|%[a-f\\d]{2})*)?\\b";
    private static final Pattern urlPattern = Pattern.compile(URL_PATTERN, Pattern.CASE_INSENSITIVE);

    private final Matcher matcher = urlPattern.matcher("");
    boolean linkHit;
    private Context context;
    private OnChatMessageTextWatcher chatTextWatcher;

    public ChatTextView(final Context context) {
        super(context);
        init(context);
    }

    public ChatTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChatTextView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void addUrl(Editable editable) {
        String stringToFind = editable.toString();
        matcher.reset(stringToFind);
        while (matcher.find()) {
            int urlStart = matcher.start();
            int urlEnd = matcher.end();
            editable.setSpan(new UrlSpan(context, stringToFind.substring(urlStart, urlEnd)), urlStart, urlEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void init(Context context) {
        this.context = context;
        chatTextWatcher = new OnChatMessageTextWatcher();
        addTextChangedListener(chatTextWatcher);
    }

    private class OnChatMessageTextWatcher extends SimpleTextWatcher {

        @Override
        public void afterTextChanged(Editable editable) {
            SmileysConvertor.addSmileySpans(ChatTextView.this.context, editable);
            addUrl(editable);
        }
    }
}