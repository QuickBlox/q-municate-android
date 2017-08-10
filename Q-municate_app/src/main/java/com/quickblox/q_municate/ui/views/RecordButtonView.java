package com.quickblox.q_municate.ui.views;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by roman on 8/9/17.
 */

public class RecordButtonView extends AppCompatImageButton {
    private RecordTouchEventListener recordTouchEventListener;
    private final RecordTouchListener recordTouchListener;
    private final RecordLongClickListener recordLongClickListener;

    private final float halfWidthScreen;
    private boolean isSpeakButtonLongPressed;


    public interface RecordTouchEventListener {
        void start();
        void cancel();
        void stop();
    }

    public RecordButtonView(Context context) {
        this(context, null);
    }

    public RecordButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        halfWidthScreen = getMiddleWidthScreen();
        recordTouchListener = new RecordTouchListener();
        recordLongClickListener = new RecordLongClickListener();
    }

    public void setRecordTouchListener(final RecordTouchEventListener recordTouchEventListener) {
        this.recordTouchEventListener = recordTouchEventListener;
        initListeners();
    }

    private void initListeners() {
        setOnLongClickListener();
        setOnTouchListener();
    }

    private void setOnTouchListener() {
        this.setOnTouchListener(recordTouchListener);
    }

    private void setOnLongClickListener() {
        this.setOnLongClickListener(recordLongClickListener);
    }

    private boolean canCancel(float x, float halfWidthScreen) {
        return x < halfWidthScreen;
    }

    private float getMiddleWidthScreen() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / 2;
    }

    private final class RecordTouchListener implements View.OnTouchListener{

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            view.onTouchEvent(motionEvent);

            float xRaw = motionEvent.getRawX();

            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    if(canCancel(xRaw, halfWidthScreen) && isSpeakButtonLongPressed) {
                        recordTouchEventListener.cancel();
                        isSpeakButtonLongPressed = false;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if(isSpeakButtonLongPressed) {
                        recordTouchEventListener.stop();
                        isSpeakButtonLongPressed = false;
                    }
                    break;
            }
            return true;
        }
    }

    private final class RecordLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            isSpeakButtonLongPressed = true;
            recordTouchEventListener.start();

            return false;
        }
    }
}