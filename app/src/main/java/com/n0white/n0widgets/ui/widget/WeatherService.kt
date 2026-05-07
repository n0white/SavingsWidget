package com.n0white.n0widgets.ui.widget

import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.serialization.json.Json

@Serializable
data class WeatherInfo(
    val temp: Int,
    val iconResId: Int
)

@Serializable
data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>
)

@Serializable
data class Main(
    val temp: Double
)

@Serializable
data class Weather(
    val main: String,
    val icon: String
)

@Serializable
data class CitySuggestion(
    val name: String,
    val country: String,
    val state: String? = null,
    val lat: Double,
    val lon: Double
)

interface OpenWeatherMapApi {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    @GET("geo/1.0/direct")
    suspend fun findCities(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<CitySuggestion>
}

object WeatherService {
    private val json = Json { ignoreUnknownKeys = true }
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: OpenWeatherMapApi = retrofit.create(OpenWeatherMapApi::class.java)
}
