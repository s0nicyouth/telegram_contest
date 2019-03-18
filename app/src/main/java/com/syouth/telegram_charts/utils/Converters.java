package com.syouth.telegram_charts.utils;

import android.graphics.Color;

import com.syouth.telegram_charts.json.Chart;
import com.syouth.telegram_charts.view.GraphView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Converters {

    private static final String X_VALS_TYPES = "x";
    private static final String LINE_TYPE = "line";

    private static long[] toArray(List<Long> list) {
        long[] res = new long[list.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    public static float[] toArrayFloat(List<Float> list) {
        float[] res = new float[list.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = list.get(i);
        }
        return res;
    }

    private static long[] findXValues(Chart chart) {
        for (Chart.Column c : chart.columns) {
            String type = Objects.requireNonNull(chart.types.get(c.label));
            if (type.equals(X_VALS_TYPES)) {
                return toArray(c.vals);
            }
        }
        throw new IllegalStateException("Can't find x values");
    }

    private static ArrayList<GraphView.LineData> findLines(Chart chart) {
        ArrayList<GraphView.LineData> result = new ArrayList<>();
        for (Chart.Column c : chart.columns) {
            String type = Objects.requireNonNull(chart.types.get(c.label));
            if (type.equals(LINE_TYPE)) {
                String name = Objects.requireNonNull(chart.names.get(c.label));
                long[] vals = toArray(c.vals);
                int color = Objects.requireNonNull(Color.parseColor(chart.colors.get(c.label)));
                result.add(new GraphView.LineData(color, vals, name));
            }
        }

        return result;
    }

    public static GraphView.Chart from(Chart chart) {
        long[] xValues = findXValues(chart);
        List<GraphView.LineData> lines = findLines(chart);
        return new GraphView.Chart(xValues, lines);
    }
}
