package es.upm.karthud

import android.Manifest

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