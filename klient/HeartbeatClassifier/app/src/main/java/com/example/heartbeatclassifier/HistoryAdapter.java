package com.example.heartbeatclassifier;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {
    private final List<Measurement> data;

    public HistoryAdapter(List<Measurement> data) { this.data = data; }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_measurement, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Measurement m = data.get(pos);
        h.txtTs.setText(m.ts);
        h.txtResult.setText(m.result);
        h.txtProb.setText(
                String.format("normal: %.2f  |  abnormal: %.2f", m.p_normal, m.p_abnormal)
        );
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtTs, txtResult, txtProb;
        VH(@NonNull View v) {
            super(v);
            txtTs    = v.findViewById(R.id.textTs);
            txtResult= v.findViewById(R.id.textResult);
            txtProb  = v.findViewById(R.id.textProb);
        }
    }
}
