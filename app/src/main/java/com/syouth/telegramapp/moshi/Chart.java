package com.syouth.telegramapp.moshi;

import com.squareup.moshi.Json;

import java.util.List;
import java.util.Map;

public class Chart {

    @Json(name = "columns")
    public List<Column> columns;

    @Json(name = "types")
    public Map<String, String> types;

    @Json(name = "names")
    public Map<String, String> names;

    @Json(name = "colors")
    public Map<String, String> colors;

    public static class Column {
        public String label;
        public List<Long> vals;

        public Column(String label, List<Long> vals) {
            this.label = label;
            this.vals = vals;
        }
    }
}


