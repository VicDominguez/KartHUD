/**
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud.activities

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
import es.upm.karthud.*
import es.upm.karthud.databinding.ActivityHudBinding
import es.upm.karthud.persistence.IKartHUDDao
import es.upm.karthud.persistence.Lap
import es.upm.karthud.persistence.Session
import es.upm.karthud.track.Checkpoint
import es.upm.karthud.track.Circuit
import es.upm.karthud.track.Coord
import es.upm.karthud.weather.WeatherManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import kotlin.concurrent.schedule
import kotlin.math.abs
import kotlin.math.roundToInt


class HUDActivity : AppCompatActivity(), LocationListener, SensorEventListener, EasyPermissions.PermissionCallbacks
{
    /*
    ---------------------------------------------------------
        Variables
    ---------------------------------------------------------
     */

    //textviews con binding: https://cursokotlin.com/capitulo-29-view-binding-en-kotlin/
    private lateinit var binding: ActivityHudBinding

    //objetos manager
    private val locationManager : LocationManager by lazy { getSystemService(LOCATION_SERVICE) as LocationManager }
    private val sensorManager: SensorManager by lazy {getSystemService(SENSOR_SERVICE) as SensorManager}

    private val accelerometer : Sensor? by lazy {sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)}

    private val timer: Timer by lazy { Timer("Weather",false) }

    private var speed: Float = 0.0f

    private var timestampStartLap : Long = 0
    private var timestampEndLap : Long? = null

    private var lap: Int = 0
    private var timeBestLap : Long = Long.MAX_VALUE
    private var timeLastLap : Long = Long.MAX_VALUE

    private var lastLocation: Location? = null

    private lateinit var circuit: Circuit

    private val dao : IKartHUDDao by lazy { InitApp.database.dao() }
    private var sessionId : Long? = null

    /*
    ---------------------------------------------------------
        Estados de la actividad
    ---------------------------------------------------------
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityHudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        circuit = intent.extras?.get("circuit") as Circuit
        binding.circuitText.text = circuit.name

        binding.closeHudButton.setOnClickListener { finish() }
    }

    override fun onStart()
    {
        super.onStart()

        insertNewSession()

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
        removeSessionEmpty()
        stopAccelerometer()
        stopGPS()
        stopWeather()
    }

    /*
    ---------------------------------------------------------
        Callbacks de manejo de permisos
    ---------------------------------------------------------
     */
    /**
     * Pedimos los permisos con EasyPermissions
     * @see onPermissionsGranted
     * @see onPermissionsDenied
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    /**
     * Si tenemos los permisos arrancamos el gps
     * @see onRequestPermissionsResult
     * @see onPermissionsDenied
     */
    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>)
    {
        Toast.makeText(this, R.string.gps_granted, Toast.LENGTH_SHORT).show()
        startGPSUnchecked()
    }

    /**
     * Si no tenemos los permisos, dejamos algunos textos por defecto. Si los permisos están
     * permanentemente denegados, avisamos al usuario para que se lo piense
     * @see onRequestPermissionsResult
     * @see onPermissionsGranted
     */
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
            EasyPermissions.requestPermissions(this, getString(R.string.gps_not_granted),
                gpsPermissionRequest, gpsPermission
            )
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

    /**
     * Para las actualizaciones del gps
     * @see checkAndStartGPS
     */
    private fun stopGPS()
    {
        locationManager.removeUpdates(this)
    }

    /**
     * Arranca el acelerometro si lo hay, y si no notifica que no lo hay
     * @see stopAccelerometer
     */
    private fun startAccelerometer()
    {
        if(accelerometer == null)
        {
            Toast.makeText(this, R.string.accelerometer_not_available, Toast.LENGTH_LONG).show()
            binding.gForceText.visibility = View.INVISIBLE
        }
        else
        {
            sensorManager.registerListener(this, accelerometer, periodAccelerometer)
        }
    }

    /**
     * Para el acelerometro si lo hay
     * @see startAccelerometer
     */
    private fun stopAccelerometer()
    {
        accelerometer.let {
            sensorManager.unregisterListener(this, it)
        }
    }

    /**
     * Arranca el timer para hacer consultas de la previsión del tiempo
     * @see stopWeather
     */
    private fun startWeather()
    {
        timer.schedule(initialDelayWeather, periodWeather){
            updateTemperature(circuit.endLine.beacon1)
        }
    }

    /**
     * Para el timer para que no se hagan más consultas a la api del tiempo
     * @see startWeather
     */
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
        //cuando empieza la app
        if (lastLocation == null)
        {
            timestampStartLap = l.time
        }
        //si tenemos dos puntos vemos si hay corte
        lastLocation?.let { itLocation ->
            timestampEndLap = circuit.timestampLineCrossed(itLocation, l)
            // Si hay timestamp es que hemos cruzado meta
            timestampEndLap?.let { itLong ->
                if (lap > 0)
                {
                    timeLastLap = itLong-timestampStartLap
                    
                    insertNewLap(timeLastLap, timestampStartLap)

                    if(timeLastLap < timeBestLap)
                        timeBestLap = timeLastLap
                    
                    updateUILaps()

                }
                lap++
                timestampStartLap = itLong
                //faltaria cuadrar bien los ms pero con esto reiniciamos el cronometro
                binding.chronometer.base = SystemClock.elapsedRealtime()
                }
        }

        //guaardamos la anterior localizacion y pasamos la velocidad a km por hora truncado a un decimal
        lastLocation = l
        speed = ((l.speed * msToKmh * 10.0).roundToInt() / 10.0).toFloat()

        //mostramos la vuelta y la velocidad
        binding.lapText.text = getString(R.string.lap, lap)
        binding.speedText.text = getString(R.string.kmh,speed.toString())
    }

    /**
     * Muestra la mejor vuelta y la última en la pantalla
     * @see onLocationChanged
     */
    private fun updateUILaps()
    {
        fun longToStringTime(l: Long): String
        {
            fun Long.format(digits: Int) = "%0${digits}d".format(this)

            val miliSeconds = l % 1000
            val seconds = (l / 1000) % 60
            val minutes = (l / 1000) / 60
            return "$minutes:${seconds.format(2)}:${miliSeconds.format(3)}"
        }

        binding.timeBestLapText.visibility = View.VISIBLE
        binding.timeLastLapText.visibility = View.VISIBLE

        binding.timeBestLapText.text = getString(R.string.best_lap_time, longToStringTime(timeBestLap))
        binding.timeLastLapText.text = getString(R.string.last_lap_time, longToStringTime(timeLastLap))
    }

    /*
    ---------------------------------------------------------
        Callback de SensorEventListener
    ---------------------------------------------------------
     */

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    /**
     * Calculamos las fuerzas G y las mostramos
     */
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
        se?.let { event ->
            val lat = abs(event.values[1].toDouble()) / SensorManager.GRAVITY_EARTH
            binding.gForceText.text = getString(R.string.gForce, lat)
        }

    }

    /*
   ---------------------------------------------------------
       Actualización de la temperatura
   ---------------------------------------------------------
    */
    /**
     * Actualiza la temperatura con una corrutina para llamar a la API en otro hilo y lo muestra
     * @see WeatherManager.getTemperature
     */
    private fun updateTemperature(c: Coord)
    {
        CoroutineScope(Dispatchers.IO).launch {
            try
            {
                val temp: Int? = WeatherManager.getTemperature(c)
                runOnUiThread {
                    if(temp != null)
                        binding.tempText.text = getString(R.string.temp, temp)
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

    /*
   ---------------------------------------------------------
       Actualización de base de datos
   ---------------------------------------------------------
    */
    
    private fun insertNewSession()
    {
        CoroutineScope(Dispatchers.IO).launch {
            sessionId = dao.insertSession(Session(circuit.name, System.currentTimeMillis()))
        }
    }

    private fun insertNewLap(time: Long, timestampStartLap: Long)
    {
        sessionId?.let { id ->
            CoroutineScope(Dispatchers.IO).launch {
                dao.insertLap(Lap(time,timestampStartLap, id))
            }
        }
    }

    private fun removeSessionEmpty()
    {
        if(this.lap < 2)
        {
            CoroutineScope(Dispatchers.IO).launch {
                dao.deleteSessionById(sessionId!!)
            }
        }
    }
}