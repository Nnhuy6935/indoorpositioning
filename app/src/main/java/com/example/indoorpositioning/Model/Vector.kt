package com.example.indoorpositioning.Model

class Vector(first: Float, second: Float) {
    var x : Float = first
    var y : Float = second
    fun mutiple(v: Vector) : Float{
        return this.x * v.getYValue() - this.y * v.getXValue()
    }
    fun normalizeVector(){
        var size = calculateVectorSize()
        x /= size
        y /= size
    }

    fun calculateVectorSize(): Float{
        return Math.sqrt(Math.pow(x.toDouble(),2.0f.toDouble()) + Math.pow(y.toDouble(), 2.0f.toDouble())).toFloat()
    }
    /**GETTER AND SETTER**/
    fun getXValue(): Float {return this.x}
    fun getYValue(): Float {return this.y}
    fun setXValue(value: Float) {this.x = value}
    fun setyValue(value: Float) {this.y = value}

}