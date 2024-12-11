package com.cs407.everywhereweather.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface WeatherOnRouteAPI {
    @POST("getWeatherOnRoute")
    fun getWeatherOnRoute(
        @Body request: GoogleRoutesResponse
    ): Call<List<RouteWeatherDTO>>
}

data class GetRouteWeatherRequest(
    val route: GoogleRoutesResponse,
    val timeOffset: Int
)

data class GoogleRoutesResponse(
    @SerializedName("routes") val routes: List<Route>
) {
    data class Route(
        @SerializedName("legs") val legs: List<Leg>,
        @SerializedName("summary") val summary: String,
        @SerializedName("overviewPolyline") val overviewPolyline: OverviewPolyline?
    )

    data class Leg(
        @SerializedName("start_location") val startLocation: Location,
        @SerializedName("end_location") val endLocation: Location,
        @SerializedName("steps") val steps: List<Step>,
        @SerializedName("distanceMeters") val distanceMeters: Int,
        @SerializedName("duration") val duration: Duration,
        @SerializedName("staticDuration") val staticDuration: String?
    ) {
        data class Duration(
            @SerializedName("value") val value: Int, // Duration in seconds
            @SerializedName("text") val text: String // Readable format like "1 min"
        )
    }


    data class Step(
        @SerializedName("start_location") var startLocation: Location,
        @SerializedName("end_location") var endLocation: Location,
        @SerializedName("polyline") val polyline: Polyline,
        @SerializedName("distanceMeters") val distanceMeters: Int,
        @SerializedName("staticDuration") val staticDuration: String
    )

    data class Polyline(
        @SerializedName("points") val encodedPolyline: String
    )

    data class OverviewPolyline(
        @SerializedName("points") val encodedPolyline: String
    )

    data class Location(
        @SerializedName("lat") val latitude: Double,
        @SerializedName("lng") val longitude: Double
    )
}



data class RouteWeatherResponse(
    val weatherInfo: List<String>
)
