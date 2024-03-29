package com.syouth.telegram_charts.controllers;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;

import com.syouth.telegram_charts.adapters.ChartsAdapter;
import com.syouth.telegram_charts.utils.Data;
import com.syouth.telegram_charts.view.GraphView;
import com.syouth.telegram_charts.view.SliderView;
import com.syouth.telegram_charts.views.GraphsView;

import java.util.List;

public class ChartController implements SliderView.RatioChangeListener {

    private final GraphsView mView;
    private final ChartsAdapter mChartsAdapter = new ChartsAdapter();
    private final Handler mMainThread = new Handler(Looper.getMainLooper());

    public ChartController(GraphsView graphsView) {
        HandlerThread handlerThread = new HandlerThread("Bg");
        handlerThread.start();
        Handler bgThread = new Handler(handlerThread.getLooper());
        mView = graphsView;

        mView.getSlider().setRatioChangeListener(this);
        mView.getGraphMap().setJustDrawGraph(true);

        bgThread.post(() -> {
            List<GraphView.Chart> data = Data.getData(mView.getAssets());
            mMainThread.post(() -> {
                mView.getGraphMap().setData(data);
                mView.getGraphView().setData(data);
                setUpChartsList(data);
            });
        });
        mView.getGraphView().setChartTouchListener(
                details -> mView.getSummaryRenderer().render(details));
    }

    private void setUpChartsList(List<GraphView.Chart> charts) {
        RecyclerView chartList = mView.getChartList();
        chartList.setAdapter(mChartsAdapter);
        mChartsAdapter.setData(charts);
        mChartsAdapter.setDataUpdatedListener(() -> {
            mView.getGraphView().invalidate();
            mView.getGraphMap().invalidate();
        });
    }

    @Override
    public void changed(float left, float right) {
        mView.getGraphView().setLeftRatio(left);
        mView.getGraphView().setRightRatio(right);
    }
}
