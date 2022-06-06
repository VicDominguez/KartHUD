/**
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud

import android.Manifest
import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import es.upm.karthud.databinding.ActivityMainBinding
import es.upm.karthud.track.Checkpoint
import es.upm.karthud.track.Circuit
import es.upm.karthud.track.Coord
import es.upm.karthud.weather.OpenWeatherMapAPI
import java.lang.Exception
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity(), LocationListener, SensorEventListener, EasyPermissions.PermissionCallbacks
{
    //gestion de permisos
    private val gpsPermission: String = Manifest.permission.ACCESS_FINE_LOCATION
    private val gpsPermissionRequest : Int = 123

    //constantes
    private val minTimeMs: Long = 250
    private val minDistance : Float = 0.0f
    private val msToKmh : Double = 3.6
    private val initialDelayWeather: Long = 1000
    private val periodWeather: Long = 600000 //10 minutes
    private val periodAccelerometer : Int = 1000000 //1s = 1000000 us
    /*
    Accelerometer, SENSOR_DELAY_FASTEST: 18-20 ms
    Accelerometer, SENSOR_DELAY_GAME: 37-39 ms
    Accelerometer, SENSOR_DELAY_UI: 85-87 ms
    Accelerometer, SENSOR_DELAY_NORMAL: 215-230 ms
     */

    //textviews con binding: https://cursokotlin.com/capitulo-29-view-binding-en-kotlin/
    private lateinit var binding: ActivityMainBinding

    //objetos manager
    private val locationManager : LocationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }
    private val sensorManager: SensorManager by lazy {getSystemService(SENSOR_SERVICE) as SensorManager}

    //sensores
    private val accelerometer : Sensor? by lazy {sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)}

    //timer
    private val timer: Timer by lazy { Timer("Weather",false) }

    //variables
    private var speed: Float = 0.0f
    private var lap: Int = 0
    private var timestampStartLap : Long = 0
    private var timestampEndLap : Long? = null

    private var bestLap : Long = Long.MAX_VALUE

    private var lastLocation: Location? = null
    private var lapTimes : ArrayList<Long> = arrayListOf<Long>()
    private var circuit: Circuit = Circuit(Checkpoint(
        Coord(40.43009068580006, -3.4454841660571036),
        Coord(40.430592940063164, -3.4446419524948317))) //rotonda henakart

    /*
    ---------------------------------------------------------
        Estados de la actividad
    ---------------------------------------------------------
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        binding.chronometer.start()

        startAccelerometer()
        checkAndStartGPS()
        startWeather()
    }

    override fun onPause()
    {
        super.onPause()
        stopAccelerometer()
        stopGPS()
        stopWeather()
    }

    override fun onResume()
    {
        super.onResume()
        startAccelerometer()
        checkAndStartGPS()
        startWeather()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        stopAccelerometer()
        stopGPS()
        stopWeather()
    }

    /*
    ---------------------------------------------------------
        Callbacks de manejo de permisos
    ---------------------------------------------------------
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>)
    {
        Toast.makeText(this, "Permisos concedidos, iniciando GPS", Toast.LENGTH_SHORT).show()
        startGPSUnchecked()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>)
    {
        binding.speedText.text = getString(R.string.kmh,"?")
        binding.lapText.text = getString(R.string.lap, 0)
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
            AppSettingsDialog.Builder(this).build().show()
    }

    /*
    ---------------------------------------------------------
        Gestión de la funcionalidad
    ---------------------------------------------------------
     */

    /**
     * Comprueba si la app dispone de permisos. Si los dispone arranca el GPS y si no solicita los permisos.
     * @see startGPSUnchecked
     */
    private fun checkAndStartGPS()
    {
        if (EasyPermissions.hasPermissions(this, gpsPermission))
           startGPSUnchecked()
        else
            EasyPermissions.requestPermissions(this, getString(R.string.gps_not_granted),gpsPermissionRequest, gpsPermission)
    }

    /**
     * Arranca la solicitud de localizaciones sin comprobar si dispone de permisos o no.
     * @see checkAndStartGPS
     */
    @SuppressLint("MissingPermission")
    private fun startGPSUnchecked()
    {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeMs, minDistance, this)
    }

    private fun stopGPS()
    {
        locationManager.removeUpdates(this)
    }

    private fun startAccelerometer()
    {
        if(accelerometer == null)
        {
            Toast.makeText(this,R.string.accelerometer_not_available, Toast.LENGTH_LONG).show()
            binding.gForceText.visibility = View.INVISIBLE
        }
        else
        {
            sensorManager.registerListener(this, accelerometer, periodAccelerometer)
        }
    }

    private fun stopAccelerometer()
    {
        if (accelerometer != null)
            sensorManager.unregisterListener(this, accelerometer)
    }

    private fun startWeather()
    {
        timer.schedule(initialDelayWeather,periodWeather){
            getTemperature(circuit.endLine.beacon1)
        }
    }

    private fun stopWeather()
    {
        timer.cancel()
    }

    /*
    ---------------------------------------------------------
        Callback de LocationListener
    ---------------------------------------------------------
     */


    private fun longToStringTime(l: Long): String
    {
        fun Long.format(digits: Int) = "%0${digits}d".format(this)

        val miliSeconds = l % 1000
        val seconds = (l / 1000) % 60
        val minutes = (l / 1000) / 60
        return "$minutes:${seconds.format(2)}:${miliSeconds.format(3)}"
    }

    private fun updateUILaps()
    {
        binding.bestLapTime.visibility = View.VISIBLE
        binding.lastLapTime.visibility = View.VISIBLE

        binding.bestLapTime.text = getString(R.string.bestLapTime, longToStringTime(bestLap))
        binding.lastLapTime.text = getString(R.string.lastLapTime, longToStringTime(lapTimes.last()))
    }

    override fun onLocationChanged(l: Location)
    {
        //cuando empieza la app
        if (lastLocation == null)
        {
            timestampStartLap = l.time
        }
        //si tenemos dos puntos vemos si hay corte
        lastLocation?.let { itLocation ->
            timestampEndLap = circuit.timeStampLineCrossed(itLocation, l) //null si no hay corte, timestamp si lo hay
            timestampEndLap?.let { itLong ->
                if (lap > 0)
                {
                    val timeElapsed = itLong-timestampStartLap
                    if(timeElapsed < bestLap)
                        bestLap = timeElapsed
                    lapTimes.add(timeElapsed)
                    updateUILaps()
                }
                lap++
                timestampStartLap = itLong
                //faltaria cuadrar bien los ms
                binding.chronometer.base = SystemClock.elapsedRealtime()
                }
        }

        lastLocation = l
        speed = ((l.speed * msToKmh * 10.0).roundToInt() / 10.0).toFloat()

        binding.lapText.text = getString(R.string.lap, lap)
        binding.speedText.text = getString(R.string.kmh,speed.toString())
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onSensorChanged(se: SensorEvent?)
    {
        /*
        Los ejes del acelerómetro son:
        - x: atraviesa la pantalla horizontalmente, verticalmente en apaisado.
        - y: atraviesa la pantalla verticalmente, horizontalmente en apaisado.
        - z: atraviesa el móvil desde la pantalla hasta la carcasa, es decir, a lo interior.

        Las fuerzas se pueden medir en dos sentidos (suponiendo que el móvil está situado apaisado y situado en un soporte completamente perpendicular al suelo):
        - longitudinal con el eje z.
        - lateralmente con el eje y.
        - combinada: teorema de pitagoras
        Se usara solamente la lateral
         */
        if (se != null)
        {
            val lat = abs(se.values[1].toDouble()) / SensorManager.GRAVITY_EARTH
            binding.gForceText.text = getString(R.string.gForce, lat)
        }

    }

    private fun getRetrofit(): Retrofit
    {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun getTemperature(c: Coord)
    {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call = getRetrofit()
                    .create(OpenWeatherMapAPI::class.java)
                    .oneCallAPI(
                        c.latitude,
                        c.longitude,
                        "a6b8ce9e32c991c71947233b1a6cf54f",
                        "es",
                        "metric"
                    )
                val response = call.body()
                runOnUiThread {
                    if(call.isSuccessful)
                    {
                        val temp: Int? = response?.main?.temp?.toInt()
                        if(temp != null)
                            binding.tempText.text = getString(R.string.temp, temp)
                    }
                    else
                        Toast.makeText(applicationContext, R.string.weather_not_available, Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: Exception)
            {
                runOnUiThread {
                    Toast.makeText(applicationContext, R.string.weather_not_available, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}