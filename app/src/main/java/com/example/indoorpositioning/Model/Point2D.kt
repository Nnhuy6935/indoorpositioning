package com.example.indoorpositioning.Model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

class Point2D(x: Float, y: Float, id: String, groupId: String) : Parcelable {
    private var X : Float = 0.0f
    private var Y : Float = 0.0f
    private var id: String = id
    private var objectId : String = groupId
    private var boundingBox: BoundingBox = BoundingBox()
    private var isBelongCurve : Boolean




    init {
        this.X = x
        this.Y = y
        this.id = id
        this.objectId = groupId
        this.isBelongCurve = false
        setBoudingBox()
    }
    fun updatePoint2D(newPoint2D: Point2D){
        this.X = newPoint2D.getX()
        this.Y = newPoint2D.getY()
        setBoudingBox()
    }

    /**GETTER AND SETTER**/
    fun getX() : Float {return  this.X}
    fun getY() : Float {return  this.Y}
    fun setX(input : Float ) {
        this.X = input
        setBoudingBox()
    }
    fun setY(input : Float ) {
        this.Y = input
        setBoudingBox()
    }
    fun getGroupId(): String{return objectId;}
    fun setGroupId(id: String){objectId = id}
    fun equal(id: String): Boolean{
        return this.id == id
    }
    fun getBoundingBox() : BoundingBox{return boundingBox}
    fun setBoudingBox(){
        boundingBox.setMaxX(this.X+0.01f)
        boundingBox.setMaxY(this.Y+0.01f)
        boundingBox.setMinX(this.X-0.01f)
        boundingBox.setMinY(this.Y-0.01f)
    }
    fun getId(): String{ return id}
    fun setId(value: String) {this.id = value}
    fun isClickOnPoint(x: Float, y:Float) : Boolean{
        return boundingBox.isUnderBoundingBox(x,y)
    }
    fun getIsBelongCurve() : Boolean{return this.isBelongCurve}
    fun setIsBelongCurve(value: Boolean) {this.isBelongCurve = value}

    /**parcelable is for transform data between activities**/
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readString().toString(),
        parcel.readString().toString(),
    ) {
        parcel.readBoolean()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloat(X)
        parcel.writeFloat(Y)
        parcel.writeString(id)
        parcel.writeString(objectId)
        parcel.writeBoolean(isBelongCurve)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<Point2D> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Point2D {
            val x = parcel.readFloat()
            val y = parcel.readFloat()
            val id = parcel.readString() ?: ""
            val objectId = parcel.readString() ?: "" // Khôi phục objectId
            val isBelongCur = parcel.readBoolean() ?: false
            val point2D = Point2D(x, y,id, objectId)
            point2D.setId(id)
            point2D.objectId = objectId // Gán objectId đã khôi phục
            point2D.setIsBelongCurve(isBelongCur)
            return point2D
        }

        override fun newArray(size: Int): Array<Point2D?> {
            return arrayOfNulls(size)
        }
    }


}