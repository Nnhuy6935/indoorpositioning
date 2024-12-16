package com.example.indoorpositioning.canvas

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.example.indoorpositioning.Helputil.MathSupport
import com.example.indoorpositioning.Interface.OnUpdateUndoAndRedo

abstract class CustomSurfaceView(context: Context, attrs : AttributeSet) : GLSurfaceView(context, attrs){
    protected var surfaceRenderer : CustomRenderer? = null
    protected  var groupId: String = ""
    protected var isMoving: Boolean = false
    protected var drawPointMode : Boolean = true
    protected var drawLineType : Boolean = true


    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x : Float = event?.getX() ?:  0.0f
        val y : Float = event?.getY() ?: 0.0f

        var normalizedX : Float = (x * 2.0f / surfaceRenderer!!.getScreenWidth() - 1.0f )
        var normalizedY : Float = -(y * 2.0f/ surfaceRenderer!!.getScreenHeight()) + 1.0f
        normalizedX = MathSupport().roundTo4DecimalPlaces(normalizedX)
        normalizedY = MathSupport().roundTo4DecimalPlaces(normalizedY)
        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                handleActionDown(normalizedX, normalizedY)
                return true
            }
            MotionEvent.ACTION_UP -> {
                handleActionUp(normalizedX, normalizedY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                handleActionMove(normalizedX, normalizedY)
                return true
            }

        }
        return false
    }
    abstract fun handleActionDown(x: Float, y: Float)
    abstract fun handleActionMove(x: Float, y: Float)
    abstract fun handleActionUp(x: Float, y: Float)
    fun handleActionUndo(){
        surfaceRenderer!!.handleUndoAction()
        requestRender()
    }
    fun handleActionRedo(){
        surfaceRenderer!!.handleRedoAction()
        requestRender()
    }
    fun cleanHistory(){
        surfaceRenderer!!.cleanHistoryInformation()
    }
    abstract fun showDialog()

    /**GETTER AND SETTER**/
    @JvmName("functionUpdateUndoAndRedoOfSurfaceView")
    fun setUpdateUndoAndRedoCallback(data: OnUpdateUndoAndRedo){
        surfaceRenderer!!.setUpdateUndoAndRedoCallback(data)
    }
    @JvmName("functionSetGroupIdOfKotlin")
    fun setGroupId(id: String){ this.groupId = id}
    @JvmName("functionGetGroupIdOfKotlin")
    fun getGroupId() : String{ return this.groupId}
    @JvmName("functionGetRendererOfKotlin")
    fun getSurfaceRenderer() : CustomRenderer? {return this.surfaceRenderer}
    @JvmName("functionSetDrawModeOfKotlin")
    fun setDrawMode(mode: Boolean) { this.drawPointMode = mode}
    @JvmName("functionSetDrawTypeOfKotlin")
    fun setLineType(mode: Boolean) {this.drawLineType = mode}
}