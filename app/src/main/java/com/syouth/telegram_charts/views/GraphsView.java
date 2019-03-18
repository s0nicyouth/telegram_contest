package com.syouth.telegram_charts.views;

import android.content.res.AssetManager;
import android.support.v7.widget.RecyclerView;

import com.syouth.telegram_charts.view.GraphView;
import com.syouth.telegram_charts.view.SliderView;

public interface GraphsView {
    SliderView getSlider();
    GraphView getGraphView();
    GraphView getGraphMap();
    RecyclerView getChartList();
    SummaryRenderer getSummaryRenderer();

    AssetManager getAssets();
}
