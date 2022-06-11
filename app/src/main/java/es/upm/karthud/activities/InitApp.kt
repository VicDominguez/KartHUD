package es.upm.karthud.activities

import android.app.Application

import es.upm.karthud.persistence.KartHUDDatabase

class InitApp: Application()
{
    companion object{
        lateinit var database: KartHUDDatabase
    }

    override fun onCreate() {
        super.onCreate()
        database = KartHUDDatabase.getDatabase(applicationContext)
    }
}