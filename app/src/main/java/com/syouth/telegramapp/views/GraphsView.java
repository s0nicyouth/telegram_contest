package com.syouth.telegramapp.views;

import android.content.res.AssetManager;
import android.support.v7.widget.RecyclerView;

import com.syouth.telegramapp.view.GraphView;
import com.syouth.telegramapp.view.SliderView;

public interface GraphsView {
    SliderView getSlider();
    GraphView getGraphView();
    GraphView getGraphMap();
    RecyclerView getChartList();
    SummaryRenderer getSummaryRenderer();

    AssetManager getAssets();
}
