package com.cs407.everywhereweather.api

data class RouteWeatherDTO(
    val routeLabel: List<String>,
    val route: Map<Cords, MinutelyWeatherDTO>
)

data class MinutelyWeatherDTO(
    val temperature: Float,
    val precipitation: Float
)
