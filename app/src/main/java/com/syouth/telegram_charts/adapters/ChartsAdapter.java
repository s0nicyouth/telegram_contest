package com.syouth.telegram_charts.adapters;

import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.syouth.telegram_charts.R;
import com.syouth.telegram_charts.view.GraphView;

import java.util.List;
import java.util.Objects;

public class ChartsAdapter extends RecyclerView.Adapter<ChartsAdapter.ViewHolder> {

    @Nullable
    private DataUpdatedListener mDataUpdatedListener;

    private List<GraphView.Chart> mCharts;
    private SparseArrayCompat<GraphView.LineData> mPositionToLine = new SparseArrayCompat<>();

    public void setData(List<GraphView.Chart> charts) {
        mCharts = charts;
        int pos = 0;
        for (GraphView.Chart c : mCharts) {
            for (GraphView.LineData l : c.lines) {
                mPositionToLine.put(pos, l);
                pos++;
            }
        }
        notifyDataSetChanged();
    }

    public void setDataUpdatedListener(DataUpdatedListener listener) {
        mDataUpdatedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chart_item, parent,
                false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        GraphView.LineData lineData = Objects.requireNonNull(mPositionToLine.get(i));
        viewHolder.text.setText(lineData.name);
        CompoundButtonCompat.setButtonTintList(viewHolder.checkbox,
                ColorStateList.valueOf(lineData.color));
        viewHolder.checkbox.setOnCheckedChangeListener(null);
        viewHolder.checkbox.setChecked(lineData.enabled);
        viewHolder.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            lineData.enabled = isChecked;
            if (mDataUpdatedListener != null) {
                mDataUpdatedListener.updated();
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mCharts == null) {
            return 0;
        }
        return mPositionToLine.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CheckBox checkbox;
        private TextView text;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            checkbox = itemView.findViewById(R.id.check);
            text = itemView.findViewById(R.id.check_text);
        }
    }

    public interface DataUpdatedListener {
        void updated();
    }

}
