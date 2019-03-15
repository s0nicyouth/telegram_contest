package com.syouth.telegramapp.views;

import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.syouth.telegramapp.R;
import com.syouth.telegramapp.view.GraphView;
import com.syouth.telegramapp.view.SummaryView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class SummaryRenderer {

    private static final int SUMMARY_PADDING = 10; // dp

    private final SummaryView mSummary;
    private final FrameLayout mContainer;
    private final SimpleDateFormat mDateFormatter = new SimpleDateFormat("EEE, MMM dd");
    private final float mSummaryPadding;

    public SummaryRenderer(SummaryView summaryView, FrameLayout container) {
        mSummary = summaryView;
        mContainer = container;
        mSummary.setVisibility(View.INVISIBLE);
        mSummaryPadding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                SUMMARY_PADDING,
                mSummary.getResources().getDisplayMetrics());
        ViewCompat.setElevation(mSummary,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        10,
                        mSummary.getResources().getDisplayMetrics()));
    }

    public void render(@Nullable GraphView.ChartDetails chartDetails) {
        if (chartDetails == null) {
            mSummary.setVisibility(View.INVISIBLE);
            return;
        }
        mSummary.removeAllViews();
        mSummary.setVisibility(View.VISIBLE);
        for (GraphView.LineData l : chartDetails.chart.lines) {
            View v = LayoutInflater.from(
                    mSummary.getContext()).inflate(R.layout.summary_item, mSummary, false);
            TextView tv = v.findViewById(R.id.value);
            tv.setText(String.valueOf(l.vals[chartDetails.closestPosition]));
            tv.setTextColor(l.color);
            tv = v.findViewById(R.id.text);
            tv.setText(l.name);
            tv.setTextColor(l.color);
            mSummary.addView(v);
            mSummary.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int width = mSummary.getMeasuredWidth();
            float newPos = chartDetails.positionXInView + mSummaryPadding;
            if (newPos + width < mContainer.getWidth()) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSummary.getLayoutParams();
                lp.setMargins((int) (chartDetails.positionXInView + mSummaryPadding),
                        0, 0, 0);
                mSummary.setLayoutParams(lp);
            } else {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSummary.getLayoutParams();
                lp.setMargins((int) (chartDetails.positionXInView - mSummaryPadding - width),
                        0, 0, 0);
                mSummary.setLayoutParams(lp);
            }
        }
        Date title = new Date(TimeUnit.SECONDS.toMillis(
                chartDetails.chart.x[chartDetails.closestPosition]));
        mSummary.setTitle(mDateFormatter.format(title));
    }
}
