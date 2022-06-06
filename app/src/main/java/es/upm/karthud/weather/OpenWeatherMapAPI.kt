/**
 * Interfaz que alberga las funciones de la API de OpenWeatherMap que utilizamos en el proyecto
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud.weather

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherMapAPI
{
    //Documentación de los parámetros: https://howtodoinjava.com/retrofit2/query-path-parameters/
    /**
     * Obtiene la previsión del tiempo a partir de las coordenadas.
     * Permite fijar el idioma de la respuesta y las unidades de medida.
     */
    @GET("data/2.5/weather")
    suspend fun oneCallAPI(@Query("lat") lat: Double,
                           @Query("lon") lon: Double,
                           @Query("appid") appid: String,
                           @Query("lang") lang: String,
                           @Query("units") units: String): Response<WeatherResponse>
}