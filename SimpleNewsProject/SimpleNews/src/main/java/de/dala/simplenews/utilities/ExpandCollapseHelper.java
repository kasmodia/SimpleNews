package de.dala.simplenews.utilities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class ExpandCollapseHelper {
    public static void animateCollapsing(final View view, boolean animated) {
        if (!animated || view.getVisibility() == View.GONE) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = 0;
            view.setLayoutParams(layoutParams);
            view.setVisibility(View.GONE);
            return;
        }
        int origHeight = view.getHeight();

        ValueAnimator animator = createHeightAnimator(view, origHeight, 0);
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(final Animator animator) {
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    public static void animateExpanding(final View view, final RecyclerView listViewWrapper, boolean animated) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }
        view.setVisibility(View.VISIBLE);

        View parent = (View) view.getParent();
        final int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), View.MeasureSpec.AT_MOST);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthSpec, heightSpec);

        if (!animated) {
            view.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.height = view.getMeasuredHeight();
            view.setLayoutParams(layoutParams);
            return;
        }

        final TypedArray styledAttributes = view.getContext().getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize});
        int mActionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        final int listViewHeight = listViewWrapper.getHeight() - mActionBarSize;

        ValueAnimator animator = createHeightAnimator(view, 0, view.getMeasuredHeight());
        animator.addUpdateListener(
                new ValueAnimator.AnimatorUpdateListener() {
                    final int listViewBottomPadding = listViewWrapper.getPaddingBottom();
                    final View v = findDirectChild(view, listViewWrapper);

                    @Override
                    public void onAnimationUpdate(final ValueAnimator animation) {
                        final int bottom = v.getBottom();
                        if (bottom > listViewHeight) {
                            final int top = v.getTop();
                            if (top > 0) {
                                listViewWrapper.smoothScrollBy(0, Math.min(bottom - listViewHeight + listViewBottomPadding, top));
                            }
                        }
                    }
                }
        );
        animator.start();
    }

    private static View findDirectChild(final View view, final ViewGroup viewGroup) {
        View result = view;
        View parent = (View) result.getParent();
        while (parent != null && !parent.equals(viewGroup)) {
            result = parent;
            parent = (View) result.getParent();
        }
        return result;
    }

    private static ValueAnimator createHeightAnimator(final View view, final int start, final int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator valueAnimator) {
                int value = (Integer) valueAnimator.getAnimatedValue();

                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.height = value;
                view.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}