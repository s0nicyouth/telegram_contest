package com.syouth.telegram_charts;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.syouth.telegram_charts.controllers.ChartController;
import com.syouth.telegram_charts.view.GraphView;
import com.syouth.telegram_charts.view.SliderView;
import com.syouth.telegram_charts.views.GraphsView;
import com.syouth.telegram_charts.views.SummaryRenderer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChartView extends AppCompatActivity implements GraphsView {

    private SliderView mSlider;
    private GraphView mGraphView;
    private GraphView mGraphMap;
    private RecyclerView mChartsList;
    private SummaryRenderer mSummaryRenderer;
    private Toolbar mToolbar;

    private ChartController mController;

    private boolean mNightMode = false;

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
        Date dt = new Date();
        mGraphView.setXValueInterpolator(val -> {
            dt.setTime(val);
            return sdt.format(dt);
        });
        mGraphView.setTitle("Followers");

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(Color.WHITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    private void changeNightMode() {
        mNightMode = !mNightMode;
        mGraphView.setNightMode(mNightMode);
        mSlider.setNightMode(mNightMode);
        mSummaryRenderer.setNightMode(mNightMode);
        if (mNightMode) {
            findViewById(R.id.background).setBackgroundColor(Color.parseColor("#212B35"));
            mToolbar.setBackgroundColor(Color.parseColor("#24313D"));
        } else {
            findViewById(R.id.background).setBackgroundColor(Color.WHITE);
            mToolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.night_mode:
                changeNightMode();
                return true;
            case R.id.change_rendering_mode:
                mGraphView.setRenderingModeGpu(!mGraphView.getRenderingModeGpu());
                mGraphMap.setRenderingModeGpu(!mGraphView.getRenderingModeGpu());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
