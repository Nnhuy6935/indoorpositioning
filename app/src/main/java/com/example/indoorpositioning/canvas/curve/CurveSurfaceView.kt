package com.example.indoorpositioning.canvas.curve

import android.content.Context
import android.content.DialogInterface
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import com.example.indoorpositioning.Helputil.StringHelper
import com.example.indoorpositioning.Model.HistoryAction
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.canvas.CustomSurfaceView

class CurveSurfaceView(context: Context, attrs: AttributeSet) : CustomSurfaceView(context, attrs) {
    var selectedPoint : Point2D? = null
    var originPoint : Point2D? = null
    var isSelectPoint : Boolean = false
    init {
        setEGLContextClientVersion(2)
        surfaceRenderer = CurveRenderer()
        groupId = StringHelper().createRandomObjectId()
        setRenderer(surfaceRenderer as CurveRenderer)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }

    override fun handleActionDown(x: Float, y: Float){
        selectedPoint = null
        originPoint = null
        isSelectPoint = false
        isMoving = false
        if (isClickOnAnyPoint(x, y)) {
            isSelectPoint = true
        } else {
            var point = Point2D(
                x,
                y,
                StringHelper().createRandomVerticeId(),
                groupId,
            )
            getSurfaceRenderer()!!.addPoint(point)
            requestRender()
        }
    }
    override fun handleActionMove(x: Float, y: Float){
            isMoving = true
            if(isSelectPoint){
                selectedPoint?.setX(x)
                selectedPoint?.setY(y)
                requestRender()
            }
    }
    override fun handleActionUp(x: Float, y: Float){
        if(isSelectPoint){
            if(isMoving){
                selectedPoint?.setX(x)
                selectedPoint?.setY(y)
                var pointMoveTo = Point2D(x,y,selectedPoint!!.getId(), StringHelper().createRandomObjectId())
                getSurfaceRenderer()!!.addHistoryAction(HistoryAction(
                    HistoryAction.HistoryActionType.MOVE,
                    pointMoveTo,
                    originPoint
                ))
            }else{
                showDialog()
            }
        }
        requestRender()
    }

    fun isClickOnAnyPoint(x: Float, y: Float): Boolean{
        if(getSurfaceRenderer()!!.getListPoint() == null || getSurfaceRenderer()!!.getListPoint().size == 0)
            return false
        for(ver in getSurfaceRenderer()!!.getListPoint()){
            if(ver.isClickOnPoint(x,y)){
                selectedPoint = ver
                originPoint = Point2D(ver.getX(), ver.getY(), ver.getId(), StringHelper().createRandomObjectId())
                return true
            }
        }
        return false
    }
    override fun showDialog(){
        var builder : AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setMessage("Do you want to delete this point?")
        builder.setCancelable(true)
        builder.setPositiveButton(
            "Ok",
            DialogInterface.OnClickListener { dialog, id ->
                getSurfaceRenderer()!!.removePoint(selectedPoint!!)
                requestRender()
                dialog.cancel()
            }
        )
        builder.setNegativeButton(
            "Cancel",
            DialogInterface.OnClickListener{ dialog, id ->
                selectedPoint = null
                isSelectPoint = false
                isMoving = false
                dialog.cancel()
            }
        )

        var alertDialog = builder.create()
        alertDialog.show()
    }

}
