package com.bmkg.retrofit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bmkg.retrofit.model_wilayah.SearchableLocation;

import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private List<LocationItem> list;
    private OnItemClick listener;

    public interface LocationItem {
        String getId();
        String getName();
    }

    public interface OnItemClick {
        void onClick(LocationItem item);
    }

    public LocationAdapter(List<LocationItem> list, OnItemClick listener) {
        this.list = list;
        this.listener = listener;
    }

    public void updateData(List<LocationItem> newList) {
        this.list.clear();
        this.list.addAll(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_location, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationItem currentItem = list.get(position);
        holder.name.setText(currentItem.getName());

        // Logika baru untuk menampilkan subtitle
        if (currentItem instanceof SearchableLocation) {
            String subtitle = ((SearchableLocation) currentItem).getSubtitle();
            if (subtitle != null && !subtitle.isEmpty()) {
                holder.code.setText(subtitle);
                holder.code.setVisibility(View.VISIBLE);
            } else {
                holder.code.setVisibility(View.GONE);
            }
        } else {
            // Logika fallback jika item bukan SearchableLocation
            holder.code.setText(currentItem.getId());
            holder.code.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(v -> listener.onClick(currentItem));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, code;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvName);
            code = itemView.findViewById(R.id.tvCode);
        }
    }
}