package com.syouth.telegramapp.utils;

import android.content.res.AssetManager;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.syouth.telegramapp.moshi.Chart;
import com.syouth.telegramapp.view.GraphView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Data {

    public static List<GraphView.Chart> getData(AssetManager assetsManager, Moshi moshi) {
        try (InputStream in = assetsManager.open("chart_data.json")) {
            String json = Streams.readFull(in);
            ParameterizedType type = Types.newParameterizedType(List.class, Chart.class);
            JsonAdapter<List<Chart>> adapter = moshi.adapter(type);
            List<Chart> list = adapter.fromJson(json);
            ArrayList<GraphView.Chart> charts = new ArrayList<>(
                    Objects.requireNonNull(list).size());
            for (Chart c : list) {
                charts.add(Converters.from(c));
            }
            return charts;
        } catch (IOException ignored) {
            return null;
        }
    }
}
