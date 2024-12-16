package com.example.indoorpositioning.Helputil

class ColorHelper {
    fun convertColorToFloatArray(color: Int) : FloatArray{
        val r: Float = ((color shr 16) and 0xFF) / 255.0f
        val g: Float = ((color shr 8) and 0xFF) / 255.0f
        val b: Float = (color and 0xFF) / 255.0f
        val a: Float = ((color shr 24) and 0xFF) / 255.0f
        return floatArrayOf(r, g, b, a)
    }
}