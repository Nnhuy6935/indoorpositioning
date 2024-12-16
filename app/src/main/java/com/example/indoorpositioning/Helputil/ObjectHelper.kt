package com.example.indoorpositioning.Helputil

import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.Model.Vector
import com.example.indoorpositioning.canvas.BaseKnowledge

class ObjectHelper {
    fun isClickOnCurve(vertices: MutableList<Point2D>, point: Point2D) : Boolean{
        var controlPoints = BaseKnowledge().defineControlPoints(vertices)
        var j = 0
        var size = controlPoints.size
        while(j < size - 3){
            val c_numPoinst = 100f
            var i = 0
            while( i < c_numPoinst){
                var t = i.toFloat() / (c_numPoinst - 1)
                var temp: Vector = BaseKnowledge().BezierAlgorithm(controlPoints.subList(j,j+4),t)
                var tempPoint: Point2D = Point2D(temp.getXValue(), temp.getYValue(),"","")
                if(tempPoint.isClickOnPoint(point.getX(),point.getY()))
                    return true
                ++i
            }
            ++j
        }
        return false
    }
}