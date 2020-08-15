package com.gama.weather2;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("weather?appid=957b2b5e5ae2f3d67d26fc4af1618784&units=metric")
    Call<Weather> getWeatherData(@Query("q") String name);

}
