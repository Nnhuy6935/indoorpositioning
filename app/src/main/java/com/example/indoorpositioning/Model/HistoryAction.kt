package com.example.indoorpositioning.Model

class HistoryAction(type: HistoryActionType, main: Point2D, related: Point2D?) {
    private var action: HistoryActionType
    private var mainObject: Point2D
    private var relatedObject: Point2D?

    private var transitionVector : Vector? = null
    private var deletedPoints : MutableList<Point2D>? = null

    init{
        action = type
        mainObject = main
        relatedObject = related
    }

    enum class HistoryActionType{
        ADD, DELETE, MOVE, DELETE_LINE, ADD_LINE, MOVE_LINE
    }
    /**GETTER AND SETTTER **/
    fun getAction() : HistoryActionType {return  this.action}
    fun setAction(type : HistoryActionType) { this.action = type}
    fun getMainObject() : Point2D {return this.mainObject}
    fun setMainObject(obj: Point2D) {this.mainObject = obj }
    fun getRelatedObject() : Point2D? {return this.relatedObject}
    fun setRelatedObject(value : Point2D?) {this.relatedObject = value}
    fun getTransitionVector() : Vector?  {return this.transitionVector}
    fun setTransitionVector(vector: Vector?){this.transitionVector = vector}
    fun getDeletedPoints(): MutableList<Point2D>? {return this.deletedPoints}
    fun setDeletedPoints(value: MutableList<Point2D>?){this.deletedPoints = value}
}