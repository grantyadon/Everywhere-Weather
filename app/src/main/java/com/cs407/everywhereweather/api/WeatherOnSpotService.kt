package com.cs407.everywhereweather.api

import okhttp3.ResponseBody
import retrofit2.Response

class WeatherOnSpotService {

    private val weatherOnSpotAPI = RetrofitClient.getClient().create(WeatherOnSpotAPI::class.java)

    fun fetchWeather(longitude: Double, latitude: Double, apiKey: String): WeatherOnSpotDTO? {
        val response: Response<WeatherOnSpotDTO> =
            weatherOnSpotAPI.getWeather(longitude, latitude, apiKey).execute()

        return if (response.isSuccessful) {
            response.body()
        } else {
            handleError(response.errorBody())
            null
        }
    }

    private fun handleError(errorBody: ResponseBody?) {
        println("Error: ${errorBody?.string() ?: "Unknown error"}")
    }
}
