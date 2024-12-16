package com.example.indoorpositioning.canvas.normal

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.indoorpositioning.Helputil.ColorHelper
import com.example.indoorpositioning.Model.HistoryAction
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.canvas.BaseKnowledge
import com.example.indoorpositioning.canvas.CustomRenderer
import com.example.indoorpositioning.shader.Shader
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ObjectRenderer : GLSurfaceView.Renderer, CustomRenderer() {

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        GLES20.glClearColor(0.878f, 0.937f, 0.882f, 1.0f)

        var vertexCode : String = "attribute vec4 vPosition;\n" +
                "void main(){\n" +
                "    gl_Position = vPosition;\n" +
                "    gl_PointSize = 20.0;" +
                "}"
        var fragmentCode : String= "precision mediump float;\n" +
                "uniform vec4 vColor;\n" +
                "void main(){\n" +
                "    gl_FragColor = vColor;\n" +
                "}"
        shader = Shader(vertexCode, fragmentCode)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }
    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0,0,width,height)
        shader?.setScreenWidth(width.toFloat())
        shader?.setScreenHeight(height.toFloat())
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
    }
    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
        for(point in lstPoint){
            shader?.drawAPoint(point)
        }
        if(lstPoint.size > 1){
            var i = 0
            while( i < lstPoint.size){
                if(i != lstPoint.size - 1)
                    shader?.drawALineConnect2Point(lstPoint[i], lstPoint[i+1],ColorHelper().convertColorToFloatArray(borderColor))
                else
                    shader?.drawALineConnect2Point(lstPoint[i], lstPoint[0], ColorHelper().convertColorToFloatArray(borderColor))
                ++i
            }
        }

    }


    override fun addPoint(point: Point2D){
        // handle history
        addHistoryAction(HistoryAction(
            HistoryAction.HistoryActionType.ADD,
            point,
            null,
        ))
        // handle real list
        lstPoint.add(point)
        var result = defineObjectVertices()
        if(!result){
            history.removeLast()
            currentActionPosition = history.size
            if(currentActionPosition == 0){
                updateUndoAndRedoCallback!!.disableUndo()
            }
            if(currentActionPosition == history.size){
                updateUndoAndRedoCallback!!.disableRedo()
            }
        }
    }
    override fun removePoint(point: Point2D) {
        //handle history
        lstPoint.remove(point)
        defineObjectVertices()
        addHistoryAction(HistoryAction(
            HistoryAction.HistoryActionType.DELETE,
            point,
            null
        ))
    }
    override fun handleRedoAction(){
        if(currentActionPosition < history.size){
            var handleAction = history.get(currentActionPosition)
            currentActionPosition++
            if(currentActionPosition == history.size){
                updateUndoAndRedoCallback?.disableRedo()
            }
            if(currentActionPosition == 1){
                updateUndoAndRedoCallback?.enableUndo()
            }
            when(handleAction.getAction()){
                HistoryAction.HistoryActionType.ADD -> {
                    Log.d("== REDO ACTION","ADD")
                    lstPoint.add(handleAction.getMainObject())
                    defineObjectVertices()
                    return
                }
                HistoryAction.HistoryActionType.DELETE ->{
                    Log.d("== REDO ACTION","DELETE")
                    for(ver in lstPoint)
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            lstPoint.remove(handleAction.getMainObject())
                            return
                        }
                    return
                }
                HistoryAction.HistoryActionType.MOVE -> {
                    Log.d("== REDO ACTION","MOVE")
                    for(ver in lstPoint){
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            var updatePoint = handleAction.getMainObject()
                            ver.setX(updatePoint.getX())
                            ver.setY(updatePoint.getY())
                            return
                        }
                    }
                    return
                }
                else -> {
                    return
                }
            }
        }
    }
    override fun handleUndoAction(){
        if(currentActionPosition != 0){
            var handleAction = history.get(currentActionPosition-1)
            currentActionPosition--
            if(currentActionPosition == 0){
                updateUndoAndRedoCallback?.disableUndo()
            }
            if(currentActionPosition == history.size - 1){
                updateUndoAndRedoCallback?.enableRedo()
            }

            when(handleAction.getAction()){
                HistoryAction.HistoryActionType.ADD -> {
                    Log.d("== UNDO ACTION", "ADD")
                    for(ver in lstPoint){
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            lstPoint.remove(handleAction.getMainObject())
                            return
                        }
                    }
                    return
                }
                HistoryAction.HistoryActionType.DELETE -> {
                    Log.d("== UNDO ACTION","DELETE")
                    lstPoint.add(handleAction.getMainObject())
                    defineObjectVertices()
                    return
                }
                HistoryAction.HistoryActionType.MOVE -> {
                    Log.d("== UNDO ACTION","MOVE")
                    for(ver in lstPoint)
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            var updatePoint = handleAction.getRelatedObject()
                            if(updatePoint != null ){
                                ver.setX(updatePoint.getX())
                                ver.setY(updatePoint.getY())
                            }
                            return
                        }
                    return
                }
                else -> {
                    return
                }
            }
        }
    }

    fun defineObjectVertices() : Boolean{
        // sắp xếp các điểm tăng dần theo x nếu x bằng nhau thì xếp tăng dần theo y
        var input = lstPoint.sortedWith(compareBy<Point2D> {it.getX()}.thenBy { it.getY() })
        var begin = input.size

        val firstBound: MutableList<Point2D> = mutableListOf()
        // xây dựng bao dưới
        for(point in input){
            while(firstBound.size >= 2 && BaseKnowledge().crossProduct(firstBound[firstBound.size - 2], firstBound[firstBound.size - 1], point) <= 0){
                firstBound.removeAt(firstBound.size - 1)
            }
            firstBound.add(point)
        }
        // xây dựng bao trên
        val secondBound: MutableList<Point2D> = mutableListOf()
        for (point in input) {
            while (secondBound.size >= 2 && BaseKnowledge().crossProduct(secondBound[secondBound.size - 2], secondBound[secondBound.size - 1], point) > 0) {
                secondBound.removeAt(secondBound.size - 1)
            }
            secondBound.add(point)
        }
        // Loại bỏ điểm đầu của secondBound vì nó đã có trong firstBound
        if (secondBound.isNotEmpty()) {
            secondBound.removeAt(0)
        }
        //loại bỏ điểm cuối của secondBound vì đã có trong firstBound
        if(secondBound.isNotEmpty())
            secondBound.removeAt(secondBound.size-1)
        // Kết hợp hai phần bao
        firstBound.addAll(secondBound.reversed())
        lstPoint = mutableListOf()
        lstPoint = firstBound
        var end = firstBound.size
        return (begin == end)
    }

}