package com.example.indoorpositioning.Model

import android.graphics.Color
import android.util.Log
import com.example.indoorpositioning.Helputil.ColorHelper
import com.example.indoorpositioning.canvas.BaseKnowledge


class Object(type: ObjectType, points: MutableList<Point2D>, id: String) {
    private var lineWidth = 0.01f
    private var type : ObjectType = type
    private var vertices : MutableList<Point2D> = points
    private var boundingBox : BoundingBox = BoundingBox()
    private var id : String = id
    private var borderColor: Int = Color.BLACK
    private var fillColor: Int = Color.BLACK

    init {
        if(type == ObjectType.POINT){
            var point = vertices[0]
            boundingBox.setMaxX(point.getX()+0.01f)
            boundingBox.setMaxY(point.getY()+0.01f)
            boundingBox.setMinX(point.getX()-0.01f)
            boundingBox.setMinY(point.getY()-0.01f)
        }
    }
    fun isClickOnObject(x: Float, y: Float): Boolean{
        if(type == ObjectType.POINT){
            setBoundingBox()
            return  boundingBox.isUnderBoundingBox(x,y)
        }else if(type == ObjectType.LINE){
            return checkPointIsInLine(x,y)
        }else if(type == ObjectType.CURVE){
            return isClickOnCurver(x,y)
        }else{
            return checkRayCasting(x,y)
        }
    }

    /** REFERENCES: RAY CASTING ALGORITHM
    * https://rosettacode.org/wiki/Ray-casting_algorithm#Kotlin**/
    //this function is for polygon/concave_polygon, triangle object and mix object
    fun checkRayCasting(x: Float, y:Float) : Boolean{
        if(type == ObjectType.POLYGON || type == ObjectType.TRIANGLE || type == ObjectType.CONCAVE_POLYGON || type == ObjectType.MIX){
            var count = 0
            var numberOfVertices = vertices.size
            var i = 0
            while( i < numberOfVertices){
                var startPoint = vertices.get(i)
                var endPoint = vertices.get((i + 1) % numberOfVertices)
                //kiểm tra điểm có nằm trong phạm vi xét không
                if( x <= Math.max(startPoint.getX(),endPoint.getX())
                    && y >= Math.min(startPoint.getY(),endPoint.getY()) && y <= Math.max(startPoint.getY(),endPoint.getY())){
                    var ratio = Math.abs(startPoint.getX()-endPoint.getX()) / Math.abs(startPoint.getY() - endPoint.getY())
                    var intersectX = 0.0f
                    if(startPoint.getX() > endPoint.getX()) {
                        intersectX += endPoint.getX() + ratio * Math.abs(y-endPoint.getY())
                    }
                    else {
                        intersectX += startPoint.getX() + ratio*Math.abs(y-startPoint.getY())
                    }
                    if(intersectX >= x)
                        ++count
                }
                ++i
            }
            if(count % 2 == 1){
                return true
            }else return false
        }
        return false
    }
    /**Check if a point is under bounding of a line
     * algorithm: calculate the distance from that point to line and compare it with width value of line
     *  if distance < width => point is under the bounding of line
     *  else => point is not under the bounding of line
     *  **/
    fun checkPointIsInLine(x: Float, y: Float): Boolean{
        var start = vertices[0]
        var end = vertices[1]
        /**calculate the distance from a point to a line**/
        var distance = Math.abs(
            (x-start.getX())*(end.getY()-start.getY())
                    - (y - start.getY())*(end.getX()-start.getX())
        ) / Math.sqrt(Math.pow(end.getX().toDouble()-start.getX(),2.0) + Math.pow(end.getY() - start.getY().toDouble(),2.0))

        if(Math.abs(distance) <= lineWidth
            && x <= Math.max(start.getX(), end.getX())
            && x >= Math.min(start.getX(), end.getX())
            )
            return true
        else return false
    }

