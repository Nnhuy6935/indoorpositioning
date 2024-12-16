package com.example.indoorpositioning.Interface

import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.map.MainActivity

interface OnUpdateRenderObject {
    fun onUpdate(type: MainActivity.UpdateType, data : MutableList<Point2D>, groupId: String, objType: ObjectType, fillColor: Int?, borderColor: Int?)
}