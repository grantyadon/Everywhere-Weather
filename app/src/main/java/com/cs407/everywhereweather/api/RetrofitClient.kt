package com.cs407.everywhereweather.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.jackson.JacksonConverterFactory

object RetrofitClient {
//    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/maps/api/"
    private val okHttpClient: OkHttpClient = OkHttpClient()
        .newBuilder()
        .build()

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(WeatherResponse::class.java, WeatherResponseAdapter())
        .create()

    fun getClient(): Retrofit =
        Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    fun getGoogleApiClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(GOOGLE_MAPS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}