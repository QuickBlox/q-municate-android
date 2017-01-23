package com.quickblox.q_municate.utils;

import android.content.Context;
import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Patterns;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import com.quickblox.core.helper.Lo;


public class QBTextViewClickMovement extends LinkMovementMethod {

    private final QBTextViewLinkClickListener clickListener;
    private final GestureDetector gestureDetector;
    private final boolean overrideOnLinkClick;
    private TextView textView;
    private Spannable buffer;

    public enum QBLinkType {

        /**
         * Indicates that phone link was clicked
         */
        PHONE,

        /**
         * Identifies that URL was clicked
         */
        WEB_URL,

        /**
         * Identifies that Email Address was clicked
         */
        EMAIL_ADDRESS,

        /**
         * Indicates that none of above mentioned were clicked
         */
        NONE
    }

    /**
     * Interface used to handle Long clicks on the {@link TextView} and taps
     * on the phone, web, mail links inside of {@link TextView}.
     */
    public interface QBTextViewLinkClickListener {

        /**
         * This method will be invoked when user press and hold
         * finger on the {@link TextView}
         *
         * @param linkText Text which contains link on which user presses.
         * @param linkType Type of the link can be one of {@link QBLinkType} enumeration
         */
        void onLinkClicked(final String linkText, final QBLinkType linkType);

        /**
         * @param text Whole text of {@link TextView}
         */
        void onLongClick(final String text);
    }


    public QBTextViewClickMovement(final QBTextViewLinkClickListener listener, boolean overrideOnClick, final Context context) {
        this.clickListener = listener;
        this.overrideOnLinkClick = overrideOnClick;
        this.gestureDetector = new GestureDetector(context, new SimpleOnGestureListener());
    }

    @Override
    public boolean onTouchEvent(final TextView widget, final Spannable buffer, final MotionEvent event) {
        this.textView = widget;
        this.buffer = buffer;
        gestureDetector.onTouchEvent(event);

        //Return super method for delegate logic sending intent from Linkify
        //or return 'false' for yourself managing logic by link clicked
        if (overrideOnLinkClick){
            return false;
        } else {
            return super.onTouchEvent(widget, buffer, event);
        }
    }

    /**
     * Detects various gestures and events.
     * Notify users when a particular motion event has occurred.
     */
    class SimpleOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            // Notified when a tap occurs.
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // Notified when a long press occurs.
            final String text = buffer.toString();

            if (clickListener != null) {
                Lo.g("Long Click Occurs on TextView with ID: " + textView.getId() +
                        "Text: " + text);

                clickListener.onLongClick(text);
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            // Notified when tap occurs.
            final String linkText = getLinkText(textView, buffer, event);

            QBLinkType linkType = QBLinkType.NONE;

            if (Patterns.PHONE.matcher(linkText).matches()) {
                linkType = QBLinkType.PHONE;
            } else if (Patterns.WEB_URL.matcher(linkText).matches()) {
                linkType = QBLinkType.WEB_URL;
            } else if (Patterns.EMAIL_ADDRESS.matcher(linkText).matches()) {
                linkType = QBLinkType.EMAIL_ADDRESS;
            }

            if (clickListener != null) {
                Lo.g("Tap Occurs on TextView with ID: " + textView.getId() +
                        "Link Text: " + linkText +
                        "Link Type: " + linkType);

                clickListener.onLinkClicked(linkText, linkType);
            }

            return false;
        }

        private String getLinkText(final TextView widget, final Spannable buffer, final MotionEvent event) {

            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

            if (link.length != 0) {
                return buffer.subSequence(buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])).toString();
            }

            return "";
        }
    }
}
