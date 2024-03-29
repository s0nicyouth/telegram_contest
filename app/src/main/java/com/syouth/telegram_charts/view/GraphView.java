package com.syouth.telegram_charts.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.syouth.telegram_charts.math.Common;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GraphView extends View implements View.OnTouchListener {

    private static final int BOTTOM_PADDING = 50; // dp
    private static final int TOP_PADDING = 50; //dp
    private static final int LEVEL_VALUE_PADDING = 5; // dp
    private static final int TEXT_SIZE = 9; // sp
    private static final int STROKE_WIDTH = 2; // dp
    private static final int CIRCLE_RADIUS = 4; // dp
    private static final int MIN_GAP_PERIOD = 110; // dp

    private static final int NUMBER_OF_LEVEL_LINES = 5;

    /**
     * Describes position in {@link Chart#x} closest to touch.
     */
    public static class ChartDetails {
        public Chart chart;
        public int closestPosition;
        public float positionXInView;

        private ChartDetails(Chart chart, int closestPosition, float positionXInView) {
            this.chart = chart;
            this.closestPosition = closestPosition;
            this.positionXInView = positionXInView;
        }
    }

    /**
     * Provides current chart touch details.
     */
    public interface ChartTouchListener {
        ChartTouchListener NULL = details -> {};

        void changed(@Nullable ChartDetails details);
    }

    /**
     * Describes y values.
     */
    public static class LineData {
        public int color;
        public long[] vals;
        public String name;
        public boolean enabled = true;

        public LineData(int color, long[] vals, String name) {
            this.color = color;
            this.vals = vals;
            this.name = name;
        }
    }

    /**
     * Describes all chart including x, ys, names, and colors
     */
    public static class Chart {
        /**
         * x values of the chart
         */
        public long[] x;

        /**
         * name -> LineData map
         */
        public List<LineData> lines;

        public Chart(long[] x, List<LineData> lines) {
            this.x = x;
            this.lines = lines;
        }
    }

    /**
     * Converts x values to string representation.
     */
    public interface XValueInterpolator {
        String interpolate(long val);
    }

    @Nullable
    private List<Chart> mCharts;
    @Nullable
    private ChartDetails mChartDetails;

    private long mMaxY;
    private long mMinY;

    private long mMaxX;
    private long mMinX;

    private float mLeftRatio = 0f;
    private float mRightRatio = 0f;
    private float mYShiftPixels = 0f;
    private boolean mUp = false;
    private long mYGapPixels = 0;

    private final Paint mPaint = new Paint();
    private final Paint mLinesPaint = new Paint();
    private final Paint mTextPaint = new Paint();
    private final Paint mCirclesPaint = new Paint();
    private final Paint mTitlePaint = new Paint();
    private final Path mGraphPath = new Path();
    private final long[] mMaxMinResult = new long[2];
    private final ValueAnimator.AnimatorListener mAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationCancel(Animator animation) {
            mYShiftPixels = 0f;
            invalidate();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mYShiftPixels = 0f;
            invalidate();
        }
    };
    private final ValueAnimator.AnimatorUpdateListener mMinYUpdateListener = animation -> {
        float val = (float) animation.getAnimatedValue();
        mMinY = (long) val;
        super.invalidate();
    };
    private final ValueAnimator.AnimatorUpdateListener mMaxYUpdateListener = animation -> {
        float val = (float) animation.getAnimatedValue();
        mMaxY = (long) val;
        mYShiftPixels = mUp ? -mYGapPixels * animation.getAnimatedFraction() : mYGapPixels * animation.getAnimatedFraction();
        super.invalidate();
    };
    private final int mScreenWidth = getResources().getDisplayMetrics().widthPixels;

    private float[] mPointsToDraw;

    private int mBottomPadding;
    private int mTopPadding;
    private int mLevelValuePadding;
    private int mCircleRadius;
    private int mStrokeWidth;
    private int mPixelsPerRatio = mScreenWidth;
    private int mMinGapPeriod;

    private boolean mJustDrawGraph = false;

    @Nullable
    private String mTitle;
    private final Rect mTextRect = new Rect();

    private XValueInterpolator mXInterpolator = String::valueOf;
    private ChartTouchListener mChartTouchListener = ChartTouchListener.NULL;

    private int mBackgroundColor = Color.WHITE;

    private boolean mRenderingModeGpu = true;

    private ValueAnimator mMaxYAnimator;
    private ValueAnimator mMinYAnimator;

    public GraphView(Context context) {
        super(context);
        init();
    }

    public GraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Sets charting data.
     * @param charts {@link Chart}
     */
    public void setData(@Nullable List<Chart> charts) {
        mCharts = charts;
        if (mCharts == null) {
            invalidate();
            return;
        }
        int maxSize = 0;
        for (Chart c : mCharts) {
            for (LineData ld : c.lines) {
                // Maximum possible size of points array
                maxSize = Math.max(maxSize, 4 + (ld.vals.length - 2) * 4);
            }
        }
        mPointsToDraw = new float[maxSize];
        updateMaxMinX();
        updateMaxMinY();
        invalidate();
    }

    private void updateMaxMinX() {
        if (mCharts == null) {
            return;
        }
        long[] maxMinX = findMaxMinX(mCharts);
        mMaxX = maxMinX[0];
        mMinX = maxMinX[1];
    }

    public void setJustDrawGraph(boolean val) {
        mJustDrawGraph = val;
        if (mJustDrawGraph) {
            mBottomPadding = 0;
            mTopPadding = 0;
        } else {
            updatePaddings();
        }
        updateMaxMinY();
    }

    private void updateMaxMinY() {
        if (mCharts == null) {
            return;
        }
        long prevMaxY = mMaxY;
        long prevMinY = mMinY;
        long[] maxMinY = findMaxMinYInCurrentBounds(mCharts);
        mMaxY = maxMinY[0];
        mMinY = maxMinY[1] < 0 ? maxMinY[1] : 0;
        if ((mMaxYAnimator == null || !mMaxYAnimator.isRunning()) && prevMaxY != mMaxY) {
            mUp = prevMaxY <= mMaxY;
            mMaxYAnimator = ValueAnimator.ofFloat(prevMaxY, mMaxY);
            mMaxYAnimator.setDuration(500);
            mMaxYAnimator.addUpdateListener(mMaxYUpdateListener);
            mMaxYAnimator.addListener(mAnimatorListener);
            mMaxYAnimator.start();
        }
        if ((mMinYAnimator == null || !mMinYAnimator.isRunning()) && prevMinY != mMinY) {
            mMinYAnimator = ValueAnimator.ofFloat(prevMinY, mMinY);
            mMaxYAnimator.setDuration(500);
            mMaxYAnimator.addUpdateListener(mMinYUpdateListener);
            mMinYAnimator.addListener(mAnimatorListener);
            mMinYAnimator.start();
        }
    }

    public void setRenderingModeGpu(boolean enabled) {
        mRenderingModeGpu = enabled;
    }

    public boolean getRenderingModeGpu() {
        return mRenderingModeGpu;
    }

    public void setChartTouchListener(ChartTouchListener listener) {
        mChartTouchListener = listener;
        mChartTouchListener.changed(mChartDetails);
    }

    public void setNightMode(boolean enabled) {
        if (enabled) {
            mBackgroundColor = Color.parseColor("#212B35");
            mTextPaint.setColor(Color.LTGRAY);
            mTextPaint.setAlpha(50);
            mLinesPaint.setColor(Color.BLACK);
        } else {
            mBackgroundColor = Color.WHITE;
            mTextPaint.setColor(Color.GRAY);
            mTextPaint.setAlpha(255);
            mLinesPaint.setColor(Color.GRAY);
        }
        invalidate();
    }

    /**
     * Sets amount of data which is scrolled from the left
     * @param lRatio amount to scroll.
     */
    public void setLeftRatio(float lRatio) {
        mLeftRatio = lRatio;
        if (mLeftRatio > 1 - mRightRatio) {
            mLeftRatio = 1 - mRightRatio;
        }
        updateMaxMinY();
        invalidate();
    }

    /**
     * Sets amount of data which is scrolled from the right.
     * @param rRatio amount to scroll.
     */
    public void setRightRatio(float rRatio) {
        mRightRatio = rRatio;
        if (mRightRatio > 1 - mLeftRatio) {
            mRightRatio = 1 - mLeftRatio;
        }
        updateMaxMinY();
        invalidate();
    }

    public void setTitle(String title) {
        mTitle = title;
        invalidate();
    }

    public void setXValueInterpolator(XValueInterpolator interpolator) {
        mXInterpolator = interpolator;
        invalidate();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mCharts == null) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            mChartDetails = null;
            mChartTouchListener.changed(null);
            invalidate();
            return true;
        }
        float xTouch = event.getX();
        long minX = getMinXConsideringRatio();
        long maxX = getMaxXConsideringRatio();

        float valToPixelRation = 1f / getPixelPerValRatioX(maxX, minX);
        long valToLookFor = (long) (minX + valToPixelRation * xTouch);
        mChartDetails = getChartsTouchPositions(valToLookFor);
        mChartTouchListener.changed(mChartDetails);
        invalidate();
        return true;
    }

    private int getClosestXPosition(long[] x, long val) {
        int pos = Arrays.binarySearch(x, val);
        if (pos < 0) {
            pos = -(pos + 1);
        }

        return Math.min(x.length - 1, pos);
    }

    @Nullable
    private ChartDetails getChartsTouchPositions(long val) {
        List<Chart> charts = Objects.requireNonNull(mCharts);
        Chart closestChart = null;
        long closestChartDistance = Long.MAX_VALUE;
        int closestXPosition = 0;
        for (Chart c : charts) {
            if (isChartEnabled(c)) {
                int xCurPos = getClosestXPosition(c.x, val);
                long distance = Math.abs(val - c.x[xCurPos]);
                if (closestChartDistance > distance) {
                    closestChartDistance = distance;
                    closestChart = c;
                    closestXPosition = xCurPos;
                }
            }
        }

        return closestChart != null ? new ChartDetails(
                closestChart,
                closestXPosition,
                getPixelsXByPlotX(closestChart.x[closestXPosition])) : null;
    }

    private void init() {
        updatePaddings();
        mLevelValuePadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                LEVEL_VALUE_PADDING,
                getResources().getDisplayMetrics());
        mStrokeWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                STROKE_WIDTH,
                getResources().getDisplayMetrics());
        mCircleRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                CIRCLE_RADIUS,
                getResources().getDisplayMetrics());
        mMinGapPeriod = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                MIN_GAP_PERIOD,
                getResources().getDisplayMetrics());

        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mLinesPaint.setColor(Color.GRAY);
        mLinesPaint.setAntiAlias(true);

        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                TEXT_SIZE,
                getResources().getDisplayMetrics());
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(textSize);

        mCirclesPaint.setColor((Color.GRAY));
        mCirclesPaint.setAntiAlias(true);
        mCirclesPaint.setStyle(Paint.Style.STROKE);
        mCirclesPaint.setStrokeWidth(mStrokeWidth);

        mTitlePaint.setColor(Color.parseColor("#4B97C7"));
        mTitlePaint.setAntiAlias(true);
        mTitlePaint.setTextSize(50);

        setOnTouchListener(this);
    }

    private void updatePaddings() {
        mBottomPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                BOTTOM_PADDING,
                getResources().getDisplayMetrics());
        mTopPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                TOP_PADDING,
                getResources().getDisplayMetrics());
    }

    private long[] findMaxMinX(List<Chart> charts) {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        for (Chart c : charts) {
            if (isChartEnabled(c)) {
                long[] maxMin = Common.findMaxMin(c.x);
                if (maxMin[0] > max) {
                    max = maxMin[0];
                }
                if (maxMin[1] < min) {
                    min = maxMin[1];
                }
            }
        }

        mMaxMinResult[0] = max;
        mMaxMinResult[1] = min;
        return mMaxMinResult;
    }

    private long[] findMaxMinYInCurrentBounds(List<Chart> charts) {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        for (Chart c : charts) {
            int firstPos = getFirstVisibleXPosition(c.x, getMinXConsideringRatio());
            int lastPos = getLastVisibleXPosition(c.x, getMaxXConsideringRatio());
            for (LineData l : c.lines) {
                if (l.enabled) {
                    long[] maxMin = findMaxMinInBounds(l.vals, firstPos, lastPos);
                    if (maxMin[0] > max) {
                        max = maxMin[0];
                    }
                    if (maxMin[1] < min) {
                        min = maxMin[1];
                    }
                }
            }
        }

        mMaxMinResult[0] = max;
        mMaxMinResult[1] = min;
        return mMaxMinResult;
    }

    private long[] findMaxMinInBounds(long[] y, int l, int r) {
        long max = Long.MIN_VALUE;
        long min = Long.MAX_VALUE;
        for (int i = l; i <= r; i++) {
            if (y[i] > max) {
                max = y[i];
            }
            if (y[i] < min) {
                min = y[i];
            }
        }

        mMaxMinResult[0] = max;
        mMaxMinResult[1] = min;
        return mMaxMinResult;
    }

    private boolean isChartEnabled(Chart chart) {
        for (LineData l : chart.lines) {
            if (l.enabled) {
                return true;
            }
        }
        return false;
    }

    private int getFirstVisibleXPosition(long[] xS, long minX) {
        int pos = Arrays.binarySearch(xS, minX);
        if (pos < 0) {
            pos = -(pos + 1);
        }

        return Math.max(0, pos - 1);
    }

    private int getLastVisibleXPosition(long[] xS, long maxX) {
        int pos = Arrays.binarySearch(xS, maxX);
        if (pos < 0) {
            pos = -(pos + 1);
        }

        return Math.min(xS.length - 1, pos + 1);
    }

    private Path getPath(long[] yVals, long[] xs) {
        mGraphPath.reset();
        int posToDrawFrom = getFirstVisibleXPosition(xs, getMinXConsideringRatio());
        int posToDrawTo = getLastVisibleXPosition(xs, getMaxXConsideringRatio());
        mGraphPath.moveTo(getPixelsXByPlotX(xs[posToDrawFrom]),
                getHeight() - getPixelsYByPlotY(yVals[posToDrawFrom]));
        for (int i = posToDrawFrom + 1; i <= posToDrawTo; i++) {
            mGraphPath.lineTo(getPixelsXByPlotX(xs[i]),
                    getHeight() - getPixelsYByPlotY(yVals[i]));
        }

        return mGraphPath;
    }

    private void drawChart(Canvas canvas, Chart chart) {
        long[] xs = chart.x;
        for (LineData lineData : chart.lines) {
            if (lineData.enabled) {
                mPaint.setColor(lineData.color);
                canvas.drawPath(getPath(lineData.vals, xs), mPaint);
            }
        }
    }

    private int constructPoints(long[] yVals, long[] xs) {
        int posToDrawFrom = getFirstVisibleXPosition(xs, getMinXConsideringRatio());
        int posToDrawTo = getLastVisibleXPosition(xs, getMaxXConsideringRatio());
        if (posToDrawFrom >= posToDrawTo) {
            return 0;
        }
        mPointsToDraw[0] = getPixelsXByPlotX(xs[posToDrawFrom]);
        mPointsToDraw[1] = getHeight() - getPixelsYByPlotY(yVals[posToDrawFrom]);
        mPointsToDraw[2] = getPixelsXByPlotX(xs[posToDrawFrom + 1]);
        mPointsToDraw[3] = getHeight() - getPixelsYByPlotY(yVals[posToDrawFrom + 1]);
        for (int i = posToDrawFrom + 2; i <= posToDrawTo; i++) {
            int realPos = (i - posToDrawFrom - 2) * 4 + 4;
            mPointsToDraw[realPos] = mPointsToDraw[realPos - 2];
            mPointsToDraw[realPos + 1] = mPointsToDraw[realPos - 1];
            mPointsToDraw[realPos + 2] = getPixelsXByPlotX(xs[i]);
            mPointsToDraw[realPos + 3] = getHeight() - getPixelsYByPlotY(yVals[i]);
        }


        return 4 + (posToDrawTo - posToDrawFrom - 1) * 4;
    }

    private void drawChartsWithLines(Canvas canvas, List<Chart> charts) {
        for (Chart c : charts) {
            for (LineData ld : c.lines) {
                if (ld.enabled) {
                    int count = constructPoints(ld.vals, c.x);
                    mPaint.setColor(ld.color);
                    canvas.drawLines(mPointsToDraw, 0, count, mPaint);
                }
            }
        }
    }

    private void drawLevelLines(Canvas canvas) {
        mTextPaint.setAlpha(255);
        float gap = mYGapPixels / getPixelPerValRatioY();
        long valsShift = (long) (mYShiftPixels / getPixelPerValRatioY());
        canvas.drawText(String.valueOf(mMinY),
                0f,
                getHeight() - (yIntervalToPixelsConsideringPadding(0) + mLevelValuePadding),
                mTextPaint);
        canvas.drawLine(
                0f,
                getHeight() - yIntervalToPixelsConsideringPadding(0),
                getWidth(), getHeight() - yIntervalToPixelsConsideringPadding(0),
                mLinesPaint);
        for (int i = 0; i <= NUMBER_OF_LEVEL_LINES + 1; i++) {
            float lineY = getHeight() - yIntervalToPixelsConsideringPadding((long) (gap * i))
                    - mYShiftPixels;
            if (lineY > mBottomPadding && lineY < getHeight() - mTopPadding) {
                canvas.drawText(String.valueOf(mMinY + (long)(gap * i) + valsShift),
                        0f,
                        getHeight() - (yIntervalToPixelsConsideringPadding((long) (gap * i)) + mLevelValuePadding) - mYShiftPixels,
                        mTextPaint);
                canvas.drawLine(
                        0f,
                        lineY,
                        getWidth(), lineY,
                        mLinesPaint);
            }
        }
    }

    private long getDistanceFromCenter() {
        long currentMinX = getMinXConsideringRatio();
        long currentMaxX = getMaxXConsideringRatio();
        long center = (mMaxX + mMinX) / 2;
        long gapCenter = (currentMaxX + currentMinX) / 2;
        return gapCenter - center;
    }

    private float getMarksPerPeriod() {
        float curPeriod = mMinGapPeriod + mPixelsPerRatio * (mLeftRatio + mRightRatio);
        return curPeriod / mMinGapPeriod;
    }

    private float getCurPeriod() {
        return mMinGapPeriod + mPixelsPerRatio * (mLeftRatio + mRightRatio);
    }

    private void drawXMarks(Canvas canvas) {
        float pixelPerValRatioX = getPixelPerValRatioX(getMaxXConsideringRatio(), getMinXConsideringRatio());
        float marksPerPeriod = getMarksPerPeriod();
        float curPeriod = getCurPeriod();
        float pixelsPerMarks = (float) (curPeriod / Math.pow(2, Math.floor(marksPerPeriod)));
        int shift = (int) (getDistanceFromCenter() * pixelPerValRatioX % curPeriod);
        float xPosition = -shift - curPeriod;
        int count = 0;
        while (xPosition < mScreenWidth) {
            if (count++ % 2 != 0) {
                float ratio = (float) (marksPerPeriod - Math.floor(marksPerPeriod));
                mTextPaint.setAlpha((int) (255 * ratio));
            } else {
                mTextPaint.setAlpha(255);
            }
            canvas.drawText(
                    String.valueOf(mXInterpolator.interpolate((long) (getMinXConsideringRatio() + (xPosition / pixelPerValRatioX)))),
                    xPosition, getHeight() - (mBottomPadding / 2f),
                    mTextPaint);
            xPosition += pixelsPerMarks;
        }
    }

    private void drawChatsTouchData(Canvas canvas) {
        if (mChartDetails == null) {
            return;
        }
        int pos = mChartDetails.closestPosition;
        long val = mChartDetails.chart.x[pos];
        float lineX = getPixelsXByPlotX(val);
        canvas.drawLine(lineX, mTopPadding, lineX, getHeight() - mBottomPadding, mLinesPaint);
        for (LineData l : mChartDetails.chart.lines) {
            if (l.enabled) {
                long y = l.vals[pos];
                mCirclesPaint.setColor(l.color);
                mCirclesPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(lineX, getHeight() - getPixelsYByPlotY(y),
                        mCircleRadius, mCirclesPaint);
                mCirclesPaint.setColor(mBackgroundColor);
                mCirclesPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(lineX, getHeight() - getPixelsYByPlotY(y),
                        mCircleRadius - mCirclesPaint.getStrokeWidth() / 2, mCirclesPaint);
            }
        }
    }

    private float getPixelsXByPlotX(long x) {
        long minX = getMinXConsideringRatio();
        float ratio = getPixelPerValRatioX(getMaxXConsideringRatio(), minX);
        return (x - minX) * ratio;
    }

    private float getPixelsYByPlotY(long y) {
        float ratio = getPixelPerValRatioY();
        return (y - mMinY) * ratio + mBottomPadding;
    }

    private float yIntervalToPixelsConsideringPadding(long interval) {
        float ratio = getPixelPerValRatioY();
        return interval * ratio + mBottomPadding;
    }

    /**
     * @return effective chart drawing height(where lines are drawn)
     */
    private float getRealHeight() {
        return getHeight() - mBottomPadding - mTopPadding;
    }

    /**
     * @return real maximum value considering {@link #mLeftRatio}
     */
    private long getMinXConsideringRatio() {
        return (long) (mMinX + (mMaxX - mMinX) * mLeftRatio);
    }

    /**
     * @return real maximum value considering {@link #mRightRatio}
     */
    private long getMaxXConsideringRatio() {
        return (long) (mMaxX - (mMaxX - mMinX) * mRightRatio);
    }

    private float getPixelPerValRatioY() {
        return getRealHeight() / (mMaxY - mMinY);
    }

    private float getPixelPerValRatioX(long maxX, long minX) {
        return (float) getWidth() / (maxX - minX);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBackgroundColor != Color.WHITE) {
            canvas.drawColor(mBackgroundColor);
        }
        if (!TextUtils.isEmpty(mTitle)) {
            mTitlePaint.getTextBounds(mTitle, 0, mTitle.length(), mTextRect);
            canvas.drawText(mTitle, 0, mTextRect.height(), mTitlePaint);
        }
        if (mCharts == null) {
            canvas.drawColor(Color.WHITE);
            return;
        }

        if (!mJustDrawGraph) {
            drawLevelLines(canvas);
            drawXMarks(canvas);
        }
        /*
          Different rendering modes may produce different results.
          Although Gpu rendering(using drawLines) should be faster because it skips
          rendering to bitmap but I've found that on some devices(COL-L29 for example)
          it's slower(I suspect that it's something wrong with the device because rendering there
          is slow in general).
          This mode can be changed using toolbar switch.
         */
        if (!mRenderingModeGpu) {
            for (Chart c : mCharts) {
                drawChart(canvas, c);
            }
        } else {
            drawChartsWithLines(canvas, mCharts);
        }
        if (!mJustDrawGraph) {
            drawChatsTouchData(canvas);
        }
    }

    @Override
    public void invalidate() {
        updateMaxMinX();
        updateMaxMinY();
        super.invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mYGapPixels = (long) (getRealHeight() / NUMBER_OF_LEVEL_LINES);
    }
}
