package es.upm.karthud.activities

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

import es.upm.karthud.persistence.KartHUDDatabase

class InitApp: Application()
{
    companion object{
        lateinit var localDb: KartHUDDatabase
        lateinit var remoteDbReference: DatabaseReference
        lateinit var remoteAuthInstance: FirebaseAuth
    }

    override fun onCreate() {
        super.onCreate()
        localDb = KartHUDDatabase.getDatabase(applicationContext)
        remoteDbReference = FirebaseDatabase.getInstance().reference
        remoteAuthInstance = FirebaseAuth.getInstance()
    }
}