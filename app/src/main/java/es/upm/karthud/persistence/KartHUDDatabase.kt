package es.upm.karthud.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Lap::class, Session::class], version = 2, exportSchema = false)
abstract class KartHUDDatabase : RoomDatabase()
{
    abstract fun dao(): IKartHUDDao

    companion object
    {
        @Volatile
        private var INSTANCE: KartHUDDatabase? = null

        fun getDatabase(context: Context): KartHUDDatabase
        {
            val tempInstance = INSTANCE
            if (tempInstance != null)
            {
                return tempInstance
            }
            synchronized(this)
            {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KartHUDDatabase::class.java,
                    "karthud.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}