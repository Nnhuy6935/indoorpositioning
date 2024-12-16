package com.example.indoorpositioning.shader

import android.opengl.GLES20
import android.util.Log
import com.example.indoorpositioning.Model.Object
import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.Model.Vector
import com.example.indoorpositioning.canvas.BaseKnowledge
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Shader(vertexCode: String, fragmentCode: String) {
    private var mProgram : Int = 0
    private var vertextShader = 0
    private var fragmentShader = 0
    private var screenWidth = 0.0f
    private var screenHeight = 0.0f
    private val uniforms: HashMap<String, Int> = HashMap<String,Int>()
    private val attributes: HashMap<String, Int> = HashMap<String,Int>()
    val colors = floatArrayOf(1.0f, 0.5f, 0.2f, 1.0f);
    val fillColor = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f)
    private val COORS_PER_VERTEX  = 3;

    init {
        createProgram()
        vertextShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode)
        fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
        attachShader()
        attributes.put("vPosition",GLES20.glGetAttribLocation(mProgram,"vPosition"))
        uniforms.put("vColor",GLES20.glGetUniformLocation(mProgram,"vColor"))
    }

    fun createProgram(){
        mProgram = GLES20.glCreateProgram()
        if(mProgram == 0){
            Log.d("-- CREATE PROGRAM --" , "Shader create program failed")
        }else{
            Log.d("-- CREATE PROGRAM --" , "Shader create program success")
        }
    }
    fun loadShader(type: Int, shaderCode: String) : Int{
        return  GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode);
            GLES20.glCompileShader(shader)
        }
    }
    fun attachShader(){
        GLES20.glAttachShader(mProgram,vertextShader)
        GLES20.glAttachShader(mProgram,fragmentShader)
        GLES20.glLinkProgram(mProgram)
    }

    fun drawObject(obj : Object){
        if(obj.getType() == ObjectType.LINE){
            drawALine(obj)
        }
        else if(obj.getType() == ObjectType.POINT){
            drawAPoint(obj)
        }
        else if(obj.getType() == ObjectType.POLYGON || obj.getType() == ObjectType.TRIANGLE){
            var i = 0
            while(i < obj.getVertices().size){
                var previousVer = (i - 1 + obj.getVertices().size) % obj.getVertices().size
                drawALineConnect2Point(obj.getVertices()[previousVer], obj.getVertices()[i],obj.getBorderColorWithFloatArray())
                ++i
            }
            painAPolygon(obj)
        }
        else if(obj.getType() == ObjectType.CONCAVE_POLYGON){
            var i = 0
            while(i < obj.getVertices().size){
                var previsousVer = (i - 1 + obj.getVertices().size) % obj.getVertices().size
                drawALineConnect2Point(obj.getVertices()[previsousVer], obj.getVertices()[i], obj.getBorderColorWithFloatArray())
                ++i
            }
            if(BaseKnowledge().checkIsComplexPolygon(obj.getVertices().toMutableList()))
                paintAComplexPolygon(obj)
            else
                paintAConcavePolygon(obj)
        }
        else if(obj.getType() == ObjectType.CURVE){
            drawACurve(obj.getVertices().toMutableList(), obj.getBorderColorWithFloatArray())
        }
        else if(obj.getType() == ObjectType.MIX){
            drawMixObject(obj)
        }
    }

    fun drawAPoint(point: Point2D){
        if(point.getX() != 0f && point.getY() != 0f) {
            var vertices = floatArrayOf(
                point.getX(), point.getY(), 0.0f
            )

            val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(vertices)
                    position(0)
                }
            }

            GLES20.glUseProgram(mProgram)
            attributes.get("vPosition")?.let {
                GLES20.glEnableVertexAttribArray(it)
                GLES20.glVertexAttribPointer(
                    it,
                    COORS_PER_VERTEX,
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    vertexBuffer
                )
            }

            uniforms.get("vColor")?.let { color ->
                GLES20.glUniform4fv(color, 1, colors, 0)
            }
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
        }
    }
    fun drawAPoint(obj: Object){
        drawAPoint(obj.getVertices()[0])
    }
    fun drawPointForCurve(point: Point2D, drawColor: FloatArray){
        if(point.getX() != 0f && point.getY() != 0f) {
            var vertices = floatArrayOf(
                point.getX(), point.getY(), 0.0f
            )

            val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(vertices)
                    position(0)
                }
            }

            GLES20.glUseProgram(mProgram)
            attributes.get("vPosition")?.let {
                GLES20.glEnableVertexAttribArray(it)
                GLES20.glVertexAttribPointer(
                    it,
                    COORS_PER_VERTEX,
                    GLES20.GL_FLOAT,
                    false,
                    0,
                    vertexBuffer
                )
            }

            uniforms.get("vColor")?.let { color ->
                GLES20.glUniform4fv(color, 1, drawColor, 0)
            }
            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1)
        }
    }

    fun drawALineConnect2Point(point1: Point2D, point2: Point2D, color: FloatArray){
        val COORS_PER_VERTEX = 3
        val lineWidth = 0.01f
        var vx = point2.getX() - point1.getX()
        var vy = point2.getY() - point1.getY()
        var size = Math.sqrt(Math.pow(vx.toDouble(), 2.toDouble()) + Math.pow(vy.toDouble(),2.toDouble()))
        var normalizeVx = vx / size
        var normalizeVy = vy / size
        var zx = -normalizeVy.toFloat()
        var zy = normalizeVx.toFloat()
        var vertices = floatArrayOf(
            point1.getX() - (lineWidth/2)  * zx, point1.getY() - (lineWidth/2) * zy , 0f,
            point1.getX() + (lineWidth/2)  * zx, point1.getY() + (lineWidth/2) * zy ,0f,
            point2.getX() - (lineWidth/2)  * zx, point2.getY() - (lineWidth/2) * zy ,0f,
            point2.getX() + (lineWidth/2)  * zx, point2.getY() + (lineWidth/2) * zy ,0f,
        )
        val vertexBuffer :FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }
        GLES20.glUseProgram(mProgram)
        attributes.get("vPosition")?.let {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                it,
                COORS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                0,
                vertexBuffer
            )
        }
        uniforms.get("vColor")?.let { i->
            GLES20.glUniform4fv(i,1,color,0)
        }
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP,0,4)
    }
    fun drawALine(obj: Object){
        drawALineConnect2Point(obj.getVertices()[0],obj.getVertices()[1], obj.getBorderColorWithFloatArray())
    }

    fun painAPolygon(polygon: Object){
        val COORS_PER_VERTEX = 3
        var vertices = mutableListOf<Float>()
        for(ver in polygon.getVertices()){
            vertices.add(ver.getX())
            vertices.add(ver.getY())
            vertices.add(0.0f)
        }
        var order = mutableListOf<Short>()
        var i = 1
        while(i < polygon.getVertices().size-1){
            order.add(0)
            order.add(i.toShort())
            order.add((i+1).toShort())
            ++i
        }
        val vertexBuffer :FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices.toFloatArray())
                position(0)
            }
        }
        val orderBuffer : ShortBuffer = ByteBuffer.allocateDirect(order.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(order.toShortArray())
                position(0)
            }
        }

        GLES20.glUseProgram(mProgram)
        attributes.get("vPosition")?.let {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                it,
                COORS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                0,
                vertexBuffer
            )
        }
        uniforms.get("vColor")?.let { color->
            GLES20.glUniform4fv(color,1,polygon.getFillColorWithFloatArray(),0)
        }
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,order.size,GLES20.GL_UNSIGNED_SHORT,orderBuffer    )
    }

    fun drawACurve(vertices: MutableList<Point2D>, curveColor: FloatArray){
        var fullVertices = BaseKnowledge().defineControlPoints(vertices)
        var j = 0
        while( j < fullVertices.size - 3){
            val c_numPoinst = 100f
            var ii = 0
            while( ii < c_numPoinst){
                var t = ii.toFloat() / (c_numPoinst - 1).toFloat()
                var temp : Vector = BaseKnowledge().BezierAlgorithm(fullVertices.subList(j, j + 4), t)
                var currentX  = temp.getXValue()
                var currentY = temp.getYValue()
                drawPointForCurve(Point2D(currentX, currentY,"",""), curveColor)
                ++ii
            }
            j += 3
        }

    }

    /**draw lines around to create a polygon**/
    fun drawAConcavePolygon(obj: Object){
        var i  = 0
        while( i < obj.getVertices().size){
            if(i == obj.getVertices().size-1){
                drawALineConnect2Point(obj.getVertices()[i], obj.getVertices()[0], obj.getBorderColorWithFloatArray())
            }else
            {
                drawALineConnect2Point(obj.getVertices()[i], obj.getVertices()[i + 1], obj.getBorderColorWithFloatArray())
            }
            ++i
        }
    }
    /**COMPLEX POLYGON is a polygon have self-intersections or holes.**/
    fun paintAComplexPolygon(obj: Object){
        var holes : MutableList<MutableList<Point2D>> = mutableListOf()
        var i = 0
        var vertices = obj.getVertices().toMutableList()
        while( i < vertices.size){
            var position = BaseKnowledge().findLastPointWithId(vertices[i].getId(), vertices)
            if(position != -1 && position != i){
                var sublist = vertices.subList(0, i).toMutableList()
                sublist.addAll(vertices.subList(position, vertices.size))
                holes.add(sublist)
                vertices = vertices.subList(i, position).toMutableList()
                i = 0
            }else{
                ++i
            }
        }
        if(vertices.size != 0)
            holes.add(vertices.toMutableList())
        for(hole in holes){
            paintAHole(hole, obj.getFillColorWithFloatArray())
        }
    }
    fun paintAHole(vers: MutableList<Point2D>, inputColor: FloatArray){
        var COORS_PER_VERTEX : Int = 3
        var vertices = mutableListOf<Float>()
        for(ver in vers){
            vertices.add(ver.getX())
            vertices.add(ver.getY())
            vertices.add(0.0f)
        }
        var order = BaseKnowledge().defineOrderDraw(vers)

        val vertexBuffer :FloatBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices.toFloatArray())
                position(0)
            }
        }
        val orderBuffer : ShortBuffer = ByteBuffer.allocateDirect(order.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(order.toShortArray())
                position(0)
            }
        }

        GLES20.glUseProgram(mProgram)
        attributes.get("vPosition")?.let {
            GLES20.glEnableVertexAttribArray(it)
            GLES20.glVertexAttribPointer(
                it,
                COORS_PER_VERTEX,
                GLES20.GL_FLOAT,
                false,
                0,
                vertexBuffer
            )
        }
        uniforms.get("vColor")?.let { color->
            GLES20.glUniform4fv(color,1,inputColor,0)
        }
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,order.size,GLES20.GL_UNSIGNED_SHORT,orderBuffer    )
    }
    fun paintAConcavePolygon(obj: Object){
        paintAHole(obj.getVertices().toMutableList(), obj.getFillColorWithFloatArray())
    }

    fun drawMixObject(obj: Object){
        var i = 0
        while(i < obj.getVertices().size){
            if(!obj.getVertices()[i].getIsBelongCurve()) {
                drawALineConnect2Point(
                    obj.getVertices()[i],
                    obj.getVertices()[(i + 1 + obj.getVertices().size) % obj.getVertices().size],
                    obj.getBorderColorWithFloatArray()
                )
                ++i
            }
            else{
                var j = i + 1;
                while( j < obj.getVertices().size){
                    if(obj.getVertices()[j].getIsBelongCurve())
                        ++j
                    else
                        break
                }
                drawACurve(obj.getVertices().subList(i,j).toMutableList(), obj.getBorderColorWithFloatArray())
                i = j
            }
        }
    }

    /*** GETTER AND SETTER ***/
    fun setScreenWidth(width: Float){this.screenWidth = width}
    fun setScreenHeight(height : Float){ this.screenHeight = height}

}



