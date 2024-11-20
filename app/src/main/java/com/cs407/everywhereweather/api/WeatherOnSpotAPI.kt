package com.cs407.everywhereweather.api
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherOnSpotAPI {
    @GET("weather")
    fun getWeather(
        @Query("lon") longitude: Double,
        @Query("lat") latitude: Double,
        @Query("appid") apiKey: String
    ): Call<WeatherOnSpotDTO>
}
