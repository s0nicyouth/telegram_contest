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

public class ChartsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int CHART_TITLE_TYPE = 0;
    private static final int CHART_LINE_TYPE = 1;

    @Nullable
    private DataUpdatedListener mDataUpdatedListener;

    private List<GraphView.Chart> mCharts;
    private SparseArrayCompat<GraphView.LineData> mPositionToLine = new SparseArrayCompat<>();
    private SparseArrayCompat<String> mTitlesPositions = new SparseArrayCompat<>();

    public void setData(List<GraphView.Chart> charts) {
        mCharts = charts;
        int pos = 0;
        int chartNum = 1;
        for (GraphView.Chart c : mCharts) {
            mTitlesPositions.put(pos++, "Chart # " + chartNum++);
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        if (getItemViewType(position) == CHART_TITLE_TYPE) {
            return new ViewHolderTitle(LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.chart_title, parent, false));
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chart_item,
                    parent,
                    false);
            return new ViewHolderLine(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder vh, int position) {
        if (getItemViewType(position) == CHART_LINE_TYPE) {
            ViewHolderLine viewHolder = (ViewHolderLine) vh;
            GraphView.LineData lineData = Objects.requireNonNull(mPositionToLine.get(position));
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
        } else {
            ViewHolderTitle viewHolder = (ViewHolderTitle) vh;
            viewHolder.title.setText(mTitlesPositions.get(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mTitlesPositions.containsKey(position)) {
            return CHART_TITLE_TYPE;
        } else {
            return CHART_LINE_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        if (mCharts == null) {
            return 0;
        }
        return mPositionToLine.size() + mTitlesPositions.size();
    }

    private static class ViewHolderLine extends RecyclerView.ViewHolder {

        private CheckBox checkbox;
        private TextView text;

        private ViewHolderLine(@NonNull View itemView) {
            super(itemView);

            checkbox = itemView.findViewById(R.id.check);
            text = itemView.findViewById(R.id.check_text);
        }
    }

    private static class ViewHolderTitle extends RecyclerView.ViewHolder {

        private TextView title;

        private ViewHolderTitle(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView;
        }
    }

    public interface DataUpdatedListener {
        void updated();
    }

}
