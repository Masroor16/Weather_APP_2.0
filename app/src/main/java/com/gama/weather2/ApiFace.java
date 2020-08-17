package com.gama.weather2;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiFace {

    @GET("data/2.5/weather?units=metric")
    Call<Weather> getCurrentData(@Query("lat") String lat, @Query("lon") String lon, @Query("APPID") String app_id);
}
