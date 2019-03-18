package com.syouth.telegramapp.utils;

import android.content.res.AssetManager;

import com.syouth.telegramapp.json.Chart;
import com.syouth.telegramapp.json.JsonParser;
import com.syouth.telegramapp.view.GraphView;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Data {

    public static List<GraphView.Chart> getData(AssetManager assetsManager) {
        try (InputStream in = assetsManager.open("chart_data.json")) {
            String json = Streams.readFull(in);
            List<Chart> list = JsonParser.parse(json);
            ArrayList<GraphView.Chart> charts = new ArrayList<>(
                    Objects.requireNonNull(list).size());
            for (Chart c : list) {
                charts.add(Converters.from(c));
            }
            return charts;
        } catch (IOException | JSONException ignored) {
            return null;
        }
    }
}
