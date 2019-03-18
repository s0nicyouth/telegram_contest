package com.syouth.telegram_charts.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.syouth.telegram_charts.R;

import java.util.Objects;

public class SummaryView extends ViewGroup {

    private static int PADDING = 5; // dp
    private static int VIEW_SPACING = 5; // dp

    private int mChildrenInARow = 2;
    private final TextView mTitle = new TextView(getContext());
    private int mPadding;
    private int mViewSpacing;

    public SummaryView(Context context) {
        super(context);
        init();
    }

    public SummaryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SummaryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                PADDING,
                getResources().getDisplayMetrics());
        mViewSpacing = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                VIEW_SPACING,
                getResources().getDisplayMetrics());
        setBackground(getResources().getDrawable(R.drawable.rounded_background));
        mTitle.setTextColor(Color.BLACK);
        mTitle.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                5,
                getResources().getDisplayMetrics()));
        addView(mTitle);
    }

    public void setNightMode(boolean enabled) {
        if (enabled) {
            mTitle.setTextColor(Color.WHITE);
            DrawableCompat.setTint(getBackground(), Color.parseColor("#212B35"));
        } else {
            mTitle.setTextColor(Color.BLACK);
            DrawableCompat.setTint(getBackground(), Color.WHITE);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = 0;
        int maxHeight = 0;
        mTitle.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        maxHeight += mTitle.getMeasuredHeight();
        maxWidth = Math.max(maxWidth, mTitle.getMeasuredWidth());
        int childrenLeft = getChildCount() - 1;
        while (childrenLeft > 0) {
            int currentWidth = 0;
            View curChild = null;
            for (int i = 0; i < mChildrenInARow && childrenLeft != 0; i++, childrenLeft--) {
                curChild = getChildAt(getChildCount() - childrenLeft);
                curChild.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                currentWidth += curChild.getMeasuredWidth();
                maxWidth = Math.max(maxWidth, currentWidth);
            }
            maxHeight += Objects.requireNonNull(curChild).getMeasuredHeight();
        }

        maxWidth += 2 * mPadding + mViewSpacing * (mChildrenInARow - 1);
        maxHeight += 2 * mPadding + mViewSpacing * (getChildCount() - 1);

        setMeasuredDimension(MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.EXACTLY));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int currentX = mPadding;
        int currentY = mPadding;
        mTitle.layout(currentX, currentY,
                currentX + mTitle.getMeasuredWidth(), currentY + mTitle.getMeasuredHeight());
        currentY += mViewSpacing + mTitle.getMeasuredHeight();
        int childrenLeft = getChildCount() - 1;
        while (childrenLeft > 0) {
            currentX = mPadding;
            View curChild = null;
            for (int i = 0; i < mChildrenInARow && childrenLeft != 0; i++, childrenLeft--) {
                curChild = getChildAt(getChildCount() - childrenLeft);
                curChild.layout(currentX, currentY,
                        currentX + curChild.getMeasuredWidth(),
                        currentY + curChild.getMeasuredHeight());
                currentX += mViewSpacing + curChild.getMeasuredWidth();
            }
            currentY += mViewSpacing + Objects.requireNonNull(curChild).getMeasuredHeight();
        }
    }

    @Override
    public void removeAllViews() {
        super.removeAllViews();
        addView(mTitle);
    }

    public void setChildrenInARow(int num) {
        mChildrenInARow = num;
        invalidate();
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }
}
