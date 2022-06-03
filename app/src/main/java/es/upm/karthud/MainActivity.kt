package es.upm.karthud

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.HandlerThread
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, EasyPermissions.RationaleCallbacks
{
    //variables
    private val TAG = "MainActivity"
    private val LOCATION_PERM = 124

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var handlerThread: HandlerThread

    private lateinit var speedText : TextView
    private lateinit var latText : TextView
    private lateinit var longText : TextView

    //https://www.develou.com/propiedades-observables-en-kotlin/
    private var isDone: Boolean by Delegates.observable(false) { _, _, newValue ->
        if (newValue)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    //estados de la actividad
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speedText = findViewById(R.id.speedText)
        latText = findViewById(R.id.latText)
        longText = findViewById(R.id.longText)

        handlerThread = HandlerThread("GPS thread",Process.THREAD_PRIORITY_MORE_FAVORABLE)
        handlerThread.start()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        askForLocationPermission()
        createLocationRequest()

        locationCallback = object : LocationCallback()
        {
            override fun onLocationResult(locationResult: LocationResult)
            {
                Log.d(TAG, "Size: " + locationResult.locations.size.toString())
                for (l in locationResult.locations)
                {
                   Log.d(TAG, "Time: " + l.time)
                }
                /*runOnUiThread {
                    speedText.text = getString(R.string.kmh,locationResult.lastLocation.speed.toString())
                    latText.text = getString(R.string.lat,locationResult.lastLocation.latitude.toString())
                    longText.text = getString(R.string.longitude,locationResult.lastLocation.longitude.toString())
                }*/

            }
        }

    }

    //34.06

    override fun onPause()
    {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume()
    {
        super.onResume()
        startLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread.quitSafely()
    }

    //funciones de localizacion
    private fun startLocationUpdates()
    {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG,"pedir permisos")
            return
        } //TODO: ActivityCompat.requestPermissions
        else
            // para quitar el Looper.getMainLooper que va a pedales
            // https://nyamebismark12-nb.medium.com/understanding-loopers-in-android-2564244a159d
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, handlerThread.looper)
    }


    private fun createLocationRequest()
    {
        Log.d(TAG,"createLocationRequest")
        locationRequest = LocationRequest.create().apply {
            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = TimeUnit.MILLISECONDS.toMillis(100)

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = TimeUnit.MILLISECONDS.toMillis(50)

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = TimeUnit.MILLISECONDS.toMillis(250)

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun askForLocationPermission()
    {
        if (hasLocationPermissions())
        {
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            {
                Log.d(TAG,"no hay permisos")
                return
            } //TODO: ActivityCompat.requestPermissions
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            Toast.makeText(this,"Permisos OK", Toast.LENGTH_LONG).show()
            }
        }
        else
        {
            Log.d(TAG, "Nos faltan permisos")
            EasyPermissions.requestPermissions(this, "need permissions to find your location and calculate the speed",
                LOCATION_PERM, Manifest.permission.ACCESS_FINE_LOCATION)
        }

    }

    private fun hasLocationPermissions(): Boolean
    {
        Log.e(TAG, "EasyPermissions.hasPermissions: " + EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION))
        return EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    //funciones de los permisos

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>)
    {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms))
            AppSettingsDialog.Builder(this).build().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE)
        {
            val yes= "Allow"
            val no="Deny"
            Toast.makeText(this,"onAcivityResult", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>)
    {
        Log.d(TAG, "onPermissionsGranted:$requestCode, $perms")
    }

    override fun onRationaleAccepted(requestCode: Int)
    {
        Log.d(TAG, "onRationaleAccepted:$requestCode")
    }

    override fun onRationaleDenied(requestCode: Int)
    {
        Log.d(TAG, "onRationaleDenied:$requestCode")
    }
}