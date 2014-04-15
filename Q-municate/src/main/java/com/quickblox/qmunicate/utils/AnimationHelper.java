package com.quickblox.qmunicate.utils;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;

public class AnimationHelper {

    public static final int DURATION_CHANGE_BACKGROUND_COLOR = 300;

    public static void changeBackgroundColor(Context context, final View view, int fromColor, int toColor) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                context.getResources().getColor(fromColor), context.getResources().getColor(toColor));
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                view.setBackgroundColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.setDuration(DURATION_CHANGE_BACKGROUND_COLOR);
        colorAnimation.start();
    }
}