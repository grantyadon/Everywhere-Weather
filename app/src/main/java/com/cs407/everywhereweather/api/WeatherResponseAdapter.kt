package com.cs407.everywhereweather.api

import com.google.gson.*
import java.lang.reflect.Type

class WeatherResponseAdapter : JsonDeserializer<WeatherResponse> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): WeatherResponse {
        val jsonObject = json.asJsonObject

        return when {
            jsonObject.has("temp") && jsonObject.has("feelsLike") -> {
                context.deserialize(jsonObject, CurrentWeatherResponse::class.java)
            }
            jsonObject.has("tempMorn") && jsonObject.has("tempDay") -> {
                context.deserialize(jsonObject, DailyWeatherResponse::class.java)
            }
            else -> {
                context.deserialize(jsonObject, HourlyWeatherResponse::class.java)
            }
        }
    }
}
