package com.example.indoorpositioning.canvas

import android.util.Log
import com.example.indoorpositioning.Helputil.MathSupport
import com.example.indoorpositioning.Helputil.StringHelper
import com.example.indoorpositioning.Model.Object
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.Model.Vector

class BaseKnowledge {
    /**check if a triangle(p1,p2,p3) is an ear **/
    fun isEar(p1: Point2D, p2: Point2D, p3: Point2D, vertices: List<Point2D>): Boolean {
        // Kiểm tra xem có đỉnh nào khác nằm trong tam giác (p1, p2, p3) hay không
        for (point in vertices) {
            if (point != p1 && point != p2 && point != p3 && isPointInTriangle(point, p1, p2, p3)) {
                return false
            }
        }
        return true
    }
    /*** check if a point(pt) lies in a triangle(p1,p2,p3)*/
    fun isPointInTriangle(pt: Point2D, p1: Point2D, p2: Point2D, p3: Point2D): Boolean {
        // Sử dụng phương pháp tích có hướng để kiểm tra
        val b1 = crossProduct2(p1, p2, pt) < 0.0
        val b2 = crossProduct2(p2, p3, pt) < 0.0
        val b3 = crossProduct2(p3, p1, pt) < 0.0
        return (b1 == b2) && (b2 == b3)
    }

    // Hàm tính tích có hướng
    fun crossProduct(p1: Point2D, p2: Point2D, p3: Point2D): Float {
        return (p2.getX() - p1.getX()) * (p3.getY() - p1.getY()) - (p2.getY() - p1.getY()) * (p3.getX() - p1.getX())
    }

