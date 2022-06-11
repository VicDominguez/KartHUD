package es.upm.karthud.persistence

import androidx.room.*
import java.text.SimpleDateFormat
import java.util.*


@Entity(
    tableName = "lap",
    indices = [Index("session")], //para evitar column references a foreign key but it is not part of an index
    foreignKeys = 
    [
        ForeignKey(
            entity = Session::class,
            parentColumns = arrayOf("idSession"),
            childColumns = arrayOf("session"),
            onDelete = ForeignKey.CASCADE)
    ])
data class Lap(val time: Long, val startTimestamp: Long, val session: Long)
{
    @PrimaryKey(autoGenerate = true)
    var idLap: Long = 0

    companion object
    {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
    }

    override fun toString(): String
    {
        fun longToStringTime(l: Long): String
        {
            fun Long.format(digits: Int) = "%0${digits}d".format(this)

            val miliSeconds = l % 1000
            val seconds = (l / 1000) % 60
            val minutes = (l / 1000) / 60
            return "$minutes:${seconds.format(2)}:${miliSeconds.format(3)}"
        }
        return "Tiempo: ${longToStringTime(time)}, fecha: ${sdf.format(startTimestamp)}"
    }
}

@Entity(tableName = "session")
data class Session(val track: String, val startTimestamp: Long)
{
    @PrimaryKey(autoGenerate = true)
    var idSession: Long = 0
}

data class SessionWithLaps(
    @Embedded val session: Session,
    @Relation(parentColumn = "idSession", entityColumn = "session")
    val laps: List<Lap>)
{
    companion object
    {
        val dayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ROOT)
        val hourFormat = SimpleDateFormat("HH:mm:ss", Locale.ROOT)
    }
    override fun toString(): String
    {
        val stringBuilder  = StringBuilder()
        stringBuilder.append("Sesión realiza en ${session.track} el dia " +
                "${dayFormat.format(session.startTimestamp)} a las " +
                "${hourFormat.format(session.startTimestamp)}.\n"
        )
        laps.forEachIndexed{ index, element -> stringBuilder.append("Vuelta ${index+1}: $element \n") }

        return stringBuilder.toString()
    }
}