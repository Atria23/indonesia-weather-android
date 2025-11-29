package com.bmkg.retrofit.api_wilayah;

import com.bmkg.retrofit.model_wilayah.Kabupaten;
import com.bmkg.retrofit.model_wilayah.Kecamatan;
import com.bmkg.retrofit.model_wilayah.Kelurahan;
import com.bmkg.retrofit.model_wilayah.Provinsi;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WilayahApiService {

    @GET("provinsi.json")
    Call<List<Provinsi>> getProvinsi();

    @GET("kabupaten/{provinsi_id}.json")
    Call<List<Kabupaten>> getKabupaten(@Path("provinsi_id") String provinsiId);

    @GET("kecamatan/{kabupaten_id}.json")
    Call<List<Kecamatan>> getKecamatan(@Path("kabupaten_id") String kabupatenId);

    @GET("kelurahan/{kecamatan_id}.json")
    Call<List<Kelurahan>> getKelurahan(@Path("kecamatan_id") String kecamatanId);

}