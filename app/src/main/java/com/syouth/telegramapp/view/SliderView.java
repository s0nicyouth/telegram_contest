package com.syouth.telegramapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class SliderView extends View {

    private static final int DRAGGER_LINE_SIDTH = 5; // dp

    private static final int SIDE_LEFT = 0;
    private static final int SIDE_RIGHT = 1;
    private static final int SIDES_BOTH = 2;

    @Nullable
    private RatioChangeListener mRatioChangeListener;

    private float mLeftRatio = 0.4f;
    private float mRightRatio = 0.4f;

    private int mDraggingSide = -1;
    private float mPrevX;

    private float mDraggerWidth;

    private final Paint mGrayPaint = new Paint();

    public SliderView(Context context) {
        super(context);
        init();
    }

    public SliderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SliderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setRatioChangeListener(@Nullable RatioChangeListener listener) {
        mRatioChangeListener = listener;
        notififNeeded();
    }

    public void setLeftRatio(float ratio) {
        mLeftRatio = ratio;
        if (mLeftRatio < 0) {
            mLeftRatio = 0;
        }
        invalidate();
    }

    public void setRightRatio(float ratio) {
        mRightRatio = ratio;
        if (mRightRatio < 0) {
            mRightRatio = 0;
        }
        invalidate();
    }

    private void setLeftRatioInternal(float ratio) {
        mLeftRatio = ratio;
        if (mLeftRatio < 0) {
            mLeftRatio = 0;
        }
        invalidate();
        notififNeeded();
    }

    private void setRightRatioInternal(float ratio) {
        mRightRatio = ratio;
        if (mRightRatio < 0) {
            mRightRatio = 0;
        }
        invalidate();
        notififNeeded();
    }

    private void notififNeeded() {
        if (mRatioChangeListener != null) {
            mRatioChangeListener.changed(mLeftRatio, mRightRatio);
        }
    }

    private void init() {
        setBackgroundColor(Color.TRANSPARENT);
        mGrayPaint.setStyle(Paint.Style.FILL);
        mGrayPaint.setAntiAlias(true);
        mGrayPaint.setColor(Color.BLUE);

        mDraggerWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                DRAGGER_LINE_SIDTH,
                getResources().getDisplayMetrics());
    }

    private float getLeftEnd() {
        return getWidth() * mLeftRatio;
    }

    private float getRightEnd() {
        return getWidth() - getWidth() * mRightRatio;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float leftSideEnd = getLeftEnd();
        float rightSideEnd = getRightEnd();
        mGrayPaint.setAlpha(10);
        canvas.drawRect(0,0, leftSideEnd, getHeight(), mGrayPaint);
        mGrayPaint.setAlpha(50);
        canvas.drawRect(leftSideEnd, 0,
                leftSideEnd + mDraggerWidth, getHeight(),
                mGrayPaint);
        canvas.drawRect(rightSideEnd - mDraggerWidth,0,
                rightSideEnd, getHeight(),
                mGrayPaint);
        mGrayPaint.setAlpha(10);
        canvas.drawRect(rightSideEnd,0,
                getWidth(), getHeight(),
                mGrayPaint);
        mGrayPaint.setAlpha(50);
        canvas.drawRect(leftSideEnd + mDraggerWidth, 0,
                rightSideEnd - mDraggerWidth, mDraggerWidth / 4,
                mGrayPaint);
        canvas.drawRect(leftSideEnd + mDraggerWidth, getHeight() - mDraggerWidth / 4,
                rightSideEnd - mDraggerWidth, getHeight(),
                mGrayPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int width = getWidth();
        float leftSideEnd = getLeftEnd();
        float rightSideEnd = getRightEnd();
        float eventX = event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = eventX;
                if (eventX >= leftSideEnd - mDraggerWidth
                    && eventX <= leftSideEnd + mDraggerWidth) {
                    mDraggingSide = SIDE_LEFT;
                } else if (eventX >= rightSideEnd - mDraggerWidth
                        && eventX <= rightSideEnd + mDraggerWidth) {
                    mDraggingSide = SIDE_RIGHT;
                } else if (eventX > leftSideEnd + mDraggerWidth
                           && eventX < rightSideEnd - mDraggerWidth) {
                    mDraggingSide = SIDES_BOTH;
                }
                return true;
            case MotionEvent.ACTION_UP:
                mDraggingSide = -1;
                return true;
            case MotionEvent.ACTION_MOVE:
                float deltaX = eventX - mPrevX;
                float deltaRatio = deltaX / width;
                if (mDraggingSide == SIDE_LEFT) {
                    setLeftRatioInternal(mLeftRatio + deltaRatio);
                } else if (mDraggingSide == SIDE_RIGHT) {
                    setRightRatioInternal(mRightRatio - deltaRatio);
                } else if (mDraggingSide == SIDES_BOTH) {
                    float newRight;
                    float newLeft;
                    if (deltaRatio < 0) {
                         newLeft = Math.max(0, mLeftRatio + deltaRatio);
                         newRight = mRightRatio + (mLeftRatio - newLeft);
                    } else {
                        newRight = Math.max(0, mRightRatio - deltaRatio);
                        newLeft = mLeftRatio + (mRightRatio - newRight);
                    }

                    setLeftRatioInternal(newLeft);
                    setRightRatioInternal(newRight);
                }
                mPrevX = eventX;
                return true;
        }

        return super.onTouchEvent(event);
    }

    /**
     * Notifies about ratio changes.
     */
    public interface RatioChangeListener {
        void changed(float left, float right);
    }
}
