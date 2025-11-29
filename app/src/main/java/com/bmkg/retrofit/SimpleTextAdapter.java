package com.bmkg.retrofit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SimpleTextAdapter extends RecyclerView.Adapter<SimpleTextAdapter.ViewHolder> {
    private List<String> data;
    private OnItemClick listener;

    public interface OnItemClick {
        void onClick(String text);
    }

    public SimpleTextAdapter(List<String> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String text = data.get(position);
        holder.tvText.setText(text);
        holder.itemView.setOnClickListener(v -> listener.onClick(text));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvText;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(android.R.id.text1);
        }
    }
}