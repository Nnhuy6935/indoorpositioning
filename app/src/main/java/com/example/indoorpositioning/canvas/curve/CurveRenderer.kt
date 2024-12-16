package com.example.indoorpositioning.canvas.curve

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.indoorpositioning.Helputil.ColorHelper
import com.example.indoorpositioning.Model.HistoryAction
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.canvas.CustomRenderer
import com.example.indoorpositioning.shader.Shader
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CurveRenderer : GLSurfaceView.Renderer, CustomRenderer(){
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

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0,0,width,height)
        shader!!.setScreenWidth(width.toFloat())
        shader!!.setScreenHeight(height.toFloat())
        this.screenWidth = width.toFloat()
        this.screenHeight = height.toFloat()
    }

    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
        for(ver in lstPoint){
            shader!!.drawAPoint(ver)
        }
        if(lstPoint.size >= 2)
            shader!!.drawACurve(lstPoint, ColorHelper().convertColorToFloatArray(borderColor))
    }

    override fun addPoint(point: Point2D){
        var beforePoint = lstPoint.lastOrNull()
        lstPoint.add(point)
        //handle history
        addHistoryAction(
            HistoryAction(
                HistoryAction.HistoryActionType.ADD,
                point,
                beforePoint
            )
        )
    }
    override fun removePoint(point: Point2D){
        var positionRemove = lstPoint.indexOf(point)
        var relatedObj : Point2D? = null
        if(positionRemove - 1 >= 0)
            relatedObj = lstPoint[positionRemove-1]
        else relatedObj = null
        addHistoryAction(
            HistoryAction(
                HistoryAction.HistoryActionType.DELETE,
                point,
                relatedObj
            )
        )
        lstPoint.removeAt(positionRemove)
    }

    override fun handleUndoAction(){
        if(currentActionPosition != 0){
            var handleAction = history.get(currentActionPosition-1)
            currentActionPosition--
            if(currentActionPosition == 0){
                updateUndoAndRedoCallback!!.disableUndo()
            }
            if(currentActionPosition == history.size - 1){
                updateUndoAndRedoCallback!!.enableRedo()
            }
            when(handleAction.getAction()){
                HistoryAction.HistoryActionType.ADD -> {
                    Log.d("== CURVE UNDO","ADD")
                    for(ver in lstPoint)
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            lstPoint.remove(ver)
                            return
                        }
                    return
                }
                HistoryAction.HistoryActionType.DELETE -> {
                    Log.d("== CURVE UNDO","DELETE")
                    var relatedObj = handleAction.getRelatedObject()
                    if(relatedObj == null){
                        lstPoint.add(0,handleAction.getMainObject())
                    }else {
                        for(ver in lstPoint)
                            if(ver.getId() == relatedObj.getId()){
                                var pos = lstPoint.indexOf(ver)
                                lstPoint.add(pos + 1, handleAction.getMainObject())
                                return
                            }
                    }
                    return
                }
                HistoryAction.HistoryActionType.MOVE ->{
                    Log.d("== CURVE UNDO","MOVE")
                    for(ver in lstPoint){
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            var updatePoint = handleAction.getRelatedObject()
                            if(updatePoint != null){
                                ver.setX(handleAction.getRelatedObject()!!.getX())
                                ver.setY(handleAction.getRelatedObject()!!.getY())
                            }
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
    override fun handleRedoAction(){
        if(currentActionPosition < history.size){
            var handleAction  = history.get(currentActionPosition)
            currentActionPosition++
            if(currentActionPosition == history.size){
                updateUndoAndRedoCallback!!.disableRedo()
            }
            if(currentActionPosition == 1){
                updateUndoAndRedoCallback!!.enableUndo()
            }
            when(handleAction.getAction()){
                HistoryAction.HistoryActionType.ADD ->{
                    Log.d("==  CURVE REDO","ADD")
                    var relatedPoint = handleAction.getRelatedObject()
                    if(relatedPoint == null ){
                        lstPoint.add(0, handleAction.getMainObject())
                    }else{
                        for(ver in lstPoint)
                            if(ver.getId() == relatedPoint.getId()){
                                var pos = lstPoint.indexOf(ver)
                                lstPoint.add(++pos, handleAction.getMainObject())
                                return
                            }
                    }
                    return
                }
                HistoryAction.HistoryActionType.DELETE -> {
                    Log.d("== CURVE REDO", "DELETE")
                    for(ver in lstPoint)
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            lstPoint.remove(ver)
                            return
                        }
                    return
                }
                HistoryAction.HistoryActionType.MOVE -> {
                    Log.d("== CURVE REDO","MOVE")
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
}