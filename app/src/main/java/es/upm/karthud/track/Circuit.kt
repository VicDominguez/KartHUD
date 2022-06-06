package es.upm.karthud.track

import android.location.Location
import kotlin.math.max
import kotlin.math.min

class Circuit(val endLine: Checkpoint)
{
    //m is longitude/latitude. x is in latitude and y in longitude
    private val mEndLine: Double by lazy {computeM(endLine.beacon1, endLine.beacon2)}
    private val aEndLine: Double by lazy {computeA(mEndLine, endLine.beacon1)}

    //only used for intersectionInEndLine
    private val maxLatitude: Double by lazy { max(endLine.beacon1.latitude, endLine.beacon2.latitude) }
    private val minLatitude: Double by lazy { min(endLine.beacon1.latitude, endLine.beacon2.latitude) }
    private val maxLongitude: Double by lazy { max(endLine.beacon1.longitude, endLine.beacon2.longitude) }
    private val minLongitude: Double by lazy { min(endLine.beacon1.longitude, endLine.beacon2.longitude) }


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
        val interLatitude: Double = (aLine - aEndLine)/(mEndLine - mLine)
        val interLongitude : Double = (mLine*interLatitude) + aLine
        return Coord(interLatitude, interLongitude)
    }

    private fun intersectionInEndLine(point: Coord): Boolean
    {
        return (point.latitude in minLatitude..maxLatitude) && (point.longitude in minLongitude..maxLongitude)
    }

    private fun lineRatio(startCoord: Coord, endCoord: Coord, intersection: Coord): Double
    {
        return (intersection.latitude - startCoord.latitude)/(endCoord.latitude-startCoord.latitude)
    }

    fun timeStampLineCrossed(previousLocation: Location, actualLocation: Location): Long?
    {
        var result: Long? = null
        val previousCoord = Coord(previousLocation.latitude, previousLocation.longitude)
        val actualCoord = Coord(actualLocation.latitude, actualLocation.longitude)
        val intersection: Coord = computeIntersection(previousCoord,actualCoord)
        if (intersectionInEndLine(intersection))
        {
            val ratio: Double = lineRatio(previousCoord, actualCoord, intersection)
            result = (previousLocation.time + ((actualLocation.time - previousLocation.time) * ratio).toLong())
        }
        return result
    }
}