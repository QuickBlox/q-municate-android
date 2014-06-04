package com.quickblox.qmunicate.utils;

import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.ui.uihelper.SimpleTextWatcher;

import java.util.HashSet;
import java.util.Set;

public class ChangeHelper {

    private Set<TextView> textViewsSet = new HashSet<TextView>();
    private Set<ImageView> imageViewsSet = new HashSet<ImageView>();

    public void addTextView(TextView textView) {
        textViewsSet.add(textView);
        textView.addTextChangedListener(new TextWatcherListener());

    }

    public void addImageView(ImageView imageView) {

    }

    public void storeData() {

    }

    public void addTextViewChangedListener() {

    }

    public interface TextViewChanged {

        public void onTextViewChanged();
    }

    private class TextWatcherListener extends SimpleTextWatcher {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    }
}
