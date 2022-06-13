/**
 * Contiene las operaciones que se pueden realizar con la API del tiempo
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud.weather

import es.upm.karthud.Utils
import es.upm.karthud.track.Coord
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherManager
{
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Utils.baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Devuelve la temperatura en un número entero a partir de las coordenadas del lugar deseado
     */
    suspend fun getTemperature(c: Coord): Int?
    {
        val call = retrofit
            .create(IWeatherAPI::class.java)
            .oneCallAPI(c.latitude, c.longitude, Utils.appid, Utils.lang, Utils.units)
        val response = call.body()
        return if(call.isSuccessful) response?.main?.temp?.toInt() else null
    }
}