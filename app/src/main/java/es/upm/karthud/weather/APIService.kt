package es.upm.karthud.weather

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService
{
    //https://howtodoinjava.com/retrofit2/query-path-parameters/
    @GET("data/2.5/weather")
    suspend fun oneCallAPI(@Query("lat") lat: Double,
                           @Query("lon") lon: Double,
                           @Query("appid") appid: String,
                           @Query("lang") lang: String,
                           @Query("units") units: String): Response<WeatherResponse>
}