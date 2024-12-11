package com.cs407.everywhereweather.api

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.google.gson.annotations.JsonAdapter

@JsonAdapter(WeatherResponseAdapter::class)

sealed class WeatherResponse

data class CurrentWeatherResponse(
    val dt: Long,
    val temp: Double,
    val feelsLike: Double,
    val windSpeed: Double,
    val rain: Double,
    val snow: Double,
    val weatherDescription: String,
    val precipitationChance: Double
) : WeatherResponse()

data class HourlyWeatherResponse(
    val dt: Long,
    val temp: Double,
    val feelsLike: Double,
    val windSpeed: Double,
    val rain: Double,
    val snow: Double,
    val weatherDescription: String,
    val precipitationChance: Double
) : WeatherResponse()

data class DailyWeatherResponse(
    val dt: Long,
    val tempMorn: Double,
    val tempDay: Double,
    val tempEve: Double,
    val tempNight: Double,
    val tempMin: Double,
    val tempMax: Double,
    val feelsLikeMorn: Double,
    val feelsLikeDay: Double,
    val feelsLikeEve: Double,
    val feelsLikeNight: Double,
    val windSpeed: Double,
    val precipitationChance: Double,
    val summary: String,
    val rain: Double,
    val snow: Double,
    val weatherDescription: String
) : WeatherResponse()
