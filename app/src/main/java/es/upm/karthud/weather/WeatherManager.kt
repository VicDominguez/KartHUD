package es.upm.karthud.weather

import es.upm.karthud.appid
import es.upm.karthud.lang
import es.upm.karthud.track.Coord
import es.upm.karthud.units
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherManager
{
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    suspend fun getTemperature(c: Coord): Int?
    {
        val call = retrofit
            .create(OpenWeatherMapAPI::class.java)
            .oneCallAPI(c.latitude, c.longitude, appid, lang, units)
        val response = call.body()
        return if(call.isSuccessful) response?.main?.temp?.toInt() else null
    }
}