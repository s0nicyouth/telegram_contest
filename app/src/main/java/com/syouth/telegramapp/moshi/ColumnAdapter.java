package com.syouth.telegramapp.moshi;

import android.support.annotation.Nullable;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

public class ColumnAdapter extends JsonAdapter<Chart.Column> {

    public static class ColumsAdapterFactory implements Factory {

        @Nullable
        @Override
        public JsonAdapter<Chart.Column> create(Type type,
                                     Set<? extends Annotation> annotations,
                                     Moshi moshi) {
            if (type.equals(Chart.Column.class)) {
                return new ColumnAdapter();
            }
            return null;
        }
    }

    @Nullable
    @Override
    public Chart.Column fromJson(JsonReader reader) throws IOException {
        reader.beginArray();
        String label = reader.nextString();
        ArrayList<Long> vals = new ArrayList<>();
        while (reader.hasNext()) {
            vals.add(reader.nextLong());
        }
        reader.endArray();

        return new Chart.Column(label, vals);
    }

    @Override
    public void toJson(JsonWriter writer, @Nullable Chart.Column value) throws IOException {
        throw new IOException("Not supported");
    }
}
