/**
 * Clase que obtiene los puntos de intersección con la linea de meta
 * @author Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud.track

import android.location.Location
import java.io.Serializable
import kotlin.math.max
import kotlin.math.min

class Circuit(val name: String, val endLine: Checkpoint): Serializable
{
    //y = mx + a es la formula que vamos a usar
    //m está en longitude/latitude. x es latitude e y in longitud
    private val mEndLine: Double by lazy {computeM(endLine.beacon1, endLine.beacon2)}
    private val aEndLine: Double by lazy {computeA(mEndLine, endLine.beacon1)}

    //usadas para ubicar la interseccion
    private val maxLatitude: Double by lazy { max(endLine.beacon1.latitude, endLine.beacon2.latitude) }
    private val minLatitude: Double by lazy { min(endLine.beacon1.latitude, endLine.beacon2.latitude) }
    private val maxLongitude: Double by lazy { max(endLine.beacon1.longitude, endLine.beacon2.longitude) }
    private val minLongitude: Double by lazy { min(endLine.beacon1.longitude, endLine.beacon2.longitude) }

    /**
     * Calcula la pendiente de la recta
     * @see computeA
     */
    private fun computeM(start: Coord, end: Coord): Double
    {
        return (end.longitude - start.longitude) / (end.latitude - start.latitude)
    }

    /**
     * Calcula el desplazamiento de la recta
     * @see computeM
     */
    private fun computeA(m: Double, start: Coord): Double
    {
        return start.longitude - (m*start.latitude)
    }

    /**
     * Calcula el punto de corte entre la recta de meta y la recta que forman las dos coordenadas recibidas
     * Resuelve por igualación el sistema de y=mx+a y devuelve la intersección
     * @see intersectionInEndLine
     */
    private fun computeIntersection(start: Coord, end: Coord): Coord
    {
        val mLine: Double = computeM(start, end)
        val aLine: Double = computeA(mLine, start)
        val interLatitude: Double = (aLine - aEndLine)/(mEndLine - mLine)
        val interLongitude : Double = (mLine*interLatitude) + aLine
        return Coord(interLatitude, interLongitude)
    }

    /**
     * Comprueba si la intersección está dentro de la recta de meta
     * @see computeIntersection
     */
    private fun intersectionInEndLine(point: Coord): Boolean
    {
        return (point.latitude in minLatitude..maxLatitude) && (point.longitude in minLongitude..maxLongitude)
    }

    /**
     * Hace la proporción de ver donde está el punto de corte dentro de la recta entre las dos
     * coordenadas medidas, en tantos por uno
     * @see timestampLineCrossed
     */
    private fun lineRatio(start: Coord, end: Coord, intersection: Coord): Double
    {
        return (intersection.latitude - start.latitude)/(end.latitude-start.latitude)
    }

    /**
     * Devuelve la marca de tiempo estimada de cuando se ha cruzado la linea de meta,
     * o null si no se ha cruzado
     */
    fun timestampLineCrossed(previousLocation: Location, actualLocation: Location): Long?
    {
        var result: Long? = null
        val previous = Coord(previousLocation.latitude, previousLocation.longitude)
        val actual = Coord(actualLocation.latitude, actualLocation.longitude)
        val intersection: Coord = computeIntersection(previous,actual)
        if (intersectionInEndLine(intersection))
        {
            val ratio: Double = lineRatio(previous, actual, intersection)
            result = (previousLocation.time + ((actualLocation.time - previousLocation.time) * ratio).toLong())
        }
        return result
    }

    override fun toString(): String {
        return name
    }
}