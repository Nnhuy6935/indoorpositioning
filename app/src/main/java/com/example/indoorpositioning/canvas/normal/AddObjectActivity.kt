package com.example.indoorpositioning.canvas.normal

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.indoorpositioning.Interface.OnUpdateUndoAndRedo
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.R
import com.example.indoorpositioning.canvas.BaseKnowledge
import com.google.android.material.bottomsheet.BottomSheetDialog
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import kotlin.collections.ArrayList

class AddObjectActivity : AppCompatActivity(), View.OnClickListener, OnUpdateUndoAndRedo {
    lateinit var btnBack : ImageButton
    lateinit var btnUndo : ImageButton
    lateinit var btnRedo : ImageButton
    lateinit var btnAdd : Button
    lateinit var btnCustom: ImageButton
    lateinit var renderView : ObjectSurfaceView
    var fillColor : Int = Color.BLACK
    var borderColor : Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_object_layout)
        btnBack = findViewById(R.id.btnBack)
        btnUndo = findViewById(R.id.btnObjectUndo)
        btnRedo = findViewById(R.id.btnObjectRedo)
        btnAdd = findViewById(R.id.btnAdd)
        renderView = findViewById(R.id.addView)
        btnCustom = findViewById(R.id.btnCustom)
        renderView.setUpdateUndoAndRedoCallback(this)
        renderView.getSurfaceRenderer()!!.setBorderColor(borderColor)
        renderView.getSurfaceRenderer()!!.setFillColor(fillColor)


        val intent : Intent = intent
        val data = intent.extras?.getParcelableArrayList<Point2D>("data")
        val fill = intent.extras?.getInt("fillColor")
        val border = intent.extras?.getInt("borderColor")
        if(fill != null)
            fillColor = fill
        if(border != null)
            borderColor = border
        updateColorToRender()
        if (data != null) {
            for(point in data){
                point.setBoudingBox()
                renderView.getSurfaceRenderer()!!.addPoint(point)
            }
            renderView.setGroupId(data.get(0).getGroupId())
        }


        renderView.cleanHistory()
        disableUndo()
        disableRedo()
        btnBack.setOnClickListener(this)
        btnAdd.setOnClickListener(this)
        btnUndo.setOnClickListener(this)
        btnRedo.setOnClickListener(this)
        btnCustom.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        if(view?.id == R.id.btnBack){
            setResult(RESULT_CANCELED)
            finish()
        }
        if(view?.id == R.id.btnAdd){
            var output : List<Point2D> = renderView.getSurfaceRenderer()!!.getListPoint()
            if(output == null )
                output = mutableListOf()
            var vertices :MutableList<Point2D>
            if(output.size > 3){
                vertices = defineObjectVertices()
            }
            else vertices = output.toMutableList()

            var data : ArrayList<Point2D> = ArrayList(vertices)
            var resultIntent = Intent()
            var args = Bundle()
            args.putParcelableArrayList("data",data)
            args.putString("groupId",renderView.getGroupId())
            args.putInt("borderColor",borderColor)
            args.putInt("fillColor",fillColor)
            resultIntent.putExtras(args)
            setResult(RESULT_OK,resultIntent)
            finish()
        }
        if(view?.id == R.id.btnObjectUndo){
            renderView.handleActionUndo()
        }
        if(view?.id == R.id.btnObjectRedo){
            renderView.handleActionRedo()
        }
        if(view?.id == R.id.btnCustom){
            val dialog = BottomSheetDialog(this)
            val view = LayoutInflater.from(this).inflate(R.layout.object_bottom_sheet, null)
            val btnClose = view.findViewById<ImageButton>(R.id.btnCloseCustomTab)
            val btnPickBorderColor = view.findViewById<Button>(R.id.btnPickBorder)
            val btnPickFillColor = view.findViewById<Button>(R.id.btnPickFillColor)
            val resultBorderColor = view.findViewById<View>(R.id.resultPickBorder)
            val resultFillColor = view.findViewById<View>(R.id.resultPickFill)
            resultFillColor.setBackgroundColor(fillColor)
            resultBorderColor.setBackgroundColor(borderColor)


            btnClose.setOnClickListener {
                dialog.dismiss()
            }
            btnPickBorderColor.setOnClickListener {
                openColorPickerDialog(borderColor, resultBorderColor, PickColorObject.BORDER)
            }
            btnPickFillColor.setOnClickListener {
                openColorPickerDialog(fillColor, resultFillColor, PickColorObject.FILL)
            }
            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.show()
        }
    }

    fun openColorPickerDialog(originColor: Int, result: View , itemSetColor: PickColorObject) {
        val colorPickerDialog = AmbilWarnaDialog(this, originColor,
            object: OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    Log.d("== CANCEL PICK COLOR", "cancel")
                    result.setBackgroundColor(originColor)
                }
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    result.setBackgroundColor(color)
                    if(itemSetColor == PickColorObject.FILL){
                        fillColor = color
                    }else if(itemSetColor == PickColorObject.BORDER){
                        borderColor = color
                    }
                    updateColorToRender()
                }
            })
        colorPickerDialog.show()
    }
    fun updateColorToRender(){
        renderView.getSurfaceRenderer()!!.setFillColor(fillColor)
        renderView.getSurfaceRenderer()!!.setBorderColor(borderColor)
        renderView.requestRender()
    }

    /** REFERENCES: Graham Scan Algorithm
     https://en.wikipedia.org/wiki/Graham_scan **/
    fun defineObjectVertices() : MutableList<Point2D>{
        // sắp xếp các điểm tăng dần theo x nếu x bằng nhau thì xếp tăng dần theo y
        var input = renderView.getSurfaceRenderer()!!.getListPoint().sortedWith(compareBy<Point2D> {it.getX()}.thenBy { it.getY() })
        val firstBound: MutableList<Point2D> = mutableListOf()
        // xây dựng bao dưới
        for(point in input){
            while(firstBound.size >= 2 && BaseKnowledge().crossProduct(firstBound[firstBound.size - 2], firstBound[firstBound.size - 1], point) <= 0){
                firstBound.removeAt(firstBound.size - 1)
            }
            firstBound.add(point)
        }
        // xây dựng bao trên
        val secondBound: MutableList<Point2D> = mutableListOf()
        for (point in input) {
            while (secondBound.size >= 2 && BaseKnowledge().crossProduct(secondBound[secondBound.size - 2], secondBound[secondBound.size - 1], point) > 0) {
                secondBound.removeAt(secondBound.size - 1)
            }
            secondBound.add(point)
        }
        // Loại bỏ điểm đầu của secondBound vì nó đã có trong firstBound
        if (secondBound.isNotEmpty()) {
            secondBound.removeAt(0)
        }
        //loại bỏ điểm cuối của secondBound vì đã có trong firstBound
        if(secondBound.isNotEmpty())
            secondBound.removeAt(secondBound.size-1)
        // Kết hợp hai phần bao
        firstBound.addAll(secondBound.reversed())
        return firstBound

    }

    override fun enableUndo() {
        btnUndo.isEnabled = true
        btnUndo.setImageResource(R.drawable.baseline_undo_24)
    }
    override fun enableRedo() {
        btnRedo.isEnabled = true
        btnRedo.setImageResource(R.drawable.baseline_redo_24)
    }
    override fun disableUndo() {
        btnUndo.isEnabled = false
        btnUndo.setImageResource(R.drawable.baseline_undo_24_disable)
    }
    override fun disableRedo() {
        btnRedo.isEnabled = false
        btnRedo.setImageResource(R.drawable.baseline_redo_24_disable)
    }

    enum class PickColorObject{
        FILL, BORDER
    }
}