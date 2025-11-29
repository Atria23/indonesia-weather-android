package com.bmkg.retrofit.model_wilayah;

import com.bmkg.retrofit.LocationAdapter;

public class SearchableLocation implements LocationAdapter.LocationItem {
    public enum Type { PROVINSI, KABUPATEN, KECAMATAN, KELURAHAN }

    private String id;
    private String name;
    private String subtitle; // e.g., "Jawa Barat" untuk "Kota Bandung"
    private Type type;

    public SearchableLocation(String id, String name, String subtitle, Type type) {
        this.id = id;
        this.name = name;
        this.subtitle = subtitle;
        this.type = type;
    }

    @Override
    public String getId() { return id; }

    @Override
    public String getName() { return name; }

    public String getSubtitle() { return subtitle; }

    public Type getType() { return type; }
}