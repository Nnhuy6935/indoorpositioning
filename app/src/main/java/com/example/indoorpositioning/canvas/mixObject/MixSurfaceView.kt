package com.example.indoorpositioning.canvas.mixObject

import android.content.Context
import android.content.DialogInterface
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import com.example.indoorpositioning.Helputil.StringHelper
import com.example.indoorpositioning.Model.HistoryAction
import com.example.indoorpositioning.Model.Object
import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.Model.Vector
import com.example.indoorpositioning.canvas.CustomSurfaceView

class MixSurfaceView(context: Context, attrs : AttributeSet) : CustomSurfaceView(context, attrs) {
    var startPoint: Point2D? = null
    var startObject: Object? = null
    var startVec : Vector? = null
    var isSelectedSomething : Boolean = false
    // undo and redo
    var originPoint: Point2D? = null
    var originObject: Object? = null

    init {
        setEGLContextClientVersion(2)
        surfaceRenderer = MixRenderer()
        groupId = StringHelper().createRandomObjectId()
        setRenderer(surfaceRenderer as MixRenderer)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun handleActionDown(x: Float, y: Float) {
        startPoint = null
        startObject = null
        startVec = null
        isSelectedSomething = false
        isMoving = false
        //CHECK IF CLICK ON ANY POINT
        for(point in surfaceRenderer!!.getListPoint()){
            if(point.isClickOnPoint(x,y)){
                isSelectedSomething = true
                startPoint = point
                originPoint = Point2D(point.getX(), point.getY(), point.getId(), StringHelper().createRandomObjectId())
                break
            }
        }
        //CHECK IF CLICK ON ANY LINE
        if(isSelectedSomething == false ) {
            for (line in surfaceRenderer!!.getListLine()) {
                if (line.isClickOnObject(x, y)) {
                    isSelectedSomething = true
                    startObject = line
                    startVec = Vector(x, y)
                    originObject = Object(
                        ObjectType.LINE,
                        mutableListOf(
                            Point2D(
                                line.getVertices()[0].getX(),
                                line.getVertices()[0].getY(),
                                line.getVertices()[0].getId(),
                                StringHelper().createRandomObjectId(),
                            ),
                            Point2D(
                                line.getVertices()[1].getX(),
                                line.getVertices()[1].getY(),
                                line.getVertices()[0].getId(),
                                StringHelper().createRandomObjectId(),
                            )
                        ),
                        line.getId()
                    )
                    break
                }
            }
        }
        // DO NOT CLICK ON ANY THING
        if(isSelectedSomething == false){
            startPoint = Point2D(x,y,StringHelper().createRandomVerticeId(),groupId)
            if(drawPointMode) {
                // add a line to before point
                var points = surfaceRenderer!!.getListPoint()
                if(points.size - 1 >= 0){
                    var vertices : MutableList<Point2D> = mutableListOf()
                    vertices.add(startPoint!!)
                    vertices.add(points[points.size-1])
                    if(!drawLineType){
                        startPoint!!.setIsBelongCurve(true)
                    }
                    if(!vertices[0].getIsBelongCurve() || !vertices[1].getIsBelongCurve()){
                        var line = Object(
                            ObjectType.LINE,
                            vertices,
                            StringHelper().createRandomObjectId()
                        )
                        line.setBorderColorWithInt(surfaceRenderer!!.getBorderColor())
                        line.setFillColorWithInt(surfaceRenderer!!.getFillColor())
                        surfaceRenderer!!.addLineWithoutAddHistory(line)
                    }
                    if(!drawLineType)
                        startPoint!!.setIsBelongCurve(true)
                    else startPoint!!.setIsBelongCurve(false)
                }
                surfaceRenderer!!.addPoint(startPoint!!)
            }
            requestRender()
        }
    }

    override fun handleActionMove(x: Float, y: Float) {
        isMoving = true
        //MOVE POINT
        if(drawPointMode == true){
            if(isSelectedSomething){
                //move a point
                if(startPoint != null) {
                    startPoint?.setX(x)
                    startPoint?.setY(y)
                }
                //move a line
                if(startObject != null){
                    for(point in startObject?.getVertices()!!){
                        point.setX(point.getX() + (x-startVec?.getXValue()!!))
                        point.setY(point.getY() + (y-startVec?.getYValue()!!))
                    }
                    startVec?.setXValue(x)
                    startVec?.setyValue(y)
                }
            }
        }
        //DRAW LINE MODE
        else{
        }
        requestRender()
    }

    override fun handleActionUp(x: Float, y: Float) {
        if(drawPointMode){
            if(isSelectedSomething){
                if(isMoving) {
                    //move point
                    if(startPoint != null ) {
                        startPoint?.setX(x)
                        startPoint?.setY(y)
                        var movedPoint = Point2D(x,y,originPoint!!.getId(), StringHelper().createRandomObjectId())
                        getSurfaceRenderer()!!.addHistoryAction(
                            HistoryAction(
                                HistoryAction.HistoryActionType.MOVE,
                                movedPoint,
                                originPoint
                            )
                        )
                    }
                    //move line
                    else{
                        for(point in startObject?.getVertices()!!){
                            point.setX(point.getX() + (x-startVec?.getXValue()!!))
                            point.setY(point.getY() + (y-startVec?.getYValue()!!))
                        }
                        startVec?.setXValue(x)
                        startVec?.setyValue(y)

                        var transitionVec = Vector(
                            startObject!!.getVertices()[0].getX() - originObject!!.getVertices()[0].getX(),
                            startObject!!.getVertices()[0].getY() - originObject!!.getVertices()[0].getY()
                        )
                        var action = HistoryAction(
                            HistoryAction.HistoryActionType.MOVE_LINE,
                            startObject!!.getVertices()[0],
                            startObject!!.getVertices()[1]
                        )
                        action.setTransitionVector(transitionVec)
                        getSurfaceRenderer()!!.addHistoryAction(action)
                    }
                }
                else{
                    //show dialog to delete point
                }
            }
        }else{
            if(isSelectedSomething){
                var endpoint : Point2D
                for(point in getSurfaceRenderer()!!.getListPoint()){
                    // connect 2 point become a line
                    if(point.isClickOnPoint(x, y)){
                        endpoint = point
                        var vertices : MutableList<Point2D> = mutableListOf()
                        if(startPoint == null) continue
                        vertices.add(startPoint!!)
                        vertices.add(endpoint)
                        var line = Object(ObjectType.LINE, vertices, StringHelper().createRandomObjectId())
                        line.setFillColorWithInt(surfaceRenderer!!.getFillColor())
                        line.setBorderColorWithInt(surfaceRenderer!!.getBorderColor())
                        getSurfaceRenderer()!!.addLine(line)
                        // rearrange position
                        var start = surfaceRenderer!!.getListPoint().indexOf(startPoint)
                        var end = surfaceRenderer!!.getListPoint().indexOf(endpoint)
                        if(startPoint != null && end != -1){
                            surfaceRenderer!!.getListPoint().remove(startPoint)
                            surfaceRenderer!!.getListPoint().add(end, startPoint!!)
                        }

                        startPoint = null
                        break
                    }else{
                    }
                }
            }
        }
        //show dialog confirm delete
        if(!isMoving && isSelectedSomething && (startPoint != null || startObject != null)){
            showDialog()
        }
        requestRender()

    }

    override fun showDialog() {
        var builder : AlertDialog.Builder = AlertDialog.Builder(context)
        var type :String
        if(startPoint != null)
            type = "point"
        else type = "line"
        builder.setMessage("Do you want to delete this $type?")
        builder.setCancelable(true)
        builder.setPositiveButton(
            "Ok",
            DialogInterface.OnClickListener { dialog, id ->
                // DELETE A POINT
                startPoint?.let { handleDeleteAPoint(it) }
                // DELETE A LINE
                startObject?.let { handleDeleteALine(it) }
                requestRender()
                dialog.cancel()
            })
        builder.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener{ dialog, id ->
                startPoint = null
                dialog.cancel()
            }
        )
        var alertDialog = builder.create()
        alertDialog.show()
    }
    fun isHistoryAllMoveAction() : Boolean{
        return surfaceRenderer!!.isHistoryAllMoveAction()
    }
    fun handleDeleteAPoint(point: Point2D){
        var vertices = surfaceRenderer!!.getListPoint()
        var lines = surfaceRenderer!!.getListLine()
        var relatedPoints : MutableList<Point2D> = mutableListOf()

        //find point before point deleted
        var i = 0
        var beforePoint : Point2D? = null
        while(i < surfaceRenderer!!.getListPoint().size){
            if(vertices[i].getId() == point.getId())
                beforePoint = vertices[(i - 1 + vertices.size) % vertices.size]
            ++i
        }
        // get related line if point does not belong a curve
        var deletedId : MutableList<String> = mutableListOf()
        if(!point.getIsBelongCurve()){
            for(line in lines){
                if(line.getVertices()[0].getId() == point.getId()
                    || line.getVertices()[1].getId() == point.getId()){
                    deletedId.add(line.getId())
                    if(line.getVertices()[0].getId() != point.getId())
                        relatedPoints.add(line.getVertices()[0])
                    else relatedPoints.add(line.getVertices()[1])
                }
            }
            for(id in deletedId){
                surfaceRenderer!!.removeLineWithId(id)
            }
        }
        var action = HistoryAction(
            HistoryAction.HistoryActionType.DELETE,
            point,
            beforePoint
        )
        if(!point.getIsBelongCurve()){
            action.setDeletedPoints(relatedPoints)
        }else
            action.setDeletedPoints(null)
        surfaceRenderer!!.removePoint(point)
        surfaceRenderer!!.addHistoryAction(action)
    }
    fun handleDeleteALine(line: Object){   surfaceRenderer!!.removeLine(line) }
}