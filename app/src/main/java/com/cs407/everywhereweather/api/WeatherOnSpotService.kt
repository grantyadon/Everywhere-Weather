package com.cs407.everywhereweather.api

import retrofit2.Response

class WeatherOnSpotService {

    private val weatherOnSpotAPI = RetrofitClient.getClient().create(WeatherOnSpotAPI::class.java)

    fun fetchWeather(request: GetSpotWeatherRequest): WeatherResponse? {
        val response: Response<WeatherResponse> =
            weatherOnSpotAPI.getSpotWeather(request).execute()

        return if (response.isSuccessful) {
            val weatherResponse = response.body()
            handleWeatherResponse(weatherResponse)
            weatherResponse
        } else {
            handleError(response.errorBody()?.string())
            null
        }
    }

    private fun handleWeatherResponse(weather: WeatherResponse?) {
        when (weather) {
            is CurrentWeatherResponse -> {
                println("Current Weather: ${weather.weatherDescription}")
            }
            is HourlyWeatherResponse -> {
                println("Hourly Weather: ${weather.weatherDescription}")
            }
            is DailyWeatherResponse -> {
                println("Daily Weather: ${weather.summary}")
            }
            else -> {
                println("Unknown Weather Response")
            }
        }
    }

    private fun handleError(error: String?) {
        println("Error: $error")
    }
}
