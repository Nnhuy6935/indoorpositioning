package com.example.indoorpositioning.map

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.indoorpositioning.Model.Object
import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.shader.Shader
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer : GLSurfaceView.Renderer{
    lateinit var shader: Shader
    private var screenWidth = 0.0f
    private var screenheight = 0.0f


    private var listObject = mutableListOf<Object>()

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.878f, 0.937f, 0.882f, 1.0f)

        var vertexCode : String = "attribute vec4 vPosition;\n" +
                "void main(){\n" +
                "    gl_Position = vPosition;\n" +
                "    gl_PointSize = 10.0;" +
                "}"
        var fragmentCode : String= "precision mediump float;\n" +
                "uniform vec4 vColor;\n" +
                "void main(){\n" +
                "    gl_FragColor = vColor;\n" +
                "}"
        shader = Shader(vertexCode, fragmentCode)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20. glViewport(0,0,width, height)
        shader.setScreenWidth(width.toFloat())
        shader.setScreenHeight(height.toFloat())
        screenWidth = width.toFloat()
        screenheight = height.toFloat()

    }

    override fun onDrawFrame(p0: GL10?) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glLineWidth(5f)
        for (obj in listObject){
            shader.drawObject(obj)
        }

    }

    fun addNewObject(obj: Object){
        if(obj.getType() == ObjectType.POINT){
            listObject.add(0,obj)
        }else{
            listObject.add(obj)
        }

    }
    fun removeObject(obj: Object){  listObject.remove(obj) }

    /**GETTER AND SETTER**/
    fun getScreenWidth(): Float {return screenWidth}
    fun getScreeHeight() : Float {return screenheight}
    fun getListObject(): List<Object>{ return listObject}



}