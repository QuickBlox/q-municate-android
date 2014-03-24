package com.quickblox.qmunicate.ui.views.smiles;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Selection;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.Lists;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.chats.SwitchViewListener;
import com.quickblox.qmunicate.ui.views.customFontComponents.clearableEditText.SimpleTextWatcher;
import com.quickblox.qmunicate.ui.views.customFontComponents.editTexts.LightEditText;

import java.util.List;

public class ChatEditText extends LightEditText {

    private Drawable smileIcon;
    private SmileClickListener smileClickListener;
    private boolean isSmileActive;
    private Drawable smileIconActive;
    private Context context;
    private ChatTextChangeListener textWatcher;
    private SwitchViewListener switchViewListener;

    public ChatEditText(final Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        isSmileActive = false;
        Resources resources = getResources();
        smileIcon = resources.getDrawable(R.drawable.chat_smile);
        smileIconActive = resources.getDrawable(R.drawable.chat_smile_active);
        setCompoundDrawablesWithIntrinsicBounds(null, null, smileIcon, null);
        setOnTouchListener(new SmileIconTouchListener());
        textWatcher = new ChatTextChangeListener();
        addTextChangedListener(textWatcher);
    }

    public ChatEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public ChatEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setSmileClickListener(SmileClickListener smileClickListener) {
        this.smileClickListener = smileClickListener;
    }

    public void setSwitchViewListener(SwitchViewListener switchViewListener) {
        this.switchViewListener = switchViewListener;
    }

    public void switchSmileIcon() {
        setCompoundDrawablesWithIntrinsicBounds(null, null, isSmileActive ? smileIcon : smileIconActive, null);
        isSmileActive = !isSmileActive;
    }

    private class SmileIconTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_UP) {
                return false;
            }

            if (event.getX() > (getWidth() - getPaddingRight() - smileIcon.getIntrinsicWidth()) && smileClickListener != null) {
                switchSmileIcon();
                smileClickListener.onSmileClick();
            } else {
                if (switchViewListener != null) {
                    switchViewListener.showLastListItem();
                }
            }

            return false;
        }
    }

    private class ChatTextChangeListener extends SimpleTextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            checkIsNeedToDeleteSpannable(charSequence, start, count, after);
        }

        @Override
        public void afterTextChanged(Editable editable) {
            SmileysConvertor.addSmileySpans(ChatEditText.this.context, editable);
        }

        private void checkIsNeedToDeleteSpannable(CharSequence charSequence, int start, int count, int after) {
            if (count <= after) {
                return;
            }
            List<ImageSpan> spans = getListOfImageSpans();
            if (spans.isEmpty()) {
                return;
            }

            ImageSpan imageSpanToDelete = null;
            for (ImageSpan imageSpan : spans) {
                int endingPosition = getText().getSpanEnd(imageSpan);
                if (endingPosition - 1 == start) {
                    imageSpanToDelete = imageSpan;
                    break;
                }
            }

            if (imageSpanToDelete == null) {
                return;
            }

            int spanEnd = getText().getSpanEnd(imageSpanToDelete);
            int spanStart = getText().getSpanStart(imageSpanToDelete);
            getText().removeSpan(imageSpanToDelete);
            removeTextChangedListener(textWatcher);
            CharSequence startSubSequence = charSequence.subSequence(0, spanStart);
            CharSequence endSubSequence = charSequence.subSequence(spanEnd, charSequence.length());
            setText(startSubSequence.toString() + endSubSequence.toString());
            SmileysConvertor.addSmileySpans(ChatEditText.this.context, getText());
            Selection.setSelection(getText(), spanStart);
            addTextChangedListener(textWatcher);
        }

        private List<ImageSpan> getListOfImageSpans() {
            ImageSpan[] spans = getText().getSpans(0, getText().length(), ImageSpan.class);
            return Lists.newArrayList(spans);
        }
    }
}