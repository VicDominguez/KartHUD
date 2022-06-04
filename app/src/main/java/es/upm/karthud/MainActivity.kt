/**
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), LocationListener, EasyPermissions.PermissionCallbacks
{
    //gestion de permisos
    private val gpsPermission: String = Manifest.permission.ACCESS_FINE_LOCATION
    private val gpsPermissionRequest : Int = 123

    //constantes
    private val minTimeMs: Long = 250
    private val minDistance : Float = 0.0f
    private val msToKmh : Double = 3.6

    //textviews
    private val speedText: TextView by lazy { findViewById(R.id.speedText) }
    private val latText: TextView by lazy { findViewById(R.id.latText) }
    private val longText: TextView by lazy { findViewById(R.id.longText) }

    //objetos manager
    private val locationManager : LocationManager by lazy { getSystemService(Context.LOCATION_SERVICE) as LocationManager }

    private var speed: Float = 0.0f

    /*
    ---------------------------------------------------------
        Estados de la actividad
    ---------------------------------------------------------
     */
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkAndStartGPS()
    }

    override fun onPause()
    {
        super.onPause()
        stopGPS()
    }

    override fun onResume()
    {
        super.onResume()
        checkAndStartGPS()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        stopGPS()
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
        speedText.text = getString(R.string.kmh,"?")
        latText.text = getString(R.string.lat, "?")
        longText.text = getString(R.string.longitude, "?")
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

    /*
    ---------------------------------------------------------
        Callback de LocationListener
    ---------------------------------------------------------
     */
    override fun onLocationChanged(l: Location)
    {
        speed = ((l.speed * msToKmh * 10.0).roundToInt() / 10.0).toFloat()
        speedText.text = getString(R.string.kmh,speed.toString())
        latText.text = getString(R.string.lat, l.latitude.toString())
        longText.text = getString(R.string.longitude, l.longitude.toString())
    }

}