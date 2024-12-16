package com.example.indoorpositioning.canvas.concave

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.indoorpositioning.Helputil.StringHelper
import com.example.indoorpositioning.Model.HistoryAction
import com.example.indoorpositioning.Model.Object
import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.canvas.CustomRenderer
import com.example.indoorpositioning.shader.Shader
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ConcavePolygonRenderer : GLSurfaceView.Renderer, CustomRenderer() {

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
        shader!!.setScreenWidth(width.toFloat())
        shader!!.setScreenHeight(height.toFloat())
        this.screenWidth = width.toFloat()
        this.screenHeight = height.toFloat()
    }
    override fun onDrawFrame(p0: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

        for(obj in lstPoint)
            shader!!.drawAPoint(obj)
        for(line in lstLine)
            shader!!.drawALine(line)
    }

    override fun addPoint(point: Point2D){
        var beforePoint : Point2D? = null
        if(lstPoint.size - 1 >= 0)
            beforePoint = lstPoint.get(lstPoint.size - 1)
        lstPoint.add(point)
        history.add(currentActionPosition,
            HistoryAction(
                HistoryAction.HistoryActionType.ADD,
                point,
                beforePoint
                ))
        currentActionPosition++
        history = history.subList(0,currentActionPosition)
        updateUndoAndRedoCallback!!.disableRedo()
        if(currentActionPosition == 1){
            updateUndoAndRedoCallback!!.enableUndo()
        }
    }
    override fun removePoint(point: Point2D){lstPoint.remove(point)}
    override fun handleRedoAction(){
        if(currentActionPosition < history.size){
            var handleAction = history[currentActionPosition]
            currentActionPosition++
            if(currentActionPosition == history.size){
                updateUndoAndRedoCallback!!.disableRedo()
            }
            if(currentActionPosition == 1){
                updateUndoAndRedoCallback!!.enableUndo()
            }
            // handle redo
            when(handleAction.getAction()){
                HistoryAction.HistoryActionType.ADD ->{
                    Log.d("== REDO CONCAVE","ADD")
                    lstPoint.add(handleAction.getMainObject())
                    if(handleAction.getRelatedObject() != null){
                        var line = Object(ObjectType.LINE,
                            mutableListOf(handleAction.getMainObject(), handleAction.getRelatedObject()!!),
                            StringHelper().createRandomObjectId()
                        )
                        line.setFillColorWithInt(fillColor)
                        line.setBorderColorWithInt(borderColor)
                        lstLine.add(line)
                    }
                    return
                }
                HistoryAction.HistoryActionType.ADD_LINE -> {
                    Log.d("== REDO CONCAVE","ADD LINE ")
                    var line = Object(ObjectType.LINE,
                        mutableListOf(handleAction.getMainObject(), handleAction.getRelatedObject()!!),
                        StringHelper().createRandomObjectId()
                    )
                    line.setBorderColorWithInt(borderColor)
                    line.setFillColorWithInt(fillColor)
                    lstLine.add(line)
                    return
                }
                HistoryAction.HistoryActionType.MOVE -> {
                    Log.d("== REDO CONCAVE","MOVE")
                    for(ver in lstPoint){
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            ver.setX(handleAction.getMainObject().getX())
                            ver.setY(handleAction.getMainObject().getY())
                        }
                    }
                    return
                }
                HistoryAction.HistoryActionType.MOVE_LINE -> {
                    Log.d("== REDO CONCAVE","MOVE LINE ")
                    var vector = handleAction.getTransitionVector()
                    for(line in lstLine){
                        if((line.getVertices()[0].getId() == handleAction.getMainObject().getId()
                                    && line.getVertices()[1].getId() == handleAction.getRelatedObject()!!.getId())
                            || (line.getVertices()[1].getId() == handleAction.getMainObject().getId()
                                    && line.getVertices()[0].getId() == handleAction.getRelatedObject()!!.getId())){
                            for(point in line.getVertices()){
                                point.setX(point.getX() + vector!!.getXValue())
                                point.setY(point.getY() + vector.getYValue())
                            }
                            return
                        }
                    }
                    return
                }
                HistoryAction.HistoryActionType.DELETE -> {
                    Log.d("== REDO CONCAVE","DELETE")
                    var i = 0
                    var mainPoint = handleAction.getMainObject()
                    while( i < lstLine.size){
                        if(lstLine[i].getVertices()[0].getId() == mainPoint.getId() || lstLine[i].getVertices()[1].getId() == mainPoint.getId()){
                            lstLine.removeAt(i)
                        }else
                            ++i
                    }
                    lstPoint.remove(mainPoint)
                    return
                }
                HistoryAction.HistoryActionType.DELETE_LINE -> {
                    Log.d("== REDO CONCAVE","DELETE LINE")
                    for(line in lstLine){
                        if((line.getVertices()[0].getId() == handleAction.getMainObject().getId()
                                    && line.getVertices()[1].getId() == handleAction.getRelatedObject()!!.getId())
                            || line.getVertices()[1].getId() == handleAction.getMainObject().getId()
                                    &&line.getVertices()[0].getId() == handleAction.getRelatedObject()!!.getId()){
                            lstLine.remove(line)
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
            var handleAction = history[currentActionPosition-1]
            currentActionPosition--
            if(currentActionPosition == 0){
                updateUndoAndRedoCallback!!.disableUndo()
            }
            if(currentActionPosition == history.size - 1){
                updateUndoAndRedoCallback!!.enableRedo()
            }
            // handle undo
            when(handleAction.getAction()){
                HistoryAction.HistoryActionType.ADD -> {
                    Log.d("== UNDO CONCAVE","ADD")
                    //remove related line
                    var i = 0
                    while(i < lstLine.size){
                        if(lstLine[i].getVertices()[0].getId() == handleAction.getMainObject().getId()
                            || lstLine[i].getVertices()[1].getId() == handleAction.getMainObject().getId()){
                            lstLine.removeAt(i)
                        }else{
                            ++i
                        }

                    }
                    for(ver in lstPoint){
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            lstPoint.remove(ver)
                            return
                        }
                    }
                    return
                }
                HistoryAction.HistoryActionType.ADD_LINE -> {
                    Log.d("== UNDO CONCAVE","ADD LINE ")
                    for(line in lstLine){
                        if((line.getVertices()[0].getId() == handleAction.getMainObject().getId() &&
                                    line.getVertices()[1].getId() == handleAction.getRelatedObject()!!.getId())
                            || (line.getVertices()[1].getId() == handleAction.getMainObject().getId() &&
                                    line.getVertices()[0].getId() == handleAction.getRelatedObject()!!.getId()) ){
                            lstLine.remove(line)
                            return
                        }
                    }
                    return
                }
                HistoryAction.HistoryActionType.MOVE -> {
                    Log.d("== UNDO CONCAVE","MOVE")
                    for(ver in lstPoint){
                        if(ver.getId() == handleAction.getMainObject().getId()){
                            ver.setX(handleAction.getRelatedObject()!!.getX())
                            ver.setY(handleAction.getRelatedObject()!!.getY())
                        }
                    }
                    return
                }
                HistoryAction.HistoryActionType.MOVE_LINE -> {
                    Log.d("== UNDO CONCAVE","MOVE LINE ")
                    var vector = handleAction.getTransitionVector()
                    for(line in lstLine){
                        if((line.getVertices()[0].getId() == handleAction.getMainObject().getId()
                                    && line.getVertices()[1].getId() == handleAction.getRelatedObject()!!.getId())
                            || (line.getVertices()[1].getId() == handleAction.getMainObject().getId()
                                    && line.getVertices()[0].getId() == handleAction.getRelatedObject()!!.getId())){
                            for(point in line.getVertices()){
                                point.setX(point.getX() - vector!!.getXValue())
                                point.setY(point.getY() - vector.getYValue())
                            }
                            return
                        }
                    }
                    return
                }
                HistoryAction.HistoryActionType.DELETE -> {
                    Log.d("== UNDO CONCAVE","DELETE")
                    lstPoint.add(handleAction.getMainObject())
                    if(handleAction.getDeletedPoints() != null ){
                        for(point in handleAction.getDeletedPoints()!!){
                            var line = Object(
                                ObjectType.LINE,
                                mutableListOf(point, handleAction.getMainObject()),
                                StringHelper().createRandomObjectId()
                            )
                            line.setFillColorWithInt(fillColor)
                            line.setBorderColorWithInt(borderColor)
                            lstLine.add(line)
                        }
                    }
                    return
                }
                HistoryAction.HistoryActionType.DELETE_LINE -> {
                    Log.d("== UNDO CONCAVE","DELETE LINE ")
                    var line = Object(
                        ObjectType.LINE,
                        mutableListOf(handleAction.getMainObject(), handleAction.getRelatedObject()!!),
                        StringHelper().createRandomObjectId()
                    )
                    line.setFillColorWithInt(fillColor)
                    line.setBorderColorWithInt(borderColor)
                    lstLine.add(line)
                    return
                }
                else -> {
                    var i = 0
                    while(i < lstLine.size){
                        if( lstLine[i].getVertices()[0].getId() == handleAction.getMainObject().getId()
                        || lstLine[i].getVertices()[1].getId() == handleAction.getMainObject().getId()){
                             lstLine.removeAt(i)
                        }else
                            ++i
                    }
                    lstPoint.remove(handleAction.getMainObject())
                    return
                }
            }
        }
    }



}