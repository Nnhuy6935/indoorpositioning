package com.example.indoorpositioning.Helputil

import com.example.indoorpositioning.Model.Point2D
import java.math.BigDecimal

class MathSupport {
    fun roundTo4DecimalPlaces(number: Float ): Float{
        return BigDecimal.valueOf(number.toDouble())
            .setScale(4, BigDecimal.ROUND_HALF_EVEN)
            .toFloat()
    }

    fun calculateDistance(point1: Point2D, point2: Point2D): Float{
        var x = Math.pow((point1.getX() - point2.getX()).toDouble(), 2.0f.toDouble())
        var y = Math.pow((point1.getY() - point2.getY()).toDouble(), 2.0f.toDouble())
        return Math.sqrt(x+y).toFloat()
    }
}