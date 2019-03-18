package com.syouth.telegram_charts.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JsonParser {

    public static List<Chart> parse(String json) throws JSONException {
        List<Chart> result = new ArrayList<>();
        JSONArray chartsArray = new JSONArray(json);
        for (int i = 0; i < chartsArray.length(); i++) {
            JSONObject chart = chartsArray.getJSONObject(i);
            List<Chart.Column> columns = parseColumns(chart.getJSONArray("columns"));
            Map<String, String> types = parseTypes(chart.getJSONObject("types"));
            Map<String, String> names = parseNames(chart.getJSONObject("names"));
            Map<String, String> colors = parseColors(chart.getJSONObject("colors"));
            result.add(new Chart(columns, types, names, colors));
        }

        return result;
    }

    private static List<Chart.Column> parseColumns(JSONArray columnsArray) throws JSONException {
        List<Chart.Column> result = new ArrayList<>();
        for (int i = 0; i < columnsArray.length(); i++) {
            JSONArray column = columnsArray.getJSONArray(i);
            String label = column.getString(0);
            List<Long> vals = new ArrayList<>();
            for (int c = 1; c < column.length(); c++) {
                vals.add(column.getLong(c));
            }
            result.add(new Chart.Column(label, vals));
        }

        return result;
    }

    private static Map<String, String> parseTypes(JSONObject typesObject) throws JSONException {
        Map<String, String> result = new HashMap<>();
        for (Iterator<String> it = typesObject.keys(); it.hasNext(); ) {
            String k = it.next();
            result.put(k, typesObject.getString(k));
        }

        return result;
    }

    private static Map<String, String> parseNames(JSONObject namesObject) throws JSONException {
        Map<String, String> result = new HashMap<>();
        for (Iterator<String> it = namesObject.keys(); it.hasNext();) {
            String k = it.next();
            result.put(k, namesObject.getString(k));
        }

        return result;
    }

    private static Map<String, String> parseColors(JSONObject colorsObject) throws JSONException {
        Map<String, String> result = new HashMap<>();
        for (Iterator<String> it = colorsObject.keys(); it.hasNext();) {
            String k = it.next();
            result.put(k, colorsObject.getString(k));
        }

        return result;
    }
}