    fun crossProduct2(p1: Point2D, p2: Point2D, p3: Point2D): Float {
        return (p2.getX() - p1.getX()) * (p3.getY() - p2.getY()) - (p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
    }
    fun calculateCrossProduct(p1: Point2D, p2: Point2D, p3: Point2D) : Float{
        return (p2.getX() - p1.getX()) * (p3.getY() - p2.getY()) - (p2.getY() - p1.getY()) * (p3.getX() - p2.getX())
    }

    /**check if a polygon with vertices is a complex polygon**/
    fun checkIsComplexPolygon(vertices: MutableList<Point2D>): Boolean{
        var i = 0
        while( i < vertices.size){
            var pos = findLastPointWithId(vertices[i].getId(), vertices)
            if(pos != -1 && pos != i)
                return true
            else ++i
        }
        return false
    }
    /**find the last position of point with "id" in list "points" **/
    fun findLastPointWithId(id: String, points: MutableList<Point2D>): Int{
        var i = points.size - 1
        while(i >= 0) {
            if (points[i].getId() == id)
                return i
            --i
        }
        return -1;
    }
    /**REFERENCE: EAR CLIPPING ALGORITHM
     * https://swaminathanj.github.io/cg/PolygonTriangulation.html **/
    fun defineOrderDraw(vers : MutableList<Point2D>): MutableList<Short>{
        var output : MutableList<Point2D> = mutableListOf()
        var vertices = vers.toMutableList()
        var size = vertices.size
        var choosenPosition = 0
        while(size  > 3){
            var point1 = vertices.get((choosenPosition) % size)
            var point2  = vertices.get((choosenPosition + 1) % size)
            var point3 = vertices.get((choosenPosition + 2) % size)
            if (
                isEar(point1,point2,point3,vers) &&
                calculateCrossProduct(point3, point2, point1) < 0
            )
            {
                output.add(point1)
                output.add(point2)
                output.add(point3)
                vertices.remove(point2)
                size = vertices.size
            }
            else {
                choosenPosition = (choosenPosition + 1) % size
            }
        }
        output.addAll(vertices)
        var order : MutableList<Short> = mutableListOf()
        for(ver in output){
            order.add(vers.indexOf(ver).toShort())
        }
        return order
    }

    /** REFERENCES: De Casteljau Algorithm for evaluating Bezier Curves
     * https://blog.demofox.org/2015/07/05/the-de-casteljeau-algorithm-for-evaluating-bezier-curves/
     * parameters:
     *  vertices: list vertices
     *  t: value at time t **/
    fun BezierAlgorithm(vertices: MutableList<Point2D>, t: Float) : Vector {
        if (vertices.size == 2) {
            return mixVertices(
                Vector(vertices[0].getX(), vertices[0].getY()),
                Vector(vertices[1].getX(), vertices[1].getY()),
                t
            )
        } else {
            var vec1 = BezierAlgorithm(vertices.subList(0, vertices.size - 1), t)
            var vec2 = BezierAlgorithm(vertices.subList(1, vertices.size), t)
            return mixVertices(vec1, vec2, t)
        }
    }
    /**parameters:
     * vector1: the coordinate of point 1
     * vector2: the coordinate of point 2
     * t: time **/
    fun mixVertices(vector1: Vector, vector2: Vector, t: Float): Vector {
        var x = vector1.getXValue() * (1.0f - t) + vector2.getXValue() * t
        var y = vector1.getYValue() * (1.0f - t) + vector2.getYValue() * t
        return Vector(x,y)
    }

    /** Define control points
     * https://math.stackexchange.com/questions/2871559/formula-or-algorithm-to-draw-curved-lines-between-points**/
    fun defineControlPoints(vertices: MutableList<Point2D>) : MutableList<Point2D>{
        var output : MutableList<Point2D> = mutableListOf()
        output.add(vertices[0])
        var vector1 = Vector(vertices[1].getX() - vertices[0].getX(), vertices[1].getY() - vertices[0].getY())
        var distance1 = MathSupport().calculateDistance(vertices[0],vertices[1]) / 3
        output.add(
            Point2D(
            vertices[0].getX() + distance1 * vector1.getXValue(),
            vertices[0].getY() + distance1 * vector1.getYValue() ,
            StringHelper().createRandomVerticeId(),
            vertices[0].getGroupId(),
        )
        )

        var i = 1
        while( i <  vertices.size - 1){
            // define neighbor vector  (i+1,i-1)
            var vector = Vector(
                vertices[i+1].getX() - vertices[i-1].getX(),
                vertices[i+1].getY() - vertices[i-1].getY()
            )
            vector.normalizeVector()
            // define control left point
            var leftSize = MathSupport().calculateDistance(vertices[i],vertices[i-1]) / 3
            var leftX = vertices[i].getX() - leftSize * vector.getXValue()
            var leftY = vertices[i].getY() - leftSize * vector.getYValue()
            output.add(Point2D(leftX,leftY, StringHelper().createRandomVerticeId(),vertices[0].getGroupId()))
            // add endpoint
            output.add(vertices[i])
            //define control right point
            var rightSize = MathSupport().calculateDistance(vertices[i], vertices[i+1]) / 3
            var rightX = vertices[i].getX() + rightSize * vector.getXValue()
            var rightY = vertices[i].getY() + rightSize * vector.getYValue()
            output.add(Point2D(rightX, rightY, StringHelper().createRandomVerticeId(), vertices[0].getGroupId()))
            ++i
        }
        var size = vertices.size
        var vector2 = Vector(
            vertices[size - 2].getX() - vertices[size - 1].getX(),
            vertices[size - 2].getY() - vertices[size - 1].getY()
        )

        var distance2 = MathSupport().calculateDistance(vertices[size-1],vertices[size - 2]) / 3
        output.add(
            Point2D(vertices[size - 1].getX() + distance2 * vector2.getXValue(),
                vertices[size - 1].getY() + distance2 * vector2.getYValue(),
                StringHelper().createRandomVerticeId(),
                vertices[0].getGroupId())
        )
        output.add(vertices[size - 1])
        return output
    }


    fun findSafePointPosition(vers: MutableList<Point2D>) : Int{
        var temp : MutableList<Point2D> = vers.toMutableList()
        temp.sortBy { it.getX() }
        return vers.indexOf(temp.get(0))
    }
    fun defineTypeOfPolygon(obj: Object){
        var i = 0
        var size = obj.getVertices().size
        while(i < obj.getVertices().size){
            var p1 : Point2D = obj.getVertices()[i % size]
            var p2 : Point2D = obj.getVertices()[(i + 1) % size]
            var p3 : Point2D = obj.getVertices()[(i + 2) % size]
            if(calculateCrossProduct(p3,p2,p1) < 0){
                Log.d("== VERTICES","(${p1.getX()};${p1.getY()}) -- (${p2.getX()};${p2.getY()}) -- (${p3.getX()};${p3.getY()})")
                Log.d("== TYPE POLGYON","NOT-CONVEX")
                return
            }else
                ++i
        }
    }
}