    /**Check if a point is under bounding of a curve
     * algorithm: using Bezier Algorithm to draw a curve
     * and then check if point could belong any point in Bezier curve was drawn
     * **/
    fun isClickOnCurver(x: Float, y : Float) : Boolean{
        var controlPoints = BaseKnowledge().defineControlPoints(vertices)
        var j = 0
        var size = controlPoints.size
        while(j < size - 3){
            val c_numPoinst = 100f
            var i = 0
            while( i < c_numPoinst){
                var t = i.toFloat() / (c_numPoinst - 1)
                var temp: Vector = BaseKnowledge().BezierAlgorithm(controlPoints.subList(j,j+4),t)
                var tempPoint = Point2D(temp.getXValue(), temp.getYValue(),"","")
                if(tempPoint.isClickOnPoint(x,y))
                    return true
                ++i
            }
            ++j
        }
        return false
    }

    /**GETTER AND SETTER**/
    fun getType(): ObjectType {return type}
    fun setType(type: ObjectType) {this.type = type}
    fun getVertices() : List<Point2D> {return vertices}
    fun setPointVertices(points : MutableList<Point2D>){
        vertices.clear()
        vertices = points
    }
    fun getBoundingBox() : BoundingBox {return boundingBox}
    open fun setBoundingBox(){
        if(type == ObjectType.POINT){
            var point = vertices[0]
            boundingBox.setMaxX(point.getX()+0.01f)
            boundingBox.setMaxY(point.getY()+0.01f)
            boundingBox.setMinX(point.getX()-0.01f)
            boundingBox.setMinY(point.getY()-0.01f)
        }
        if(type == ObjectType.LINE){
            var startPoint = vertices[0]
            var endPoint = vertices[1]
            var range : Float = Math.abs(startPoint.getY() - endPoint.getY())

            if(range > 0.3f){
                boundingBox.setMaxX(Math.max(startPoint.getX() + lineWidth, endPoint.getX()+lineWidth))
                boundingBox.setMaxY(Math.max(startPoint.getY(),endPoint.getY()))
                boundingBox.setMinX(Math.min(startPoint.getX(),endPoint.getX()))
                boundingBox.setMinY(Math.min(startPoint.getY(), endPoint.getY()))
            }else{
                boundingBox.setMaxY(Math.max(startPoint.getY() + lineWidth, endPoint.getY() + lineWidth))
                boundingBox.setMaxX(Math.max(startPoint.getX(), endPoint.getX()))
                boundingBox.setMinX(Math.min(startPoint.getX(), endPoint.getX()))
                boundingBox.setMinY(Math.min(startPoint.getY(), endPoint.getY()))
            }
        }else if(type == ObjectType.POLYGON){
            var maxX = Float.MIN_VALUE
            var minX = Float.MAX_VALUE
            var maxY = Float.MIN_VALUE
            var minY = Float.MAX_VALUE
            for( ver in vertices){
                if(ver.getX() > maxX) maxX = ver.getX()
                if(ver.getY() > maxY) maxY = ver.getY()
                if(ver.getX() < minX) minX = ver.getX()
                if(ver.getY() < minY) minY = ver.getY()
            }
            boundingBox.setMaxY(maxY)
            boundingBox.setMaxX(maxX)
            boundingBox.setMinX(minX)
            boundingBox.setMinY(minY)
        }
    }
    fun  getId(): String{ return this.id}
    fun getBorderColorWithInt() : Int{return this.borderColor}
    fun setBorderColorWithInt(color: Int) {this.borderColor = color}
    fun getFillColorWithInt() : Int{return this.fillColor}
    fun setFillColorWithInt(color: Int) {this.fillColor = color}
    fun getBorderColorWithFloatArray() : FloatArray { return ColorHelper().convertColorToFloatArray(this.borderColor)}
    fun getFillColorWithFloatArray() : FloatArray { return ColorHelper().convertColorToFloatArray(this.fillColor)}
}


/**For CONCAVE_POLYGON, the points will be connected in vertices order to create a polygonal line**/
enum class ObjectType{
    NULL,POINT, LINE, TRIANGLE, POLYGON, CONCAVE_POLYGON, CURVE, MIX
}

