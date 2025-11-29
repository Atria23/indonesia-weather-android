package com.bmkg.retrofit.api;

import com.bmkg.retrofit.model.ResponseData;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("prakiraan-cuaca")
    Call<ResponseData> getWeatherByAdm4(
            @Query("adm4") String adm4
    );
}
