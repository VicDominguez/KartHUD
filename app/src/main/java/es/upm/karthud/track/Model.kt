/**
 * Data class para la funcionalidad de los circuitos
 * @author Victor Manuel Dominguez Rivas y Juan Luis Moreno Sancho
 */

package es.upm.karthud.track

import java.io.Serializable

data class Coord(val latitude: Double, val longitude: Double): Serializable
data class Checkpoint(val beacon1: Coord, val beacon2: Coord): Serializable
