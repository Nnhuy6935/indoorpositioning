package com.example.indoorpositioning.Interface

import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.Model.Point2D

// interface này dùng cho GLSurfaceView yêu cầu MainActivity start một intent tới Edit Activity để thực hiện chỉnh sửa hình ảnh
interface OnIntentToEditActivity {
    fun onStartIntentToEditActivity(vertices: MutableList<Point2D>, type: ObjectType, fillColor: Int, borderColor: Int)
}