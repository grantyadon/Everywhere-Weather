package com.cs407.everywhereweather.api
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface WeatherOnSpotAPI {
    @POST("getWeatherOnSpot")
    fun getSpotWeather(@Body request: GetSpotWeatherRequest): Call<WeatherResponse>
}
data class Cords(
    val latitude: Double,
    val longitude: Double
)

data class GetSpotWeatherRequest(
    val cords: Cords,
    val timeOffset: Int
)

