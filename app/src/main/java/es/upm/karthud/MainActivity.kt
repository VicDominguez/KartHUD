/**
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import es.upm.karthud.databinding.ActivityMainBinding
import es.upm.karthud.weather.APIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.math.roundToInt

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

    private var lastLocation: Location? = null

    private var circuit: Circuit = Circuit(Checkpoint(
        Coord(40.087692916294074, -6.350750671809091),
        Coord(40.08765136226703, -6.350748660152493))) //calle la paz

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
    override fun onLocationChanged(l: Location)
    {
        if (lastLocation != null)
        {
            if(circuit.intersectionInEndLine(Coord(l.latitude, l.longitude)))
            {
                lap++
            }
        }
        lastLocation = l

        speed = ((l.speed * msToKmh * 10.0).roundToInt() / 10.0).toFloat()
        binding.speedText.text = getString(R.string.kmh,speed.toString())
        binding.lapText.text = getString(R.string.lap, lap)
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
            val lat = se.values[1].toDouble() / SensorManager.GRAVITY_EARTH
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
            Log.d("MainActivity","lanzada la corrutina")
            try {
                val call = getRetrofit()
                    .create(APIService::class.java)
                    .oneCallAPI(
                        c.latitude,
                        c.longitude,
                        "a6b8ce9e32c991c71947233b1a6cf54f",
                        "es",
                        "metric"
                    )
                Log.d("MainActivity","hecha la call")
                val response = call.body()
                Log.d("MainActivity","vamos al thread ui")
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