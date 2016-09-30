package com.example.callapp_user.customtooltip;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.support.annotation.IntDef;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AnimationUtils {

    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {
    }

    public static ObjectAnimator fadeIn(View v, int duration, int startDelay) {
        v.setVisibility(View.VISIBLE);//view must be visible for animation
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", v.getAlpha(), 1f);
        fadeIn.setDuration(duration);
        fadeIn.setStartDelay(startDelay);
        return fadeIn;
    }

    public static ObjectAnimator fadeOut(final View v, int duration, int startDelay, @Visibility final int visibilityAfterAnimEnd, final Animator.AnimatorListener listener) {
        final ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha", v.getAlpha(), 0f);
        fadeOut.setDuration(duration);
        fadeOut.setStartDelay(startDelay);
        if (visibilityAfterAnimEnd != v.getVisibility()) {
            fadeOut.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (listener != null) {
                        listener.onAnimationStart(animation);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    v.setVisibility(visibilityAfterAnimEnd);
                    if (listener != null) {
                        listener.onAnimationEnd(animation);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    v.setAlpha(1);
                    if (listener != null) {
                        listener.onAnimationCancel(animation);
                    }
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                    if (listener != null) {
                        listener.onAnimationRepeat(animation);
                    }
                }
            });
        }

        return fadeOut;
    }
}
