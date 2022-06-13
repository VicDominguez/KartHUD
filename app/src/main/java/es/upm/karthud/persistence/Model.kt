/**
 * Contiene las clases almacenadas en base de datos
 * @author: Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud.persistence

import androidx.room.*
import es.upm.karthud.Utils.formatoDMA
import es.upm.karthud.Utils.formatoHMS
import es.upm.karthud.Utils.longToStringTime


/**
 * Clase que guarda el tiempo de vuelta, cuándo se inició la vuelta y la sesión a la que pertenece
 */
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

    /**
     * Mostramos el tiempo y la fecha formateada a hora, minuto y segundo
     */
    override fun toString(): String = "Tiempo: ${longToStringTime(time)}," +
            " fecha: ${formatoHMS.format(startTimestamp)}"

    /**
     * Devolvemos los campos del objeto en un mapa, ideal para subir datos a base de datos remotas
     */
    fun fields2Map(includeId: Boolean = false, includeFk: Boolean = false) : Map<String,String>
    {
        val map: HashMap<String,String> = HashMap()
        map["time"] = time.toString()
        map["startTimestamp"] = startTimestamp.toString()
        if (includeFk)
            map["session"] = session.toString()
        if (includeId)
            map["idLap"] = idLap.toString()
        return map
    }
}

/**
 * Clase que contiene los datos de la tanda en cuestión: Nombre del circuito, fecha de inicio y usuario que la ha realizado
 */
@Entity(tableName = "session")
data class Session(val track: String, val startTimestamp: Long, val userId: String)
{
    @PrimaryKey(autoGenerate = true)
    var idSession: Long = 0

    /**
     * Mostramos los campos del objeto a formato mapa, útil para subir datos a base de datos remotas
     */
    fun fields2Map(includeId: Boolean = false, includeUserId: Boolean = false) : Map<String,String>
    {
        val map: HashMap<String,String> = HashMap()
        map["track"] = track
        map["startTimestamp"] = startTimestamp.toString()
        if (includeId)
            map["idSession"] = idSession.toString()
        if (includeUserId)
            map["userId"] = userId
        return map
    }
}

/**
 * Clase que contiene la sesión con sus vueltas correspondientes.
 * @see Session
 * @see Lap
 */
data class SessionWithLaps(
    @Embedded val session: Session,
    @Relation(parentColumn = "idSession", entityColumn = "session")
    val laps: List<Lap>)
{
   /**
    * Devolvemos infomación sobre cuándo se ha hecho la tanda y las vueltas realizadas
    */
    override fun toString(): String
    {
        val stringBuilder  = StringBuilder()
        stringBuilder.append("Sesión realizada en ${session.track} el dia " +
                "${formatoDMA.format(session.startTimestamp)} a las " +
                "${formatoHMS.format(session.startTimestamp)}.\n"
        )
        laps.forEachIndexed{ index, element -> stringBuilder.append("Vuelta ${index+1}: $element \n") }

        return stringBuilder.toString()
    }
}