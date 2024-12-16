package com.example.indoorpositioning.canvas.normal

import android.content.Context
import android.content.DialogInterface
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import com.example.indoorpositioning.Helputil.StringHelper
import com.example.indoorpositioning.Model.HistoryAction
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.canvas.CustomSurfaceView


class ObjectSurfaceView(context: Context, attrs : AttributeSet) : CustomSurfaceView(context, attrs) {
    var startPoint: Point2D? = null
    var originPoint: Point2D? = null
    var isSelectedPoint : Boolean = false
    init {
        setEGLContextClientVersion(2)
        surfaceRenderer = ObjectRenderer()
        setRenderer(surfaceRenderer as ObjectRenderer)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        groupId = StringHelper().createRandomObjectId()
    }

    override fun handleActionDown(x: Float, y: Float){
        startPoint = null
        originPoint = null
        isSelectedPoint = false
        for(point in surfaceRenderer!!.getListPoint()){
            if(point.isClickOnPoint(x,y)){
                isSelectedPoint =  true
                startPoint = point
                originPoint = Point2D(point.getX(), point.getY(), point.getId(), StringHelper().createRandomObjectId())
                break
            }
        }
        if(startPoint == null) {
            startPoint = Point2D(x, y, StringHelper().createRandomVerticeId(),groupId)
            isSelectedPoint = false
            surfaceRenderer!!.addPoint(startPoint!!)

        }
        isMoving = false
        requestRender()
    }
    override fun handleActionMove(x: Float, y: Float){
        if(isSelectedPoint == true){
            startPoint!!.setX(x)
            startPoint!!.setY(y)
        }
        isMoving = true
        requestRender()
    }
    override fun handleActionUp(x: Float, y: Float){
        if(isMoving) {
            //MOVE POINT
            if(isSelectedPoint){
                startPoint!!.setX(x)
                startPoint!!.setY(y)
                surfaceRenderer!!.addHistoryAction(HistoryAction(
                    HistoryAction.HistoryActionType.MOVE,
                    Point2D(startPoint!!.getX(), startPoint!!.getY(),startPoint!!.getId(),StringHelper().createRandomObjectId()),
                    originPoint
                ))
                originPoint = null
            }else
            {
                //todo: nothing
            }
        }else{
            //SHOW DELETE POINT DIALOG
            if(isSelectedPoint && startPoint != null)
                showDialog()

        }

        requestRender()
    }
    override fun showDialog(){
        var builder : AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("Do you want to delete this point?")
        builder.setCancelable(true)
        builder.setPositiveButton(
            "Ok",
            DialogInterface.OnClickListener { dialog, id ->
                surfaceRenderer!!.removePoint(startPoint!!)
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
}