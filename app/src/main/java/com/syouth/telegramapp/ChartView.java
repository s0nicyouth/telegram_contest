package com.syouth.telegramapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.syouth.telegramapp.controllers.ChartController;
import com.syouth.telegramapp.view.GraphView;
import com.syouth.telegramapp.view.SliderView;
import com.syouth.telegramapp.views.GraphsView;
import com.syouth.telegramapp.views.SummaryRenderer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ChartView extends AppCompatActivity implements GraphsView {

    private SliderView mSlider;
    private GraphView mGraphView;
    private GraphView mGraphMap;
    private RecyclerView mChartsList;
    private SummaryRenderer mSummaryRenderer;

    private ChartController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGraphView = findViewById(R.id.graph);
        mSlider = findViewById(R.id.slider);
        mGraphMap = findViewById(R.id.slided_graph);
        mChartsList = findViewById(R.id.charts_list);
        mSummaryRenderer = new SummaryRenderer(findViewById(R.id.summary),
                findViewById(R.id.container));

        mChartsList.setLayoutManager(new LinearLayoutManager(this));

        mController = new ChartController(this);
        DateFormat sdt = new SimpleDateFormat("MMM dd");
        mGraphView.setXValueInterpolator(val -> {
            Date dt = new Date(TimeUnit.SECONDS.toMillis(val));
            return sdt.format(dt);
        });
    }

    @Override
    public SliderView getSlider() {
        return mSlider;
    }

    @Override
    public GraphView getGraphView() {
        return mGraphView;
    }

    @Override
    public GraphView getGraphMap() {
        return mGraphMap;
    }

    @Override
    public RecyclerView getChartList() {
        return mChartsList;
    }

    @Override
    public SummaryRenderer getSummaryRenderer() {
        return mSummaryRenderer;
    }
}
