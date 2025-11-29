package com.bmkg.retrofit.model_wilayah;

import com.bmkg.retrofit.LocationAdapter;

public class Kecamatan implements LocationAdapter.LocationItem {
    public String id;
    public String nama;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return nama;
    }
}
