package com.example.indoorpositioning.canvas
import android.graphics.Color
import com.example.indoorpositioning.Interface.OnUpdateUndoAndRedo
import com.example.indoorpositioning.Model.HistoryAction
import com.example.indoorpositioning.Model.Object
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.shader.Shader

abstract class CustomRenderer {
    protected var screenWidth : Float = 0.0f
    protected var screenHeight: Float = 0.0f
    protected var shader : Shader? = null
    protected var lstPoint: MutableList<Point2D> = mutableListOf()
    protected var lstLine: MutableList<Object> = mutableListOf()
    protected var history : MutableList<HistoryAction> = mutableListOf()
    protected var currentActionPosition : Int = 0
    protected var updateUndoAndRedoCallback : OnUpdateUndoAndRedo? = null
    protected var borderColor : Int = Color.BLACK
    protected var fillColor : Int = Color.BLACK

    abstract fun addPoint(point: Point2D)
    abstract fun removePoint(point: Point2D)
    abstract fun handleRedoAction()
    abstract fun handleUndoAction()
    fun addHistoryAction(action : HistoryAction){
        history.add(currentActionPosition,action)
        currentActionPosition++
        history = history.subList(0, currentActionPosition)
        updateUndoAndRedoCallback?.disableRedo()
        if(currentActionPosition == 1){
            updateUndoAndRedoCallback?.enableUndo()
        }
    }
    fun cleanHistoryInformation(){
        history.clear()
        history = mutableListOf()
        currentActionPosition = 0
    }

    // handle line
    fun addLineWithoutAddHistory(line: Object){
        lstLine.add(line)
    }
    fun addLine(line: Object){
        lstLine.add(line)
        history.add(HistoryAction(
            HistoryAction.HistoryActionType.ADD_LINE,
            line.getVertices()[0],
            line.getVertices()[1]
        ))
        currentActionPosition++
        history = history.subList(0,currentActionPosition)
        if(currentActionPosition == history.size){
            updateUndoAndRedoCallback!!.disableRedo()
        }
        if(currentActionPosition == 1){
            updateUndoAndRedoCallback!!.enableUndo()
        }
    }
    fun removeLineWithId(id: String){
        var deleteLine : Object? = null
        for(line in lstLine)
            if(line.getId() == id){
                deleteLine = line
                break
            }
        if(deleteLine != null)
            lstLine.remove(deleteLine)
    }
    fun removeLine(line: Object){
        history.add(HistoryAction(
            HistoryAction.HistoryActionType.DELETE_LINE,
            line.getVertices()[0],
            line.getVertices()[1]
        ))
        lstLine.remove(line)
        currentActionPosition = history.size
        updateUndoAndRedoCallback!!.disableRedo()
        if(currentActionPosition == 1){
            updateUndoAndRedoCallback!!.enableUndo()
        }
    }

    fun isHistoryAllMoveAction() : Boolean{
        for(item in history){
            if(item.getAction() != HistoryAction.HistoryActionType.MOVE
                && item.getAction() != HistoryAction.HistoryActionType.MOVE_LINE){
                return false
            }
        }
        return true
    }
    /**GETTER AN SETTER**/
    @JvmName("functionGetScreenWidthOfKotlin")
    fun getScreenWidth(): Float {return  this.screenWidth}
    @JvmName("functionGetScreenHeightOfKotlin")
    fun getScreenHeight() : Float{return  this.screenHeight}
    @JvmName("functionGetListPointOfKotlin")
    fun getListPoint() : MutableList<Point2D>{return this.lstPoint}
    @JvmName("functionUpdateCallback")
    fun setUpdateUndoAndRedoCallback(context: OnUpdateUndoAndRedo){
        this.updateUndoAndRedoCallback  = context
        if(history.size == 0){
            updateUndoAndRedoCallback!!.disableUndo()
            updateUndoAndRedoCallback!!.disableRedo()
        }
        else {
            if(currentActionPosition == 0){
                updateUndoAndRedoCallback!!.disableUndo()
            }
            if(currentActionPosition == history.size){
                updateUndoAndRedoCallback!!.disableRedo()
            }
        }
    }
    @JvmName("functionGetListLine")
    fun getListLine(): MutableList<Object> {return this.lstLine}
    @JvmName("functionSetBorderColorOfKotlin")
    fun setBorderColor(color: Int) {this.borderColor = color}
    @JvmName("functionSetFillColorOfKotlin")
    fun setFillColor(color: Int) {this.fillColor = color}
    @JvmName("functionGetFillColorOfKotlin")
    fun getFillColor() : Int { return this.fillColor }
    @JvmName("functionGetBorderColorOfKotlin")
    fun getBorderColor() : Int{ return this.borderColor}

}