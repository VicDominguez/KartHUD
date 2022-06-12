package es.upm.karthud.persistence

import androidx.room.*

@Dao
interface IKartHUDDao {
    @Transaction //porque nos afecta a dos tablas con @Relation
    @Query("SELECT * FROM session WHERE session.userId = :userId ORDER BY session.startTimestamp DESC")
    fun getSessionWithLaps(userId: String) : MutableList<SessionWithLaps>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSession(session: Session) : Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLap(lap: Lap) : Long

    @Query("DELETE FROM session WHERE idSession = :id")
    fun deleteSessionById(id : Long)
}