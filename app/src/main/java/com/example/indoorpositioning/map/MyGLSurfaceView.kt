package com.example.indoorpositioning.map

import android.content.Context
import android.content.DialogInterface
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.indoorpositioning.Helputil.MathSupport
import com.example.indoorpositioning.Interface.OnIntentToEditActivity
import com.example.indoorpositioning.Interface.OnUpdateColor
import com.example.indoorpositioning.Interface.OnUpdateRenderObject
import com.example.indoorpositioning.Model.Object
import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.R
import com.example.indoorpositioning.adapter.VerticeAdapter
import com.example.indoorpositioning.canvas.normal.AddObjectActivity.PickColorObject
import com.google.android.material.bottomsheet.BottomSheetDialog
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

class MyGLSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context,attrs),
    OnUpdateRenderObject, OnUpdateColor {
    var renderer:MyGLRenderer
    var isMoving : Boolean = false
    var objClicked : Object? = null
    var startX : Float = 0f
    var startY : Float = 0f
    var mainCtx : Context = context
    lateinit var startActivityCallback : OnIntentToEditActivity
    var onUpdateColor: OnUpdateColor
    var currentHandleObject : Object? = null

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer()
        setRenderer(renderer)
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        onUpdateColor = this
    }
    fun setCallback(callback: OnIntentToEditActivity){
        this.startActivityCallback = callback
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val x : Float = event?.getX() ?:  0.0f
        val y : Float = event?.getY() ?: 0.0f

        var normalizedX : Float = (x * 2.0f / renderer.getScreenWidth() - 1.0f )
        var normalizedY : Float = -(y * 2.0f/ renderer.getScreeHeight()) + 1.0f
        normalizedX = MathSupport().roundTo4DecimalPlaces(normalizedX)
        normalizedY = MathSupport().roundTo4DecimalPlaces(normalizedY)

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                handleActionDown(normalizedX,normalizedY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                handleActionMove(normalizedX,normalizedY)
                return true
            }
            MotionEvent.ACTION_UP -> {
                handleActionUp(normalizedX,normalizedY)
                return true
            }
        }
        return false
    }

    fun handleActionDown(x: Float, y: Float){
        startX = x
        startY = y
        objClicked = isClickOnAnyObject(x,y)
        if(objClicked == null){
            Toast.makeText(mainCtx, "Nothing", Toast.LENGTH_SHORT).show()
        }

    }
    fun handleActionMove(x: Float, y: Float){
        var transitionX = x - startX
        var transitionY = y - startY
        startX = x
        startY = y
        isMoving = true
        if(objClicked != null){
            for(vertex in objClicked?.getVertices()!!){
                vertex.setX(MathSupport().roundTo4DecimalPlaces(vertex.getX() + transitionX))
                vertex.setY(MathSupport().roundTo4DecimalPlaces(vertex.getY() + transitionY))
                vertex.setBoudingBox()
            }
            objClicked!!.setBoundingBox()
        }else{
            //todo: nothing
        }
        requestRender()
    }
    fun handleActionUp(x: Float, y: Float){
        if(objClicked == null) return
        if(isMoving == false){      // show dialog to edit object or remove object
                var builder : AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setMessage("Which action you want to do with this ${objClicked?.getType().toString()}?")
                builder.setCancelable(true)
                builder.setPositiveButton(
                    "Delete",
                    DialogInterface.OnClickListener { dialog, id ->
                        handleRemoveObject()
                        objClicked = null
                        requestRender()
                        dialog.cancel()
                    })
                builder.setNegativeButton(
                    "Edit",
                    DialogInterface.OnClickListener{dialog, id ->
                       /**gọi callback tới main activity để thực hiện start intent **/
                        startActivityCallback.onStartIntentToEditActivity(objClicked?.getVertices()?.toMutableList()!!, objClicked!!.getType(), objClicked!!.getFillColorWithInt(),objClicked!!.getBorderColorWithInt())
                        dialog.cancel()
                    })
                builder.setNeutralButton(
                    "Detail",
                    DialogInterface.OnClickListener{dialog, id ->
                        showDetailInformationObject(objClicked!!)
                        objClicked = null
                        dialog.cancel()
                    }
                )
                var alertDialog = builder.create()
                alertDialog.show()
        }
        startX = 0f
        startY = 0f
        isMoving = false
    }

    fun showDetailInformationObject(obj: Object){
        currentHandleObject = null
        currentHandleObject = obj
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.main_bottom_sheet, null)
        val btnClose = view.findViewById<ImageButton>(R.id.btnCloseCustomTab)
        val txtId = view.findViewById<TextView>(R.id.txtObjId)
        val txtType = view.findViewById<TextView>(R.id.txtObjType)
        val borderColor = view.findViewById<View>(R.id.objBorderColor)
        val fillColor = view.findViewById<View>(R.id.objFillColor)
        val listVertices = view.findViewById<ListView>(R.id.listVertices)
        val txtChangeBorderColor = view.findViewById<TextView>(R.id.txtChangeBorderColor)
        val txtChangeFillColor = view.findViewById<TextView>(R.id.txtChangeFillColor)

        txtId.text = "${obj.getId()}"
        txtType.text = "${obj.getType()}"
        borderColor.setBackgroundColor(obj.getBorderColorWithInt())
        fillColor.setBackgroundColor(obj.getFillColorWithInt())

        var adapter = VerticeAdapter()
        adapter.setData(obj.getVertices().toMutableList())
        listVertices.adapter = adapter

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        txtChangeFillColor.setOnClickListener{
            openColorPickerDialog(obj.getFillColorWithInt(), fillColor, PickColorObject.FILL)
        }
        txtChangeBorderColor.setOnClickListener{
            openColorPickerDialog(obj.getBorderColorWithInt(), borderColor,  PickColorObject.BORDER)
        }

        requestRender()
        dialog.setCancelable(false)
        dialog.setContentView(view)
        dialog.show()
    }
    fun isClickOnAnyObject(x : Float, y : Float) : Object?{
        for(obj in renderer.getListObject()){
            if(obj.isClickOnObject(x,y))
                return obj
        }
        return null
    }
    fun handleRemoveObject(){  renderer.removeObject(objClicked!!) }

    fun openColorPickerDialog(originColor: Int, result: View , itemSetColor: PickColorObject)  {
        val colorPickerDialog = AmbilWarnaDialog(mainCtx, originColor,
            object: OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    Log.d("== CANCEL PICK COLOR", "cancel")
                    return
                }
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    result.setBackgroundColor(color)
                    if(itemSetColor == PickColorObject.FILL){
                        onUpdateColor.updateFillColor(color)
                    }else if(itemSetColor == PickColorObject.BORDER){
                        onUpdateColor.updateBorderColor(color)
                    }
                }
            })
        colorPickerDialog.show()
    }

    override fun onUpdate(
        type: MainActivity.UpdateType,
        data: MutableList<Point2D>,
        groupId: String,
        objType: ObjectType,
        fillColor: Int?,
        borderColor: Int?
    ) {
        if(type == MainActivity.UpdateType.ADD){
            var newObj = createObject(data, objType)
            if (newObj != null) {
                if(borderColor != null )
                    newObj.setBorderColorWithInt(borderColor)
                if(fillColor != null )
                    newObj.setFillColorWithInt(fillColor)
                renderer.addNewObject(newObj)
            }
        }
        else if(type == MainActivity.UpdateType.EDIT){
            for(obj in renderer.getListObject()){
                if(obj.getId() == groupId){
                    if(objType != ObjectType.CURVE)
                        obj.setType(objType)
                    obj.setPointVertices(data)
                    obj.setBorderColorWithInt(borderColor!!)
                    obj.setFillColorWithInt(fillColor!!)
                }
            }
        }
        else if(type == MainActivity.UpdateType.DELETE){
            for(obj in renderer.getListObject()){
                if(obj.getId() == groupId){
                    renderer.removeObject(obj)
                }
            }
        }
    }

    fun createObject(vertices: MutableList<Point2D>, objType: ObjectType): Object? {
        if(vertices.size <= 0)
            return  null
        var obj = Object(objType, vertices, vertices[0].getGroupId())
        return obj
    }

    override fun updateFillColor(newColor: Int) {
        currentHandleObject!!.setFillColorWithInt(newColor)
        requestRender()
    }

    override fun updateBorderColor(newColor: Int) {
        currentHandleObject!!.setBorderColorWithInt(newColor)
        requestRender()
    }
}