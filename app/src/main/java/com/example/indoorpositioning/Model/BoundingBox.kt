package com.example.indoorpositioning.Model

class BoundingBox {
    private var minX: Float = Float.MAX_VALUE
    private var maxX: Float = Float.MIN_VALUE
    private var minY: Float = Float.MAX_VALUE
    private var maxY: Float = Float.MIN_VALUE


    fun isUnderBoundingBox(x: Float, y: Float): Boolean {
        if (minX <= x && x <= maxX && minY <= y && y <= maxY)
            return true
        else return false
    }

    fun isUnderBoundingBox(point: Point2D): Boolean {
        var X = point.getX()
        var Y = point.getY()
        if (minX <= X && X <= maxX && minY <= Y && Y <= maxY)
            return true
        else return false
    }

    /**GETTER AND SETTER**/
    fun setMinX(value: Float) { minX = value }
    fun setMaxX(value: Float) { maxX = value }
    fun setMaxY(value: Float) { maxY = value }
    fun setMinY(value: Float) { minY = value }
    fun getMinX(): Float { return minX }
    fun getMinY(): Float { return minY }
    fun getMaxX(): Float { return maxX }
    fun getMaxY(): Float { return maxY }
}