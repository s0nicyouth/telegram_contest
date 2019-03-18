package com.syouth.telegram_charts.json;

import java.util.List;
import java.util.Map;

public class Chart {

    public List<Column> columns;

    public Map<String, String> types;

    public Map<String, String> names;

    public Map<String, String> colors;

    Chart(List<Column> columns, Map<String, String> types,
          Map<String, String> names, Map<String, String> colors) {
        this.columns = columns;
        this.types = types;
        this.names = names;
        this.colors = colors;
    }

    public static class Column {
        public String label;
        public List<Long> vals;

        Column(String label, List<Long> vals) {
            this.label = label;
            this.vals = vals;
        }
    }
}


