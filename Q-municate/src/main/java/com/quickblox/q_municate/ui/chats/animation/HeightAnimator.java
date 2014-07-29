package com.quickblox.q_municate.ui.chats.animation;

import android.view.View;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

public class HeightAnimator {

    private static final long ANIMATION_DURATION = 300;

    private View triggerView;
    private View animatedView;

    public HeightAnimator(View triggerView, View animatedView) {
        this.triggerView = triggerView;
        this.animatedView = animatedView;
    }

    public void animateHeightFrom(int from, int to) {
        ValueAnimator attachmentViewAnimator = ValueAnimator.ofInt(from, to);
        attachmentViewAnimator.setDuration(ANIMATION_DURATION);
        attachmentViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                animatedView.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                animatedView.requestLayout();
            }
        });
        attachmentViewAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
                triggerView.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                triggerView.setEnabled(true);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                triggerView.setEnabled(true);
            }

        });
        attachmentViewAnimator.start();
    }
}