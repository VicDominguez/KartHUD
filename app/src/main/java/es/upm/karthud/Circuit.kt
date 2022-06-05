package es.upm.karthud

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class Circuit(val endLine: Checkpoint)
{
    //m is longitude/latitude. x is in latitude and y in longitude
    private val mEndLine: Double by lazy {computeM(endLine.beacon1, endLine.beacon2)}
    private val aEndLine: Double by lazy {computeA(mEndLine, endLine.beacon1)}

    //only used for intersectionInEndLine
    private val maxLatitude: Double by lazy { max(endLine.beacon1.latitude, endLine.beacon2.latitude) }
    private val minLatitude: Double by lazy { min(endLine.beacon1.latitude, endLine.beacon2.latitude) }
    private val maxLongitude: Double by lazy { max(endLine.beacon1.latitude, endLine.beacon2.latitude) }
    private val minLongitude: Double by lazy { min(endLine.beacon1.latitude, endLine.beacon2.latitude) }


    private fun computeM(startCoord: Coord, endCoord: Coord): Double
    {
        return (endCoord.longitude - startCoord.longitude) / (endCoord.latitude - startCoord.latitude)
    }

    private fun computeA(m: Double, startCoord: Coord): Double
    {
        return startCoord.longitude - (m*startCoord.latitude)
    }

    private fun computeIntersection(startCoord: Coord, endCoord: Coord): Coord
    {
        val mLine: Double = computeM(startCoord, endCoord)
        val aLine: Double = computeA(mLine, startCoord)
        val interLatitude: Double = (aLine - aEndLine)/(mLine - mEndLine)
        val interLongitude : Double = (mLine*interLatitude) + aLine
        return Coord(interLatitude, interLongitude)
    }

    fun intersectionInEndLine(point: Coord): Boolean
    {
        return (point.latitude in minLatitude..maxLatitude) && (point.longitude in minLongitude..maxLongitude)
    }

    fun computeMilisecondLap(startCoord: Coord, endCoord: Coord): Double
    {
        val intersection: Coord = computeIntersection(startCoord, endCoord)
        return (intersection.latitude - startCoord.latitude)/(endCoord.latitude-startCoord.latitude)
    }

    fun computeMilisecondTimestamp(startCoord: Coord, endCoord: Coord): Int
    {
        return (computeMilisecondLap(startCoord, endCoord) * 1000.0).roundToInt()
    }
}