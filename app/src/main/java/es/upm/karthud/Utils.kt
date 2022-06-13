/**
 * Objeto singleton que contiene valores a utilizar por el resto de la app
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud

import android.Manifest
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import es.upm.karthud.persistence.AppDatabase
import java.text.SimpleDateFormat
import java.util.*

object Utils
{
    //objetos de firebase
    val remoteDbReference: DatabaseReference by lazy { FirebaseDatabase.getInstance().reference }
    val remoteAuthInstance: FirebaseAuth = FirebaseAuth.getInstance()

    //gestion de permisos
    const val gpsPermission: String = Manifest.permission.ACCESS_FINE_LOCATION
    const val gpsPermissionRequest : Int = 123

    //ajustes de periodos
    const val minTimeMs: Long = 250
    const val minDistance : Float = 0.0f
    const val initialDelayWeather: Long = 1000
    const val periodWeather: Long = 600000 //10 minutes
    const val periodAccelerometer : Int = 1000000 //1s = 1000000 us

    //conversiones
    const val msToKmh : Double = 3.6

    //weather
    const val appid = "a6b8ce9e32c991c71947233b1a6cf54f"
    const val lang = "es"
    const val units = "metric"
    const val baseUrl = "https://api.openweathermap.org/"

    //formatos de horas
    val formatoHMS = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
    val formatoDMA = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)

    fun longToStringTime(l: Long): String
    {
        fun Long.format(digits: Int) = "%0${digits}d".format(this)

        val miliSeconds = l % 1000
        val seconds = (l / 1000) % 60
        val minutes = (l / 1000) / 60
        return "$minutes:${seconds.format(2)}:${miliSeconds.format(3)}"
    }

    //bbdd
    fun getLocalDatabase(context: Context) = AppDatabase.getDatabase(context)
}