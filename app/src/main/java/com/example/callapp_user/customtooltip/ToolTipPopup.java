package com.example.callapp_user.customtooltip;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ToolTipPopup {
    public static final int FADE_IN_DURATION = 500;
    public static final int FADE_OUT_DURATION = 100;
    private final int TOOLTIP_TEXTVIEW_SIDES_PADDING = (int) ApplicationExtended.get().getResources().getDimension(R.dimen.tooltip_left_right_padding);
    private final int TOOLTIP_TEXTVIEW_TOP_OR_BOTTOM_PADDING_WITH_ARROW = (int) Activities.getHowManyPxInDp(12);
    private final int TOOLTIP_TEXTVIEW_TOP_OR_BOTTOM_PADDING_WITHOUT_ARROW = (int) Activities.getHowManyPxInDp(14);
    private final int TOOLTIP_LAYOUT_SIDES_PADDING = (int) ApplicationExtended.get().getResources().getDimension(R.dimen.tooltip_layout_left_right_padding);
    private final int TOOLTIP_LAYOUT_TOP_BOTTOM_PADDING = (int) ApplicationExtended.get().getResources().getDimension(R.dimen.tooltip_layout_top_bottom_padding);
    public static int MAX_ALLOWED_DIALOG_WIDTH = 392;
    private PopupWindow window;
    private TextView helpTextView;
    private ImageView upImageView;
    private ImageView downImageView;
    private View inflatedViewGroup;
    private Drawable backgroundDrawable = null;
    private ShowListener showListener;
    private ArrowPosition arrowPosition = ArrowPosition.TOP;
    private ObjectAnimator tooltipFadeInAnimator;
    private ObjectAnimator tooltipFadeOutAnimator;

    interface ShowListener {
        void onPreShow();

        void onDismiss();

        void onShow();
    }

    private enum ArrowPosition {
        TOP,
        BOTTOM
    }

    private ToolTipPopup(Context context, String text) {
        this(context, R.layout.tooltip_layout);
        setText(text);
    }

    private ToolTipPopup(Context context, int viewResource) {
        window = new PopupWindow(context);
        LayoutInflater layoutInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setContentView(layoutInflater.inflate(viewResource, null));
        initViews();
    }

    private void initViews() {
        helpTextView = (TextView) inflatedViewGroup.findViewById(R.id.tooltip_text);

        int color = ContextCompat.getColor(ApplicationExtended.get(), R.color.colorAccent);
        upImageView = (ImageView) inflatedViewGroup.findViewById(R.id.arrow_up);
        upImageView.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        downImageView = (ImageView) inflatedViewGroup.findViewById(R.id.arrow_down);
        downImageView.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private void show(View selectedView, int marginYPosition) {
        if (!tryInitWindowProperties()) {
            //If can't set windows parameters
            return;
        }

        int[] locationOnScreen = new int[2];
        selectedView.getLocationOnScreen(locationOnScreen);

        Rect rectInSelectedViewSize = new Rect(locationOnScreen[0], locationOnScreen[1], locationOnScreen[0]
                + selectedView.getWidth(), locationOnScreen[1] + selectedView.getHeight());  // Created selected view size in screen without margins

        inflatedViewGroup.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int tooltipLayoutContainerWidth = inflatedViewGroup.getMeasuredWidth();

        final int screenWidth = Activities.getScreenWidth(Activities.getScreenOrientation());
        if (tooltipLayoutContainerWidth > screenWidth) {
            // Wider than screen width
            tooltipLayoutContainerWidth = screenWidth - (2 * TOOLTIP_LAYOUT_SIDES_PADDING);
        }

        if (tooltipLayoutContainerWidth > Activities.getHowManyPxInDp(MAX_ALLOWED_DIALOG_WIDTH)) {
            //Max width of tooltip should be 392dp
            tooltipLayoutContainerWidth = (int) Activities.getHowManyPxInDp(MAX_ALLOWED_DIALOG_WIDTH);
        }

        // Set max width of textview to be the max of the container we calculated without the container's padding
        Activities.setViewWidth(helpTextView, tooltipLayoutContainerWidth - 2 * TOOLTIP_LAYOUT_SIDES_PADDING);

        final int screenHeight = Activities.getScreenHeight(Activities.getScreenOrientation());
        if (rectInSelectedViewSize.top < screenHeight / 2) {
            // If on the top half of screen make the arrow as bottom
            arrowPosition = ArrowPosition.BOTTOM;
        }

        // show correct arrow up/down and hide the other one
        View arrow = showArrow(arrowPosition);
        setTextViewPadding(arrowPosition);

        // position arrow to point at center x of selected view
        final int arrowWidth = arrow.getMeasuredWidth();
        final int selectedViewCenterX = rectInSelectedViewSize.centerX();
        ViewGroup.MarginLayoutParams arrowParams = (ViewGroup.MarginLayoutParams) arrow.getLayoutParams();
        if (selectedViewCenterX + tooltipLayoutContainerWidth / 2 <= screenWidth && selectedViewCenterX - tooltipLayoutContainerWidth / 2 >= 0) {
            // if there is space so that center of tooltip can be aligned to the center of selected view, arrow should be placed in the center of tooltip
            arrowParams.leftMargin = (tooltipLayoutContainerWidth - arrowWidth) / 2 - TOOLTIP_LAYOUT_SIDES_PADDING;
        } else if (selectedViewCenterX > screenWidth / 2) {
            int screenWidthLeft = screenWidth - tooltipLayoutContainerWidth;
            arrowParams.leftMargin = selectedViewCenterX - screenWidthLeft - TOOLTIP_LAYOUT_SIDES_PADDING - arrowWidth / 2;
        } else {
            // (selectedViewCenterX < screenWidth/2)
            arrowParams.leftMargin = selectedViewCenterX - TOOLTIP_LAYOUT_SIDES_PADDING - arrowWidth / 2;
        }

        // Calculate x and y offset from the bottom-left corner of selected view
        // Align center x of tooltip to center x of selected view. If the tooltip is off screen, it will be pushed back into the screen by clipping
        final int xPosOffsetFromSelectedView = (selectedView.getWidth() - tooltipLayoutContainerWidth) / 2;
        final int yPosOffsetFromSelectedView;
        if (arrowPosition == ArrowPosition.TOP) {
            int TOOLTIP_TEXTVIEW_TEXT_SIZE = 14;
            final int toolTipTextViewHeight = Activities.getHeightOfTextViewFromSp(selectedView.getContext(), TOOLTIP_TEXTVIEW_TEXT_SIZE, tooltipLayoutContainerWidth - TOOLTIP_LAYOUT_SIDES_PADDING * 2,
                    Typeface.SANS_SERIF, TOOLTIP_TEXTVIEW_SIDES_PADDING * 2, TOOLTIP_TEXTVIEW_TOP_OR_BOTTOM_PADDING_WITH_ARROW + TOOLTIP_TEXTVIEW_TOP_OR_BOTTOM_PADDING_WITHOUT_ARROW, helpTextView.getText());
            yPosOffsetFromSelectedView = -selectedView.getHeight() - TOOLTIP_LAYOUT_TOP_BOTTOM_PADDING - toolTipTextViewHeight + marginYPosition;
        } else {
            yPosOffsetFromSelectedView = -TOOLTIP_LAYOUT_TOP_BOTTOM_PADDING - marginYPosition;
        }

        try {
            window.getContentView().setAlpha(0F);
            window.showAsDropDown(selectedView, xPosOffsetFromSelectedView, yPosOffsetFromSelectedView);
            tooltipFadeInAnimator = AnimationUtils.fadeIn(window.getContentView(), FADE_IN_DURATION, 0);
            tooltipFadeInAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    dismissTooltipWithAnimation();
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            tooltipFadeInAnimator.start();
            if (showListener != null) {
                showListener.onShow();
            }
        } catch (WindowManager.BadTokenException e) {
            Log.d(ToolTipPopup.class.getSimpleName(), e.getMessage());
        }
    }

    private void setTextViewPadding(ArrowPosition arrowPosition) {
        final int topPadding;
        final int bottomPadding;

        switch (arrowPosition) {
            case TOP:
                topPadding = TOOLTIP_TEXTVIEW_TOP_OR_BOTTOM_PADDING_WITH_ARROW;
                bottomPadding = TOOLTIP_TEXTVIEW_TOP_OR_BOTTOM_PADDING_WITHOUT_ARROW;
                break;

            case BOTTOM:
            default:
                topPadding = TOOLTIP_TEXTVIEW_TOP_OR_BOTTOM_PADDING_WITHOUT_ARROW;
                bottomPadding = TOOLTIP_TEXTVIEW_TOP_OR_BOTTOM_PADDING_WITH_ARROW;
                break;
        }

        helpTextView.setPadding(TOOLTIP_TEXTVIEW_SIDES_PADDING, topPadding, TOOLTIP_TEXTVIEW_SIDES_PADDING, bottomPadding);
    }

    private View showArrow(ArrowPosition arrowPos) {
        final View arrow;
        final View hideArrow;
        switch (arrowPos) {
            case TOP:
                arrow = downImageView;
                hideArrow = upImageView;
                break;

            case BOTTOM:
            default:
                arrow = upImageView;
                hideArrow = downImageView;
                break;
        }

        arrow.setVisibility(View.VISIBLE);
        hideArrow.setVisibility(View.INVISIBLE);
        return arrow;
    }

    private boolean tryInitWindowProperties() {
        if (inflatedViewGroup == null) {
            return false;
        }

        if (showListener != null) {
            showListener.onPreShow();
        }

        if (backgroundDrawable == null) {
            window.setBackgroundDrawable(new BitmapDrawable());
        } else {
            window.setBackgroundDrawable(backgroundDrawable);
        }

        window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        window.setContentView(inflatedViewGroup);

        return true;
    }

    public void setContentView(View root) {
        inflatedViewGroup = root;
        window.setContentView(root);
    }

    public void dismiss(boolean dismissWithAnimation) {
        if (!dismissWithAnimation) {
            removeToolTipFromWindow();
            if (tooltipFadeOutAnimator != null) {
                tooltipFadeOutAnimator.cancel();
            }
            return;
        }

        if (tooltipFadeInAnimator != null && tooltipFadeInAnimator.isRunning()) {
            tooltipFadeInAnimator.cancel();
        } else {
            dismissTooltipWithAnimation();
        }
    }

    private void dismissTooltipWithAnimation() {
        tooltipFadeOutAnimator = AnimationUtils.fadeOut(window.getContentView(), FADE_OUT_DURATION, 0, View.GONE, new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeToolTipFromWindow();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                removeToolTipFromWindow();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        tooltipFadeOutAnimator.start();
    }

    private void removeToolTipFromWindow() {
        if (window != null) {
            window.dismiss();
            if (showListener != null) {
                showListener.onDismiss();
            }
        }
    }

    public void setText(String text) {
        helpTextView.setText(text);
    }

    private void setOnDismissListener(PopupWindow.OnDismissListener listener) {
        window.setOnDismissListener(listener);
    }

    public void setBackgroundDrawable(Drawable background) {
        backgroundDrawable = background;
    }

    public void setShowListener(ShowListener showListener) {
        this.showListener = showListener;
    }

    public boolean isTooltipShowing() {
        return window != null && window.isShowing();
    }

    public static ToolTipPopup createToolTip(final View viewForTooltip, final String text,
                                             final PopupWindow.OnDismissListener onDismissListener, int startDelayTimeMilli) {
        if (viewForTooltip != null) {
            final ToolTipPopup toolTipPopup = new ToolTipPopup(viewForTooltip.getContext(), text);
            ApplicationExtended.get().postRunnableDelayed(new Runnable() {
                @Override
                public void run() {
                    toolTipPopup.setOnDismissListener(onDismissListener);
                    toolTipPopup.show(viewForTooltip, (int) Activities.getHowManyPxInDp(-14));
                }
            }, startDelayTimeMilli);

            return toolTipPopup;
        }
        return null;
    }
}