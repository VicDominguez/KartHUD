package es.upm.karthud

import android.location.Location
import android.location.LocationListener
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), LocationListener
{
    private lateinit var speedText: TextView
    private lateinit var latText: TextView
    private lateinit var longText: TextView

    private lateinit var locationManager: LocationManager

    //estados de la actividad
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        speedText = findViewById(R.id.speedText)
        latText = findViewById(R.id.latText)
        longText = findViewById(R.id.longText)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkAppPermissions()
        executeHUD()

    }


    override fun onPause()
    {
        super.onPause()
        locationManager.removeUpdates(this)
    }

    override fun onResume()
    {
        super.onResume()
        executeHUD()
    }

    override fun onDestroy()
    {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }

    private fun checkAppPermissions()
    {
        //This if is for SDK >=23, but our target is SDK 28
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(Array<String>(1){Manifest.permission.ACCESS_FINE_LOCATION},1000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                executeHUD()
            else
                finish()
        }
    }


    private fun executeHUD()
    {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 250,0.0f, this)
        Toast.makeText(this, "Waiting for GPS connection", Toast.LENGTH_SHORT).show()
    }

    override fun onLocationChanged(l: Location) {
        val speed = l.speed * 3.6f
        speedText.text = getString(R.string.kmh,speed.toString())
        latText.text = getString(R.string.lat, l.latitude.toString())
        longText.text = getString(R.string.longitude, l.longitude.toString())
        Log.d("MainActivity", "Time: " + l.time)
        Log.d("MainActivity", "Raw speed: " +l.speed)
    }


}